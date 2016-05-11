/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.SimpleEdgeSupport;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * The stock EditingGraphMousePlugin breaks when you have an observable graph
 * observing a directed graph. It always thinks the observable graph is
 * undirected. This code is basically copied directly from the superclass
 * (ugh!).
 *
 * @author ryan
 * @param <V>
 * @param <E>
 */
public class QueryGraphMousePlugin<V, E> extends EditingGraphMousePlugin<V, E> {

	private enum Creating {

		EDGE, VERTEX, MOVE, UNDETERMINED
	}
	private Creating createMode = Creating.UNDETERMINED;
	private V moveVertex = null;
	private final EasyEdgeSupport<V, E> edger;

	public QueryGraphMousePlugin( Supplier<V> vertexFactory, Supplier<E> edgeFactory ) {
		super( vertexFactory, edgeFactory );
		edger = new EasyEdgeSupport<>( edgeFactory );
		setEdgeSupport( edger );
	}

	public QueryGraphMousePlugin( int modifiers, Supplier<V> vertexFactory, Supplier<E> edgeFactory ) {
		super( modifiers, vertexFactory, edgeFactory );
		edger = new EasyEdgeSupport<>( edgeFactory );
		setEdgeSupport( edger );
	}

	/**
	 * If the mouse is pressed in an empty area, create a new vertex there. If the
	 * mouse is pressed on an existing vertex, prepare to create an edge from that
	 * vertex to another
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public void mousePressed( MouseEvent e ) {
		if ( checkModifiers( e ) ) {
			final VisualizationViewer<V, E> vv
					= (VisualizationViewer<V, E>) e.getSource();
			final Point2D p = e.getPoint();
			GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
			if ( pickSupport != null ) {
				final V vertex = pickSupport.getVertex( vv.getModel().getGraphLayout(), p.getX(), p.getY() );
				if ( vertex != null ) {
					this.createMode = ( e.isControlDown() ? Creating.MOVE
							: Creating.EDGE );

					if ( e.isControlDown() ) {
						this.createMode = Creating.MOVE;
						moveVertex = vertex;
					}
					else {// get ready to make an edge
						this.createMode = Creating.EDGE;
						edgeSupport.startEdgeCreate( vv, vertex, e.getPoint(), EdgeType.DIRECTED );
					}
				}
				else { // make a new vertex
					this.createMode = Creating.VERTEX;
					vertexSupport.startVertexCreate( vv, e.getPoint() );
				}
			}
		}
	}

	/**
	 * If startVertex is non-null, and the mouse is released over an existing
	 * vertex, create an undirected edge from startVertex to the vertex under the
	 * mouse pointer. If shift was also pressed, create a directed edge instead.
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public void mouseReleased( MouseEvent e ) {
		if ( checkModifiers( e ) ) {
			final VisualizationViewer<V, E> vv
					= (VisualizationViewer<V, E>) e.getSource();
			final Point2D p = e.getPoint();
			Layout<V, E> layout = vv.getGraphLayout();
			if ( createMode == Creating.EDGE ) {
				GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
				V vertex = null;
				if ( pickSupport != null ) {
					vertex = pickSupport.getVertex( layout, p.getX(), p.getY() );
				}
				if ( null == vertex ) {
					edger.cleanup( vv );
				}
				else {
					edgeSupport.endEdgeCreate( vv, vertex );
				}
			}
			else if ( createMode == Creating.VERTEX ) {
				vertexSupport.endVertexCreate( vv, e.getPoint() );
			}
		}
		createMode = Creating.UNDETERMINED;
	}

	/**
	 * If startVertex is non-null, stretch an edge shape between startVertex and
	 * the mouse pointer to simulate edge creation
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public void mouseDragged( MouseEvent e ) {
		if ( checkModifiers( e ) ) {
			VisualizationViewer<V, E> vv
					= (VisualizationViewer<V, E>) e.getSource();
			if ( createMode == Creating.EDGE ) {
				edgeSupport.midEdgeCreate( vv, e.getPoint() );
			}
			else if ( createMode == Creating.VERTEX ) {
				vertexSupport.midVertexCreate( vv, e.getPoint() );
			}
			else if ( Creating.MOVE == createMode ) {
				vv.getGraphLayout().setLocation( moveVertex, e.getPoint() );
			}
		}
	}

	private class EasyEdgeSupport<V, E> extends SimpleEdgeSupport<V, E> {

		public EasyEdgeSupport( Supplier<E> edgeFactory ) {
			super( edgeFactory );
			setEdgeType( EdgeType.DIRECTED );
		}

		public void cleanup( VisualizationViewer<V, E> vv ) {
			// trying to fix the superimposed arrows when we have nothing to connect to
			// FIXME: this doesn't actually work at the moment
			startVertex = null;
			super.getEdgeEffects().endEdgeEffects( vv );
			super.getEdgeEffects().endArrowEffects( vv );
			vv.repaint();
		}
	}
}
