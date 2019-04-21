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
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * The GridScatterPlaySheet class creates the panel and table for a scatter plot
 * view of data from a SPARQL query.
 */
public class GridScatterSheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = -895084553665944922L;

	/**
	 * Constructor for GridScatterSheet.
	 */
	public GridScatterSheet() {
		super( "/html/RDFSemossCharts/app/gridscatterchart.html" );
	}

	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		
		
		Map<String, Object> dataHash = new HashMap<>();
		List<Object[]> allData = new ArrayList<>();
		for ( Value[] listElement : data ){
			Object[] dataSet = new Object[4];

			dataSet[0] = Literal.class.cast( listElement[1] ).doubleValue();
			dataSet[1] = Literal.class.cast( listElement[2] ).doubleValue();
			
			dataSet[2]= ( listElement.length < 4 
					? 0.0 : Literal.class.cast( listElement[3] ).doubleValue() );
			
			dataSet[3] = listElement[0].stringValue();
			allData.add( dataSet );
		}
		
		Object[][] dataSeries = new Object[allData.size()][4];
		for ( int i = 0; i < allData.size(); i++ ) {
			dataSeries[i] = (Object[]) allData.get( i );
		}
		dataHash.put( "Series", dataSeries );
		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "dataSeries", dataHash );
		allHash.put( "type", "scatter" );
		String[] var = headers.toArray( new String[0] );
		allHash.put( "title", var[1] + " vs " + var[2] );
		allHash.put( "xAxisTitle", var[1] );
		allHash.put( "yAxisTitle", var[2] );
		
		addDataHash( allHash );
		createView();
	}

}
