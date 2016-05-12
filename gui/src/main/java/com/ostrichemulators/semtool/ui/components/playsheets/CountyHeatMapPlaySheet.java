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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * The Play Sheet for the United States geo-location data heatmap. Visualizes a
 * world heat map that can show any numeric property on a node.
 */
public class CountyHeatMapPlaySheet extends BrowserPlaySheet2 {

	/**
	 * Constructor for USHeatMapPlaySheet.
	 */
	public CountyHeatMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/countyheatmap.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		Set<Map<String, Object>> data = new HashSet<>();
		String[] var = headers.toArray( new String[0] );

		//Possibly filter out all US Facilities from the query?
		for ( Value[] listElement : newdata ) {
			Map<String, Object> elementHash = new LinkedHashMap<>();
			Double value;
			for ( int j = 0; j < var.length; j++ ) {
				String colName = var[j];
				Value v = listElement[j];
				if ( v instanceof URI ) {
					URI u = URI.class.cast( v );
					elementHash.put( colName, u.getLocalName() );
				}
				else if ( v instanceof Literal ) {
					Literal l = Literal.class.cast( v );
					if ( j == 1 ) {

						if ( l.stringValue().contains( "NaN" ) ) {
							elementHash.put( colName, 0.0 );
						}
						else {
							Double numVal = l.doubleValue();
							elementHash.put( colName, numVal );
						}
					}
					else if ( j > 1 ) {
						elementHash.put( colName, l.stringValue().replaceAll( "\"", "" ) );
					}
				}
			}
			data.add( elementHash );
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", data );
		allHash.put( "locationName", var[0] );
		allHash.put( "value", var[1] );
		List<String> propertyName = new ArrayList<>();
		for ( int i = 0; i < var.length - 2; i++ ) {
			propertyName.add( var[i + 2] );
		}
		allHash.put( "propertyNames", propertyName );

		addDataHash( allHash );
		createView();
	}
}
