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

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JDesktopPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import gov.va.semoss.om.GraphDataModel;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.ui.components.ControlPanel;
import gov.va.semoss.ui.components.GraphPlaySheetFrame;
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
import gov.va.semoss.ui.transformer.ArrowDrawPaintTransformer;
import gov.va.semoss.ui.transformer.ArrowFillPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeArrowStrokeTransformer;
import gov.va.semoss.ui.transformer.EdgeLabelFontTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.ui.transformer.VertexLabelFontTransformer;
import gov.va.semoss.ui.transformer.VertexPaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.ui.transformer.VertexStrokeTransformer;
import gov.va.semoss.ui.transformer.TooltipTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.util.Arrays;
import java.util.HashSet;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 */
public class GraphPlaySheet extends PlaySheetCentralComponent {

	private static final long serialVersionUID = 4699492732234656487L;
	protected static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view;
	private JSplitPane graphSplitPane;
	private ControlPanel controlPanel;
	private LegendPanel2 legendPanel;
	private VertexColorShapeData colorShapeData = new VertexColorShapeData();

	protected GraphDataModel gdm;
	protected String layoutName = Constants.FR;
	protected ControlData controlData = new ControlData();
	protected PropertySpecData predData = new PropertySpecData();
	protected VertexFilterData filterData = new VertexFilterData();
	protected VertexLabelFontTransformer vlft;
	protected EdgeLabelFontTransformer elft;
	protected VertexShapeTransformer vsht;
	protected boolean traversable = true, nodesHidable = true;

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public GraphPlaySheet() {
		this( new GraphDataModel() );
	}

