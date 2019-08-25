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
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter;
import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.GraphModelListener;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.GraphNodeListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.RingsButtonListener;
import com.ostrichemulators.semtool.util.Utility;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 */
public class TreeGraphPlaySheet extends GraphPlaySheet {

	private static final Logger log = Logger.getLogger( TreeGraphPlaySheet.class );
	//private TreeGraphDataModel model;
	public static final IRI DUPLICATES_SIZE = Utility.makeInternalIRI( "num-duplicates" );

	private final RingsButtonListener rings = new RingsButtonListener();

	public TreeGraphPlaySheet( DirectedGraph<SEMOSSVertex, SEMOSSEdge> basegraph,
			Collection<SEMOSSVertex> roots, Class<? extends Layout> klass ) {
		super( new GraphDataModel( makeDuplicateForest( basegraph, roots,
				new GraphToTreeConverter() ) ) );
		setTitle( "Tree Conversion" );

		getView().setGraphLayout( klass );
		addSelectionExpander();

		GraphDataModel gdm = super.getGraphData();
		addDuplicateValues( gdm );
		gdm.addModelListener( new GraphModelListener() {

			@Override
			public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					int level, GraphDataModel gdm ) {

				for ( GraphElement ge : gdm.elementsFromLevel( level ) ) {
					ge.addPropertyChangeListener( TreeGraphPlaySheet.this );
				}

				addDuplicateValues( gdm );
			}
		} );
	}

	@Override
	protected GraphNodeListener getGraphNodeListener() {
		return new GraphNodeListener( this );
	}

	@Override
	public void populateToolBar( JToolBar toolBar, String tabTitle ) {
		super.populateToolBar( toolBar, tabTitle );

		rings.setViewer( getView() );
		rings.setGraph( Forest.class.cast( getGraphData().getGraph() ) );
		Layout lay = getView().getEffectiveLayout();
		rings.setEnabled( lay instanceof BalloonLayout || lay instanceof RadialTreeLayout );
		JToggleButton ringbtn = new JToggleButton( rings );
		toolBar.add( ringbtn );

		addGraphListener( new GraphListener() {
			@Override
			public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
				rings.setGraph( Forest.class.cast( graph ) );
			}

			@Override
			public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout, GraphPlaySheet gps ) {
				rings.setEnabled( newlayout instanceof BalloonLayout
						|| newlayout instanceof RadialTreeLayout );
			}
		} );
	}

	private void addDuplicateValues( GraphDataModel gdm ) {

		List<GraphElement> ll = new ArrayList<>();
		Graph<SEMOSSVertex, SEMOSSEdge> newforest = gdm.getGraph();
		ll.addAll( newforest.getVertices() );
		ll.addAll( newforest.getEdges() );

		ValueFactory vf = SimpleValueFactory.getInstance();
		for ( GraphElement ge : ll ) {
			ge.setValue( DUPLICATES_SIZE,
					vf.createLiteral( gdm.getDuplicatesOf( ge ).size() ) );
		}
	}

	private void addSelectionExpander() {
		// we want to highlight all nodes/edges that share the same "true" element
		getLabelCache().put( DUPLICATES_SIZE, "Duplicates" );

		GraphDataModel gdm = getGraphData();

		ItemListener il = new ItemListener() {
			@Override
			public void itemStateChanged( ItemEvent e ) {
				// increase/decrease the size of nodes as they get selected/unselected
				GraphElement ele = GraphElement.class.cast( e.getItem() );
				Set<? extends GraphElement> expandeds = gdm.getDuplicatesOf( ele );

				PickedState<SEMOSSVertex> vpicks = getView().getPickedVertexState();
				PickedState<SEMOSSEdge> epicks = getView().getPickedEdgeState();

				final boolean SELECTED = ( ItemEvent.SELECTED == e.getStateChange() );

				if ( ele.isNode() ) {
					for ( GraphElement ge : expandeds ) {
						vpicks.pick( SEMOSSVertex.class.cast( ge ), SELECTED );
					}
				}
				else {
					for ( GraphElement ge : expandeds ) {
						epicks.pick( SEMOSSEdge.class.cast( ge ), SELECTED );
					}
				}
			}
		};

		getView().getPickedEdgeState().addItemListener( il );
		getView().getPickedVertexState().addItemListener( il );
	}

	private static DirectedGraph<SEMOSSVertex, SEMOSSEdge> makeDuplicateForest(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots,
			GraphToTreeConverter gttc ) {

		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();
		for ( SEMOSSVertex root : roots ) {
			Map<IRI, SEMOSSVertex> vlkp = new HashMap<>();
			Map<IRI, SEMOSSEdge> elkp = new HashMap<>();

			Tree<IRI, IRI> tree = gttc.convert( graph, root, vlkp, elkp );

			// convert the IRI tree to our node/edge tree
			DelegateTree<SEMOSSVertex, SEMOSSEdge> dupetree = new DelegateTree<>();

			// make duplicate vertices and edges with the new URIs
			Map<IRI, SEMOSSVertex> dupesV = new HashMap<>();
			Map<IRI, SEMOSSEdge> dupesE = new HashMap<>();
			for ( Map.Entry<IRI, SEMOSSVertex> en : vlkp.entrySet() ) {
				dupesV.put( en.getKey(), duplicate( en.getKey(), vlkp ) );
			}
			for ( Map.Entry<IRI, SEMOSSEdge> en : elkp.entrySet() ) {
				dupesE.put( en.getKey(), duplicate( en.getKey(), elkp ) );
			}

			dupetree.setRoot( dupesV.get( tree.getRoot() ) );

			Deque<IRI> todo = new ArrayDeque<>( tree.getChildren( tree.getRoot() ) );
			while ( !todo.isEmpty() ) {
				IRI child = todo.poll();
				IRI edge = tree.getParentEdge( child );
				IRI parent = tree.getParent( child );

				dupetree.addChild( dupesE.get( edge ),
						dupesV.get( parent ), dupesV.get( child ) );

				todo.addAll( tree.getChildren( child ) );
			}

			newforest.addTree( dupetree );
		}

		return newforest;
	}

	private static <X extends GraphElement> X duplicate( IRI uri, Map<IRI, X> lkp ) {
		X old = lkp.get( uri );
		X newer = old.duplicate();
		newer.setGraphId( uri );

		return newer;
	}
}
