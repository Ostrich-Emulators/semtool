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

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.util.Constants;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Holds the database in memory, and uses the Sesame API to facilitate querying
 * of RDF data sources.
 */
public class InMemorySesameEngine extends AbstractSesameEngine {

	public static final String SYNC_DELAY = "sync-delay";
	public static final String MEMSTORE_DIR = "memory-store-dir";
	public static final String INFER = "infer";

	private static final Logger log = Logger.getLogger( InMemorySesameEngine.class );
	private RepositoryConnection rc = null;
	private boolean iControlMyRc = false;

	protected InMemorySesameEngine() {
	}

	public static InMemorySesameEngine open() {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		try {
			eng.openDB( new Properties() );
		}
		catch ( Exception e ) {
			log.error( e );
		}
		return eng;
	}

	public static Properties generateProperties( File file ) {

		Properties props = new Properties();
		props.setProperty( MEMSTORE_DIR, ( "memorystore.data".equals( file.getName() )
				? file.getAbsoluteFile().getParent()
				: file.getPath() ) );
		props.setProperty( Constants.ENGINE_IMPL,
				InMemorySesameEngine.class.getCanonicalName() );
		return props;
	}

	@Override
	protected final void createRc( Properties p ) {
		if ( null != rc ) {
			// we've already have our rc created, so there's nothing to do here
			return;
		}

		Security.getSecurity().associateUser( this, new LocalUserImpl() );

		MemoryStore memstore = ( p.containsKey( MEMSTORE_DIR )
				? new MemoryStore( new File( p.getProperty( MEMSTORE_DIR ) ) )
				: new MemoryStore() );

		if ( p.containsKey( SYNC_DELAY ) ) {
			memstore.setSyncDelay( Long.parseLong( p.getProperty( SYNC_DELAY ) ) );
		}

		Sail sail = ( p.containsKey( INFER )
				? new ForwardChainingRDFSInferencer( memstore )
				: memstore );

		Repository repo = new SailRepository( sail );

		try {
			repo.initialize();
			rc = repo.getConnection();
		}
		catch ( Exception e ) {
			try {
				repo.shutDown();
			}
			catch ( Exception ex ) {
				log.error( ex, ex );
			}
		}

		setRepositoryConnection( rc, true );
	}

	/**
	 * Method setRepositoryConnection. Sets the repository connection.
	 *
	 * @param rc RepositoryConnection. The repository connection that this is
	 * being set to.
	 */
	private void setRepositoryConnection( RepositoryConnection rc,
			boolean takeControl ) {

		this.rc = rc;
		iControlMyRc = takeControl;

		try {

			URI baseuri = null;
			// if the baseuri isn't already set, then query the kb for void:Dataset
			RepositoryResult<Statement> rr
					= rc.getStatements( null, RDF.TYPE, SEMTOOL.Database, false );
			List<Statement> stmts = Iterations.asList( rr );
			for ( Statement s : stmts ) {
				baseuri = URI.class.cast( s.getSubject() );
				break;
			}

			if ( null == baseuri ) {
				// no base uri in the DB, so make a new one
				baseuri = getNewBaseUri();
				//rc.begin();
				rc.add( baseuri, RDF.TYPE, SEMTOOL.Database );
				//rc.add(  baseuri, SEMTOOL.ReificationModel, SEMTOOL.SEMTOOL_Reification );
				//rc.commit();
			}

			setBaseUri( baseuri );
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}
	}

	public void setBuilders( UriBuilder data, UriBuilder schema ) {
		this.setDataBuilder( data );
		this.setSchemaBuilder( schema );
	}

	/**
	 * Method getRepositoryConnection. Gets the repository connection.
	 *
	 * @return RepositoryConnection - the connection to the repository.
	 */
	@Override
	public RepositoryConnection getRawConnection() {
		return this.rc;
	}

	public Model toModel() throws RepositoryException {
		TreeModel model = new TreeModel();

		RepositoryResult<Statement> stmts
				= rc.getStatements( null, null, null, false );
		while ( stmts.hasNext() ) {
			model.add( stmts.next() );
		}

		return model;
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		if ( iControlMyRc ) {
			try {
				rc.close();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}

			Repository repo = rc.getRepository();
			try {
				repo.shutDown();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}

			rc = null;
		}
		super.closeDB();
	}
}
