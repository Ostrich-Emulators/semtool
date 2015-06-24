/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.util.MultiMap;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author ryan
 */
public class LowMemXlsReader {

	private static final Logger log = Logger.getLogger( LowMemXlsReader.class );

	private final Map<String, String> sheetNameIdLkp;
	private final ArrayList< String> sharedStrings;
	private final XSSFReader reader;
	private final OPCPackage pkg;

	public LowMemXlsReader( File filename ) throws IOException, OpenXML4JException, SAXException {
		this( new BufferedInputStream( new FileInputStream( filename ) ) );
	}

	public LowMemXlsReader( String filename ) throws IOException, OpenXML4JException, SAXException {
		this( new File( filename ) );
	}

	public LowMemXlsReader( InputStream stream ) throws IOException, OpenXML4JException, SAXException {
		pkg = OPCPackage.open( stream );
		reader = new XSSFReader( pkg );

		sheetNameIdLkp = readSheetInfo( reader );
		sharedStrings = readSharedStrings( reader );
	}

	/**
	 * Releases resources used by this reader
	 */
	public void release() {
		try {
			pkg.close();
			sheetNameIdLkp.clear();
			sharedStrings.clear();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	public List<Value[]> read( String sheetname ) throws SAXException, IOException {
		XMLReader parser = XMLReaderFactory.createXMLReader();
		SheetHandler handler = new SheetHandler( sharedStrings );
		parser.setContentHandler( handler );

		try ( InputStream sheet2 = reader.getSheet( sheetNameIdLkp.get( sheetname ) ) ) {
			InputSource sheetSource = new InputSource( sheet2 );
			parser.parse( sheetSource );
		}
		catch ( InvalidFormatException ife ) {
			log.error( ife, ife );
		}

		MultiMap<Integer, Value> data = handler.getData();
		// now reorder the map so we have sequential keys, and arrays instead of lists
		// for that, we must find our biggest key, and loop
		int maxkey = Integer.MIN_VALUE;
		for ( int key : data.keySet() ) {
			if ( key > maxkey ) {
				maxkey = key;
			}
		}

		List<Value[]> rows = new ArrayList<>();
		for ( int i = 0; i <= maxkey; i++ ) {
			if ( data.containsKey( i ) ) {
				rows.add( data.get( i ).toArray( new Value[0] ) );
			}
		}

		return rows;
	}

	public Collection<String> getSheetNames() {
		return sheetNameIdLkp.keySet();
	}

	/**
	 * Gets sheet name-to-id mapping
	 *
	 * @param r
	 * @return
	 */
	private Map<String, String> readSheetInfo( XSSFReader r ) {
		Map<String, String> map = new LinkedHashMap<>();

		try ( InputStream is = r.getWorkbookData() ) {
			SAXReader sax = new SAXReader();
			Document doc = sax.read( is );

			Namespace ns = new Namespace( "r",
					"http://schemas.openxmlformats.org/officeDocument/2006/relationships" );

			Element sheets = doc.getRootElement().element( "sheets" );
			for ( Object sheet : sheets.elements( "sheet" ) ) {
				Element e = Element.class.cast( sheet );
				String name = e.attributeValue( "name" );
				String id = e.attributeValue( new QName( "id", ns ) );
				map.put( name, id );
			}
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		return map;
	}

	private ArrayList<String> readSharedStrings( XSSFReader r ) {
		ArrayList<String> map = new ArrayList<>();

		try ( InputStream is = r.getSharedStringsData() ) {
			XMLReader parser = XMLReaderFactory.createXMLReader();// "org.apache.xerces.parsers.SAXParser" );
			ContentHandler handler = new DefaultHandler() {
				String content;
				boolean reading = false;

				@Override
				public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException {
					reading = ( "t".equals( localName ) );
					content = "";
				}

				@Override
				public void endElement( String uri, String localName, String qName ) throws SAXException {
					if ( reading ) {
						map.add( content );
						reading = false;
					}
				}

				@Override
				public void characters( char[] ch, int start, int length ) throws SAXException {
					if ( reading ) {
						content += new String( ch, start, length );
					}
				}
			};
			parser.setContentHandler( handler );
			parser.parse( new InputSource( is ) );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		return map;
	}

	private static class SheetHandler extends DefaultHandler {

		private final ArrayList<String> sst;
		private final MultiMap<Integer, Value> data = new MultiMap<>();
		private final ValueFactory vf = new ValueFactoryImpl();
		private String lastContents;
		private String valtype;
		private boolean reading = false;
		private int currentrow;
		private int currentcol;
		private final Map<Integer, Value> currentrowdata = new LinkedHashMap<>();

		private SheetHandler( ArrayList<String> sst ) {
			this.sst = sst;
		}

		public MultiMap<Integer, Value> getData() {
			return data;
		}

		public static int getColNum( String colname ) {
			int sum = 0;

			for ( int i = 0; i < colname.length(); i++ ) {
				sum *= 26;
				sum += ( colname.charAt( i ) - 'A' + 1 );
			}

			return sum;
		}

		@Override
		public void startElement( String uri, String localName, String name,
				Attributes attributes ) throws SAXException {
			// c => cell
			if ( "c".equals( name ) ) {
				valtype = attributes.getValue( "t" );
				String colname = attributes.getValue( "r" );
				currentcol = getColNum( colname.substring( 0,
						colname.lastIndexOf( Integer.toString( currentrow ) ) ) );
			}
			else if ( "row".equals( name ) ) {
				currentrow = Integer.parseInt( attributes.getValue( "r" ) );
				currentrowdata.clear();
			}
			else if ( "v".equals( name ) ) {
				reading = true;
				lastContents = "";
			}
		}

		@Override
		public void endElement( String uri, String localName, String name )
				throws SAXException {

//			log.debug( "end: " + localName + "..." + name + "..." + uri );
			//		log.debug( "I am " + ( reading ? "" : "NOT" ) + " reading" );
			if ( reading ) {
				// If we've fully read the data, add it to our row mapping
				Value val = null;
				if ( "s".equals( valtype ) ) {
					int idx = Integer.parseInt( lastContents );
					val = vf.createLiteral( sst.get( idx ) );
				}
				else {
					val = vf.createLiteral( lastContents );
				}

				currentrowdata.put( currentcol, val );

				reading = false;
			}
			else if ( "row".equals( name ) ) {
				// convert our current column mapping to a list
				data.put( currentrow, listify( currentrowdata ) );
			}
		}

		private List<Value> listify( Map<Integer, Value> map ) {
			List<Value> vals = new ArrayList<>();
			// our "currentcol" variable is the last column we saw, so it's the biggest
			for ( int i = 1; i <= currentcol; i++ ) {
				vals.add( map.containsKey( i ) ? map.get( i ) : null );
			}

			return vals;
		}

		@Override
		public void characters( char[] ch, int start, int length )
				throws SAXException {
			if ( reading ) {
				lastContents += new String( ch, start, length );
			}
		}
	}
}
