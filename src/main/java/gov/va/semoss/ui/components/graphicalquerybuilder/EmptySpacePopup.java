/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.util.Pair;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.rdf.engine.util.TheAwesomeClass;
import gov.va.semoss.ui.components.SaveAsInsightPanel;
import gov.va.semoss.ui.components.graphicalquerybuilder.SparqlResultTableModel.RowLocator;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class EmptySpacePopup<T extends AbstractNodeEdgeBase> extends JPopupMenu {

	private static final Logger log = Logger.getLogger( EmptySpacePopup.class );

	public EmptySpacePopup( GraphicalQueryPanel pnl ) {
		add( new AbstractAction( "Manage Constraints" ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				JPanel jpnl = new JPanel( new BorderLayout() );

				List<URI> concepts = TheAwesomeClass.instance().createConceptList( pnl.getEngine() );
				Map<URI, String> conceptmap
						= Utility.sortUrisByLabel( Utility.getInstanceLabels( concepts, pnl.getEngine() ) );
				final ValueEditor types = new ValueEditor();
				final ValueEditor normal = new ValueEditor();

				List<QueryNodeEdgeBase> elements = new ArrayList<>( pnl.getGraph().getVertices() );
				elements.addAll( pnl.getGraph().getEdges() );

				SparqlResultTableModel model = new SparqlResultTableModel( elements );

				final JTable tbl = new JTable( model ) {

					@Override
					public TableCellEditor getCellEditor( int row, int column ) {
						if ( 2 == column ) {
							ValueEditor editor;
							RowLocator src = model.getRawRow( row );
							if ( RDF.TYPE.equals( src.property ) ) {

								// FIXME: need to figure out if we're a concept or an edge
								boolean isconcept = false;
								for ( QueryNode v : pnl.getGraph().getVertices() ) {
									if ( src.base.equals( v ) ) {
										isconcept = true;
									}
								}

								if ( isconcept ) {
									types.setChoices( conceptmap );
								}
								else {
									// we have an edge, so figure out the endpoints
									Pair<QueryNode> verts
											= pnl.getGraph().getEndpoints( QueryEdge.class.cast( src.base ) );
									URI starttype = verts.getFirst().getType();
									URI endtype = verts.getSecond().getType();

									List<URI> links = TheAwesomeClass.instance().getPredicatesBetween( starttype,
											endtype, pnl.getEngine() );
									Map<URI, String> labels = Utility.getInstanceLabels( links, pnl.getEngine() );
									labels.put( Constants.ANYNODE, "<Any>" );
									types.setChoices( Utility.sortUrisByLabel( labels ) );
								}

								editor = types;
							}
							else {
								editor = normal;
							}

							editor.setNode( src.base );
							editor.setType( src.property );
							editor.setChecked( src.base.isSelected( src.property ) );
							return editor;
						}
						else {
							return super.getCellEditor( row, column );
						}
					}
				};

				LabeledPairTableCellRenderer renderer
						= LabeledPairTableCellRenderer.getUriPairRenderer();
				Set<URI> labels = getAllProperties( elements );
				renderer.cache( Utility.getInstanceLabels( labels, pnl.getEngine() ) );

				LabeledPairTableCellRenderer trenderer
						= LabeledPairTableCellRenderer.getValuePairRenderer( pnl.getEngine() );
				trenderer.cache( Constants.ANYNODE, "<Any>" );

				tbl.setAutoCreateRowSorter( true );
				tbl.setFillsViewportHeight( true );

				tbl.setDefaultRenderer( URI.class, renderer );
				tbl.setDefaultRenderer( Value.class, trenderer );

				jpnl.add( new JScrollPane( tbl ) );

				JOptionPane.showConfirmDialog( JOptionPane.getFrameForComponent( pnl ),
						jpnl, "Query Results Config", JOptionPane.PLAIN_MESSAGE );
				pnl.update();
			}
		} );

		add( new AbstractAction( "Save as Insight" ) {
			@Override
			public void actionPerformed( ActionEvent e ) {
				SaveAsInsightPanel.showDialog( JOptionPane.getFrameForComponent( pnl ),
						pnl.getEngine(), pnl.getQuery() );

			}
		} );

		addSeparator();
		add( new AbstractAction( "Clear Query" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( null,
						"Really clear the query?",
						"Clear the Graph", JOptionPane.YES_NO_OPTION ) ) {
					pnl.clear();
				}
			}
		} );
	}

	private static Set<URI> getAllProperties( Collection<QueryNodeEdgeBase> data ) {
		Set<URI> props = new HashSet<>();
		for ( QueryNodeEdgeBase b : data ) {
			props.addAll( b.getAllValues().keySet() );
		}
		return props;
	}
}
