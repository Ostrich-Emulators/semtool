/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.EditingPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import com.ostrichemulators.semtool.ui.components.OperationsProgress;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.PaintLabel;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.tabbedqueries.SparqlTextArea;
import com.ostrichemulators.semtool.om.GraphColorRepository;
import com.ostrichemulators.semtool.ui.helpers.DefaultGraphShapeRepository;
import com.ostrichemulators.semtool.ui.transformer.ArrowPaintTransformer;
import com.ostrichemulators.semtool.ui.transformer.EdgeStrokeTransformer;
import com.ostrichemulators.semtool.ui.transformer.LabelFontTransformer;
import com.ostrichemulators.semtool.ui.transformer.PaintTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexShapeTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexStrokeTransformer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;

import com.ostrichemulators.semtool.util.Utility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final DirectedGraph<QueryNode, QueryEdge> graph = new DirectedSparseMultigraph<>();
	private final ObservableGraph<QueryNode, QueryEdge> observer
			= new ObservableGraph<>( graph );
	private final Layout<QueryNode, QueryEdge> vizlayout = new StaticLayout<>( observer );
	private final VisualizationViewer<QueryNode, QueryEdge> view
			= new VisualizationViewer<>( vizlayout );
	private final VertexFactory vfac = new VertexFactory();
	private final EdgeFactory efac = new EdgeFactory();
	private final List<QueryOrder> ordering = new ArrayList<>();
	private GqbLabelTransformer<QueryNode> vlt;
	private GqbLabelTransformer<QueryEdge> elt;
	private SparqlTextArea sparqlarea;
	private EditingModalGraphMouse mouse;
	private ButtonGroup buttongroup;

	/**
	 * Creates new form GraphicalQueryBuilderPanel
	 */
	public GraphicalQueryPanel( String progressname ) {
		progress = progressname;
		initComponents();
		initVizualizer();
		visarea.add( new GraphZoomScrollPane( view ) );

		addConceptNodeAction = new AbstractAction() {
			private static final long serialVersionUID = -2138227128423655724L;

			@Override
			public void actionPerformed( ActionEvent e ) {
				URI concept = new URIImpl( e.getActionCommand() );
				vfac.setType( concept );
			}
		};

		addGraphListener();
	}

	public void setSparqlArea( SparqlTextArea ste ) {
		sparqlarea = ste;
	}

	public String getQuery() {
		return ( null == sparqlarea ? "" : sparqlarea.getText() );
	}

	public void setEngine( IEngine eng ) {
		engine = eng;

		ProgressTask pt = new ProgressTask( "Setting engine for Graphical Query Builder",
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
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DefaultGraphShapeRepository shapefactory = new DefaultGraphShapeRepository();
					vlt.setEngine( engine );
					elt.setEngine( engine );
					if ( null != engine ) {

						typearea.removeAll();
						GridLayout gl = GridLayout.class.cast( typearea.getLayout() );

						List<URI> concepts = NodeDerivationTools.createConceptList( engine );
						Map<URI, String> conceptlabels
								= Utility.getInstanceLabels( concepts, engine );
						conceptlabels.put( Constants.ANYNODE, "<Any>" );
						gl.setRows( conceptlabels.size() );

						buttongroup = new ButtonGroup();

						Map<URI, String> sorted = Utility.sortUrisByLabel( conceptlabels );
						for ( Map.Entry<URI, String> en : sorted.entrySet() ) {
							JToggleButton button = new JToggleButton( addConceptNodeAction );
							button.setText( en.getValue() );
							button.setActionCommand( en.getKey().stringValue() );
							QueryNode v = new QueryNode( uribuilder.uniqueUri(),
									en.getKey(), en.getValue() );
							v.setColor( GraphColorRepository.instance().getColor( en.getKey() ) );
							v.setShape( shapefactory.getRawShape( en.getKey() ) );

							button.setIcon( PaintLabel.makeShapeIcon( v.getColor(), v.getShape(),
									new Dimension( 12, 12 ) ) );
							typearea.add( button );
							buttongroup.add( button );
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
		LabelFontTransformer<QueryNode> vft = new LabelFontTransformer<>();
		vlt = new GqbLabelTransformer( getEngine() );
		PaintTransformer<QueryNode> vpt = new PaintTransformer<>();
		VertexShapeTransformer vht = new VertexShapeTransformer();
		VertexStrokeTransformer vst = new VertexStrokeTransformer();

		LabelFontTransformer<QueryEdge> eft = new LabelFontTransformer<>();
		elt = new GqbLabelTransformer( getEngine() );
		PaintTransformer<QueryEdge> ept = new PaintTransformer<QueryEdge>() {
			@Override
			protected Paint transformNotSelected( QueryEdge t, boolean skel ) {
				// always show the edge
				return super.transformNotSelected( t, false );
			}
		};
		EdgeStrokeTransformer est = new EdgeStrokeTransformer( 1.5, 1.5, 1.5 );
		ArrowPaintTransformer adpt = new ArrowPaintTransformer();
		ArrowPaintTransformer aft = new ArrowPaintTransformer();

		addMouse();
		view.setBackground( Color.WHITE );

		RenderContext<QueryNode, QueryEdge> rc = view.getRenderContext();
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

	public VisualizationViewer<QueryNode, QueryEdge> getViewer() {
		return view;
	}

	public void update() {
		updateSparql();
		view.repaint();
	}

	public void clear() {
		List<QueryNode> verts = new ArrayList<>( graph.getVertices() );
		for ( QueryNode v : verts ) {
			graph.removeVertex( v );
		}

		vizlayout.reset();
		ordering.clear();
		update();
	}

	public void remove( QueryGraphElement v ) {
		if ( v instanceof QueryNode ) {
			graph.removeVertex( QueryNode.class.cast( v ) );
		}
		else {
			graph.removeEdge( QueryEdge.class.cast( v ) );
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
	public DirectedGraph<QueryNode, QueryEdge> getGraph() {
		return graph;
	}

	private void addMouse() {
		mouse = new EditingModalGraphMouse( view.getRenderContext(), vfac, efac );

		mouse.remove( mouse.getPopupEditingPlugin() );
		mouse.add( new MousePopuper() );

		mouse.remove( mouse.getEditingPlugin() );
		mouse.add( new QueryGraphMousePlugin<>( vfac, efac ) );

		view.setGraphMouse( mouse );
	}

	private void updateSparql() {
		updateSparqlConfigs();

		if ( null != sparqlarea ) {
			String sparql = ( 0 == graph.getVertexCount()
					? ""
					: new GraphToSparql( getEngine().getNamespaces() ).select( graph,
							getQueryOrdering() ) );
			sparqlarea.setText( sparql );
		}
	}

	private String createQueryId( QueryGraphElement v,
			Collection<QueryGraphElement> nodesAndEdges ) {
		// this is a two-step process
		// 1) figure out what names have been used
		// 2) come up with a name based on my first type
		// 2a) if that name has already been used, come up with another one
		// new names will be typelabel plus an integer
		Set<String> used = new HashSet<>();
		int maxid = -1;
		String base = ( Constants.ANYNODE.equals( v.getType() )
				? ( v.isNode() ? "node" : "link" )
				: v.getType().getLocalName() );

		Pattern pat = Pattern.compile( base + "([0-9]+)$" );

		for ( QueryGraphElement qn : nodesAndEdges ) {
			String usedlabel = qn.getQueryId();

			if ( null != usedlabel ) {
				used.add( usedlabel );

				Matcher m = pat.matcher( usedlabel );
				if ( m.matches() ) {
					String val = m.group( 1 );
					int id = Integer.parseInt( val );

					if ( id > maxid ) {
						maxid = id;
					}
				}
			}
		}

		++maxid;
		String val = base + ( 0 == maxid ? "" : maxid );
		while ( used.contains( val ) ) {
			val = base + ( ++maxid );
		}
		return val;

	}

	private String createVariableId( QueryGraphElement v, URI type,
			Collection<QueryGraphElement> nodesAndEdges ) {
		// just like createQueryId, but for objects
		Set<String> used = new HashSet<>();
		int maxid = -1;
		String base = v.getQueryId() + "_" + type.getLocalName();
		Pattern pat = Pattern.compile( base + "([0-9]+)$" );

		for ( QueryGraphElement qn : nodesAndEdges ) {
			for ( URI prop : qn.getAllValues().keySet() ) {
				String usedlabel = qn.getLabel( prop );

				if ( null != usedlabel ) {
					used.add( usedlabel );

					Matcher m = pat.matcher( usedlabel );
					if ( m.matches() ) {
						String val = m.group( 1 );
						int id = Integer.parseInt( val );

						if ( id > maxid ) {
							maxid = id;
						}
					}
				}
			}
		}

		++maxid;
		String val = base + ( 0 == maxid ? "" : maxid );
		while ( used.contains( val ) ) {
			val = base + ( ++maxid );
		}
		return val;
	}

	/**
	 * Assigns new configs to nodes in the graph after first removing old configs.
	 */
	private void updateSparqlConfigs() {
		Set<QueryGraphElement> todo = new LinkedHashSet<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		// remove any ordering information for non-existant elements
		Iterator<QueryOrder> it = ordering.iterator();
		while ( it.hasNext() ) {
			QueryOrder qo = it.next();

			if ( !todo.contains( qo.base ) ) {
				it.remove();
			}
		}

		for ( QueryGraphElement v : todo ) {
			if ( null == v.getQueryId() ) {
				v.setQueryId( createQueryId( v, todo ) );
			}

			for ( URI uri : v.getAllValues().keySet() ) {
				if ( null == v.getLabel( uri ) ) {
					// come up with a variable name
					v.setLabel( uri, createVariableId( v, uri, todo ) );
				}
			}
		}
	}

	private void addGraphListener() {
		observer.addGraphEventListener( new GraphEventListener() {

			@Override
			public void handleGraphEvent( GraphEvent evt ) {
				updateSparql();
			}
		} );
	}

	public List<QueryOrder> getQueryOrdering() {
		return new ArrayList<>( ordering );
	}

	public void setQueryOrdering( List<QueryOrder> neworder ) {
		ordering.clear();
		ordering.addAll( neworder );
		updateSparql();
	}

	private class MousePopuper extends EditingPopupGraphMousePlugin {

		public MousePopuper() {
			super( vfac, efac );
		}

		@Override
		protected void handlePopup( MouseEvent e ) {
			Point p = e.getPoint();

			GraphElementAccessor<QueryNode, QueryEdge> pickSupport = view.getPickSupport();
			if ( null != pickSupport ) {
				final QueryNode vertex
						= pickSupport.getVertex( view.getGraphLayout(), p.getX(), p.getY() );
				if ( null == vertex ) {
					final QueryEdge edge
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

	public static class QueryOrder {

		public final QueryGraphElement base;
		public final URI property;

		public QueryOrder( QueryGraphElement base, URI property ) {
			this.base = base;
			this.property = property;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 47 * hash + Objects.hashCode( this.base );
			hash = 47 * hash + Objects.hashCode( this.property );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final QueryOrder other = (QueryOrder) obj;
			if ( !Objects.equals( this.base, other.base ) ) {
				return false;
			}
			if ( !Objects.equals( this.property, other.property ) ) {
				return false;
			}
			return true;
		}
	}
}
