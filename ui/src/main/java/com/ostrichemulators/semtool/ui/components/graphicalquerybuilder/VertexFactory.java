/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class VertexFactory implements Supplier<QueryNode> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
	private URI verttype;

	@Override
	public QueryNode get() {
		QueryNode v = new QueryNode( uribuilder.uniqueUri(), verttype, "" );
		v.setSelected( RDFS.LABEL, true );
		return v;
	}

	public void setType( URI type ) {
		verttype = type;
	}
}
