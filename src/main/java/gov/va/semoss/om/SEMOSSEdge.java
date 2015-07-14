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
public class SEMOSSEdge extends AbstractNodeEdgeBase implements Comparable<SEMOSSEdge> {
	/** The origin resource (Vertex) URI for this edge/relation */
	private final URI originVertexURI;
	/** The destination resource (Vertex) URI for this edge/relation */
	private final URI destinationVertexURI;
	/** Flag which signifies whether the vertices of this edge are ALL visible - for rendering purposes */
	private boolean verticesVisible;

	/**
	 * @param _outVertex
	 * @param _inVertex
	 * @param _uri Vertex1 (OutVertex) -------> Vertex2 (InVertex) (OutEdge)
	 * (InEdge)
	 */
	public SEMOSSEdge( SEMOSSVertex _outVertex, SEMOSSVertex _inVertex, URI _uri ) {
		super( _uri, null, _uri.getLocalName() );
		destinationVertexURI = _inVertex.getURI();
		originVertexURI = _outVertex.getURI();

		_inVertex.addInEdge( this );
		_outVertex.addOutEdge( this );
		setColor( Color.DARK_GRAY );
	}

	/**
	 * Get the resource/node, identified by its unique URI, 
	 * from which this relation/edge emanates 
	 * @return The unique URI of the origin resource/vertex
	 */
	public URI getOriginVertexURI() {
		return originVertexURI;
	}

	/**
	 * Get the resource/node identified by its unique URI,
	 * to which this relation/edge leads
	 * @return The unique URI of the destination resource/vertex
	 */
	public URI getDestinationVertexURI() {
		return destinationVertexURI;
	}

	public String getName() {
		return getLabel();
	}

	public void setName( String _name ) {
		setLabel( _name );
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode( this.originVertexURI );
		hash = 97 * hash + Objects.hashCode( this.destinationVertexURI );
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
		final SEMOSSEdge other = (SEMOSSEdge) obj;
		if ( !Objects.equals( this.originVertexURI, other.originVertexURI ) ) {
			return false;
		}
		if ( !Objects.equals( this.destinationVertexURI, other.destinationVertexURI ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo( SEMOSSEdge t ) {
		return toString().compareTo( t.toString() );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if ( null != destinationVertexURI ) {
			sb.append( destinationVertexURI );
		}

		if ( null != getURI() ) {
			sb.append( "->" ).append( getURI() );
		}

		if ( null != originVertexURI ) {
			sb.append( "->" ).append( originVertexURI );
		}

		return sb.toString();
	}

	/**
	 * Set whether all of the vertices for this edge are visible
	 * @param visible the visibility of this edge's vertices
	 */
	public void setVerticesVisible(boolean visible) {
		this.verticesVisible = visible;
	}
	
	/**
	 * Get whether all of the vertices for this edge are visible
	 * @return True if all vertices are visible, false otherwise
	 */
	public boolean getVerticesVisible(){
		return this.verticesVisible;
	}
}
