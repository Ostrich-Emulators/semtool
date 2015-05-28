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
package gov.va.semoss.ui.components;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.models.FilterRowModel;
import gov.va.semoss.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * This class is used to filter vertex data.
 */
public class VertexFilterData {
	// Maps to retrieve nodes by type and by URI
	private final Map<URI, List<SEMOSSVertex>> nodeTypeMap = new HashMap<>();
	private final Map<URI, SEMOSSVertex> nodeMap = new HashMap<>();
	private List<FilterRowModel> nodes = new ArrayList<FilterRowModel>();
	
	// Maps to retrieve edges by type and by URI
	private final Map<URI, List<SEMOSSEdge>> edgeTypeMap = new HashMap<>();
	private final Map<URI, SEMOSSEdge> edgeMap = new HashMap<>();
	private List<FilterRowModel> edges = new ArrayList<FilterRowModel>();

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph;

	public VertexFilterData(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
		this.graph = graph;
	}

	public void generateAllRows() {
		if (nodes.size() != 0) 
			return;

		populateNodeMaps();
		populateEdgeMaps();
		fillNodeRows();
		fillEdgeRows();
	}


	/**
	 * Populates the nodeTypeMap and nodeMap
	 */
	private void populateNodeMaps() {
		for ( SEMOSSVertex vertex : graph.getVertices() ) {
			nodeMap.put ( vertex.getURI(), vertex );
			
			List<SEMOSSVertex> typeList = nodeTypeMap.get( vertex.getType() );
			if (typeList == null)
				typeList = new ArrayList<SEMOSSVertex>();
			typeList.add( vertex );
			nodeTypeMap.put( vertex.getType(), typeList );
		}
	}
	
	/**
	 * Populates the edgeTypeMap and edgeMap
	 */
	private void populateEdgeMaps() {
		for ( SEMOSSEdge edge : graph.getEdges() ) {
			edgeMap.put ( edge.getURI(), edge );
			
			List<SEMOSSEdge> typeList = edgeTypeMap.get( edge.getType() );
			if (typeList == null)
				typeList = new ArrayList<SEMOSSEdge>();
			typeList.add( edge );
			edgeTypeMap.put( edge.getType(), typeList );
		}
	}

	/**
	 * Fills the rows based on the node name and type.
	 */
	private void fillNodeRows() {
		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : nodeTypeMap.entrySet() ) {
			nodes.add( new FilterRowModel(true, true, false, entry.getKey(), Constants.ANYNODE) );
			for ( SEMOSSVertex vertex : entry.getValue() )
				nodes.add( new FilterRowModel(true, false, false, Constants.ANYNODE, vertex.getURI()) );
		}
	}
	
	/**
	 * Fills the edge rows based on the edge name and type.
	 */
	private void fillEdgeRows() {
		for ( Map.Entry<URI, List<SEMOSSEdge>> entry : edgeTypeMap.entrySet() ) {
			edges.add( new FilterRowModel(true, true, false, entry.getKey(), Constants.ANYNODE) );
			for ( SEMOSSEdge edge: entry.getValue() )
				edges.add( new FilterRowModel(true, false, false, Constants.ANYNODE, edge.getURI()) );
		}
	}
	
	/**
	 * Get the graph
	 *
	 * @return DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph
	 */
	public DirectedGraph<SEMOSSVertex, SEMOSSEdge> getGraph() {
		return graph;
	}
	
	/**
	 * Get the nodes as rows of the table
	 *
	 * @return List<FilterRowModel> nodes
	 */
	public List<FilterRowModel> getNodes() {
		return nodes;
	}

	/**
	 * Returns the map of nodes by URI.
	 *
	 * @return Map<URI, SEMOSSVertex> nodeMap
	 */
	public Map<URI, SEMOSSVertex> getNodeMap() {
		return nodeMap;
	}

	/**
	 * Returns a map of the nodes by type
	 *
	 * @return Map<URI, List<SEMOSSVertex>> nodeTypeMap
	 */
	public Map<URI, List<SEMOSSVertex>> getNodeTypeMap() {
		return nodeTypeMap;
	}
		
	/**
	 * Get the edges as rows of the table
	 *
	 * @return List<FilterRowModel> edges
	 */
	public List<FilterRowModel> getEdges() {
		return edges;
	}

	/**
	 * Returns the map of edges by URI.
	 *
	 * @return Map<URI, SEMOSSEdge> edgeMap
	 */
	public Map<URI, SEMOSSEdge> getEdgeMap() {
		return edgeMap;
	}

	/**
	 * Returns a map of the edges by type
	 *
	 * @return Map<URI, List<SEMOSSEdge>> edgeTypeMap
	 */
	public Map<URI, List<SEMOSSEdge>> getEdgeTypeMap() {
		return edgeTypeMap;
	}
}