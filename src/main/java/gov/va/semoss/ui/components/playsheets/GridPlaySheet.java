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
import java.util.List;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * The GridPlaySheet class creates the panel and table for a grid view of data
 * from a SPARQL query.
 */
public class GridPlaySheet extends GridRAWPlaySheet {

	private static final Logger log = Logger.getLogger(GridPlaySheet.class );

	LabeledPairTableCellRenderer<URI> renderer = new LabeledPairTableCellRenderer<>();

	public GridPlaySheet() {
		super( new ValueTableModel( false ) );
		JTable table = getTable();
		table.setDefaultRenderer( URI.class, renderer );
	}

	@Override
	public void create( List<Value[]> data, List<String> newheaders, IEngine engine ) {
		super.create( convertUrisToLabels( data, engine ), newheaders, engine );
	}

	@Override
	public void overlay( List<Value[]> data, List<String> newheaders, IEngine eng ) {
		super.overlay( convertUrisToLabels( data, eng ), newheaders, eng );
	}
}
