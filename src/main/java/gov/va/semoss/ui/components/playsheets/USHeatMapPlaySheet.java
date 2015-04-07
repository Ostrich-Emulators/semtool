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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * The Play Sheet for the United States geo-location data heatmap. Visualizes a
 * world heat map that can show any numeric property on a node.
 */
public class USHeatMapPlaySheet extends BrowserPlaySheet2 {

	/**
	 * Constructor for USHeatMapPlaySheet.
	 */
	public USHeatMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/usheatmap.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, getPlaySheetFrame().getEngine() );
		Set<Map<String, Object>> data = new HashSet<>();
		String[] var = headers.toArray( new String[0] );

		//Possibly filter out all US Facilities from the query?
		for ( Value[] listElement : newdata ) {
			LinkedHashMap elementHash = new LinkedHashMap();
			for ( int j = 0; j < var.length; j++ ) {
				String colName = var[j];
				Literal l = Literal.class.cast( listElement[j] );

				try {
					elementHash.put( colName, l.doubleValue() );
				}
				catch ( Exception ex ) {
					elementHash.put( colName, l.stringValue() );
				}
			}
			data.add( elementHash );
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", data );

		allHash.put( "value", var[1] );
		allHash.put( "locationName", var[0] );

		addDataHash( allHash );
		createView();
	}
}
