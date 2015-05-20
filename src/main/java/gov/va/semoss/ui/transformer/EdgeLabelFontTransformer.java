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

import java.awt.Font;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.util.Constants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Transforms the font label on an edge in the graph.
 */
public class EdgeLabelFontTransformer implements Transformer<SEMOSSEdge, Font> {

	private static final Logger logger = Logger.getLogger( EdgeLabelFontTransformer.class );
	Set<SEMOSSEdge> edgeURI2Show = new HashSet<>();
	int currentDefaultSize;
	int maxSize = 55;
	int minSize = 0;
	//This stores all font size data about the nodes.  Different than verticeURI2Show because need to remember size information when vertex label is unhidden
	Map<SEMOSSEdge, Integer> edgeSizeData = new HashMap<>();

	/**
	 * Constructor for EdgeLabelFontTransformer.
	 */
	public EdgeLabelFontTransformer() {
		currentDefaultSize = Constants.INITIAL_GRAPH_FONT_SIZE;
	}

	/**
	 * Method getFontSizeData. Gets the hashtable of node font size data.
	 *
	 * @return Hashtable
	 */
	public Map<SEMOSSEdge, Integer> getFontSizeData() {
		return edgeSizeData;
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
	public void setVertHash( Set<SEMOSSEdge> eds ) {
		this.edgeURI2Show.clear();
		if ( null != eds ) {
			edgeURI2Show.addAll( eds );
		}
	}

	/**
	 * Method clearSizeData. Clears all the font size data from the size
	 * hashtable.
	 */
	public void clearSizeData() {
		edgeSizeData.clear();
		currentDefaultSize = Constants.INITIAL_GRAPH_FONT_SIZE;
	}

	/**
	 * Method increaseFontSize. Increases the font size for all the nodes in the
	 * graph.
	 */
	public void increaseFontSize() {
		if ( currentDefaultSize < maxSize ) {
			currentDefaultSize++;
		}

		for ( Map.Entry<SEMOSSEdge, Integer> en : edgeSizeData.entrySet() ) {
			int size = en.getValue();
			if ( size < maxSize ) {
				en.setValue( size + 1 );
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

		for ( Map.Entry<SEMOSSEdge, Integer> en : edgeSizeData.entrySet() ) {
			int size = en.getValue();
			if ( size < maxSize ) {
				en.setValue( size - 1 );
			}
		}
	}

	/**
	 * Method increaseFontSize. Increases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void increaseFontSize( SEMOSSEdge nodeURI ) {
		if ( edgeSizeData.containsKey( nodeURI ) ) {
			int size = edgeSizeData.get( nodeURI );
			if ( size < maxSize ) {
				size = size + 1;
			}
			edgeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size < maxSize ) {
				size = size + 1;
			}
			edgeSizeData.put( nodeURI, size );
		}
	}

	/**
	 * Method decreaseFontSize. Decreases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void decreaseFontSize( SEMOSSEdge nodeURI ) {
		if ( edgeSizeData.containsKey( nodeURI ) ) {
			int size = edgeSizeData.get( nodeURI );
			if ( size > minSize ) {
				size = size - 1;
			}
			edgeSizeData.put( nodeURI, size );
		}
		else {
			int size = currentDefaultSize;
			if ( size > minSize ) {
				size = size - 1;
			}
			edgeSizeData.put( nodeURI, size );
		}
	}

	/**
	 * Method transform. Transforms the label on an edge in the graph
	 *
	 * @param arg0 DBCMEdge - the edge to be transformed
	 *
	 * @return Font - the font of the edge
	 */
	@Override
	public Font transform( SEMOSSEdge arg0 ) {
		int customSize = ( edgeSizeData.containsKey( arg0 )
				? edgeSizeData.get( arg0 ) : currentDefaultSize );

		Font font = new Font( "Plain", Font.PLAIN, customSize );

		if ( edgeURI2Show.contains( arg0 ) ) {
			font = new Font( "Plain", Font.PLAIN, customSize );
		}
		else {
			font = new Font( "Plain", Font.PLAIN, 0 );
		}
		return font;
	}
}
