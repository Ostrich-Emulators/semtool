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
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DelegateForest;
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
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
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
import gov.va.semoss.ui.transformer.EdgeLabelTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.EdgeTooltipTransformer;
import gov.va.semoss.ui.transformer.VertexLabelFontTransformer;
import gov.va.semoss.ui.transformer.VertexLabelTransformer;
import gov.va.semoss.ui.transformer.VertexPaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.ui.transformer.VertexStrokeTransformer;
import gov.va.semoss.ui.transformer.VertexTooltipTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

/**
 */
public class GraphPlaySheet extends PlaySheetCentralComponent {
	private static final long serialVersionUID = 4699492732234656487L;
	protected static final Logger log = Logger.getLogger( GraphPlaySheet.class );

	private DelegateForest<SEMOSSVertex, SEMOSSEdge> forest;
	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view;
	private JSplitPane graphSplitPane;
	private ControlPanel searchPanel;
	private LegendPanel2 legendPanel;
	private VertexColorShapeData colorShapeData = new VertexColorShapeData();
	private String query;
	private boolean overlay;

	protected GraphDataModel gdm;
	protected String layoutName = Constants.FR;
	protected Layout<SEMOSSVertex, SEMOSSEdge> layout2Use;
	protected ControlData controlData = new ControlData();
	protected PropertySpecData predData = new PropertySpecData();
	protected final SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph
			= new SimpleGraph<>( SEMOSSEdge.class );
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
		createView();
	}

	/**
	 * Method setAppend.
	 *
	 * @param append boolean
	 */
	public void setAppend( boolean append ) {
		log.debug( "Append set to " + append );
		this.overlay = append;
		gdm.setOverlay( append );
	}

	public DelegateForest<SEMOSSVertex, SEMOSSEdge> getForest() {
		return forest;
	}

	public boolean getSudowl() {
		return gdm.showSudowl();
	}

	public GraphDataModel getGraphData() {
		return gdm;
	}

	public boolean isAppending() {
		return overlay;
	}

	public void setGraphData( GraphDataModel gdm ) {
		this.gdm = gdm;
	}

	/**
	 * Method createView.
	 */
	@Override
	public void createView() {
		setAppend( false );

		try {
			searchPanel = new ControlPanel( gdm.enableSearchBar() );
			addInitialPanel();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	public void processView() throws PropertyVetoException {
		createVisualizer();
		addPanel();
	}

	/**
	 * Method undoView. Get the latest view and undo it.
	 */
	public void undoView() {
		try {
			if ( gdm.getModelCounter() > 1 ) {
				gdm.undoData();
				filterData = new VertexFilterData();
				controlData = new ControlData();
				predData = new PropertySpecData();

				refineView();
				log.debug( "model size: " + gdm.getRC().size() );
			}

			genAllData();
		}
		catch ( RepositoryException e ) {
			log.error( e );
		}

	}

	/**
	 * Method redoView.
	 */
	public void redoView() {
		if ( gdm.getRCStoreSize() > gdm.getModelCounter() - 1 ) {
			gdm.redoData();
			refineView();
		}
	}

	@Override
	public void overlayView() {
		try {
			createForest( false );

			//add to overall modelstore			
			boolean successfulLayout = createLayout();
			if ( !successfulLayout ) {
				Utility.showMessage( "Current layout cannot handle the extend. Resetting to "
						+ Constants.FR + " layout..." );
				layoutName = Constants.FR;
				createLayout();
			}

			processView();
			setUndoRedoBtn();
		}
		catch ( Exception ex ) {
			log.error( ex, ex );
		}
	}

	/**
	 * Method removeView.
	 */
	public void removeView() {
		log.debug( "Removing view with SPARQL: " + getQuery() );

		gdm.removeView( getQuery(), getEngine() );
		gdm.fillStoresFromModel();

		refineView();
		log.debug( "Removing Forest Complete >>>>>> " );
	}

	/**
	 * Method refineView.
	 */
	@Override
	public void refineView() {
		try {
			gdm.fillStoresFromModel();
			createForest( true );
			log.debug( "Refining Forest Complete >>>>>" );

			createLayout();
			createVisualizer();

			addPanel();
			legendPanel.drawLegend();
			setUndoRedoBtn();
		}
		catch ( Exception e ) {
			log.error( e );
		}
	}

	/**
	 * Method refreshView.
	 */
	public void refreshView() {
		createVisualizer();
		addPanel();
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
	 * Method addInitialPanel
	 *
	 * Create the listener and add the frame. If there is a view, remove it.
	 */
	public void addInitialPanel() {
		legendPanel = new LegendPanel2();

		graphSplitPane = new JSplitPane();
		graphSplitPane.setEnabled( false );
		graphSplitPane.setOneTouchExpandable( true );
		graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
		searchPanel.setPlaySheet( this );

		graphSplitPane.setTopComponent( searchPanel );
		graphSplitPane.setBottomComponent( new JLabel() );

		setLayout( new BorderLayout() );
		add( graphSplitPane, BorderLayout.CENTER );
		add( legendPanel, BorderLayout.SOUTH );
		setVisible( true );
	}

	/**
	 * Method addPanel - adds the model to search panel
	 */
	protected void addPanel() {
		try {
			if ( gdm.enableSearchBar() ) {
				searchPanel.getSearchController().indexStatements( gdm.getJenaModel() );
			}

			GraphZoomScrollPane gzPane = new GraphZoomScrollPane( view );
			gzPane.getVerticalScrollBar().setUI( new NewScrollBarUI() );
			gzPane.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );

			graphSplitPane.setTopComponent( searchPanel );
			graphSplitPane.setBottomComponent( gzPane );

			legendPanel.setFilterData( filterData );

			log.debug( "Adding graph pane." );
//			addComponentAsTab( "Graph", graphSplitPane );
		}
		catch ( Exception ex ) {
			log.error( "problem adding panel to play sheet", ex );
		}
	}

	/**
	 * Method createVisualizer.
	 */
	protected void createVisualizer() {
		view = new VisualizationViewer<>( layout2Use );
		view.setPreferredSize( this.layout2Use.getSize() );
		view.setBounds( 10000000, 10000000, 10000000, 100000000 );
		view.setRenderer( new BasicRenderer<>() );

		GraphNodeListener gl = new GraphNodeListener( this );
		view.setGraphMouse( new GraphNodeListener( this ) );
		gl.setMode( ModalGraphMouse.Mode.PICKING );
		view.setGraphMouse( gl );
		
		VertexLabelTransformer vlt = new VertexLabelTransformer( controlData );
		VertexPaintTransformer vpt = new VertexPaintTransformer();
		VertexTooltipTransformer vtt = new VertexTooltipTransformer( controlData );
		EdgeLabelTransformer elt = new EdgeLabelTransformer( controlData );
		EdgeTooltipTransformer ett = new EdgeTooltipTransformer( controlData );
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

		view.setBackground( Color.WHITE );
		view.getRenderContext().setVertexLabelTransformer( vlt );
		view.getRenderContext().setEdgeLabelTransformer( elt );
		view.getRenderContext().setVertexStrokeTransformer( vst );
		view.getRenderContext().setVertexShapeTransformer( vsht );
		view.getRenderContext().setVertexFillPaintTransformer( vpt );
		view.getRenderContext().setEdgeStrokeTransformer( est );
		view.getRenderContext().setArrowDrawPaintTransformer( adpt );
		view.getRenderContext().setEdgeArrowStrokeTransformer( east );
		view.getRenderContext().setArrowFillPaintTransformer( aft );
		view.getRenderContext().setVertexFontTransformer( vlft );
		view.getRenderContext().setEdgeFontTransformer( elft );
		view.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.CNTR );
		view.getRenderContext().setLabelOffset( 0 );
		view.setVertexToolTipTransformer( vtt );
		view.setEdgeToolTipTransformer( ett );

		PickedStateListener psl = new PickedStateListener( view, this );
		PickedState<SEMOSSVertex> ps = view.getPickedVertexState();
		ps.addItemListener( psl );

		controlData.setViewer( view);
		searchPanel.setViewer( view );
		
		log.debug( "Completed Visualization >>>> " );
	}

	/**
	 * Method createLayout.
	 *
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean createLayout() {
		Class<?> layoutClass = (Class<?>) DIHelper.getInstance().getLocalProp( layoutName );
		log.debug( "Create layout from layoutName " + layoutName
				+ ", and layoutClass " + layoutClass );

		String errorMessage = "";
		layout2Use = null;
		try {
			Constructor<?> constructor = layoutClass.getConstructor( edu.uci.ics.jung.graph.Forest.class );
			layout2Use = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( forest );
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			errorMessage = e.toString();
		}

		try {
			Constructor<?> constructor = layoutClass.getConstructor( edu.uci.ics.jung.graph.Graph.class );
			layout2Use = (Layout<SEMOSSVertex, SEMOSSEdge>) constructor.newInstance( forest );
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			errorMessage = e.toString();
		}

		if ( layout2Use == null ) {
			log.error( errorMessage );
			return false;
		}

		searchPanel.setGraphLayout( layout2Use, getForest() );
		return true;
	}

	public String getLayoutName() {
		return layoutName;
	}

	protected void createForest( boolean initForest ) throws Exception {

		if ( initForest ) {
			forest = new DelegateForest<>();
		}

		Map<String, String> filteredNodes = filterData.getFilterNodes();
		log.debug( "Filtered Nodes " + filteredNodes );

		log.debug( "Adding edges from edgeStore to forest" );
		for ( String edgeURI : gdm.getEdgeStore().keySet() ) {
			SEMOSSEdge edge = gdm.getEdgeStore().get( edgeURI );
			SEMOSSVertex outVert = edge.getOutVertex();
			SEMOSSVertex inVert = edge.getInVertex();

			if ( filteredNodes.containsKey( inVert.getURI() )
					|| filteredNodes.containsKey( outVert.getURI() )
					|| filterData.getEdgeFilterNodes().containsKey( edge.getURI() ) ) {
				continue;
			}

			//add to forest
			forest.addEdge( edge, outVert, inVert );
			processControlData( edge );

			//add to filter data
			filterData.addEdge( edge );

			//add to pred data
			predData.addPredicateAvailable( edge.getURI() );
			predData.addConceptAvailable( inVert.getURI() );
			predData.addConceptAvailable( outVert.getURI() );

			//add to simple graph
			graph.addVertex( outVert );
			graph.addVertex( inVert );
			if ( outVert != inVert ) {// loops not allowed in simple graph
				log.debug( "Adding edge to graph <> " + outVert.getURI() + " <> "
						+ edge.getURI() + " <> " + inVert.getURI() + " <>" );
				graph.addEdge( outVert, inVert, edge );
			}
		}

		log.debug( "Done with edges... checking for isolated nodes" );
		//now for vertices--process control data and add what is necessary to the graph
		//use vert store to check for any isolated nodes and add to forest
		for ( SEMOSSVertex vert : gdm.getVertStore().values() ) {
			if ( filteredNodes.containsKey( vert.getURI() ) ) {
				continue;
			}

			processControlData( vert );
			filterData.addVertex( vert );
			if ( !forest.containsVertex( vert ) ) {
				forest.addVertex( vert );
				graph.addVertex( vert );
			}
		}

		genAllData();
		log.debug( "Creating Forest Complete >>>>>> " );
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
		if ( gdm.getModelCounter() > 1 ) {
			searchPanel.setUndoButtonEnabled( true );
		}
		else {
			searchPanel.setUndoButtonEnabled( false );
		}

		if ( gdm.getRCStoreSize() >= gdm.getModelCounter() ) {
			searchPanel.setRedoButtonEnabled( true );
		}
		else {
			searchPanel.setRedoButtonEnabled( false );
		}
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
	 * Method setForest.
	 *
	 * @param forest DelegateForest
	 */
	public void setForest( DelegateForest<SEMOSSVertex, SEMOSSEdge> forest ) {
		this.forest = forest;
	}

	/**
	 * Method setLayoutName.
	 *
	 * @param layout String
	 */
	public void setLayoutName( String layout ) {
		this.layoutName = layout;
	}

	/**
	 * Method getGraph.
	 *
	 * @return Graph
	 */
	public SimpleGraph<SEMOSSVertex, SEMOSSEdge> getGraph() {
		return graph;
	}

	public VisualizationViewer<SEMOSSVertex, SEMOSSEdge> getView() {
		return view;
	}

	/**
	 * Method printConnectedNodes.
	 */
	@SuppressWarnings("unused")
	private void printConnectedNodes() {
		log.debug( "In print connected Nodes routine " );
		ConnectivityInspector<SEMOSSVertex, SEMOSSEdge> ins = new ConnectivityInspector<SEMOSSVertex, SEMOSSEdge>( graph );
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
	@SuppressWarnings("unused")
	private void printSpanningTree() {
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
	 * Method setRC.
	 *
	 * @param rc RepositoryConnection
	 */
	public void setRC( RepositoryConnection rc ) {
		gdm.setRC( rc );
	}

	public RepositoryConnection getRC() {
		return gdm.getRC();
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

		if ( searchPanel.isHighlightButtonSelected() ) {
			VertexPaintTransformer ptx = (VertexPaintTransformer) view.getRenderContext().getVertexFillPaintTransformer();
			Map<String, String> searchVertices = new HashMap<>();
			searchVertices.putAll( searchPanel.getSearchController().getCleanResHash() );
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
		for ( String remQuery : subVector ) {
			try {
				log.debug( "Removing query " + remQuery );
				Update update = gdm.getRC().prepareUpdate( QueryLanguage.SPARQL, remQuery );
				update.execute();
				log.error( "removing concepts not implemented (being refactored)" );
				//this.gdm.baseRelEngine.execInsertQuery(remQuery);

			}
			catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
				log.error( e, e );
			}
		}
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
		String listOfChilds = null;
		for ( String adder : subjects.split( ";" ) ) {
			String parent = adder.substring( 0, adder.indexOf( "@@" ) );
			String child = adder.substring( adder.indexOf( "@@" ) + 2 );

			if ( listOfChilds == null ) {
				listOfChilds = child;
			}
			else {
				listOfChilds = listOfChilds + ";" + child;
			}

			SesameJenaConstructStatement st = new SesameJenaConstructStatement();
			st.setSubject( child );
			st.setPredicate( predicate );
			st.setObject( baseObject );

			gdm.addToSesame( st );
			log.debug( " Query....  " + parent + "<>" + child );
		}

		return listOfChilds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getData() {
		Map<String, Object> returnHash = (Map<String, Object>) super.getData();
		if ( overlay ) {
			returnHash.put( "nodes", gdm.getIncrementalVertStore() );
			returnHash.put( "edges", gdm.getIncrementalEdgeStore().values() );
		}
		else {
			returnHash.put( "nodes", gdm.getVertStore() );
			returnHash.put( "edges", gdm.getEdgeStore().values() );
		}

		return returnHash;
	}

	public void genAllData() {
		filterData.fillRows();
		filterData.fillEdgeRows();

		controlData.generateAllRows();

		if ( gdm.showSudowl() ) {
			predData.genPredList();
		}

		colorShapeData.fillRows( filterData.getTypeHash() );
	}

	@Override
	public void runAnalytics() {}

	private void processControlData( SEMOSSEdge edge ) {
		for ( String property : edge.getProperties().keySet() )
			controlData.addEdgeProperty( edge.getEdgeType(), property );
	}

	private void processControlData( SEMOSSVertex vertex ) {
		for ( String property : vertex.getProperties().keySet() )
			controlData.addVertexProperty( vertex.getType(), property );
	}

	@Override
	public void createData() {
		gdm.createModel( getQuery(), getEngine() );
		gdm.fillStoresFromModel();
	}

	@Override
	public void setQuery( String q ) {
		query = q;
	}

	@Override
	public String getQuery() {
		return query;
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
	public void overlay( List<Value[]> data, List<String> headers ) {
		setAppend( true );
		createData();
		try {
			createForest( false );
			createLayout();
			processView();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public void create( List<Value[]> valdata, List<String> headers, IEngine engine ) {
		createData();

		try {
			createForest( true );
			createLayout();
			processView();
		}
		catch ( Exception ex ) {
			log.error( ex, ex );
		}
	}

	@Override
	public void run() {
	}

	public static void initVvRenderer( RenderContext<SEMOSSVertex, SEMOSSEdge> rc, ControlData controlData ) {
		VertexLabelTransformer vlt = new VertexLabelTransformer( controlData );
		VertexPaintTransformer vpt = new VertexPaintTransformer();
		EdgeLabelTransformer elt = new EdgeLabelTransformer( controlData );
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
		super.incrementFont(incr);
		boolean increaseFont = ( incr > 0 ? true : false );
		
		VisualizationViewer<SEMOSSVertex,SEMOSSEdge> viewer = searchPanel.getSearchController().getTarget();
		VertexLabelFontTransformer transformerV = (VertexLabelFontTransformer) viewer.getRenderContext().getVertexFontTransformer();
		EdgeLabelFontTransformer transformerE = (EdgeLabelFontTransformer) viewer.getRenderContext().getEdgeFontTransformer();
		
		//if no vertices or edges are selected, perform action on all vertices and edges
		if(		viewer.getPickedVertexState().getPicked().isEmpty() && 
				viewer.getPickedEdgeState().getPicked().isEmpty() ){
			if(increaseFont){
				transformerV.increaseFontSize();
				transformerE.increaseFontSize();
			} else {
				transformerV.decreaseFontSize();
				transformerE.decreaseFontSize();
			}
		}
		
		//otherwise, only perform action on the selected vertices and edges
		for (SEMOSSVertex vertex:viewer.getPickedVertexState().getPicked()) {
			if(increaseFont)
				transformerV.increaseFontSize(vertex.getURI());
			else
				transformerV.decreaseFontSize(vertex.getURI());
		}

		for (SEMOSSEdge edge:viewer.getPickedEdgeState().getPicked()) {
			if(increaseFont)
				transformerE.increaseFontSize(edge.getURI());
			else
				transformerE.decreaseFontSize(edge.getURI());
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
		return searchPanel;
	}
}