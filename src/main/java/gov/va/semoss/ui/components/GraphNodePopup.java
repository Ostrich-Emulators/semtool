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

import gov.va.semoss.algorithm.impl.DistanceDownstreamProcessor;
import gov.va.semoss.algorithm.impl.IslandIdentifierProcessor;
import gov.va.semoss.algorithm.impl.LoopIdentifierProcessor;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.BrowserTabSheet3;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.main.listener.impl.AdjacentPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.GraphNodeRankListener;
import gov.va.semoss.ui.main.listener.impl.GraphPlaySheetExportListener;
import gov.va.semoss.ui.main.listener.impl.HideVertexPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.MSTPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.MousePickingPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.MouseTransformPopupMenuListener;
import gov.va.semoss.ui.main.listener.impl.UnHideVertexPopupMenuListener;
import gov.va.semoss.util.DIHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import java.util.List;
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
	private SEMOSSVertex[] highlightedVertices;
	private SEMOSSVertex pickedVertex;
	private final IEngine engine;

	public GraphNodePopup( GraphPlaySheet gps, SEMOSSVertex pickedVertex,
			SEMOSSVertex[] highlightedVertices ) {
		super();

		this.gps = gps;
		this.highlightedVertices = highlightedVertices;
		this.pickedVertex = pickedVertex;

		if ( this.highlightedVertices.length == 1 ) {
			this.pickedVertex = this.highlightedVertices[0];
		}

		if ( this.pickedVertex != null && this.highlightedVertices.length == 0 ) {
			this.highlightedVertices = new SEMOSSVertex[]{ this.pickedVertex };
		}

		engine = DIHelper.getInstance().getRdfEngine();

		addHighlightingOptions();
		addGraphOptions();
		addDataOptions();
		addSOATransitionOptions();
		addTraverseAndAlgorithmOptions();
		addCosmeticsOptions();
		addHidingOptions();
		addChartOptions();
	}

	private void addChartOptions() {
		addSeparator();

		JMenuItem item = add( "Create Custom Chart" );
		item.setToolTipText( "Invoke a screen to build a custom chart" );
		item.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				BrowserTabSheet3 tab
						= new BrowserTabSheet3( "/html/RDFSemossCharts/app/index.html", gps );
				gps.getPlaySheetFrame().addTab( "Custom Chart", tab );
				tab.pullData();
			}
		} );
	}

	private void addHidingOptions() {
		addSeparator();

		JMenuItem item = add( "Hide Nodes" );
		item.addActionListener( new HideVertexPopupMenuListener( gps,
				Arrays.asList( highlightedVertices ) ) );
		item.setEnabled( highlightedVertices.length > 0 && gps.areNodesHidable() );

		item = add( "Unhide Nodes" );
		item.addActionListener( new UnHideVertexPopupMenuListener( gps ) );
		item.setEnabled( gps.areNodesHidable() );
	}

	private void addCosmeticsOptions() {
		addSeparator();
		List<SEMOSSVertex> verts = Arrays.asList( highlightedVertices );

		JMenuItem item = add( new ColorPopup( "Modify Color", gps, verts ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
		item.setEnabled( highlightedVertices.length > 0 );

		item = add( new ShapePopup( "Modify Shape", gps, verts ) );
		item.setToolTipText( "Modify overall appearance of the graph" );
		item.setEnabled( highlightedVertices.length > 0 );

		item = add( new LayoutPopup( "Modify Layout", gps ) );
		item.setToolTipText( "To select nodes press Shift and click on nodes" );
	}

	private void addTraverseAndAlgorithmOptions() {
		addSeparator();
		List<SEMOSSVertex> verts = Arrays.asList( highlightedVertices );

		if ( pickedVertex == null || !gps.isTraversable() ) {
			add( "Traverse Freely" ).setEnabled( false );
		}
		else {
			add( new TFRelationPopup( pickedVertex, gps, highlightedVertices ) );
			add( new TFInstanceRelationPopup( pickedVertex, engine, gps, highlightedVertices ) );
		}

		JMenu menu = new JMenu( "Perform Algorithms" );
		menu.setToolTipText( "To select multiple nodes, hold Shift and left click on nodes." );
		menu.setEnabled( true );

		menu.add( new GraphNodeRankListener( gps ) );
		menu.add( new DistanceDownstreamProcessor( gps, verts ) );
		menu.add( new LoopIdentifierProcessor( gps, verts ) );
		menu.add( new IslandIdentifierProcessor( gps, verts ) );

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
		add( new NodeInfoPopup( gps, Arrays.asList( highlightedVertices ) ) );
	}

	private void addGraphOptions() {
		addSeparator();

		JMenuItem item = add( "Move Graph" );
		item.setToolTipText( "Move entire graph as a single unit" );
		item.addActionListener( new MouseTransformPopupMenuListener( gps.getView() ) );

		item = add( "Pick Graph" );
		item.addActionListener( new MousePickingPopupMenuListener( gps.getView() ) );
	}

	private void addHighlightingOptions() {
		List<SEMOSSVertex> verts = Arrays.asList( highlightedVertices );

		AdjacentPopupMenuListener highAdjBoth
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.ADJACENT,
						gps, verts );

		JMenu moreHighlight = new JMenu( "More Highlight Options" );

		AdjacentPopupMenuListener highAdjDown
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.DOWNSTREAM,
						gps, verts );

		AdjacentPopupMenuListener highAdjUp
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.UPSTREAM,
						gps, verts );

		AdjacentPopupMenuListener highAdjAll
				= new AdjacentPopupMenuListener( AdjacentPopupMenuListener.Type.ALL,
						gps, verts );

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
		for ( SEMOSSVertex vertex : gps.getFilterData().getGraph().getVertices() ) {
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
