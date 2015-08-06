/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML parser that tries to figure out a sheet's type
 *
 * @author ryan
 */
public class SheetTypeXmlHandler extends DefaultHandler {

	private static final Logger log = Logger.getLogger( SheetTypeXmlHandler.class );
	private final ArrayList<String> sst;
	private String lastContents;
	private int rownum;
	private int colnum;
	private SheetType sheettype = null;
	private boolean reading = false;
	private boolean isstring = false;
	private boolean skipping = false;

	public SheetTypeXmlHandler( ArrayList<String> sst ) {
		this.sst = sst;
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
				reading = ( isstring && 0 == colnum );
				lastContents = "";
				break;
		}

	}

	@Override
	public void endElement( String uri, String localName, String name )
			throws SAXException {
		if ( skipping ) {
			return;
		}

		if ( reading ) {
			reading = false;
			skipping = true;

			boolean ok = false;
			try {
				String val = decomment( sst.get( Integer.parseInt( lastContents ) ) );
				sheettype = SheetType.valueOf( val.toUpperCase() );
				ok = ( SheetType.METADATA == sheettype || SheetType.NODE == sheettype
						|| SheetType.RELATION == sheettype );
			}
			catch ( IllegalArgumentException iae ) {
				ok = false;
			}

			if ( !ok ) {
				throw new ImportValidationException( ImportValidationException.ErrorType.INVALID_TYPE,
						"Cell A1 must be one of: \"Relation\", \"Node\", or \"Metadata\"" );

			}
		}
	}

	@Override
	public void characters( char[] ch, int start, int length )
			throws SAXException {
		if ( reading ) {
			lastContents += new String( ch, start, length );
		}
	}

	private static String decomment( String str ) {
		return str.replaceAll( "#.*", "" );
	}
}
