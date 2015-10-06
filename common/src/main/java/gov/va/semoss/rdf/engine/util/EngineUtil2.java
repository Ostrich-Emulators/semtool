/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * A class to centralize Engine operations. This class is thread-safe, and if
 * started as a thread and used to mount repositories, those repositories can be
 * successfully unmounted later.
 *
 * @author ryan
 */
public class EngineUtil2 {

	private static final Logger log = Logger.getLogger( EngineUtil2.class );

	private EngineUtil2() {
	}

	public static void clear( IEngine engine ) throws RepositoryException {
		try {
			final Map<URI, Value> metas = engine.query( new MetadataQuery() );
			metas.remove( VAS.Database );

			engine.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					conn.remove( (Resource) null, null, null );
					ValueFactory vf = conn.getValueFactory();

					// re-add the metadata
					for ( Map.Entry<URI, Value> en : metas.entrySet() ) {
						conn.add( engine.getBaseUri(),
								URI.class.cast( EngineLoader.cleanValue( en.getKey(), vf ) ),
								EngineLoader.cleanValue( en.getValue(), vf ) );
					}
				}
			} );
		}
		catch ( MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	public static String getEngineLabel( IEngine engine ) {
		String label = engine.getEngineName();
		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		engine.queryNoEx( mq );
		String str = mq.getString();
		if ( null != str ) {
			label = str;
		}
		return label;
	}

	/**
	 * Gets the reification model URI from the given engine
	 *
	 * @param engine
	 * @return return the reification model, or {@link Constants#NONODE} if none
	 * is defined
	 */
	public static ReificationStyle getReificationStyle( IEngine engine ) {
		URI reif = Constants.NONODE;
		if ( null != engine ) {
			MetadataQuery mq = new MetadataQuery( VAS.ReificationModel );
			try {
				engine.query( mq );
				Value str = mq.getOne();
				reif = ( null == str ? Constants.NONODE : URI.class.cast( str ) );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
				// don't care
			}
		}

		return ReificationStyle.fromUri( reif );
	}

	public static ImportData createImportData( IEngine eng ) {
		ImportMetadata metas = null;
		if ( null == eng ) {
			metas = new ImportMetadata();
		}
		else {
			metas = new ImportMetadata( eng.getBaseUri(), eng.getSchemaBuilder(),
					eng.getDataBuilder() );
			metas.setNamespaces( eng.getNamespaces() );
		}

		return new ImportData( metas );
	}

	/**
	 * Prints all statements in the main database to the log's DEBUG output. This
	 * only works in if the logger prints debug output.
	 *
	 * @param eng
	 */
	public static void logAllDataStatements( IEngine eng ) {
		if ( log.isDebugEnabled() ) {
			eng.queryNoEx( new VoidQueryAdapter( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" ) {
				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					log.debug( set.getValue( "s" ) + " " + set.getValue( "p" ) + " "
							+ set.getValue( "o" ) );
				}
			} );
		}
	}

	/**
	 * Factory method for loading an engine.
	 *
	 * @param smssfile
	 *
	 * @return Loaded engine.
	 *
	 * @throws java.io.IOException
	 */
	public static IEngine loadEngine( File smssfile ) throws IOException {
		log.debug( "In Utility file name is " + smssfile );
		String smssloc = smssfile.getCanonicalPath();
		IEngine engine = null;
		String engineName = FilenameUtils.getBaseName( smssloc );

		if ( "jnl".equalsIgnoreCase(
				FilenameUtils.getExtension( smssfile.getName() ).toLowerCase() ) ) {
			// we're loading a BigData journal file, so jump straight to its ctor
			engine = new BigDataEngine( smssfile );
		}
		else {
			Properties props = Utility.loadProp( smssfile );
			engineName = props.getProperty( Constants.ENGINE_NAME, engineName );

			String engineClass = props.getProperty( Constants.ENGINE_IMPL );
			engineClass = engineClass.replaceAll( "prerna", "gov.va.semoss" );

			try {
				Class<IEngine> theClass = (Class<IEngine>) Class.forName( engineClass );
				engine = (IEngine) theClass.getConstructor( Properties.class ).newInstance( props );

			}
			catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				log.error( e );
			}
		}

		if ( null == engine ) {
			throw new IOException( "Could not create engine" );
		}

		if ( null == engine.getEngineName() && null != engineName ) {
			engine.setEngineName( engineName );
		}

		engine.setProperty( Constants.SMSS_LOCATION, smssloc );

		return engine;
	}

	/**
	 * Pair for {@link #loadEngine(java.io.File) }. Implementation simply calls
	 * {@link IEngine#closeDB() }
	 * @param eng
	 */
	public static void closeEngine( IEngine eng ) {
		eng.closeDB();
	}
}
