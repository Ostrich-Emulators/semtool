/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ModificationExecutor;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
public final class SemtoolStructureManagerImpl implements StructureManager {

	private static final Logger log = Logger.getLogger( SemtoolStructureManagerImpl.class );

	private final IEngine engine;
	private final ListQueryAdapter<URI> propqa = OneVarListQueryAdapter.getUriList(
			"SELECT ?prop WHERE {\n"
			+ "  ?s a semtool:StructureData ;"
			+ "    rdfs:domain ?domain ;"
			+ "    owl:DatatypeProperty ?prop .\n"
			+ "  ?dom a|rdfs:subPropertyOf* ?domain .\n"
			+ "}" );

	protected SemtoolStructureManagerImpl( IEngine engine ) {
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
	public Set<URI> getTopLevelConcepts() {
		ListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList(
				"SELECT ?s WHERE { ?s rdfs:subClassOf ?concept }" );
		lqa.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		lqa.useInferred( false );
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

	@Override
	public Model getModel() {
		ModelQueryAdapter qa = new ModelQueryAdapter(
				"CONSTRUCT { ?s ?p ?o } WHERE { ?s a ?struct ; ?p ?o .}" );
		qa.bind( "struct", SEMTOOL.Structure );
		qa.useInferred( false );
		return engine.constructNoEx( qa );
	}

	private Model doRebuild( Collection<URI> uris ){
		// get all concepts
		String cquery = "SELECT DISTINCT ?instance WHERE { ?instance rdfs:subClassOf ?concept }";
		ListQueryAdapter<URI> cqa = OneVarListQueryAdapter.getUriList( cquery );
		cqa.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		cqa.useInferred( false );
		List<URI> concepts = engine.queryNoEx( cqa );
		if( null != uris ){
			concepts.retainAll( uris );
		}

		// get all edge types
		String equery = "SELECT DISTINCT ?instance WHERE { ?instance rdfs:subPropertyOf ?semrel }";
		ListQueryAdapter<URI> eqa = OneVarListQueryAdapter.getUriList( equery );
		eqa.bind( "semrel", engine.getSchemaBuilder().getRelationUri().build() );
		eqa.useInferred( false );
		List<URI> edges = engine.queryNoEx( eqa );
		if( null != uris ){
			edges.retainAll( uris );
		}

		Model edgemodel = rebuildEdges( concepts, edges );
		Model propmodel = rebuildProps( concepts, edges );
		Set<Resource> needlabels = new HashSet<>();

		for ( Statement s : edgemodel ) {
			needlabels.add( URI.class.cast( s.getSubject() ) );
			needlabels.add( s.getPredicate() );
			needlabels.add( URI.class.cast( s.getObject() ) );
		}
		for ( Statement s : propmodel ) {
			needlabels.add( URI.class.cast( s.getSubject() ) );
			needlabels.add( s.getPredicate() );
		}

		Map<Resource, String> labels = Utility.getInstanceLabels( needlabels, engine );
		Map<String, URI> structurelkp = new HashMap<>();
		UriBuilder schema = engine.getSchemaBuilder();

		LinkedHashModel model = new LinkedHashModel();

		for ( Statement s : edgemodel ) {
			String sub = labels.get( s.getSubject() );
			String rel = labels.get( s.getPredicate() );
			String obj = labels.get( Resource.class.cast( s.getObject() ) );
			String name = sub + "_" + rel + "_" + obj;

			model.addAll( getEdgeStructure( s.getPredicate(),
					URI.class.cast( s.getSubject() ), URI.class.cast( s.getObject() ),
					schema, structurelkp, name ) );
		}

		for ( Statement s : propmodel ) {
			String sub = labels.get( s.getSubject() );
			String rel = labels.get( s.getPredicate() );
			String name = sub + "_" + rel;

			model.addAll( getPropStructure( s.getPredicate(),
					URI.class.cast( s.getSubject() ), schema, structurelkp, name ) );
		}

		return model;
	}

	@Override
	public Model rebuild( Collection<URI> uris ){
		return doRebuild( uris );
	}

	@Override
	public Model rebuild( boolean saveToEngine ) {
		Model model = doRebuild( null );

		if ( saveToEngine ) {
			save( model );
		}

		return model;
	}

	private Model rebuildProps( List<URI> concepts, List<URI> edges ) {
		String cimplosion = Utility.implode( concepts );
		String eimplosion = Utility.implode( edges );

		// now see what properties are on concepts and edges
		String query = "SELECT DISTINCT ?type ?prop WHERE {\n"
				+ "  ?s ?prop ?propval . FILTER ( isLiteral( ?propval ) )\n"
				+ "  ?s a|rdfs:subClassOf+|rdfs:subPropertyOf+ ?type .\n"
				+ "  FILTER ( ?prop != rdfs:label ) .\n"
				+ "} VALUES ?type { " + cimplosion + "\n" + eimplosion + " }";

		ModelQueryAdapter mqa = new ModelQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI type = URI.class.cast( set.getValue( "type" ) );
				URI prop = URI.class.cast( set.getValue( "prop" ) );
				result.add( type, prop, Constants.ANYNODE );
			}
		};

		return engine.queryNoEx( mqa );
	}

