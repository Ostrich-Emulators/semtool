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
 *****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.RDFDatatypeTools;

import java.util.HashSet;
import java.util.LinkedHashMap;

import gov.va.semoss.ui.components.models.ValueTableModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * The Play Sheet for Continental United States (CONUS) geo-location data.
 * Visualizes Latitude/Longitude coordinates on a map of the CONUS.
 */
public class CONUSMapPlaySheet extends BrowserPlaySheet2 {

	private static final Logger log = Logger.getLogger( CONUSMapPlaySheet.class );

	/**
	 * Constructor for CONUSMapPlaySheet.
	 */
	public CONUSMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/conusmap.html" );
	}

	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		convertUrisToLabels( data, getPlaySheetFrame().getEngine() );
		
		Set<Map<String, Object>> hashes = new HashSet<>();
		
		for ( Value[] listElement : data ) {
			Map<String, Object> elementHash = new LinkedHashMap<>();
			String colName;
			Double value;
			for ( int j = 0; j < headers.size(); j++ ) {
				Value v = listElement[j];
				colName = headers.get( j );
				Class<?> k = RDFDatatypeTools.instance().getClassForValue( v );
				
				elementHash.put( "size", 1000000 );
				if ( k.equals( String.class ) ){
					String text = v.stringValue();
					elementHash.put( colName, text );
				}
				else {
					value = Literal.class.cast( listElement[j] ).doubleValue();
					elementHash.put( colName, value );
				}
			}
			hashes.add( elementHash );
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", hashes );

		allHash.put( "lat", "lat" );
		allHash.put( "lon", "lon" );
		allHash.put( "size", "size" );
		allHash.put( "locationName", headers.get(  0 ) );
//		allHash.put("xAxisTitle", var[0]);
//		allHash.put("yAxisTitle", var[1]);
//		allHash.put("value", var[2]);

		addDataHash( allHash );
		createView();
	}
}
