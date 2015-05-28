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

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
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
import gov.va.semoss.ui.components.LegendPanel2;
import gov.va.semoss.ui.components.NewHoriScrollBarUI;
import gov.va.semoss.ui.components.NewScrollBarUI;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.PropertySpecData;
import gov.va.semoss.ui.components.VertexColorShapeData;
import gov.va.semoss.ui.components.VertexFilterData;
import gov.va.semoss.ui.main.listener.impl.GraphNodeListener;
import gov.va.semoss.ui.main.listener.impl.GraphPlaySheetListener;
import gov.va.semoss.ui.main.listener.impl.PickedStateListener;
import gov.va.semoss.ui.main.listener.impl.PlaySheetColorShapeListener;
import gov.va.semoss.ui.main.listener.impl.PlaySheetControlListener;
import gov.va.semoss.ui.main.listener.impl.PlaySheetOWLListener;
import gov.va.semoss.ui.transformer.ArrowPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import gov.va.semoss.ui.transformer.SelectingTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.ui.transformer.VertexStrokeTransformer;
import gov.va.semoss.ui.transformer.TooltipTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import org.apache.commons.collections15.Predicate;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;

/**
 */
public class GraphPlaySheet extends PlaySheetCentralComponent {

	private static final long serialVersionUID = 4699492732234656487L;
	protected static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private final VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view;
	private JSplitPane graphSplitPane;
	private ControlPanel controlPanel;
	private LegendPanel2 legendPanel;
	private VertexColorShapeData colorShapeData = new VertexColorShapeData();

	protected GraphDataModel gdm;
	protected String layoutName = Constants.FR;
	protected ControlData controlData = new ControlData();
	protected PropertySpecData predData = new PropertySpecData();
	protected VertexFilterData filterData = new VertexFilterData();

	protected LabelFontTransformer<SEMOSSVertex> vft = new LabelFontTransformer<>();
	protected VertexShapeTransformer vht = new VertexShapeTransformer();
	protected LabelTransformer<SEMOSSVertex> vlt = new LabelTransformer<>( controlData );
	protected TooltipTransformer<SEMOSSVertex> vtt = new TooltipTransformer<>( controlData );
	protected PaintTransformer<SEMOSSVertex> vpt = new PaintTransformer<>();
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
	private final HidingPredicate predicate = new HidingPredicate();

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public GraphPlaySheet() {
		this( new GraphDataModel() );
	}

	public GraphPlaySheet( GraphDataModel model ) {
		log.debug( "new Graph PlaySheet" );
		gdm = model;

		controlPanel = new ControlPanel( gdm.enableSearchBar() );

		legendPanel = new LegendPanel2();

		graphSplitPane = new JSplitPane();
		graphSplitPane.setEnabled( false );
		graphSplitPane.setOneTouchExpandable( true );
		graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );

		setLayout( new BorderLayout() );
		add( graphSplitPane, BorderLayout.CENTER );
		add( legendPanel, BorderLayout.SOUTH );

		Layout<SEMOSSVertex, SEMOSSEdge> layout = new FRLayout( gdm.getGraph() );
		view = new VisualizationViewer<>( layout );
		initVisualizer( view );

		controlData.setViewer( view );
		controlPanel.setPlaySheet( this );
		controlPanel.setGraphLayout( layout, gdm.getGraph() );
		controlPanel.setViewer( view );