	private Model rebuildEdges( List<URI> concepts, List<URI> edges ) {
		String cimplosion = Utility.implode( concepts );
		String eimplosion = Utility.implode( edges );

		// now see what concepts connect to others via our edge types
		String query = "CONSTRUCT { ?stype ?rtype ?otype } WHERE {\n"
				+ "  ?s a|rdfs:subClassOf+ ?stype .\n"
				+ "  ?o a|rdfs:subClassOf+ ?otype .\n"
				+ "  ?r a|rdfs:subPropertyOf+ ?rtype .\n"
				+ "  ?s ?r ?o .\n"
				+ "  VALUES ?stype {" + cimplosion + "} .\n"
				+ "  VALUES ?otype {" + cimplosion + "} .\n"
				+ "  VALUES ?rtype {" + eimplosion + "} .\n"
				+ "}";

		final Model model = engine.constructNoEx( new ModelQueryAdapter( query ) );

		return model;
	}

	private void save( Model model ) {
		// get URIs to completely remove
		ListQueryAdapter<URI> oldqa
				= OneVarListQueryAdapter.getUriList( "SELECT ?s WHERE { s a ?type }" );
		oldqa.bind( "type", SEMTOOL.Structure );
		List<URI> olds = engine.queryNoEx( oldqa );

		ModificationExecutor eme = new ModificationExecutorAdapter() {

			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				for ( URI old : olds ) {
					conn.remove( old, null, null );
				}

				conn.add( model );
			}
		};

		try {
			engine.execute( eme );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	public static Collection<Statement> getEdgeStructure( URI predicate, URI domain,
			URI range, UriBuilder schema, Map<String, URI> structurelkp, String name ) {

		Model stmts = new LinkedHashModel();

		String key = predicate.stringValue() + domain + range;
		if ( !structurelkp.containsKey( key ) ) {
			URI structure = schema.build( name );
			structurelkp.put( key, structure );
		}

		URI structure = structurelkp.get( key );

		stmts.add( structure, RDF.TYPE, SEMTOOL.Structure );
		stmts.add( structure, RDF.PREDICATE, predicate );
		stmts.add( structure, RDFS.DOMAIN, domain );
		stmts.add( structure, RDFS.RANGE, range );

		return stmts;
	}

	public static Collection<Statement> getPropStructure( URI prop, URI domain,
			UriBuilder schema, Map<String, URI> structurelkp, String title ) {

		Model stmts = new LinkedHashModel();

		String key = prop.stringValue() + domain;
		if ( !structurelkp.containsKey( key ) ) {
			URI structure = schema.build( title );
			structurelkp.put( key, structure );
		}

		URI structure = structurelkp.get( key );

		stmts.add( structure, RDF.TYPE, SEMTOOL.Structure );
		stmts.add( structure, OWL.DATATYPEPROPERTY, prop );
		stmts.add( structure, RDFS.DOMAIN, domain );

		return stmts;
	}

	@Override
	public URI getTopLevelType( URI instance ) {
		String query = "SELECT ?type WHERE { "
				+ "  ?subject a|rdfs:subClassOf|rdfs:subPropertyOf ?type "
				+ "}";
		OneValueQueryAdapter<URI> qa = OneValueQueryAdapter.getUri( query );
		qa.bind( "subject", instance );
		qa.useInferred( false );
		String q = qa.bindAndGetSparql();
		log.debug( "type query: " + q );

		return engine.queryNoEx( qa );
	}
}
