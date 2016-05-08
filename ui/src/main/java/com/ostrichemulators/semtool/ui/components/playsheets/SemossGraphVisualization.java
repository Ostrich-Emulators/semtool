/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.google.common.base.Predicate;
import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.GraphModelListener;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.transformer.ArrowPaintTransformer;
import com.ostrichemulators.semtool.ui.transformer.EdgeStrokeTransformer;
import com.ostrichemulators.semtool.ui.transformer.LabelFontTransformer;
import com.ostrichemulators.semtool.ui.transformer.LabelTransformer;
import com.ostrichemulators.semtool.ui.transformer.PaintTransformer;
import com.ostrichemulators.semtool.ui.transformer.SelectingTransformer;
import com.ostrichemulators.semtool.ui.transformer.TooltipTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexShapeTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexStrokeTransformer;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class SemossGraphVisualization extends VisualizationViewer<SEMOSSVertex, SEMOSSEdge> {

	public static final String VISIBILITY_CHANGED = "visibility-changed";
	public static final String LAYOUT_CHANGED = "layout-changed";
	public static final String GRAPH_CHANGED = "graph-changed";

	private static final Logger log = Logger.getLogger( SemossGraphVisualization.class );
	private final GraphDataModel gdm;
	protected int overlayLevel = 0;

	protected LabelFontTransformer<SEMOSSVertex> vft = new LabelFontTransformer<>();
	protected LabelTransformer<SEMOSSVertex> vlt = new LabelTransformer<>();
	protected TooltipTransformer<SEMOSSVertex> vtt = new TooltipTransformer<>();
	protected PaintTransformer<SEMOSSVertex> vpt = new PaintTransformer<>();
	protected VertexShapeTransformer vht = new VertexShapeTransformer();
	protected VertexStrokeTransformer vst = new VertexStrokeTransformer();

	protected LabelFontTransformer<SEMOSSEdge> eft = new LabelFontTransformer<>();
	protected LabelTransformer<SEMOSSEdge> elt = new LabelTransformer<>();
	protected TooltipTransformer<SEMOSSEdge> ett = new TooltipTransformer<>();
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
	protected boolean skeletonmode = false;

	private final HidingPredicate<? extends GraphElement> predicate = new HidingPredicate<>();
	private final VertexPredicateFilter<SEMOSSVertex, SEMOSSEdge> visibleFilter
			= new VertexPredicateFilter<>( (HidingPredicate<SEMOSSVertex>) predicate );
	private final Set<URI> hiddens = new HashSet<>();

	public SemossGraphVisualization( GraphDataModel gdm ) {
		super( new FRLayout<>( gdm.getGraph() ) );
		this.gdm = gdm;
		init();

		gdm.addModelListener( new GraphModelListener() {

			@Override
			public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					int level, GraphDataModel gdm ) {
				refresh();
			}
		} );

	}

	public void setOverlayLevel( int level ) {
		overlayLevel = level;
	}

	public void setLabelCache( Map<Value, String> map ) {
		LabelTransformer lt[] = { vlt, vtt, elt, ett };
		for ( LabelTransformer l : lt ) {
			l.setLabelCache( map );
		}
	}

	public void setColorShapeRepository( GraphColorShapeRepository repo ) {
		vht.setColorShapeRepository( repo );
		vpt.setColorShapeRepository( repo );
		ept.setColorShapeRepository( repo );
		adpt.setColorShapeRepository( repo );
		aft.setColorShapeRepository( repo );
	}

	public void hide( URI uri, boolean hideme ) {
		if ( hideme ) {
			hiddens.add( uri );
		}
		else {
			hiddens.remove( uri );
		}

		firePropertyChange( VISIBILITY_CHANGED, false, true );
		refresh();
	}

	public void hide( Collection<? extends GraphElement> elements, boolean hideme ) {
		List<URI> uris = new ArrayList<>();
		for ( GraphElement e : elements ) {
			uris.add( e.getURI() );
		}

		if ( hideme ) {
			hiddens.addAll( uris );
		}
		else {
			hiddens.removeAll( uris );
		}

		firePropertyChange( VISIBILITY_CHANGED, false, true );
		refresh();
	}

	public void clearHiddens() {
		hiddens.clear();
		firePropertyChange( VISIBILITY_CHANGED, false, true );
		refresh();
	}

	public Set<URI> getHiddens() {
		return new HashSet<>( hiddens );
	}

	public boolean isHidden( GraphElement element ) {
		return ( isHidden( element.getURI() ) || isHidden( element.getType() ) );
	}

	public boolean isHidden( URI uri ) {
		return hiddens.contains( uri );
	}

	public void refresh() {
		setGraphLayout( getEffectiveLayout().getClass() );
	}

	public Layout<SEMOSSVertex, SEMOSSEdge> getEffectiveLayout() {
		Layout<SEMOSSVertex, SEMOSSEdge> currentlayout = super.getGraphLayout();
		ObservableCachingLayout<SEMOSSVertex, SEMOSSEdge> ocl
				= ObservableCachingLayout.class.cast( currentlayout );
		return (Layout<SEMOSSVertex, SEMOSSEdge>) ocl.getDelegate();
	}

	public void setGraphLayout( Class<? extends Layout> layklass ) {
		Layout oldlayout = getEffectiveLayout();

		Graph<SEMOSSVertex, SEMOSSEdge> graph = visibleFilter.apply( gdm.getGraph() );
		Constructor<? extends Layout<SEMOSSVertex, SEMOSSEdge>> constructor = null;

		try {
			Constructor<? extends Layout> ctor = layklass.getConstructor( new Class[]{ Graph.class } );
			constructor = (Constructor<? extends Layout<SEMOSSVertex, SEMOSSEdge>>) ctor;
		}
		catch ( NoSuchMethodException | SecurityException e ) {
			// ignore this for now
		}

		if ( null == constructor ) {
			try {
				Constructor<? extends Layout> ctor = layklass.getConstructor( new Class[]{ Forest.class } );
				constructor = (Constructor<? extends Layout<SEMOSSVertex, SEMOSSEdge>>) ctor;
			}
			catch ( NoSuchMethodException | SecurityException e ) {
				log.warn( "cannot figure out this layout, using FR by default", e );
				throw new RuntimeException( "Cannot find default layout" );
			}
		}

		if ( null == constructor ) {
			try {
				Constructor<? extends Layout> ctor
						= FRLayout.class.getConstructor( new Class[]{ Graph.class } );
				constructor = (Constructor<? extends Layout<SEMOSSVertex, SEMOSSEdge>>) ctor;
			}
			catch ( NoSuchMethodException | SecurityException x ) {
				log.error( x, x );
			}
		}

		if ( null == constructor ) {
			// should never be null here anyway
			return;
		}

		Layout<SEMOSSVertex, SEMOSSEdge> newlayout = null;
		try {
			Object o = constructor.newInstance( graph );
			newlayout = (Layout<SEMOSSVertex, SEMOSSEdge>) o;

			newlayout.setInitializer( super.getGraphLayout() );
			try {
				newlayout.setSize( getSize() );
			}
			catch ( UnsupportedOperationException ueo ) {
				// not all layouts can have their sizes set, but
				// there's no way to tell which is which
			}

			LayoutTransition<SEMOSSVertex, SEMOSSEdge> lt
					= new LayoutTransition<>( this, getGraphLayout(), newlayout );
			Animator animator = new Animator( lt );
			animator.start();
			getRenderContext().getMultiLayerTransformer().setToIdentity();
			repaint();
		}
		catch ( InstantiationException | IllegalAccessException |
				IllegalArgumentException | InvocationTargetException e ) {
			log.error( e, e );
		}

		if ( !oldlayout.getClass().equals( layklass ) ) {
			firePropertyChange( LAYOUT_CHANGED, oldlayout, newlayout );
		}
	}

	public void incrementFont( float incr ) {
		//if no vertices or edges are selected, perform action on all vertices and edges
		double delta = ( incr > 0 ? VertexShapeTransformer.STEPSIZE
				: -VertexShapeTransformer.STEPSIZE );
		if ( getPickedVertexState().getPicked().isEmpty()
				&& getPickedEdgeState().getPicked().isEmpty() ) {
			vft.changeSize( (int) incr );
			eft.changeSize( (int) incr );
			vht.changeSize( delta );
		}
		else {
			//otherwise, only perform action on the selected vertices and edges
			vft.changeSize( (int) incr, getPickedVertexState().getPicked() );
			eft.changeSize( (int) incr, getPickedEdgeState().getPicked() );
			vht.changeSize( delta, getPickedVertexState().getPicked() );

		}

		repaint();
	}

	/**
	 * This function differs from {@link GraphDataModel#getGraph() } because only
	 * visible nodes are included
	 *
	 * @return
	 */
	public DirectedGraph<SEMOSSVertex, SEMOSSEdge> getGraph() {
		return (DirectedGraph<SEMOSSVertex, SEMOSSEdge>) visibleFilter.apply( gdm.getGraph() );
	}

	public void setSkeletonMode( boolean skele ) {
		skeletonmode = skele;
		for ( SelectingTransformer<?, ?> s : new SelectingTransformer[]{ vft, vpt, vst,
			vht, est, ept, eft, elt, adpt, aft } ) {
			s.setSkeletonMode( skeletonmode );
		}

		repaint();
	}

	/**
	 * Clears the highlighting and resizes all nodes with a custom size
	 */
	public void clearHighlighting() {
		getPickedEdgeState().clear();
		getPickedVertexState().clear();

		for ( SelectingTransformer<?, ?> s : new SelectingTransformer[]{ vft, vpt, vst,
			vht, est, ept, eft, elt, adpt, aft } ) {
			s.clearSelected();
		}

		eft.clearSizeData();
		vft.clearSizeData();
		vht.clearSizeData();

		repaint();
	}

	/**
	 * Adds the given vertices and edges to the highlighted parts of the graph
	 *
	 * @param verts
	 * @param edges
	 */
	public void highlight( Collection<SEMOSSVertex> verts, Collection<SEMOSSEdge> edges ) {

		if ( !( null == verts || verts.isEmpty() ) ) {
			PickedState<SEMOSSVertex> vpicks = getPickedVertexState();
			for ( SEMOSSVertex v : verts ) {
				vpicks.pick( v, true );
			}
		}

		if ( !( null == edges || edges.isEmpty() ) ) {
			PickedState<SEMOSSEdge> epicks = getPickedEdgeState();
			for ( SEMOSSEdge v : edges ) {
				epicks.pick( v, true );
			}
		}
	}

	public Collection<SEMOSSVertex> getHighlightedVertices() {
		return new HashSet<>( vft.getSelected() );
	}

	public Collection<SEMOSSEdge> getHighlightedEdges() {
		return new HashSet<>( est.getSelected() );
	}

	public LabelTransformer<SEMOSSVertex> getVertexLabelTransformer() {
		return vlt;
	}

	public TooltipTransformer<SEMOSSVertex> getVertexTooltipTransformer() {
		return vtt;
	}

	public LabelTransformer<SEMOSSEdge> getEdgeLabelTransformer() {
		return elt;
	}

	public TooltipTransformer<SEMOSSEdge> getEdgeTooltipTransformer() {
		return ett;
	}

	private void init() {
		setBackground( Color.WHITE );

		vlt.setDefaultDisplayables( Arrays.asList( RDFS.LABEL ) );
		elt.setDefaultDisplayables( new ArrayList<>() );

		RenderContext<SEMOSSVertex, SEMOSSEdge> rc = getRenderContext();
		// FIXME: this edge function seems to be needed since JUNG 2.1
		// However, it seems that graph is always null, so can't use the default impl
		// this call is related to the edgelabelrenderer, y the way
		// rc.setParallelEdgeIndexFunction( DefaultParallelEdgeIndexFunction.getInstance() );
		rc.setParallelEdgeIndexFunction( new EdgeIndexFunction<SEMOSSVertex, SEMOSSEdge>() {
			@Override
			public int getIndex( Graph<SEMOSSVertex, SEMOSSEdge> graph, SEMOSSEdge e ) {
				return 0;
			}

			@Override
			public void reset( Graph<SEMOSSVertex, SEMOSSEdge> graph, SEMOSSEdge e ) {
			}

			@Override
			public void reset() {
			}
		} );
		rc.setVertexLabelTransformer( vlt );
		setVertexToolTipTransformer( vtt );
		rc.setVertexStrokeTransformer( vst );
		rc.setVertexShapeTransformer( vht );
		rc.setVertexFillPaintTransformer( vpt );
		rc.setVertexFontTransformer( vft );

		rc.setEdgeLabelTransformer( elt );
		setEdgeToolTipTransformer( ett );
		rc.setEdgeDrawPaintTransformer( ept );
		rc.setEdgeStrokeTransformer( est );
		rc.setEdgeArrowStrokeTransformer( est );
		rc.setEdgeFontTransformer( eft );
		rc.setArrowDrawPaintTransformer( adpt );
		rc.setArrowFillPaintTransformer( aft );
		getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.S );
		rc.setLabelOffset( 0 );
	}

	public void addPickingSupport() {
		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged( ItemEvent e ) {
				// increase/decrease the size of nodes as they get selected/unselected
				if ( e.getItem() instanceof SEMOSSVertex ) {
					SEMOSSVertex v = SEMOSSVertex.class.cast( e.getItem() );

					log.debug( ( ItemEvent.DESELECTED == e.getStateChange()
							? "Deselecting"
							: "Selecting" ) + " node: " + v );

					double delta = ( ItemEvent.DESELECTED == e.getStateChange()
							? -VertexShapeTransformer.STEPSIZE
							: VertexShapeTransformer.STEPSIZE );
					vht.changeSize( delta, Arrays.asList( v ) );
				}
				else {
					log.debug( ( ItemEvent.DESELECTED == e.getStateChange()
							? "Deselecting"
							: "Selecting" ) + " edge: " + e.getItem() );
				}

				Collection<SEMOSSVertex> nodes = getPickedVertexState().getPicked();
				for ( SelectingTransformer<SEMOSSVertex, ?> s : new SelectingTransformer[]{ vlt, vft, vpt, vht, vst } ) {
					s.setSelected( nodes );
				}

				Collection<SEMOSSEdge> edges = getPickedEdgeState().getPicked();
				for ( SelectingTransformer<SEMOSSEdge, ?> s : new SelectingTransformer[]{ est, eft, elt, adpt, aft } ) {
					s.setSelected( edges );
				}

				SemossGraphVisualization.this.repaint();
			}
		};

		getPickedEdgeState().addItemListener( il );
		getPickedVertexState().addItemListener( il );
	}

	protected class HidingPredicate<V extends GraphElement> implements Predicate<V> {

		@Override
		public boolean apply( V v ) {

			if ( gdm.presentAtLevel( v, overlayLevel ) ) {
				return !( hiddens.contains( v.getURI() ) || hiddens.contains( v.getType() ) );
			}

			return false;
		}
	}
}
