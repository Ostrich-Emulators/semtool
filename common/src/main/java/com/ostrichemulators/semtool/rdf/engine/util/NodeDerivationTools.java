package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.MapQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.BindingSet;

/**
 * This class analogous to {@link StructureManager}, but instances instead of
 * top-level URIs.
 *
 * @author ryan
 *
 */
public class NodeDerivationTools {

	/**
	 * The logger for this class
	 */
	private static final Logger log = Logger.getLogger( NodeDerivationTools.class );

	/**
	 * Private singleton default constructor
	 */
	private NodeDerivationTools() {

	}

	public static String getConceptQuery( IEngine engine ) {
		MetadataQuery mq = new MetadataQuery( SEMTOOL.ReificationModel );
		engine.queryNoEx( mq );
		URI reif = URI.class.cast( mq.getOne() );

		if ( SEMTOOL.Custom_Reification.equals( reif ) ) {
			OneValueQueryAdapter<String> qq = OneValueQueryAdapter.
					getString( "SELECT ?val WHERE { ?base ?pred ?val }" );
			qq.bind( "base", engine.getBaseUri() );
			qq.bind( "pred", SEMTOOL.ConceptsSparql );

			return engine.queryNoEx( qq );
		}
		else {
			String query = "SELECT ?concept WHERE "
					+ "{ "
					+ "  ?concept rdfs:subClassOf+ ?top ."
					+ "  FILTER( ?concept != ?top && \n"
					+ "    ?concept != <http://www.w3.org/2004/02/skos/core#Concept> ) ."
					+ "  VALUES ?top {<replace-with-binding>} "
					+ " }";

			return query.replaceAll( "replace-with-binding",
					engine.getSchemaBuilder().getConceptUri().build().stringValue() );
		}
	}

	public static List<URI> createInstanceList( URI concept, IEngine engine ) {
		String query = "SELECT DISTINCT ?instance WHERE { "
				+ "  ?instance a|rdfs:subClassOf+|rdfs:subPropertyOf+ ?concept ."
				+ "  FILTER( ?instance != ?concept ) ."
				+ "}";
		ListQueryAdapter<URI> qa = OneVarListQueryAdapter.getUriList( query );
		qa.bind( "concept", concept );

		return engine.queryNoEx( qa );
	}

	public static Model getInstances( URI subtype, URI predtype, URI objtype,
			Collection<URI> propsToInclude, IEngine engine ) {

		// round one: get the relationships themselves
		String query = "CONSTRUCT { ?s ?p ?o } WHERE {\n"
				+ "  ?s a|rdfs:subClassOf+ ?subtype .\n"
				+ "  ?o a|rdfs:subClassOf+ ?objtype .\n"
				+ "  ?p a|rdfs:subPropertyOf+ ?predtype .\n"
				+ "  FILTER( ?s != ?subtype && ?o != ?objtype ) .\n"
				+ "  ?s ?p ?o .\n"
				+ "}";

		ModelQueryAdapter mqa = new ModelQueryAdapter( query );
		mqa.bind( "subtype", subtype );
		mqa.bind( "objtype", objtype );
		mqa.bind( "predtype", predtype );
		mqa.useInferred( true );

		Model model = engine.constructNoEx( mqa );

		// we get inferred rel types as well as declared types, so if we have
		// both, use the declared type
		List<Statement> removers = new ArrayList<>();
		for ( Statement s : model ) {
			URI subj = URI.class.cast( s.getSubject() );
			URI obj = URI.class.cast( s.getObject() );

			Model filts = model.filter( subj, null, obj );
			if ( filts.size() > 1 ) {
				removers.add( new StatementImpl( subj, predtype, obj ) );
			}
		}
		model.removeAll( removers );

		// round two: get properties for the relationships if they exist
		if ( !( null == propsToInclude || propsToInclude.isEmpty() ) ) {
			String propq = "CONSTRUCT { ?p ?prop ?propval } WHERE {\n"
					+ "  ?p rdfs:subPropertyOf+ ?predtype ; ?prop ?propval .\n"
					+ "  VALUES ?prop {" + Utility.implode( propsToInclude ) + "}.\n"
					+ "}";

			ModelQueryAdapter propqa = new ModelQueryAdapter( propq );
			propqa.bind( "predtype", predtype );

			propqa.setModel( model );
			propqa.useInferred( false );

			engine.constructNoEx( propqa );
		}

		return model;
	}

