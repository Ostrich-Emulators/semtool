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
import gov.va.semoss.ui.components.GridFilterData;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.util.Constants;
import edu.uci.ics.jung.graph.DelegateForest;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * This class uses the information from DistanceDownstreamInserter in order to
 * actually perform the distance downstream calculation.
 */
public class DistanceDownstreamProcessor extends AbstractAction implements IAlgorithm {
	private static final long serialVersionUID = 3191222375480129585L;
	public static final URI WEIGHT = new URIImpl( "semoss://weight" );
	private static final Logger log
			= Logger.getLogger( DistanceDownstreamProcessor.class );
	protected DelegateForest forest = null;
	protected ArrayList<SEMOSSVertex> selectedVerts = new ArrayList<>();
	GridFilterData gfd = new GridFilterData();
	protected GraphPlaySheet playSheet;
	public Hashtable masterHash = new Hashtable();
	public String distanceString = "Distance";
	public String pathString = "vertexPath";
	public String edgePathString = "edgePathString";
	public String leafString = "leafString";
	String selectedNodes = "";
	protected ArrayList<SEMOSSVertex> nextNodes = new ArrayList<>();

	public DistanceDownstreamProcessor( GraphPlaySheet gps, SEMOSSVertex[] verts ) {
		super( "Distance Downstream" );

		playSheet = gps;
		forest = gps.getForest();		
		setSelectedNodes( verts );
	}

	public DistanceDownstreamProcessor( DelegateForest df ) {
		forest = df;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		execute();
		setGridFilterData();
		createTab();
	}

	/**
	 * Performs downstream processing.
	 */
	@Override
	public void execute() {
		ArrayList<SEMOSSVertex> currentNodes = setRoots();
		performDownstreamProcessing( currentNodes );
	}

	/**
	 * Starts with a hashtable of all the roots and moves downward without ever
	 * touching the same node twice to set the roots.
	 *
	 * @return ArrayList<DBCMVertex> List of roots.
	 */
	protected ArrayList<SEMOSSVertex> setRoots() {
		//use current nodes as the next set of nodes that I will have to traverse downward from.  Starts with root nodes
		ArrayList<SEMOSSVertex> currentNodes = new ArrayList<SEMOSSVertex>();
		//as we go, put in masterHash with vertHash.  vertHash has distance and path with the key being the actual vertex
		if ( selectedVerts.size() != 0 ) {
			int count = 0;
			for ( SEMOSSVertex selectedVert : selectedVerts ) {
				if ( count > 0 ) {
					selectedNodes = selectedNodes + ", ";
				}
				selectedNodes = selectedNodes + selectedVert.getProperty( Constants.VERTEX_NAME );
				currentNodes.add( selectedVerts.indexOf( selectedVert ), selectedVert );
				count++;
			}
		}
		else {
			selectedNodes = "All";
			currentNodes.addAll( forest.getRoots() );
		}

		//start with the root nodes in the masterHash
		for ( SEMOSSVertex vert : currentNodes ) {
			Hashtable vertHash = new Hashtable();
			ArrayList<SEMOSSVertex> path = new ArrayList<SEMOSSVertex>();
			ArrayList<SEMOSSVertex> edgePath = new ArrayList<SEMOSSVertex>();
			path.add( vert );
			vertHash.put( distanceString, 0 );
			vertHash.put( pathString, path );
			vertHash.put( edgePathString, edgePath );
			masterHash.put( vert, vertHash );
		}
		return currentNodes;
	}

	/**
	 * Performs the downstream processing by using the current nodes as the future
	 * set of nodes to traverse down from.
	 *
	 * @param currentNodes ArrayList<DBCMVertex>	List of current nodes.
	 */
	protected void performDownstreamProcessing( ArrayList<SEMOSSVertex> currentNodes ) {
		int nodeIndex = 0;
		int levelIndex = 1;
		while ( !nextNodes.isEmpty() || levelIndex == 1 ) {
			nextNodes.clear();

			while ( !currentNodes.isEmpty() ) {
				nodeIndex = 0;
				SEMOSSVertex vert = currentNodes.remove( nodeIndex );

				Hashtable vertHash = (Hashtable) masterHash.get( vert );
				ArrayList<SEMOSSVertex> parentPath = (ArrayList<SEMOSSVertex>) vertHash.get( pathString );
				ArrayList<SEMOSSEdge> parentEdgePath = (ArrayList<SEMOSSEdge>) vertHash.get( edgePathString );

				ArrayList<SEMOSSVertex> subsetNextNodes = traverseDownward( vert, levelIndex, parentPath, parentEdgePath );

				nextNodes.addAll( subsetNextNodes );

				nodeIndex++;
			}
			currentNodes.addAll( nextNodes );

			levelIndex++;
		}
	}

