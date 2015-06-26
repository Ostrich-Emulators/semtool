/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.getRDFStringValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ryan
 */
public class LoadingSheetXmlHandler extends DefaultHandler {

	private static final Logger log = Logger.getLogger( LoadingSheetXmlHandler.class );
	private static final Map<String, Integer> formats = new HashMap<>();
	private static final ValueFactory vf = new ValueFactoryImpl();

	private final Map<Integer, Value> currentrowdata = new LinkedHashMap<>();
	private final Map<Integer, String> proplkp = new HashMap<>();
	private final Map<String, String> namespaces;
	private final LoadingSheetData loadingsheet;
	private final ArrayList<String> sst;
	private final StylesTable styles;
	private String lastContents;
	private boolean reading = false;
	private boolean isdate = false;
	private int rownum;
	private int colnum;
	private int celltype;

	static {
		formats.put( "s", Cell.CELL_TYPE_STRING );
		formats.put( "n", Cell.CELL_TYPE_NUMERIC );
		formats.put( "b", Cell.CELL_TYPE_BOOLEAN );
	}

	public static int getColNum( String colname ) {
		int sum = 0;

		for ( int i = 0; i < colname.length(); i++ ) {
			sum *= 26;
			char charat = colname.charAt( i );
			sum += ( charat - 'A' + 1 );
		}

		return sum - 1;
	}

	public LoadingSheetXmlHandler( ArrayList<String> sst, StylesTable styles,
			String sheetname, Map<String, String> ns ) {
		this.sst = sst;
		this.styles = styles;
		namespaces = ns;

		// we'll reset this to relationship if needed
		loadingsheet = LoadingSheetData.nodesheet( sheetname, "" );
	}

	public LoadingSheetData getSheet() {
		return loadingsheet;
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
					// dates don't always have a type attribute
					if ( Cell.CELL_TYPE_NUMERIC == celltype || null == celltypestr ) {
						celltype = Cell.CELL_TYPE_NUMERIC;
						
						// check if it's a date
						String styleidstr = attributes.getValue( "s" );
						int styleid = ( null == styleidstr ? 0
								: Integer.parseInt( styleidstr ) );

						XSSFCellStyle style = styles.getStyleAt( styleid );
						int formatIndex = style.getDataFormat();
						String formatString = style.getDataFormatString();
						isdate = DateUtil.isADateFormat( formatIndex, formatString );
					}
					
					String colname = attributes.getValue( "r" );
					colnum = getColNum( colname.substring( 0,
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
			if ( 0 == rownum ) {
				proplkp.clear();

				String sheettype = currentrowdata.remove( 0 ).stringValue();
				String subjtype = currentrowdata.remove( 1 ).stringValue();

				loadingsheet.setSubjectType( subjtype );

				if ( "relation".equalsIgnoreCase( sheettype ) ) {
					String objtype = currentrowdata.remove( 2 ).stringValue();
					loadingsheet.setObjectType( objtype );
				}

				for ( Map.Entry<Integer, Value> en : currentrowdata.entrySet() ) {
					loadingsheet.addProperty( en.getValue().stringValue() );
					proplkp.put( en.getKey(), en.getValue().stringValue() );
				}

				return;
			}
			else if ( 1 == rownum ) {
				if ( loadingsheet.isRel() ) {
					loadingsheet.setRelname( currentrowdata.get( 0 ).stringValue() );
				}
			}

			fillInRow( currentrowdata, loadingsheet, proplkp );
		}

		if ( reading ) {
			// If we've fully read the data, add it to our row mapping
			switch ( celltype ) {
				case Cell.CELL_TYPE_STRING:
					String strval = sst.get( Integer.parseInt( lastContents ) );
					if ( !strval.isEmpty() ) {
						currentrowdata.put( colnum, getRDFStringValue( strval, namespaces, vf ) );
					}
					break;
				case Cell.CELL_TYPE_BLANK:
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					currentrowdata.put( colnum,
							vf.createLiteral( "1".equals( lastContents ) ) );
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if ( isdate ) {
						currentrowdata.put( colnum,
								vf.createLiteral( DateUtil.getJavaDate( Double.parseDouble( lastContents ) ) ) );
					}
					else {
						currentrowdata.put( colnum, vf.createLiteral( Double.parseDouble( lastContents ) ) );
					}
					break;
				case Cell.CELL_TYPE_ERROR:
					log.warn( "unhandled cell type: CELL_TYPE_ERROR" );
					break;
				case Cell.CELL_TYPE_FORMULA:
					log.warn( "unhandled cell type: CELL_TYPE_FORMULA" );
					break;
				default:
					log.warn( "unhandled cell type: " + celltype );
			}

//			if ( null != currentcell ) {
//				log.debug( sheet.getSheetName() + "(" + currentrow.getRowNum() + ","
//						+ currentcell.getColumnIndex() + ") " + currentcell.getStringCellValue() );
//			}
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

	private static void fillInRow( Map<Integer, Value> rowdata,
			LoadingSheetData sheet, Map<Integer, String> proplkp ) {
		removeComments( rowdata );
		if ( rowdata.isEmpty() || !rowdata.containsKey( 1 ) ) {
			return;
		}

		rowdata.remove( 0 );
		LoadingNodeAndPropertyValues nap = sheet.add( rowdata.remove( 1 ).stringValue() );

		if ( sheet.isRel() && rowdata.containsKey( 2 ) ) {
			nap.setObject( rowdata.remove( 2 ).stringValue() );
		}

		for ( Map.Entry<Integer, Value> en : rowdata.entrySet() ) {
			nap.put( proplkp.get( en.getKey() ), en.getValue() );
		}
	}

	/**
	 * Removes any comments from the given mapping. Any column index after the
	 * comment is likewise removed
	 *
	 * @param rowdata the data to remove comments from
	 */
	private static void removeComments( Map<Integer, Value> rowdata ) {
		int commentcol = Integer.MAX_VALUE;
		List<Integer> removers = new ArrayList<>();
		for ( Map.Entry<Integer, Value> en : rowdata.entrySet() ) {
			int col = en.getKey();
			String val = en.getValue().stringValue();

			if ( val.startsWith( "<" ) && val.endsWith( ">" ) ) {
				// this is a URI, and doesn't have a comment, even if it contains a #
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
				en.setValue( vf.createLiteral( val ) );
			}
		}

		for ( int col : removers ) {
			rowdata.remove( col );
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		if ( log.isDebugEnabled() ) {
			log.debug( "Loading sheet " + loadingsheet.getName()
					+ " processed. properties: "
					+ Arrays.toString( loadingsheet.getProperties().toArray() ) );
			log.debug( "Created " + loadingsheet.getData().size()
					+ ( loadingsheet.isRel() ? " relationships" : " entities" ) );
		}
	}
}