		GraphZoomScrollPane gzPane = new GraphZoomScrollPane( view );
		gzPane.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		gzPane.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );

		graphSplitPane.setTopComponent( controlPanel );
		graphSplitPane.setBottomComponent( gzPane );

		legendPanel.setFilterData( filterData );
	}

	public DelegateForest<SEMOSSVertex, SEMOSSEdge> getForest() {
		return new DelegateForest<>( gdm.getGraph() );
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

		for ( SEMOSSEdge v : visible.getEdges() ) {
			graph.addEdge( visible.getSource( v ), visible.getDest( v ) );
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
		VertexPredicateFilter filter = new VertexPredicateFilter<>( predicate );
		return (DirectedGraph<SEMOSSVertex, SEMOSSEdge>) filter.transform( gdm.getGraph() );
	}

	public boolean getSudowl() {
		return gdm.showSudowl();
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
			updateLayout();
			setUndoRedoBtn();
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
		}
	}

	public void updateLayout() {
		VertexPredicateFilter filter = new VertexPredicateFilter<>( predicate );
		Layout<SEMOSSVertex, SEMOSSEdge> layout = view.getGraphLayout();
		layout.setGraph( filter.transform( gdm.getGraph() ) );
		view.setGraphLayout( layout );
	}

	@Override
	public void setFrame( PlaySheetFrame frame ) {
		super.setFrame( frame );

		frame.addInternalFrameListener( new GraphPlaySheetListener( this ) );
		frame.addInternalFrameListener( new PlaySheetControlListener( this ) );
		frame.addInternalFrameListener( new PlaySheetOWLListener( this ) );
		frame.addInternalFrameListener( new PlaySheetColorShapeListener( this ) );
	}

	/**
	 * Regenerates all the data needed to display the graph
	 */
	public void updateGraph() {
		try {
			Graph<SEMOSSVertex, SEMOSSEdge> g = gdm.getGraph();
			setLayoutName( layoutName );

			if ( gdm.enableSearchBar() ) {
				controlPanel.getSearchController().indexGraph( g, getEngine() );
			}

			processControlData( g );
			genAllData();
			legendPanel.drawLegend();

			setUndoRedoBtn();
		}
		catch ( Exception ex ) {
			log.error( "problem adding panel to play sheet", ex );
		}
	}

	/**
	 * Method initVisualizer.
	 */
	protected void initVisualizer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer ) {
		viewer.setRenderer( new SemossBasicRenderer() );

		GraphNodeListener gl = new GraphNodeListener( this );
		viewer.setGraphMouse( new GraphNodeListener( this ) );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		viewer.setGraphMouse( gl );
		viewer.setBackground( Color.WHITE );

		RenderContext rc = viewer.getRenderContext();
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

		PickedStateListener psl = new PickedStateListener( viewer, this );
		viewer.getPickedVertexState().addItemListener( psl );
	}

	public String getLayoutName() {
		return layoutName;
	}

	/**
	 * Method exportDB not implemented.
	 */
	public void exportDB() {
		log.error( "exportDB not implemented (being refactored)" );
	}

	/**
	 * Method setUndoRedoBtn.
	 */
	private void setUndoRedoBtn() {
		controlPanel.setUndoButtonEnabled( overlayLevel > 1 );
		controlPanel.setRedoButtonEnabled( maxOverlayLevel > overlayLevel );
	}

	/**
	 * Method getFilterData.
	 *
	 * @return VertexFilterData
	 */
	public VertexFilterData getFilterData() {
		return filterData;
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
	 * Method setGraph.
	 *
	 * @param forest DelegateForest
	 */
	public void setForest( DelegateForest<SEMOSSVertex, SEMOSSEdge> forest ) {
		gdm.setGraph( forest );
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
		this.layoutName = newName;

		Class<?> layoutClass = (Class<?>) DIHelper.getInstance().getLocalProp( layoutName );
		log.debug( "Create layout from layoutName " + layoutName
				+ ", and layoutClass " + layoutClass );

		boolean ok = false;
		Layout<SEMOSSVertex, SEMOSSEdge> layout;

		VertexPredicateFilter<SEMOSSVertex, SEMOSSEdge> filter
				= new VertexPredicateFilter<>( predicate );
		Graph<SEMOSSVertex, SEMOSSEdge> graph = filter.transform( gdm.getGraph() );
		try {
			Constructor<?> constructor = layoutClass.getConstructor( Graph.class );
			layout = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( graph );
			ok = true;
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			log.error( "could not create layout", e );
			layout = new FRLayout( gdm.getGraph() );
		}

		controlPanel.setGraphLayout( layout,
				(DirectedGraph<SEMOSSVertex, SEMOSSEdge>) graph );
		view.setGraphLayout( layout );

		return ok;
	}

	public VisualizationViewer<SEMOSSVertex, SEMOSSEdge> getView() {
		return view;
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

	public void removeExistingConcepts( List<String> subVector ) {
		throw new UnsupportedOperationException( "this function is not operational until refactored" );

//		for ( String remQuery : subVector ) {
//			try {
//				log.debug( "Removing query " + remQuery );
//				Update update = gdm.getRC().prepareUpdate( QueryLanguage.SPARQL, remQuery );
//				update.execute();
//				log.error( "removing concepts not implemented (being refactored)" );
//				//this.gdm.baseRelEngine.execInsertQuery(remQuery);
//
//			}
//			catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
//				log.error( e, e );
//			}
//		}
	}

	/**
	 * Method addNewConcepts.
	 *
	 * @param subjects String
	 * @param baseObject String
	 * @param predicate String
	 * @return String
	 */
	public String addNewConcepts( String subjects, String baseObject, String predicate ) {
		throw new UnsupportedOperationException( "this function is not operational until refactored" );

//		String listOfChilds = null;
//		for ( String adder : subjects.split( ";" ) ) {
//			String parent = adder.substring( 0, adder.indexOf( "@@" ) );
//			String child = adder.substring( adder.indexOf( "@@" ) + 2 );
//
//			if ( listOfChilds == null ) {
//				listOfChilds = child;
//			}
//			else {
//				listOfChilds = listOfChilds + ";" + child;
//			}
//
//			SesameJenaConstructStatement st = new SesameJenaConstructStatement();
//			st.setSubject( child );
//			st.setPredicate( predicate );
//			st.setObject( baseObject );
//
//			gdm.addToSesame( st );
//			log.debug( " Query....  " + parent + "<>" + child );
//		}
//
//		return listOfChilds;
	}

	private void genAllData() {
		filterData.fillRows();
		filterData.fillEdgeRows();
		controlData.generateAllRows();

		if ( gdm.showSudowl() ) {
			predData.genPredList();
		}

		colorShapeData.fillRows( filterData.getTypeHash() );
	}

	private void processControlData( Graph<SEMOSSVertex, SEMOSSEdge> graph ) {
		for ( SEMOSSVertex vertex : graph.getVertices() ) {
			for ( URI property : vertex.getProperties().keySet() ) {
				controlData.addVertexProperty( vertex.getType(), property );
			}

			filterData.addVertex( vertex );
		}

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			for ( URI property : edge.getProperties().keySet() ) {
				controlData.addEdgeProperty( edge.getEdgeType(), property );
			}

			//add to filter data
			filterData.addEdge( edge );

			//add to pred data
			predData.addPredicateAvailable( edge.getURI().stringValue() );
			predData.addConceptAvailable( edge.getInVertex().getURI().stringValue() );
			predData.addConceptAvailable( edge.getOutVertex().getURI().stringValue() );
		}
	}

	/**
	 * Method getPredicateData.
	 *
	 * @return PropertySpecData
	 */
	public PropertySpecData getPredicateData() {
		return predData;
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
		List<Resource> nodes = new ArrayList<>();
		Model model = new LinkedHashModel();
		for ( Value[] row : data ) {
			Resource s = Resource.class.cast( row[0] );
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

	public void add( Model m, List<Resource> nodes, IEngine engine ) {
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

		if ( overlayLevel < maxOverlayLevel ) {
			gdm.removeElementsSinceLevel( overlayLevel );
		}

		overlayLevel++;

		if ( !m.isEmpty() ) {
			gdm.addGraphLevel( m, engine, overlayLevel );
		}
		if ( !nodes.isEmpty() ) {
			gdm.addGraphLevel( nodes, engine, overlayLevel );
		}

		if ( overlayLevel > maxOverlayLevel ) {
			maxOverlayLevel = overlayLevel;
		}

		updateGraph();
	}

	@Override
	public void run() {
	}

	public LegendPanel2 getLegendPanel() {
		return legendPanel;
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );

		//if no vertices or edges are selected, perform action on all vertices and edges
		if ( view.getPickedVertexState().getPicked().isEmpty()
				&& view.getPickedEdgeState().getPicked().isEmpty() ) {
			vft.changeFontSize( (int) incr );
			eft.changeFontSize( (int) incr );
		}
		else {
			//otherwise, only perform action on the selected vertices and edges
			vft.changeFontSize( (int) incr, view.getPickedVertexState().getPicked() );
			eft.changeFontSize( (int) incr, view.getPickedEdgeState().getPicked() );
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
	 * Clears the highlighting and turns off skeleton mode if it's enabled
	 */
	public void clearHighlighting() {
		for ( SelectingTransformer s : new SelectingTransformer[]{ vft, vpt, est,
			ept, eft, elt, adpt, aft } ) {
			s.setSkeletonMode( false );
			s.clearSelected();
		}
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

		for ( SelectingTransformer s : new SelectingTransformer[]{ vft, vpt, est,
			ept, eft, elt, adpt, aft } ) {
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

	private class SemossBasicRenderer extends BasicRenderer<SEMOSSVertex, SEMOSSEdge> {

		Predicate<SEMOSSEdge> edgehider = new Predicate<SEMOSSEdge>() {

			@Override
			public boolean evaluate( SEMOSSEdge v ) {
				return ( v.isVisible() && v.getLevel() <= overlayLevel
						&& v.getInVertex().isVisible() && v.getOutVertex().isVisible() );
			}
		};

		@Override
		public void render( RenderContext<SEMOSSVertex, SEMOSSEdge> renderContext,
				Layout<SEMOSSVertex, SEMOSSEdge> layout ) {

			try {

				for ( SEMOSSEdge e : layout.getGraph().getEdges() ) {
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
				for ( SEMOSSVertex v : layout.getGraph().getVertices() ) {
					if ( predicate.evaluate( v ) ) {
						renderVertex( renderContext, layout, v );
						renderVertexLabel( renderContext, layout, v );
					}
				}
			}
			catch ( ConcurrentModificationException cme ) {
				renderContext.getScreenDevice().repaint();
			}
		}
	}

	private class HidingPredicate implements Predicate<SEMOSSVertex> {

		@Override
		public boolean evaluate( SEMOSSVertex v ) {
			return ( v.isVisible() && v.getLevel() <= overlayLevel );
		}
	}
}
