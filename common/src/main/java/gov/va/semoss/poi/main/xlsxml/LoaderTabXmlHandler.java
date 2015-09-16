/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main.xlsxml;

import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.poi.main.SheetType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An XML parser that handles the Loading Sheet's "Loader" tab
 *
 * @author ryan
 */
public class LoaderTabXmlHandler extends XlsXmlBase {

	private static final Logger log = Logger.getLogger( LoaderTabXmlHandler.class );
	private final Map<String, SheetType> sheettypes = new HashMap<>();
	private final Map<String, String> rawdata = new LinkedHashMap<>();
	private int rownum;
	private int colnum;
	private String colA;
	private boolean isstring = false;

	public LoaderTabXmlHandler( List<String> sst ) {
		super( sst );
	}

	public Map<String, SheetType> getSheetTypes() {
		return new HashMap<>( sheettypes );
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		rawdata.clear();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		rownum = 0;
		boolean mustHaveType = true;

		for ( Map.Entry<String, String> en : rawdata.entrySet() ) {
			String rawA = en.getKey();
			String rawB = en.getValue();
			if ( 0 == rownum ) {
				mustHaveType = verifyHeaders( rawA, rawB );
			}
			else {
				boolean stopnow = !fillInRow( rawA, rawB, mustHaveType );
				if ( stopnow ) {
					break;
				}
			}

			rownum++;
		}
	}

	@Override
	public void startElement( String uri, String localName, String name,
			Attributes attributes ) throws SAXException {
		switch ( name ) {
			case "row":
				rownum = Integer.parseInt( attributes.getValue( "r" ) ) - 1;
				colA = "";
				break;

			case "c": // c is a new cell
				String celltypestr = attributes.getValue( "t" );
				isstring = ( "s".equals( celltypestr ) );

				String colname = attributes.getValue( "r" );
				colnum = LoadingSheetXmlHandler.getColNum( colname.substring( 0,
						colname.lastIndexOf( Integer.toString( rownum + 1 ) ) ) );
				break;

			case "v": // new value for a cell
				setReading( isstring );
				resetContents();
				break;
		}

	}

	@Override
	public void endElement( String uri, String localName, String name )
			throws SAXException {
		if ( isReading() ) {
			String val = getStringFromContentsInt();
			if ( 0 == colnum ) {
				colA = val;
				rawdata.put( val, null );
			}
			else if ( 1 == colnum ) {
				rawdata.put( colA, val );
			}
			else if ( !decomment( val ).isEmpty() ) {
				throw new ImportValidationException( ErrorType.TOO_MUCH_DATA,
						"Too much data in row " + rownum );
			}

			setReading( false );
		}
	}

	private static String decomment( String str ) {
		if( null == str ){
			return "";
		}
		
		return str.replaceAll( "#.*", "" );
	}

	private boolean verifyHeaders( String rawA, String rawB ) {
		boolean mustHaveType = false;

		if ( !"Sheet Name".equalsIgnoreCase( rawA ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"Cell A1 must be \"Sheet Name\"" );
		}

		String bcol = decomment( rawB );

		if ( !( bcol.isEmpty() || "Type".equalsIgnoreCase( bcol ) ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"Cell B1 must be \"Type\" if not omitted" );
		}
		else {
			mustHaveType = !bcol.isEmpty();
		}

		return mustHaveType;
	}

	/**
	 * Fills in a new sheet type
	 *
	 * @param rawA
	 * @param rawB
	 * @param mustHaveB
	 * @return true if processing should continue to the next row. false ends the
	 * data processing loop
	 */
	private boolean fillInRow( String rawA, String rawB, boolean mustHaveType ) {
		if ( rawA.startsWith( "#" ) ) {
			return true;
		}

		if ( rawA.isEmpty() && rawB.isEmpty() ) {
			log.debug( "empty row at " + ( rownum + 1 ) + "...skipping rest of tab" );
			return false;
		}

		String acol = decomment( rawA );
		String bcol = decomment( rawB );

		if ( bcol.isEmpty() ) {
			if ( mustHaveType ) {
				throw new ImportValidationException( ErrorType.MISSING_DATA,
						"No type specified for sheet " + colA );
			}
			else {
				sheettypes.put( acol, SheetType.UNSPECIFIED );
			}
		}
		else {
			if ( "usual".equalsIgnoreCase( bcol ) ) {
				sheettypes.put( acol, SheetType.USUAL );
			}
			else if ( "metadata".equalsIgnoreCase( bcol ) ) {
				sheettypes.put( acol, SheetType.METADATA );
			}
			else {
				throw new ImportValidationException( ErrorType.INVALID_TYPE,
						"Column B" + ( rownum + 1 )
						+ " must be either \"Usual\", or \"Metadata\"" );
			}
		}

		return true;
	}
}
