/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.bigdata.rdf.sail.BigdataSail;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class EngineFactory {

	private static final Logger log = Logger.getLogger( EngineFactory.class );

	protected EngineFactory() {

	}

	/**
	 * Gets a new in-memory engine
	 * @return
	 */
	public static IEngine memory() {
		IEngine eng = new InMemorySesameEngine();
		try {
			eng.openDB( new Properties() );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
		return eng;
	}

	/**
	 * Creates and opens an IEngine based on some heuristics of the given
	 * properties. Of particular interest, {@link Constants#ENGINE_IMPL} will
	 * create the given engine no questions asked.
	 *
	 * @param props
	 * @return
	 */
	public static IEngine getEngine( Properties props ) {
		IEngine engine = null;
		Class<? extends IEngine> klass = null;
		if ( props.containsKey( Constants.ENGINE_IMPL ) ) {
			Object obj;
			try {
				obj = Class.forName( props.getProperty( Constants.ENGINE_IMPL ) );
				klass = (Class<? extends IEngine>) obj;
			}
			catch ( ClassNotFoundException ex ) {
				log.error( "unknown engine type: "
						+ props.getProperty( Constants.ENGINE_IMPL ), ex );
			}
		}

		if ( null == klass ) {
			Map<String, Class<? extends IEngine>> heuristicKeys = new HashMap<>();
			heuristicKeys.put( BigdataSail.Options.FILE, BigDataEngine.class );
			heuristicKeys.put( InMemorySesameEngine.MEMSTORE_DIR, InMemorySesameEngine.class );
			heuristicKeys.put( AbstractSesameEngine.REMOTE_KEY, SesameEngine.class );

			for ( Map.Entry<String, Class<? extends IEngine>> en : heuristicKeys.entrySet() ) {
				if ( props.containsKey( en.getKey() ) ) {
					klass = en.getValue();
					break;
				}
			}
		}

		if ( null == klass ) {
			log.error( "unable to determine engine type" );
		}
		else {
			try {
				engine = klass.newInstance();
				engine.openDB( props );
			}
			catch ( InstantiationException | IllegalAccessException | RepositoryException e ) {
				log.error( e, e );
			}
		}

		return engine;
	}

	/**
	 * Creates and opens an IEngine based on the given file's extension
	 *
	 * @param file
	 * @return
	 */
	public static IEngine getEngine( File file ) {
		IEngine engine = null;
		final String name = ( null == file ? "" : file.getName() );
		final String abspath = ( null == file ? "null" : file.getAbsolutePath() );
		Properties props = null;
		try {
			if ( FilenameUtils.isExtension( name, Arrays.asList( "prop", "properties" ) ) ) {
				engine = getEngine( Utility.loadProp( file ) );
				engine.setProperty( Constants.SMSS_LOCATION, abspath );
			}
			else if ( FilenameUtils.isExtension( name, "jnl" ) ) {
				engine = new BigDataEngine();
				props = BigDataEngine.generateProperties( file );
			}
			else if ( name.isEmpty() ) {
				engine = new InMemorySesameEngine();
			}
			else if ( "memorystore.data".equals( name ) ) {
				engine = new InMemorySesameEngine();
				props = InMemorySesameEngine.generateProperties( file );
			}
			else if ( "tdb".equalsIgnoreCase( name ) ) {
				engine = new JenaEngine();
			}

			if ( !( null == engine || null == props ) ) {
				engine.setProperty( Constants.SMSS_LOCATION, abspath );
				engine.openDB( props );
			}
		}
		catch ( IOException | RepositoryException ioe ) {
			log.error( ioe, ioe );
		}

		return engine;
	}
}