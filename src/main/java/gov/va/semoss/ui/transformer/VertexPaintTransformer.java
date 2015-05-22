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

import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import gov.va.semoss.om.SEMOSSVertex;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms the color of vertices/nodes on the graph.
 */
public class VertexPaintTransformer implements Transformer<SEMOSSVertex, Paint> {

	private Set<SEMOSSVertex> verticeURI2Show = new HashSet<>();

	/**
	 * Constructor for VertexPaintTransformer.
	 */
	public VertexPaintTransformer() {

	}

	/**
	 * Method setSelectedVertices. Sets the Hashtable of vertices.
	 *
	 * @param verts Hashtable
	 */
	public void setSelectedVertices( Collection<SEMOSSVertex> verts ) {
		this.verticeURI2Show.clear();
		if ( null != verts ) {
			this.verticeURI2Show.addAll( verts );
		}
	}

	/**
	 * Method getSelectedVertices. Retreives the hashtable of vertices 	 *
	 * @return Hashtable
	 */
	public Collection<SEMOSSVertex> getSelectedVertices() {
		return verticeURI2Show;
	}

	/**
	 * Method transform. Get the DI Helper to find what is needed to get for
	 * vertex
	 *
	 * @param arg0 DBCMVertex - The edge of which this returns the properties.
	 *
	 * @return Paint - The type of Paint.
	 */
	@Override
	public Paint transform( SEMOSSVertex vertex ) {
		return vertex.getColor();
	}
}
