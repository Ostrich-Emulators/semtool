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
package com.ostrichemulators.semtool.om;

import com.ostrichemulators.semtool.ui.helpers.DynamicColorRepository;

import com.ostrichemulators.semtool.ui.helpers.DefaultGraphShapeRepository;
import java.awt.Shape;

import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * Variables are transient because this tells the json writer to ignore them
 */
public class SEMOSSVertexImpl extends AbstractGraphElement implements SEMOSSVertex {

	private transient Shape shape;

	public SEMOSSVertexImpl( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public SEMOSSVertexImpl( URI id, URI type, String label ) {
		super( id, type, label, DynamicColorRepository.instance().getColor( type ) );
		shape = new DefaultGraphShapeRepository().getRawShape( type );
	}

	@Override
	public SEMOSSVertex duplicate() {
		SEMOSSVertexImpl newone = new SEMOSSVertexImpl( getURI(), getType(), getLabel() );
		newone.setShape( getShape() );
		newone.setColor( getColor() );

		for ( Map.Entry<URI, Value> en : getValues().entrySet() ) {
			newone.setValue( en.getKey(), en.getValue() );
		}

		return newone;
	}

	@Override
	public void setValue( URI prop, Value val ) {
		super.setValue( prop, val );
		if ( RDF.TYPE.equals( prop ) ) {
			URI typeURI = getType();
			setColor( DynamicColorRepository.instance().getColor( typeURI ) );
			setShape(new DefaultGraphShapeRepository().getRawShape( getType() ) );
		}
	}

	@Override
	public final void setShape( Shape _shape ) {
		Shape old = shape;
		shape = ( null == _shape
				? NamedShape.CIRCLE.getShape( 16 )
				: _shape );
		fireIfPropertyChanged( CHANGE_SHAPE, old, shape );
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	@Override
	public boolean isNode() {
		return true;
	}
}
