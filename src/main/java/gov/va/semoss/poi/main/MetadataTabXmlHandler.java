/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.openrdf.model.impl.URIImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ryan
 */
public class MetadataTabXmlHandler extends DefaultHandler {

	private static final Logger log = Logger.getLogger( MetadataTabXmlHandler.class );
	private static final Map<String, Integer> formats = new HashMap<>();

	private final Map<Integer, String> currentrowdata = new LinkedHashMap<>();
	private final ImportMetadata metas;
	private final ArrayList<String> sst;
	private final List<String[]> triples = new ArrayList<>();
	private final Map<String, String> namespaces;

	private String lastContents;
	private boolean reading = false;
	private int rownum;
	private int colnum;
	private int celltype;
	private String datanamespace = null;
	private String schemanamespace = null;
	private String baseuri = null;

	static {
		formats.put( "s", Cell.CELL_TYPE_STRING );
		formats.put( "n", Cell.CELL_TYPE_NUMERIC );
		formats.put( "b", Cell.CELL_TYPE_BOOLEAN );
	}

	public MetadataTabXmlHandler( ArrayList<String> sst, ImportMetadata metadata ) {
		this.sst = sst;
		metas = metadata;
		namespaces = metas.getNamespaces();
	}

	public ImportMetadata getMetadata() {
		return metas;
	}

	@Override
	public void startElement( String uri, String localName, String name,
			Attributes attributes ) throws SAXException {
		if ( null != name ) {
			switch ( name ) {
				case "row":
					rownum = Integer.parseInt( attributes.getValue( "r" ) ) - 1;
					currentrowdata.clear();
					break;
				case "c": // c is a new cell
					String celltypestr = attributes.getValue( "t" );
					celltype = ( formats.containsKey( celltypestr )
							? formats.get( celltypestr ) : Cell.CELL_TYPE_BLANK );

					String colname = attributes.getValue( "r" );
					colnum = LoadingSheetXmlHandler.getColNum( colname.substring( 0,
							colname.lastIndexOf( Integer.toString( rownum + 1 ) ) ) );
					break;
				case "v": // new value for a cell
					reading = true;
					lastContents = "";
					break;
			}
		}
	}

	@Override
	public void endElement( String uri, String localName, String name )
			throws SAXException {

		if ( "row".equals( name ) ) {
			currentrowdata.remove( 0 );
			fillInMetadata( currentrowdata );
		}

		if ( reading ) {
			// If we've fully read the data, add it to our row mapping
			if ( Cell.CELL_TYPE_STRING == celltype ) {
				String strval = sst.get( Integer.parseInt( lastContents ) );
				if ( !strval.isEmpty() ) {
					currentrowdata.put( colnum, strval );
				}
			}
			else {
				currentrowdata.put( colnum, lastContents );
			}

			reading = false;
		}
	}

	@Override
	public void characters( char[] ch, int start, int length )
			throws SAXException {
		if ( reading ) {
			lastContents += new String( ch, start, length );
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		triples.clear();
		namespaces.clear();
		metas.clear();
		datanamespace = null;
		schemanamespace = null;
		baseuri = null;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		// now set the data
		if ( null != baseuri ) {
			log.debug( "setting base uri to " + baseuri );
			metas.setBase( new URIImpl( baseuri ) );
		}
		if ( null != datanamespace ) {
			log.debug( "setting data namespace to " + datanamespace );
			metas.setDataBuilder( datanamespace );
		}
		if ( null != schemanamespace ) {
			log.debug( "setting schema namespace to " + schemanamespace );
			metas.setSchemaBuilder( schemanamespace );
		}

		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			log.debug( "registering namespace: "
					+ en.getKey() + " => " + en.getValue() );
			metas.setNamespace( en.getKey(), en.getValue() );
		}

		for ( String[] triple : triples ) {
			log.debug( "adding custom triple: "
					+ triple[0] + " => " + triple[1] + " => " + triple[2] );

			try {
				metas.add( triple[0], triple[1], triple[2] );
			}
			catch ( Exception e ) {
				throw new ImportValidationException( ImportValidationException.ErrorType.INVALID_DATA, e );
			}
		}
	}

