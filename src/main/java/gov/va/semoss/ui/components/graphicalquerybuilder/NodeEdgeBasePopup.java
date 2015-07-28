/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.Graph;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.ui.components.renderers.LabeledPairRenderer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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
public abstract class NodeEdgeBasePopup<T extends QueryNodeEdgeBase> extends JPopupMenu {

	private static final Logger log = Logger.getLogger( NodeEdgeBasePopup.class );

	public NodeEdgeBasePopup( T v, GraphicalQueryPanel pnl ) {
		add( new OneVariableDialogItem( v, pnl, RDFS.LABEL, "Set Instance Label",
				"Set the label of this node", "Instance Label" ) );
		add( makeTypeItem( v, pnl ) );

		add( new AbstractAction( "Change Query ID" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				String oldid = v.getQueryId();
				String newval = JOptionPane.showInputDialog( pnl, "New Query ID", oldid );
				v.setQueryId( newval );
				pnl.update();
			}
		} );

		JCheckBoxMenuItem selectMe = new JCheckBoxMenuItem( "Return this Entity",
				v.isSelected( RDF.SUBJECT ) );
		add( selectMe );

		selectMe.addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged( ItemEvent e ) {
				v.setSelected( RDF.SUBJECT, selectMe.isSelected() );
				pnl.update();
			}
		} );

		finishMenu( v, pnl );

		addSeparator();
		add( new AbstractAction( "Remove this Element" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				pnl.remove( v );
			}

		} );

		add( new AbstractAction( "Clear Graph" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				pnl.clear();
			}
		} );

	}

	protected void finishMenu( T v, GraphicalQueryPanel pnl ) {
	}

	protected static Collection<URI> getAllPossibleProperties( URI type, IEngine engine ) {
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

	protected void addConstraintRemover( T v, GraphicalQueryPanel pnl ) {
		ConstraintRemover cr = new ConstraintRemover( v, pnl );
		add( cr );

		Set<URI> myprops = v.getValues().keySet();
		myprops.removeAll( Arrays.asList( RDF.SUBJECT, RDF.TYPE ) );
		cr.setEnabled( !myprops.isEmpty() );
	}

	public static NodeEdgeBasePopup<QueryNode> forVertex( QueryNode v,
			GraphicalQueryPanel pnl ) {
		return new NodeEdgeBasePopup<QueryNode>( v, pnl ) {

			@Override
			protected Action makeTypeItem( QueryNode v, GraphicalQueryPanel pnl ) {
				Map<URI, String> labels = Utility.getInstanceLabels(
						DBToLoadingSheetExporter.createConceptList( pnl.getEngine() ), pnl.getEngine() );
				labels.put( Constants.ANYNODE, "<Any>" );
				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Vertex", "New Type", Utility.sortUrisByLabel( labels ) );

			}

			@Override
			protected void finishMenu( QueryNode v, GraphicalQueryPanel pnl ) {
				Collection<URI> props
						= getAllPossibleProperties( v.getType(), pnl.getEngine() );

				Map<URI, String> propmap = Utility.getInstanceLabels( props, pnl.getEngine() );
				propmap.put( Constants.ANYNODE, "<Any>" );
				add( new OneVariableDialogItem( v, pnl, null, "Add Constraint",
						"Add a constraint to this Vertex", "New Value", propmap ) );
				addConstraintRemover( v, pnl );
			}
		};
	}

	public static NodeEdgeBasePopup<QueryEdge> forEdge( QueryEdge v,
			GraphicalQueryPanel pnl ) {
		return new NodeEdgeBasePopup<QueryEdge>( v, pnl ) {

			@Override
			protected Action makeTypeItem( QueryEdge v, GraphicalQueryPanel pnl ) {
				Graph<QueryNode, QueryEdge> graph
						= pnl.getViewer().getGraphLayout().getGraph();

				URI starttype = graph.getSource( v ).getType();
				URI endtype = graph.getDest( v ).getType();

				ListQueryAdapter<URI> links
						= DBToLoadingSheetExporter.getPredicatesBetween( starttype, endtype );

				return new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
						"Change the type of this Edge", "New Type", links );
			}

			@Override
			protected void finishMenu( QueryEdge v, GraphicalQueryPanel pnl ) {
				String query = "SELECT DISTINCT ?pred WHERE { ?s ?pred ?o . ?s rdf:predicate ?type . FILTER ( isLiteral( ?o ) ) }";
				ListQueryAdapter<URI> qa = OneVarListQueryAdapter.getUriList( query, "pred" );
				URI type = v.getType();
				qa.bind( "type", type );

				add( new OneVariableDialogItem( v, pnl, null, "Add Constraint",
						"Add a constraint to this Edge", "New Value", qa ) );
				addConstraintRemover( v, pnl );
			}

		};
	}

	private static class ConstraintRemover extends AbstractAction {

		QueryNodeEdgeBase nodeedge;
		GraphicalQueryPanel pnl;

		public ConstraintRemover( QueryNodeEdgeBase ne, GraphicalQueryPanel p ) {
			super( "Remove Constraint(s)" );
			nodeedge = ne;
			pnl = p;
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			Set<URI> props = new HashSet<>( nodeedge.getAllValues().keySet() );
			props.remove( RDF.SUBJECT );
			props.remove( RDF.TYPE );
			JList<URI> cons = new JList<>( props.toArray( new URI[0] ) );
			LabeledPairRenderer<URI> renderer = LabeledPairRenderer.getUriPairRenderer();
			renderer.cache( Utility.getInstanceLabels( props, pnl.getEngine() ) );
			cons.setCellRenderer( renderer );

			String[] choices = { "OK", "Cancel" };
			int ans = JOptionPane.showOptionDialog( null, new JScrollPane( cons ),
					"Constraints to Remove", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, choices, choices[0] );
			if ( 0 == ans ) {
				for ( URI u : cons.getSelectedValuesList() ) {
					nodeedge.removeProperty( u );
				}
				pnl.update();
			}
		}
	};
}
