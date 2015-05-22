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

import java.util.ArrayList;
import java.util.Collection;

import gov.va.semoss.algorithm.api.IAlgorithm;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.GridFilterData;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.transformer.ArrowDrawPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import edu.uci.ics.jung.graph.DelegateForest;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;

/**
 * This class is used to identify loops within a network.
 */
public class LoopIdentifierProcessor extends AbstractAction implements IAlgorithm {

	private final DelegateForest<SEMOSSVertex, SEMOSSEdge> forest;
	private final List<SEMOSSVertex> selectedVerts;
	private final GridFilterData gfd = new GridFilterData();
	private final GraphPlaySheet playSheet;
	private final Set<SEMOSSEdge> nonLoopEdges = new HashSet<>();
	private final  Set<SEMOSSEdge> loopEdges = new HashSet<>();
	private final Set<SEMOSSVertex> nonLoopVerts = new HashSet<>();
	private final  Set<SEMOSSVertex> loopVerts = new HashSet<>();
	private final  List<SEMOSSEdge> masterEdgeVector = new ArrayList();//keeps track of everything accounted for in the forest
	private final  List<SEMOSSVertex> masterVertexVector = new ArrayList();
	private final  List<SEMOSSVertex> currentPathVerts = new ArrayList<>();//these are used for depth search first
	private final  List<SEMOSSEdge> currentPathEdges = new ArrayList<>();

	public LoopIdentifierProcessor( GraphPlaySheet gps, Collection<SEMOSSVertex> verts ) {
		super( "Loop Identifier" );
		playSheet = gps;
		forest = playSheet.getForest();
		selectedVerts = new ArrayList<>( verts );
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
	@Override
	public void execute() {
		//All I have to do is go through every node in the forest
		//if the node has in and out, it could be part of a loop
		//if a node has only in or only out edges, it is not part of a loop
		//therefore, remove the vertex and all edges associated with it from the forest
		//once there are no edges getting removed, its time to stop
		//Then I run depth search first to validate the edges left

		List<SEMOSSVertex> currentVertices = new ArrayList<>( forest.getVertices() );
		List<SEMOSSVertex> nextVertices = new ArrayList<>( currentVertices );
		List<SEMOSSEdge> newlyRemovedEdges = new ArrayList<>();

		int count = 0;
		while ( count == 0 || !newlyRemovedEdges.isEmpty() ) {
			newlyRemovedEdges.clear();
			for ( SEMOSSVertex vertex : currentVertices ) {
				Collection<SEMOSSEdge> inEdges = getValidEdges( vertex.getInEdges() );
				Collection<SEMOSSEdge> outEdges = getValidEdges( vertex.getOutEdges() );

				//if inEdges is 0, put the vert and its edges in hashtables and remove everything associated with it from the forest
				if ( inEdges.isEmpty() ) {
					nonLoopVerts.add( vertex );
					nonLoopEdges.addAll( outEdges );
					newlyRemovedEdges.addAll( removeEdgesFromMaster( outEdges ) );
					nextVertices.remove( vertex );
					masterVertexVector.remove( vertex );
				}
				else if ( outEdges.isEmpty() ) {
					nonLoopVerts.add( vertex );
					nonLoopEdges.addAll( inEdges );

					newlyRemovedEdges.addAll( removeEdgesFromMaster( inEdges ) );
					nextVertices.remove( vertex );
					masterVertexVector.remove( vertex );
				}
				count++;
			}
			currentVertices.clear();
			currentVertices.addAll( nextVertices );
		}
		//phase 1 is now complete.  The only vertices and edges left must have in and out edges
		//However, there is still the possiblity of fake edges and nodes that exist only between two loops
		//Now I will perform depth search first on all remaining nodes to ensure that every edge is a loop
		runDepthSearchFirst();
		//Everything that is left in nextVertices and the forest now must be loopers
		//lets put them in their respective hashtables and set the transformers
		setTransformers();

	}

	/**
	 * Get all possible full length paths for every vertex in the master vertex
	 * vector. If a path returns back to the starting node, then put it inside the
	 * loop hashtable.
	 */
	private void runDepthSearchFirst() {
		//for every vertex remaining in master vertex vector, I will get all possible full length paths
		//If a path return back to the starting node, put it in the loop hash
		for ( SEMOSSVertex vertex : masterVertexVector ) {
			Set<SEMOSSVertex> usedLeafVerts = new HashSet<>();
			usedLeafVerts.add( vertex );

			List<SEMOSSVertex> currentNodes = new ArrayList<>();
			//use next nodes as the future set of nodes to traverse down from.
			List<SEMOSSVertex> nextNodes = new ArrayList<>();

			//check if there is a loop with itself
			if ( checkIfCompletesLoop( vertex, vertex ) ) {
				addPathAsLoop( currentPathEdges, currentPathVerts );
			}

			int levelIndex = 0;
			while ( !currentPathVerts.isEmpty() || levelIndex == 0 ) {
				int pathIndex = 0;
				currentNodes.add( vertex );
				currentPathVerts.clear();
				currentPathEdges.clear();
				while ( !nextNodes.isEmpty() || pathIndex == 0 ) {
					nextNodes.clear();
					while ( !currentNodes.isEmpty() ) {
						SEMOSSVertex vert = currentNodes.remove( 0 );

						SEMOSSVertex nextNode = traverseDepthDownward( vert, usedLeafVerts );
						if ( nextNode != null ) {
							nextNodes.add( nextNode );
						}

						pathIndex++;
					}
					currentNodes.addAll( nextNodes );

					levelIndex++;
				}
				//Now I should have a complete path.  I need to check to see it it can make it back to the root node.
				//If it can make it back to the root node, it is a loop and should be added to the loop hashtables
				if ( !currentPathVerts.isEmpty() ) {
					SEMOSSVertex leafVert = currentPathVerts.get( currentPathVerts.size() - 1 );
					if ( checkIfCompletesLoop( leafVert, vertex ) ) {
						//add loop to loop hashtables
						addPathAsLoop( currentPathEdges, currentPathVerts );
					}
					usedLeafVerts.add( leafVert );
				}
			}

		}
	}

	/**
	 * Validate whether or not the loop is complete.
	 *
	 * @param leaf DBCMVertex	Child node.
	 * @param root DBCMVertex	Parent node.
	 *
	 * @return boolean Returns true if the loop is complete.
	 */
	private boolean checkIfCompletesLoop( SEMOSSVertex leaf, SEMOSSVertex root ) {
		boolean retBool = false;
		if ( leaf == null ) {
			return false;
		}

		Collection<SEMOSSEdge> edgeArray = getValidEdges( forest.getOutEdges( leaf ) );
		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			if ( inVert.equals( root ) ) {
				currentPathEdges.add( edge );
				currentPathVerts.add( root );
				return true;
			}
		}
		return retBool;
	}

