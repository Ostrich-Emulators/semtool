/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.ui.actions.DbAction;
import com.ostrichemulators.semtool.ui.components.models.LoadingSheetModel;
import com.ostrichemulators.semtool.ui.components.models.ValueTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public abstract class LoadingPlaySheetBase extends GridRAWPlaySheet implements ActionListener {

	private static final Logger log = Logger.getLogger( LoadingPlaySheetBase.class );
	private final JLabel errorLabel = new JLabel();
	private final EditHeaderAction editheaders = new EditHeaderAction();
	private final ConformanceRenderer renderer = new ConformanceRenderer();
	private final ConformanceRowFilter filter = new ConformanceRowFilter();

	protected LoadingPlaySheetBase( LoadingSheetModel mod ) {
		super( mod );
		errorLabel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );

		JTable tbl = getTable();

		tbl.getColumnModel().getColumn( 0 ).setCellRenderer( renderer );
		if ( mod.isRel() ) {
			tbl.getColumnModel().getColumn( 1 ).setCellRenderer( renderer );
		}

		ValueTableModel m = getModel();
		TableRowSorter<ValueTableModel> sorter = new TableRowSorter<>( m );
		sorter.setRowFilter( filter );
		tbl.setRowSorter( sorter );

		tbl.getModel().addTableModelListener( new TableModelListener() {

			@Override
			public void tableChanged( TableModelEvent e ) {
				// not sure why we need to reset the renderer so much
				TableColumnModel tcm = tbl.getColumnModel();
				tcm.getColumn( 0 ).setCellRenderer( renderer );
				if ( LoadingPlaySheetBase.this.getLoadingModel().isRel() ) {
					tcm.getColumn( 1 ).setCellRenderer( renderer );
				}

				setErrorLabel();
			}
		} );
	}

	@Override
	public void setHeaders( List<String> newheads ) {
		super.setHeaders( newheads );
		getLoadingModel().setHeaders( newheads );

		TableColumnModel tcm = getTable().getColumnModel();
		ModelHeaderRenderer mhr = new ModelHeaderRenderer( getTable() );
		for ( int col = 0; col < tcm.getColumnCount(); col++ ) {
			tcm.getColumn( col ).setHeaderRenderer( mhr );
		}
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		AbstractButton btn = AbstractButton.class.cast( ae.getSource() );
		filter.setFiltering( btn.isSelected() );
		getModel().fireTableDataChanged();
	}

	/**
	 * Is this data ready to be loaded?
	 *
	 * @return
	 */
	public abstract boolean okToLoad();

	/**
	 * Gives the user a chance to correct any errors that caused
	 * {@link #okToLoad()} to return false
	 *
	 * @return true, if the bad data has been corrected, false if the loading
	 * process should be canceled
	 */
	public abstract boolean correct();

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
		setupToolBarButtons( tabTitle );
		jtb.add( errorLabel );
		jtb.add( editheaders );
		setErrorLabel();
	}

	public LoadingSheetModel getLoadingModel() {
		return LoadingSheetModel.class.cast( getModel() );
	}

	protected void setErrorLabel() {
		StringBuilder msg = new StringBuilder();

		LoadingSheetModel model = getLoadingModel();
		boolean conf = model.hasConformanceErrors();
		boolean mod = model.hasModelErrors();
		int cerrs = model.getConformanceErrorCount();
		int merrs = model.getModelErrorColumns().size();

		if ( conf && mod ) {
			msg.append( cerrs ).append( " QA Error" );
			if ( 1 != cerrs ) {
				msg.append( "s" );
			}
			msg.append( ", " ).append( merrs ).append( " Model Error" );
			if ( 1 != merrs ) {
				msg.append( "s" );
			}
			msg.append( " Found" );
		}
		else if ( conf ) {
			msg.append( Integer.toString( cerrs ) ).append( " QA Error" );
			if ( 1 != cerrs ) {
				msg.append( "s" );
			}
			msg.append( " Found" );
		}
		else if ( mod ) {
			msg.append( Integer.toString( merrs ) ).append( " Model Error" );
			if ( 1 != merrs ) {
				msg.append( "s" );
			}
			msg.append( " Found" );
		}
		else {
			msg.append( "No Errors Found" );
		}

		errorLabel.setText( msg.toString() );
		errorLabel.setOpaque( true );
		errorLabel.setBackground( conf || mod ? Color.PINK : this.getBackground() );

		errorLabel.setVisible( getLoadingModel().isRealTimeChecking() );
		errorLabel.repaint();
	}

	protected class ConformanceRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int fakerow, int column ) {
			super.getTableCellRendererComponent( table, value, isSelected,
					hasFocus, fakerow, column );

			if ( !isSelected ) {
				int row = table.getRowSorter().convertRowIndexToModel( fakerow );
				LoadingSheetModel lsm = LoadingSheetModel.class.cast( table.getModel() );
				LoadingSheetData.LoadingNodeAndPropertyValues nap = lsm.getNap( row );

				if ( null == nap ) {
					super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
							fakerow, column );
					setBackground( table.getBackground() );
					setToolTipText( "Add a new row to this table" );
					return this;
				}
				else {
					setBackground( table.getBackground() );
					setToolTipText( "This endpoint is valid" );

					if ( nap.hasError() ) {
						// we have an error, but don't know if it's in the current column					
						if ( ( 0 == column && nap.isSubjectError() )
								|| ( 1 == column && nap.isObjectError() ) ) {
							setBackground( Color.PINK );
							String type = getHeaders().get( column );
							setToolTipText( "There is no existing " + type + " named " + value );
						}
					}
				}
			}
			return this;
		}
	}

	protected class ConformanceRowFilter extends RowFilter<ValueTableModel, Integer> {

		private boolean filtering = false;

		public void setFiltering( boolean filter ) {
			filtering = filter;
		}

		@Override
		public boolean include( RowFilter.Entry<? extends ValueTableModel, ? extends Integer> entry ) {
			if ( filtering ) {
				LoadingSheetModel lsm = LoadingSheetModel.class.cast( entry.getModel() );
				LoadingSheetData.LoadingNodeAndPropertyValues nap
						= lsm.getNap( entry.getIdentifier() );
				return ( null == nap ? true : nap.hasError() );
			}
			return true;
		}
	}

	protected class ModelHeaderRenderer implements TableCellRenderer {

		private final TableCellRenderer orig;

		public ModelHeaderRenderer( JTable tbl ) {
			orig = tbl.getTableHeader().getDefaultRenderer();
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int fakerow, int column ) {
			Component c = orig.getTableCellRendererComponent( table, value, isSelected,
					hasFocus, fakerow, column );

			if ( c instanceof JLabel ) {
				JLabel lbl = JLabel.class.cast( c );
				LoadingSheetModel model = getLoadingModel();
				if ( model.getModelErrorColumns().contains( column ) ) {
					lbl.setIcon( DbAction.getIcon( "error" ) );

					if ( 0 == column || ( 1 == column && model.isRel() ) ) {
						lbl.setToolTipText( "There is no node type named " + value );
					}
					else {
						lbl.setToolTipText( "There is no property type named " + value );
					}
				}
				else {
					lbl.setIcon( null );
					lbl.setToolTipText( null );
				}
			}

			return c;
		}
	}

	private class EditHeaderAction extends AbstractAction {

		public EditHeaderAction() {
			super( "Edit Headers", DbAction.getIcon( "table" ) );
			putValue( Action.SHORT_DESCRIPTION, "Edit Column Headers" );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			TableColumnModel tcm = getTable().getColumnModel();
			List<Value[]> vals = new ArrayList<>();
			ValueFactory vf = new ValueFactoryImpl();
			for ( int col = 0; col < tcm.getColumnCount(); col++ ) {
				String val = tcm.getColumn( col ).getHeaderValue().toString();
				vals.add( new Value[]{ vf.createLiteral( val ) } );
			}

			JPanel panel = new JPanel( new BorderLayout() );
			ValueTableModel vtm = new ValueTableModel();
			vtm.setData( vals, Arrays.asList( "Column Name" ) );

			JTable list = new JTable( vtm );
			list.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );
			panel.add( list, BorderLayout.CENTER );

			String opts[] = { "Save", "Cancel" };
			int ans = JOptionPane.showOptionDialog( LoadingPlaySheetBase.this,
					new JScrollPane( panel ), "Edit Column Names", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] );
			if ( 0 == ans ) {
				List<String> heads = new ArrayList<>();
				for ( int i = 0; i < vtm.getRowCount(); i++ ) {
					heads.add( vtm.getValueAt( i, 0 ).toString() );
				}
				LoadingPlaySheetBase.this.setHeaders( heads );
			}
		}
	}
}
