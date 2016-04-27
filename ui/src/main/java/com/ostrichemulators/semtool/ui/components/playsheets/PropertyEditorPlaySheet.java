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
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.models.PropertyEditorTableModel;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTableCellRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.SimpleValueEditor;
import com.ostrichemulators.semtool.ui.helpers.NodeEdgeNumberedPropertyUtility;
import com.ostrichemulators.semtool.util.Utility;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 */
public class PropertyEditorPlaySheet extends PlaySheetCentralComponent {
	private static final long serialVersionUID = -8685007953734205297L;
	private static final Logger log = Logger.getLogger( GridPlaySheet.class );

	private final PropertyEditorTableModel model;
	private final JTable table;

	public PropertyEditorPlaySheet( Collection<SEMOSSVertex> pickedVertices,
			String title, IEngine engine ) {
		setLayout( new BorderLayout() );
		setTitle( title );	

		model = new PropertyEditorTableModel( pickedVertices, engine );
		table = new JTable( model );

		LabeledPairTableCellRenderer renderer
				= LabeledPairTableCellRenderer.getUriPairRenderer();
		Set<URI> labels = getUrisThatNeedLabels( pickedVertices );
		renderer.cache( Utility.getInstanceLabels( labels, engine ) );
		
		Map<URI, String> displayNames = NodeEdgeNumberedPropertyUtility.getDisplayNameMap();
		for (URI key:displayNames.keySet()) {
			renderer.cache( key, displayNames.get(key) );
		}

		LabeledPairTableCellRenderer trenderer
				= LabeledPairTableCellRenderer.getValuePairRenderer( engine );

		table.setDefaultRenderer( URI.class, renderer );
		table.setDefaultRenderer( Value.class, trenderer );

		table.setDefaultEditor( Value.class, new SimpleValueEditor() );
		
		table.setAutoCreateRowSorter( true );
		table.setCellSelectionEnabled( true );

		final JScrollPane jsp = new JScrollPane( table );
		jsp.setAutoscrolls( true );
		add( jsp, BorderLayout.CENTER );
	}

	private Set<URI> getUrisThatNeedLabels( Collection<SEMOSSVertex> verts ) {
		Set<URI> needs = new HashSet<>();
		for ( SEMOSSVertex v : verts ) {
			for ( Map.Entry<URI, Value> en : v.getValues().entrySet() ) {
				needs.add( en.getKey() );
				if ( en.getValue() instanceof URI ) {
					needs.add( URI.class.cast( en.getValue() ) );
				}
			}
		}

		return needs;
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
	}

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
		model.populateRows();
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		log.debug( "overlay not implemented for PropoertEditorPlaysheet" );
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
