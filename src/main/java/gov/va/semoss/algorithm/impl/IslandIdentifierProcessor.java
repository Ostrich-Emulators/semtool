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
import java.util.Hashtable;
import java.util.Iterator;

import gov.va.semoss.algorithm.api.IAlgorithm;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.transformer.ArrowDrawPaintTransformer;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.VertexPaintTransformer;
import gov.va.semoss.util.Constants;
import edu.uci.ics.jung.graph.DelegateForest;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;

/**
 * This class is used to identify islands in the network.
 */
public class IslandIdentifierProcessor extends AbstractAction implements IAlgorithm {

	private final DelegateForest<SEMOSSVertex, SEMOSSEdge> forest;
	private final List<SEMOSSVertex> selectedVerts = new ArrayList<>();
	private final GraphPlaySheet playSheet;
	public Hashtable masterHash = new Hashtable();//this will have key: node, object: hashtable with verts.  Also key: node + edgeHashKey and object: hastable with edges
	String selectedNodes = "";
	List<SEMOSSEdge> masterEdgeVector = new ArrayList<>();//keeps track of everything accounted for in the forest
	List<SEMOSSVertex> masterVertexVector = new ArrayList<>();
	Set<SEMOSSVertex> islandVerts = new HashSet<>();
	Set<SEMOSSEdge> islandEdges = new HashSet<>();
	String edgeHashKey = "EdgeHashKey";

	public IslandIdentifierProcessor( GraphPlaySheet gps, Collection<SEMOSSVertex> pickedV ) {
		super( "Island Identifier" );
		playSheet = gps;
		forest = playSheet.getForest();
		selectedVerts.addAll( pickedV );
	}

	@Override
	public void execute() {
		actionPerformed( null );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Collection<SEMOSSVertex> forestRoots = new ArrayList();
		if ( !selectedVerts.isEmpty() ) {
			int count = 0;
			for ( SEMOSSVertex selectedVert : selectedVerts ) {
				if ( masterVertexVector.contains( selectedVert ) ) {
					forestRoots.add( selectedVert );
					if ( count > 0 ) {
						selectedNodes = selectedNodes + ", ";
					}
					selectedNodes = selectedNodes + selectedVert.getProperty( Constants.VERTEX_NAME );
					masterVertexVector.remove( selectedVert );
					addNetworkToMasterHash( selectedVert );
					count++;
				}
			}
			addRemainingToIslandHash( masterEdgeVector, masterVertexVector );
		}
		else {
			selectedNodes = "All";
			forestRoots = forest.getRoots();
			for ( SEMOSSVertex forestVert : forestRoots ) {
				if ( masterVertexVector.contains( forestVert ) ) {
					masterVertexVector.remove( forestVert );
					addNetworkToMasterHash( forestVert );
				}
			}
			differentiateMainlandFromIslands();
		}
		setTransformers();
	}

	/**
	 * Iterate through the master hash, get values from the keys and put it into
	 * the vertHash to determine the mainland and islands Set up new hashtables
	 * for the islands.
	 */
	private void differentiateMainlandFromIslands() {
		Iterator<String> masterHashIt = masterHash.keySet().iterator();
		String mainlandKey = "";
		int mainlandSize = 0;
		while ( masterHashIt.hasNext() ) {
			String key = masterHashIt.next();
			if ( !key.contains( edgeHashKey ) ) {
				Hashtable vertHash = (Hashtable) masterHash.get( key );
				int vertHashSize = vertHash.size();
				if ( vertHashSize > mainlandSize ) {
					mainlandSize = vertHashSize;
					mainlandKey = key;
				}
			}
		}
		//now we know the mainland and therefore the islands.  Time to set the island hastables

		masterHashIt = masterHash.keySet().iterator();
		while ( masterHashIt.hasNext() ) {
			String key = masterHashIt.next();
			if ( !key.contains( mainlandKey ) ) {
				if ( key.contains( edgeHashKey ) ) {
					Collection<SEMOSSEdge> islandEdgeHash
							= (Collection<SEMOSSEdge>) masterHash.get( key );
					islandEdges.addAll( islandEdgeHash );
				}
				else {
					Collection<SEMOSSVertex> islandVertHash
							= (Collection<SEMOSSVertex>) masterHash.get( key );
					islandVerts.addAll( islandVertHash );
				}
			}
		}
	}

	/**
	 * Sets the transformers based on valid edges and vertices for the playsheet.
	 */
	private void setTransformers() {
		EdgeStrokeTransformer tx = (EdgeStrokeTransformer) playSheet.getView().getRenderContext().getEdgeStrokeTransformer();
		tx.setSelectedEdges( islandEdges );
		
		ArrowDrawPaintTransformer atx = (ArrowDrawPaintTransformer) playSheet.getView().getRenderContext().getArrowDrawPaintTransformer();
		atx.setEdges( islandEdges );
		VertexPaintTransformer vtx = (VertexPaintTransformer) playSheet.getView().getRenderContext().getVertexFillPaintTransformer();
		vtx.setSelectedVertices( islandVerts );
		LabelFontTransformer vlft = (LabelFontTransformer) playSheet.getView().getRenderContext().getVertexFontTransformer();
		vlft.setSelected( islandVerts );
		// repaint it
		playSheet.getView().repaint();
	}

