/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.actions.SaveAllGridAction;
import gov.va.semoss.ui.actions.SaveAsGridAction;
import gov.va.semoss.ui.actions.SaveGridAction;
import gov.va.semoss.ui.components.LineNumberTableRowHeader;
import gov.va.semoss.ui.components.NewScrollBarUI;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.components.renderers.URIEditor;

import gov.va.semoss.util.MultiMap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 */
public class GridRAWPlaySheet extends PlaySheetCentralComponent {

	private static final long serialVersionUID = -8685007953734205297L;
	private static final Logger log = Logger.getLogger( GridPlaySheet.class );
	private final ValueTableModel model;
	private final JTable table;
	private final SaveGridAction save = new SaveGridAction( false );
	private final SaveAsGridAction saveas = new SaveAsGridAction( true );
	private final SaveAllGridAction saveall = new SaveAllGridAction();
	private final JLabel searchlabel = new JLabel( "Search" );
	private final JTextField searchfield = new JTextField();
	private final HighlightingRenderer renderer = new HighlightingRenderer();

	public GridRAWPlaySheet() {
		this( new ValueTableModel() );
	}

	public GridRAWPlaySheet( ValueTableModel mod ) {
		model = mod;
		table = new JTable( model );
		setLayout( new BorderLayout() );

		final JScrollPane jsp = new JScrollPane( table );
		table.setAutoCreateRowSorter( true );
		table.setDefaultEditor( URI.class, new URIEditor() );

		table.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getInputMap( JComponent.WHEN_FOCUSED ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getActionMap().put( "saveGridAction", save );

		table.setCellSelectionEnabled( true );

		jsp.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		jsp.setAutoscrolls( true );

		LineNumberTableRowHeader tableLineNumber
				= new LineNumberTableRowHeader( jsp, table );
		JTableHeader header = table.getTableHeader();
		tableLineNumber.setBackground( header.getBackground() );
		jsp.setRowHeaderView( tableLineNumber );

		add( jsp, BorderLayout.CENTER );

		JLabel btn = new JLabel( DbAction.getIcon( "selectall" ), JLabel.TRAILING );
		btn.setBorder( new EmptyBorder( 0, 0, 0, 5 ) );

		jsp.setCorner( JScrollPane.UPPER_LEFT_CORNER, btn );
		btn.setToolTipText( "Select All" );
		btn.addMouseListener( new MouseAdapter() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				table.selectAll();
				ActionEvent ae
						= new ActionEvent( table, ActionEvent.ACTION_PERFORMED, "copy" );
				table.getActionMap().get( "copy" ).actionPerformed( ae );
			}
		} );

		model.addPropertyChangeListener( ValueTableModel.NEEDS_SAVE,
				new PropertyChangeListener() {

					@Override
					public void propertyChange( PropertyChangeEvent evt ) {
						boolean show = Boolean.parseBoolean( evt.getNewValue().toString() );
						GridRAWPlaySheet.this.getPlaySheetFrame().showSaveMnemonic( show );
					}
				} );

		searchfield.setPreferredSize( new Dimension( 50, 20 ) );
		Border b1 = BorderFactory.createLineBorder( Color.DARK_GRAY, 1 );
		Border b2 = BorderFactory.createEmptyBorder( 0, 3, 0, 3 );
		searchfield.setBorder( BorderFactory.createCompoundBorder( b1, b2 ) );

		searchfield.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				search( searchfield.getText() );
			}
		} );
	}

	protected void setupToolBarButtons( final String tabTitle ) {
		save.setDefaultFileName( tabTitle );
		save.setTable( model );
		saveas.setDefaultFileName( tabTitle );
		saveas.setTable( model );
	}

	private void search( String ltxt ) {
		log.debug( "searching for: " + ltxt );
		String txt = ltxt.toUpperCase();
		renderer.clear();

		for ( int r = 0; r < table.getRowCount(); r++ ) {
			for ( int c = 0; c < table.getColumnCount(); c++ ) {
				Object val = table.getValueAt( r, c );
				if ( null != val ) {
					if ( val.toString().toUpperCase().contains( txt ) ) {
						log.debug( "found " + txt + " at (" + r + "," + c + ") {" + val + "}" );
						renderer.highlight( r, c );
						model.fireTableCellUpdated( r, c );
					}
				}
			}
		}
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
		setupToolBarButtons( tabTitle );
		jtb.add( save );

		saveall.setPlaySheetFrame( getPlaySheetFrame() );
		jtb.add( saveall );

		jtb.add( searchlabel );
		jtb.add( searchfield );
	}

	@Override
	public boolean hasChanges() {
		return model.needsSave();
	}

	@Override
	public Map<String, Action> getActions() {
		Map<String, Action> map = super.getActions();
		map.put( PlaySheetFrame.SAVE, save );
		map.put( PlaySheetFrame.SAVE_ALL, saveall );
		map.put( PlaySheetFrame.SAVE_AS, saveas );
		return map;
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		table.setRowHeight( table.getRowHeight() + (int) incr );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		setHeaders( newheaders );
		log.debug( "into create: " + data.size() + " items" );
		model.setData( data, newheaders );

		TableColumnModel tm = table.getColumnModel();
		for ( int c = 0; c < tm.getColumnCount(); c++ ) {
			tm.getColumn( c ).setCellRenderer( renderer );
		}
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		log.debug( "into overlay: " + data.size() + " items" );
		model.addData( data );
	}

	@Override
	public void run() {
	}

	protected JTable getTable() {
		return table;
	}

	protected ValueTableModel getModel() {
		return model;
	}

	@Override
	public List<Object[]> getTabularData() {
		List<Object[]> data = new ArrayList<>();

		final int cols = model.getColumnCount();
		final int rows = model.getRowCount();

		for ( int r = 0; r < rows; r++ ) {
			Object[] row = new Object[cols];

			for ( int c = 0; c < cols; c++ ) {
				row[c] = model.getValueAt( r, c );
			}

			data.add( row );
		}

		return data;
	}

	@Override
	public boolean prefersTabs() {
		return true;
	}

	private class HighlightingRenderer extends DefaultTableCellRenderer {

		private final MultiMap<Integer, Integer> highlights = new MultiMap<>();

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column ) {
			super.getTableCellRendererComponent( table, value, isSelected,
					hasFocus, row, column );
			setOpaque( true );

			setBackground( table.getBackground() );
			setForeground( table.getForeground() );
			if ( highlights.containsKey( row ) ) {
				if ( highlights.get( row ).contains( column ) ) {
					setBackground( table.getSelectionBackground() );
					setForeground( table.getSelectionForeground() );
				}
			}

			return this;
		}

		public void clear() {
			highlights.clear();
		}

		public void highlight( int row, int col ) {
			highlights.add( row, col );
		}
	}
}
