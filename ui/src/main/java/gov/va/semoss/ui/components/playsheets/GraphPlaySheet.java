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
package gov.va.semoss.ui.components.playsheets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;

import javax.swing.JSplitPane;

import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import gov.va.semoss.om.GraphDataModel;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.ui.components.ControlPanel;
import gov.va.semoss.ui.components.GraphToTreeConverter;
import gov.va.semoss.ui.components.LegendPanel2;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.VertexColorShapeData;
import gov.va.semoss.ui.components.api.GraphListener;
import gov.va.semoss.ui.main.listener.impl.GraphNodeListener;
import gov.va.semoss.ui.main.listener.impl.GraphPlaySheetListener;
import gov.va.semoss.ui.main.listener.impl.PickedStateListener;
import gov.va.semoss.ui.main.listener.impl.PlaySheetColorShapeListener;
import gov.va.semoss.ui.main.listener.impl.PlaySheetControlListener;
import gov.va.semoss.ui.transformer.ArrowPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import gov.va.semoss.ui.transformer.SelectingTransformer;
import gov.va.semoss.ui.transformer.TooltipTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.ui.transformer.VertexStrokeTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.MultiMap;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 */
public class GraphPlaySheet extends ImageExportingPlaySheet implements PropertyChangeListener {

	private static final long serialVersionUID = 4699492732234656487L;
	private static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view;
	private JSplitPane graphSplitPane;
	protected ControlPanel controlPanel;

	private VertexColorShapeData colorShapeData = new VertexColorShapeData();

	protected GraphDataModel gdm;
	protected String layoutName = Constants.FR;
	protected ControlData controlData = new ControlData();

	protected LabelFontTransformer<SEMOSSVertex> vft = new LabelFontTransformer<>();
	protected LabelTransformer<SEMOSSVertex> vlt = new LabelTransformer<>( controlData );
	protected TooltipTransformer<SEMOSSVertex> vtt = new TooltipTransformer<>( controlData );
	protected PaintTransformer<SEMOSSVertex> vpt = new PaintTransformer<>();
	protected VertexShapeTransformer vht = new VertexShapeTransformer();
	protected VertexStrokeTransformer vst = new VertexStrokeTransformer();

	protected LabelFontTransformer<SEMOSSEdge> eft = new LabelFontTransformer<>();
	protected LabelTransformer<SEMOSSEdge> elt = new LabelTransformer<>( controlData );
	protected TooltipTransformer<SEMOSSEdge> ett = new TooltipTransformer<>( controlData );
	protected PaintTransformer<SEMOSSEdge> ept = new PaintTransformer<SEMOSSEdge>() {
		@Override
		protected Paint transformNotSelected( SEMOSSEdge t, boolean skel ) {
			// always show the edge
			return super.transformNotSelected( t, false );
		}
	};
	protected EdgeStrokeTransformer est = new EdgeStrokeTransformer();
	protected ArrowPaintTransformer adpt = new ArrowPaintTransformer();
	protected ArrowPaintTransformer aft = new ArrowPaintTransformer();

	protected boolean traversable = true;
	protected boolean nodesHidable = true;

	protected int overlayLevel = 0;
	protected int maxOverlayLevel = 0;
	private final HidingPredicate<SEMOSSVertex> predicate = new HidingPredicate<>();

	private final List<GraphListener> listenees = new ArrayList<>();
	private boolean inGraphOp = false;
	private ItemListener pickStateListener = null;

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public GraphPlaySheet() {
		this( new GraphDataModel() );
	}

	public GraphPlaySheet( GraphDataModel model ) {
		log.debug( "new Graph PlaySheet" );
		gdm = model;

		controlPanel = new ControlPanel();

		graphSplitPane = new JSplitPane();
		graphSplitPane.setEnabled( false );
		graphSplitPane.setOneTouchExpandable( true );
		graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );

		setLayout( new BorderLayout() );
		add( graphSplitPane, BorderLayout.CENTER );
		LegendPanel2 legendPanel = new LegendPanel2();
		add( legendPanel, BorderLayout.SOUTH );

