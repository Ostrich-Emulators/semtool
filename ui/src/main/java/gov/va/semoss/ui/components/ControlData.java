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
package gov.va.semoss.ui.components;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.GraphElement;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.GraphListener;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;

import gov.va.semoss.util.GuiUtility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * This class is used to keep track of all the properties within the tool.
 */
public class ControlData implements GraphListener {

	private final ControlDataTable vertexCDT;
	private final ControlDataTable edgeCDT;
	private final Map<URI, String> labelcache = new HashMap<>();
	private IEngine engine;

	/**
	 * Constructor for ControlData.
	 */
	public ControlData() {
		// put what we want to show first in all of these things
		Set<URI> propertyHide = new HashSet<>();
		propertyHide.add( Constants.VERTEX_COLOR );

		Set<URI> vertexPropertyShow = new HashSet<>();
		vertexPropertyShow.add( Constants.VERTEX_NAME );

		Set<URI> edgePropertyShow = new HashSet<>();

		Set<URI> propertyShowTT = new HashSet<>();
		propertyShowTT.add( Constants.EDGE_NAME );
		propertyShowTT.add( Constants.EDGE_TYPE );
		propertyShowTT.add( Constants.URI_KEY );
		propertyShowTT.add( Constants.VERTEX_NAME );
		propertyShowTT.add( Constants.VERTEX_TYPE );

		vertexCDT = new ControlDataTable( vertexPropertyShow, propertyShowTT,
				propertyHide, new String[]{ "Node Type", "Property", "Label", "Tooltip" } );
		edgeCDT = new ControlDataTable( edgePropertyShow, propertyShowTT, propertyHide,
				new String[]{ "Edge Type", "Property", "Label", "Tooltip" } );
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {		
		for ( SEMOSSVertex vertex : graph.getVertices() ) {
			for ( URI property : vertex.getProperties().keySet() ) {
				addVertexProperty( vertex.getType(), property );
			}
		}

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			for ( URI property : edge.getProperties().keySet() ) {
				addEdgeProperty( edge.getType(), property );
			}
		}

		generateAllRows();
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout,
			GraphPlaySheet gps ) {
		// nothing to update in this case
	}

	public void setEngine( IEngine e ) {
		engine = e;
	}

	/**
	 * Generates all the rows in the control panel.
	 */
	public void generateAllRows() {
		vertexCDT.populateAllRows();
		edgeCDT.populateAllRows();
	}

	/**
	 * Adds a property of a specific type to the edge ControlDataTable.
	 *
	 * @param type Type of property.
	 * @param property Property.
	 */
	public void addEdgeProperty( URI type, URI property ) {
		edgeCDT.addProperty( type, property );
	}

	/**
	 * Adds a property of a specific type to the vertex ControlDataTable.
	 *
	 * @param type Type of property.
	 * @param property Property.
	 */
	public void addVertexProperty( URI type, URI property ) {
		vertexCDT.addProperty( type, property );
	}

	public String getLabel( URI uri ) {
		if ( !labelcache.containsKey( uri ) ) {
			String l = ( null == engine ? uri.getLocalName()
					: GuiUtility.getInstanceLabel( uri, engine ) );
			labelcache.put( uri, l );
		}
		return labelcache.get( uri );
	}

	public void clear() {
		vertexCDT.clear();
		edgeCDT.clear();
	}

	/**
	 * Gets properties of a specific type.
	 *
	 * @param type Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedProperties( GraphElement v ) {
		return ( v instanceof SEMOSSVertex
				? vertexCDT.getSelectedProperties( v.getType() )
				: edgeCDT.getSelectedProperties( v.getType() ) );
	}

	/**
	 * Gets all of the tooltip specific properties.
	 *
	 * @param type String Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedPropertiesTT( URI type ) {
		List<URI> vertexProperties = vertexCDT.getSelectedPropertiesTT( type );
		List<URI> edgeProperties = edgeCDT.getSelectedPropertiesTT( type );

		if ( vertexProperties == null && edgeProperties == null ) {
			return new ArrayList<>();
		}

		if ( vertexProperties == null || vertexProperties.isEmpty() ) {
			return edgeProperties;
		}

		if ( edgeProperties == null || edgeProperties.isEmpty() ) {
			return vertexProperties;
		}

		vertexProperties.addAll( edgeProperties );

		return vertexProperties;
	}

	public void setViewer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer ) {
		vertexCDT.setViewer( viewer );
		edgeCDT.setViewer( viewer );
	}

	public ControlDataTable.ControlDataTableModel getVertexTableModel() {
		return vertexCDT.getTableModel();
	}

	public ControlDataTable.ControlDataTableModel getEdgeTableModel() {
		return edgeCDT.getTableModel();
	}
}
