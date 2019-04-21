/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.datastore;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

/**
 * An initial class for storing users, database locations for the webservice.
 *
 * @author ryan
 */
public class DataStore {

	private static final Logger log = Logger.getLogger( DataStore.class );
	private static final Pattern pat
			= Pattern.compile( "^(http.*/openrdf-sesame)/repositories/(.*)",
					Pattern.CASE_INSENSITIVE );

	private RepositoryConnection rc;

	public DataStore() {
		// must call setStoreLocation( ... ) before this instance is usable
	}

	public DataStore( String rawstoreloc ) {
		try {
			setStoreLocation( rawstoreloc );
		}
		catch ( RepositoryException | RepositoryConfigException e ) {
			log.fatal( e, e );
		}
	}

	/**
	 * Shuts down the current connection (if any), and opens a connection to the
	 * given store location. This function decodes the rawstoreloc to add some
	 * options. The available options are:
   * <code>
	 * <li>/tmp - create/open a local repository from the given path</li>
	 * <li>http://... - open a remote repository from the given url</li>
	 * <li>:memory:/tmp - create an in-memory repository, but persist the data at
	 * location</li>
	 * <li>:memory: - create the repository only in-memory</li>
	 * </code>
	 *
	 * @param rawstoreloc The location
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 */
	public final void setStoreLocation( String rawstoreloc )
			throws RepositoryException, RepositoryConfigException {
		log.debug( "setting RDF store location to: " + rawstoreloc );

		shutdown();

		Repository repository = null;

		boolean inmem = rawstoreloc.toLowerCase().startsWith( ":memory:" );
		String storeloc
				= ( inmem ? rawstoreloc.substring( ":memory:".length() ) : rawstoreloc );

		if ( inmem ) {
			MemoryStore ms = null;
			if ( ":memory:".equalsIgnoreCase( storeloc ) ) {
				ms = new MemoryStore();
			}
			else {
				File syncdir = new File( storeloc );
				if ( syncdir.exists() ) {
					log.debug( "reading persistence database at " + syncdir.getAbsolutePath() );
				}
				else {
					log.debug( "putting persistence database at " + syncdir.getAbsolutePath() );
					syncdir.mkdirs();
				}

				if ( syncdir.isDirectory() ) {
					ms = new MemoryStore( syncdir );
					ms.setSyncDelay( 3000L );
				}
			}

			repository = new SailRepository( ms );
		}
		else {
			repository = getRepository( storeloc );
		}

		if ( !repository.isInitialized() ) {
			repository.initialize();
		}

		rc = repository.getConnection();
	}

	private Repository getRepository( String storeloc )
			throws RepositoryException, RepositoryConfigException, IllegalArgumentException {
		Repository srcrepo = null;
		Matcher httpmatch = pat.matcher( storeloc );

		if ( httpmatch.matches() ) {
			// remote repository
			String httpstore = httpmatch.group( 1 );
			String reponame = httpmatch.group( 2 );
			srcrepo = new HTTPRepository( httpstore, reponame );
		}
		else {
			if ( !storeloc.isEmpty() ) {
				File datadir = new File( storeloc );
				log.debug( "datadir store is: " + datadir.getAbsolutePath() );

				if ( !datadir.exists() ) { // make the data store if it's not there
					log.warn( "Creating a new database at " + storeloc );
					datadir.mkdirs();
				}

				if ( datadir.isDirectory() ) { // we have to have a directory					
					srcrepo = new SailRepository( new NativeStore( datadir ) );
				}
			}
		}

		if ( null == srcrepo ) {
			throw new IllegalArgumentException( "No data repository opened!" );
		}

		return srcrepo;
	}

	/**
	 * Gets the raw handle to the repository connection. This RC should only be
	 * used for db-operations. It must not be closed by the caller.
	 *
	 * @return
	 */
	public RepositoryConnection getConnection() {
		return rc;
	}

	private static void closeRc( RepositoryConnection closer ) {
		if ( null != closer ) {
			try {
				closer.close();
			}
			catch ( RepositoryException re ) {
				log.warn( re, re );
			}

			try {
				closer.getRepository().shutDown();
			}
			catch ( RepositoryException re ) {
				log.warn( re, re );
			}
		}

	}

	public void shutdown() {
		closeRc( rc );
	}
}
