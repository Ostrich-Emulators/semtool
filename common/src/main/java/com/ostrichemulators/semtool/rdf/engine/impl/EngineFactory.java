/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException.ErrorCode;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.RepositoryException;

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
	 *
	 * @return
	 */
	public static IEngine memory() {
		return InMemorySesameEngine.open();
	}

	/**
	 * Creates and opens an IEngine based on some heuristics of the given
	 * properties. Of particular interest, {@link Constants#ENGINE_IMPL} will
	 * create the given engine no questions asked.
	 *
	 * @param props
	 * @return
	 * @throws
	 * com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException
	 */
	public static IEngine getEngine( Properties props ) throws EngineManagementException {
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
			throw new EngineManagementException( ErrorCode.UNKNOWN,
					"unable to determine engine type" );
		}
		else {
			try {
				engine = klass.getDeclaredConstructor().newInstance();
				engine.openDB( props );
			}
			catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e ) {
				throw new EngineManagementException( ErrorCode.UNKNOWN,
						"Unable to determine engine type", e );
			}
			catch ( RepositoryException e ) {
				throw new EngineManagementException( ErrorCode.BAD_CONNECTION, e );
			}
		}

		return engine;
	}

	/**
	 * Creates and opens an IEngine based on the given file's extension
	 *
	 * @param file
	 * @return
	 * @throws
	 * com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException
	 */
	public static IEngine getEngine( File file ) throws EngineManagementException {
		IEngine engine = null;
		final String name = ( null == file ? "" : file.getName() );
		final String abspath = ( null == file ? "null" : file.getAbsolutePath() );
		Properties props = null;
		try {
			if ( FilenameUtils.isExtension( name, Arrays.asList( "prop", "properties" ) ) ) {
				engine = getEngine( Utility.loadProp( file ) );
			}
			else if ( FilenameUtils.isExtension( name, "jnl" ) ) {
				throw new IllegalArgumentException( "BigData/Blazegraph is no longer supported" );
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
			else if ( null != file && file.isDirectory() ) {
				// default behaviour: assume it's a plain old openrdf triplestore
				engine = new SesameEngine();
				props = SesameEngine.generateProperties( file );
			}

			if ( !( null == engine || null == props ) ) {
				engine.openDB( props );
				if ( null == engine.getProperty( Constants.SMSS_LOCATION ) ) {
					engine.setProperty( Constants.SMSS_LOCATION, abspath );
				}
			}
		}
		catch ( IOException ioe ) {
			throw new EngineManagementException( ErrorCode.FILE_ERROR, ioe );
		}
		catch ( RepositoryException ioe ) {
			throw new EngineManagementException( ErrorCode.BAD_CONNECTION, ioe );
		}

		return engine;
	}

	public static IEngine getEngine( String fileOrUrl ) throws EngineManagementException {
		if ( Utility.isFile( fileOrUrl ) ) {
			return getEngine( new File( fileOrUrl ) );
		}
		else {
			// this is a remote DB
			Properties props = new Properties();
			props.setProperty( SesameEngine.REMOTE_KEY, Boolean.TRUE.toString() );
			props.setProperty( SesameEngine.REPOSITORY_KEY, fileOrUrl );
			props.setProperty( Constants.SMSS_LOCATION, fileOrUrl );
			return getEngine( props );
		}
	}
}
