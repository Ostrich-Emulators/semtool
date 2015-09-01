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
package gov.va.semoss.rdf.engine.impl;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.security.Security;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * An Abstract Engine that sets up the base constructs needed to create an
 * engine.
 */
public abstract class AbstractEngine implements IEngine {

	private static final Logger log = Logger.getLogger( AbstractEngine.class );

	private String engineName = null;
	protected Properties prop = new Properties();
	private InsightManager insightEngine;
	private UriBuilder schemabuilder;
	private UriBuilder databuilder;
	private URI baseuri;

	/**
	 * Opens the database. This function calls (in this order) {@link #loadAllProperties(java.util.Properties,
	 * java.lang.String, java.io.File...) }, {@link #setUris(java.lang.String, java.lang.String) },
	 * {@link #finishLoading(java.util.Properties) }
	 *
	 * @param initprops
	 */
	@Override
	public void openDB( Properties initprops ) {
		try {
			File[] searchpath = makeSearchPath( initprops );

			prop = loadAllProperties( initprops, engineName, searchpath );
			startLoading( prop );

			String baseuristr = prop.getProperty( Constants.BASEURI_KEY, "" );
			String owlstarter = prop.getProperty( Constants.SEMOSS_URI,
					DIHelper.getInstance().getProperty( Constants.SEMOSS_URI ) );
			if ( null == owlstarter ) {
				log.warn( "no schema URI set anywhere...using "
						+ Constants.DEFAULT_SEMOSS_URI );
				owlstarter = Constants.DEFAULT_SEMOSS_URI;
			}
			baseuri = new URIImpl( setUris( baseuristr, owlstarter ).stringValue() );

			String dreamerfileloc = prop.getProperty( Constants.DREAMER );
			if ( null != dreamerfileloc ) {
				Properties legacyquestions = Utility.loadProp( new File( dreamerfileloc ) );
				loadLegacyInsights( legacyquestions );
			}

			String ontofileloc = prop.getProperty( Constants.OWLFILE );
			if ( null != ontofileloc ) {
				loadLegacyOwl( ontofileloc );
			}

			finishLoading( prop );
		}
		catch ( IOException | RepositoryException e ) {
			log.error( e );
		}
	}

	/**
	 * Loads the insights (questions and perspectives) from the given properties.
	 * The default behavior is to do nothing
	 *
	 * @param props the insights
	 * @throws org.openrdf.repository.RepositoryException
	 */
	protected void loadLegacyInsights( Properties props ) throws RepositoryException {
		log.warn( "this engine type does load legacy insights" );
	}

	/**
	 * Loads the metadata information from the given file.
	 *
	 * @param ontoloc the location of the owl file. It is guaranteed to exist
	 */
	protected abstract void loadLegacyOwl( String ontoloc );

	/**
	 * Loads and optionally modifies the given properties. This is the first step
	 * in {@link #openDB(java.util.Properties) }
	 *
	 * @param props the properties
	 * @param ename the engine name
	 * @param searchpath where to look for any files specified in
	 * <code>props</code>
	 * @return the modified properties
	 * @throws IOException
	 */
	protected Properties loadAllProperties( Properties props, String ename,
			File... searchpath ) throws IOException {

		Properties newprops = Utility.copyProperties( props );
		Properties ontoProp = new Properties( newprops );
		Properties dreamerProp = new Properties( ontoProp );

		String questionPropFile = props.getProperty( Constants.DREAMER,
				getDefaultName( Constants.DREAMER, ename ) );
		File dreamer = searchFor( questionPropFile, searchpath );
		if ( null != dreamer ) {
			try {
				Utility.loadProp( dreamer, dreamerProp );
				newprops.setProperty( Constants.DREAMER, dreamer.getCanonicalPath() );
			}
			catch ( IOException ioe ) {
				log.warn( ioe, ioe );
			}
		}

		File owlf = searchFor( props.getProperty( Constants.OWLFILE,
				getDefaultName( Constants.OWLFILE, ename ) ), searchpath );
		if ( owlf != null ) {
			newprops.setProperty( Constants.OWLFILE, owlf.getCanonicalPath() );
		}

		File ontof = searchFor( props.getProperty( Constants.ONTOLOGY,
				getDefaultName( Constants.ONTOLOGY, ename ) ), searchpath );
		if ( ontof != null ) {
			newprops.setProperty( Constants.ONTOLOGY, ontof.getCanonicalPath() );
			Utility.loadProp( ontof, ontoProp );
		}

		return newprops;
	}

