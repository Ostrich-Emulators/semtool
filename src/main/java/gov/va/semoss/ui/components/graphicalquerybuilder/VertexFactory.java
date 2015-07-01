/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import org.apache.commons.collections15.Factory;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class VertexFactory implements Factory<SEMOSSVertex> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
	private URI verttype;

	@Override
	public SEMOSSVertex create() {
		return new SEMOSSVertex( uribuilder.uniqueUri(), verttype, "" );
	}

	public void setType( URI type ) {
		verttype = type;
	}
}
