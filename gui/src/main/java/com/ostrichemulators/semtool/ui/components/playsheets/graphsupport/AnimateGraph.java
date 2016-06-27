/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import com.ostrichemulators.semtool.om.GraphElement;
import edu.uci.ics.jung.graph.DirectedGraph;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.GraphAnimationPanel;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import edu.uci.ics.jung.algorithms.layout.Layout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class AnimateGraph extends AbstractAction {

	private static final Logger log = Logger.getLogger( AnimateGraph.class );
	private final GraphPlaySheet gps;

	public AnimateGraph( GraphPlaySheet ps ) {
		super( "Animate Graph" );
		putValue( Action.SHORT_DESCRIPTION,
				"Animate the graph using an edge property" );
		gps = ps;

		gps.addGraphListener( new GraphListener() {

			@Override
			public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					GraphPlaySheet gps ) {
				Collection<? extends GraphElement> edges = gps.getVisibleGraph().getEdges();
				setEnabled( GraphAnimationPanel.hasAnimationCandidates( edges ) );
			}

			@Override
			public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout, GraphPlaySheet gps ) {
				// don't care
			}
		} );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Collection<? extends GraphElement> edges = gps.getVisibleGraph().getEdges();

		Map<URI, URI> map = GraphAnimationPanel.getAnimationInput(
				JOptionPane.getRootFrame(), gps.getLabelCache(),
				gps.getEngine(), edges );


		for( Map.Entry<URI, URI> ee : map.entrySet() ){
			log.debug( ee );
		}
	}
}
