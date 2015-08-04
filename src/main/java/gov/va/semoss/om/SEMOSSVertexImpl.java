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

import gov.va.semoss.ui.helpers.GraphColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.util.Constants;

import java.awt.Shape;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * Variables are transient because this tells the json writer to ignore them
 */
public class SEMOSSVertexImpl extends AbstractGraphElement implements SEMOSSVertex {

	public static final String CHANGE_SHAPE = "shape";
	private transient Shape shape;

	public SEMOSSVertexImpl( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public SEMOSSVertexImpl( URI id, URI type, String label ) {
		super( id, type, label, GraphColorRepository.instance().getColor( type ) );
		shape = GraphShapeRepository.instance().getShape( type );
	}

	@Override
	public void setValue( URI prop, Value val ) {
		super.setValue( prop, val );
		if ( RDF.TYPE.equals( prop ) ) {
			URI typeURI = getType();
			setColor( GraphColorRepository.instance().getColor( typeURI ) );
			setShape( GraphShapeRepository.instance().getShape( getType() ) );
		}
	}

	@Override
	public final void setShape( Shape _shape ) {
		Shape old = shape;
		shape = ( null == _shape
				? GraphShapeRepository.instance().getShapeByName( Constants.CIRCLE )
				: _shape );
		fireIfPropertyChanged( CHANGE_SHAPE, old, shape );
	}

	@Override
	public Shape getShape() {
		return shape;
	}
}