	/**
	 * Traverses downward from the nodes.
	 *
	 * @param vert DBCMVertex	A single vertex.
	 * @param levelIndex int	Level index.
	 * @param parentPath ArrayList<DBCMVertex>	List of path distances.
	 * @param parentEdgePath ArrayList<DBCMEdge>	List of edge paths.
	 *
	 * @return ArrayList<DBCMVertex> Vert array. Used to calculate network value
	 * in DistanceDownstreamInserter.
	 */
	public ArrayList<SEMOSSVertex> traverseDownward( SEMOSSVertex vert, int levelIndex, ArrayList<SEMOSSVertex> parentPath, ArrayList<SEMOSSEdge> parentEdgePath ) {
		ArrayList<SEMOSSVertex> vertArray = new ArrayList<>();
		Collection<SEMOSSEdge> edgeArray = forest.getOutEdges( vert );
		for ( SEMOSSEdge edge : edgeArray ) {
			SEMOSSVertex inVert = edge.getInVertex();
			if ( !masterHash.containsKey( inVert ) ) {
				vertArray.add( inVert );//this is going to be the returned array, so this is all set

				//now I have to add this new vertex to masterHash.  This requires using the vertHash of the parent child to get path
				Hashtable vertHash = new Hashtable();
				ArrayList<SEMOSSVertex> newPath = new ArrayList<>();
				ArrayList<SEMOSSEdge> newEdgePath = new ArrayList<>();
				newPath.addAll( parentPath );
				newEdgePath.addAll( parentEdgePath );
				newPath.add( inVert );
				newEdgePath.add( edge );
				vertHash.put( distanceString, levelIndex );
				vertHash.put( pathString, newPath );
				vertHash.put( edgePathString, newEdgePath );
				masterHash.put( inVert, vertHash );
			}
		}

		//if the vertArray is null, I'm going to add a key saying that it is a leaf of the tree
		if ( vertArray.isEmpty() ) {
			Hashtable parentHash = (Hashtable) masterHash.get( vert );
			parentHash.put( leafString, "Leaf" );
		}

		return vertArray;
	}

	/**
	 * Sets the forest.
	 *
	 * @param f DelegateForest	Forest that is set.
	 */
	public void setForest( DelegateForest f ) {
		forest = f;
	}

	/**
	 * Sets selected nodes.
	 *
	 * @param pickedVertices DBCMVertex[]	Array of picked vertices to be set.
	 */
	public void setSelectedNodes( SEMOSSVertex[] pickedVertices ) {
		for ( int idx = 0; idx < pickedVertices.length; idx++ ) {
			selectedVerts.add( pickedVertices[idx] );
		}
	}

	/**
	 * Iterates through the roots and adds them to the array list of selected
	 * vertices.
	 */
	public void setRootNodesAsSelected() {
		Collection roots = forest.getRoots();
		Iterator<SEMOSSVertex> rootsIt = roots.iterator();
		while ( rootsIt.hasNext() ) {
			selectedVerts.add( rootsIt.next() );
		}
	}

