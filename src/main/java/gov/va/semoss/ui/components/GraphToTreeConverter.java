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
package gov.va.semoss.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import gov.va.semoss.algorithm.impl.DistanceDownstreamProcessor;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import edu.uci.ics.jung.graph.DelegateForest;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class extends downstream processing in order to convert the graph into
 * the tree format.
 */
public class GraphToTreeConverter extends DistanceDownstreamProcessor {

	private static final Logger logger = Logger.getLogger( GraphToTreeConverter.class );

	Map<String, List<SEMOSSVertex>> uriVertHash = new HashMap<>();
	DelegateForest<SEMOSSVertex, SEMOSSEdge> newForest;

	/**
	 * Constructor for GraphToTreeConverter.
	 *
	 * @param p Graph playsheet to be set.
	 */
	public GraphToTreeConverter( GraphPlaySheet p ) {
		super( p, p.getView().getPickedVertexState().getPicked() );
	}

	/**
	 * Resets the hashtables containing URIs of the vertices and selected
	 * vertices.
	 */
	private void resetConverter() {
		newForest = new DelegateForest<>();
		masterHash.clear();
		uriVertHash.clear();
		selectedVerts.clear();
	}

	/**
	 * Resets the converters, sets the forest and selected nodes, performs
	 * downstream processing on the current nodes, and sets the forest.
	 */
	@Override
	public void execute() {
		resetConverter();
		List<SEMOSSVertex> currentNodes = setRoots();
		performDownstreamProcessing( currentNodes );
		playSheet.setForest( newForest );
	}

	/**
	 * This method is used to traverse downward from the nodes and ultimately
	 * create the tree.
	 *
	 * @param vert A single DBCM vertex.
	 * @param levelIndex Level index.
	 * @param parentPath List of path distances.
	 * @param parentEdgePath List of edge paths.
	 *
	 * @return ArrayList<DBCMVertex> Vert array, used to calculate network value.
	 */
	@Override
	public List<SEMOSSVertex> traverseDownward( SEMOSSVertex vert, int levelIndex,
			List<SEMOSSVertex> parentPath, List<SEMOSSEdge> parentEdgePath ) {
		List<SEMOSSVertex> vertArray = new ArrayList<>();
		Collection<SEMOSSEdge> edgeArray = forest.getOutEdges( vert );
		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			if ( !masterHash.containsKey( inVert ) ) {
				vertArray.add( inVert );//this is going to be the returned array, so this is all set
				addEdges( edge, vert, inVert );

				//now I have to add this new vertex to masterHash.  This requires using the vertHash of the parent child to get path
				Hashtable vertHash = new Hashtable();
				List<SEMOSSVertex> newPath = new ArrayList<>();
				List<SEMOSSEdge> newEdgePath = new ArrayList<>();
				newPath.addAll( parentPath );
				newEdgePath.addAll( parentEdgePath );
				newPath.add( inVert );
				newEdgePath.add( edge );
				vertHash.put(DISTANCE, levelIndex );
				vertHash.put(PATH, newPath );
				vertHash.put(EDGEPATH, newEdgePath );
				masterHash.put( inVert, vertHash );
			}
			//This is the key piece for creating a tree
			//if the node has already been added, but has been added on this level, I need to duplicate the node and add it
			else if ( masterHash.containsKey( inVert ) && nextNodes.contains( inVert ) ) {
				addEdges( edge, vert, inVert );

			}
		}

		//if the vertArray is null, I'm going to add a key saying that it is a leaf of the tree
		//this will be used in giving network value in distancedownstreaminserter
		if ( vertArray.isEmpty() ) {
			Hashtable parentHash = (Hashtable) masterHash.get( vert );
			parentHash.put(LEAF, "Leaf" );
		}

		return vertArray;
	}

	/**
	 * Adds edges to the new delegate forest.
	 *
	 * @param edge DBCM edge.
	 * @param vert DBCM vertex.
	 * @param inVert Node that has URI to be added to the URI vertex hash.
	 */
	private void addEdges( SEMOSSEdge edge, SEMOSSVertex vert, SEMOSSVertex inVert ) {
		//need to get all vertices that exist with this uri and create edge downward to new instances of this invert
		String uri = vert.getURI().stringValue();
		List<SEMOSSVertex> vertArray = uriVertHash.get( uri );
		if ( vertArray == null ) {
			SEMOSSEdge newEdge = new SEMOSSEdge( vert, inVert, edge.getURI() );
			newEdge.getProperties().putAll( edge.getProperties() );
			newForest.addEdge( newEdge, vert, inVert );
			addToURIVertHash( inVert );
		}
		else {
			for ( SEMOSSVertex vertex : vertArray ) {
				SEMOSSVertex newDownstreamVert = new SEMOSSVertex( inVert.getURI() );
				newDownstreamVert.getProperties().putAll( inVert.getProperties() );
				SEMOSSEdge newEdge = new SEMOSSEdge( vertex, newDownstreamVert, edge.getURI() );
				newEdge.getProperties().putAll( edge.getProperties() );
				newForest.addEdge( newEdge, vertex, newDownstreamVert );
				addToURIVertHash( newDownstreamVert );
			}
		}
	}

	/**
	 * Adds the URI of a particular node to the hashtable of URI vertices.
	 *
	 * @param vert DBCM vertex.
	 */
	private void addToURIVertHash( SEMOSSVertex vert ) {
		String uri = vert.getURI().stringValue();
		List<SEMOSSVertex> vertArray = null;
		if ( uriVertHash.containsKey( uri ) ) {
			vertArray = uriVertHash.get( uri );
			if ( vertArray.contains( vert ) ) {
				logger.warn( "Seems like we are adding the same vertex twice..." );
			}
			else {
				vertArray.add( vert );
			}
		}
		else {
			vertArray = new ArrayList();
			vertArray.add( vert );
		}
		uriVertHash.put( uri, vertArray );
	}
}
