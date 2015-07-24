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
	private static final long serialVersionUID = 150592881428916712L;
	private final static String LOCATION_ID = "locationId";
	private final static String HEAT_VALUE  = "heatValue";

	/**
	 * Constructor for USHeatMapPlaySheet.
	 */
	public USHeatMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/heatmapus.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, getPlaySheetFrame().getEngine() );
		
		Set<Map<String, Object>> data = new HashSet<Map<String, Object>>();
		outsideLoop: for ( Value[] listElement : newdata ) {
			LinkedHashMap<String,Object> elementHash = new LinkedHashMap<String,Object>();
						
			for ( int i = 0; i < headers.size(); i++ ) {
				Literal literal = Literal.class.cast( listElement[i] );
				if (literal==null)
					continue outsideLoop;
				
				if (LOCATION_ID.equals(headers.get(i))) {
					elementHash.put( LOCATION_ID, literal.stringValue() );
				} else if (HEAT_VALUE.equals(headers.get(i))) {
					try {
						elementHash.put( HEAT_VALUE, literal.doubleValue() );
					} catch (Exception e) {
						continue outsideLoop;
					}
				}
			}
			
			data.add( elementHash );
		}
		
		Map<String, Object> allHash = new HashMap<>();
//		allHash.put( "dataSeries", convertDataValuesToPercentages(data) );
		allHash.put( "dataSeries", data );
		addDataHash( allHash );
		
		createView();
	}

	private Set<Map<String, Object>> convertDataValuesToPercentages(Set<Map<String, Object>> data) {
		double maxValue = 0d;
		for (Map<String, Object> thisMap:data) {
			double thisValue = Double.parseDouble(""+thisMap.get(HEAT_VALUE));
			if (thisValue > maxValue)
				maxValue = thisValue;
		}
		
		for (Map<String, Object> thisMap:data) {
			double thisValue = Double.parseDouble(""+thisMap.get(HEAT_VALUE));
			thisMap.put(HEAT_VALUE, new Double(thisValue/maxValue));
		}

		return data;
	}
}
