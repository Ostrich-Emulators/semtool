/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
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

	private final LinkedHashMap<String, String> sheetIdNameLkp = new LinkedHashMap<>();
	private final List<String> sharedStringsLkp = new ArrayList<>();

	public void processOneSheet( File filename ) throws IOException, OpenXML4JException, SAXException {
		OPCPackage pkg = OPCPackage.open( filename );
		XSSFReader r = new XSSFReader( pkg );

		resetSheetsInfo( r );
		resetSharedStrings( r );

		XMLReader parser = fetchSheetParser();

		try ( InputStream sheet2 = r.getSheet( "rId2" ) ) {
			InputSource sheetSource = new InputSource( sheet2 );
			parser.parse( sheetSource );
		}
	}

	public void processAllSheets( File filename ) throws IOException, OpenXML4JException, SAXException {
		OPCPackage pkg = OPCPackage.open( filename );
		XSSFReader r = new XSSFReader( pkg );
		resetSheetsInfo( r );
		resetSharedStrings( r );

		XMLReader parser = fetchSheetParser();

		for ( Map.Entry<String, String> en : sheetIdNameLkp.entrySet() ) {
			try ( InputStream sheet = r.getSheet( en.getKey() ) ) {
				log.debug( "Processing new sheet: " + en.getValue() );
				InputSource sheetSource = new InputSource( sheet );
				parser.parse( sheetSource );
			}
			log.debug( "" );
		}
	}

	public XMLReader fetchSheetParser() throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader();// "org.apache.xerces.parsers.SAXParser" );
		ContentHandler handler = new SheetHandler( sharedStringsLkp );
		parser.setContentHandler( handler );
		return parser;
	}

	private void resetSheetsInfo( XSSFReader r ) {
		sheetIdNameLkp.clear();

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
				sheetIdNameLkp.put( id, name );
			}
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	private void resetSharedStrings( XSSFReader r ) {
		sharedStringsLkp.clear();

		try ( InputStream is = r.getSharedStringsData() ) {
			XMLReader parser = XMLReaderFactory.createXMLReader();// "org.apache.xerces.parsers.SAXParser" );
			ContentHandler handler = new DefaultHandler() {
				String content;
				boolean reading = false;
				int count = 0;
				int depth = 0;

				@Override
				public void startDocument() throws SAXException {
					super.startDocument();
					count = 0;
					depth = 0;
				}

				@Override
				public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException {
					reading = ( "t".equals( localName ) );
					depth++;
					content = "";
				}

				@Override
				public void endElement( String uri, String localName, String qName ) throws SAXException {
					if ( reading ) {
						sharedStringsLkp.add( content );
						reading = false;
					}
					depth--;
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

//		try ( InputStream is = r.getSharedStringsData() ) {
//			SAXReader sax = new SAXReader();
//			Document doc = sax.read( is );
//
//			Namespace ns = new Namespace( "r",
//					"http://schemas.openxmlformats.org/spreadsheetml/2006/main" );
//			Map<String, String> namespaces = new HashMap<>();
//			namespaces.put( "r", ns.getURI() );
//
//			XPath xpath = new Dom4jXPath( "//r:t" );
//			xpath.setNamespaceContext( new SimpleNamespaceContext( namespaces ) );
//
//			for( Object t : xpath.selectNodes( doc ) ){
//				Element e = Element.class.cast( t );
//				
//				sharedStringsLkp.put( count++, e.getTextTrim() );
//			}
//		}
//		catch ( Exception e ) {
//			log.error( e, e );
//		}
		// the shared strings is really just an ordered list of strings,
		// so we'll skip all the formality of XML, and just parse directly		
//		try ( BufferedReader rdr = new BufferedReader( new InputStreamReader( r.getSharedStringsData() ) ) ) {
//			String line = null;
//			while ( null != ( line = rdr.readLine() ) ) {
//				for ( String piece : line.split( "</t>" ) ) {
//					int stop = piece.( "</t>" );
//					if ( stop >= 0 ) {
//						sharedStringsLkp.put( count++, piece.substring( 0, stop ) );
//					}
//				}
//			}
//		}
//		catch ( Exception e ) {
//			log.error( e, e );
//		}
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private static class SheetHandler extends DefaultHandler {

		private final List<String> sst;
		private String lastContents;
		private boolean nextIsString;

		private SheetHandler( List<String> sst ) {
			this.sst = sst;
		}

		@Override
		public void startElement( String uri, String localName, String name,
				Attributes attributes ) throws SAXException {
			// c => cell
			if ( name.equals( "c" ) ) {
				// Print the cell reference
				System.out.print( attributes.getValue( "r" ) + " - " );
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue( "t" );
				if ( cellType != null && cellType.equals( "s" ) ) {
					nextIsString = true;
				}
				else {
					nextIsString = false;
				}
			}

			// Clear contents cache
			lastContents = "";
		}

		@Override
		public void endElement( String uri, String localName, String name )
				throws SAXException {
			// Process the last contents as required.
			// Do now, as characters() may be called more than once
			if ( nextIsString ) {
				int idx = Integer.parseInt( lastContents );
				lastContents = sst.get( idx );
				nextIsString = false;
			}

			// v => contents of a cell
			// Output after we've seen the string contents
			if ( name.equals( "v" ) ) {
				System.out.println( lastContents );
			}
		}

		@Override
		public void characters( char[] ch, int start, int length )
				throws SAXException {
			lastContents += new String( ch, start, length );
		}
	}
}
