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
package gov.va.semoss.algorithm.impl;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import gov.va.semoss.algorithm.api.IAlgorithm;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;

/**
 * This class performs the calculations for data latency.
 */
public class DataLatencyPerformer implements IAlgorithm {

	private static final Logger logger = Logger.getLogger( DataLatencyPerformer.class );
	public static final URI FREQUENCY = new URIImpl( "semoss://freq" );
	private final GraphPlaySheet ps;
	private final List<SEMOSSVertex> pickedVertex = new ArrayList<>();
	private double value;
	private final Set<SEMOSSEdge> masterEdgeVector = new HashSet<>();//keeps track of everything accounted for in the forest
	private final Set<SEMOSSVertex> masterVertexVector = new HashSet<>();
	private final List<SEMOSSVertex> currentPathVerts = new ArrayList<>();//these are used for depth search first
	private final List<SEMOSSEdge> currentPathEdges = new ArrayList<>();
	private Forest<SEMOSSVertex, SEMOSSEdge> forest;
	private double currentPathLate;
	Set<SEMOSSEdge> validEdges = new HashSet<>();
	Set<SEMOSSVertex> validVerts = new HashSet<>();
	String selectedNodes = "";
	double naFrequencyFraction = 0;
	double notInterfaceFraction = 1;
	Hashtable<SEMOSSEdge, Double> finalEdgeScores = new Hashtable();
	Hashtable<SEMOSSVertex, Double> finalVertScores = new Hashtable();
	boolean finalScoresFilled = false;

	/**
	 * Given a specific playsheet and a vector, delegate the forest, get edges and
	 * vertices and add them to master vectors.
	 *
	 * @param p GraphPlaySheetFrame
	 * @param vect DBCMVertex[]
	 */
	public DataLatencyPerformer( GraphPlaySheet p ) {
		ps = p;
	}

	/**
	 * Sets initial value for performing data latency calculation.
	 *
	 * @param val double
	 */
	public void setValue( double val ) {
		value = val;
	}

	/**
	 * Clears the hashtables containing strings and DBCM vertices. Create a new
	 * vector of vertices of forest roots and run the depth search to look for
	 * complete paths / loops.
	 */
	@Override
	public void execute( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> nodes ) {
		validVerts.clear();
		validEdges.clear();

		pickedVertex.clear();
		pickedVertex.addAll( nodes );
		forest = new DelegateForest<SEMOSSVertex, SEMOSSEdge>( graph );
		Collection<SEMOSSEdge> edges = forest.getEdges();
		Collection<SEMOSSVertex> v = forest.getVertices();
		masterEdgeVector.addAll( edges );
		masterVertexVector.addAll( v );

		Collection<SEMOSSVertex> forestRoots = getForestRoots();
		runDepthSearchFirst( forestRoots );
		setTransformers();
	}

	/**
	 * Puts selected roots into a vector and adds properties into the hashtable of
	 * valid vertices. If not specified, this method performs the above actions on
	 * all nodes.
	 *
	 * @return Vector<DBCMVertex>
	 */
	private Collection<SEMOSSVertex> getForestRoots() {
		List<SEMOSSVertex> forestRoots = new ArrayList<>();
		if ( pickedVertex.isEmpty() ) {
			selectedNodes = "All";
			List<SEMOSSVertex> forestRootsCollection = new ArrayList<>();
			for ( Tree<SEMOSSVertex, SEMOSSEdge> t : forest.getTrees() ) {
				forestRootsCollection.add( t.getRoot() );
			}
			for ( SEMOSSVertex v : forestRootsCollection ) {
				forestRoots.add( v );
				validVerts.add( v );
				finalVertScores.put( v, 0.0 );
			}
		}
		else {
			int count = 0;
			for ( SEMOSSVertex selectedVert : pickedVertex ) {
				forestRoots.add( selectedVert );
				validVerts.add( selectedVert );
				finalVertScores.put( selectedVert, 0.0 );
				if ( count > 0 ) {
					selectedNodes = selectedNodes + ", ";
				}
				selectedNodes = selectedNodes + selectedVert.getProperty( RDFS.LABEL );
				count++;
			}
		}
		return forestRoots;
	}

