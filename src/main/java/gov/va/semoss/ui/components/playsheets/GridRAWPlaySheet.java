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

import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.actions.SaveAllGridAction;
import gov.va.semoss.ui.actions.SaveGridAction;
import gov.va.semoss.ui.components.LineNumberTableRowHeader;
import gov.va.semoss.ui.components.NewScrollBarUI;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.components.renderers.URIEditor;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
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
	private final SaveGridAction saveGridAction;
	private final SaveAllGridAction saveall;

	public GridRAWPlaySheet() {
		this( new ValueTableModel() );
	}

	public GridRAWPlaySheet( ValueTableModel mod ) {
		model = mod;
		table = new JTable( model );
		setLayout( new BorderLayout() );

		saveGridAction = new SaveGridAction();
		saveall = new SaveAllGridAction();

		final JScrollPane jsp = new JScrollPane( table );
		table.setAutoCreateRowSorter( true );
		table.setDefaultEditor( URI.class, new URIEditor() );

		table.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getInputMap( JComponent.WHEN_FOCUSED ).
				put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ),
						"saveGridAction" );
		table.getActionMap().put( "saveGridAction", saveGridAction );

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
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
		saveGridAction.setDefaultFileName( tabTitle );
		saveGridAction.setTable( table );
		jtb.add( saveGridAction );

		saveall.setPlaySheetFrame( getPlaySheetFrame() );
		jtb.add( saveall );
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		table.setRowHeight( table.getRowHeight() + (int) incr );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders ) {
		setHeaders( newheaders );
		log.debug( "into create: " + data.size() + " items" );
		model.setData( data, newheaders );
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders ) {
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
}
