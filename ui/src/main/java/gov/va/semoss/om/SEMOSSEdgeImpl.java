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
package gov.va.semoss.om;

import java.awt.Color;

import java.util.Objects;
import org.openrdf.model.URI;

/**
 *
 * @author pkapaleeswaran Something that expresses the edge
 * @version $Revision: 1.0 $
 */
public class SEMOSSEdgeImpl extends AbstractGraphElement
		implements SEMOSSEdge, Comparable<SEMOSSEdge> {

	private final String uniqueifier;
	/**
	 * Flag which signifies whether the vertices of this edge are ALL visible -
	 * for rendering purposes
	 */
	private boolean verticesVisible;

	/**
	 * @param _outVertex
	 * @param _inVertex
	 * @param _uri Vertex1 (OutVertex) -------> Vertex2 (InVertex) (OutEdge)
	 * (InEdge)
	 */
	public SEMOSSEdgeImpl( SEMOSSVertex _outVertex, SEMOSSVertex _inVertex, URI _uri ) {
		super( _uri, null, _uri.getLocalName(), Color.DARK_GRAY );

		uniqueifier = ( null == _outVertex ? "" : _outVertex.getURI().stringValue() )
				+ ( null == _inVertex ? "" : _inVertex.getURI().stringValue() );
	}

	public SEMOSSEdgeImpl( URI _uri ) {
		super( _uri, null, _uri.getLocalName(), Color.DARK_GRAY );
		uniqueifier = "";
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode( uniqueifier );
		hash = 97 * hash + super.hashCode();
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final SEMOSSEdgeImpl other = (SEMOSSEdgeImpl) obj;
		if ( !Objects.equals( this.getURI(), other.getURI() ) ) {
			return false;
		}
		if ( !Objects.equals( uniqueifier, other.uniqueifier ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo( SEMOSSEdge t ) {
		return toString().compareTo( t.toString() );
	}

	/**
	 * Set whether all of the vertices for this edge are visible
	 *
	 * @param visible the visibility of this edge's vertices
	 */
	@Override
	public void setVerticesVisible( boolean visible ) {
		this.verticesVisible = visible;
	}

	/**
	 * Get whether all of the vertices for this edge are visible
	 *
	 * @return True if all vertices are visible, false otherwise
	 */
	@Override
	public boolean getVerticesVisible() {
		return this.verticesVisible;
	}
}
