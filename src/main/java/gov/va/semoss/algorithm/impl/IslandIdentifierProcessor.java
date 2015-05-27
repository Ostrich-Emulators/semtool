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
package gov.va.semoss.algorithm.impl;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.RenderContext;
import java.util.ArrayList;
import java.util.Collection;

import gov.va.semoss.algorithm.api.IAlgorithm;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.transformer.ArrowPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;

/**
 * This class is used to identify islands in the network.
 */
public class IslandIdentifierProcessor extends AbstractAction implements IAlgorithm {

	private final List<SEMOSSVertex> selectedVerts = new ArrayList<>();
	private final GraphPlaySheet gps;

	public IslandIdentifierProcessor( GraphPlaySheet gps, Collection<SEMOSSVertex> pickedV ) {
		super( "Island Identifier" );
		this.gps = gps;

		// if no nodes are selected, then all nodes are selected
		selectedVerts.addAll( pickedV.isEmpty()
				? gps.getVisibleGraph().getVertices() : pickedV );
	}

	@Override
	public void execute() {
		actionPerformed( null );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gps.getVisibleGraph();

		// just do a depth-first search of everything this node connects to
		// and highlight all the edges and nodes of this island		
		Set<SEMOSSEdge> islandEdges = new HashSet<>();
		Set<SEMOSSVertex> islandVerts = new HashSet<>( selectedVerts );

		// get all downstream nodes
		Set<SEMOSSVertex> seen = new HashSet<>();
		Deque<SEMOSSVertex> todo = new ArrayDeque<>( selectedVerts );
		while ( !todo.isEmpty() ) {
			SEMOSSVertex v = todo.pop();
			islandVerts.add( v );
			seen.add( v );

			for ( SEMOSSEdge ed : graph.getIncidentEdges( v ) ) {
				islandEdges.add( ed );

				SEMOSSVertex v2 = ( ed.getInVertex().equals( v ) ? ed.getOutVertex()
						: ed.getInVertex() );

				if ( !seen.contains( v2 ) ) { // don't add a node we've already seen
					todo.push( v2 );
				}
			}
		}

		highlightIsland( islandVerts, islandEdges );
	}

	/**
	 * Sets the transformers based on valid edges and vertices for the playsheet.
	 */
	private void highlightIsland( Collection<SEMOSSVertex> islandVerts,
			Collection<SEMOSSEdge> islandEdges ) {

		PickedState state = gps.getView().getPickedVertexState();

		for ( SEMOSSVertex v : islandVerts ) {
			state.pick( v, true );
		}

		gps.skeleton( islandVerts, islandEdges );
 	}

	/**
	 * Sets playsheet as a graph play sheet.
	 *
	 * @param ps IPlaySheet	Playsheet to be cast.
	 */
	@Override
	public void setPlaySheet( IPlaySheet ps ) {
		throw new UnsupportedOperationException( "set the playsheet in the constructor" );
		// playSheet = ( (GraphPlaySheet) ps ).getGraphComponent();
	}

	/**
	 * Gets variables.
	 *
	 * //TODO: Return empty object instead of null
	 *
	 * @return String[]	List of variable names as strings.
	 */
	@Override
	public String[] getVariables() {
		throw new UnsupportedOperationException( "don't know what this is" );
	}

	/**
	 * Get algorithm name - in this case, "Island Identifier."
	 *
	 * @return String	Name of algorithm.
	 */
	@Override
	public String getAlgoName() {
		return "Island Identifier";
	}

}
