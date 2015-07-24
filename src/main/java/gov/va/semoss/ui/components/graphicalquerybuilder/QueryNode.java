/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.NodeBase;
import gov.va.semoss.ui.helpers.GraphColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;

import java.awt.Shape;

import org.openrdf.model.URI;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryNode extends AbstractQueryNodeEdgeBase implements NodeBase {

	private Shape shape;

	public QueryNode( URI id ) {
		super( id );
	}

	public QueryNode( URI id, URI type, String label ) {
		super( id, type, label );

		if ( null != type ) {
			setColor( GraphColorRepository.instance().getColor( type ) );
			setShape( GraphShapeRepository.instance().getShape( type ) );
		}
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	@Override
	public void setShape( Shape s ) {
		shape = s;
	}
}
