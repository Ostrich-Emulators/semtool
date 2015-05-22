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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import gov.va.semoss.om.SEMOSSVertex;

/**
 * Transforms the size and shape of selected nodes.
 */
public class VertexShapeTransformer implements Transformer<SEMOSSVertex, Shape> {

	private static final double INITIAL_SCALE = 1;
	private static final double SIZE_DELTA = .5;
	private static final double maxSize = 100.0;
	private static final double minSize = 0.0;

	private final Map<SEMOSSVertex, Double> vertSizeHash = new HashMap<>();
	private final Map<SEMOSSVertex, Double> vertSelectionHash = new HashMap<>();
	private double currentDefaultScale = INITIAL_SCALE;

	/**
	 * Gets the default scale
	 *
	 * @return
	 */
	public double getDefaultScale() {
		return currentDefaultScale;
	}

	/**
	 * Method setVertexSizeHash. Sets the local vertex size hash
	 *
	 * @param sizes
	 */
	public void setVertexSizeHash( Map<SEMOSSVertex, Double> sizes ) {
		vertSizeHash.clear();
		if ( null != sizes ) {
			vertSizeHash.putAll( sizes );
		}
	}

	/**
	 * Method setSelected. Keeps track of the changes that need to be undone.
	 *
	 * @param nodeURI String
	 */
	public void setSelected( SEMOSSVertex nodeURI ) {
		double selectedSize = SIZE_DELTA;
		if ( vertSelectionHash.containsKey( nodeURI ) ) {
			selectedSize = vertSelectionHash.get( nodeURI ) - SIZE_DELTA;
		}
		vertSelectionHash.put( nodeURI, selectedSize );
	}

	/**
	 * Method emptySelected. Clears the selection hashtable of all selections.
	 */
	public void emptySelected() {
		vertSelectionHash.clear();
	}

	/**
	 * Method increaseSize. Increases the size of a selected node.
	 *
	 * @param nodeURI String - the URI of the node to be increased.
	 */
	public void increaseSize( SEMOSSVertex nodeURI ) {
		if ( vertSizeHash.containsKey( nodeURI ) ) {
			double size = vertSizeHash.get( nodeURI );
			if ( size < maxSize ) {
				size = size + SIZE_DELTA;
			}
			vertSizeHash.put( nodeURI, size );
		}
		else {
			double size = currentDefaultScale;
			if ( size < maxSize ) {
				size = size + SIZE_DELTA;
			}
			vertSizeHash.put( nodeURI, size );
		}
	}

	/**
	 * Method decreaseSize. Decreases the size of a selected node.
	 *
	 * @param nodeURI String - the URI of the node to be decreased.
	 */
	public void decreaseSize( SEMOSSVertex nodeURI ) {
		if ( vertSizeHash.containsKey( nodeURI ) ) {
			double size = vertSizeHash.get( nodeURI );
			if ( size > minSize ) {
				size = size - SIZE_DELTA;
			}
			vertSizeHash.put( nodeURI, size );
		}
		else {
			double size = currentDefaultScale;
			if ( size > minSize ) {
				size = size - SIZE_DELTA;
			}
			vertSizeHash.put( nodeURI, size );
		}
	}

	/**
	 * Method increaseSize. Increases the size of all the nodes on a graph.
	 */
	public void increaseSize() {
		// Increase everything that does not have a size specified
		if ( currentDefaultScale < maxSize ) {
			currentDefaultScale = currentDefaultScale + SIZE_DELTA;
		}

		// Increase everything with a size specified
		for ( Map.Entry<SEMOSSVertex, Double> en : vertSizeHash.entrySet() ) {
			Double size = en.getValue();
			if ( size < maxSize ) {
				en.setValue( size + SIZE_DELTA );
			}
		}
	}

	/**
	 * Method decreaseSize. Decreases the size of all the nodes on a graph.
	 */
	public void decreaseSize() {
		// Decrease everything that does not have a size specified
		if ( currentDefaultScale > minSize ) {
			currentDefaultScale = currentDefaultScale - SIZE_DELTA;
		}

		for ( Map.Entry<SEMOSSVertex, Double> en : vertSizeHash.entrySet() ) {
			Double size = en.getValue();
			if ( size > minSize ) {
				en.setValue( size - SIZE_DELTA );
			}
		}
	}

	/**
	 * Method transform. Get the DI Helper to find what is needed to get for
	 * vertex
	 *
	 * @param vertex DBCMVertex - The edge of which this returns the properties.
	 *
	 * @return Shape - The name of the new shape.
	 */
	@Override
	public Shape transform( SEMOSSVertex vertex ) {
		// only need to tranform if uri is contained in hash or current size
		// does not equal default size
		if ( !vertSizeHash.containsKey( vertex )
				&& currentDefaultScale == INITIAL_SCALE
				&& !vertSelectionHash.containsKey( vertex ) ) {
			return vertex.getShape();
		}

		double customScale = currentDefaultScale;
		if ( vertSizeHash.containsKey( vertex ) ) {
			customScale = vertSizeHash.get( vertex );
		}

		if ( vertSelectionHash.containsKey( vertex ) ) {
			customScale = customScale + vertSelectionHash.get( vertex );
		}

		return AffineTransform.getScaleInstance( customScale, customScale )
				.createTransformedShape( vertex.getShape() );
	}
}
