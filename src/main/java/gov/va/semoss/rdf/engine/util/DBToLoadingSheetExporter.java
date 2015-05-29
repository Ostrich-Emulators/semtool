package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.text.DateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.poi.main.XlsWriter.NodeAndPropertyValues;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.util.Arrays;
import java.util.Collection;
import org.openrdf.model.Value;

public class DBToLoadingSheetExporter {

	private static final Logger logger = Logger.getLogger( DBToLoadingSheetExporter.class );
	private final Map<URI, URI> dupsToFilterOut = new HashMap<>(); // less specific -> more specific
	private IEngine engine;

	public DBToLoadingSheetExporter( IEngine eng ) {
		engine = eng;
	}

	/**
	 * Sets the engine to use for the queries.
	 *
	 * @param eng the engine to use. if null, will use the current engine
	 */
	public void setEngine( IEngine eng ) {
		engine = eng;
	}

	public IEngine getEngine() {
		return engine;
	}

	public ImportData runExport( boolean runNodeExport, boolean runRelationshipExport ) {
		List<URI> nodes = createConceptList();

		ImportData data = ImportData.forEngine( engine );

		if ( runNodeExport ) {
			exportNodes( nodes, data );
		}

		if ( runRelationshipExport ) {
			exportAllRelationships( nodes, data );
		}

		return data;
	}

	private static LoadingSheetData convertToNodes( URI subjectType,
			Collection<NodeAndPropertyValues> data, Set<URI> properties,
			Map<URI, String> labels ) {
		String sheetname = labels.get( subjectType );
		LoadingSheetData ret = LoadingSheetData.nodesheet( sheetname );
		for ( URI prop : properties ) {
			ret.addProperty( labels.get( prop ) );
		}

		for ( NodeAndPropertyValues nap : data ) {
			String sname = labels.get( nap.getSubject() );
			LoadingNodeAndPropertyValues ls = ret.add( sname );

			for ( Map.Entry<URI, Value> en : nap.entrySet() ) {
				ls.put( labels.get( en.getKey() ), en.getValue() );
			}
		}

		return ret;
	}

	private static LoadingSheetData convertToRelationshipLoadingSheetData( URI subjectType,
			URI predType, URI objectType, Collection<NodeAndPropertyValues> data, Set<URI> properties,
			Map<URI, String> labels ) {
		String stname = labels.get( subjectType );
		String otname = labels.get( objectType );
		String relname = labels.get( predType );

		LoadingSheetData ret = LoadingSheetData.relsheet( stname, otname, relname );
		for ( URI prop : properties ) {
			ret.addProperty( labels.get( prop ) );
		}

		for ( NodeAndPropertyValues nap : data ) {
			String sname = labels.get( nap.getSubject() );
			String oname = labels.get( nap.getObject() );
			LoadingNodeAndPropertyValues ls = ret.add( sname, oname );

			for ( Map.Entry<URI, Value> en : nap.entrySet() ) {
				ls.put( labels.get( en.getKey() ), en.getValue() );
			}
		}

		return ret;
	}

	public void exportNodes( List<URI> subjectTypes, ImportData data ) {
		subjectTypes.addAll( findSubclassNodesToAdd( subjectTypes ) );

		Map<URI, String> labels
				= Utility.getInstanceLabels( subjectTypes, engine );

		for ( URI subjectType : subjectTypes ) {
			Set<URI> properties = new HashSet<>();
			Collection<NodeAndPropertyValues> hash
					= getConceptInstanceData( subjectType, properties, labels );

			LoadingSheetData nlsd
					= convertToNodes( subjectType, hash, properties, labels );
			data.add( nlsd );
		}
	}

	public void exportTheseRelationships( List<URI[]> relationships, ImportData data ) {
		relationships.addAll( findSubclassRelationshipsToAdd( relationships ) );

		List<URI> needlabels = new ArrayList<>();
		for ( URI[] spo : relationships ) {
			needlabels.addAll( Arrays.asList( spo ) );
		}
		Map<URI, String> labels = Utility.getInstanceLabels( needlabels, engine );

		int count = 0;
		for ( URI[] spo : relationships ) {
			Set<URI> properties = new HashSet<>();
			Collection<NodeAndPropertyValues> list = getOneRelationshipsData( spo[0], spo[1],
					spo[2], properties, labels );

			LoadingSheetData rlsd
					= convertToRelationshipLoadingSheetData( spo[0], spo[1], spo[2], list,
							properties, labels );
			data.add( rlsd );

			count++;
			if ( logger.isDebugEnabled() && 0 == count % 1000 ) {
				logger.debug( "relcount: " + count );
			}
		}
	}

	public static File getDefaultExportFile( File exploc, String fileType, boolean isAll ) {
		if ( null != exploc && !exploc.isDirectory() ) {
			return exploc;
		}

		StringBuilder fileloc = new StringBuilder();

		// the base directory
		if ( null != exploc ) {
			fileloc.append( exploc.getAbsolutePath() );
		}
		else {
			fileloc.append( DIHelper.getInstance().getProperty( Constants.BASE_FOLDER ) );
			fileloc.append( File.separator ).append( "export" );
			fileloc.append( File.separator ).append( fileType );
		}

		// the filename    
		fileloc.append( File.separator );
		if ( isAll ) {
			fileloc.append( "All_" );
		}

		DateFormat df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT );
		String dateString = df.format( new Date() ).replace( ":", "" );

