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

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;

import com.ostrichemulators.semtool.ui.components.playsheets.GridPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridRAWPlaySheet;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTableCellRenderer;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Controls the running of the node rank algorithm
 */
public class GraphNodeRankListener extends AbstractAction {

	private static final long serialVersionUID = 5899960815619489928L;
	private final GraphPlaySheet playsheet;

	public GraphNodeRankListener( GraphPlaySheet gps ) {
		super( "NodeRank Algorithm" );
		playsheet = gps;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {

		//set up page rank
		double alpha = 0.15;
		double tolerance = 0.001;
		int maxIterations = 100;
		final PageRank<SEMOSSVertex, ?> ranker
				= new PageRank<>( playsheet.getVisibleGraph(), alpha );

		ranker.setTolerance( tolerance );
		ranker.setMaxIterations( maxIterations );
		ranker.evaluate();

		List<SEMOSSVertex> col = new ArrayList<>( playsheet.getVisibleGraph().getVertices() );
		// sort based on ranking score
		Collections.sort( col, new Comparator<SEMOSSVertex>() {

			@Override
			public int compare( SEMOSSVertex t, SEMOSSVertex t1 ) {
				double d = ranker.getVertexScore( t ) - ranker.getVertexScore( t1 );
				if ( 0 == d ) {
					return 0;
				}
				return ( d < 0 ? -1 : 1 );
			}
		} );

		GridRAWPlaySheet grid = new GridPlaySheet();

		List<String> colNames
				= Arrays.asList( "Vertex Name", "Vertex Type", "Page Rank Score" );
		List<Value[]> list = new ArrayList<>();
		LabeledPairTableCellRenderer<Value> renderer
				= LabeledPairTableCellRenderer.getValuePairRenderer( playsheet.getEngine() );

		// process the graph and list out all nodes, type, pagerank
		ValueFactory vf = new ValueFactoryImpl();
		for ( SEMOSSVertex v : col ) {
			URI type = v.getType();
			double r = ranker.getVertexScore( v );

			Value[] scores
					= { vf.createLiteral( v.getLabel() ), type, vf.createLiteral( r ) };
			list.add( scores );
		}

		grid.create( list, colNames, playsheet.getEngine() );
		playsheet.addSibling( "NodeRank Scores", grid );
	}
}