		Layout<SEMOSSVertex, SEMOSSEdge> layout = new FRLayout<>( gdm.getGraph() );
		view = new VisualizationViewer<>( layout );
		initVisualizer( view );

		controlData.setViewer( view );

		controlPanel.setPlaySheet( this );
		controlPanel.layoutChanged( gdm.getGraph(), null, layout, this );

		graphSplitPane.setTopComponent( controlPanel );
		graphSplitPane.setBottomComponent( new GraphZoomScrollPane( view ) );

		addGraphListener( legendPanel );
		addGraphListener( controlData );
		addGraphListener( colorShapeData );
		addGraphListener( controlPanel );
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
		VertexPredicateFilter<SEMOSSVertex, SEMOSSEdge> filter;
		filter = new VertexPredicateFilter<>( predicate );
		return (DirectedGraph<SEMOSSVertex, SEMOSSEdge>) filter.transform( gdm.getGraph() );
	}

	@Override
	protected BufferedImage getExportImage() {

		Dimension d = view.getGraphLayout().getSize();
		d.setSize( d.getWidth() * 1.2, d.getHeight() * 1.2 );

		VisualizationImageServer<SEMOSSVertex, SEMOSSEdge> vis
				= new VisualizationImageServer<>( view.getGraphLayout(), d );

		vis.setBackground( view.getBackground() );
		vis.setRenderContext( view.getRenderContext() );

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
		VertexPredicateFilter<SEMOSSVertex, SEMOSSEdge> filter
				= new VertexPredicateFilter<>( predicate );
		Layout<SEMOSSVertex, SEMOSSEdge> layout = view.getGraphLayout();
		layout.setGraph( filter.transform( gdm.getGraph() ) );
		view.setGraphLayout( layout );
	}

	@Override
	public void setFrame( PlaySheetFrame frame ) {
		super.setFrame( frame );

		frame.addInternalFrameListener( new GraphPlaySheetListener( frame ) );
		frame.addInternalFrameListener( new PlaySheetControlListener( frame ) );
		frame.addInternalFrameListener( new PlaySheetColorShapeListener( frame ) );
	}

	/**
	 * Regenerates all the data needed to display the graph
	 */
	public void updateGraph() {
		setLayoutName( layoutName );
		fireGraphUpdated();
		setUndoRedoBtn();
	}

	@Override
	public void refineView() {
		updateGraph();
	}

	/**
	 * Method initVisualizer.
	 */
	private void initVisualizer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer ) {
		viewer.setRenderer( new SemossBasicRenderer() );

		GraphNodeListener gl = new GraphNodeListener( this );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		viewer.setGraphMouse( gl );
		viewer.setBackground( Color.WHITE );

		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = viewer.getRenderContext();
		rc.setVertexLabelTransformer( vlt );
		viewer.setVertexToolTipTransformer( vtt );
		rc.setVertexStrokeTransformer( vst );
		rc.setVertexShapeTransformer( vht );
		rc.setVertexFillPaintTransformer( vpt );
		rc.setVertexFontTransformer( vft );

		rc.setEdgeLabelTransformer( elt );
		viewer.setEdgeToolTipTransformer( ett );
		rc.setEdgeDrawPaintTransformer( ept );
		rc.setEdgeStrokeTransformer( est );
		rc.setEdgeArrowStrokeTransformer( est );
		rc.setEdgeFontTransformer( eft );
		rc.setArrowDrawPaintTransformer( adpt );
		rc.setArrowFillPaintTransformer( aft );
		viewer.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.S );
		rc.setLabelOffset( 0 );

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
		controlPanel.setUndoButtonEnabled( overlayLevel > 1 );
		controlPanel.setRedoButtonEnabled( maxOverlayLevel > overlayLevel );
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
	 * Method getColorShapeData.
	 *
	 * @return VertexColorShapeData
	 */
	public VertexColorShapeData getColorShapeData() {
		return colorShapeData;
	}

	/**
	 * Method getControlData.
	 *
	 * @return ControlData
	 */
	public ControlData getControlData() {
		return controlData;
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
		for ( GraphListener gl : listenees ) {
			try {
				gl.graphUpdated( gdm.getGraph(), this );
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

	public VisualizationViewer<SEMOSSVertex, SEMOSSEdge> getView() {
		return view;
	}

	protected void setView( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> v ) {
		view = v;
	}

	/**
	 * Method getEdgeLabelFontTransformer.
	 *
	 * @return EdgeLabelFontTransformer
	 */
	public LabelFontTransformer<SEMOSSEdge> getEdgeLabelFontTransformer() {
		return eft;
	}

	/**
	 * Method getVertexLabelFontTransformer.
	 *
	 * @return VertexLabelFontTransformer
	 */
	public LabelFontTransformer<SEMOSSVertex> getVertexLabelFontTransformer() {
		return vft;
	}

	@Override
	public void create( Model m, IEngine engine ) {
		add( m, null, engine );
	}

	/**
	 * Creates graph nodes from the given data. If the {@code Value[]}s have
	 * length 1, the values are expected to be nodes ({@link Resource}s. If they
	 * have length 3, then they are repackaged as Statements, and forwarded on to
	 * {@link #create(org.openrdf.model.Model, gov.va.semoss.rdf.engine.api.IEngine) }.
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
	 * gov.va.semoss.rdf.engine.api.IEngine) }
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
	public void run() {
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );

		//if no vertices or edges are selected, perform action on all vertices and edges
		if ( view.getPickedVertexState().getPicked().isEmpty()
				&& view.getPickedEdgeState().getPicked().isEmpty() ) {
			vft.changeSize( (int) incr );
			eft.changeSize( (int) incr );
		}
		else {
			//otherwise, only perform action on the selected vertices and edges
			vft.changeSize( (int) incr, view.getPickedVertexState().getPicked() );
			eft.changeSize( (int) incr, view.getPickedEdgeState().getPicked() );
		}

		view.repaint();
	}

	public boolean isTraversable() {
		return traversable;
	}

	public boolean areNodesHidable() {
		return nodesHidable;
	}

	public ControlPanel getSearchPanel() {
		return controlPanel;
	}

	/**
	 * Clears the highlighting, turns off skeleton mode if it's enabled, and
	 * resizes all nodes with a custom size
	 */
	public void clearHighlighting() {
		for ( SelectingTransformer<?, ?> s : new SelectingTransformer[]{ vft, vpt, vst,
			vht, est, ept, eft, elt, adpt, aft } ) {
			s.setSkeletonMode( false );
			s.clearSelected();
		}

		eft.clearSizeData();
		vft.clearSizeData();
		vht.clearSizeData();
		view.repaint();
	}

	/**
	 * Adds the given vertices and edges to the highlighted parts of the graph
	 *
	 * @param verts
	 * @param edges
	 * @param asSkeleton should the skeleton mode be activated as well?
	 */
	protected void highlight( Collection<SEMOSSVertex> verts, Collection<SEMOSSEdge> edges,
			boolean asSkeleton ) {

		for ( SelectingTransformer<?, ?> s : new SelectingTransformer[]{ vft, vpt, vst,
			vht, est, ept, eft, elt, adpt, aft } ) {
			s.setSkeletonMode( asSkeleton );
		}

		vft.select( verts );
		vpt.select( verts );

		est.select( edges );
		ept.select( edges );
		eft.select( edges );
		elt.select( edges );
		adpt.select( edges );
		aft.select( edges );

		view.repaint();
	}

	public void highlight( Collection<SEMOSSVertex> verts, Collection<SEMOSSEdge> edges ) {
		highlight( verts, edges, false );
	}

	public void skeleton( Collection<SEMOSSVertex> verts, Collection<SEMOSSEdge> edges ) {
		highlight( verts, edges, true );
	}

	public Collection<SEMOSSVertex> getHighlightedVertices() {
		return new HashSet<>( vft.getSelected() );
	}

	public Collection<SEMOSSEdge> getHighlightedEdges() {
		return new HashSet<>( est.getSelected() );
	}
	
	/**
	 * Allow subclasses to substitute a different vertex for the given one
	 * @param v
	 * @return 
	 */
	public SEMOSSVertex getRealVertex( SEMOSSVertex v ){
		return v;
	}

	protected boolean isloading(){
		return inGraphOp;
	}
	
	@Override
	public void propertyChange( PropertyChangeEvent evt ) {
		if ( !isloading() ) {
			view.repaint();
			fireGraphUpdated();
		}
	}

	protected class SemossBasicRenderer<V extends SEMOSSVertex, E extends SEMOSSEdge> extends BasicRenderer<V, E> {

		Predicate<E> edgehider = new Predicate<E>() {

			@Override
			public boolean evaluate( E v ) {
				return ( v.isVisible() && v.getVerticesVisible()
						&& getGraphData().presentAtLevel( v, overlayLevel ) );
			}
		};

		@Override
		public void render( RenderContext<V, E> renderContext,
				Layout<V, E> layout ) {
			setEdgeVisibilities( layout.getGraph() );
			try {
				for ( E e : layout.getGraph().getEdges() ) {
					if ( edgehider.evaluate( e ) ) {
						renderEdge( renderContext, layout, e );
						renderEdgeLabel( renderContext, layout, e );
					}
				}
			}
			catch ( ConcurrentModificationException cme ) {
				renderContext.getScreenDevice().repaint();
			}

			// paint all the vertices
			try {
				for ( V v : layout.getGraph().getVertices() ) {
					if ( predicate.evaluate( v ) ) {
						renderVertex( renderContext, layout, v );
						renderVertexLabel( renderContext, layout, v );
					}
				}
			}
			catch ( ConcurrentModificationException cme ) {
				renderContext.getScreenDevice().repaint();
			}

			if ( log.isTraceEnabled() ) {
				renderContext.getGraphicsContext().setPaint( Color.RED );
				log.debug( "size: (" + view.getX() + "," + view.getY() + ","
						+ view.getWidth() + "," + view.getHeight() + ")" );
				renderContext.getGraphicsContext().drawRect( view.getX() + 1, view.getY() + 1,
						view.getWidth() - 2, view.getHeight() - 2 );
				renderContext.getGraphicsContext().drawOval( (int) view.getCenter().getX(),
						(int) view.getCenter().getY(), 20, 20 );
			}
		}

		/**
		 * Convenience method which sets t the visibility of edges in the current
		 * graph based on the visibility flag within the edge, but ALSO on whether
		 * the vertices which the edge connects are visible
		 */
		private void setEdgeVisibilities( Graph<V, E> graph ) {
			Collection<E> allEdges = graph.getEdges();
			for ( E edge : allEdges ) {
				V destination = graph.getDest( edge );
				V source = graph.getSource( edge );
				boolean destVisible = true;
				boolean sourceVisible = true;
				// If the destination vertex/node is not visible, neither should the edge 
				// be visible
				// The edge may not have a destination vertex/node, so prepare for that
				if ( destination != null ) {
					if ( !destination.isVisible() ) {
						destVisible = false;
					}
				}
				else {
					destVisible = false;
				}
				// If the destination vertex/node is not visible, neither should the edge 
				// be visible
				// The edge may not have a source vertex/node, so prepare for that
				if ( source != null ) {
					if ( !source.isVisible() ) {
						sourceVisible = false;
					}
				}
				else {
					sourceVisible = false;
				}
				// If both of the edges are visible, set the edgesVisible flag in the Edge object
				if ( destVisible && sourceVisible ) {
					edge.setVerticesVisible( true );
				}
				else {
					edge.setVerticesVisible( false );
				}
			}
		}
	}

	protected class HidingPredicate<V extends SEMOSSVertex> implements Predicate<V> {

		@Override
		public boolean evaluate( V v ) {
			return ( v.isVisible() && getGraphData().presentAtLevel( v, overlayLevel ) );
		}
	}
}
