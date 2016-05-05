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

import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.GraphModelListener;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.om.TreeGraphDataModel;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.RingsButtonListener;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.openrdf.model.URI;

/**
 */
public class TreeGraphPlaySheet extends GraphPlaySheet {

	private static final Logger log = Logger.getLogger( TreeGraphPlaySheet.class );
	private TreeGraphDataModel model;
	private final RingsButtonListener rings = new RingsButtonListener();

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public TreeGraphPlaySheet() {
		this( new TreeGraphDataModel(), TreeLayout.class );
	}

	public TreeGraphPlaySheet( TreeGraphDataModel model, Class<? extends Layout> klass ) {
		super( model );
		this.model = model;
		log.debug( "new treeplaysheet" );

		getView().setGraphLayout( klass );
		addSelectionExpander();

		model.addModelListener( new GraphModelListener() {

			@Override
			public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphDataModel gdm ) {
				updateLabels();
			}
		} );

		for ( SEMOSSVertex v : model.getForest().getVertices() ) {
			v.addPropertyChangeListener( this );
		}

		for ( SEMOSSEdge e : model.getForest().getEdges() ) {
			e.addPropertyChangeListener( this );
		}
	}

	@Override
	public void populateToolBar( JToolBar toolBar, String tabTitle ) {
		super.populateToolBar( toolBar, tabTitle );
		rings.setViewer( getView() );
		rings.setGraph( model.getForest() );
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

	@Override
	public void setFrame( PlaySheetFrame f ) {
		super.setFrame( f );
		updateLabels();
	}

	private void updateLabels() {
		// we want our duplicate (random) URIs to resolve to their true labels
		RetrievingLabelCache rlc = getLabelCache();
		Map<URI, URI> map = new HashMap<>();

		List<GraphElement> all = new ArrayList<>();
		all.addAll( model.getGraph().getVertices() );
		all.addAll( model.getGraph().getEdges() );

		for ( GraphElement e : all ) {
			map.put( e.getURI(), model.getRealUri( e ) );
		}

		Map<URI, String> labels = Utility.getInstanceLabels( map.values(), getEngine() );
		rlc.putAll( labels );

		for ( Map.Entry<URI, URI> en : map.entrySet() ) {
			rlc.put( en.getKey(), rlc.get( en.getValue() ) );
		}

		rlc.put( TreeGraphDataModel.DUPLICATES_SIZE, "Duplicates" );
		rlc.put( TreeGraphDataModel.DUPLICATE_OF, "True Identity" );
	}

	private void addSelectionExpander() {
		// we want to highlight all nodes/edges that share the same "true" element
		ItemListener il = new ItemListener() {
			@Override
			public void itemStateChanged( ItemEvent e ) {
				// increase/decrease the size of nodes as they get selected/unselected
				GraphElement ele = GraphElement.class.cast( e.getItem() );
				Set<? extends GraphElement> expandeds = model.getDuplicatesOf( ele );

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
}