	/**
	 * Makes an array of directories to search to resolve file locations in the
	 * properties
	 *
	 * @param props the properties (should contain {@link Constants#SMSS_LOCATION}
	 * @return an array of directories to search for files
	 */
	private File[] makeSearchPath( Properties props ) {
		Set<File> searchpath = new HashSet<>();

		File propfile = new File( props.getProperty( Constants.SMSS_LOCATION, "." ) );
		File propdir = propfile.getParentFile();
		String verprop = props.getProperty( Constants.SMSS_VERSION_KEY, "0.0" );

		// support the old path resolution, but we really check all over
		// because the paths inside the properties files might also be wrong
		if ( Double.parseDouble( verprop ) < 1.0 ) {
			// these are legacy locations
			File dbdir
					= ( null == engineName ? propdir
							: new File( propdir, this.engineName ) );

			String basefolder
					= DIHelper.getInstance().getProperty( Constants.BASE_FOLDER );
			final File BASEDIR = new File( basefolder );
			searchpath.add( dbdir );
			searchpath.add( BASEDIR );
		}

		searchpath.add( propdir );

		String searchpathprop = props.getProperty( Constants.SMSS_SEARCHPATH, "" );
		if ( !searchpathprop.isEmpty() ) {
			for ( String p : searchpathprop.split( ";" ) ) {
				File f = new File( p );
				searchpath.add( f );
			}
		}

		return searchpath.toArray( new File[0] );
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
	public org.openrdf.model.URI getBaseUri() {
		return baseuri;
	}

	protected void setBaseUri( URI base ) {
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
	 */
	protected abstract URI setUris( String data, String schema );

	@Override
	public void closeDB() {
		if ( null != insightEngine ) {
			insightEngine.release();
		}
	}

	/**
	 * Update the "last modified" date of the dataset. This operation should fail
	 * silently if necessary
	 */
	protected abstract void updateLastModifiedDate();

	/**
	 * Returns whether or not an engine is currently connected to the data store.
	 * The connection becomes true when {@link #openDB(String)} is called and the
	 * connection becomes false when {@link #closeDB()} is called.
	 *
	 * @return true if the engine is connected to its data store and false if it
	 * is not
	 */
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
	public WriteableInsightManager getWriteableInsightManager() {
		log.warn( "getting a non-committing WriteableInsightManager (this isn't what you want)" );
		return new WriteableInsightManagerImpl( insightEngine,
				Security.getSecurity().getAssociatedUser( this ) ) {

					@Override
					public void commit() {
						log.warn( "this WriteableInsightManager doesn't write" );
					}
				};
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

	/**
	 * Retrieves the "by convention" name for the given file type
	 *
	 * @param filetype one of:
	 * {@link Constants#ONTOLOGY}, {@link Constants#DREAMER}, or
	 * {@link Constants#OWLFILE}
	 * @param engineName the name of the engine
	 *
	 * @return the default name for the given file
	 *
	 * @throws IllegalArgumentException if an unknown file type arg is given
	 */
	public static String getDefaultName( String filetype, String engineName ) {
		switch ( filetype ) {
			case Constants.DREAMER:
				return engineName + "_Questions.properties";
			case Constants.ONTOLOGY:
				return engineName + "_Custom_Map.prop";
			case Constants.OWLFILE:
				return engineName + "_OWL.OWL";
			default:
				throw new IllegalArgumentException( "unhandled file type: " + filetype );
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( null == engineName ? "unnamed" : engineName );
		sb.append( " (base:" );
		sb.append( null == databuilder ? "not set" : getBaseUri() ).append( ")" );
		return sb.toString();
	}

	public abstract boolean supportsSparqlBindings();

	@Override
	public boolean serverIsRunning() {
		return false;
	}

	@Override
	public boolean isServerSupported() {
		return false;
	}

	@Override
	public void startServer( int port ) {
		log.error(	"Server mode is not supported. Please check isServerSupported() "
				+ "before calling startServer(int)" );
	}

	@Override
	public void stopServer() {
	}

	@Override
	public java.net.URI getServerUri() {
		return null;
	}

	@Override
	public void calculateInferences() throws RepositoryException {
		// nothing to do
	}

	public static final URI getNewBaseUri() {
		URI baseuri = UriBuilder.getBuilder( "http://semoss.va.gov/database/" ).uniqueUri();
		return baseuri;
	}
}
