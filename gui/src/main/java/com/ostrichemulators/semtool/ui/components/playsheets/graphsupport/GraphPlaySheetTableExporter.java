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

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridRAWPlaySheet;
import com.ostrichemulators.semtool.util.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Controls the export graph to grid feature.
 */
public class GraphPlaySheetTableExporter extends AbstractAction {

	private static final long serialVersionUID = -8428913823791195745L;
	private static final Logger logger
			= Logger.getLogger( GraphPlaySheetTableExporter.class );
	private final GraphPlaySheet gps;

	public GraphPlaySheetTableExporter( GraphPlaySheet ps ) {
		super( "Convert to Table" );
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

		Set<SEMOSSVertex> verts	= new HashSet<>( gps.getVisibleGraph().getVertices() );
		Set<SEMOSSEdge> edges	= new HashSet<>( gps.getVisibleGraph().getEdges() );

		ValueFactory vf = new ValueFactoryImpl();
		List<Value[]> vals = new ArrayList<>();
		for ( SEMOSSVertex v : verts ) {
			Value subj = vf.createLiteral( v.getLabel() );
			Value o =  v.getValue( RDF.TYPE );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Vertex Type" );
				// Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, o } );
			}
			o = v.getIRI();
			if ( null != o ) {
				Value pred = vf.createLiteral( "URI" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}
			Value pred = vf.createLiteral( "Type" );
			Value obj = vf.createLiteral( "Vertex" );
			vals.add( new Value[]{ subj, pred, obj } );
		}

		for ( SEMOSSEdge v : edges ) {
			Value subj = vf.createLiteral( v.getLabel() );
			Value o = v.getValue( Constants.EDGE_NAME );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Name" );
				//Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, o } );
			}
			o = v.getIRI();
			if ( null != o ) {
				Value pred = vf.createLiteral( "URI" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}

			o = v.getValue( Constants.EDGE_TYPE );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Edge Type" );
				// Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, o } );
			}

			Value pred = vf.createLiteral( "Type" );
			Value obj = vf.createLiteral( "Edge" );
			vals.add( new Value[]{ subj, pred, obj } );
		}

		GridRAWPlaySheet.convertUrisToLabels( vals, gps.getEngine() );

		GridRAWPlaySheet newGps = new GridRAWPlaySheet();
		newGps.setTitle( "EXPORT: " + gps.getTitle() );
		newGps.create( vals, Arrays.asList( "Vertex or Edge Label", "Property", "Value" ),
				gps.getEngine() );
		gps.addSibling( "Graph as Table", newGps );
	}
}
