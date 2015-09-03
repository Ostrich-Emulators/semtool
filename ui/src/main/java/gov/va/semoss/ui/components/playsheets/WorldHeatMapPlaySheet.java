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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * The Play Sheet for the World geo-location data heatmap. Visualizes a world
 * heat map that can show any numeric property on a node.
 */
public class WorldHeatMapPlaySheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = 5117841017866221965L;
	private final static String LOCATION_ID = "locationId";
	private final static String HEAT_VALUE  = "heatValue";
	private final static String PARAM_MAP  = "paramMap";

	/**
	 * Constructor for WorldHeatMapPlaySheet.
	 */
	public WorldHeatMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/heatmapworld.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, getPlaySheetFrame().getEngine() );
		
		Set<Map<String, Object>> data = new HashSet<Map<String, Object>>();
		
		outsideLoop: for ( Value[] listElement : newdata ) {
			Literal location  = Literal.class.cast( listElement[0] );
			Literal heatValue = Literal.class.cast( listElement[1] );
			if (location == null || heatValue == null)
				continue outsideLoop;
			
			LinkedHashMap<String,Object> elementHash = new LinkedHashMap<String,Object>();
			
			try {
				elementHash.put( HEAT_VALUE, heatValue.doubleValue() );
			} catch (Exception e) {
				continue outsideLoop;
			}
			
			elementHash.put( LOCATION_ID, location.stringValue().toUpperCase() );
			
			HashMap<String, String> tooltipParams = new HashMap<String, String>();
			insideLoop: for (int i=2; i<listElement.length; i++) {
				Literal thisParam = Literal.class.cast( listElement[i] );
				if (thisParam == null)
					continue insideLoop;
				tooltipParams.put( headers.get(i), thisParam.stringValue() );
			}
			elementHash.put( PARAM_MAP, tooltipParams );
			
			data.add( elementHash );
		}
		
		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", data );
		allHash.put( "heatDataName", headers.get(1) );
		addDataHash( allHash );
		
		createView();
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}
}