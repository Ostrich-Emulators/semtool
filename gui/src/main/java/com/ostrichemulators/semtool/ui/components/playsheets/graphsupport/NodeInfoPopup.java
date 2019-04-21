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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridPlaySheet;
import com.ostrichemulators.semtool.util.MultiMap;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 * This class is used to display information about a node in a popup window.
 */
public class NodeInfoPopup extends AbstractAction {

	private static final long serialVersionUID = -1859278887122010885L;

	private final GraphPlaySheet gps;
	private final Collection<GraphElement> pickedVertex;

	public NodeInfoPopup( GraphPlaySheet gps, Collection<GraphElement> picked ) {
		super( "Show Information about Selected Node(s)" );
		this.putValue( Action.SHORT_DESCRIPTION,
				"Draw a box to select nodes, or hold Shift and click on nodes" );
		this.gps = gps;
		pickedVertex = picked;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		MultiMap<URI, GraphElement> typeCounts = new MultiMap<>();
		for ( GraphElement v : pickedVertex ) {
			URI vType = v.getType();
			typeCounts.add( vType, v );
		}

		ValueFactory vf = new ValueFactoryImpl();
		List<Value[]> data = new ArrayList<>();
		int total = 0;
		for ( Map.Entry<URI, List<GraphElement>> en : typeCounts.entrySet() ) {
			Value[] row = { en.getKey(), vf.createLiteral( en.getValue().size() ) };
			data.add( row );
			total += en.getValue().size();
		}

		data.add( new Value[]{ vf.createLiteral( "Total Vertex Count" ),
			vf.createLiteral( total ) } );

		GridPlaySheet grid = new GridPlaySheet();
		grid.setTitle( "Selected Node/Edge Information" );
		grid.create( data, Arrays.asList( "Property Name", "Value" ), gps.getEngine() );
		gps.addSibling( grid );
	}
}
