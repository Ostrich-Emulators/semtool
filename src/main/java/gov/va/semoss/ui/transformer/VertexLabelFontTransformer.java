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
package gov.va.semoss.ui.transformer;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;

/**
 * Transforms the font label on a node vertex in the graph.
 */
public class VertexLabelFontTransformer implements Transformer<SEMOSSVertex, Font> {

	Map<String, String> verticeURI2Show = null;
	private static final Logger logger
			= Logger.getLogger( VertexLabelFontTransformer.class );
	int initialDefaultSize = Constants.INITIAL_GRAPH_FONT_SIZE;
	int currentDefaultSize;
	int maxSize = 55;
	int minSize = 0;
	//This stores all font size data about the nodes.  Different than verticeURI2Show because need to remember size information when vertex label is unhidden
	Map<String, Integer> nodeSizeData;

	/**
	 * Constructor for VertexLabelFontTransformer.
	 */
	public VertexLabelFontTransformer() {
		nodeSizeData = new HashMap<>();
		currentDefaultSize = initialDefaultSize;
	}

	/**
	 * Method getVertHash. Gets the hashtable of vertices and URIs.
	 *
	 * @return Hashtable<String,Object>
	 */
	public Map<String, String> getVertHash() {
		return verticeURI2Show;
	}

	/**
	 * Method getFontSizeData. Gets the hashtable of node font size data.
	 *
	 * @return Hashtable
	 */
	public Map<String, Integer> getFontSizeData() {
		return nodeSizeData;
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
	 * Method setVertHash. Sets the hashtable of all the vertices to show on a
	 * graph.
	 *
	 * @param verticeURI2Show Hashtable
	 */
	public void setVertHash( Map<String, String> verticeURI2Show ) {
		this.verticeURI2Show = verticeURI2Show;
	}

	/**
	 * Method clearSizeData. Clears all the font size data from the size
	 * hashtable.
	 */
	public void clearSizeData() {
		nodeSizeData.clear();
		currentDefaultSize = initialDefaultSize;
	}

	/**
	 * Method increaseFontSize. Increases the font size for all the nodes in the
	 * graph.
	 */
	public void increaseFontSize() {
		if ( currentDefaultSize < maxSize ) {
			currentDefaultSize++;
		}
		for ( Map.Entry<String, Integer> entry : nodeSizeData.entrySet() ) {
			int size = entry.getValue();
			if ( size < maxSize ) {
				entry.setValue( size + 1 );
			}
		}
	}

	/**
	 * Method decreaseFontSize. Decreases the font size for all the nodes in the
	 * graph.
	 */
	public void decreaseFontSize() {
		if ( currentDefaultSize > minSize ) {
			currentDefaultSize--;
		}

		for ( Map.Entry<String, Integer> entry : nodeSizeData.entrySet() ) {
			int size = entry.getValue();
			if ( size > minSize ) {
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
	public void increaseFontSize( String nodeURI ) {
		if ( nodeSizeData.containsKey( nodeURI ) ) {
			int size = nodeSizeData.get( nodeURI );
			if ( size < maxSize ) {
				size = size + 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size < maxSize ) {
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
	public void decreaseFontSize( String nodeURI ) {
		if ( nodeSizeData.containsKey( nodeURI ) ) {
			int size = nodeSizeData.get( nodeURI );
			if ( size > minSize ) {
				size = size - 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size > minSize ) {
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
	public Font transform( SEMOSSVertex arg0 ) {
		int customSize = currentDefaultSize;
		if ( nodeSizeData.containsKey( arg0.getURI() ) ) {
			customSize = nodeSizeData.get( arg0.getURI() );
		}
		Font font = new Font( "Plain", Font.PLAIN, customSize );

		if ( verticeURI2Show != null ) {
			String URI = (String) arg0.getProperty( Constants.URI_KEY );
			logger.debug( "URI " + URI );
			if ( verticeURI2Show.containsKey( URI ) ) {
				font = new Font( "Plain", Font.PLAIN, customSize );
			}
			else {
				font = new Font( "Plain", Font.PLAIN, 0 );
			}
		}
		return font;
	}

}
