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

import static com.ostrichemulators.semtool.ui.helpers.NodeEdgeNumberedPropertyUtility.transformProperties;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.util.Constants;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;

/**
 * This class is used in chart listeners to create the appropriate browser and
 * pull the appropriate data for a playsheet.
 */
public class ChartItPlaySheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = 5944414296343639772L;
	
	/**
	 * Constructor for ChartItPlaySheet.
	 *
	 * @param gps	Playsheet whose nodes we are charting.
	 */
	public ChartItPlaySheet( GraphPlaySheet gps ) {
		super( "/html/RDFSemossCharts/app/chartit.html" );		
		pullData(gps);
	}
	
	public void pullData(GraphPlaySheet gps) {
		Map<URI, List<SEMOSSVertex>> nodeHash = gps.getVerticesByType();
		Map<String, List<SEMOSSVertex>> nodeHashAsLocalNames = new HashMap<>();
		
		for(URI nodeType:nodeHash.keySet()) {
			for (SEMOSSVertex node:nodeHash.get(nodeType)) {
				Map<URI, Object> originalProps = new HashMap<>();
				Map<URI, Value> vals = node.getValues();
				for ( Map.Entry<URI, Value> en : vals.entrySet() ) {
					originalProps.put( en.getKey(),
							RDFDatatypeTools.getObjectFromValue( en.getValue() ) );
				}

				int  inEdgeCount = gps.getGraphData().getGraph().getInEdges( node ).size();
				int outEdgeCount = gps.getGraphData().getGraph().getOutEdges( node ).size();
				int allEdgeCount = gps.getGraphData().getGraph().getIncidentEdges( node ).size();

				originalProps.put(Constants.IN_EDGE_CNT, inEdgeCount+"");
				originalProps.put(Constants.OUT_EDGE_CNT, outEdgeCount+"");
				originalProps.put(Constants.EDGE_CNT, allEdgeCount+"");

				Map<String, Object> props = transformProperties(originalProps, true);

				node.setPropHash(props);
			}
			
			nodeHashAsLocalNames.put(nodeType.getLocalName(), nodeHash.get(nodeType));
		}
		
		Map<String, Object> newHash = new HashMap<>();
		newHash.put( "Nodes", nodeHashAsLocalNames );
		
		addDataHash( newHash );
		createView();
	}
}