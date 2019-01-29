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
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.poi.main.XlsWriter.NodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;

import com.ostrichemulators.semtool.util.Utility;
import java.util.Arrays;
import java.util.Collection;

import java.util.function.Consumer;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;

public class DBToLoadingSheetExporter {

	private static final Logger log = Logger.getLogger( DBToLoadingSheetExporter.class );
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
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );
		Set<IRI> nodes = sm.getTopLevelConcepts();

		ImportData data = EngineUtil2.createImportData( engine );

		if ( runNodeExport ) {
			exportNodes( nodes, data );
		}

		if ( runRelationshipExport ) {
			exportAllRelationships( nodes, data );
		}

		return data;
	}

	private static LoadingSheetData convertToNodes( IRI subjectType,
			Collection<NodeAndPropertyValues> data, Set<IRI> properties,
			Map<IRI, String> labels ) {

		properties.remove( RDFS.LABEL ); // don't add an extra label column

		String sheetname = labels.get( subjectType );
		LoadingSheetData ret = LoadingSheetData.nodesheet( sheetname );
		for ( IRI prop : properties ) {
			ret.addProperty( labels.get( prop ) );
		}

		for ( NodeAndPropertyValues nap : data ) {
			String sname = labels.get( nap.getSubject() );
			LoadingNodeAndPropertyValues ls = ret.add( sname );

			for ( Map.Entry<IRI, Value> en : nap.entrySet() ) {
				ls.put( labels.get( en.getKey() ), en.getValue() );
			}
		}

		return ret;
	}

	private static LoadingSheetData convertToRelationshipLoadingSheetData( IRI subjectType,
			IRI predType, IRI objectType, Collection<NodeAndPropertyValues> data, Set<IRI> properties,
			Map<Resource, String> labels ) {

		properties.remove( RDFS.LABEL ); // don't add an extra label column

		String stname = labels.get( subjectType );
		String otname = labels.get( objectType );
		String relname = labels.get( predType );

		LoadingSheetData ret = LoadingSheetData.relsheet( stname, otname, relname );
		for ( IRI prop : properties ) {
			ret.addProperty( labels.get( prop ) );
		}

		for ( NodeAndPropertyValues nap : data ) {
			String sname = labels.get( nap.getSubject() );
			String oname = labels.get( nap.getObject() );
			LoadingNodeAndPropertyValues ls = ret.add( sname, oname );

			for ( Map.Entry<IRI, Value> en : nap.entrySet() ) {
				ls.put( labels.get( en.getKey() ), en.getValue() );
			}
		}

		return ret;
	}

	public void exportNodes( Collection<IRI> subjectTypes, ImportData data ) {
		Map<IRI, String> labels
				= Utility.getInstanceLabels( subjectTypes, engine );
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );

		for ( IRI subjectType : subjectTypes ) {
			Set<IRI> properties = sm.getPropertiesOf( subjectType );
			Collection<NodeAndPropertyValues> hash
					= getConceptInstanceData( subjectType, properties, labels );

			LoadingSheetData nlsd
					= convertToNodes( subjectType, hash, properties, labels );
			data.add( nlsd );
		}
	}

	public void exportOneRelationship( IRI subtype, IRI predicate, IRI objtype,
			ImportData data ) {

		List<Resource> needlabels
				= new ArrayList<>( Arrays.asList( subtype, predicate, objtype ) );
		Map<Resource, String> labels = Utility.getInstanceLabels( needlabels, engine );

		StructureManager sm = StructureManagerFactory.getStructureManager( engine );

		Set<IRI> properties = sm.getPropertiesOf( subtype, predicate, objtype );
		Collection<NodeAndPropertyValues> list
				= getOneRelationshipsData( subtype, predicate, objtype, properties, labels );

		LoadingSheetData rlsd = convertToRelationshipLoadingSheetData( subtype,
				predicate, objtype, list, properties, labels );
		data.add( rlsd );
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

	public void exportAllRelationships( Collection<IRI> subjectTypes, ImportData data ) {
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );
		for ( IRI subtype : subjectTypes ) {
			Model m = sm.getLinksBetween( subtype, Constants.ANYNODE );
			for ( Statement s : m.filter( subtype, null, null ) ) {
				exportOneRelationship( IRI.class.cast( s.getSubject() ),
						s.getPredicate(),
						IRI.class.cast( s.getObject() ), data );
			}
		}
	}

	/**
	 * Gets the subjects, predicates, and values for the given subject type
	 *
	 * @param subjectType the type nodes to retrieve
	 * @param properties all properties found in the data will be added to this
	 * set
	 * @param labels this map will be filled with IRI labels for later use
	 *
	 * @return a list of all the data for this subject type
	 */
	private Collection<NodeAndPropertyValues> getConceptInstanceData( IRI subjectType,
			final Collection<IRI> properties, final Map<IRI, String> labels ) {
		final Map<IRI, NodeAndPropertyValues> seen = new HashMap<>();

		String query
				= "SELECT ?s ?p ?prop WHERE { "
				+ " ?s ?p ?prop . "
				+ " ?s a ?subjType . "
				+ " FILTER ( isLiteral( ?prop ) ) "
				+ "} ";

		final List<IRI> needlabels = new ArrayList<>();
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				IRI s = IRI.class.cast( set.getValue( "s" ) );
				IRI p = IRI.class.cast( set.getValue( "p" ) );
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

		vqa.bind( "subjType", subjectType );

		try {
			getEngine().query( vqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( "Error processing subject " + subjectType + ": " + e, e );
		}

		// don't refetch stuff already in the labels cache
		needlabels.removeAll( labels.keySet() );
		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );
		return seen.values();
	}

	private Collection<NodeAndPropertyValues> getOneRelationshipsData( IRI subjectType,
			IRI predicateType, IRI objectType, final Collection<IRI> properties,
			Map<Resource, String> labels ) {
		log.debug( "getOneRelData for " + subjectType.getLocalName() + "->"
				+ predicateType.getLocalName() + "->" + objectType.getLocalName() );

		Model model = NodeDerivationTools.getInstances( subjectType, predicateType,
				objectType, properties, engine );
		Set<Resource> needlabels = new HashSet<>( model.subjects() );
		needlabels.addAll( model.predicates() );
		Model rels = new TreeModel();
		Model props = new TreeModel();
		model.forEach( new Consumer<Statement>() {

			@Override
			public void accept( Statement t ) {
				if ( t.getObject() instanceof IRI ) {
					rels.add( t );
					needlabels.add( Resource.class.cast( t.getObject() ) );
				}
				else {
					props.add( t );
				}
			}
		} );

		List<NodeAndPropertyValues> list = new ArrayList<>();

		for ( Statement s : rels ) {
			IRI in = IRI.class.cast( s.getSubject() );
			IRI out = IRI.class.cast( s.getObject() );

			NodeAndPropertyValues nap = new NodeAndPropertyValues( in, out );
			list.add( nap );

			for ( Statement p : props.filter( s.getPredicate(), null, null ) ) {
				nap.put( p.getPredicate(), p.getObject() );
			}
		}

		// don't refetch stuff already in the labels cache
		needlabels.removeAll( labels.keySet() );
		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );
		return list;
	}
}
