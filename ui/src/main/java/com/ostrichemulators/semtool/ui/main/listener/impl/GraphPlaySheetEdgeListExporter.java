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
package com.ostrichemulators.semtool.ui.main.listener.impl;

import edu.uci.ics.jung.graph.DirectedGraph;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridRAWPlaySheet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Controls the export graph to grid feature.
 */
public class GraphPlaySheetEdgeListExporter extends AbstractAction {

	private static final long serialVersionUID = -8428913823791195745L;
	private static final Logger logger
			= Logger.getLogger( GraphPlaySheetEdgeListExporter.class );
	private final GraphPlaySheet gps;

	public GraphPlaySheetEdgeListExporter( GraphPlaySheet ps ) {
		super( "Convert to Edge List" );
		putValue( Action.SHORT_DESCRIPTION,
				"Convert graph display to a table display" );
		gps = ps;
	}

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param arg0 ActionEvent - The event that triggers the actions in the
	 * method.
	 */
	@Override
	public void actionPerformed( ActionEvent arg0 ) {
		logger.debug( "Export button has been pressed" );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gps.getVisibleGraph();

		ValueFactory vf = new ValueFactoryImpl();
		List<Value[]> vals = new ArrayList<>();

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			SEMOSSVertex src = graph.getSource( edge );
			SEMOSSVertex dst = graph.getDest( edge );

			Value subj = vf.createLiteral( src.getLabel() );
			Value obj = vf.createLiteral( dst.getLabel() );
			vals.add( new Value[] { subj, obj } );
		}

		GridRAWPlaySheet.convertUrisToLabels( vals, gps.getEngine() );

		GridRAWPlaySheet newGps = new GridRAWPlaySheet();
		newGps.setTitle( "EXPORT: " + gps.getTitle() );
		newGps.create( vals, Arrays.asList( "Source", "Destination" ), gps.getEngine() );
		gps.addSibling( "Graph Edge List", newGps );
	}
}
