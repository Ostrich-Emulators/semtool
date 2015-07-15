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

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.models.PropertyEditorTableModel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;

/**
 */
public class PropertyEditorPlaySheet extends PlaySheetCentralComponent {
	private static final long serialVersionUID = -8685007953734205297L;
	private static final Logger log = Logger.getLogger( GridPlaySheet.class );
	
	private final PropertyEditorTableModel model;
	private final JTable table;

	public PropertyEditorPlaySheet(AbstractNodeEdgeBase _nodeOrEdge) {
		this( new PropertyEditorTableModel(_nodeOrEdge) );
	}

	public PropertyEditorPlaySheet( PropertyEditorTableModel mod ) {
		setLayout( new BorderLayout() );
		
		model = mod;
		table = new JTable( model );
		
		table.setAutoCreateRowSorter( true );
		table.setCellSelectionEnabled( true );

		final JScrollPane jsp = new JScrollPane( table );
		jsp.setAutoscrolls( true );
		add( jsp, BorderLayout.CENTER );
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {}

	@Override
	public boolean hasChanges() {
		//not yet implemented
		return false;
	}

	@Override
	public Map<String, Action> getActions() {
		return super.getActions();
	}

	@Override
	public void incrementFont( float incr ) {
		super.incrementFont( incr );
		table.setRowHeight( table.getRowHeight() + (int) incr );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		model.setData( data, null );
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		log.debug( "overlay not implemented for PropoertEditorPlaysheet" );
	}

	@Override
	public void run() {
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