		fileloc.append( fileType ).append( "_LoadingSheet_" ).append( dateString ).append( ".xlsx" );

		return new File( fileloc.toString() );
	}

	public List<URI> createConceptList() {
		final List<URI> conceptList = new ArrayList<>();
		String query = "SELECT ?entity WHERE "
				+ "{ ?entity rdfs:subClassOf ?concept . FILTER( ?entity != ?concept ) }";
		OneVarListQueryAdapter<URI> qe
				= OneVarListQueryAdapter.getUriList( query, "entity" );
		qe.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );

		try {
			conceptList.addAll( engine.query( qe ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}
		return conceptList;
	}

	private List<URI> findSubclassNodesToAdd( List<URI> nodes ) {
		findDupsToFilterOut();

		List<URI> nodesToAdd = new ArrayList<>();
		for ( URI node : nodes ) {
			//e.g. if someone is asking to see all ApplicationModules, they will get a tab with the 
			//subclass VCAMPApplicationModules as well
			URI moreSpecificNodeType = dupsToFilterOut.get( node );
			if ( moreSpecificNodeType != null && !nodes.contains( moreSpecificNodeType ) ) {
				nodesToAdd.add( moreSpecificNodeType );
			}
		}

		return nodesToAdd;
	}

	private List<URI[]> findSubclassRelationshipsToAdd( List<URI[]> relationships ) {
		findDupsToFilterOut();

		List<URI[]> relationshipsToAdd = new ArrayList<>();
		for ( URI[] spo : relationships ) {
			//e.g. if someone is asking to see all ApplicationModules, they will get a tab with the 
			//subclass VCAMPApplicationModules as well
			URI moreSpecificNodeType = dupsToFilterOut.get( spo[0] );
			if ( moreSpecificNodeType != null ) {
				relationshipsToAdd.add( new URI[]{ moreSpecificNodeType, spo[1], spo[2] } );
			}
		}

		return relationshipsToAdd;
	}

	private void findDupsToFilterOut() {
		if ( !dupsToFilterOut.isEmpty() ) {
			return;
		}

		String q = "SELECT ?keeper ?duplicate WHERE {"
				+ " ?keeper ?subclass ?concept ."
				+ " ?duplicate ?subclass ?concept ."
				+ " ?keeper ?subclass ?duplicate ."
				+ " FILTER( ?duplicate != ?concept ) }";
		QueryExecutor<Void> qe = new QueryExecutorAdapter<Void>( q ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				Value dupe = set.getValue( "duplicate" );
				Value keep = set.getValue( "keeper" );
				dupsToFilterOut.put( URI.class.cast( dupe ), URI.class.cast( keep ) );
			}
		};

		qe.bind( "subclass", RDFS.SUBCLASSOF );
		qe.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		IEngine eng = ( null == engine ? DIHelper.getInstance().getRdfEngine()
				: engine );
		try {
			eng.query( qe );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		if ( logger.isDebugEnabled() ) {
			for ( Map.Entry<URI, URI> key : dupsToFilterOut.entrySet() ) {
				logger.debug( "Found duplicate, if an instance with type " + key.getKey()
						+ " has another instance of type " + key.getValue()
						+ ", then disregard the instance with type " + key.getKey() + "." );
			}
		}
	}

	public void exportAllRelationships( List<URI> subjectTypes, ImportData data ) {
		findDupsToFilterOut();

		List<URI[]> relsToExport = new ArrayList<>();

		for ( URI subjectType : subjectTypes ) {
			List<URI> objectTypes
					= getAllSubclassesOfConceptThatAreObjectsInATripleWithSubjectOfType( subjectType );
			for ( URI objectType : objectTypes ) {

				List<URI> predicateTypes = getPredicatesBetween( subjectType, objectType,
						getEngine() );
				for ( URI predicateType : predicateTypes ) {
					relsToExport.add( new URI[]{ subjectType, predicateType, objectType } );
				}
			}
		}

		exportTheseRelationships( relsToExport, data );
	}

	/**
	 * Gets the subjects, predicates, and values for the given subject type
	 *
	 * @param subjectType the type nodes to retrieve
	 * @param properties all properties found in the data will be added to this
	 * set
	 * @param labels this map will be filled with URI labels for later use
	 *
	 * @return a list of all the data for this subject type
	 */
	private Collection<NodeAndPropertyValues> getConceptInstanceData( URI subjectType,
			final Collection<URI> properties, final Map<URI, String> labels ) {
		final Map<URI, NodeAndPropertyValues> seen = new HashMap<>();

		String queryFilterLine = "";
		URI moreSpecificSubjectType = dupsToFilterOut.get( subjectType );
		if ( moreSpecificSubjectType != null ) {
			queryFilterLine = "  FILTER NOT EXISTS { ?s a ?specType } ";
		}

		String query
				= "SELECT ?s ?p ?prop WHERE { "
				+ " ?s ?p ?prop . "
				+ " ?s a  ?subjType . "
				+ queryFilterLine
				+ " FILTER ( isLiteral( ?prop ) ) "
				+ "} ";

		final List<URI> needlabels = new ArrayList<>();
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI s = URI.class.cast( set.getValue( "s" ) );
				URI p = URI.class.cast( set.getValue( "p" ) );
				Value prop = set.getValue( "prop" );

				needlabels.add( s );
				if ( !seen.containsKey( s ) ) {
					seen.put( s, new NodeAndPropertyValues( s ) );
				}

				if ( !p.equals( RDFS.LABEL ) ) { // don't add extra label column
					seen.get( s ).put( p, prop );
					needlabels.add( p );
					properties.add( p );
				}
			}
		};

		if ( null != moreSpecificSubjectType ) {
			vqa.bind( "specType", moreSpecificSubjectType );
		}
		vqa.bind( "subjType", subjectType );

		try {
			getEngine().query( vqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( "Error processing subject " + subjectType + ": " + e, e );
		}

		// don't refetch stuff already in the labels cache
		needlabels.removeAll( labels.keySet() );
		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );
		return seen.values();
	}

	private List<URI> getAllSubclassesOfConceptThatAreObjectsInATripleWithSubjectOfType( URI subjectType ) {
		List<URI> results = new ArrayList<>();
		UriBuilder owlb = engine.getSchemaBuilder();
		String query
				= "SELECT DISTINCT ?s WHERE { "
				+ "  ?in  a               ?stype . "
				+ "  ?out a               ?s ."
				+ "  ?s   rdfs:subClassOf ?concept ."
				+ "  ?in  ?p              ?out . "
				+ "} ";

		OneVarListQueryAdapter<URI> q = OneVarListQueryAdapter.getUriList( query, "s" );
		q.bind( "stype", subjectType );
		q.bind( "concept", owlb.getConceptUri().build() );
		try {
			results.addAll( getEngine().query( q ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		return results;
	}

	public static List<URI> getPredicatesBetween( URI subjectNodeType, URI objectNodeType,
			IEngine engine ) {
		String q
				= "SELECT DISTINCT ?relationship WHERE {"
				+ "?in  a ?stype . "
				+ "?out a ?otype . "
				+ "?in ?relationship ?out . "
				+ "MINUS{ ?relationship rdf:predicate ?p }"
				+ "}";
		OneVarListQueryAdapter<URI> varq = OneVarListQueryAdapter.getUriList( q, "relationship" );
		varq.useInferred( false );
		varq.bind( "stype", subjectNodeType );

		if ( !objectNodeType.equals( Constants.ANYNODE ) ) {
			varq.bind( "otype", objectNodeType );
		}

		List<URI> values;
		try {
			values = engine.query( varq );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			values = new ArrayList<>();
		}

		return values;
	}

	private Collection<NodeAndPropertyValues> getOneRelationshipsData( URI subjectType,
			URI predicateType, URI objectType, final Collection<URI> properties,
			Map<URI, String> labels ) {
		logger.debug( "getOneRelData for " + subjectType + "->" + predicateType + "->" + objectType );
		final Map<String, NodeAndPropertyValues> seen = new HashMap<>();
		final Set<URI> needlabels = new HashSet<>();

		String query = "SELECT * WHERE {"
				+ "  ?s a ?stype ."
				+ "  ?s ?relation ?o ."
				+ "  ?o a ?otype ."
				+ "  OPTIONAL {"
				+ "    ?edge rdf:predicate ?relation ."
				+ "    ?edge ?p ?prop . FILTER ( isLiteral( ?prop ) ) ."
				+ "    ?s ?edge ?o ."
				+ "   }"
				+ "}";

		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI in = URI.class.cast( set.getValue( "s" ) );
				URI rel = URI.class.cast( set.getValue( "relation" ) );
				URI out = URI.class.cast( set.getValue( "o" ) );
				Value prop = set.getValue( "prop" );

				String key = in.toString() + out.toString();
				if ( !seen.containsKey( key ) ) {
					seen.put( key, new NodeAndPropertyValues( in, out ) );
				}

				needlabels.add( in );
				needlabels.add( rel );
				needlabels.add( out );

				if ( null != prop ) {
					URI pred = URI.class.cast( set.getValue( "p" ) );
					seen.get( key ).put( pred, prop );
					properties.add( pred );
					needlabels.add( pred );
				}
			}
		};

		vqa.bind( "stype", subjectType );
		vqa.bind( "relation", predicateType );
		vqa.bind( "otype", objectType );
		vqa.useInferred( false );

		try {
			getEngine().query( vqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( query, e );
		}

		// don't refetch stuff already in the labels cache
		needlabels.removeAll( labels.keySet() );
		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );
		return seen.values();
	}
}