	/**
	 * Derives a query adapter capable of pulling out the predicates that connect
	 * all subject nodes of a given type and all object nodes of a given type. The
	 * results will contain *all* types, so they will generally be run through {@link
	 * #getTopLevelRelations(java.util.Collection,
	 * com.ostrichemulators.semtool.rdf.engine.api.IEngine) } to get only the
	 * top-level relationships
	 *
	 * @param subjectNodeType The type (in URI form) of the subject node
	 * @param objectNodeType The type (in URI form) of the object node
	 * @param engine
	 * @return A proper query adapter capable of querying a knowledgebase for the
	 * desired predicates
	 */
	public static ListQueryAdapter<URI> getPredicatesBetweenQA( URI subjectNodeType,
			URI objectNodeType, IEngine engine ) {
		String q
				= "SELECT DISTINCT ?relationship WHERE {\n"
				+ "  ?in  a ?stype . \n"
				+ "  ?out a ?otype . \n"
				+ "  ?in ?relationship ?out .\n"
				+ "  FILTER( ?relationship != ?semrel )\n"
				+ "}";
		OneVarListQueryAdapter<URI> varq = OneVarListQueryAdapter.getUriList( q );
		varq.useInferred( false );
		varq.bind( "semrel", engine.getSchemaBuilder().getRelationUri().build() );
		varq.bind( "stype", subjectNodeType );
		if ( !objectNodeType.equals( Constants.ANYNODE ) ) {
			varq.bind( "otype", objectNodeType );
		}

		log.debug( varq.bindAndGetSparql() );
		return varq;
	}

	/**
	 *
	 * @param instance
	 * @param engine
	 * @param instanceIsSubject
	 * @return
	 */
	public static List<URI> getConnectedConceptTypes( URI instance, IEngine engine,
			boolean instanceIsSubject ) {
		String query = "SELECT DISTINCT ?subtype ?objtype \n"
				+ "WHERE { \n"
				+ "  ?subject ?predicate ?object .\n"
				+ "  ?subject a ?subtype .\n"
				+ "  ?subtype rdfs:subClassOf ?concept . FILTER( ?subtype != ?concept && ?subtype != ?skos ) .\n"
				+ "  ?object a ?objtype .\n"
				+ "  ?objtype rdfs:subClassOf ?concept . FILTER( ?objtype != ?concept && ?objtype != ?skos ) .\n"
				+ "}";

		OneVarListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList( query );
		lqa.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		lqa.bind( "skos", SKOS.CONCEPT );

		if ( instanceIsSubject ) {
			lqa.setVariableName( "objtype" );
			lqa.bind( "subject", instance );
		}
		else {
			lqa.setVariableName( "subtype" );
			lqa.bind( "object", instance );
		}

		log.debug( "connected types (one instance) is: " + lqa.bindAndGetSparql() );

		return engine.queryNoEx( lqa );
	}

	/**
	 * Gets the top-level connections between the give node types
	 *
	 * @param subtype
	 * @param objtype
	 * @param engine
	 * @return a collection of connections
	 */
	public static Collection<URI> getConnections( URI subtype, URI objtype, IEngine engine ) {
		String query = "SELECT DISTINCT ?rel {\n"
				+ "  ?s a ?subtype .\n"
				+ "  ?o a ?objtype .\n"
				+ "  ?s ?rel ?o .\n"
				+ "  ?rel rdfs:subPropertyOf ?semrel .\n"
				+ "  FILTER( ?rel != ?semrel ) .\n"
				+ "  FILTER( ?subtype != ?concept && ?subtype != ?skos ) .\n"
				+ "  FILTER( ?objtype != ?concept && ?objtype != ?skos ) .\n"
				+ "}";
		ListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList( query );
		lqa.bind( "semrel", engine.getSchemaBuilder().getRelationUri().build() );
		lqa.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		lqa.bind( "skos", SKOS.CONCEPT );
		lqa.bind( "subtype", subtype );
		lqa.bind( "objtype", objtype );
		Set<URI> edges = getTopLevelRelations( engine.queryNoEx( lqa ), engine );

		return edges;
	}

