/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.UriBuilder;
import org.apache.commons.collections15.Factory;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class EdgeFactory implements Factory<SEMOSSEdge> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
	private final MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> map;
	private URI edgetype;
	private int count = 0;

	public EdgeFactory( MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> map ) {
		this.map = map;
	}

	@Override
	public SEMOSSEdge create() {
		SEMOSSEdge edge = new SEMOSSEdge( uribuilder.uniqueUri() );
		edge.setLabel( "" );
		edge.removeProperty( AbstractNodeEdgeBase.LEVEL );

		SparqlResultConfig src = new SparqlResultConfig( edge, RDF.SUBJECT,
				"link" + ( count++ ) );
		map.add( edge, src );

		return edge;
	}

	public void setType( URI type ) {
		edgetype = type;
	}
}
