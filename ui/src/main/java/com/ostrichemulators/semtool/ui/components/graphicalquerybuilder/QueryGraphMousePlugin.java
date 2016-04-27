/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import java.awt.event.MouseEvent;
import org.apache.commons.collections15.Factory;
import org.apache.log4j.Logger;

/**
 * The stock EditingGraphMousePlugin breaks when you have an observable graph
 * observing a directed graph. It always think the observable graph is
 * undirected.
 *
 * @author ryan
 */
public class QueryGraphMousePlugin<V, E> extends EditingGraphMousePlugin<V, E> {

	public QueryGraphMousePlugin( Factory<V> vertexFactory, Factory<E> edgeFactory ) {
		super( vertexFactory, edgeFactory );
	}

	public QueryGraphMousePlugin( int modifiers, Factory<V> vertexFactory, Factory<E> edgeFactory ) {
		super( modifiers, vertexFactory, edgeFactory );
	}

	@Override
	public void mousePressed( MouseEvent e ) {
		super.mousePressed( e );
		edgeIsDirected = EdgeType.DIRECTED;
	}
}
