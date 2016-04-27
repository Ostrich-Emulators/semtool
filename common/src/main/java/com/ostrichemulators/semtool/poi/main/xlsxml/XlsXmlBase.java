/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main.xlsxml;

import java.util.List;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ryan
 */
public class XlsXmlBase extends DefaultHandler {

	private final List<String> sst;
	private boolean reading;
	private final StringBuilder lastContents = new StringBuilder();

	public XlsXmlBase( List<String> sharedStrings ) {
		sst = sharedStrings;
	}

	protected void setReading( boolean b ) {
		reading = b;
	}

	protected boolean isReading() {
		return reading;
	}

	protected String getContents() {
		return lastContents.toString();
	}

	protected int getContentsAsInt() {
		return Integer.parseInt( getContents() );
	}

	protected String getStringFromContentsInt() {
		return sst.get( getContentsAsInt() );
	}

	protected String getString( int idx ) {
		return sst.get( idx );
	}

	protected void resetContents() {
		lastContents.setLength( 0 );
	}

	@Override
	public void characters( char[] ch, int start, int length )
			throws SAXException {
		if ( reading ) {
			lastContents.append( ch, start, length );
		}
	}
}
