package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;

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

import com.ostrichemulators.semtool.poi.main.XlsWriter.NodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.QueryExecutor;
import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;

import com.ostrichemulators.semtool.util.Utility;
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
		List<URI> nodes = NodeDerivationTools.createConceptList( getEngine() );

		ImportData data = EngineUtil2.createImportData( engine );

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

		properties.remove( RDFS.LABEL ); // don't add an extra label column

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

		properties.remove( RDFS.LABEL ); // don't add an extra label column

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
				+ " ?keeper rdfs:subClassOf ?concept ."
				+ " ?duplicate rdfs:subClassOf ?concept ."
				+ " ?keeper rdfs:subClassOf ?duplicate ."
				+ " FILTER( ?duplicate != ?concept ) }";
		QueryExecutor<Void> qe = new QueryExecutorAdapter<Void>( q ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				Value dupe = set.getValue( "duplicate" );
				Value keep = set.getValue( "keeper" );
				dupsToFilterOut.put( URI.class.cast( dupe ), URI.class.cast( keep ) );
			}
		};

		qe.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );
		IEngine eng = ( null == engine ? DIHelper.getInstance().getRdfEngine()
				: engine );
		try {
			// logger.debug( qe.bindAndGetSparql() );
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

		String q = "SELECT DISTINCT ?sub ?subtype ?superrel ?obj ?objtype WHERE {"
				+ "  ?sub a ?subtype ."
				+ "  ?sub ?rel ?obj ."
				+ "  ?objtype rdfs:subClassOf* semonto:Concept ."
				+ "  ?obj a ?objtype ."
				+ "  ?rel rdfs:subClassOf+ ?superrel ."
				+ "  ?superrel rdfs:subClassOf semonto:Relation ."
				+ "  FILTER ( ?objtype != semonto:Concept ) ."
				+ "  FILTER ( ?subtype != semonto:Concept ) ."
				+ "  FILTER ( ?superrel != ?rel ) ."
				+ "  FILTER ( ?superrel != semonto:Relation ) ."
				+ "}";
		ListQueryAdapter<URI[]> triples = new ListQueryAdapter<URI[]>( q ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI triple[] = {
					URI.class.cast( set.getValue( "subtype" ) ),
					URI.class.cast( set.getValue( "superrel" ) ),
					URI.class.cast( set.getValue( "objtype" ) ) };
				add( triple );
			}
		};
		triples.useInferred( true );

		for ( URI subjectType : subjectTypes ) {
			triples.bind( "subtype", subjectType );

			try {
				// logger.debug( triples.bindAndGetSparql() );
				List<URI[]> relsToExport = getEngine().query( triples );
				exportTheseRelationships( relsToExport, data );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
				logger.error( e, e );
			}
		}
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

				seen.get( s ).put( p, prop );
				needlabels.add( p );
				properties.add( p );
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

	private Collection<NodeAndPropertyValues> getOneRelationshipsData( URI subjectType,
			URI predicateType, URI objectType, final Collection<URI> properties,
			Map<URI, String> labels ) {

		// break this into two pieces...the first query gets the subjects and objects,
		// and the second query gets properties for edges (if they exist)
		logger.debug( "getOneRelData for " + subjectType.getLocalName() + "->"
				+ predicateType.getLocalName() + "->" + objectType.getLocalName() );
		final Map<String, NodeAndPropertyValues> seen = new HashMap<>();
		final Set<URI> needlabels = new HashSet<>();

		String query = "SELECT DISTINCT ?sub ?obj WHERE {"
				+ "  ?sub a ?subtype ."
				+ "  ?sub ?rel ?obj ."
				+ "  ?obj a ?objtype ."
				+ "  ?rel rdfs:subClassOf ?superrel ."
				+ "}";

		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI in = URI.class.cast( set.getValue( "sub" ) );
				URI out = URI.class.cast( set.getValue( "obj" ) );

				String key = in.toString() + out.toString();
				if ( !seen.containsKey( key ) ) {
					seen.put( key, new NodeAndPropertyValues( in, out ) );
				}

				needlabels.add( in );
				needlabels.add( out );
			}
		};

		vqa.bind( "subtype", subjectType );
		vqa.bind( "superrel", predicateType );
		vqa.bind( "objtype", objectType );
		logger.debug( vqa.bindAndGetSparql() );
		vqa.useInferred( false );

		try {
			getEngine().query( vqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( query, e );
		}

		String edgequery = "SELECT DISTINCT ?sub ?obj ?prop ?propval WHERE {"
				+ "  ?sub a ?subtype ."
				+ "  ?sub ?specificrel ?obj ."
				+ "  ?obj a ?objtype ."
				+ "  ?specificrel rdfs:subClassOf ?rel ."
				+ "  ?specificrel ?prop ?propval ."
				+ "  FILTER( isLiteral( ?propval ) ) ."
				+ "}";
		VoidQueryAdapter edges = new VoidQueryAdapter( edgequery ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI in = URI.class.cast( set.getValue( "sub" ) );
				URI out = URI.class.cast( set.getValue( "obj" ) );
				URI prop = URI.class.cast( set.getValue( "prop" ) );
				Value val = set.getValue( "propval" );

				String key = in.toString() + out.toString();
				if ( !seen.containsKey( key ) ) {
					seen.put( key, new NodeAndPropertyValues( in, out ) );
				}

				needlabels.add( prop );
				properties.add( prop );

				NodeAndPropertyValues nap = seen.get( key );
				nap.put( prop, val );
			}
		};

		edges.bind( "subtype", subjectType );
		edges.bind( "rel", predicateType );
		edges.bind( "objtype", objectType );
		edges.useInferred( false );

		try {
			getEngine().query( edges );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( "could not retrieve edge properties", e );
		}

		// don't refetch stuff already in the labels cache
		needlabels.removeAll( labels.keySet() );
		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );
		return seen.values();
	}
}