	/**
	 * Determines whether a selected node will be added.
	 *
	 * @param pickedVertex String	Object name as a string
	 * @param position int	If 0, have the chance to traverse from data object
	 * directly
	 *
	 * @return boolean	True if the picked vertex matches
	 */
	public boolean addSelectedNode( String pickedVertex, int position ) {
		Collection<SEMOSSVertex> vertices = forest.getVertices();
		for ( SEMOSSVertex vert : vertices ) {
			if ( pickedVertex.equals( vert.getURI() ) ) {
				selectedVerts.add( position, vert );
				log.debug( "SET VERT..................." + vert.getURI() + " to position "
						+ position );
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets playsheet as a graph play sheet.
	 *
	 * @param ps IPlaySheet	Playsheet that will be cast.
	 */
	@Override
	public void setPlaySheet( IPlaySheet ps ) {
		throw new UnsupportedOperationException( "this function is not not necessary" );
		//GraphPlaySheet gps = GraphPlaySheetFrame.class.cast( ps );
		//setPlaySheet( gps.getGraphComponent() );
	}

	public void setPlaySheet( GraphPlaySheet ps ) {
		playSheet = ps;
	}

	/**
	 * Creates new tab on GraphPlaySheetFrame.
	 */
	public void createTab() {
		List<String> heads = new ArrayList<>();
		List<Value[]> data = new ArrayList<>();
		int cols = gfd.getColumnCount();
		for ( int i = 0; i < cols; i++ ) {
			heads.add( gfd.getColumnName( i ) );
		}

		// the objects in a row are string, string, int, string, (optional)double
		Map<Integer, Class<?>> classes = new HashMap<>();
		classes.put( 0, String.class );
		classes.put( 1, String.class );
		classes.put( 2, Integer.class );
		classes.put( 3, String.class );
		classes.put( 4, Double.class );

		ValueFactory vf = new ValueFactoryImpl();
		for ( Object[] row : gfd.getDataList() ) {
			Value[] vrow = new Value[cols];
			for ( int i = 0; i < cols; i++ ) {
				switch ( i ) {
					case 2:
						vrow[i] = vf.createLiteral( Integer.class.cast( row[i] ) );
						break;
					case 4:
						vrow[i] = vf.createLiteral( Double.class.cast( row[i] ) );
						break;
					default:
						vrow[i] = vf.createLiteral( String.class.cast( row[i] ) );
				}
			}
			data.add( vrow );
		}

		GridRAWPlaySheet grid = new GridRAWPlaySheet();
		grid.create( data, heads, playSheet.getPlaySheetFrame().getEngine() );
		playSheet.getPlaySheetFrame().addTab( "Hops Downstream From " + selectedNodes,
				grid );
	}

	/**
	 * Iterates through master hashtable to get vertex name, vertex type, hops,
	 * and root node. If the edge path has a weight, then add "multiplied weight"
	 * as a column name for the grid filter.
	 */
	public void setGridFilterData() {
		String[] colNames = new String[5];
		colNames[0] = "Vertex Name";
		colNames[1] = "Vertex Type";
		colNames[2] = "Hops";
		colNames[3] = "Root Node";

		//use masterHash to fill tableList and gfd
		ArrayList<Object[]> tableList = new ArrayList();
		Iterator masterIt = masterHash.keySet().iterator();

		boolean weightCheck = false;
		while ( masterIt.hasNext() ) {
			SEMOSSVertex vertex = (SEMOSSVertex) masterIt.next();
			Hashtable vertHash = (Hashtable) masterHash.get( vertex );

			int dist = (Integer) vertHash.get( distanceString );
			ArrayList path = (ArrayList) vertHash.get( pathString );
			String root = (String) ( (SEMOSSVertex) path.get( 0 ) ).getProperty( Constants.VERTEX_NAME );

			Double multWeight = getMultipliedWeight( (ArrayList<SEMOSSEdge>) vertHash.get( edgePathString ) );
			Object[] rowArray = { vertex.getLabel(), vertex.getType(), dist, root, multWeight };
			tableList.add( rowArray );
			if ( multWeight > 0 ) {
				weightCheck = true;
			}
		}

		if ( weightCheck == true ) {
			colNames[4] = "Multiplied Weight";
		}
		else {
			colNames = new String[4];
			colNames[0] = "Vertex Name";
			colNames[1] = "Vertex Type";
			colNames[2] = "Hops";
			colNames[3] = "Root Node";
			//remove all weight columns
			tableList = removeColumn( tableList, 4 );
		}
		gfd.setColumnNames( colNames );

		gfd.setDataList( tableList );
	}

	/**
	 * Removes column.
	 *
	 * @param tableList ArrayList	Existing list of column names.
	 * @param column int	Number of columns that currently exist.
	 *
	 * @return ArrayList List with new column names.
	 */
	private ArrayList removeColumn( ArrayList tableList, int column ) {
		ArrayList newTableList = new ArrayList();
		for ( int i = 0; i < tableList.size(); i++ ) {
			Object[] row = (Object[]) tableList.get( i );
			Object[] newRow = new Object[row.length - 1];
			int count = 0;
			for ( int j = 0; j < row.length; j++ ) {
				if ( j != column ) {
					newRow[count] = row[j];
					count++;
				}
			}
			newTableList.add( newRow );
		}
		return newTableList;
	}

	/**
	 * Returns the multiplied weight of an edge.
	 *
	 * @param edgePath ArrayList<DBCMEdge>	List containing all the edges.
	 *
	 * @return Double Final edge weight.
	 */
	private Double getMultipliedWeight( ArrayList<SEMOSSEdge> edgePath ) {
		int count = 0;
		double total = 1.0;
		Iterator<SEMOSSEdge> edgeIt = edgePath.iterator();
		while ( edgeIt.hasNext() ) {
			SEMOSSEdge edge = edgeIt.next();
			if ( edge.getProperties().containsKey( WEIGHT ) ) {
				total = total * (Double) edge.getProperty( WEIGHT );
				count++;
			}
		}
		if ( count > 0 ) {
			return total;
		}
		return 0.0;
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
		if ( freqString.equalsIgnoreCase( "Real-time (user-initiated)" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (monthly)" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "Weekly" ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( Constants.TBD ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Monthly" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "Batch (daily)" ) ) {
			freqInt = 24;
		}
		if ( freqString.equalsIgnoreCase( "Batch(Daily)" ) ) {
			freqInt = 24;
		}
		if ( freqString.equalsIgnoreCase( "Real-time" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "n/a" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Transactional" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "On Demand" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Event Driven (seconds-minutes)" ) ) {
			freqInt = 60;
		}
		if ( freqString.equalsIgnoreCase( "TheaterFramework" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Event Driven (Seconds)" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Web services" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "TF" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (12/day)" ) ) {
			freqInt = 2;
		}
		if ( freqString.equalsIgnoreCase( "SFTP" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (twice monthly)" ) ) {
			freqInt = 360;
		}
		if ( freqString.equalsIgnoreCase( "Daily" ) ) {
			freqInt = 24;
		}
		if ( freqString.equalsIgnoreCase( "Hourly" ) ) {
			freqInt = 1;
		}
		if ( freqString.equalsIgnoreCase( "Near Real-time (transaction initiated)" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (three times a week)" ) ) {
			freqInt = 2;
		}
		if ( freqString.equalsIgnoreCase( "Batch (weekly)" ) ) {
			freqInt = 7;
		}
		if ( freqString.equalsIgnoreCase( "Near Real-time" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Real Time" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (bi-monthly)" ) ) {
			freqInt = 1440;
		}
		if ( freqString.equalsIgnoreCase( "Batch (semiannually)" ) ) {
			freqInt = 4392;
		}
		if ( freqString.equalsIgnoreCase( "Event Driven (Minutes-hours)" ) ) {
			freqInt = 1;
		}
		if ( freqString.equalsIgnoreCase( "Annually" ) ) {
			freqInt = 8760;
		}
		if ( freqString.equalsIgnoreCase( "Batch(Monthly)" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "Bi-Weekly" ) ) {
			freqInt = 336;
		}
		if ( freqString.equalsIgnoreCase( "Daily at end of day" ) ) {
			freqInt = 24;
		}
		if ( freqString.equalsIgnoreCase( "TCP" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "event-driven (Minutes-hours)" ) ) {
			freqInt = 1;
		}
		if ( freqString.equalsIgnoreCase( "Interactive" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Weekly Quarterly" ) ) {
			freqInt = 2184;
		}
		if ( freqString.equalsIgnoreCase( "Weekly Daily Weekly Weekly Weekly Weekly Daily Daily Daily" ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( "Weekly Daily" ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( "Periodic" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (4/day)" ) ) {
			freqInt = 6;
		}
		if ( freqString.equalsIgnoreCase( "Batch(Daily/Monthly)" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "Weekly; Interactive; Interactive" ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( "interactive" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch (quarterly)" ) ) {
			freqInt = 2184;
		}
		if ( freqString.equalsIgnoreCase( "Every 8 hours (KML)/On demand (HTML)" ) ) {
			freqInt = 8;
		}
		if ( freqString.equalsIgnoreCase( "Monthly at beginning of month, or as user initiated" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "On demad" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Monthly Bi-Monthly Weekly Weekly" ) ) {
			freqInt = 720;
		}
		if ( freqString.equalsIgnoreCase( "Quarterly" ) ) {
			freqInt = 2184;
		}
		if ( freqString.equalsIgnoreCase( "On-demand" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "user upload" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "1/hour (KML)/On demand (HTML)" ) ) {
			freqInt = 1;
		}
		if ( freqString.equalsIgnoreCase( "DVD" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Real-time " ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Weekly " ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( "Annual" ) ) {
			freqInt = 8760;
		}
		if ( freqString.equalsIgnoreCase( "Daily Interactive" ) ) {
			freqInt = 24;
		}
		if ( freqString.equalsIgnoreCase( "NFS, Oracle connection" ) ) {
			freqInt = 0;
		}
		if ( freqString.equalsIgnoreCase( "Batch(Weekly)" ) ) {
			freqInt = 168;
		}
		if ( freqString.equalsIgnoreCase( "Batch(Quarterly)" ) ) {
			freqInt = 2184;
		}
		if ( freqString.equalsIgnoreCase( "Batch (yearly)" ) ) {
			freqInt = 8760;
		}
		if ( freqString.equalsIgnoreCase( "Each user login instance" ) ) {
			freqInt = 0;
		}
		return freqInt;
	}

	/**
	 * Gets variables.
	 *
	 * //TODO: Return empty object instead of null
	 *
	 * @return String[] List of variable names stored in a string array.
	 */
	@Override
	public String[] getVariables() {

		return null;
	}

	/**
	 * Gets algorithm name - in this case, "Distance Downstream."
	 *
	 * @return String	Name of algorithm.
	 */
	@Override
	public String getAlgoName() {
		return "Distance Downstream";
	}

}