	/**
	 * Handles a row of metadata from the event parser
	 *
	 * @param rowdata
	 * @param metas
	 * @throws ImportValidationException if something is wrong
	 */
	private void fillInMetadata( Map<Integer, String> rowdata ) {

		metas.setLegacyMode( false );
		// we want to load the base uri first, data-namespace, schema-namespace,
		// prefixes, and finally triples. so read everything first, and load later

		removeComments( rowdata );

		if ( rowdata.isEmpty() ) {
			return;
		}

		String propName = rowdata.get( 1 );
		String propertyMiddleColumn = rowdata.get( 2 );
		String propValue = rowdata.get( 3 );

		if ( "@base".equals( propName ) ) {
			if ( null == baseuri ) {
				if ( propValue.startsWith( "<" ) && propValue.endsWith( ">" ) ) {
					baseuri = propValue.substring( 1, propValue.length() - 1 );
				}
				else {
					throw new ImportValidationException( ImportValidationException.ErrorType.INVALID_DATA,
							"@base value does not appear to be a URI: \"" + propValue + "\"" );
				}
			}
			else {
				throw new ImportValidationException( ImportValidationException.ErrorType.TOO_MUCH_DATA,
						"Multiple @base lines in Metadata sheet" );
			}
		}
		else if ( "@prefix".equals( propName ) ) {
			// validate that this is necessary:
			if ( !( propValue.startsWith( "<" ) && propValue.endsWith( ">" ) ) ) {
				throw new ImportValidationException( ImportValidationException.ErrorType.INVALID_DATA,
						"@prefix value does not appear to be a URI: \"" + propValue + "\"" );
			}

			propValue = propValue.substring( 1, propValue.length() - 1 );
			if ( ":schema".equals( propertyMiddleColumn ) ) {
				if ( null == schemanamespace ) {
					schemanamespace = propValue;
				}
				else {
					throw new ImportValidationException( ImportValidationException.ErrorType.TOO_MUCH_DATA,
							"Multiple :schema lines in Metadata sheet" );
				}
			}
			else if ( ":data".equals( propertyMiddleColumn ) ) {
				if ( null == datanamespace ) {
					datanamespace = propValue;
				}
				else {
					throw new ImportValidationException( ImportValidationException.ErrorType.TOO_MUCH_DATA,
							"Multiple :data lines in Metadata sheet" );
				}
			}
			else if ( ":".equals( propertyMiddleColumn ) ) {
				/*
				 * The default namespace, ":", applies to all un-prefixed data elements.
				 * Specifically setting the schema or data namespace will override the
				 * default namespace.
				 */
				if ( null == schemanamespace ) {
					schemanamespace = propValue;
				}
				if ( null == datanamespace ) {
					datanamespace = propValue;
				}
				// we may still need to set the default namespace to handle RDF exports. keep an eye on this.
			}
			else {
				namespaces.put( propertyMiddleColumn.replaceAll( ":$", "" ), propValue );
			}
		}
		else {
			if ( !propertyMiddleColumn.isEmpty() ) {
				triples.add( new String[]{ propName, propertyMiddleColumn, propValue } );
			}
		}
	}

	/**
	 * Removes any comments from the given mapping. Any column index after the
	 * comment is likewise removed
	 *
	 * @param rowdata the data to remove comments from
	 */
	private static void removeComments( Map<Integer, String> rowdata ) {
		int commentcol = Integer.MAX_VALUE;
		List<Integer> removers = new ArrayList<>();
		for ( Map.Entry<Integer, String> en : rowdata.entrySet() ) {
			int col = en.getKey();
			String val = en.getValue();

			if ( val.startsWith( "<" ) && val.endsWith( ">" ) ) {
				// this is really a URI and not a string, so skip it
				continue;
			}

			if ( col > commentcol ) {
				removers.add( col );
			}

			// if we start with a comment, we need to remove the whole value
			// but if we have a comment inside our text, only remove the commented part
			if ( val.startsWith( "#" ) ) {
				removers.add( col );
			}
			else if ( val.contains( "#" ) ) {
				commentcol = col;
				val = val.substring( 0, val.indexOf( "#" ) );
				en.setValue( val );
			}
		}

		for ( int col : removers ) {
			rowdata.remove( col );
		}
	}
}
