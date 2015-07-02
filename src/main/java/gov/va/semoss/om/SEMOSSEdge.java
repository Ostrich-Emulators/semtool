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

	private SEMOSSVertex inVertex;
	private SEMOSSVertex outVertex;

	/**
	 * @param _outVertex
	 * @param _inVertex
	 * @param _uri Vertex1 (OutVertex) -------> Vertex2 (InVertex) (OutEdge)
	 * (InEdge)
	 */
	public SEMOSSEdge( SEMOSSVertex _outVertex, SEMOSSVertex _inVertex, URI _uri ) {
		super( _uri, null, _uri.getLocalName() );
		inVertex = _inVertex;
		outVertex = _outVertex;

		inVertex.addInEdge( this );
		outVertex.addOutEdge( this );
		setColor( Color.DARK_GRAY );
	}

	public SEMOSSEdge( URI _uri ) {
		super( _uri, null, _uri.getLocalName() );
		setColor( Color.DARK_GRAY );
		inVertex = null;
		outVertex = null;
	}

	public SEMOSSVertex getInVertex() {
		return inVertex;
	}

	public SEMOSSVertex getOutVertex() {
		return outVertex;
	}

	public String getName() {
		return getLabel();
	}

	public void setName( String _name ) {
		setLabel( _name );
	}

	@Override
	public int compareTo( SEMOSSEdge t ) {
		return toString().compareTo( t.toString() );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if ( !( null == outVertex || null == outVertex.getURI() ) ) {
			sb.append( outVertex.getURI() );
		}

		if ( null != getURI() ) {
			sb.append( "->" ).append( getURI() );
		}

		if ( !( null == inVertex || null == inVertex.getURI() ) ) {
			sb.append( "->" ).append( inVertex.getURI() );
		}

		return sb.toString();
	}
}
