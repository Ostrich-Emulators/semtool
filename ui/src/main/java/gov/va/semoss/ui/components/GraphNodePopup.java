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
package gov.va.semoss.ui.components;

import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import gov.va.semoss.algorithm.impl.DistanceDownstreamProcessor;
import gov.va.semoss.algorithm.impl.IslandIdentifierProcessor;
import gov.va.semoss.algorithm.impl.LoopIdentifierProcessor;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.ChartItPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.main.listener.impl.AdjacentPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.CondenseGraph;
import gov.va.semoss.ui.main.listener.impl.GraphNodeRankListener;
import gov.va.semoss.ui.main.listener.impl.GraphPlaySheetExportListener;
import gov.va.semoss.ui.main.listener.impl.HideVertexPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.MSTPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.MouseTransformPickPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.UnHideVertexPopupMenuListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
	private final Set<SEMOSSVertex> highlightedVertices;
	private SEMOSSVertex pickedVertex;
	private final IEngine engine;
	private final boolean forTree;

	public GraphNodePopup( GraphPlaySheet gps, SEMOSSVertex pickedVertex,
			SEMOSSVertex[] highlightedVertices, boolean forTree ) {

		this.forTree = forTree;
		this.gps = gps;
		this.highlightedVertices = new HashSet<>( Arrays.asList( highlightedVertices ) );
		this.pickedVertex = pickedVertex;

		if ( 1 == this.highlightedVertices.size() ) {
			this.pickedVertex = highlightedVertices[0];
		}

		if ( this.pickedVertex != null && this.highlightedVertices.isEmpty() ) {
			this.highlightedVertices.add( this.pickedVertex );
		}

		engine = gps.getEngine();

		addHighlightingOptions();
		addGraphOptions();
		addDataOptions();
		addSOATransitionOptions();
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

		JMenuItem item = add( "Hide Nodes" );
		item.addActionListener( new HideVertexPopupMenuListener( highlightedVertices ) );
		item.setEnabled( !highlightedVertices.isEmpty() && gps.areNodesHidable() );

		item = add( "Unhide Nodes" );
		item.addActionListener( new UnHideVertexPopupMenuListener( gps ) );
		item.setEnabled( gps.areNodesHidable() );
	}

	private void addCosmeticsOptions() {
		addSeparator();

		JMenuItem item = add( new ColorPopup( gps, highlightedVertices ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
		item.setEnabled( !highlightedVertices.isEmpty() );

		item = add( new ShapePopup( gps, highlightedVertices ) );
		item.setToolTipText( "Modify overall appearance of the graph" );
		item.setEnabled( !highlightedVertices.isEmpty() );

		item = add( new LayoutPopup( "Modify Layout", gps, highlightedVertices ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
	}

	private void addTraverseAndAlgorithmOptions() {
		addSeparator();

		if ( pickedVertex == null || !gps.isTraversable() ) {
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

	private void addSOATransitionOptions() {
		addSeparator();

		JMenuItem item = add( "SOA Transition All" );
		item.setEnabled( containsICDType() );
	}

	private void addDataOptions() {
		addSeparator();

		add( new GraphPlaySheetExportListener( gps ) );
		add( new NodeInfoPopup( gps, highlightedVertices ) );

		if ( !forTree ) {
			JMenuItem item = add( new NodePropertiesPopup( gps, highlightedVertices ) );
			item.setEnabled( highlightedVertices.size() >= 1 );
			
			add( new CondenseGraph( gps ) );
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

	/**
	 * Checks whether the node type represents an interface control document.
	 *
	 * @return boolean True if the type of node represents an ICD.
	 */
	public final boolean containsICDType() {
		for ( SEMOSSVertex vertex : gps.getGraphData().getGraph().getVertices() ) {
			if ( vertex.getType().stringValue().equals( "InterfaceControlDocument" ) ) {
				return true;
			}
		}

		return false;
	}

	public void show( MouseEvent event ) {
		super.show( event.getComponent(), event.getX(), event.getY() );
	}
}
