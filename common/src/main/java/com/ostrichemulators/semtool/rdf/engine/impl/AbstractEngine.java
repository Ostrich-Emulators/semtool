/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;

import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.api.UpdateExecutor;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.util.Collection;
import org.apache.log4j.Level;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * An Abstract Engine that sets up the base constructs needed to create an
 * engine.
 */
public abstract class AbstractEngine implements IEngine {

	public static final String REMOTE_KEY = "repo-is-remote";

	private static final Logger log = Logger.getLogger( AbstractEngine.class );
	private static final Logger provenance = Logger.getLogger( "provenance" );

	private String engineName = null;
	private Properties prop = new Properties();
	private InsightManager insightEngine;
	private UriBuilder schemabuilder;
	private UriBuilder databuilder;
	private IRI baseuri;

	public AbstractEngine() {
	}

	/**
	 * Opens the database. This function calls (in this order)
	 * <ol>
	 * <li>{@link #startLoading(java.util.Properties) },
	 * <li>{@link #setUris(java.lang.String, java.lang.String) },
	 * <li>{@link #finishLoading(java.util.Properties) }
	 * </ol>
	 *
	 * @param initprops
	 */
	@Override
	public void openDB( Properties initprops ) throws RepositoryException {
		prop = Utility.copyProperties( initprops );

		if ( log.isDebugEnabled() ) {
			StringBuilder sb = new StringBuilder( "db properties:" );
			final String lf = System.getProperty( "line.separator" );
			for ( String key : initprops.stringPropertyNames() ) {
				sb.append( lf ).append( key ).append( "=>" ).append( initprops.getProperty( key ) );
			}
			log.debug( sb.toString() );
		}

		startLoading( prop );

		String baseuristr = prop.getProperty( Constants.BASEIRI_KEY, "" );
		String owlstarter = prop.getProperty( Constants.SEMOSS_IRI, null );
		if ( null == owlstarter ) {
			log.warn( "no schema URI set...using " + SEMONTO.NAMESPACE );
			owlstarter = SEMONTO.NAMESPACE;
		}
		baseuri = SimpleValueFactory.getInstance().createIRI(setUris( baseuristr, owlstarter ).stringValue() );

		finishLoading( prop );
	}

	/**
	 * Initiates the loading process with the given properties. If overridden,
	 * subclasses should be sure to call their superclass's version of this
	 * function in addition to whatever other processing they do.
	 *
	 * @param props
	 * @throws RepositoryException
	 */
	protected void startLoading( Properties props ) throws RepositoryException {
		insightEngine = createInsightManager();
	}

	protected abstract InsightManager createInsightManager() throws RepositoryException;

	protected void finishLoading( Properties props ) throws RepositoryException {
	}

	@Override
	public String getProperty( String key ) {
		return prop.getProperty( key );
	}

	@Override
	public final void setSchemaBuilder( UriBuilder ns ) {
		schemabuilder = ns;
	}

	@Override
	public UriBuilder getSchemaBuilder() {
		return schemabuilder;
	}

	@Override
	public UriBuilder getDataBuilder() {
		return databuilder;
	}

	@Override
	public void setDataBuilder( UriBuilder b ) {
		databuilder = b;
	}

	@Override
	public IRI getBaseIri() {
		return baseuri;
	}

	protected void setBaseUri( IRI base ) {
		baseuri = base;
	}

	/**
	 * An extension point for subclasses to set their base uris during the load
	 * process. This function should set the {@link #baseuri},
	 * {@link #databuilder}, {@link #schemabuilder}
	 *
	 * @param data the data builder uri property value from the properties file
	 * (possibly empty)
	 * @param schema the schema builder uri (never empty)
	 * @return this database's unique id. this will include some sort of UUID
	 * @throws org.eclipse.rdf4j.repository.RepositoryException
	 */
	protected abstract IRI setUris( String data, String schema ) throws RepositoryException;

	/**
	 * Update the "last modified" date of the dataset. This operation should fail
	 * silently if necessary
	 */
	protected abstract void updateLastModifiedDate();

	@Override
	public boolean isConnected() {
		return false;
	}

