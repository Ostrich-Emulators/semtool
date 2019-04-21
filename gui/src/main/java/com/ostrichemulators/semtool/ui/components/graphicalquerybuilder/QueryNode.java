/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.om.SEMOSSVertex;

import org.eclipse.rdf4j.model.URI;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryNode extends AbstractQueryGraphElement implements SEMOSSVertex {

	public QueryNode( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public QueryNode( URI id, URI type, String label ) {
		super( id, type, label );
	}

	@Override
	public boolean isNode() {
		return true;
	}
}
