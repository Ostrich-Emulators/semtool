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
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;

import gov.va.semoss.util.MultiSetMap;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * The GridPlaySheet class creates the panel and table for a grid view of data
 * from a SPARQL query.
 */
public class GridPlaySheet extends GridRAWPlaySheet {

	private static final long serialVersionUID = 983237700844677993L;

	@SuppressWarnings( "unused" )
	private static final Logger log = Logger.getLogger( GridPlaySheet.class );

	private final LabeledPairTableCellRenderer<URI> renderer = new LabeledPairTableCellRenderer<>();
	private final MultiSetMap<Integer, Integer> urilocations = new MultiSetMap<>();
	private final Map<RowCol, Resource> uris = new HashMap<>();

	public GridPlaySheet() {
		super( new ValueTableModel( false ) );
		JTable table = getTable();
		table.setDefaultRenderer( URI.class, renderer );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		uris.clear();
		urilocations.clear();
		markResources( data );
		super.create( convertUrisToLabels( data, engine ), newheaders, engine );
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		markResources( data );
		super.overlay( convertUrisToLabels( data, eng ), newheaders, eng );
	}

	private void markResources( List<Value[]> data ) {
		final int startrow = getTable().getRowCount();

		int row = -1;
		for ( Value[] rowdata : data ) {
			row++;
			int col = -1;
			for ( Value val : rowdata ) {
				col++;

				if ( val instanceof Resource ) {
					urilocations.add( startrow + row, col );
					uris.put( new RowCol( startrow + row, col ),
							Resource.class.cast( val ) );
				}
			}
		}
	}

	@Override
	protected Resource asResource( int row, int col ) {
		return uris.get( new RowCol( row, col ) );
	}

	private static class RowCol {

		public final int row;
		private final int col;

		public RowCol( int row, int col ) {
			this.row = row;
			this.col = col;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 61 * hash + this.row;
			hash = 61 * hash + this.col;
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final RowCol other = (RowCol) obj;
			if ( this.row != other.row ) {
				return false;
			}
			if ( this.col != other.col ) {
				return false;
			}
			return true;
		}

	}
}
