/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.ui.components.NewHoriScrollBarUI;
import gov.va.semoss.ui.components.NewScrollBarUI;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PaintLabel;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.tabbedqueries.SyntaxTextEditor;
import gov.va.semoss.ui.transformer.ArrowPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.ui.transformer.VertexStrokeTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class GraphicalQueryPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( GraphicalQueryPanel.class );
	private IEngine engine;
	private final String progress;
	private final Action addConceptNodeAction;
	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE );
	private final DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = new DirectedSparseGraph<>();
	//private final ObservableGraph<SEMOSSVertex, SEMOSSEdge> observer =
	//		new ObservableGraph<>( graph );
	private final Layout<SEMOSSVertex, SEMOSSEdge> vizlayout = new StaticLayout<>( graph );
	private final VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view
			= new VisualizationViewer<>( vizlayout );
	private final MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config
			= new MultiMap<>();
	private final VertexFactory vfac = new VertexFactory( config );
	private final EdgeFactory efac = new EdgeFactory( config );
	private GqbLabelTransformer<SEMOSSVertex> vlt;
	private GqbLabelTransformer<SEMOSSEdge> elt;
	private SyntaxTextEditor sparqlarea;

	/**
	 * Creates new form GraphicalQueryBuilderPanel
	 */
	public GraphicalQueryPanel( String progressname ) {
		progress = progressname;
		initComponents();
		initVizualizer();

		GraphZoomScrollPane zoomer = new GraphZoomScrollPane( view );
		zoomer.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		zoomer.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );
		visarea.add( zoomer );

		addConceptNodeAction = new AbstractAction() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				URI concept = new URIImpl( e.getActionCommand() );
				log.debug( "clicked on " + concept );
				vfac.setType( concept );
			}
		};
	}

	public void setSparqlArea( SyntaxTextEditor ste ) {
		sparqlarea = ste;
	}

	public String getQuery() {
		return ( null == sparqlarea ? "" : sparqlarea.getText() );
	}

	public void setEngine( IEngine eng ) {
		engine = eng;

		ProgressTask pt = new ProgressTask( "Initializing Graphical Query Builder",
				new Runnable() {

					@Override
					public void run() {
						buildTypeSelector();
					}
				}
		);

		OperationsProgress.getInstance( progress ).add( pt );
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    topsplit = new javax.swing.JSplitPane();
    visarea = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    typearea = new javax.swing.JPanel();

    setLayout(new java.awt.BorderLayout());

    topsplit.setDividerLocation(250);
    topsplit.setResizeWeight(0.75);

    visarea.setLayout(new java.awt.BorderLayout());
    topsplit.setRightComponent(visarea);

    typearea.setLayout(new java.awt.GridLayout(1, 1));
    jScrollPane1.setViewportView(typearea);

    topsplit.setLeftComponent(jScrollPane1);

    add(topsplit, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSplitPane topsplit;
  private javax.swing.JPanel typearea;
  private javax.swing.JPanel visarea;
  // End of variables declaration//GEN-END:variables

	private void buildTypeSelector() {

		try {
			SwingUtilities.invokeAndWait( new Runnable() {

				@Override
				public void run() {
					vlt.setEngine( engine );
					elt.setEngine( engine );
					if ( null != engine ) {

						typearea.removeAll();
						GridLayout gl = GridLayout.class.cast( typearea.getLayout() );

						List<URI> concepts = DBToLoadingSheetExporter.createConceptList( engine );
						Map<URI, String> conceptlabels = Utility.getInstanceLabels( concepts, engine );
						conceptlabels.put( Constants.ANYNODE, "<Any>" );
						gl.setRows( conceptlabels.size() );

						ButtonGroup group = new ButtonGroup();

						for ( Map.Entry<URI, String> en : Utility.sortUrisByLabel( conceptlabels ).entrySet() ) {
							JToggleButton button = new JToggleButton( addConceptNodeAction );
							button.setText( en.getValue() );
							button.setActionCommand( en.getKey().stringValue() );
							SEMOSSVertex v = new SEMOSSVertex( uribuilder.uniqueUri(),
									en.getKey(), en.getValue() );
							button.setIcon( PaintLabel.makeShapeIcon( v.getColor(), v.getShape(),
									new Dimension( 15, 15 ) ) );
							typearea.add( button );
							group.add( button );
						}
					}

					typearea.revalidate();
					typearea.repaint();
				}
			} );
		}
		catch ( InterruptedException | InvocationTargetException e ) {
			log.error( e, e );
		}
	}

	private void initVizualizer() {
		LabelFontTransformer<SEMOSSVertex> vft = new LabelFontTransformer<>();
		vlt = new GqbLabelTransformer<>( getEngine() );
		PaintTransformer<SEMOSSVertex> vpt = new PaintTransformer<>();
		VertexShapeTransformer vht = new VertexShapeTransformer();
		VertexStrokeTransformer vst = new VertexStrokeTransformer();

		LabelFontTransformer<SEMOSSEdge> eft = new LabelFontTransformer<>();
		elt = new GqbLabelTransformer<>( getEngine() );
		PaintTransformer<SEMOSSEdge> ept = new PaintTransformer<SEMOSSEdge>() {
			@Override
			protected Paint transformNotSelected( SEMOSSEdge t, boolean skel ) {
				// always show the edge
				return super.transformNotSelected( t, false );
			}
		};
		EdgeStrokeTransformer est = new EdgeStrokeTransformer();
		ArrowPaintTransformer adpt = new ArrowPaintTransformer();
		ArrowPaintTransformer aft = new ArrowPaintTransformer();

		addMouse();
		view.setBackground( Color.WHITE );

		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = view.getRenderContext();
		rc.setVertexLabelTransformer( vlt );
		rc.setVertexStrokeTransformer( vst );
		rc.setVertexShapeTransformer( vht );
		rc.setVertexFillPaintTransformer( vpt );
		rc.setVertexFontTransformer( vft );

		rc.setEdgeLabelTransformer( elt );
		rc.setEdgeDrawPaintTransformer( ept );
		rc.setEdgeStrokeTransformer( est );
		rc.setEdgeArrowStrokeTransformer( est );
		rc.setEdgeFontTransformer( eft );
		rc.setArrowDrawPaintTransformer( adpt );
		rc.setArrowFillPaintTransformer( aft );
		view.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.S );
		rc.setLabelOffset( 0 );

		//PickedStateListener psl = new PickedStateListener( view, this );
		//view.getPickedVertexState().addItemListener( psl );
		//view.getPickedEdgeState().addItemListener( psl );
	}

	public VisualizationViewer<SEMOSSVertex, SEMOSSEdge> getViewer() {
		return view;
	}

	public void update() {
		view.repaint();
		updateSparql();
	}

	public void clear() {
		config.clear();
		List<SEMOSSVertex> verts = new ArrayList<>( graph.getVertices() );
		for ( SEMOSSVertex v : verts ) {
			graph.removeVertex( v );
		}
		vizlayout.reset();
		update();
	}

	public void remove( AbstractNodeEdgeBase v ) {
		if ( v instanceof SEMOSSVertex ) {
			graph.removeVertex( SEMOSSVertex.class.cast( v ) );
		}
		else {
			graph.removeEdge( SEMOSSEdge.class.cast( v ) );
		}
		vizlayout.reset();
		update();
	}

	public IEngine getEngine() {
		return engine;
	}

	/**
	 * Gets a reference to this query's graph
	 *
	 * @return
	 */
	public DirectedGraph<SEMOSSVertex, SEMOSSEdge> getGraph() {
		return graph;
	}

	/**
	 * Gets a reference to the sparql configs for this query
	 *
	 * @return
	 */
	public MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> getSparqlConfigs() {
		return config;
	}

	private void addMouse() {
		EditingModalGraphMouse gm
				= new EditingModalGraphMouse( view.getRenderContext(), vfac, efac );
		gm.remove( gm.getPopupEditingPlugin() );
		gm.add( new MousePopuper() );
		view.setGraphMouse( gm );
	}

	private void updateSparql() {
		if ( null != sparqlarea ) {
			updateSparqlConfigs();
			String sparql = ( 0 == graph.getVertexCount()
					? ""
					: new GraphToSparql( getEngine().getNamespaces() ).select( graph, config ) );
			sparqlarea.setText( sparql );
		}
	}

	private int findNextId( String nodeOrLink ) {
		int maxid = -1;
		Pattern pat = Pattern.compile( nodeOrLink + "([0-9]+)$" );

		for ( Map.Entry<AbstractNodeEdgeBase, List<SparqlResultConfig>> en : config.entrySet() ) {
			for ( SparqlResultConfig src : en.getValue() ) {
				Matcher m = pat.matcher( src.getLabel() );
				if ( m.matches() ) {
					String val = m.group( 1 );
					int id = Integer.parseInt( val );

					if ( id > maxid ) {
						id = maxid;
					}
				}
			}
		}

		return maxid + 1;
	}

	private void updateSparqlConfigs() {
		int nextnodeid = findNextId( "node" );
		int nextlinkid = findNextId( "link" );
		int nextobjid = findNextId( "obj" );

		for ( SEMOSSVertex v : graph.getVertices() ) {
			if ( !config.containsKey( v ) ) {
				config.add( v, new SparqlResultConfig( v, RDF.SUBJECT,
						"node" + ( nextnodeid++ ) ) );
			}
		}

		for ( SEMOSSEdge v : graph.getEdges() ) {
			if ( !config.containsKey( v ) ) {
				config.add( v, new SparqlResultConfig( v, RDF.SUBJECT,
						"link" + ( nextlinkid++ ) ) );
			}
		}

		for ( SEMOSSVertex v : graph.getVertices() ) {
			List<SparqlResultConfig> vals = config.getNN( v );

			Set<URI> seen = new HashSet<>();
			for ( SparqlResultConfig src : vals ) {
				seen.add( src.getProperty() );
			}

			// skip properties if we already have a property label for it
			Set<URI> todo = new HashSet<>( v.getProperties().keySet() );
			todo.removeAll( seen );

			for ( URI prop : todo ) {
				vals.add( new SparqlResultConfig( v, prop, "obj" + ( nextobjid++ ) ) );
			}
		}

		for ( SEMOSSEdge v : graph.getEdges() ) {
			List<SparqlResultConfig> vals = config.getNN( v );

			Set<URI> seen = new HashSet<>();
			for ( SparqlResultConfig src : vals ) {
				seen.add( src.getProperty() );
			}

			// skip properties if we already have a property label for it
			Set<URI> todo = new HashSet<>( v.getProperties().keySet() );
			todo.removeAll( seen );

			for ( URI prop : todo ) {
				vals.add( new SparqlResultConfig( v, prop, "obj" + ( nextobjid++ ) ) );
			}
		}
	}

	private class MousePopuper extends AbstractPopupGraphMousePlugin {

		public MousePopuper() {
		}

		public MousePopuper( int modifiers ) {
			super( modifiers );
		}

		@Override
		protected void handlePopup( MouseEvent e ) {
			Point p = e.getPoint();

			GraphElementAccessor<SEMOSSVertex, SEMOSSEdge> pickSupport = view.getPickSupport();
			if ( null != pickSupport ) {
				final SEMOSSVertex vertex
						= pickSupport.getVertex( view.getGraphLayout(), p.getX(), p.getY() );
				if ( null == vertex ) {
					final SEMOSSEdge edge
							= pickSupport.getEdge( view.getGraphLayout(), p.getX(), p.getY() );
					if ( null == edge ) {
						new EmptySpacePopup( GraphicalQueryPanel.this ).show( view, p.x, p.y );
					}
					else {
						NodeEdgeBasePopup edgepop
								= NodeEdgeBasePopup.forEdge( edge, GraphicalQueryPanel.this );
						edgepop.show( view, p.x, p.y );
					}
				}
				else {
					NodeEdgeBasePopup vertpop
							= NodeEdgeBasePopup.forVertex( vertex, GraphicalQueryPanel.this );
					vertpop.show( view, p.x, p.y );
				}
			}
		}
	}
}
