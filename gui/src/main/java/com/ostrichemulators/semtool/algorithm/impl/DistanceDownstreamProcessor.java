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
package com.ostrichemulators.semtool.algorithm.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter;
import com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter.Search;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridPlaySheet;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * This class uses the information from DistanceDownstreamInserter in order to
 * actually perform the distance downstream calculation.
 */
public class DistanceDownstreamProcessor extends AbstractAction {

	private static final long serialVersionUID = 3191222375480129585L;
	public static final IRI WEIGHT = SimpleValueFactory.getInstance().createIRI( "semoss://weight" );
	private static final Logger log
			= Logger.getLogger( DistanceDownstreamProcessor.class );
	protected final DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph;
	protected Set<SEMOSSVertex> selecteds = new HashSet<>();
	protected GraphPlaySheet gps;

	public DistanceDownstreamProcessor( GraphPlaySheet gps, Collection<SEMOSSVertex> verts ) {
		super( "Distance Downstream" );

		this.gps = gps;
		graph = gps.getVisibleGraph();
		selecteds.addAll( verts.isEmpty() ? graph.getVertices() : verts );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		ValueFactory vf = SimpleValueFactory.getInstance();

		Forest<SEMOSSVertex, SEMOSSEdge> forest
				= GraphToTreeConverter.convertq( graph, selecteds, Search.BFS );

		List<Value[]> rows = new ArrayList<>();
		for ( Tree<SEMOSSVertex, SEMOSSEdge> tree : forest.getTrees() ) {
			SEMOSSVertex root = tree.getRoot();

			// we want a dfs of the tree to keep the nodes in a path together
			Queue<SEMOSSVertex> q = Collections.asLifoQueue( new ArrayDeque<>() );
			q.addAll( tree.getChildren( root ) );
			while ( !q.isEmpty() ) {
				SEMOSSVertex child = q.poll();
				Value row[] = {
					root.getIRI(),
					vf.createLiteral( tree.getDepth( child ) ),
					child.getIRI(),
					child.getType()
				};
				rows.add( row );

				q.addAll( tree.getChildren( child ) );
			}
		}

		GridPlaySheet grid = new GridPlaySheet();
		grid.create( rows, Arrays.asList( "Root Vertex", "Hops", "Child Vertex",
				"Child Type" ), gps.getEngine() );
		gps.addSibling( "Hops Downstream", grid );
	}

	public void setPlaySheet( GraphPlaySheet ps ) {
		gps = ps;
	}
}