	/**
	 * Sets the name of the engine. This may be a lot of times the same as the
	 * Repository Name
	 *
	 * @param engineName - Name of the engine that this is being set to
	 */
	@Override
	public void setEngineName( String engineName ) {
		this.engineName = engineName;
	}

	/**
	 * Gets the engine name for this engine
	 *
	 * @return Name of the engine it is being set to
	 */
	@Override
	public String getEngineName() {
		return engineName;
	}

	/**
	 * Commits the database.
	 */
	@Override
	public void commit() {

	}

	@Override
	public InsightManager getInsightManager() {
		return insightEngine;
	}

	@Override
	public void updateInsights( InsightManager im ) throws EngineManagementException {
		log.warn( "updateInsights not yet implemented. This call does nothing." );
	}

	@Override
	public void setInsightManager( InsightManager ie ) {
		this.insightEngine = ie;
	}

	protected static File searchFor( String filename, File... dirs ) {
		if ( null != filename ) {
			File orig = new File( filename );
			if ( orig.isAbsolute() && orig.exists() ) {
				return orig;
			}

			final String basefilename = orig.getName();
			final List<String> dirparts
					= new ArrayList<>( Arrays.asList( filename.split( Pattern.quote(
													File.separator ) ) ) );
			// we're going to use the filename anyway, so we can remove it from the list
			if ( !dirparts.isEmpty() ) {
				dirparts.remove( dirparts.size() - 1 );
			}

			// special case: filename is db/XXX, we can skip the db part
			if ( !dirparts.isEmpty() && "db".equals( dirparts.get( 0 ) ) ) {
				dirparts.remove( 0 );
			}

			List<File> searchpath = new ArrayList<>();
			for ( File dir : dirs ) {
				searchpath.add( new File( dir, basefilename ) );
				StringBuilder sb = new StringBuilder();
				for ( String dirpart : dirparts ) {
					sb.append( File.separator ).append( dirpart );

					File check = new File( dir + sb.toString(), basefilename );
					searchpath.add( check );
				}
			}
			searchpath.add( orig );

			Set<String> checked = new HashSet<>(); // don't check the same place twice
			for ( File loc : searchpath ) {
				if ( !checked.contains( loc.getAbsolutePath() ) ) {
					try {
						checked.add( loc.getAbsolutePath() );

						log.debug( "looking for " + filename + " as " + loc.
								getAbsolutePath() );
						if ( loc.exists() ) {
							log.debug( "using " + loc.getCanonicalPath() );
							return loc.getCanonicalFile();
						}
					}
					catch ( IOException ioe ) {
						log.error( "could not access file: " + loc.getAbsolutePath(), ioe );
					}
				}
			}
		}

		return null;
	}

	@Override
	public void setProperty( String key, String val ) {
		if ( null == val || val.isEmpty() ) {
			prop.remove( key );
		}
		else {
			prop.setProperty( key, val );
		}
	}

	@Override
	public Properties getProperties() {
		Properties p = new Properties();
		p.putAll( prop );
		return p;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( null == engineName ? "unnamed" : engineName );
		sb.append( " (base:" );
		sb.append(null == databuilder ? "not set" : getBaseIri() ).append( ")" );
		return sb.toString();
	}

	public abstract boolean supportsSparqlBindings();

	@Override
	public void calculateInferences() throws RepositoryException {
		// nothing to do
	}

	public static final IRI getNewBaseUri() {
		IRI baseuri = UriBuilder.getBuilder( "http://os-em.com/semtool/database/" ).uniqueIri();
		return baseuri;
	}

	protected void logProvenance( UpdateExecutor ue ) {
		if ( provenance.isEnabledFor( Level.INFO ) ) {
			User user = Security.getSecurity().getAssociatedUser( this );
			provenance.info( user.getUsername() + ": " + ue.bindAndGetSparql() );
		}
	}

	protected void logProvenance( Collection<Statement> stmts ) {
		if ( provenance.isEnabledFor( Level.INFO ) ) {
			User user = Security.getSecurity().getAssociatedUser( this );
			for ( Statement stmt : stmts ) {
				provenance.info( user.getUsername() + ": " + stmt );
			}
		}
	}

	@Override
	public boolean isRemote() {
		return Boolean.parseBoolean( prop.getProperty( REMOTE_KEY,
				Boolean.FALSE.toString() ) );
	}
}
