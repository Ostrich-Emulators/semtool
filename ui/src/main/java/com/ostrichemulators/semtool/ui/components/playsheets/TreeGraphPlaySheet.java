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

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.om.TreeGraphDataModel;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.DuplicatingPickedStateListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.GraphNodeListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.RingsButtonListener;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import java.awt.event.ItemListener;
import java.util.Set;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

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
		fixVis();

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
		rings.setGraph( asForest() );
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

	private void fixVis() {
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view = getView();
		GraphNodeListener gl = new GraphNodeListener( this );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		view.setGraphMouse( gl );

		ItemListener il = new DuplicatingPickedStateListener( view, this );
		view.getPickedEdgeState().addItemListener( il );
		view.getPickedVertexState().addItemListener( il );
	}

	public Set<SEMOSSVertex> getDuplicates( SEMOSSVertex v ) {
		return model.getDuplicatesOf( v );
	}

	@Override
	public SEMOSSVertex getRealVertex( SEMOSSVertex v ) {
		return model.getRealVertex( v );
	}

	@Override
	public SEMOSSEdge getRealEdge( SEMOSSEdge v ) {
		return model.getRealEdge( v );
	}
}
