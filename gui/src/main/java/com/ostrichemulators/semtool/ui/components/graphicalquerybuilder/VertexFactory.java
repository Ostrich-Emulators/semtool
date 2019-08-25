/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import com.ostrichemulators.semtool.util.Utility;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class VertexFactory implements Supplier<QueryNode> {

	private IRI verttype;

	@Override
	public QueryNode get() {
		QueryNode v = new QueryNode( Utility.getUniqueIri(), verttype, "" );
		v.setSelected( RDFS.LABEL, true );
		return v;
	}

	public void setType( IRI type ) {
		verttype = type;
	}
}
