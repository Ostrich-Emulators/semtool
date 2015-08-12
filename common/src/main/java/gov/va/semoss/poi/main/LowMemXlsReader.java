/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.Utility;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
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
 * A class to read an xlsx file and produce an ImportData instance.
 *
 * @author ryan
 */
public class LowMemXlsReader {

	private static final Logger log = Logger.getLogger( LowMemXlsReader.class );

	private final LinkedHashMap<String, String> sheetNameIdLkp;
	private final ArrayList<String> sharedStrings;
	private final XSSFReader reader;
	private final OPCPackage pkg;
	private final StylesTable styles;

	public LowMemXlsReader( File filename ) throws IOException {
		this( new BufferedInputStream( new FileInputStream( filename ) ) );
	}

	public LowMemXlsReader( String filename ) throws IOException {
		this( new File( filename ) );
	}

	public LowMemXlsReader( InputStream stream ) throws IOException {
		try {
			pkg = OPCPackage.open( stream );
			reader = new XSSFReader( pkg );

			styles = reader.getStylesTable();

			sheetNameIdLkp = readSheetInfo( reader );
			sharedStrings = readSharedStrings( reader );
		}
		catch ( OpenXML4JException e ) {
			throw new IOException( "unexpected error" + e.getLocalizedMessage(), e );
		}
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

	/**
	 * Gets the sheet types. If a loader tab exists, only those tabs will be
	 * checked (and the metadata tab will be verified against what the loader tab
	 * says).
	 *
	 *
	 * @return
	 * @throws ImportValidationException
	 */
	public Map<String, SheetType> getSheetTypes() throws ImportValidationException {
		Map<String, SheetType> types = new HashMap<>();
		Set<String> tabsToCheck = new HashSet<>();
		boolean checktypes = false;

		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();

			if ( sheetNameIdLkp.containsKey( "Loader" ) ) {
				checktypes = true;
				try ( InputStream is = reader.getSheet( sheetNameIdLkp.get( "Loader" ) ) ) {

					LoaderTabXmlHandler handler = new LoaderTabXmlHandler( sharedStrings );
					parser.setContentHandler( handler );

					InputSource sheetSource = new InputSource( is );
					parser.parse( sheetSource );

					types.putAll( handler.getSheetTypes() );
					tabsToCheck.addAll( types.keySet() );

					if ( tabsToCheck.isEmpty() ) {
						throw new ImportValidationException( ErrorType.MISSING_DATA,
								"No data to process" );
					}
				}
			}
			else {
				tabsToCheck.addAll( sheetNameIdLkp.keySet() );
			}

			// now check the actual sheets
			SheetTypeXmlHandler handler = new SheetTypeXmlHandler( sharedStrings );
			parser.setContentHandler( handler );

			boolean seenMetadata = false; // we can only have 1 metadata tab
			for ( String sheetname : tabsToCheck ) {
				if ( !sheetNameIdLkp.containsKey( sheetname ) ) {
					throw new ImportValidationException( ErrorType.MISSING_DATA,
							"Missing sheet: " + sheetname );
				}

				try ( InputStream is = reader.getSheet( sheetNameIdLkp.get( sheetname ) ) ) {
					InputSource sheetSource = new InputSource( is );
					parser.parse( sheetSource );

					SheetType sheettype = handler.getSheetType();
					boolean sheetsaysM = ( SheetType.METADATA == sheettype );

					if ( sheetsaysM ) {
						if ( seenMetadata ) {
							throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
									"Too many metadata tabs in loading file" );
						}
						seenMetadata = true;
					}

					SheetType loadertype = types.get( sheetname );
					if ( checktypes ) {
						if ( ( SheetType.USUAL == loadertype && sheetsaysM )
								|| SheetType.METADATA == loadertype && !sheetsaysM ) {
							// if the loader or the sheet itself says its a metadata sheet,
							// then both types must agree
							throw new ImportValidationException( ErrorType.WRONG_TABTYPE,
									"Loader Sheet data type for " + sheetname
									+ " conflicts with sheet type" );
						}
					}

					types.put( sheetname, sheettype );
				}
			}
		}
		catch ( SAXException | IOException | InvalidFormatException e ) {
			log.error( e, e );
		}

		return types;
	}

	public ImportMetadata getMetadata() throws ImportValidationException {
		ImportData id = new ImportData();
		id.getMetadata().setNamespaces( Utility.DEFAULTNAMESPACES );

		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();

			Map<String, SheetType> types = getSheetTypes();
			MultiMap<SheetType, String> mm = MultiMap.flip( types );

			// load the metadata sheet first, if we have one
			for ( String metasheet : mm.getNN( SheetType.METADATA ) ) {
				try ( InputStream is = reader.getSheet( sheetNameIdLkp.get( metasheet ) ) ) {
					MetadataTabXmlHandler handler
							= new MetadataTabXmlHandler( sharedStrings, id.getMetadata() );
					parser.setContentHandler( handler );

					InputSource sheetSource = new InputSource( is );
					parser.parse( sheetSource );

					id.setMetadata( handler.getMetadata() );
				}
			}
		}
		catch ( SAXException | InvalidFormatException | IOException ife ) {
			log.error( ife, ife );
		}

		return id.getMetadata();
	}

	public ImportData getData() throws ImportValidationException {
		ImportData id = new ImportData();
		id.getMetadata().setNamespaces( Utility.DEFAULTNAMESPACES );

		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();

			Map<String, SheetType> types = getSheetTypes();
			MultiMap<SheetType, String> mm = MultiMap.flip( types );

			// load the metadata sheet first, if we have one
			for ( String metasheet : mm.getNN( SheetType.METADATA ) ) {
				try ( InputStream is = reader.getSheet( sheetNameIdLkp.get( metasheet ) ) ) {
					MetadataTabXmlHandler handler
							= new MetadataTabXmlHandler( sharedStrings, id.getMetadata() );
					parser.setContentHandler( handler );

					InputSource sheetSource = new InputSource( is );
					parser.parse( sheetSource );

					id.setMetadata( handler.getMetadata() );
				}

				types.remove( metasheet ); // don't reprocess in the next loop								
			}

			for ( Map.Entry<String, SheetType> typeen : types.entrySet() ) {
				String sheetname = typeen.getKey();
				String sheetid = sheetNameIdLkp.get( sheetname );
				SheetType sheettype = typeen.getValue();

				try ( InputStream is = reader.getSheet( sheetid ) ) {
					if ( SheetType.NODE == sheettype || SheetType.RELATION == sheettype ) {
						LoadingSheetXmlHandler handler
								= new LoadingSheetXmlHandler( sharedStrings, styles, sheetname,
										id.getMetadata().getNamespaces() );
						parser.setContentHandler( handler );

						InputSource sheetSource = new InputSource( is );
						parser.parse( sheetSource );

						LoadingSheetData lsd = handler.getSheet();
						if ( lsd.isEmpty() ) {
							throw new ImportValidationException( ErrorType.NOT_A_LOADING_SHEET,
									"Sheet " + sheetname + " contains no loadable data" );
						}
						id.add( lsd );
					}
				}
			}
		}
		catch ( SAXException | InvalidFormatException | IOException ife ) {
			log.error( ife, ife );
		}

		if ( id.isEmpty() ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"No data to process" );
		}

		return id;
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
	private LinkedHashMap<String, String> readSheetInfo( XSSFReader r ) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();

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
			XMLReader parser = XMLReaderFactory.createXMLReader();
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
}
