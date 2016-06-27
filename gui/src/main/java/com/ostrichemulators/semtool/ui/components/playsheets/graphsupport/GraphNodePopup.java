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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import com.ostrichemulators.semtool.algorithm.impl.DistanceDownstreamProcessor;
import com.ostrichemulators.semtool.algorithm.impl.IslandIdentifierProcessor;
import com.ostrichemulators.semtool.algorithm.impl.LoopIdentifierProcessor;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.TraverseFreelyPopup;
import com.ostrichemulators.semtool.ui.components.playsheets.ChartItPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class is used to create the right click popup menu for the graph
 * playsheet.
 */
public class GraphNodePopup extends JPopupMenu {

	private static final long serialVersionUID = 7106248215097748901L;

	private final GraphPlaySheet gps;
	private final Set<GraphElement> highlightedElements;
	private final Set<SEMOSSVertex> highlightedVertices = new HashSet<>();

	private GraphElement pickedVertex;
	private final IEngine engine;
	private final boolean forTree;

	public GraphNodePopup( GraphPlaySheet gps, GraphElement pickedVertex,
			SEMOSSVertex[] highlights, boolean forTree ) {

		this.forTree = forTree;
		this.gps = gps;
		this.highlightedElements = new HashSet<>( Arrays.asList( highlights ) );
		this.pickedVertex = pickedVertex;

		if ( 1 == this.highlightedElements.size() ) {
			this.pickedVertex = highlights[0];
		}

		if ( this.pickedVertex != null && this.highlightedElements.isEmpty() ) {
			this.highlightedElements.add( this.pickedVertex );
		}

		for ( GraphElement ge : highlightedElements ) {
			if ( ge.isNode() ) {
				highlightedVertices.add( SEMOSSVertex.class.cast( ge ) );
			}
		}

		engine = gps.getEngine();

		addHighlightingOptions();
		addGraphOptions();
		addDataOptions();

		if ( !forTree ) {
			addTraverseAndAlgorithmOptions();
		}
		addCosmeticsOptions();
		addHidingOptions();

		if ( !forTree ) {
			addChartOptions();
		}
	}

	private void addChartOptions() {
		addSeparator();

		JMenuItem item = add( "Create Custom Chart" );
		item.setToolTipText( "Invoke a screen to build a custom chart" );
		item.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				gps.addSibling( "Custom Chart", new ChartItPlaySheet( gps ) );
			}
		} );
	}

	private void addHidingOptions() {
		addSeparator();

		Action hider = new HideVertexPopupMenuListener( highlightedElements, gps.getView() );
		JMenuItem item = add( hider );
		hider.setEnabled( !highlightedElements.isEmpty() );

		Action unhider = new UnHideVertexPopupMenuListener( gps );
		item = add( unhider );
		item.setEnabled( gps.getView().isHidingSomething() );
	}

	private void addCosmeticsOptions() {
		addSeparator();

		JMenuItem item = add( new ColorPopup( gps, highlightedElements ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
		item.setEnabled( !highlightedElements.isEmpty() );

		item = add( new ShapePopup( gps, highlightedVertices ) );
		item.setToolTipText( "Modify overall appearance of the graph" );
		item.setEnabled( !highlightedElements.isEmpty() );

		item = add( new LayoutPopup( "Modify Layout", gps, highlightedVertices ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
	}

	private void addTraverseAndAlgorithmOptions() {
		addSeparator();

		if ( pickedVertex == null || !pickedVertex.isNode() ) {
			add( "Traverse Freely" ).setEnabled( false );
		}
		else {
			add( new TraverseFreelyPopup( pickedVertex, engine, gps, highlightedVertices, false ) );
			add( new TraverseFreelyPopup( pickedVertex, engine, gps, highlightedVertices, true ) );
		}

		JMenu menu = new JMenu( "Perform Algorithms" );
		menu.setToolTipText( "To select multiple nodes, hold Shift and left click on nodes." );
		menu.setEnabled( true );

		menu.add( new GraphNodeRankListener( gps ) );
		menu.add( new DistanceDownstreamProcessor( gps, highlightedVertices ) );
		menu.add( new LoopIdentifierProcessor( gps, highlightedVertices ) );
		menu.add( new IslandIdentifierProcessor( gps, highlightedVertices ) );

		add( menu );
	}

	private void addDataOptions() {
		addSeparator();

		add( new GraphPlaySheetTableExporter( gps ) );
		add( new GraphPlaySheetEdgeListExporter( gps ) );
		add( new NodeInfoPopup( gps, highlightedElements ) );

		if ( !forTree ) {
			JMenuItem item = add( new NodePropertiesPopup( gps, highlightedElements ) );
			item.setEnabled( highlightedElements.size() >= 1 );

			add( new CondenseGraph( gps ) );

			add( new AnimateGraph( gps ) );
		}
	}

	private void addGraphOptions() {
		addSeparator();
		add( new MouseTransformPickPopupMenuListener( gps.getView(),
				ModalGraphMouse.Mode.TRANSFORMING ) );
		add( new MouseTransformPickPopupMenuListener( gps.getView(),
				ModalGraphMouse.Mode.PICKING ) );
	}

	private void addHighlightingOptions() {

		AdjacentPopupMenuListener highAdjBoth
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.ADJACENT,
						gps, highlightedVertices );

		JMenu moreHighlight = new JMenu( "More Highlight Options" );

		AdjacentPopupMenuListener highAdjDown
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.DOWNSTREAM,
						gps, highlightedVertices );

		AdjacentPopupMenuListener highAdjUp
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.UPSTREAM,
						gps, highlightedVertices );

		AdjacentPopupMenuListener highAdjAll
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.ALL,
						gps, highlightedVertices );

		MSTPopupMenuListener MST = new MSTPopupMenuListener( gps );

		add( highAdjBoth );
		moreHighlight.add( highAdjUp );
		moreHighlight.add( highAdjDown );
		moreHighlight.add( highAdjAll );
		moreHighlight.add( MST );
		this.add( moreHighlight );
	}

	public void show( MouseEvent event ) {
		super.show( event.getComponent(), event.getX(), event.getY() );
	}
}
