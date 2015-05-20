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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


import org.apache.log4j.Logger;

import gov.va.semoss.algorithm.impl.DistanceDownstreamProcessor;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.transformer.ArrowDrawPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeArrowStrokeTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.VertexLabelFontTransformer;
import gov.va.semoss.ui.transformer.VertexPaintTransformer;
import gov.va.semoss.util.Constants;
import edu.uci.ics.jung.visualization.picking.PickedState;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		UPSTREAM( "Highlight Downstream", "Highlights downstream nodes adjacent to selected node" ),
		DOWNSTREAM( "Highlight Upstream", "Highlights upstream nodes adjacent to selected node" );
		public final String name;
		public final String tooltip;

		Type( String nam, String toolt ) {
			name = nam;
			tooltip = toolt;
		}
	};

	GraphPlaySheet ps = null;
	SEMOSSVertex[] vertices = null;
	private final Type type;

	private static final Logger logger = Logger.getLogger( AdjacentPopupMenuListener.class );

	public AdjacentPopupMenuListener( Type type, GraphPlaySheet gps, SEMOSSVertex[] verts ) {
		super( type.name );
		ps = gps;
		vertices = verts;
		this.type = type;
		setEnabled( vertices.length > 0 );
		putValue( Action.SHORT_DESCRIPTION, type.tooltip );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Collection<List<SEMOSSEdge>> allPlaySheetEdges = ps.getFilterData().getEdgeTypeHash().values();
		List<SEMOSSEdge> allEdgesVect = new ArrayList<>();
		for ( List<SEMOSSEdge> v : allPlaySheetEdges ) {
			allEdgesVect.addAll( v );
		}
		logger.debug( "Getting the base graph" );

		//Get what edges are already highlighted so that we can just add to it
		//Get what vertices are already painted so we can just add to it
		EdgeStrokeTransformer tx = (EdgeStrokeTransformer) ps.getView().getRenderContext().getEdgeStrokeTransformer();
		Map<SEMOSSEdge, Double> edgeHash = tx.getEdges();

		VertexPaintTransformer vtx = (VertexPaintTransformer) ps.getView().getRenderContext().getVertexFillPaintTransformer();
		Set<SEMOSSVertex> vertHash = new HashSet<>( vtx.getVertHash() );

		PickedState state = ps.getView().getPickedVertexState();
		state.clear();

		//if it is All, must use distance downstream processor to get all of the edges
		if ( Type.ALL != type ) {
			for ( int vertIndex = 0; vertIndex < vertices.length; vertIndex++ ) {
				SEMOSSVertex vert = vertices[vertIndex];
				logger.debug( "In Edges count is " + vert.getInEdges().size() );
				logger.debug( "Out Edges count is " + vert.getOutEdges().size() );
				vertHash.add( vert );

				//if the button name contains upstream, get the upstream edges and vertices
				if ( Type.ADJACENT == type || Type.DOWNSTREAM == type ) {
					edgeHash = putEdgesInHash( vert.getOutEdges(), edgeHash );
					for ( SEMOSSEdge edge : vert.getOutEdges() ) {
						if ( allEdgesVect.contains( edge ) ) {
							vertHash.add( edge.getInVertex() );
							state.pick( edge.getInVertex(), true );
						}
					}
				}

				//if the button name contains downstream, get the downstream edges and vertices
				if ( Type.ADJACENT == type || Type.UPSTREAM == type ) {
					edgeHash = putEdgesInHash( vert.getInEdges(), edgeHash );
					for ( SEMOSSEdge edge : vert.getInEdges() ) {
						if ( allEdgesVect.contains( edge ) ) {
							vertHash.add( edge.getOutVertex() );
							state.pick( edge.getOutVertex(), true );
						}
					}
				}

			}
		}
		else if ( Type.ALL == type ) {
			DistanceDownstreamProcessor ddp = new DistanceDownstreamProcessor(ps, vertices );
			ddp.execute();
			//use the master hash to set the nodes and edges
			Hashtable masterHash = ddp.masterHash;
			Iterator masterIt = masterHash.keySet().iterator();
			while ( masterIt.hasNext() ) {
				SEMOSSVertex vert = (SEMOSSVertex) masterIt.next();
				Hashtable vHash = (Hashtable) masterHash.get( vert );
				ArrayList<SEMOSSVertex> parentPath = (ArrayList<SEMOSSVertex>) vHash.get( ddp.pathString );
				ArrayList<SEMOSSEdge> parentEdgePath = (ArrayList<SEMOSSEdge>) vHash.get( ddp.edgePathString );
				edgeHash = putEdgesInHash( new Vector( parentEdgePath ), edgeHash );
				for ( SEMOSSEdge edge : parentEdgePath ) {
					if ( allEdgesVect.contains( edge ) ) {
						vertHash.add( edge.getOutVertex() );
						vertHash.add( edge.getInVertex() );
						state.pick( edge.getOutVertex(), true );
						state.pick( edge.getInVertex(), true );
					}
				}
			}

		}
		ps.getView().setPickedVertexState( state );

		tx.setEdges( edgeHash );
		vtx.setVertHash( vertHash );
		VertexLabelFontTransformer vlft = (VertexLabelFontTransformer) ps.getView().getRenderContext().getVertexFontTransformer();
		vlft.setVertHash( vertHash );
		ArrowDrawPaintTransformer atx = (ArrowDrawPaintTransformer) ps.getView().getRenderContext().getArrowDrawPaintTransformer();
		atx.setEdges( edgeHash.keySet() );
		EdgeArrowStrokeTransformer stx = (EdgeArrowStrokeTransformer) ps.getView().getRenderContext().getEdgeArrowStrokeTransformer();
		stx.setEdges( edgeHash );

		// repaint it
		ps.getView().repaint();
	}

	/**
	 * Method putEdgesInHash. Puts the new relationships in the in-memory graph
	 * hashtable.
	 *
	 * @param edges Vector<DBCMEdge> The Vector of new edges.
	 * @param hash Hashtable<String,DBCMEdge> The hashtable to be updated.
	 *
	 * @return Hashtable<String,DBCMEdge> The updated hashtable.
	 */
	private Map<SEMOSSEdge, Double> putEdgesInHash( Collection<SEMOSSEdge> edges,
			Map<SEMOSSEdge, Double> hash ) {
		for ( SEMOSSEdge e : edges ) {
			hash.put( e, 3d ); // RPB: I'm just making this up.
		}

		return hash;
	}

}
