/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.models.GraphLabelsTableModel;
import com.ostrichemulators.semtool.ui.components.api.GraphListener;
import com.ostrichemulators.semtool.ui.components.models.NodeEdgePropertyTableModel;
import com.ostrichemulators.semtool.ui.components.models.NodeEdgeFilterTableModel;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTableCellRenderer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 * A panel to hold the metadata and controls about the graph
 *
 * @author ryan
 */
public class VisualizationControlPanel extends JTabbedPane implements GraphListener {

	private static final Logger log = Logger.getLogger( VisualizationControlPanel.class );
	private final JTable nodes;
	private final JTable edges;
	private final JTable nodelabels;
	private final JTable edgelabels;
	private final JTable selecteds;

	private final NodeEdgeFilterTableModel vertexmodel = new NodeEdgeFilterTableModel( "Node Type" );
	private final NodeEdgeFilterTableModel edgemodel = new NodeEdgeFilterTableModel( "Edge Type" );
	private final GraphLabelsTableModel<SEMOSSVertex> vlabelmodel
			= new GraphLabelsTableModel<>( "Node Type", "Property", "Label", "Tooltip" );
	private final GraphLabelsTableModel<SEMOSSEdge> elabelmodel
			= new GraphLabelsTableModel<>( "Edge Type", "Property", "Label", "Tooltip" );
	private final NodeEdgePropertyTableModel propmodel = new NodeEdgePropertyTableModel();

	public VisualizationControlPanel() {
		nodes = new JTable( vertexmodel );
		edges = new JTable( edgemodel );

		nodelabels = new JTable( vlabelmodel );
		edgelabels = new JTable( elabelmodel );

		selecteds = new JTable( propmodel );

		super.add( "Properties", new JScrollPane( selecteds ) );
		super.add( "Node Labels", new JScrollPane( nodelabels ) );
		super.add( "Edge Labels", new JScrollPane( edgelabels ) );
		super.add( "Node Filter", new JScrollPane( nodes ) );
		super.add( "Edge Filter", new JScrollPane( edges ) );
	}

	public VisualizationControlPanel( RetrievingLabelCache cacher ) {
		this();

		setRenderer( cacher );
	}

	public void setVisualization( SemossGraphVisualization vizzy ) {
		propmodel.setVisualization( vizzy );
	}

	private void setRenderer( RetrievingLabelCache cacher ) {
		cacher.put( Constants.ANYNODE, "SELECT ALL" );
		cacher.put( RDF.SUBJECT, "URI" );
		cacher.put( RDFS.LABEL, "Label" );
		cacher.put( RDF.TYPE, "Type" );
		cacher.put( Constants.IN_EDGE_CNT, "Inputs" );
		cacher.put( Constants.OUT_EDGE_CNT, "Outputs" );

		LabeledPairTableCellRenderer<Value> renderer
				= LabeledPairTableCellRenderer.getValuePairRenderer( cacher );
		for ( JTable jt : new JTable[]{ nodes, edges, nodelabels, edgelabels, selecteds } ) {
			jt.setDefaultRenderer( URI.class, renderer );
			jt.setDefaultRenderer( Value.class, renderer );
		}

	}

	public void setLabelCache( RetrievingLabelCache cacher ) {
		setRenderer( cacher );
	}

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		SemossGraphVisualization view = gps.getView();

		vertexmodel.refresh( graph.getVertices(), view );
		edgemodel.refresh( graph.getEdges(), view );

		vlabelmodel.refresh( graph.getVertices(), view );
		vlabelmodel.setLabelers( view.getVertexLabelTransformer(),
				view.getVertexTooltipTransformer() );

		elabelmodel.refresh( graph.getEdges(), view );
		elabelmodel.setLabelers( view.getEdgeLabelTransformer(),
				view.getEdgeTooltipTransformer() );
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout, GraphPlaySheet gps ) {
		// don't care when the layout changes
	}
}
