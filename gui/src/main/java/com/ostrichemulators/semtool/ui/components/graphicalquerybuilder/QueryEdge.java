/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import org.eclipse.rdf4j.model.IRI;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryEdge extends AbstractQueryGraphElement implements QueryGraphElement {

	public QueryEdge( IRI id ) {
		super( id );
	}

	public QueryEdge( IRI id, IRI type, String label ) {
		super( id, type, label );
	}

	@Override
	public boolean isNode() {
		return false;
	}

}
