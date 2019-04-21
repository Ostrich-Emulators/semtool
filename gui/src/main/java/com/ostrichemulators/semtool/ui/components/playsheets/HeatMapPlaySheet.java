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
import com.ostrichemulators.semtool.util.Constants;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * The Play Sheet for creating a heat map diagram.
 */
public class HeatMapPlaySheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = -1884361528714708952L;
	private static final Logger log = Logger.getLogger( HeatMapPlaySheet.class );

	/**
	 * Constructor for HeatMapPlaySheet.
	 */
	public HeatMapPlaySheet() {
		super( "/html/RDFSemossCharts/app/heatmap.html" );
	}

	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		try {
			setHeaders( headers );
			convertUrisToLabels( data, engine );
	
			String xName = headers.get(0);
			String yName = headers.get(1);
			String zName = headers.get(2);
			
			Map<String, Object> hash = new HashMap<>();
			for ( Value[] listElement : data ) {
				String methodName = listElement[0].stringValue().replaceAll( "\"", "" );
				String  groupName = listElement[1].stringValue().replaceAll( "\"", "" );
				String key = methodName + "-" + groupName;
				double count = Literal.class.cast( listElement[2] ).doubleValue();
				
				Map<String, Object> elementHash = new HashMap<>();
				elementHash.put( xName, methodName );  //X-axis value, e.g.: ("ApplicationModule", "BCMA").
				elementHash.put( yName, groupName );   //Y-axis value, e.g.: ("ApplicationList", "CPRS").
				elementHash.put( zName, count );       // count value, e.g.: ("ctd", 20).
				hash.put( key, elementHash );
			}
	
			Map<String, Object> allHash = new HashMap<>();
			allHash.put( Constants.DATASERIES, hash );
			String[] var1 = headers.toArray( new String[0] );
			allHash.put( "title", var1[0] + " vs " + var1[1] );  //e.g.: ("title", "ApplicationModule vs ApplicationList").
			allHash.put( "xAxisTitle", var1[0] );                //e.g.: ("xAxisTitle", "ApplicationModule").
			allHash.put( "yAxisTitle", var1[1] );                //e.g.: ("yAxisTitle", "ApplicationList").
			allHash.put( "value", var1[2] );                     //e.g.: ("value", "ctd").
	
			addDataHash( allHash );
			createView();
		}
		catch (Exception e) {
			log.debug("Exception in create: " + e, e);
		}
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}
}
