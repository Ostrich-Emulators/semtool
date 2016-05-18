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
import java.util.Properties;
import org.apache.log4j.Logger;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.util.List;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Holds the database in memory, and uses the Sesame API to facilitate querying
 * of RDF data sources.
 */
public class InMemorySesameEngine extends AbstractSesameEngine {

	private static final Logger log = Logger.getLogger( InMemorySesameEngine.class );
	private RepositoryConnection rc = null;
	private boolean iControlMyRc = false;

	public InMemorySesameEngine() {
		super(new Properties());
		Properties initProps = new Properties();
		createRc( initProps );
		this.openDB(initProps);
	}

	public InMemorySesameEngine(Properties initProps, RepositoryConnection rc ) {
		super(initProps);
		setRepositoryConnection( rc, false );
		this.openDB(initProps);
	}

	public InMemorySesameEngine(Properties initProps, RepositoryConnection rc, boolean takeControl ) {
		super(initProps);
		this.openDB(initProps);
		setRepositoryConnection( rc, takeControl );
	}

	@Override
	protected final void createRc( Properties p ) {
		if ( null != rc ) {
			// we've already have our rc created, so there's nothing to do here
			return;
		}

		Security.getSecurity().associateUser( this, new LocalUserImpl() );
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		Repository repo = new SailRepository( inferencer );

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

			startLoading( new Properties() );

			URI baseuri = null;
			// if the baseuri isn't already set, then query the kb for void:Dataset
			RepositoryResult<Statement> rr
					= rc.getStatements(null, RDF.TYPE, SEMTOOL.Database, false );
			List<Statement> stmts = Iterations.asList( rr );
			for ( Statement s : stmts ) {
				baseuri = URI.class.cast( s.getSubject() );
				break;
			}

			if ( null == baseuri ) {
				// no base uri in the DB, so make a new one
				baseuri = getNewBaseUri();
				//rc.begin();
				rc.add(baseuri, RDF.TYPE, SEMTOOL.Database );
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
