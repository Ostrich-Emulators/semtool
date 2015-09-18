/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main.xlsxml;

import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.SheetType;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An XML parser that tries to figure out a sheet's type
 *
 * @author ryan
 */
public class SheetTypeXmlHandler extends XlsXmlBase {

	private static final Logger log = Logger.getLogger( SheetTypeXmlHandler.class );
	private int rownum;
	private int colnum;
	private SheetType sheettype = null;
	private boolean isstring = false;
	private boolean skipping = false;

	public SheetTypeXmlHandler( List<String> sst ) {
		super( sst );
	}

	public SheetType getSheetType() {
		return sheettype;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		skipping = false;
		sheettype = null;
	}

	@Override
	public void startElement( String uri, String localName, String name,
			Attributes attributes ) throws SAXException {
		if ( skipping ) {
			return;
		}

		switch ( name ) {
			case "row":
				rownum = Integer.parseInt( attributes.getValue( "r" ) ) - 1;
				break;

			case "c": // c is a new cell
				String celltypestr = attributes.getValue( "t" );
				isstring = ( "s".equals( celltypestr ) );

				String colname = attributes.getValue( "r" );
				colnum = LoadingSheetXmlHandler.getColNum( colname.substring( 0,
						colname.lastIndexOf( Integer.toString( rownum + 1 ) ) ) );
				break;

			case "v": // new value for a cell
				setReading( isstring && 0 == colnum );
				resetContents();
				break;
		}

	}

	@Override
	public void endElement( String uri, String localName, String name )
			throws SAXException {
		if ( skipping ) {
			return;
		}

		if ( isReading() ) {
			setReading( false );
			skipping = true;

			boolean ok = false;
			String contents = getStringFromContentsInt();
			String val = decomment( contents );
			try {
				sheettype = SheetType.valueOf( val.toUpperCase() );
				ok = ( SheetType.METADATA == sheettype || SheetType.NODE == sheettype
						|| SheetType.RELATION == sheettype );
			}
			catch ( IllegalArgumentException iae ) {
				ok = false;
			}

			if ( !ok ) {
				throw new ImportValidationException( ImportValidationException.ErrorType.INVALID_TYPE,
						"Cell A1 must be one of: \"Relation\", \"Node\", or \"Metadata\" (was:"
						+ val + ")" );

			}
		}
	}

	private static String decomment( String str ) {
		return str.replaceAll( "#.*", "" );
	}
}
