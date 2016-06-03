/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;

/**
 * An implementation that queries the IEngine for {@link SEMTOOL#Structure}
 * triples. This class is *not* thread-safe, but it is very lightweight. Note
 * that no matter what is passed into these functions, top-level results are
 * returned...so, for example, if the Person concept has three possible
 * properties, but an instance of Person is passed into getPropertiesOf, three
 * properties will be returned, even if that particular instance doesn't have
 * any of them set.
 *
 * @author ryan
 */
public class StructureManagerImpl implements StructureManager {

	private static final Logger log = Logger.getLogger( StructureManagerImpl.class );

	private final IEngine engine;
	private final ListQueryAdapter<URI> propqa = OneVarListQueryAdapter.getUriList(
			"SELECT ?prop WHERE {\n"
			+ "  ?s a semtool:StructureData ;"
			+ "    rdfs:domain ?domain ;"
			+ "    owl:DatatypeProperty ?prop .\n"
			+ "  ?dom a|rdfs:subPropertyOf* ?domain .\n"
			+ "}" );

	public StructureManagerImpl( IEngine engine ) {
		this.engine = engine;
	}

	@Override
	public Set<URI> getPropertiesOf( URI type ) {
		propqa.bind( "dom", type );
		return new HashSet<>( engine.queryNoEx( propqa ) );
	}

	@Override
	public Model getLinksBetween( URI subtype, URI objtype ) {
		String query = "CONSTRUCT{ ?s ?p ?o } WHERE { \n"
				+ "  ?mm a semtool:StructureData ;\n"
				+ "    rdf:predicate ?p ;\n"
				+ "    rdfs:domain ?s ;\n"
				+ "    rdfs:range ?o .\n";
		// + "}"

		Map<String, Value> bindings = new HashMap<>();
		if ( !Constants.ANYNODE.equals( subtype ) ) {
			query += "   ?dom a|rdfs:subClassOf* ?s .\n";
			bindings.put( "dom", subtype );
		}
		if ( !Constants.ANYNODE.equals( objtype ) ) {
			query += "   ?ran a|rdfs:subClassOf* ?o .\n";
			bindings.put( "ran", objtype );
		}
		query += "}";

		ModelQueryAdapter modelqa = new ModelQueryAdapter( query );
		modelqa.setBindings( bindings );

		return engine.constructNoEx( modelqa );
	}

	@Override
	public Model getEndpoints( URI reltype ) {
		String query = "CONSTRUCT{ ?s ?p ?o } WHERE { \n"
				+ "  ?mm a semtool:StructureData ;\n"
				+ "    rdf:predicate ?p ;\n"
				+ "    rdfs:domain ?s ;\n"
				+ "    rdfs:range ?o .\n"
				+ "  ?rel a|rdfs:subPropertyOf* ?p .\n"
				+ "}";

		ModelQueryAdapter modelqa = new ModelQueryAdapter( query );
		modelqa.bind( "rel", reltype );

		return engine.constructNoEx( modelqa );
	}

	@Override
	public Set<URI> getTopLevelRelations( Collection<URI> instances ) {
		if ( null == instances ) {
			ListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList(
					"SELECT ?p WHERE { ?mm a semtool:StructureData ; rdf:predicate ?p . }" );
			return new HashSet<>( engine.queryNoEx( lqa ) );
		}

		if ( instances.isEmpty() ) {
			return new HashSet<>();
		}

		String implosion = Utility.implode( instances );
		ListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList(
				"SELECT ?p WHERE { ?mm a semtool:StructureData ; rdf:predicate ?p . "
				+ "?reltype a|rdfs:subPropertyOf* ?p . "
				+ "VALUES ?reltype {" + implosion + "} }" );
		return new HashSet<>( engine.queryNoEx( lqa ) );
	}

	@Override
	public Model getConnectedConceptTypes( Collection<URI> instances ) {
		final String query = "CONSTRUCT{ ?s ?p ?o } WHERE { \n"
				+ "  ?mm a semtool:StructureData ;\n"
				+ "    rdf:predicate ?p ;\n"
				+ "    rdfs:domain ?s ;\n"
				+ "    rdfs:range ?o .\n";
		String implosion = Utility.implode( instances );

		LinkedHashModel model = new LinkedHashModel();
		ModelQueryAdapter subjects = new ModelQueryAdapter( query
				+ " ?dom a|rdfs:subClassOf* ?s . VALUES ?dom {" + implosion + "} }", model );
		ModelQueryAdapter objects = new ModelQueryAdapter( query
				+ " ?ran a|rdfs:subClassOf* ?o . VALUES ?ran {" + implosion + "} }", model );
		engine.constructNoEx( subjects );
		engine.constructNoEx( objects );

		return model;
	}
}
