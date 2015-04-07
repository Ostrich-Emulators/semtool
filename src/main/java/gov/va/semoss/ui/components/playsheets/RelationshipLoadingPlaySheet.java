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

import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.RelationshipLoadingSheetData;
import gov.va.semoss.ui.components.models.LoadingSheetModel;
import gov.va.semoss.ui.components.models.ValueTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;

/**
 */
public class RelationshipLoadingPlaySheet extends LoadingPlaySheetBase implements ActionListener {

	private static final Logger log
			= Logger.getLogger( RelationshipLoadingPlaySheet.class );
	private final JTextField relationship = new JTextField();
	private final String defaultRelationship;
	private final ConformanceRenderer renderer = new ConformanceRenderer();
	private final ConformanceRowFilter filter = new ConformanceRowFilter();

	public RelationshipLoadingPlaySheet( String rel, List<Value[]> valdata,
			List<String> headers ) {
		super( new LoadingSheetModel() );
		defaultRelationship = rel;

		if ( headers.size() < 2 ) {
			throw new IllegalArgumentException( "Incomplete headers given" );
		}

		List<String> list = new ArrayList<>( headers );
		String props[] = list.toArray( new String[0] );
		String stype = list.remove( 0 );
		String otype = list.remove( 0 );

		RelationshipLoadingSheetData lsd
				= new RelationshipLoadingSheetData( stype + "-" + rel + "-" + otype,
						stype, otype, rel, list );
		for ( Value[] val : valdata ) {
			LoadingNodeAndPropertyValues nap
					= lsd.add( val[0].stringValue(), val[1].stringValue() );
			for ( int i = 2; i < val.length; i++ ) {
				nap.put( props[i], val[i] );
			}
		}

		getLoadingModel().setLoadingSheetData( lsd );
		init();
	}

	public RelationshipLoadingPlaySheet( LoadingSheetData lsd ) {
		super( new LoadingSheetModel( lsd ) );
		defaultRelationship = lsd.getRelname();
		init();
		setTitle( lsd.getName() );
		setHeaders( lsd.getHeaders() );		
	}

	private void init() {
		JLabel lbl = new JLabel( "Relationship name:" );
		relationship.setText( defaultRelationship );

		JPanel pnl = new JPanel( new BorderLayout() );
		pnl.add( lbl, BorderLayout.WEST );
		pnl.add( relationship, BorderLayout.CENTER );

		add( pnl, BorderLayout.NORTH );

		JTable tbl = getTable();
		tbl.setDefaultRenderer( String.class, renderer );
		TableRowSorter<ValueTableModel> sorter = new TableRowSorter<>( getModel() );
		sorter.setRowFilter( filter );
		tbl.setRowSorter( sorter );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		AbstractButton btn = AbstractButton.class.cast( ae.getSource() );
		filter.setFiltering( btn.isSelected() );
		getModel().fireTableDataChanged();
	}

	@Override
	public boolean okToLoad() {
		return !relationship.getText().isEmpty();
	}

	@Override
	public boolean correct() {
		Object str = JOptionPane.showInputDialog( null, "Please specify a relationship name",
				"Invalid Relationship", JOptionPane.QUESTION_MESSAGE, null, null,
				defaultRelationship );
		if ( !( null == str || str.toString().isEmpty() ) ) {
			relationship.setText( str.toString() );
			return true;
		}
		return false;
	}
}
