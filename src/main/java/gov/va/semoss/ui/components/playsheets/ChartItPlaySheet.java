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

import gov.va.semoss.om.SEMOSSVertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * This class is used in chart listeners to create the appropriate browser and
 * pull the appropriate data for a playsheet.
 */
public class ChartItPlaySheet extends BrowserPlaySheet2 {
	private static final long serialVersionUID = 5944414296343639772L;
	private static final String filename = "/html/RDFSemossCharts/app/index.html";

	/**
	 * Constructor for BrowserTabSheet3.
	 *
	 * @param fileName File name to be navigated to in the browser.
	 * @param ps	Playsheet being called.
	 */
	public ChartItPlaySheet( GraphPlaySheet gps ) {
		super( filename );		
		pullData(gps);
	}
	
	public void pullData(GraphPlaySheet gps) {
		Map<URI, List<SEMOSSVertex>> nodeHash = gps.getFilterData().getNodeTypeMap();
		Map<String, List<SEMOSSVertex>> nodeHashAsLocalNames = new HashMap<String, List<SEMOSSVertex>>();
		
		for(URI nodeType:nodeHash.keySet()) {
			nodeHashAsLocalNames.put(nodeType.getLocalName(), nodeHash.get(nodeType));
		}
		
		Map<String, Object> newHash = new HashMap<>();
		newHash.put( "Nodes", nodeHashAsLocalNames );
		
		addDataHash( newHash );
		createView();
	}
}