	public GraphPlaySheet( GraphDataModel model ) {
		log.debug( "new Graph PlaySheet" );
		gdm = model;

		try {
			controlPanel = new ControlPanel( gdm.enableSearchBar() );

			legendPanel = new LegendPanel2();

			graphSplitPane = new JSplitPane();
			graphSplitPane.setEnabled( false );
			graphSplitPane.setOneTouchExpandable( true );
			graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
			controlPanel.setPlaySheet( this );

			setLayout( new BorderLayout() );
			add( graphSplitPane, BorderLayout.CENTER );
			add( legendPanel, BorderLayout.SOUTH );

			Layout<SEMOSSVertex, SEMOSSEdge> layout2Use = new FRLayout( gdm.getGraph() );
			controlPanel.setGraphLayout( layout2Use, gdm.getGraph() );
			view = createVisualizer( layout2Use );

			controlData.setViewer( view );
			controlPanel.setViewer( view );

			GraphZoomScrollPane gzPane = new GraphZoomScrollPane( view );
			gzPane.getVerticalScrollBar().setUI( new NewScrollBarUI() );
			gzPane.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );

			graphSplitPane.setTopComponent( controlPanel );
			graphSplitPane.setBottomComponent( gzPane );

			legendPanel.setFilterData( filterData );

		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	public DelegateForest<SEMOSSVertex, SEMOSSEdge> getForest() {
		return new DelegateForest<>( gdm.getGraph() );
	}

	public SimpleGraph<SEMOSSVertex, SEMOSSEdge> getSimpleGraph() {
		SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph
				= new SimpleGraph<>( SEMOSSEdge.class );
		for ( SEMOSSVertex v : gdm.getGraph().getVertices() ) {
			graph.addVertex( v );
		}
		for ( SEMOSSEdge v : gdm.getGraph().getEdges() ) {
			graph.addEdge( v.getInVertex(), v.getOutVertex(), v );
		}

		return graph;
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
		if ( gdm.hasUndoData() ) {
			gdm.undoData();
			filterData = new VertexFilterData();
			controlData = new ControlData();
			predData = new PropertySpecData();
			refineView();
		}

		genAllData();
	}

	/**
	 * Method redoView.
	 */
	public void redoView() {
		if ( gdm.hasRedoData() ) {
			gdm.redoData();
			refineView();
		}
	}

	public GraphPlaySheetFrame getGraphPlaySheet() {
		return GraphPlaySheetFrame.class.cast( getPlaySheetFrame() );
	}

	public JDesktopPane getDesktopPane() {
		return getPlaySheetFrame().getDesktopPane();
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
	 * Method createVisualizer.
	 */
	protected VisualizationViewer<SEMOSSVertex, SEMOSSEdge>
			createVisualizer( Layout<SEMOSSVertex, SEMOSSEdge> layout ) {

		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer
				= new VisualizationViewer<>( layout );
		viewer.setPreferredSize( layout.getSize() );
		viewer.setBounds( 10000000, 10000000, 10000000, 100000000 );
		viewer.setRenderer( new BasicRenderer<>() );

		GraphNodeListener gl = new GraphNodeListener( this );
		viewer.setGraphMouse( new GraphNodeListener( this ) );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		viewer.setGraphMouse( gl );

		LabelTransformer<SEMOSSVertex> vlt = new LabelTransformer<>( controlData );
		TooltipTransformer<SEMOSSVertex> vtt = new TooltipTransformer<>( controlData );

		LabelTransformer<SEMOSSEdge> elt = new LabelTransformer<>( controlData );
		TooltipTransformer<SEMOSSEdge> ett = new TooltipTransformer<>( controlData );

		VertexPaintTransformer vpt = new VertexPaintTransformer();
		EdgeStrokeTransformer est = new EdgeStrokeTransformer();

		VertexStrokeTransformer vst = new VertexStrokeTransformer();
		ArrowDrawPaintTransformer adpt = new ArrowDrawPaintTransformer();
		EdgeArrowStrokeTransformer east = new EdgeArrowStrokeTransformer();
		ArrowFillPaintTransformer aft = new ArrowFillPaintTransformer();

		//keep the stored one if possible
		if ( vlft == null ) {
			vlft = new VertexLabelFontTransformer();
		}
		if ( elft == null ) {
			elft = new EdgeLabelFontTransformer();
		}
		if ( vsht == null ) {
			vsht = new VertexShapeTransformer();
		}
		else {
			vsht.emptySelected();
		}

		viewer.setBackground( Color.WHITE );
		viewer.getRenderContext().setVertexLabelTransformer( vlt );
		viewer.getRenderContext().setEdgeLabelTransformer( elt );
		viewer.getRenderContext().setVertexStrokeTransformer( vst );
		viewer.getRenderContext().setVertexShapeTransformer( vsht );
		viewer.getRenderContext().setVertexFillPaintTransformer( vpt );
		viewer.getRenderContext().setEdgeStrokeTransformer( est );
		viewer.getRenderContext().setArrowDrawPaintTransformer( adpt );
		viewer.getRenderContext().setEdgeArrowStrokeTransformer( east );
		viewer.getRenderContext().setArrowFillPaintTransformer( aft );
		viewer.getRenderContext().setVertexFontTransformer( vlft );
		viewer.getRenderContext().setEdgeFontTransformer( elft );
		viewer.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.CNTR );
		viewer.getRenderContext().setLabelOffset( 0 );
		viewer.setVertexToolTipTransformer( vtt );
		viewer.setEdgeToolTipTransformer( ett );

		PickedStateListener psl = new PickedStateListener( viewer, this );
		PickedState<SEMOSSVertex> ps = viewer.getPickedVertexState();
		ps.addItemListener( psl );

		return viewer;
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
		controlPanel.setUndoButtonEnabled( gdm.getOverlayLevel() > 1 );
		controlPanel.setRedoButtonEnabled( gdm.hasRedoData() );
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
	 * is reset to {@link Constants#FR}. (Not all layouts can support all graph
	 * topologies)
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
		try {
			Constructor<?> constructor = layoutClass.getConstructor( Graph.class );
			layout = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( gdm.getGraph() );
			ok = true;
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			log.error( "could not create layout", e );
			layout = new FRLayout( gdm.getGraph() );
		}

		controlPanel.setGraphLayout( layout, gdm.getGraph() );
		view.setGraphLayout( layout );
		
		return ok;
	}

	public VisualizationViewer<SEMOSSVertex, SEMOSSEdge> getView() {
		return view;
	}

	/**
	 * Method printConnectedNodes.
	 */
	private void printConnectedNodes() {
		log.debug( "In print connected Nodes routine " );
		SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph = getSimpleGraph();

		ConnectivityInspector<SEMOSSVertex, SEMOSSEdge> ins
				= new ConnectivityInspector<>( graph );
		log.debug( "Number of vertices: " + graph.vertexSet().size()
				+ ", and edges: " + graph.edgeSet().size() );
		log.debug( "ins.isGraphConnected(): " + ins.isGraphConnected() );
		log.debug( "Number of connected sets is: " + ins.connectedSets().size() );
		Iterator<Set<SEMOSSVertex>> csIterator = ins.connectedSets().iterator();
		while ( csIterator.hasNext() ) {
			Set<SEMOSSVertex> vertSet = csIterator.next();
			Iterator<SEMOSSVertex> si = vertSet.iterator();
			while ( si.hasNext() ) {
				SEMOSSVertex vert = si.next();
				//log.info("Set " + count + ">>>> " + vert.getProperty(Constants.VERTEX_NAME));
			}
		}
	}

	/**
	 * Method printSpanningTree.
	 */
	@SuppressWarnings( "unused" )
	private void printSpanningTree() {
		SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph = getSimpleGraph();

		KruskalMinimumSpanningTree<SEMOSSVertex, SEMOSSEdge> spanningTree
				= new KruskalMinimumSpanningTree<>( graph );

		log.debug( "Spanning tree, Number of vertices: " + graph.vertexSet().size() );
		log.debug( "Spanning tree, Number of Edges:    " + spanningTree.getEdgeSet().size() );

		if ( log.isDebugEnabled() ) {
			for ( SEMOSSEdge edge : spanningTree.getEdgeSet() ) {
				log.debug( "Edge Name: " + edge.getProperty( Constants.EDGE_NAME ) );
			}
		}
	}

	/**
	 * Method getEdgeLabelFontTransformer.
	 *
	 * @return EdgeLabelFontTransformer
	 */
	public EdgeLabelFontTransformer getEdgeLabelFontTransformer() {
		return elft;
	}

	/**
	 * Method getVertexLabelFontTransformer.
	 *
	 * @return VertexLabelFontTransformer
	 */
	public VertexLabelFontTransformer getVertexLabelFontTransformer() {
		return vlft;
	}

	/**
	 * Method resetTransformers.
	 */
	public void resetTransformers() {

		EdgeStrokeTransformer tx = (EdgeStrokeTransformer) view.getRenderContext().getEdgeStrokeTransformer();
		tx.setEdges( null );
		ArrowDrawPaintTransformer atx = (ArrowDrawPaintTransformer) view.getRenderContext().getArrowDrawPaintTransformer();
		atx.setEdges( null );
		EdgeArrowStrokeTransformer east = (EdgeArrowStrokeTransformer) view.getRenderContext().getEdgeArrowStrokeTransformer();
		east.setEdges( null );
		VertexShapeTransformer vst = (VertexShapeTransformer) view.getRenderContext().getVertexShapeTransformer();
		vst.setVertexSizeHash( new HashMap<>() );

		if ( controlPanel.isHighlightButtonSelected() ) {
			VertexPaintTransformer ptx = (VertexPaintTransformer) view.getRenderContext().getVertexFillPaintTransformer();
			Set<SEMOSSVertex> searchVertices = new HashSet<>();
			searchVertices.addAll( controlPanel.getSearchController().getCleanResHash() );
			ptx.setVertHash( searchVertices );
			VertexLabelFontTransformer vfl = (VertexLabelFontTransformer) view.getRenderContext().getVertexFontTransformer();
			vfl.setVertHash( searchVertices );
		}
		else {
			VertexPaintTransformer ptx = (VertexPaintTransformer) view.getRenderContext().getVertexFillPaintTransformer();
			ptx.setVertHash( null );
			VertexLabelFontTransformer vfl = (VertexLabelFontTransformer) view.getRenderContext().getVertexFontTransformer();
			vfl.setVertHash( null );
		}
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
		setUndoRedoBtn();
	}

	@Override
	public void runAnalytics() {
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
		add( m, engine );
	}

	@Override
	public void overlay( Model m, IEngine engine ) {
		add( m, engine );
	}

	public void add( Model m, IEngine engine ) {
		setHeaders( Arrays.asList( "Subject", "Predicate", "Object" ) );
		gdm.addGraphLevel( m, engine );
		updateGraph();
	}

	@Override
	public void run() {
	}

	public static void initVvRenderer( RenderContext<SEMOSSVertex, SEMOSSEdge> rc, ControlData controlData ) {
		LabelTransformer<SEMOSSVertex> vlt = new LabelTransformer<>( controlData );
		LabelTransformer<SEMOSSEdge> elt = new LabelTransformer<>( controlData );

		VertexPaintTransformer vpt = new VertexPaintTransformer();
		EdgeStrokeTransformer est = new EdgeStrokeTransformer();
		VertexStrokeTransformer vst = new VertexStrokeTransformer();
		ArrowDrawPaintTransformer adpt = new ArrowDrawPaintTransformer();
		EdgeArrowStrokeTransformer east = new EdgeArrowStrokeTransformer();
		ArrowFillPaintTransformer aft = new ArrowFillPaintTransformer();
		//keep the stored one if possible
		VertexLabelFontTransformer vlft = new VertexLabelFontTransformer();
		EdgeLabelFontTransformer elft = new EdgeLabelFontTransformer();
		VertexShapeTransformer vsht = new VertexShapeTransformer();

		//view.setGraphMouse(mc);
		rc.setVertexLabelTransformer( vlt );
		rc.setEdgeLabelTransformer( elt );
		rc.setVertexStrokeTransformer( vst );
		rc.setVertexShapeTransformer( vsht );
		rc.setVertexFillPaintTransformer( vpt );
		rc.setEdgeStrokeTransformer( est );
		rc.setArrowDrawPaintTransformer( adpt );
		rc.setEdgeArrowStrokeTransformer( east );
		rc.setArrowFillPaintTransformer( aft );
		rc.setVertexFontTransformer( vlft );
		rc.setEdgeFontTransformer( elft );
		rc.setLabelOffset( 0 );
	}

	public LegendPanel2 getLegendPanel() {
		return legendPanel;
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		boolean increaseFont = ( incr > 0 );

		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer = controlPanel.getSearchController().getTarget();
		VertexLabelFontTransformer transformerV = (VertexLabelFontTransformer) viewer.getRenderContext().getVertexFontTransformer();
		EdgeLabelFontTransformer transformerE = (EdgeLabelFontTransformer) viewer.getRenderContext().getEdgeFontTransformer();

		//if no vertices or edges are selected, perform action on all vertices and edges
		if ( viewer.getPickedVertexState().getPicked().isEmpty()
				&& viewer.getPickedEdgeState().getPicked().isEmpty() ) {
			if ( increaseFont ) {
				transformerV.increaseFontSize();
				transformerE.increaseFontSize();
			}
			else {
				transformerV.decreaseFontSize();
				transformerE.decreaseFontSize();
			}
		}

		//otherwise, only perform action on the selected vertices and edges
		for ( SEMOSSVertex vertex : viewer.getPickedVertexState().getPicked() ) {
			if ( increaseFont ) {
				transformerV.increaseFontSize( vertex );
			}
			else {
				transformerV.decreaseFontSize( vertex );
			}
		}

		for ( SEMOSSEdge edge : viewer.getPickedEdgeState().getPicked() ) {
			if ( increaseFont ) {
				transformerE.increaseFontSize( edge );
			}
			else {
				transformerE.decreaseFontSize( edge );
			}
		}

		viewer.repaint();
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
}