	/**
	 * Get all possible full length paths for every vertex in the master vertex
	 * vector. If a path returns back to the starting node, then put it inside the
	 * loop hashtable.
	 *
	 * @param roots Vector<DBCMVertex>	List of roots.
	 */
	private void runDepthSearchFirst( Collection<SEMOSSVertex> roots ) {

		for ( SEMOSSVertex vertex : roots ) {
			Map<SEMOSSEdge, Double> usedLeafEdges = new HashMap<>();//keeps track of all bottom edges previously visited and their score

			List<SEMOSSVertex> currentNodes = new ArrayList<>();
			//use next nodes as the future set of nodes to traverse down from.
			List<SEMOSSVertex> nextNodes = new ArrayList<>();

			int levelIndex = 0;
			while ( !currentPathVerts.isEmpty() || levelIndex == 0 ) {
				int pathIndex = 0;
				currentPathVerts.clear();
				currentNodes.add( vertex );
				currentPathEdges.clear();
				currentPathLate = 0;
				while ( !nextNodes.isEmpty() || pathIndex == 0 ) {
					nextNodes.clear();
					SEMOSSVertex nextNode = null;
					while ( !currentNodes.isEmpty() ) {
						SEMOSSVertex vert = currentNodes.remove( 0 );
						nextNode = traverseDepthDownward( vert, usedLeafEdges, vertex );
						if ( nextNode != null ) {
							nextNodes.add( nextNode );
						}

						pathIndex++;
					}
					currentNodes.addAll( nextNodes );

					levelIndex++;
					//if the path has created a loop, it needs to be done.  Otherwise it will unfairly evaluate the rest of the paths downstream
					//if(currentPathVerts.indexOf(nextNode)!=currentPathVerts.lastIndexOf(nextNode)) nextNodes.clear();
				}
				// Complete path

				if ( currentPathEdges.size() > 0 ) {
					SEMOSSEdge leafEdge = currentPathEdges.get( currentPathEdges.size() - 1 );
					addPathAsValid( currentPathEdges, currentPathVerts );
					usedLeafEdges.put( leafEdge, currentPathLate );
					//put in the final scores hash if it is a better score
					Double edgeScore = currentPathLate;
					if ( finalEdgeScores.containsKey( leafEdge ) ) {
						if ( finalEdgeScores.get( leafEdge ) < currentPathLate ) {
							edgeScore = finalEdgeScores.get( leafEdge );
						}
					}
					finalEdgeScores.put( leafEdge, edgeScore );
				}
			}

		}
	}

