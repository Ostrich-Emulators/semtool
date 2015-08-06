/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.RDFDatatypeTools;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.util.Constants;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * The Play Sheet for creating a Parallel Coordinates diagram.
 */
public class ParallelCoordinatesPlaySheet extends BrowserPlaySheet2 {

	private static final long serialVersionUID = 5265922729047978486L;

	/**
	 * Constructor for ParallelCoordinatesPlaySheet.
	 */
	public ParallelCoordinatesPlaySheet() {
		super("/html/RDFSemossCharts/app/parcoords.html");
	}

	@Override
	public void create( List<Value[]> data, List<String> heads, IEngine engine ) {
		setHeaders( heads );
		
		convertUrisToLabels( data, getPlaySheetFrame().getEngine() );
		
		List<Map<String, Object>> dataArrayList = new ArrayList<>();
		for ( Value[] varValuesArray : data ) {
			Map<String, Object> elementHash = new LinkedHashMap<>();
			for ( int i = 0; i < varValuesArray.length; i++ ) {
				Value v = varValuesArray[i];
				Class<?> k = RDFDatatypeTools.instance().getClassForValue( v );
				if ( String.class.equals( k ) ) {
					elementHash.put( heads.get( i ), cleanText( v.stringValue() ) );
				}
				else {
					elementHash.put( heads.get( i ), Literal.class.cast( v ).doubleValue() );
				}
			}

			dataArrayList.add( elementHash );
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( Constants.DATASERIES, dataArrayList );
		addDataHash( allHash );
		createView();
	}

	private String cleanText( String inputString ) {
		inputString = inputString.replaceAll( "^\"|\"$", "" );
		inputString = inputString.replaceAll( "_", " " );
		if ( inputString.length() >= 30 ) {
			inputString = inputString.substring( 0, Math.min( inputString.length(), 30 ) ) + "...";
		}

		return inputString;
	}
}
