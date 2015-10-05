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

import com.bigdata.journal.ITx;
import com.bigdata.journal.Journal;

import org.openrdf.repository.RepositoryException;
import gov.va.semoss.util.Constants;

import com.bigdata.rdf.rules.InferenceEngine;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.sail.CreateKBTask;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.task.AbstractApiTask;
import gov.va.semoss.rdf.engine.api.InsightManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import static gov.va.semoss.rdf.engine.impl.AbstractEngine.searchFor;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.StatementSorter;
import gov.va.semoss.user.LocalUserImpl;
import gov.va.semoss.user.Security;
import gov.va.semoss.user.User;
import gov.va.semoss.util.Utility;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * Big data engine serves to connect the .jnl files, which contain the RDF
 * database, to the java engine.
 */
public class BigDataEngine extends AbstractSesameEngine {

	private static final Logger log = Logger.getLogger( BigDataEngine.class );

	private Journal journal = null;
	private BigdataSailRepository repo = null;
	private BigdataSailRepositoryConnection rc = null;
	private BigdataSail sail = null;
	private BigdataSailRepository insightrepo = null;
	private InsightManagerImpl insightEngine = null;

	public BigDataEngine( Properties initProps ) {
		super( initProps );
		this.openDB( initProps );
	}

	public BigDataEngine( File jnl ) {
		this( generateProperties( jnl ) );
	}

	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		Properties rws = getRWSProperties( props );
		boolean isremote = Boolean.parseBoolean( props.getProperty( REMOTE_KEY, "false" ) );

