/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class EdgeFactory implements Supplier<QueryEdge> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );

	@Override
	public QueryEdge get() {
		QueryEdge edge = new QueryEdge( uribuilder.uniqueUri() );
		edge.removeProperty( RDFS.LABEL );

		return edge;
	}
}
