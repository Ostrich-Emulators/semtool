/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.google.common.base.Supplier;
import com.ostrichemulators.semtool.util.Utility;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class EdgeFactory implements Supplier<QueryEdge> {

	@Override
	public QueryEdge get() {
		QueryEdge edge = new QueryEdge( Utility.getUniqueUri() );
		edge.removeProperty( RDFS.LABEL );

		return edge;
	}
}
