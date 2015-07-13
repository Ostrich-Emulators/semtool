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
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public abstract class NodeEdgeBasePopup<T extends AbstractNodeEdgeBase> extends JPopupMenu {
	
	private static final Logger log = Logger.getLogger( NodeEdgeBasePopup.class );
	
	public NodeEdgeBasePopup( T v, GraphicalQueryPanel pnl ) {
		add( new AbstractAction( "Change Query ID" ) {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				SparqlResultConfig src = SparqlResultConfig.getOne( pnl.getSparqlConfigs().get( v ),
						GraphicalQueryPanel.SPARQLNAME );
				if ( null != src ) {
					String oldid = src.getLabel();
					String newval = JOptionPane.showInputDialog( pnl, "New Query ID", oldid );
					src.setLabel( newval );
					v.setProperty( GraphicalQueryPanel.SPARQLNAME, newval );
					pnl.update();
				}
			}
		} );
		
		add( new OneVariableDialogItem( v, pnl, RDFS.LABEL, "Set Instance Label",
				"Set the label of this node", "Instance Label" ) );
		add( makeTypeItem( v, pnl ) );
		
		add( new AbstractAction( "Remove this Element" ) {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				pnl.remove( v );
			}
			
		} );
		
		finishMenu( v, pnl );
		
		addSeparator();
		add( new AbstractAction( "Clear Graph" ) {
			
			@Override
			public void actionPerformed( ActionEvent e ) {
				pnl.clear();
			}
		} );
		
	}
	
	protected void finishMenu( T v, GraphicalQueryPanel pnl ) {
	}
	
	protected Collection<URI> getAllPossibleProperties( URI type, IEngine engine ) {
		String query = "SELECT DISTINCT ?pred WHERE { ?s ?pred ?o . ?s a ?type . FILTER ( isLiteral( ?o ) ) }";
		ListQueryAdapter<URI> qa = OneVarListQueryAdapter.getUriList( query, "pred" );
		qa.bind( "type", type );
		
		List<URI> props = new ArrayList<>();
		props.add( Constants.ANYNODE );
		try {
			props.addAll( engine.query( qa ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
		
		return props;
	}
	
	protected abstract Action makeTypeItem( T v, GraphicalQueryPanel pnl );
	
	public static NodeEdgeBasePopup<SEMOSSVertex> forVertex( SEMOSSVertex v,
			GraphicalQueryPanel pnl ) {
		return new NodeEdgeBasePopup<SEMOSSVertex>( v, pnl ) {
			
			@Override
			protected Action makeTypeItem( SEMOSSVertex v, GraphicalQueryPanel pnl ) {
				Map<URI, String> labels = Utility.getInstanceLabels(
						DBToLoadingSheetExporter.createConceptList( pnl.getEngine() ), pnl.getEngine() );
				labels.put( Constants.ANYNODE, "<Any>" );
				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Vertex", "New Type", Utility.sortUrisByLabel( labels ) );
				
			}
			
			@Override
			protected void finishMenu( SEMOSSVertex v, GraphicalQueryPanel pnl ) {
				Collection<URI> props
						= getAllPossibleProperties( v.getType(), pnl.getEngine() );
				
				Map<URI, String> propmap = Utility.getInstanceLabels( props, pnl.getEngine() );
				propmap.put( Constants.ANYNODE, "<Any>" );
				add( new OneVariableDialogItem( v, pnl, null, "Add Constraint",
						"Add a constraint to this Vertex", "New Value", propmap ) );
				
				addSeparator();
				
				JCheckBoxMenuItem selectMe = new JCheckBoxMenuItem( "Return this Entity",
						v.isMarked( RDF.SUBJECT ) );
				add( selectMe );
				
				selectMe.addItemListener( new ItemListener() {
					
					@Override
					public void itemStateChanged( ItemEvent e ) {
						v.mark( RDF.SUBJECT, selectMe.isSelected() );
						v.mark( GraphicalQueryPanel.SPARQLNAME, selectMe.isSelected() );
						pnl.update();
					}
				} );
			}
		};
	}
	
	public static NodeEdgeBasePopup<SEMOSSEdge> forEdge( SEMOSSEdge v,
			GraphicalQueryPanel pnl ) {
		return new NodeEdgeBasePopup<SEMOSSEdge>( v, pnl ) {
			
			@Override
			protected Action makeTypeItem( SEMOSSEdge v, GraphicalQueryPanel pnl ) {
				Graph<SEMOSSVertex, SEMOSSEdge> graph
						= pnl.getViewer().getGraphLayout().getGraph();
				
				URI starttype = graph.getSource( v ).getType();
				URI endtype = graph.getDest( v ).getType();
				
				List<URI> links = DBToLoadingSheetExporter.getPredicatesBetween( starttype,
						endtype, pnl.getEngine() );
				
				Map<URI, String> labels = Utility.getInstanceLabels( links, pnl.getEngine() );
				labels.put( Constants.ANYNODE, "<Any>" );
				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Edge", "New Type", Utility.sortUrisByLabel( labels ) );
			}
		};
	}
}
