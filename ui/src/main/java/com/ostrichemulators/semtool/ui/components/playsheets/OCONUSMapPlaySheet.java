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
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * The Play Sheet for Outside the Continental United States (OCONUS)
 * geo-location data. Visualizes Latitude/Longitude coordinates on a map of
 * OCONUS.
 */
public class OCONUSMapPlaySheet extends BrowserPlaySheet2 {

	private static final Logger log = Logger.getLogger( OCONUSMapPlaySheet.class );

	/**
	 * Constructor for OCONUSMapPlaySheet.
	 */
	public OCONUSMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/worldmap.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, engine );

		Set<Map<String, Object>> data = new HashSet<>();
		String[] var = headers.toArray( new String[0] );

		//Possibly filter out all US Facilities from the query?
		for ( Value[] listElement : newdata ) {
			Map<String, Object> elementHash = new LinkedHashMap<>();
			for ( int j = 0; j < var.length; j++ ) {
				Value v = listElement[j];
				String colName = var[j];
				elementHash.put( "size", 1000000 );

				Class<?> k = RDFDatatypeTools.getClassForValue( v );
				if ( k.equals( String.class ) ) {
					elementHash.put( colName, v.stringValue() );
				}
				else {
					Literal l = Literal.class.cast( v );
					elementHash.put( colName, l.doubleValue() );
				}
			}
			data.add( elementHash );
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", data );
		allHash.put( "lat", "lat" );
		allHash.put( "lon", "lon" );
		allHash.put( "size", "size" );
		allHash.put( "locationName", var[0] );
		/*allHash.put("xAxisTitle", var[0]);
		 allHash.put("yAxisTitle", var[1]);
		 allHash.put("value", var[2]);*/

		addDataHash( allHash );
		createView();
	}
}