	/**
	 * Returns the next node for loop identification to be performed upon. Uses
	 * the current vertex and keeps track of which edges are valid. Scores
	 * vertices based on most efficient way to get to that vertex.
	 *
	 * @param vert DBCMVertex	Current node.
	 * @param usedLeafVerts Vector<DBCMVertex>
	 *
	 * @return DBCMVertex Next node for processing.
	 */
	private SEMOSSVertex traverseDepthDownward( SEMOSSVertex vert,
			Collection<SEMOSSVertex> usedLeafVerts ) {
		SEMOSSVertex nextVert = null;
		Collection<SEMOSSEdge> edgeArray = getValidEdges( forest.getOutEdges( vert ) );
		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			if ( masterVertexVector.contains( inVert )
					&& !usedLeafVerts.contains( inVert )
					&& !currentPathVerts.contains( inVert ) ) {
				nextVert = inVert;//this is going to be the returned vert, so this is all set
				currentPathVerts.add( inVert );
				currentPathEdges.add( edge );
				return nextVert;
			}
		}
		return nextVert;
	}

	/**
	 * From the collection of DBCM edges, determine which edges are valid.
	 *
	 * @param vector Collection<DBCMEdge>	Collection of edges.
	 *
	 * @return Vector<DBCMEdge>	List of valid edges.
	 */
	private Collection<SEMOSSEdge> getValidEdges( Collection<SEMOSSEdge> vector ) {
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
	 * Sets the transformers based on valid edges and vertices for the playsheet.
	 */
	private void setTransformers() {
		
		EdgeStrokeTransformer tx = (EdgeStrokeTransformer) playSheet.getView().getRenderContext().getEdgeStrokeTransformer();
		tx.setSelectedEdges( loopEdges );

		ArrowDrawPaintTransformer atx = (ArrowDrawPaintTransformer) playSheet.getView().getRenderContext().getArrowDrawPaintTransformer();
		atx.setEdges( loopEdges );
		PaintTransformer vtx = (PaintTransformer) playSheet.getView().getRenderContext().getVertexFillPaintTransformer();
		vtx.setSelected( loopVerts );
		LabelFontTransformer vlft = (LabelFontTransformer) playSheet.getView().getRenderContext().getVertexFontTransformer();
		vlft.setSelected( loopVerts );
		// repaint it
		playSheet.getView().repaint();
	}

	/**
	 * Adds a given path as a loop in the network.
	 *
	 * @param edges Vector<DBCMEdge>	List of edges.
	 * @param verts Vector<DBCMVertex>	List of nodes.
	 */
	private void addPathAsLoop( Collection<SEMOSSEdge> edges, Collection<SEMOSSVertex> verts ) {
		for ( SEMOSSVertex vertex : verts ) {
			loopVerts.add( vertex );
		}

		for ( SEMOSSEdge edge : edges ) {
			loopEdges.add( edge );
		}
	}

	/**
	 * Removes edges from the master list of edges.
	 *
	 * @param edges Vector<DBCMEdge>	Original list of edges.
	 *
	 * @return Vector<DBCMEdge>	Updated list of edges.
	 */
	private Collection<SEMOSSEdge> removeEdgesFromMaster( Collection<SEMOSSEdge> edges ) {
		List<SEMOSSEdge> removedEdges = new ArrayList<>();
		for ( SEMOSSEdge edge : edges ) {
			if ( masterEdgeVector.contains( edge ) ) {
				removedEdges.add( edge );
				masterEdgeVector.remove( edge );
			}
		}
		return removedEdges;
	}
	
	/**
	 * Sets selected nodes.
	 *
	 * @param pickedVertices DBCMVertex[]	List of picked vertices to be set.
	 */
	public void setSelectedNodes( SEMOSSVertex[] pickedVertices ) {
		selectedVerts.addAll( Arrays.asList( pickedVertices ) );
	}

	/**
	 * Sets playsheet as a graph playsheet.
	 *
	 * @param ps IPlaySheet	Playsheet to be cast.
	 */
	@Override
	public void setPlaySheet( IPlaySheet ps ) {
		throw new UnsupportedOperationException( "set the playsheet in the constructor" );
	}

	/**
	 * Gets variable names.
	 *
	 * //TODO: Return empty object instead of null
	 *
	 * @return String[] List of variable names in a string array.
	 */
	@Override
	public String[] getVariables() {
		throw new UnsupportedOperationException( "this doesn't do anything" );
	}

	/**
	 * Gets algorithm name - in this case, "Loop Identifier."
	 *
	 * @return String	Name of algorithm.
	 */
	@Override
	public String getAlgoName() {
		return "Loop Identifier";
	}
}
