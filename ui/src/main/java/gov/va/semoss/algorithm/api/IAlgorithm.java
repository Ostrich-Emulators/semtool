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
package gov.va.semoss.algorithm.api;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import java.util.Collection;

/**
 * This interface is used to standardize the functionality of the algorithms
 * applied to graphs. Algorithms are, in general terms, a way of using the data
 * and attributes of a play sheet to gather insight that is not immediately
 * apparent from the graph by itself.
 *
 * @author karverma
 * @version $Revision: 1.0 $
 */
public interface IAlgorithm {

	/**
	 * Executes the algorithm on the given nodes within the given graph
	 *
	 * @param graph
	 * @param verts
	 */
	public void execute( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> verts );
}
