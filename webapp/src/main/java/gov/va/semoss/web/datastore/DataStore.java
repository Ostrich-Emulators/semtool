/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.datastore;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

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

	/**
	 * Creates a new datastore. Any existing connection is shut down first. This
	 * function decodes the rawstoreloc to add some options. The available options
	 * are:
   * <code>
	 * <li>/tmp - create/open a local repository from the given path</li>
	 * <li>http://... - open a remote repository from the given url</li>
	 * <li>:memory:/tmp - create an in-memory repository, but persist the data at
	 * location</li>
	 * <li>:memory: - create the repository only in-memory</li>
	 * </code>
	 *
	 * @param rawstoreloc The location
	 */
	public DataStore( String rawstoreloc ) {

		try {
			Repository repository = null;

			log.debug( "setting RDF store location to: " + rawstoreloc );

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
					if ( !syncdir.exists() ) {
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
		catch ( RepositoryException | RepositoryConfigException re ) {
			log.fatal( re, re );
		}

		if ( null == rc ) {
			throw new IllegalArgumentException( "No data repository opened!" );
		}
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

				if ( !datadir.exists() ) { // make the data store if it's not there
					log.warn( "Creating a new database at " + storeloc );
					datadir.mkdirs();
				}

				if ( !datadir.isFile() ) { // we have to have a directory					
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
