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

import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.SemossGraphVisualization;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.VisualizationControlPanel;
import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.om.GraphModelListener;
import com.ostrichemulators.semtool.search.GraphSearchTextField;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.GraphLegendPanel;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.WeightDropDownButton;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.GraphNodeListener;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.TreeifyGraph;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import org.eclipse.rdf4j.model.IRI;

/**
 */
public class GraphPlaySheet extends ImageExportingPlaySheet implements PropertyChangeListener {

	private static final String PROPERTY_PREF = "GraphPropertyLocation";
	private static final long serialVersionUID = 4699492732234656487L;
	private static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private SemossGraphVisualization view;
	private JSplitPane graphSplitPane;
	private final VisualizationControlPanel control;

	private GraphDataModel gdm;

	private int overlayLevel = 0;
	private int maxOverlayLevel = 0;

	private final List<GraphListener> listenees = new ArrayList<>();
	private boolean inGraphOp = false;
	private GraphColorShapeRepository shaper;

	private final Action undo = new AbstractAction( "Undo", GuiUtility.loadImageIcon( "undo.png" ) ) {
		@Override
		public void actionPerformed( ActionEvent e ) {
			undoView();
		}
	};

	private final Action redo = new AbstractAction( "Redo", GuiUtility.loadImageIcon( "redo.png" ) ) {
		@Override
		public void actionPerformed( ActionEvent e ) {
			redoView();
		}
	};

	private final Action reset = new AbstractAction( "Reset", GuiUtility.loadImageIcon( "refresh.png" ) ) {
		@Override
		public void actionPerformed( ActionEvent e ) {
			if ( xray.isSelected() ) {
				xray.doClick();
			}
			view.clearHighlighting();
		}
	};

	private final JToggleButton graphprops = new JToggleButton();
	private final JToggleButton xray = new JToggleButton();
	private final TreeifyGraph tree = new TreeifyGraph();
	private final WeightDropDownButton weightButton = new WeightDropDownButton();
	private final GraphSearchTextField searcher = new GraphSearchTextField();

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public GraphPlaySheet() {
		this( new GraphDataModel() );
	}

	public GraphPlaySheet( GraphDataModel model ) {
		this( model, new VisualizationControlPanel() );
	}

