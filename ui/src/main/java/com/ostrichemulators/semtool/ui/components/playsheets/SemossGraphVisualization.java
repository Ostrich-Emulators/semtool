/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.google.common.base.Predicate;
import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
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
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.Color;
import java.awt.Paint;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class SemossGraphVisualization extends VisualizationViewer<SEMOSSVertex, SEMOSSEdge> {

	private static final Logger log = Logger.getLogger( SemossGraphVisualization.class );
	private final List<SemossVisualizationListener> listenees = new ArrayList<>();
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

	private final HidingPredicate<? extends GraphElement> predicate = new HidingPredicate<>();
	private final VertexPredicateFilter<SEMOSSVertex, SEMOSSEdge> visibleFilter
			= new VertexPredicateFilter<>( (HidingPredicate<SEMOSSVertex>) predicate );
	private final Set<URI> hiddens = new HashSet<>();

	public SemossGraphVisualization( GraphDataModel gdm ) {
		super( new FRLayout<>( gdm.getGraph() ) );
		this.gdm = gdm;
		init();
	}

	public void setOverlayLevel( int level ) {
		overlayLevel = level;
		refresh();
	}

	public void setLabelCache( Map<Value, String> map ) {
		LabelTransformer lt[] = { vlt, vtt, elt, ett };
		for ( LabelTransformer l : lt ) {
			l.setLabelCache( map );
		}
	}

	public void hide( URI uri, boolean hideme ) {
		if ( hideme ) {
			hiddens.add( uri );
		}
		else {
			hiddens.remove( uri );
		}

		fireUpdated();
		refresh();
	}

	public void hide( Collection<URI> uris, boolean hideme ) {
		if ( hideme ) {
			hiddens.addAll( uris );
		}
		else {
			hiddens.removeAll( uris );
		}

		fireUpdated();
		refresh();
	}

	public void clearHiddens() {
		hiddens.clear();
		fireUpdated();
	}

	protected void fireUpdated() {
		for ( SemossVisualizationListener l : listenees ) {
			l.nodesUpdated( gdm.getGraph(), this );
		}
	}

	public void addListener( SemossVisualizationListener l ) {
		listenees.add( l );
	}

	public void removeListener( SemossVisualizationListener l ) {
		listenees.remove( l );
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
		Layout<SEMOSSVertex, SEMOSSEdge> currentlayout = super.getGraphLayout();
		ObservableCachingLayout<SEMOSSVertex, SEMOSSEdge> ocl
				= ObservableCachingLayout.class.cast( currentlayout );
		setLayout( (Class<Layout<SEMOSSVertex, SEMOSSEdge>>) ocl.getDelegate().getClass() );
	}

	public void setLayout( Class<Layout<SEMOSSVertex, SEMOSSEdge>> layklass ) {

		try {
			Constructor<? extends Layout<SEMOSSVertex, SEMOSSEdge>> constructor = layklass
					.getConstructor( new Class[]{ Graph.class } );

			Graph<SEMOSSVertex, SEMOSSEdge> graph = visibleFilter.apply( gdm.getGraph() );
			Object o = constructor.newInstance( graph );
			Layout<SEMOSSVertex, SEMOSSEdge> l = (Layout<SEMOSSVertex, SEMOSSEdge>) o;
			l.setInitializer( super.getGraphLayout() );
			l.setSize( getSize() );

			LayoutTransition<SEMOSSVertex, SEMOSSEdge> lt
					= new LayoutTransition<>( this, getGraphLayout(), l );
			Animator animator = new Animator( lt );
			animator.start();
			getRenderContext().getMultiLayerTransformer().setToIdentity();
			repaint();
		}
		catch ( NoSuchMethodException | SecurityException |
				InstantiationException | IllegalAccessException |
				IllegalArgumentException | InvocationTargetException e ) {
			log.error( e, e );
		}
	}

	public void incrementFont( float incr ) {
		//if no vertices or edges are selected, perform action on all vertices and edges
		if ( getPickedVertexState().getPicked().isEmpty()
				&& getPickedEdgeState().getPicked().isEmpty() ) {
			vft.changeSize( (int) incr );
			eft.changeSize( (int) incr );
		}
		else {
			//otherwise, only perform action on the selected vertices and edges
			vft.changeSize( (int) incr, getPickedVertexState().getPicked() );
			eft.changeSize( (int) incr, getPickedEdgeState().getPicked() );
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
		repaint();
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

		repaint();
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

	private void init() {
		setBackground( Color.WHITE );
		//setRenderer( new SemossBasicRenderer() );

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