	/**
	 * Returns the next vertex for data latency calculations to be performed upon.
	 * Uses the current vertex and keeps track of which edges are valid. Scores
	 * vertices based on most efficient way to get to that vertex.
	 *
	 * @param vert DBCMVertex
	 * @param usedLeafEdges Hashtable<DBCMEdge,Double>
	 * @param rootVert DBCMVertex
	 *
	 * @return DBCMVertex
	 */
	private SEMOSSVertex traverseDepthDownward( SEMOSSVertex vert,
			Map<SEMOSSEdge, Double> usedLeafEdges, SEMOSSVertex rootVert ) {
		SEMOSSVertex nextVert = null;
		Collection<SEMOSSEdge> edgeArray = getValidEdges( forest.getOutEdges( vert ) );
		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			String freqString = "";
			if ( edge.getProperty( FREQUENCY ) != null ) {
				String frequency = edge.getProperty( FREQUENCY ) + "";
				freqString = frequency.replaceAll( "\"", "" );
			}
			else {
				validEdges.add( edge );
			}
			//if the edge is not available or doens't have a frequency, remove from master edges and make red
			if ( !isAvailable( freqString ) ) {
				//masterEdgeVector.remove(edge);
				validEdges.add( edge );
			}
			double freqDouble = translateString( freqString );
			double tempPathLate = currentPathLate + freqDouble;
			double leafEdgeScore = 0.0;
			if ( usedLeafEdges.containsKey( edge ) ) {
				leafEdgeScore = usedLeafEdges.get( edge );
			}
			if ( tempPathLate <= value && masterVertexVector.contains( inVert )
					&& ( !usedLeafEdges.containsKey( edge ) || tempPathLate < leafEdgeScore ) && !currentPathEdges.contains( edge ) ) {
				nextVert = inVert;//this is going to be the returned vert, so this is all set
				if ( currentPathVerts.contains( inVert ) ) {
					currentPathVerts.add( inVert );
					currentPathEdges.add( edge );
					return null;
				}
				currentPathVerts.add( inVert );
				currentPathEdges.add( edge );
				currentPathLate = tempPathLate;
				//add vertex to final scores if this is a better way to get to that vertex
				Double vertScore = currentPathLate;
				if ( finalVertScores.containsKey( inVert ) ) {
					if ( finalVertScores.get( inVert ) < currentPathLate ) {
						vertScore = finalVertScores.get( inVert );
					}
				}
				finalVertScores.put( inVert, vertScore );
				return nextVert;
			}
		}
		return nextVert;
	}

	/**
	 * From the collection of DBCM edges, determine which edges are valid
	 *
	 * @param vector Collection<DBCMEdge>
	 *
	 * @return Vector<DBCMEdge>
	 */
	private List<SEMOSSEdge> getValidEdges( Collection<SEMOSSEdge> vector ) {
		List<SEMOSSEdge> validEdges = new ArrayList<>();
		if ( vector == null ) {
			return validEdges;
		}
		for ( SEMOSSEdge edge : vector ) {
			if ( masterEdgeVector.contains( edge ) ) {
				validEdges.add( edge );
			}
		}
		return validEdges;
	}

	/**
	 * Goes through edges and vertices to put valid paths in the appropriate hash
	 * tables. Valid edges hashtable has properties and the edge score for the
	 * best paths.
	 *
	 * @param edges Vector<DBCMEdge>
	 * @param verts Vector<DBCMVertex>
	 */
	private void addPathAsValid( Collection<SEMOSSEdge> edges, Collection<SEMOSSVertex> verts ) {
		validVerts.addAll( verts );
		logger.warn( "this code is probably wrong" );
		for ( SEMOSSEdge e : edges ) {
			validEdges.add( e );
		}
	}

	/**
	 * Given an edge, return a double of the edge score based on the frequency of
	 * the edge.
	 *
	 * @param edge DBCMEdge
	 *
	 * @return double
	 */
	private double getEdgeScore( SEMOSSEdge edge ) {
		double ret = 1.0;
		if ( edge.getProperty( FREQUENCY ) == null ) {
			ret = notInterfaceFraction;
		}
		else {
			String frequency = edge.getProperty( FREQUENCY ) + "";
			String freqString = frequency.replaceAll( "\"", "" );
			if ( !isAvailable( freqString ) ) {
				ret = naFrequencyFraction;
			}
		}

		return ret;
	}

	/**
	 * If the string representing the frequency of data is anything other than TBD
	 * or N/A, then the calculation can be performed.
	 *
	 * @param freqString String	String representing how frequently data is
	 * released.
	 *
	 * @return boolean	True as long as frequency is not TBD or N/A for when data
	 * is added
	 */
	private boolean isAvailable( String freqString ) {
		if ( ( freqString.equalsIgnoreCase( Constants.TBD ) )
				|| ( freqString.equalsIgnoreCase( Constants.NA ) ) ) {
			return false;
		}
		return true;
	}

	/**
	 * Fills hashtables up to the point of the provided input value. Gets the
	 * forest roots from a vector of vertices and gets all possible full length
	 * paths.
	 *
	 * @param inputValue Double
	 */
	public void fillHashesWithValuesUpTo( Double inputValue ) {
		value = inputValue;

		Collection<SEMOSSVertex> forestRoots = getForestRoots();
		runDepthSearchFirst( forestRoots );

		finalScoresFilled = true;
	}

	/**
	 * Clears the valid vertices and edges hashtables; fills them with new values;
	 * sets the new transformers.
	 */
	public void executeFromHash() {
		validVerts.clear();
		validEdges.clear();
		fillValidComponentHashes();
		setTransformers();
	}

	/**
	 * Creates new vectors of valid edges and vertices. Iterates through the
	 * hashtables of vertices and vertex scores and edges and edge scores. Add the
	 * vertex or edge to the vectors as long as the score is not null. Given the
	 * valid edges and vertices, we can add the path as valid.
	 */
	public void fillValidComponentHashes() {
		List<SEMOSSEdge> validEdges = new ArrayList<>();
		List<SEMOSSVertex> validVerts = new ArrayList<>();
		Iterator vertIt = finalVertScores.keySet().iterator();
		while ( vertIt.hasNext() ) {
			SEMOSSVertex vert = (SEMOSSVertex) vertIt.next();
			Double score = finalVertScores.get( vert );
			if ( score != null ) {
				if ( score <= value ) {
					validVerts.add( vert );
				}
			}
		}
		Iterator edgeIt = finalEdgeScores.keySet().iterator();
		while ( edgeIt.hasNext() ) {
			SEMOSSEdge edge = (SEMOSSEdge) edgeIt.next();
			Double score = finalEdgeScores.get( edge );
			if ( score != null ) {//It will be null if it is TBD or if the max value wasn't big enough
				if ( score <= value ) {
					validEdges.add( edge );
				}
			}
		}
		addPathAsValid( validEdges, validVerts );
	}

	/**
	 * Given a string representing frequency of data sent through systems,
	 * quantify this value based on frequency.
	 *
	 * @param freqString String	String representing how frequently data is
	 * released.
	 *
	 * @return int	Number associated with the frequency string.
	 */
	private int translateString( String freqString ) {
		int freqInt = 0;
		if ( freqString.equalsIgnoreCase( Constants.TBD ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( Constants.NA ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Real-time (user-initiated)" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (monthly)" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Monthly" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (daily)" ) ) {
			freqInt = 24;
		}
		else if ( freqString.equalsIgnoreCase( "Batch(Daily)" ) ) {
			freqInt = 24;
		}
		else if ( freqString.equalsIgnoreCase( "Real-time" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Transactional" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "On Demand" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Event Driven (seconds-minutes)" ) ) {
			freqInt = 60;
		}
		else if ( freqString.equalsIgnoreCase( "TheaterFramework" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Event Driven (Seconds)" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Web services" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "TF" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (12/day)" ) ) {
			freqInt = 2;
		}
		else if ( freqString.equalsIgnoreCase( "SFTP" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (twice monthly)" ) ) {
			freqInt = 360;
		}
		else if ( freqString.equalsIgnoreCase( "Daily" ) ) {
			freqInt = 24;
		}
		else if ( freqString.equalsIgnoreCase( "Hourly" ) ) {
			freqInt = 1;
		}
		else if ( freqString.equalsIgnoreCase( "Near Real-time (transaction initiated)" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (three times a week)" ) ) {
			freqInt = 56;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (weekly)" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Near Real-time" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Real Time" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (bi-monthly)" ) ) {
			freqInt = 1440;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (semiannually)" ) ) {
			freqInt = 4392;
		}
		else if ( freqString.equalsIgnoreCase( "Event Driven (Minutes-hours)" ) ) {
			freqInt = 1;
		}
		else if ( freqString.equalsIgnoreCase( "Annually" ) ) {
			freqInt = 8760;
		}
		else if ( freqString.equalsIgnoreCase( "Batch(Monthly)" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "Bi-Weekly" ) ) {
			freqInt = 336;
		}
		else if ( freqString.equalsIgnoreCase( "Daily at end of day" ) ) {
			freqInt = 24;
		}
		else if ( freqString.equalsIgnoreCase( "TCP" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "event-driven (Minutes-hours)" ) ) {
			freqInt = 1;
		}
		else if ( freqString.equalsIgnoreCase( "Interactive" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly Quarterly" ) ) {
			freqInt = 2184;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly Daily Weekly Weekly Weekly Weekly Daily Daily Daily" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly Daily" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Periodic" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (4/day)" ) ) {
			freqInt = 6;
		}
		else if ( freqString.equalsIgnoreCase( "Batch(Daily/Monthly)" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly; Interactive; Interactive" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "interactive" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (quarterly)" ) ) {
			freqInt = 2184;
		}
		else if ( freqString.equalsIgnoreCase( "Every 8 hours (KML)/On demand (HTML)" ) ) {
			freqInt = 8;
		}
		else if ( freqString.equalsIgnoreCase( "Monthly at beginning of month, or as user initiated" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "On demad" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Monthly Bi-Monthly Weekly Weekly" ) ) {
			freqInt = 720;
		}
		else if ( freqString.equalsIgnoreCase( "Quarterly" ) ) {
			freqInt = 2184;
		}
		else if ( freqString.equalsIgnoreCase( "On-demand" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "user upload" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "1/hour (KML)/On demand (HTML)" ) ) {
			freqInt = 1;
		}
		else if ( freqString.equalsIgnoreCase( "DVD" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Real-time " ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Weekly " ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Annual" ) ) {
			freqInt = 8760;
		}
		else if ( freqString.equalsIgnoreCase( "Daily Interactive" ) ) {
			freqInt = 24;
		}
		else if ( freqString.equalsIgnoreCase( "NFS, Oracle connection" ) ) {
			freqInt = 0;
		}
		else if ( freqString.equalsIgnoreCase( "Batch(Weekly)" ) ) {
			freqInt = 168;
		}
		else if ( freqString.equalsIgnoreCase( "Batch(Quarterly)" ) ) {
			freqInt = 2184;
		}
		else if ( freqString.equalsIgnoreCase( "Batch (yearly)" ) ) {
			freqInt = 8760;
		}
		else if ( freqString.equalsIgnoreCase( "Each user login instance" ) ) {
			freqInt = 0;
		}
		return freqInt;
	}

	/**
	 * Sets the transformers based on valid edges and vertices for the playsheet.
	 */
	private void setTransformers() {
		ps.highlight( validVerts, validEdges );
	}
}
