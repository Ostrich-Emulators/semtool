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
package gov.va.semoss.ui.transformer;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import gov.va.semoss.util.Constants;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms the font label on a node vertex in the graph.
 */
public class LabelFontTransformer<T extends AbstractNodeEdgeBase> implements Transformer<T, Font> {

	private static final Logger logger
			= Logger.getLogger( LabelFontTransformer.class );
	private static final int DEFAULT_SIZE = Constants.INITIAL_GRAPH_FONT_SIZE;
	private static final int MAXSIZE = 55;
	private static final int MINSIZE = 0;

	private final Set<T> selecteds = new HashSet<>();
	private int currentDefaultSize = Constants.INITIAL_GRAPH_FONT_SIZE;
	//This stores all font size data about the nodes.  Different than 
	// verticeURI2Show because need to remember size information when vertex label is unhidden
	private final Map<T, Integer> nodeSizeData = new HashMap<>();

	private int unselectedSize = MINSIZE;

	/**
	 * Constructor for VertexLabelFontTransformer.
	 */
	public LabelFontTransformer() {
	}

	public void setUnselectedSize( int sz ) {
		unselectedSize = sz;
	}

	public int getUnselectedSize() {
		return unselectedSize;
	}

	/**
	 * Method getSelectedVertices. Gets the hashtable of vertices and URIs.
	 *
	 * @return Hashtable<String,Object>
	 */
	public Collection<T> getSelectedVertices() {
		return selecteds;
	}

	/**
	 * Method getCurrentFontSize. Retrieves the current default font size for the
	 * nodes.
	 *
	 * @return int - the current font size.
	 */
	public int getCurrentFontSize() {
		return currentDefaultSize;
	}

	/**
	 * Method setSelected. Sets the hashtable of all the vertices to show on a
	 * graph.
	 *
	 * @param verticeURI2Show Hashtable
	 */
	public void setSelected( Collection<T> verts ) {
		this.selecteds.clear();
		if ( null != verts ) {
			this.selecteds.addAll( verts );
		}
	}

	/**
	 * Method clearSizeData. Clears all the font size data from the size
	 * hashtable.
	 */
	public void clearSizeData() {
		nodeSizeData.clear();
		currentDefaultSize = DEFAULT_SIZE;
	}

	/**
	 * Method increaseFontSize. Increases the font size for all the nodes in the
	 * graph.
	 */
	public void increaseFontSize() {
		if ( currentDefaultSize < MAXSIZE ) {
			currentDefaultSize++;
		}
		for ( Map.Entry<T, Integer> entry : nodeSizeData.entrySet() ) {
			int size = entry.getValue();
			if ( size < MAXSIZE ) {
				entry.setValue( size + 1 );
			}
		}
	}

	/**
	 * Method decreaseFontSize. Decreases the font size for all the nodes in the
	 * graph.
	 */
	public void decreaseFontSize() {
		if ( currentDefaultSize > MINSIZE ) {
			currentDefaultSize--;
		}

		for ( Map.Entry<T, Integer> entry : nodeSizeData.entrySet() ) {
			int size = entry.getValue();
			if ( size > MINSIZE ) {
				entry.setValue( size - 1 );
			}
		}
	}

	/**
	 * Method increaseFontSize. Increases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void increaseFontSize( T nodeURI ) {
		if ( nodeSizeData.containsKey( nodeURI ) ) {
			int size = nodeSizeData.get( nodeURI );
			if ( size < MAXSIZE ) {
				size = size + 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size < MAXSIZE ) {
				size = size + 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
	}

	/**
	 * Method decreaseFontSize. Decreases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void decreaseFontSize( T nodeURI ) {
		if ( nodeSizeData.containsKey( nodeURI ) ) {
			int size = nodeSizeData.get( nodeURI );
			if ( size > MINSIZE ) {
				size = size - 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size > MINSIZE ) {
				size = size - 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
	}

	/**
	 * Method transform. Transforms the label on a node vertex in the graph
	 *
	 * @param arg0 DBCMVertex - the vertex to be transformed
	 *
	 * @return Font - the font of the vertex
	 */
	@Override
	public Font transform( T arg0 ) {
		if ( selecteds.isEmpty() ) {
			return new Font( "Plain", Font.PLAIN, currentDefaultSize );
		}
		else {
			int size = unselectedSize;
			if ( selecteds.contains( arg0 ) ) {
				size = ( nodeSizeData.containsKey( arg0 )
						? nodeSizeData.get( arg0 ) : currentDefaultSize );
			}

			return new Font( "Plain", Font.PLAIN, size );
		}
	}
}
