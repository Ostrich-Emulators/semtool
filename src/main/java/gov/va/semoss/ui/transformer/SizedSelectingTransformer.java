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
import java.util.HashMap;
import java.util.Map;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Selecting Transformer that also tracks custom sizing information. For
 * example, different nodes might have differently-sized shapes; fonts might be
 * different between edges. Internally, size information is stored and
 * calculated as Doubles.
 *
 */
public abstract class SizedSelectingTransformer<T extends AbstractNodeEdgeBase, V>
		extends SelectingTransformer<T, V> {

	// This stores all data. it is separate from what is selected or not
	private final Map<T, Double> customSizeData = new HashMap<>();

	private final double DEFAULTSIZE;
	private final double MAXSIZE;
	private final double MINSIZE;
	private final double STEPSIZE;
	private double defaultSize;
	private double unselectedSize;

	protected SizedSelectingTransformer( double normal, double max, double min, double step ) {
		DEFAULTSIZE = normal;
		STEPSIZE = step;
		MAXSIZE = max;
		MINSIZE = min;
		defaultSize = normal;
	}

	public void setUnselectedSize( double sz ) {
		unselectedSize = sz;
	}

	public double getUnselectedSize() {
		return unselectedSize;
	}

	/**
	 * Method getDefaultSize. Retrieves the current default font size for the
	 * nodes.
	 *
	 * @return int - the current font size.
	 */
	public double getDefaultSize() {
		return defaultSize;
	}

	public void setDefaultSize( double n ) {
		defaultSize = n;
	}

	/**
	 * Clears all the custom size data
	 */
	public void clearSizeData() {
		customSizeData.clear();
		setDefaultSize( DEFAULTSIZE );
		setUnselectedSize( MINSIZE );
	}

	/**
	 * Method increaseSize. Increases the font size for all the nodes in the
	 * graph.
	 */
	public void increaseSize() {
		changeSize( STEPSIZE );
	}

	/**
	 * Method decreaseSize. Decreases the font size for all the nodes in the
	 * graph.
	 */
	public void decreaseSize() {
		changeSize( -STEPSIZE );
	}

	public void changeSize( double delta ) {
		double newnorm = defaultSize + delta;
		if ( newnorm >= MINSIZE && newnorm <= MAXSIZE ) {
			setDefaultSize( newnorm );

			Set<T> toremove = new HashSet<>();

			for ( Map.Entry<T, Double> entry : customSizeData.entrySet() ) {
				double size = entry.getValue() + delta;

				if ( size == DEFAULTSIZE ) {
					// remove elements that no longer have custom sizes
					toremove.add( entry.getKey() );
				}
				else {
					if ( size > MAXSIZE ) {
						size = MAXSIZE;
					}
					else if ( size < MINSIZE ) {
						size = MINSIZE;
					}

					entry.setValue( size );
				}
			}

			for ( T t : toremove ) {
				customSizeData.remove( t );
			}
		}
	}

	public void changeSize( double delta, Collection<T> todo ) {
		for ( T t : todo ) {
			double newsize = delta + ( customSizeData.containsKey( t )
					? customSizeData.get( t ) : defaultSize );

			if ( newsize == DEFAULTSIZE ) {
				// if we're no longer a custom size, remove it from our cache
				customSizeData.remove( t );
			}
			else {
				if ( newsize > MAXSIZE ) {
					newsize = MAXSIZE;
				}
				else if ( newsize < MINSIZE ) {
					newsize = MINSIZE;
				}

				customSizeData.put( t, newsize );
			}
		}
	}

	/**
	 * Method increaseSize. Increases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void increaseSize( T nodeURI ) {
		changeSize( STEPSIZE, Arrays.asList( nodeURI ) );
	}

	/**
	 * Method decreaseSize. Decreases the font size of a selected node on the
	 * graph.
	 *
	 * @param nodeURI String - the node URI of the selected node.
	 */
	public void decreaseSize( T nodeURI ) {
		changeSize( -STEPSIZE, Arrays.asList( nodeURI ) );
	}

	public void setSizeMap( Map<T, Double> map ) {
		customSizeData.clear();
		if ( null != map ) {
			customSizeData.putAll( map );
		}
	}

	@Override
	protected V transformNormal( T t ) {
		return getNormal( t, customSizeData.get( t ), defaultSize );
	}

	@Override
	protected V transformSelected( T t ) {
		return getSelected( t, customSizeData.get( t ), defaultSize );
	}

	/**
	 * @param t the thing to transform
	 * @param sz the new size info...if null, use default size
	 * @defaultSize the current default size
	 * @return
	 */
	protected abstract V getNormal( T t, Double sz, double defaultSize );

	/**
	 * @param t the thing to transform
	 * @param sz the new size info...if null, use default size
	 * @defaultSize the current default size
	 * @return
	 */
	protected abstract V getSelected( T t, Double sz, double defaultSize );
}