	public GraphPlaySheet( GraphDataModel model, VisualizationControlPanel vcp ) {
		log.debug( "new graphplaysheet" );
		shaper = DIHelper.getInstance().getPlayPane().getColorShapeRepository();
		gdm = model;

		undo.setEnabled( false );
		redo.setEnabled( false );

		control = vcp;
		control.setLabelCache( getLabelCache() );

		graphSplitPane = new JSplitPane();
		graphSplitPane.setOneTouchExpandable( false );
		graphSplitPane.setOrientation( JSplitPane.HORIZONTAL_SPLIT );

		setLayout( new BorderLayout() );
		add( graphSplitPane, BorderLayout.CENTER );
		GraphLegendPanel legendPanel = new GraphLegendPanel( getLabelCache() );
		legendPanel.setMinimumSize( new Dimension( 40, 40 ) );
		add( legendPanel, BorderLayout.SOUTH );

		view = new SemossGraphVisualization( gdm );
		initVisualizer( view );
		view.addPickingSupport();
		control.setVisualization( view );
		control.setMinimumSize( new Dimension( 0, 0 ) );

		graphSplitPane.setLeftComponent( new GraphZoomScrollPane( view ) );
		graphSplitPane.setRightComponent( control );
		graphSplitPane.setDividerLocation( 1d );
		graphSplitPane.setResizeWeight( 1d );
		graphSplitPane.setDividerSize( 0 );

		addGraphListener( legendPanel );
		addGraphListener( control );

		searcher.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		searcher.setColumns( 15 );

		gdm.addModelListener( new GraphModelListener() {

			@Override
			public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					int level, GraphDataModel gdm ) {
				if ( !inGraphOp ) {
					searcher.index( graph );
				}
			}
		} );
	}

	@Override
	public void setLabelCache( RetrievingLabelCache t ) {
		super.setLabelCache( t );
		control.setLabelCache( t );
		view.setLabelCache( t );
	}

	@Override
	public void setFrame( PlaySheetFrame f ) {
		super.setFrame( f );
		attachActions();

		if ( gdm.getGraph().getVertexCount() > 0 ) {
			searcher.index( gdm.getGraph() );
			fireGraphUpdated();
		}
	}

	public GraphColorShapeRepository getShapeRepository() {
		return shaper;
	}

	public void setShapeRepository( GraphColorShapeRepository gsr ) {
		shaper = gsr;
		view.setColorShapeRepository( gsr );
		view.refresh();
	}

	protected void attachActions() {
		graphprops.setAction( new AbstractAction( "Graph Properties" ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				Preferences prefs = Preferences.userNodeForPackage( getClass() );

				graphSplitPane.setOneTouchExpandable( graphprops.isSelected() );

				if ( graphprops.isSelected() ) {
					graphSplitPane.setDividerSize( 10 );
					int pos = prefs.getInt( PROPERTY_PREF, -1 );
					if ( pos < 0 ) {
						graphSplitPane.setDividerLocation( 0.75 );
					}
					else {
						graphSplitPane.setDividerLocation( pos );
					}
				}
				else {
					graphSplitPane.setDividerSize( 0 );
					int loc = graphSplitPane.getDividerLocation();
					int width = getWidth() - 20;
					if ( loc < width ) { // don't save a "closed" pref panel's location
						prefs.putInt( PROPERTY_PREF, graphSplitPane.getDividerLocation() );
					}
					else {
						prefs.remove( PROPERTY_PREF );
					}
					graphSplitPane.setDividerLocation( 1d );
				}
			}
		} );

		weightButton.setPlaySheet( this );
		searcher.setPlaySheet( this );
		tree.setPlaySheet( this );

		xray.setAction( new AbstractAction( "",
				GuiUtility.loadImageIcon( "search.png" ) ) {
					@Override
					public void actionPerformed( ActionEvent e ) {
						view.setSkeletonMode( xray.isSelected() );
					}
				} );
		xray.setToolTipText( "X-Ray Highlighting" );

		reset.putValue( Action.SHORT_DESCRIPTION, "Reset graph transformers" );

		view.getPickedVertexState().addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged( ItemEvent e ) {
				Set<? extends SEMOSSVertex> picks
						= view.getPickedVertexState().getPicked();
				tree.setEnabled( !picks.isEmpty() );
			}
		} );
	}

	@Override
	public void populateToolBar( JToolBar toolBar, String tabTitle ) {
		super.populateToolBar( toolBar, tabTitle );

		toolBar.add( graphprops );
		toolBar.add( reset );
		toolBar.add( weightButton );
		toolBar.add( undo );
		toolBar.add( redo );
		toolBar.addSeparator();
		toolBar.add( tree );
		toolBar.add( searcher );
		toolBar.add( xray );
	}

	/**
	 * Gets the visible graph as a SimpleGraph. This function differs from 
	 * {@link GraphDataModel#asSimpleGraph() } because only visible nodes are
	 * included
	 *
	 * @return
	 */
	public SimpleGraph<SEMOSSVertex, SEMOSSEdge> asSimpleGraph() {
		Graph<SEMOSSVertex, SEMOSSEdge> visible = getVisibleGraph();

		SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph
				= new SimpleGraph<>( SEMOSSEdge.class );

		for ( SEMOSSVertex v : visible.getVertices() ) {
			graph.addVertex( v );
		}

		for ( SEMOSSEdge e : visible.getEdges() ) {
			SEMOSSVertex s = visible.getSource( e );
			SEMOSSVertex d = visible.getDest( e );
			graph.addEdge( s, d, e );
		}

		return graph;
	}

	/**
	 * This function differs from {@link GraphDataModel#getGraph() } because only
	 * visible nodes are included
	 *
	 * @return
	 */
	public DirectedGraph<SEMOSSVertex, SEMOSSEdge> getVisibleGraph() {
		return view.getGraph();
	}

	@Override
	protected BufferedImage getExportImage() {

		Dimension d = view.getGraphLayout().getSize();
		d.setSize( d.getWidth() * 1.2, d.getHeight() * 1.2 );

		VisualizationImageServer<SEMOSSVertex, SEMOSSEdge> vis
				= new VisualizationImageServer<>( view.getGraphLayout(), d );

		vis.setBackground( view.getBackground() );
		vis.setRenderContext( view.getRenderContext() );

		try {
			// the visualization server seems to take a little time to render
			// correctly. Wait a little before processing the image
			// (this is really just voodoo...I don't know if/why it works)
			Thread.sleep( 500 );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		BufferedImage image = (BufferedImage) vis.getImage(
				new Point2D.Double( view.getGraphLayout().getSize().getWidth(),
						view.getGraphLayout().getSize().getHeight() ),
				new Dimension( view.getGraphLayout().getSize() ) );
		return image;
	}

	public GraphDataModel getGraphData() {
		return gdm;
	}

	public void setGraphData( GraphDataModel gdm ) {
		this.gdm = gdm;
	}

	/**
	 * Method undoView. Get the latest view and undo it.
	 */
	public void undoView() {
		if ( overlayLevel > 1 ) {
			overlayLevel--;
			setUndoRedoBtn();
			view.refresh();
			fireGraphUpdated();
		}
	}

	/**
	 * Method redoView.
	 */
	public void redoView() {
		if ( overlayLevel < maxOverlayLevel ) {
			overlayLevel++;
			setUndoRedoBtn();
			view.refresh();
			fireGraphUpdated();
		}
	}

	/**
	 * Regenerates all the data needed to display the graph
	 */
	public void updateGraph() {
		searcher.index( gdm.getGraph() );
		setUndoRedoBtn();
		fireGraphUpdated();

		if ( log.isTraceEnabled() ) {
			log.debug( "GRAPH UPDATE!" );
			for ( SEMOSSVertex v : gdm.getGraph().getVertices() ) {
				log.debug( "vertex: " + v );
				for ( Map.Entry<IRI, Value> en : v.getValues().entrySet() ) {
					log.debug( "\t" + en.getKey().getLocalName() + " -> " + en.getValue().stringValue() );
				}
			}

			for ( SEMOSSEdge v : gdm.getGraph().getEdges() ) {
				log.debug( "edge: " + v );
				for ( Map.Entry<IRI, Value> en : v.getValues().entrySet() ) {
					log.debug( "\t" + en.getKey().getLocalName() + " -> " + en.getValue().stringValue() );
				}
			}
		}
	}

	protected GraphNodeListener getGraphNodeListener() {
		return new GraphNodeListener( this );
	}

	/**
	 * Method initVisualizer.
	 */
	private void initVisualizer( SemossGraphVisualization viewer ) {
		GraphNodeListener gl = getGraphNodeListener();
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		viewer.setGraphMouse( gl );
		viewer.setLabelCache( getLabelCache() );

		viewer.setColorShapeRepository( shaper );

		viewer.addPropertyChangeListener( SemossGraphVisualization.VISIBILITY_CHANGED,
				new PropertyChangeListener() {

					@Override
					public void propertyChange( PropertyChangeEvent evt ) {
						fireGraphUpdated();
					}
				} );

		viewer.addPropertyChangeListener( SemossGraphVisualization.LAYOUT_CHANGED,
				new PropertyChangeListener() {

					@Override
					public void propertyChange( PropertyChangeEvent evt ) {
						fireLayoutUpdated( gdm.getGraph(), "",
								(Layout<SEMOSSVertex, SEMOSSEdge>) evt.getNewValue() );
					}
				} );
	}

	/**
	 * Method setUndoRedoBtn.
	 */
	private void setUndoRedoBtn() {
		undo.setEnabled( overlayLevel > 1 );
		redo.setEnabled( maxOverlayLevel > overlayLevel );
		view.setOverlayLevel( overlayLevel );
	}

	public MultiMap<IRI, SEMOSSVertex> getVerticesByType() {
		MultiMap<IRI, SEMOSSVertex> typeToInstances = new MultiMap<>();
		for ( SEMOSSVertex v : getVisibleGraph().getVertices() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	public MultiMap<IRI, SEMOSSEdge> getEdgesByType() {
		MultiMap<IRI, SEMOSSEdge> typeToInstances = new MultiMap<>();
		for ( SEMOSSEdge v : getVisibleGraph().getEdges() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	public void fireLayoutUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldName, Layout<SEMOSSVertex, SEMOSSEdge> layout ) {
		for ( GraphListener gl : listenees ) {
			try {
				gl.layoutChanged( graph, oldName, layout, this );
			}
			catch ( Exception ex ) {
				log.error( "Error updating layout for GraphListener " + gl + ": " + ex, ex );
			}
		}
	}

	private void fireGraphUpdated() {
		DirectedGraph<SEMOSSVertex, SEMOSSEdge> g = getGraphData().getGraph();
		for ( GraphListener gl : listenees ) {
			try {
				gl.graphUpdated( g, this );
			}
			catch ( Exception ex ) {
				log.error( "Error updating graph for GraphListener " + gl + ": " + ex, ex );
			}
		}
	}

	public final void addGraphListener( GraphListener gl ) {
		listenees.add( gl );
	}

	public final void removeGraphListener( GraphListener gl ) {
		listenees.remove( gl );
	}

	public SemossGraphVisualization getView() {
		return view;
	}

	@Override
	public void create( Model m, IEngine engine ) {
		add( m, null, engine );
	}

	/**
	 * Creates graph nodes from the given data. If the {@code Value[]}s have
	 * length 1, the values are expected to be nodes ({@link Resource}s. If they
	 * have length 3, then they are repackaged as Statements, and forwarded on to
	 * {@link #create(org.eclipse.rdf4j.model.Model, com.ostrichemulators.semtool.rdf.engine.api.IEngine) }.
	 * Anything else will throw an exception
	 *
	 * @param data the data to add are expected to be
	 * @param headers ignored
	 * @param engine
	 * @throws IllegalArgumentException
	 *
	 */
	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		List<IRI> nodes = new ArrayList<>();
		Model model = new LinkedHashModel();
		for ( Value[] row : data ) {
			IRI s = IRI.class.cast( row[0] );
			if ( 1 == row.length ) {
				nodes.add( s );
			}
			else if ( 3 == row.length ) {
				IRI p = IRI.class.cast( row[1] );
				Value o = row[2];

				model.add( s, p, o );
			}
			else {
				throw new IllegalArgumentException( "Values cannot be converted for graph usage" );
			}
		}

		add( model, nodes, engine );
	}

	@Override
	public void overlay( Model m, IEngine engine ) {
		add( m, null, engine );
	}

	/**
	 * Redirects to {@link #create(java.util.List, java.util.List,
	 * com.ostrichemulators.semtool.rdf.engine.api.IEngine) }
	 *
	 * @param data
	 * @param headers
	 * @param eng
	 */
	@Override
	public void overlay( List<Value[]> data, List<String> headers, IEngine eng ) {
		create( data, headers, eng );
	}

	protected void add( Model m, List<IRI> nodes, IEngine engine ) {
		getLabelCache().setEngine( engine );
		setHeaders( Arrays.asList( "Subject", "Predicate", "Object" ) );
		if ( null == nodes ) {
			nodes = new ArrayList<>();
		}
		if ( null == m ) {
			m = new LinkedHashModel();
		}

		if ( m.isEmpty() && nodes.isEmpty() ) {
			return; // nothing to add to the graph
		}

		inGraphOp = true;
		if ( overlayLevel < maxOverlayLevel ) {
			Collection<GraphElement> removed = gdm.removeElementsSinceLevel( overlayLevel );
			for ( GraphElement ge : removed ) {
				ge.removePropertyChangeListener( this );
			}
		}

		overlayLevel++;
		view.setOverlayLevel( overlayLevel );

		List<GraphElement> added = new ArrayList<>();
		if ( !m.isEmpty() ) {
			added.addAll( gdm.addGraphLevel( m, engine, overlayLevel ) );
		}
		if ( !nodes.isEmpty() ) {
			added.addAll( gdm.addGraphLevel( nodes, engine, overlayLevel ) );
		}

		for ( GraphElement e : added ) {
			e.addPropertyChangeListener( this );
		}

		if ( overlayLevel > maxOverlayLevel ) {
			maxOverlayLevel = overlayLevel;
		}
		inGraphOp = false;

		updateGraph();
	}

	@Override
	public boolean canAcceptDataWithHeaders( List<String> newheaders ) {
		// we can accept either a model's headers, or a single header 
		return ( Arrays.asList( "Subject", "Predicate", "Object" ).equals( newheaders )
				|| ( 1 == newheaders.size() ) );
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		view.incrementFont( incr );
	}

	protected boolean isloading() {
		return inGraphOp;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt ) {
		if ( !isloading() ) {
			view.refresh();
			fireGraphUpdated();
		}
	}

	protected int getOverlayLevel() {
		return overlayLevel;
	}
}
