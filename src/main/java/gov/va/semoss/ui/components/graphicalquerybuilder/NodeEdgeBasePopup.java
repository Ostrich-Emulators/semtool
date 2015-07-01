/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.Graph;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.util.Utility;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public abstract class NodeEdgeBasePopup<T extends AbstractNodeEdgeBase> extends JPopupMenu {

	private static final Logger log = Logger.getLogger( NodeEdgeBasePopup.class );

	public NodeEdgeBasePopup( T v, GraphicalQueryBuilderPanel pnl ) {
		add( new OneVariableDialogItem( v, pnl, RDFS.LABEL, "Set Instance Label",
				"Set the label of this node", "Instance Label" ) );
		add( makeTypeItem( v, pnl ) );
	}

	protected abstract Action makeTypeItem( T v, GraphicalQueryBuilderPanel pnl );

	public static NodeEdgeBasePopup<SEMOSSVertex> forVertex( SEMOSSVertex v,
			GraphicalQueryBuilderPanel pnl ) {
		return new NodeEdgeBasePopup<SEMOSSVertex>( v, pnl ) {

			@Override
			protected Action makeTypeItem( SEMOSSVertex v, GraphicalQueryBuilderPanel pnl ) {
				Map<URI, String> labels = Utility.getInstanceLabels(
						DBToLoadingSheetExporter.createConceptList( pnl.getEngine() ), pnl.getEngine() );

				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Vertex", "New Type", Utility.sortUrisByLabel( labels ) );

			}
		};
	}

	public static NodeEdgeBasePopup<SEMOSSEdge> forEdge( SEMOSSEdge v,
			GraphicalQueryBuilderPanel pnl ) {
		return new NodeEdgeBasePopup<SEMOSSEdge>( v, pnl ) {

			@Override
			protected Action makeTypeItem( SEMOSSEdge v, GraphicalQueryBuilderPanel pnl ) {
				Graph<SEMOSSVertex, SEMOSSEdge> graph
						= pnl.getViewer().getGraphLayout().getGraph();

				URI starttype = graph.getSource( v ).getType();
				URI endtype = graph.getDest( v ).getType();

				List<URI> links = DBToLoadingSheetExporter.getPredicatesBetween( starttype,
						endtype, pnl.getEngine() );

				Map<URI, String> labels = Utility.getInstanceLabels( links, pnl.getEngine() );
				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Edge", "New Type", Utility.sortUrisByLabel( labels ) );
			}
		};
	}
}