		if ( isremote ) {
			String url = props.getProperty( REPOSITORY_KEY );
			String ins = props.getProperty( INSIGHTS_KEY );
			log.debug( "big data remote! " + url + " ... " + ins );
			throw new UnsupportedOperationException( "Remote Bigdata repositories are not yet supported" );
		}
		else {
			// users have full access to local DBs 
			Security.getSecurity().associateUser( this, new LocalUserImpl() );

			// the journal is the file itself
			journal = new Journal( rws );

			// the main KB
			rws.setProperty( BigdataSail.Options.NAMESPACE, "kb" );
			CreateKBTask ctor = new CreateKBTask( "kb", rws );
			try {
				AbstractApiTask.submitApiTask( journal, ctor ).get();
				AbstractTripleStore triples
						= AbstractTripleStore.class.cast( journal.getResourceLocator().
								locate( "kb", ITx.UNISOLATED ) );

				sail = new BigdataSail( triples );
				repo = new BigdataSailRepository( sail );
				repo.initialize();

				// the insights KB
				rws.setProperty( BigdataSail.Options.NAMESPACE, Constants.INSIGHTKB );
				CreateKBTask ctor2 = new CreateKBTask( Constants.INSIGHTKB, rws );
				AbstractApiTask.submitApiTask( journal, ctor2 ).get();
				AbstractTripleStore insights
						= AbstractTripleStore.class.cast( journal.getResourceLocator().
								locate( Constants.INSIGHTKB, ITx.UNISOLATED ) );
				BigdataSail insightSail = new BigdataSail( insights );
				insightrepo = new BigdataSailRepository( insightSail );
				insightrepo.initialize();
			}
			catch ( InterruptedException | ExecutionException e ) {
				log.fatal( e, e );
			}
			rc = repo.getConnection();
		}
	}

	@Override
	protected Properties loadAllProperties( Properties props, String engineName,
			File... searchpath ) throws IOException {
		Properties ret = super.loadAllProperties( props, engineName, searchpath );

		String rwspropfile
				= ret.getProperty( Constants.SMSS_RWSTORE_KEY, "RWStore.properties" );
		File rwsfile = searchFor( rwspropfile, searchpath );

		Properties rws = ( null == rwsfile ? props : Utility.loadProp( rwsfile ) );

		String jnlName
				= rws.getProperty( BigdataSail.Options.FILE, getEngineName() + ".jnl" );
		// fix the path for the jnl file
		boolean isremote = Boolean.parseBoolean( props.getProperty( REMOTE_KEY, "false" ) );
		if ( !isremote ) {
			File jnl = searchFor( jnlName, searchpath );
			ret.put( BigdataSail.Options.FILE, jnl.toString() );
		}
		return ret;
	}

	@Override
	protected RepositoryConnection getRawConnection() {
		return rc;
	}

	@Override
	protected InsightManager createInsightManager() throws RepositoryException {
		// create an in-memory KB, but copy everything from our jnl-based
		// KB to it

		BigdataSailRepositoryConnection insightrc = insightrepo.getReadOnlyConnection();
		insightEngine = new InsightManagerImpl();
		log.debug( "loading on-disk insights stmts: " + insightrc.size() );
		insightEngine.loadFromRepository( insightrc );
		return insightEngine;
	}

	private void copyInsightsToDisk( InsightManager im ) throws RepositoryException {
		// this function is a bit tricky...we want to:
		// 1) commit then close this engine's write-handle on the main KB
		// 2) open it on the Insights KB
		// 3) rewrite everything to the Insights KB
		// 4) close the Insights write handle
		// 5) re-open the write handle to the main KB

		List<Statement> stmts = new ArrayList<>();
		User user = Security.getSecurity().getAssociatedUser( this );
		stmts.addAll( InsightManagerImpl.getStatements( im, user ) );
		Collections.sort( stmts, new StatementSorter() );

		try {
			// 1
			rc.commit();
			rc.close();
		}
		catch ( Exception e ) {
			log.error( "unable to prepare repository for insights management", e );
			throw e;
		}

		try {
			// 2
			BigdataSailRepositoryConnection repoc = insightrepo.getConnection();
			// 3
			log.debug( "writing " + stmts.size() + " statements to on-disk insight kb" );
			// sort the statements so a later export looks nice (totally unnecessary,
			// but troubleshooting is easier)
			repoc.begin();
			repoc.clear();
			repoc.add( stmts );
			repoc.commit();
			// 4
			repoc.close();
			logProvenance( stmts );
		}
		finally {
			try {
				// 5
				rc = BigDataEngine.this.repo.getConnection();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}

		if ( log.isTraceEnabled() ) {
			File dumpfile
					= new File( FileUtils.getTempDirectory(), "semoss-outsights-committed.ttl" );
			try ( Writer w = new BufferedWriter( new FileWriter( dumpfile ) ) ) {
				TurtleWriter tw = new TurtleWriter( w );
				for ( Statement s : stmts ) {
					tw.handleStatement( s );
				}
			}
			catch ( Exception ioe ) {
				log.warn( ioe, ioe );
			}
		}
	}

	@Override
	protected void loadLegacyInsights( Properties props ) throws RepositoryException {
		// this gets called from the startup logic
		if ( !props.isEmpty() ) {
			insightEngine.loadLegacyData( props );
			copyInsightsToDisk( insightEngine );
		}
	}

	@Override
	public void updateInsights( InsightManager im ) throws EngineManagementException {
		try {
			copyInsightsToDisk( im );
			insightEngine.addAll( im.getPerspectives(), true );
		}
		catch ( UnauthorizedException ue ) {
			throw new EngineManagementException(
					EngineManagementException.ErrorCode.ACCESS_DENIED, ue );
		}
		catch ( RepositoryException re ) {
			throw new EngineManagementException(
					EngineManagementException.ErrorCode.UNKNOWN, re );
		}
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		super.closeDB();

		try {
			journal.close();
		}
		catch ( Exception e1 ) {
			log.warn( "could not close journal file", e1 );
		}
	}

	@Override
	public void calculateInferences() throws RepositoryException {
		try {
			log.debug( "start calculating inferences" );
			InferenceEngine ie = sail.getInferenceEngine();
			ie.computeClosure( null );
			updateLastModifiedDate();
			rc.commit();
			log.debug( "done calculating inferences" );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
	}

	public static Properties generateProperties( File jnl ) {
		Properties props = new Properties();
		props.setProperty( Constants.SMSS_LOCATION, jnl.toString() );
		props.setProperty( BigdataSail.Options.FILE, jnl.toString() );
		props.setProperty( Constants.ENGINE_IMPL,
				BigDataEngine.class.getCanonicalName() );
		props.setProperty( Constants.SMSS_VERSION_KEY, "1.0" );

		return props;
	}

	/**
	 * Gets the bigdata-specific properties
	 *
	 * @param prop all the properties to look through
	 *
	 * @return bigdata-specific properties
	 */
	private Properties getRWSProperties( Properties prop ) {
		Properties rws = new Properties();
		for ( String key : prop.stringPropertyNames() ) {
			if ( key.startsWith( "com.bigdata" ) ) {
				String val = prop.getProperty( key );
				rws.setProperty( key, val );
			}
		}

		return rws;
	}
}
