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
package gov.va.semoss.ui.main.listener.impl;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.RenderContext;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.ArrowDrawPaintTransformer;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Controls what to do when the pop up menu is selected on a graph.
 */
public class AdjacentPopupMenuListener extends AbstractAction {

	public static enum Type {

		ALL( "Highlight All Downstream", "Highlights all downstream nodes adjacent to the selected node" ),
		ADJACENT( "Highlight Adjacent", "Highlights nodes adjacent to selected node" ),
		DOWNSTREAM( "Highlight Downstream", "Highlights downstream nodes adjacent to selected node" ),
		UPSTREAM( "Highlight Upstream", "Highlights upstream nodes adjacent to selected node" );
		public final String name;
		public final String tooltip;

		Type( String nam, String toolt ) {
			name = nam;
			tooltip = toolt;
		}
	};

	private final GraphPlaySheet ps;
	private final List<SEMOSSVertex> vertices;
	private final Type type;

	private static final Logger logger = Logger.getLogger( AdjacentPopupMenuListener.class );

	public AdjacentPopupMenuListener( Type type, GraphPlaySheet gps,
			Collection<SEMOSSVertex> verts ) {
		super( type.name );
		ps = gps;
		vertices = new ArrayList<>( verts );
		this.type = type;
		setEnabled( !vertices.isEmpty() );
		putValue( Action.SHORT_DESCRIPTION, type.tooltip );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = ps.getView().getRenderContext();

		EdgeStrokeTransformer etx = (EdgeStrokeTransformer) rc.getEdgeStrokeTransformer();

		PickedState state = ps.getView().getPickedVertexState();
		Set<SEMOSSVertex> selectedVerts = new HashSet<>( state.getPicked() );
		Set<SEMOSSEdge> selectedEdges = new HashSet<>( etx.getSelectedEdges() );
		state.clear();

		//if it is All, must use distance downstream processor to get all of the edges
		if ( Type.ALL != type ) {
			for ( SEMOSSVertex vert : vertices ) {
				selectedVerts.add( vert );

				//if the button name contains upstream, get the upstream edges and vertices
				if ( Type.ADJACENT == type ) {
					selectedEdges.addAll( vert.getInEdges() );
					selectedEdges.addAll( vert.getOutEdges() );
				}
				else if ( Type.DOWNSTREAM == type ) {
					selectedEdges.addAll( vert.getOutEdges() );
				}
				else {
					selectedEdges.addAll( vert.getInEdges() );
				}
			}
		}
		else if ( Type.ALL == type ) {
			// get all downstream nodes
			Set<SEMOSSVertex> seen = new HashSet<>();
			Deque<SEMOSSVertex> todo = new ArrayDeque<>( vertices );
			while ( !todo.isEmpty() ) {
				SEMOSSVertex v = todo.pop();
				selectedVerts.add( v );
				seen.add( v );

				for ( SEMOSSEdge ed : v.getOutEdges() ) {
					selectedEdges.add( ed );

					SEMOSSVertex v2 = ed.getInVertex();
					if ( !seen.contains( v2 ) ) { // don't add a node we've already seen
						todo.push( v2 );
					}
				}
			}
		}

		for ( SEMOSSEdge ed : selectedEdges ) {
			selectedVerts.add( ed.getInVertex() );
			selectedVerts.add( ed.getOutVertex() );
		}

		for ( SEMOSSVertex v : selectedVerts ) {
			state.pick( v, true );
		}

		etx.setSelectedEdges( selectedEdges );

		LabelFontTransformer vlft = (LabelFontTransformer) rc.getVertexFontTransformer();
		vlft.setSelected( selectedVerts );
		ArrowDrawPaintTransformer atx = (ArrowDrawPaintTransformer) ps.getView().getRenderContext().getArrowDrawPaintTransformer();
		atx.setEdges( selectedEdges );

		// repaint it
		ps.getView().repaint();
	}
}
