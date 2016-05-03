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

import java.awt.BorderLayout;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.ControlData;
import com.ostrichemulators.semtool.ui.components.ControlPanel;
import com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter;
import com.ostrichemulators.semtool.ui.components.LegendPanel2;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.main.listener.impl.GraphNodeListener;
import com.ostrichemulators.semtool.ui.main.listener.impl.PickedStateListener;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 */
public class GraphPlaySheet extends ImageExportingPlaySheet implements PropertyChangeListener {

	private static final long serialVersionUID = 4699492732234656487L;
	private static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private SemossGraphVisualization view;
	private JSplitPane graphSplitPane;
	private final VisualizationControlPanel control;

	protected GraphDataModel gdm;
	protected String layoutName = Constants.FR;

	protected boolean traversable = true;
	protected boolean nodesHidable = true;

	protected int overlayLevel = 0;
	protected int maxOverlayLevel = 0;

	private final List<GraphListener> listenees = new ArrayList<>();
	private boolean inGraphOp = false;
	private ItemListener pickStateListener = null;
	private final RetrievingLabelCache labelcache = new RetrievingLabelCache();

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
		gdm = model;

		control = vcp;
		control.setLabelCache( labelcache );
		control.setVisible( false );

		graphSplitPane = new JSplitPane();
		graphSplitPane.setEnabled( false );
		graphSplitPane.setOneTouchExpandable( true );
		graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );

		setLayout( new BorderLayout() );
		add( graphSplitPane, BorderLayout.CENTER );
		LegendPanel2 legendPanel = new LegendPanel2( labelcache );
		add( legendPanel, BorderLayout.SOUTH );
		add( control, BorderLayout.EAST );

		view = new SemossGraphVisualization( gdm );
		initVisualizer( view );
		control.setVisualization( view );

		graphSplitPane.setBottomComponent( new GraphZoomScrollPane( view ) );

		addGraphListener( legendPanel );
		addGraphListener( control );
	}

	@Override
	public void populateToolBar( JToolBar toolBar, String tabTitle ) {
		super.populateToolBar( toolBar, tabTitle );
		JToggleButton tb = new JToggleButton();
		tb.setAction( new AbstractAction( "Graph Properties" ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				control.setVisible( tb.isSelected() );
			}
		} );
		toolBar.add( tb );
	}

	public Forest<SEMOSSVertex, SEMOSSEdge> asForest() {
		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		Forest<SEMOSSVertex, SEMOSSEdge> forest = ( graph instanceof Forest
				? Forest.class.cast( graph )
				: new GraphToTreeConverter().convert( graph,
						view.getPickedVertexState().getPicked() ) );
		GraphToTreeConverter.printForest( forest );
		return forest;
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
	 * Method resetView. Reset to original view.
	 */
	public void resetView() {
		if ( overlayLevel > 1 ) {
			overlayLevel = 1;
			view.setOverlayLevel( overlayLevel );
			updateLayout();
			fireGraphUpdated();
		}
	}

	/**
	 * Method undoView. Get the latest view and undo it.
	 */
	public void undoView() {
		if ( overlayLevel > 1 ) {
			overlayLevel--;
			view.setOverlayLevel( overlayLevel );
			updateLayout();
			setUndoRedoBtn();
			fireGraphUpdated();
		}
	}

	/**
	 * Method redoView.
	 */
	public void redoView() {
		if ( overlayLevel < maxOverlayLevel ) {
			overlayLevel++;
			updateLayout();
			setUndoRedoBtn();
			fireGraphUpdated();
		}
	}

	public void updateLayout() {
		view.setOverlayLevel( overlayLevel );
	}

	@Override
	public void activated() {
		// FilterPanel fp = DIHelper.getInstance().getPlayPane().getFilterPanel();
		// fp.setModels( nodemodel, edgemodel, propmodel, getEngine() );

//		Set<SEMOSSVertex> pickedVerts = getView().getPickedVertexState().getPicked();
//		Set<SEMOSSEdge> pickedEdges = getView().getPickedEdgeState().getPicked();
//		if ( !pickedVerts.isEmpty() ) {
//			propmodel.setVertex( pickedVerts.iterator().next() );
//		}
//		else if ( !pickedEdges.isEmpty() ) {
//			propmodel.setEdge( pickedEdges.iterator().next() );
//		}
	}

	/**
	 * Regenerates all the data needed to display the graph
	 */
	public void updateGraph() {
		//setLayoutName( layoutName );
		view.refresh();
		fireGraphUpdated();
		setUndoRedoBtn();
	}

	/**
	 * Method initVisualizer.
	 */
	private void initVisualizer( SemossGraphVisualization viewer ) {
		GraphNodeListener gl = new GraphNodeListener( this );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		view.setGraphMouse( gl );
		view.setLabelCache( labelcache );

		view.addPropertyChangeListener( SemossGraphVisualization.REPAINT_NEEDED,
				new PropertyChangeListener() {

					@Override
					public void propertyChange( PropertyChangeEvent evt ) {
						fireGraphUpdated();
					}
				} );

		setPicker( new PickedStateListener( viewer, this ) );
	}

	protected void setPicker( ItemListener psl ) {
		if ( null != pickStateListener ) {
			// remove the old listener
			view.getPickedVertexState().removeItemListener( pickStateListener );
			view.getPickedEdgeState().removeItemListener( pickStateListener );
		}
		if ( null != psl ) {
			pickStateListener = psl;
			view.getPickedVertexState().addItemListener( pickStateListener );
			view.getPickedEdgeState().addItemListener( pickStateListener );
		}
	}

	public String getLayoutName() {
		return layoutName;
	}

	/**
	 * Method setUndoRedoBtn.
	 */
	private void setUndoRedoBtn() {
		//controlPanel.setUndoButtonEnabled( overlayLevel > 1 );
		//controlPanel.setRedoButtonEnabled( maxOverlayLevel > overlayLevel );
	}

	public MultiMap<URI, SEMOSSVertex> getVerticesByType() {
		MultiMap<URI, SEMOSSVertex> typeToInstances = new MultiMap<>();
		for ( SEMOSSVertex v : getVisibleGraph().getVertices() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	public MultiMap<URI, SEMOSSEdge> getEdgesByType() {
		MultiMap<URI, SEMOSSEdge> typeToInstances = new MultiMap<>();
		for ( SEMOSSEdge v : getVisibleGraph().getEdges() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	/**
	 * Method getControlData.
	 *
	 * @return ControlData
	 */
	public ControlData getControlData() {
		//return controlData;
		return null;
	}

	/**
	 * Sets the layout of the visualization. The name must be a key pointing to a
	 * a class name in the semoss.properties file. If any error occurs, the layout
	 * is clearSelected to {@link Constants#FR}. (Not all layouts can support all
	 * graph topologies)
	 *
	 * @return true if the desired layout was applied
	 * @param layout String
	 */
	public boolean setLayoutName( String newName ) {
		String oldName = this.layoutName;
		this.layoutName = newName;

		Class<?> layoutClass = (Class<?>) DIHelper.getInstance().getLocalProp( layoutName );
		log.debug( "Create layout from layoutName " + layoutName
				+ ", and layoutClass " + layoutClass );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = getVisibleGraph();

		boolean ok = false;
		Layout<SEMOSSVertex, SEMOSSEdge> layout = null;
		try {
			Constructor<?> constructor = layoutClass.getConstructor( Graph.class );
			layout = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( graph );
			ok = true;
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			log.error( "could not create layout", e );
		}

		if ( null == layout ) {
			layout = new FRLayout<>( graph );
		}

		fitGraphinWindow();
		layout.initialize();
		try {
			double scale = 0.85;
			Dimension d = view.getSize();
			d.setSize( d.getWidth() * scale, d.getHeight() * scale );
			layout.setSize( d );
		}
		catch ( UnsupportedOperationException uoe ) {
			// you can set the layout size for some layouts...but there's no way to
			// know which ones
		}

		view.setGraphLayout( layout );

		fireLayoutUpdated( graph, oldName, layout );

		return ok;
	}

	/*
	 * This method tries to fit the graph to the available space.
	 * It could work better.
	 */
	protected void fitGraphinWindow() {
		MultiLayerTransformer mlt = view.getRenderContext().getMultiLayerTransformer();
		double vscalex = mlt.getTransformer( Layer.VIEW ).getScaleX() * 1.1;
		double vscaley = mlt.getTransformer( Layer.VIEW ).getScaleY() * 1.1;

		// Dimension d = view.getSize();
		// d.setSize( d.getWidth() / vscalex, d.getHeight() / vscaley );
		double scalex = 1 / vscaley;
		double scaley = 1 / vscalex;

		mlt.getTransformer( Layer.LAYOUT ).setScale( scalex, scaley, view.getCenter() );
//
//		
//		
//		// two steps here: figure out the center of our layout, and translate
//		// that center so it's in the center of our visualization
//		// we'll take the average X and Y, so big clusters get closer to the center
//		double totalX = 0;
//		double totalY = 0;
//
//		Graph<SEMOSSVertex, SEMOSSEdge> graph = layout.getGraph();
//		Collection<SEMOSSVertex> verts = graph.getVertices();
//		for ( SEMOSSVertex v : verts ) {
//			double x = layout.getX( v );
//			double y = layout.getY( v );
//
//			totalX += x;
//			totalY += y;
//		}
//
//		Point2D viewCenter = view.getCenter();
//		Point2D layoutCenter
//				= new Point2D.Double( totalX / verts.size(), totalY / verts.size() );
//		log.debug( "layout center is: " + layoutCenter );
//		log.debug( "view center is: " + view.getCenter() );
//
//		view.getRenderContext().getMultiLayerTransformer().getTransformer( Layer.LAYOUT ).
//				translate( viewCenter.getX() - layoutCenter.getX(),
//						viewCenter.getY() - layoutCenter.getY() );
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

	public void fireGraphUpdated() {
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
	 * {@link #create(org.openrdf.model.Model, com.ostrichemulators.semtool.rdf.engine.api.IEngine) }.
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
		labelcache.setEngine( engine );
		List<URI> nodes = new ArrayList<>();
		Model model = new LinkedHashModel();
		for ( Value[] row : data ) {
			URI s = URI.class.cast( row[0] );
			if ( 1 == row.length ) {
				nodes.add( s );
			}
			else if ( 3 == row.length ) {
				URI p = URI.class.cast( row[1] );
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

	public void add( Model m, List<URI> nodes, IEngine engine ) {
		labelcache.setEngine( engine );
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
			Set<SEMOSSVertex> removedVs = new HashSet<>();
			Set<SEMOSSEdge> removedEs = new HashSet<>();
			gdm.removeElementsSinceLevel( overlayLevel, removedVs, removedEs );
			for ( SEMOSSVertex v : removedVs ) {
				v.removePropertyChangeListener( this );
			}
			for ( SEMOSSEdge v : removedEs ) {
				v.removePropertyChangeListener( this );
			}
		}

		overlayLevel++;
		view.setOverlayLevel( overlayLevel );

		Set<SEMOSSVertex> oldverts = new HashSet<>( gdm.getGraph().getVertices() );
		Set<SEMOSSEdge> oldedges = new HashSet<>( gdm.getGraph().getEdges() );
		if ( !m.isEmpty() ) {
			gdm.addGraphLevel( m, engine, overlayLevel );
		}
		if ( !nodes.isEmpty() ) {
			gdm.addGraphLevel( nodes, engine, overlayLevel );
		}

		Set<SEMOSSVertex> newverts = new HashSet<>( gdm.getGraph().getVertices() );
		Set<SEMOSSEdge> newedges = new HashSet<>( gdm.getGraph().getEdges() );
		newverts.removeAll( oldverts );
		newedges.removeAll( oldedges );

		for ( SEMOSSVertex v : newverts ) {
			v.addPropertyChangeListener( this );
		}
		for ( SEMOSSEdge v : newedges ) {
			v.addPropertyChangeListener( this );
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

	public boolean isTraversable() {
		return traversable;
	}

	public boolean areNodesHidable() {
		return nodesHidable;
	}

	public ControlPanel getSearchPanel() {
		// return controlPanel;
		return null;
	}

	/**
	 * Allow subclasses to substitute a different vertex for the given one
	 *
	 * @param v
	 * @return
	 */
	public SEMOSSVertex getRealVertex( SEMOSSVertex v ) {
		return v;
	}

	public SEMOSSEdge getRealEdge( SEMOSSEdge v ) {
		return v;
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
}
