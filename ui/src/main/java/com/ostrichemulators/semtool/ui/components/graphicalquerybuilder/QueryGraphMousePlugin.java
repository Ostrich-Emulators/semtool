/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import java.awt.event.MouseEvent;

/**
 * The stock EditingGraphMousePlugin breaks when you have an observable graph
 * observing a directed graph. It always think the observable graph is
 * undirected.
 *
 * @author ryan
 */
public class QueryGraphMousePlugin<V, E> extends EditingGraphMousePlugin<V, E> {

	public QueryGraphMousePlugin( Supplier<V> vertexFactory, Supplier<E> edgeFactory ) {
		super( vertexFactory, edgeFactory );
	}

	public QueryGraphMousePlugin( int modifiers, Supplier<V> vertexFactory, Supplier<E> edgeFactory ) {
		super( modifiers, vertexFactory, edgeFactory );
	}

	@Override
	public void mousePressed( MouseEvent e ) {
		super.mousePressed( e );
		// edgeIsDirected = EdgeType.DIRECTED;
	}
}
