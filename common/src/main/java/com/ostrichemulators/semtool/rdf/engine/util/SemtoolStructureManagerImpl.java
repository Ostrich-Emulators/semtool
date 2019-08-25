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
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

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
	private final ListQueryAdapter<IRI> propqa = OneVarListQueryAdapter.getIriList(
			"SELECT ?prop WHERE {\n"
			+ "  ?s a semtool:StructureData ;"
			+ "    rdfs:domain ?domain ;"
			+ "    owl:DatatypeProperty ?prop ."
			+ "    FILTER NOT EXISTS { ?s rdf:predicate ?x  }\n"
			+ "  ?dom a|rdfs:subPropertyOf* ?domain .\n"
			+ "}" );

	protected SemtoolStructureManagerImpl( IEngine engine ) {
		this.engine = engine;
	}

	@Override
	public Set<IRI> getPropertiesOf( IRI type ) {
		propqa.bind( "dom", type );
		Set<IRI> set = new HashSet<>( engine.queryNoEx( propqa ) );
		if ( set.isEmpty() ) {
			// check to see if type is actually a relation
			set.addAll( getPropertiesOf( null, type, null ) );
		}

		return set;
	}

	@Override
	public Set<IRI> getPropertiesOf( IRI subtype, IRI predtype, IRI objtype ) {
		Map<String, Value> bindings = new HashMap<>();
		bindings.put( "pred", predtype );

		String query = "SELECT ?prop WHERE {\n"
				+ "  ?s a semtool:StructureData ;"
				+ "     rdfs:domain ?domain ;"
				+ "     rdfs:range ?range ;"
				+ "     rdf:predicate ?predicate ;"
				+ "     owl:DatatypeProperty ?prop .\n"
				+ "  ?pred a|rdfs:subPropertyOf* ?predicate .\n";
		if ( !( null == subtype || Constants.ANYNODE == subtype ) ) {
			query += "  ?dom a|rdfs:subClassOf+ ?domain .\n";
			bindings.put( "dom", subtype );
		}
		if ( !( null == objtype || Constants.ANYNODE == objtype ) ) {
			query += "  ?ran a|rdfs:subClassOf+ ?range .\n";
			bindings.put( "ran", objtype );
		}
		query += "}";

		OneVarListQueryAdapter<IRI> qa = OneVarListQueryAdapter.getIriList( query );
		qa.setBindings( bindings );
		return new HashSet<>( engine.queryNoEx( qa ) );

	}

	@Override
	public Model getLinksBetween( IRI subtype, IRI objtype ) {
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
	public Model getEndpoints( IRI reltype ) {
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
	public Set<IRI> getTopLevelRelations( Collection<IRI> instances ) {
		if ( null == instances ) {
			ListQueryAdapter<IRI> lqa = OneVarListQueryAdapter.getIriList(
					"SELECT ?p WHERE { ?mm a semtool:StructureData ; rdf:predicate ?p . }" );
			return new HashSet<>( engine.queryNoEx( lqa ) );
		}

		if ( instances.isEmpty() ) {
			return new HashSet<>();
		}

		String implosion = Utility.implode( instances );
		ListQueryAdapter<IRI> lqa = OneVarListQueryAdapter.getIriList(
				"SELECT ?p WHERE { ?mm a semtool:StructureData ; rdf:predicate ?p . "
				+ "?reltype a|rdfs:subPropertyOf* ?p . "
				+ "VALUES ?reltype {" + implosion + "} }" );
		return new HashSet<>( engine.queryNoEx( lqa ) );
	}

	@Override
	public Set<IRI> getTopLevelConcepts() {
		ListQueryAdapter<IRI> lqa = OneVarListQueryAdapter.getIriList(
				"SELECT ?s WHERE { ?s rdfs:subClassOf ?concept }" );
		lqa.bind( "concept", engine.getSchemaBuilder().getConceptIri().build() );
		lqa.useInferred( false );
		return new HashSet<>( engine.queryNoEx( lqa ) );
	}

	@Override
	public Model getConnectedConceptTypes( Collection<IRI> instances ) {
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

	private Model doRebuild( Collection<IRI> uris ) {
		// get all concepts
		String cquery = "SELECT DISTINCT ?instance WHERE { ?instance rdfs:subClassOf ?concept }";
		ListQueryAdapter<IRI> cqa = OneVarListQueryAdapter.getIriList( cquery );
		cqa.bind( "concept", engine.getSchemaBuilder().getConceptIri().build() );
		cqa.useInferred( false );
		List<IRI> concepts = engine.queryNoEx( cqa );
		if ( null != uris ) {
			concepts.retainAll( uris );
		}

		// get all edge types
		String equery = "SELECT DISTINCT ?instance WHERE { ?instance rdfs:subPropertyOf ?semrel }";
		ListQueryAdapter<IRI> eqa = OneVarListQueryAdapter.getIriList( equery );
		eqa.bind( "semrel", engine.getSchemaBuilder().getRelationIri().build() );
		eqa.useInferred( false );
		List<IRI> edges = engine.queryNoEx( eqa );
		if ( null != uris ) {
			edges.retainAll( uris );
		}

		Model edgemodel = rebuildEdges( concepts, edges );
		Model propmodel = rebuildConceptProps( concepts );
		Set<Resource> needlabels = new HashSet<>();

		for ( Statement s : edgemodel ) {
			needlabels.add( IRI.class.cast( s.getSubject() ) );
			needlabels.add( s.getPredicate() );
			needlabels.add( IRI.class.cast( s.getObject() ) );
		}
		for ( Statement s : propmodel ) {
			needlabels.add( IRI.class.cast( s.getSubject() ) );
			needlabels.add( s.getPredicate() );
		}

		Map<Resource, String> labels = Utility.getInstanceLabels( needlabels, engine );
		Map<String, IRI> structurelkp = new HashMap<>();
		UriBuilder schema = engine.getSchemaBuilder();

		LinkedHashModel model = new LinkedHashModel();

		for ( Statement s : edgemodel ) {
			String sub = labels.get( s.getSubject() );
			String rel = labels.get( s.getPredicate() );
			String obj = labels.get( Resource.class.cast( s.getObject() ) );
			String name = sub + "_" + rel + "_" + obj;

			model.addAll( getEdgeStructure( s.getPredicate(),
					IRI.class.cast( s.getSubject() ), IRI.class.cast( s.getObject() ),
					schema, structurelkp, name ) );
		}

		model.addAll( createEdgeProps( concepts, edges, structurelkp, schema, labels ) );

		for ( Statement s : propmodel ) {
			String sub = labels.get( s.getSubject() );
			String rel = labels.get( s.getPredicate() );
			String name = sub + "_" + rel;

			model.addAll( getPropStructure( s.getPredicate(),
					IRI.class.cast( s.getSubject() ), schema, structurelkp, name ) );
		}

		return model;
	}

	@Override
	public Model rebuild( Collection<IRI> uris ) {
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

	private Model rebuildConceptProps( List<IRI> concepts ) {
		String cimplosion = Utility.implode( concepts );

		// now see what properties are on concepts and edges
		String query = "SELECT DISTINCT ?type ?prop WHERE {\n"
				+ "  ?s ?prop ?propval . FILTER ( isLiteral( ?propval ) )\n"
				+ "  ?s a|rdfs:subClassOf+ ?type .\n"
				+ "  FILTER ( ?prop != rdfs:label ) .\n"
				+ "} VALUES ?type { " + cimplosion + " }";

		ModelQueryAdapter mqa = new ModelQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				IRI type = IRI.class.cast( set.getValue( "type" ) );
				IRI prop = IRI.class.cast( set.getValue( "prop" ) );
				result.add( type, prop, Constants.ANYNODE );
			}
		};

		return engine.queryNoEx( mqa );
	}

	private Model rebuildEdges( List<IRI> concepts, List<IRI> edges ) {
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

	private Model createEdgeProps( List<IRI> concepts, List<IRI> edges,
			Map<String, IRI> structurelkp, UriBuilder schema,
			Map<Resource, String> labels ) {
		String cimplosion = Utility.implode( concepts );
		String eimplosion = Utility.implode( edges );

		String propq = "SELECT DISTINCT ?stype ?rtype ?otype ?prop WHERE {\n"
				+ "  ?s a|rdfs:subClassOf+ ?stype .\n"
				+ "  ?o a|rdfs:subClassOf+ ?otype .\n"
				+ "  ?r a|rdfs:subPropertyOf+ ?rtype .\n"
				+ "  ?s ?r ?o .\n"
				+ "  ?r ?prop ?propval . FILTER( ?prop != rdfs:label && isLiteral( ?propval ) ) .\n"
				+ "  VALUES ?stype {" + cimplosion + "} .\n"
				+ "  VALUES ?otype {" + cimplosion + "} .\n"
				+ "  VALUES ?rtype {" + eimplosion + "} .\n"
				+ "}";

		Model model = new LinkedHashModel();

		VoidQueryAdapter vqa = new VoidQueryAdapter( propq ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				IRI domain = IRI.class.cast( set.getValue( "stype" ) );
				IRI pred = IRI.class.cast( set.getValue( "rtype" ) );
				IRI range = IRI.class.cast( set.getValue( "otype" ) );
				IRI prop = IRI.class.cast( set.getValue( "prop" ) );

				String sub = labels.get( domain );
				String rel = labels.get( pred );
				String obj = labels.get( range );
				String name = sub + "_" + rel + "_" + obj;

				model.addAll( getPropStructure( pred, domain, range, prop, schema,
						structurelkp, name ) );

			}
		};

		engine.queryNoEx( vqa );

		return model;
	}

	private void save( Model model ) {
		// get old model, which we'll remove
		Model olds = getModel();

		ModificationExecutor eme = new ModificationExecutorAdapter( true ) {

			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				conn.remove( olds );
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

	public static Collection<Statement> getEdgeStructure( IRI predicate, IRI domain,
			IRI range, UriBuilder schema, Map<String, IRI> structurelkp, String name ) {

		Model stmts = new LinkedHashModel();

		String key = predicate.stringValue() + domain + range;
		if ( !structurelkp.containsKey( key ) ) {
			IRI structure = schema.build( name );
			structurelkp.put( key, structure );
		}

		IRI structure = structurelkp.get( key );

		stmts.add( structure, RDF.TYPE, SEMTOOL.Structure );
		stmts.add( structure, RDF.PREDICATE, predicate );
		stmts.add( structure, RDFS.DOMAIN, domain );
		stmts.add( structure, RDFS.RANGE, range );

		return stmts;
	}

	public static Collection<Statement> getPropStructure( IRI prop, IRI domain,
			UriBuilder schema, Map<String, IRI> structurelkp, String title ) {

		Model stmts = new LinkedHashModel();

		String key = prop.stringValue() + domain;
		if ( !structurelkp.containsKey( key ) ) {
			IRI structure = schema.build( title );
			structurelkp.put( key, structure );
		}

		IRI structure = structurelkp.get( key );

		stmts.add( structure, RDF.TYPE, SEMTOOL.Structure );
		stmts.add( structure, OWL.DATATYPEPROPERTY, prop );
		stmts.add( structure, RDFS.DOMAIN, domain );

		return stmts;
	}

	public static Collection<Statement> getPropStructure( IRI predicate, IRI domain,
			IRI range, IRI prop, UriBuilder schema, Map<String, IRI> structurelkp,
			String title ) {

		Model stmts = new LinkedHashModel();

		String key = prop.stringValue() + domain + range;
		if ( !structurelkp.containsKey( key ) ) {
			IRI structure = schema.build( title );
			structurelkp.put( key, structure );
		}

		IRI structure = structurelkp.get( key );

		stmts.add( structure, RDF.TYPE, SEMTOOL.Structure );
		stmts.add( structure, RDF.PREDICATE, predicate );
		stmts.add( structure, RDFS.DOMAIN, domain );
		stmts.add( structure, RDFS.RANGE, range );
		stmts.add( structure, OWL.DATATYPEPROPERTY, prop );

		return stmts;
	}

	@Override
	public IRI getTopLevelType( IRI instance ) {
		String query = "SELECT ?type WHERE { "
				+ "  ?subject a|rdfs:subClassOf|rdfs:subPropertyOf ?type "
				+ "}";
		OneValueQueryAdapter<IRI> qa = OneValueQueryAdapter.getUri( query );
		qa.bind( "subject", instance );
		qa.useInferred( false );
		String q = qa.bindAndGetSparql();
		log.debug( "type query: " + q );

		return engine.queryNoEx( qa );
	}
}
