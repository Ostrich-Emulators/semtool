/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.models.VertexFilterTableModel;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTableCellRenderer;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import java.awt.Dimension;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class VisualizationControlPanel extends JTabbedPane implements GraphListener {

	private static final Logger log = Logger.getLogger( VisualizationControlPanel.class );
	private final JTable nodes;
	private final JTable edges;
	private final VertexFilterTableModel vertexmodel = new VertexFilterTableModel( "Node Type" );
	private final VertexFilterTableModel edgemodel = new VertexFilterTableModel( "Edge Type" );

	public VisualizationControlPanel() {
		nodes = new JTable( vertexmodel );
		edges = new JTable( edgemodel );

		super.add( "Node Filter", new JScrollPane( nodes ) );
		super.add( "Edge Filter", new JScrollPane( edges ) );
		super.setPreferredSize( new Dimension( 250, 400 ) );
	}

	public VisualizationControlPanel( RetrievingLabelCache cacher ) {
		this();

		LabeledPairTableCellRenderer<Value> renderer
				= LabeledPairTableCellRenderer.getValuePairRenderer( cacher );
		nodes.setDefaultRenderer( URI.class, renderer );
		edges.setDefaultRenderer( URI.class, renderer );
	}

	public void setLabelCache( RetrievingLabelCache cacher ) {
		LabeledPairTableCellRenderer<Value> renderer
				= LabeledPairTableCellRenderer.getValuePairRenderer( cacher );
		nodes.setDefaultRenderer( URI.class, renderer );
		edges.setDefaultRenderer( URI.class, renderer );
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		vertexmodel.refresh( graph.getVertices(), gps.getView() );
		edgemodel.refresh( graph.getEdges(), gps.getView() );
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout, GraphPlaySheet gps ) {
		// don't care when the layout changes
	}
}
