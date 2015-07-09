/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.UriBuilder;
import org.apache.commons.collections15.Factory;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class VertexFactory implements Factory<SEMOSSVertex> {

	private final UriBuilder uribuilder = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
	private final MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> map;
	private URI verttype;
	private int count = 0;

	public VertexFactory( MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> map ) {
		this.map = map;
	}

	@Override
	public SEMOSSVertex create() {
		SEMOSSVertex v = new SEMOSSVertex( uribuilder.uniqueUri(), verttype, "" );
		v.mark( RDFS.LABEL, true );
		v.removeProperty( AbstractNodeEdgeBase.LEVEL );

		SparqlResultConfig src = new SparqlResultConfig( v, RDF.SUBJECT,
				"node" + ( count++ ) );
		map.add( v, src );
		return v;
	}

	public void setType( URI type ) {
		verttype = type;
	}
}