	/**
	 * Gets the subject (or object) types connected
	 *
	 * @param instances
	 * @param engine
	 * @param instanceIsSubject
	 * @return a list of top-level types
	 */
	public static List<URI> getConnectedConceptTypes( Collection<URI> instances,
			IEngine engine, boolean instanceIsSubject ) {

		StringBuilder query = new StringBuilder( "SELECT DISTINCT ?subtype ?objtype " )
				.append( "WHERE { " )
				.append( "  ?subject ?predicate ?object ." )
				.append( "  ?subject a ?subtype . FILTER( ?subtype != ?skos ) ." )
				.append( "  ?object a ?objtype . FILTER isUri( ?object ) ." )
				.append( "  FILTER( ?objtype != ?skos && ?objtype != owl:Thing && ?objtype != rdfs:Resource && ?objtype != ?concept ) ." )
				.append( "  FILTER( ?subtype != ?skos && ?subtype != owl:Thing && ?subtype != rdfs:Resource && ?subtype != ?concept ) ." )
				.append( "  MINUS { ?subject a ?object } " )
				.append( "} VALUES ?" );
		query.append( instanceIsSubject ? "subject " : "object" );
		query.append( "{" );
		query.append( Utility.implode( instances ) );
		query.append( "}" );

		OneVarListQueryAdapter<URI> lqa
				= OneVarListQueryAdapter.getUriList( query.toString() );
		lqa.bind( "skos", SKOS.CONCEPT );
		lqa.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		if ( instanceIsSubject ) {
			lqa.setVariableName( "objtype" );
		}
		else {
			lqa.setVariableName( "subtype" );
		}

		log.debug( "connected types (many instances) is: " + lqa.bindAndGetSparql() );

		return engine.queryNoEx( lqa );
	}

	/**
	 * Gets the set of top-level relations for the given set. The input may
	 * contain all "specific" relationships, all "top level", or a mixture of both
	 *
	 * @param rels
	 * @param engine
	 * @return the smallest set of relations that cover the input rels. All the
	 * returned URIs will be <code>rdfs:subClassOf semonto:Relation</code>
	 */
	private static Set<URI> getTopLevelRelations( Collection<URI> rels,
			IEngine engine ) {

		Set<URI> todo = new HashSet<>( rels ); // get unique set of input

		if ( todo.isEmpty() ) {
			return todo;
		}

		// this query gets the top level URI for any specific URI
		String query = "SELECT ?rel ?superrel WHERE {\n"
				+ "  ?rel rdfs:subPropertyOf ?superrel .\n"
				+ "  ?superrel rdfs:subPropertyOf ?semrel .\n"
				+ "  FILTER( ?superrel != ?semrel ) .\n"
				+ "  FILTER( ?rel != ?superrel ) .\n"
				+ "  FILTER( ?rel != ?semrel ) ."
				+ "  VALUES ?rel {" + Utility.implode( todo ) + "} ."
				+ "}";
		MapQueryAdapter<URI, URI> mqa = new MapQueryAdapter<URI, URI>( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				add( URI.class.cast( set.getValue( "rel" ) ),
						URI.class.cast( set.getValue( "superrel" ) ) );
			}

		};
		mqa.bind( "semrel", engine.getSchemaBuilder().getRelationUri().toUri() );

		mqa.useInferred( false );
		Map<URI, URI> inputOutput = engine.queryNoEx( mqa );

		// anything *not* in the inputOutput map wasn't a specific relation,
		// so it must be a top-level relation. Remove the specifics from the todo
		// set, and then add whatever's left to the output
		todo.removeAll( inputOutput.keySet() );
		for ( URI alreadytop : todo ) {
			inputOutput.put( alreadytop, alreadytop );
		}

		return new HashSet<>( inputOutput.values() );
	}
}
