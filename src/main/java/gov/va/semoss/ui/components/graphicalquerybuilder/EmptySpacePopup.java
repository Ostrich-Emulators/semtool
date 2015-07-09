/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.SaveAsInsightPanel;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.Utility;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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
				MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> map
						= pnl.getSparqlConfigs();
				JTable tbl = new JTable( new SparqlResultTableModel( map ) );
				LabeledPairTableCellRenderer renderer
						= LabeledPairTableCellRenderer.getUriPairRenderer();
				Set<URI> labels = SparqlResultConfig.getProperties( map );
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
}
