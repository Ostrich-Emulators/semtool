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
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;

/**
 * This class is used to identify loops within a network.
 */
public class LoopIdentifierProcessor extends AbstractAction {

	private static final Logger log = Logger.getLogger( LoopIdentifierProcessor.class );
	private final GraphPlaySheet gps;
	private final List<SEMOSSVertex> verts;

	public LoopIdentifierProcessor( GraphPlaySheet ps, Collection<SEMOSSVertex> sels ) {
		super( "Loop Identifier" );
		this.gps = ps;
		this.verts = new ArrayList<>( sels.isEmpty()
				? gps.getVisibleGraph().getVertices() : sels );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		execute();
	}

	/**
	 * Executes the process of loop identification. If a node in the forest is not
	 * part of a loop, remove it from the forest. Run depth search in order to
	 * validate the remaining edges in the forest.
	 */
	public void execute() {
		//All I have to do is go through every node in the forest
		//if the node has in and out, it could be part of a loop
		//if a node has only in or only out edges, it is not part of a loop
		//therefore, remove the vertex and all edges associated with it from the forest
		//once there are no edges getting removed, its time to stop
		//Then I run depth search first to validate the edges left

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> realg = gps.getVisibleGraph();
		// now do a DFS for every node. If we ever get back to our "root," we 
		// have a loop

		for ( SEMOSSVertex root : verts ) {
			// we're going to do a DFS here, but keep track of what edges we've traversed
			// along the way so we can highlight the loop

			for ( SEMOSSEdge edge : realg.getOutEdges( root ) ) {
				Set<SEMOSSVertex> seen = new HashSet<>();
				List<SEMOSSEdge> steps = new ArrayList<>();
				steps.add( edge );

				List<SEMOSSEdge> edges = lookForLoopBackTo( root, realg.getDest( edge ),
						seen, steps, realg, 0 );

				if ( !edges.isEmpty() && realg.getDest( edges.get( edges.size() - 1 ) ).equals( root ) ) {
					// highlight all the loop edges

					Set<SEMOSSVertex> vs = new HashSet<>();
					for ( SEMOSSEdge e : edges ) {
						vs.addAll( realg.getEndpoints( e ) );
					}

					gps.getView().setSkeletonMode( true );
					gps.getView().highlight( vs, edges );
				}
			}
		}
	}

	/**
	 * Keep traversing outward until we either reach our goal or exhaust our edges
	 *
	 * @param goal
	 * @param root
	 * @param visited
	 * @param pathSoFar
	 * @param mygraph
	 * @return
	 */
	private List<SEMOSSEdge> lookForLoopBackTo( SEMOSSVertex goal, SEMOSSVertex step,
			Set<SEMOSSVertex> visited, List<SEMOSSEdge> steps,
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> mygraph, int depth ) {

		if ( step.equals( goal ) ) {
			//we're on our goal, so we must have looped
			return steps;
		}

		if ( visited.contains( step ) ) {
			return steps;
		}

		visited.add( step );

		if ( mygraph.getOutEdges( step ).isEmpty() ) {
			return steps;
		}

		// we haven't stopped yet, so take another step
		List<SEMOSSEdge> shortest = null; // we want the shortest loop we can get
		for ( SEMOSSEdge edge : mygraph.getOutEdges( step ) ) {
			List<SEMOSSEdge> journey = new ArrayList<>( steps );
			journey.add( edge );

			List<SEMOSSEdge> path = lookForLoopBackTo( goal, mygraph.getDest( edge ),
					visited, journey, mygraph, depth + 1 );
			if ( null == shortest || path.size() < shortest.size() ) {
				shortest = path;
			}
		}
		steps = shortest;

		return steps;
	}
}
