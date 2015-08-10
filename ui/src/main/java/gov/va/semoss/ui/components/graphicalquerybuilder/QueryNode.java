/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.om.GraphColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;

import java.awt.Shape;

import java.util.Collection;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryNode extends AbstractQueryGraphElement implements SEMOSSVertex {

	private Shape shape;

	public QueryNode( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public QueryNode( URI id, URI type, String label ) {
		super( id, type, label, GraphColorRepository.instance().getColor( type ) );
		shape = GraphShapeRepository.instance().getShape( type );
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	@Override
	public void setShape( Shape s ) {
		shape = s;
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
	public void setProperties( URI prop, Collection<Value> val ) {
		super.setProperties( prop, val );
		if ( RDF.TYPE.equals( prop ) ) {
			URI typeURI = getType();
			setColor( GraphColorRepository.instance().getColor( typeURI ) );
			setShape( GraphShapeRepository.instance().getShape( getType() ) );
		}
	}
}
