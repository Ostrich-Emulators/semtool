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
 *****************************************************************************
 */
package gov.va.semoss.algorithm.impl;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * This class has linear optimization functionality via the lpsolve import that
 * is used primarily for ServiceOptimizer.
 */
public class LPOptimizer extends AbstractOptimizer {

	Logger logger = Logger.getLogger( getClass() );

	public LpSolve solver;

	/**
	 * Sets up the model for calculations to be performed upon.
	 */
	@Override
	public void setupModel() {
		try {
			setVariables();
		}
		catch ( LpSolveException e1 ) {
			logger.error( e1 );
		}
		setConstraints();
		solver.setAddRowmode( false );
		setObjFunction();
	}

	/**
	 * Gathers data set.
	 */
	@Override
	public void gatherDataSet() {

	}

	/**
	 * Sets variables in model.
	 */
	@Override
	public void setVariables() throws LpSolveException {

	}

	/**
	 * Sets constraints in the model.
	 */
	@Override
	public void setConstraints() {

	}

	/**
	 * Sets the function for calculations.
	 */
	@Override
	public void setObjFunction() {

	}

	/**
	 * Executes the optimization.
	 */
	@Override
	public void execute( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> verts ){
		solver.setVerbose( LpSolve.IMPORTANT );
		// solve the problem
		try {
			solver.solve();
		}
		catch ( LpSolveException e ) {
			logger.error( e );
		}

		// print solution
		//logger.info("Value of objective function: " + solver.getObjective());
	}

	/**
	 * Deletes the model in order to free memory.
	 */
	@Override
	public void deleteModel() {
		solver.deleteLp();
	}
}
