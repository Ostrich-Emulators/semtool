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

import gov.va.semoss.util.Constants;
import java.util.Collection;

/**
 * Transforms the font label on a node vertex in the graph.
 */
public class LabelFontTransformer<T extends AbstractNodeEdgeBase> extends SelectingTransformer<T, Font> {

	private static final int DEFAULT_SIZE = Constants.INITIAL_GRAPH_FONT_SIZE;
	private static final int MAXSIZE = 55;
	private static final int MINSIZE = 0;

	private int normalSize = Constants.INITIAL_GRAPH_FONT_SIZE;
	//This stores all font size data about the nodes.  Different than 
	// verticeURI2Show because need to remember size information when vertex label is unhidden
	private final Map<T, Integer> nodeSizeData = new HashMap<>();

	private int unselectedSize = MINSIZE;
	private Font normal = new Font( "Plain", Font.PLAIN, DEFAULT_SIZE );
	private Font unsel = new Font( "Plain", Font.PLAIN, MINSIZE );

	public void setUnselectedSize( int sz ) {
		unselectedSize = sz;
		unsel = new Font( "Plain", Font.PLAIN, sz );
	}

	public int getUnselectedSize() {
		return unselectedSize;
	}

	/**
	 * Method getNormalFontSize. Retrieves the current default font size for the
	 * nodes.
	 *
	 * @return int - the current font size.
	 */
	public int getNormalFontSize() {
		return normalSize;
	}

	public void setNormalFontSize( int n ) {
		normalSize = n;
		normal = new Font( "Plain", Font.PLAIN, n );
	}

	/**
	 * Method clearSizeData. Clears all the font size data from the size
	 * hashtable.
	 */
	public void clearSizeData() {
		nodeSizeData.clear();
		normalSize = DEFAULT_SIZE;
	}

	/**
	 * Method increaseFontSize. Increases the font size for all the nodes in the
	 * graph.
	 */
	public void increaseFontSize() {
		if ( normalSize < MAXSIZE ) {
			setNormalFontSize( normalSize + 1 );
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
		if ( normalSize > MINSIZE ) {
			setNormalFontSize( normalSize - 1 );
		}

		for ( Map.Entry<T, Integer> entry : nodeSizeData.entrySet() ) {
			int size = entry.getValue();
			if ( size > MINSIZE ) {
				entry.setValue( size - 1 );
			}
		}
	}

	public void changeFontSize( int delta ) {
		int newnorm = normalSize + delta;
		if ( newnorm > MINSIZE && newnorm < MAXSIZE ) {
			setNormalFontSize( newnorm );

			for ( Map.Entry<T, Integer> entry : nodeSizeData.entrySet() ) {
				int size = entry.getValue() + delta;
				if ( size > MAXSIZE ) {
					size = MAXSIZE;
				}
				else if ( size < MINSIZE ) {
					size = MINSIZE;
				}

				entry.setValue( size );
			}
		}
	}

	public void changeFontSize( int delta, Collection<T> todo ) {
		for ( T t : todo ) {
			int newsize = delta + ( nodeSizeData.containsKey( t )
					? nodeSizeData.get( t ) : normalSize );
			if ( newsize > MAXSIZE ) {
				newsize = MAXSIZE;
			}
			else if ( newsize < MINSIZE ) {
				newsize = MINSIZE;
			}

			nodeSizeData.put( t, newsize );
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
			int size = normalSize;
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
			int size = normalSize;
			if ( size > MINSIZE ) {
				size = size - 1;
			}
			nodeSizeData.put( nodeURI, size );
		}
	}

	@Override
	protected Font transformNormal( T t ) {
		if ( nodeSizeData.containsKey( t ) ) {
			return new Font( "Plain", Font.PLAIN, nodeSizeData.get( t ) );
		}
		return normal;
	}

	@Override
	protected Font transformSelected( T t ) {
		int size = ( nodeSizeData.containsKey( t )
				? nodeSizeData.get( t ) : normalSize );
		return new Font( "Plain", Font.PLAIN, size );
	}

	@Override
	protected Font transformNotSelected( T t, boolean skel ) {
		return unsel;
	}
}