	/**
	 * Method addRemainingToIslandHash.
	 *
	 * @param masterEdges Vector<DBCMEdge>
	 * @param masterVerts Vector<DBCMVertex>
	 */
	private void addRemainingToIslandHash( Collection<SEMOSSEdge> masterEdges,
			Collection<SEMOSSVertex> masterVerts ) {
		islandVerts.addAll( masterVerts );
		islandEdges.addAll( masterEdges );
	}

	/**
	 * Add network to the master hash based on the current nodes and future set of
	 * nodes to traverse downward from.
	 *
	 * @param vertex DBCMVertex	Passed node.
	 */
	public void addNetworkToMasterHash( SEMOSSVertex vertex ) {
		String vertexName = vertex.getLabel();

		//use current nodes as the next set of nodes that I will have to traverse downward from.  Starts with passed node
		List<SEMOSSVertex> currentNodes = new ArrayList<>();
		currentNodes.add( vertex );

		Set<SEMOSSVertex> islandHash = new HashSet<>();
		islandHash.add( vertex );
		Set<SEMOSSEdge> islandEdgeHash = new HashSet<>();
		//use next nodes as the future set of nodes to traverse down from.
		List<SEMOSSVertex> nextNodes = new ArrayList<>();

		int nodeIndex = 0;
		int levelIndex = 1;
		while ( !nextNodes.isEmpty() || levelIndex == 1 ) {
			nextNodes.clear();
			while ( !currentNodes.isEmpty() ) {
				nodeIndex = 0;
				SEMOSSVertex vert = currentNodes.remove( nodeIndex );

				Collection<SEMOSSVertex> subsetNextNodes
						= traverseOutward( vert, islandHash, islandEdgeHash, vertexName );

				nextNodes.addAll( subsetNextNodes );

				nodeIndex++;
			}
			currentNodes.addAll( nextNodes );

			levelIndex++;
		}
	}

	/**
	 * Gets nodes upstream and downstream, adds all edges.
	 *
	 * @param vert DBCMVertex	Passed node.
	 * @param vertNetworkHash Hashtable	Hashtable of the nodes in the network.
	 * @param edgeNetworkHash Hashtable	Hashtable of the edges in the network.
	 * @param vertKey String	Key of the vert hashtable.
	 *
	 * @return ArrayList<DBCMVertex>	List of nodes from traversing.
	 */
	public List<SEMOSSVertex> traverseOutward( SEMOSSVertex vert,
			Set<SEMOSSVertex> vertNetworkHash, Set<SEMOSSEdge> edgeNetworkHash,
			String vertKey ) {
		//get nodes downstream
		List<SEMOSSVertex> vertArray = new ArrayList<>();
		Collection<SEMOSSEdge> edgeArray = forest.getOutEdges( vert );
		//add all edges
		edgeNetworkHash.addAll( edgeArray );

		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			if ( masterVertexVector.contains( inVert ) ) {
				vertArray.add( inVert );//this is going to be the returned array, so this is all set

				vertNetworkHash.add( inVert );
				removeAllEdgesAssociatedWithNode( inVert );
			}
		}
		//get nodes upstream
		Collection<SEMOSSEdge> inEdgeArray = forest.getInEdges( vert );
		edgeNetworkHash.addAll( inEdgeArray );

		for ( SEMOSSEdge edge : inEdgeArray ) {
			SEMOSSVertex outVert = edge.getOutVertex();
			if ( masterVertexVector.contains( outVert ) ) {
				vertArray.add( outVert );//this is going to be the returned array, so this is all set

				vertNetworkHash.add( outVert );
				edgeNetworkHash.add( edge );
				removeAllEdgesAssociatedWithNode( outVert );
			}
		}

		masterHash.put( vertKey, vertNetworkHash );
		masterHash.put( vertKey + edgeHashKey, edgeNetworkHash );
		return vertArray;
	}

	/**
	 * Given a specific node, it removes the vertex itself in addition to the
	 * upstream/downstream edges.
	 *
	 * @param vert DBCMVertex	Passed node.
	 */
	private void removeAllEdgesAssociatedWithNode( SEMOSSVertex vert ) {
		//remove vertex
		masterVertexVector.remove( vert );

		//remove downstream edges
		masterEdgeVector.removeAll( forest.getOutEdges( vert ) );

		//remove upstream edges
		masterEdgeVector.removeAll( forest.getInEdges( vert ) );
	}

	/**
	 * Sets playsheet as a graph play sheet.
	 *
	 * @param ps IPlaySheet	Playsheet to be cast.
	 */
	@Override
	public void setPlaySheet( IPlaySheet ps ) {
		throw new UnsupportedOperationException( "set the playsheet in the constructor" );
		// playSheet = ( (GraphPlaySheet) ps ).getGraphComponent();
	}

	/**
	 * Gets variables.
	 *
	 * //TODO: Return empty object instead of null
	 *
	 * @return String[]	List of variable names as strings.
	 */
	@Override
	public String[] getVariables() {
		throw new UnsupportedOperationException( "don't know what this is" );
	}

	/**
	 * Get algorithm name - in this case, "Island Identifier."
	 *
	 * @return String	Name of algorithm.
	 */
	@Override
	public String getAlgoName() {
		return "Island Identifier";
	}

}
