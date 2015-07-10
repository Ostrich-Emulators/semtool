/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import org.apache.commons.collections15.Factory;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class EdgeFactory implements Factory<SEMOSSEdge> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );

	@Override
	public SEMOSSEdge create() {
		SEMOSSEdge edge = new SEMOSSEdge( uribuilder.uniqueUri() );
		edge.removeProperty( RDFS.LABEL );
		edge.removeProperty( AbstractNodeEdgeBase.LEVEL );

		return edge;
	}
}
