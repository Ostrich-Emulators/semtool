/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException.ErrorCode;
import com.ostrichemulators.semtool.rdf.engine.util.StatementSorter;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 *
 * @author ryan
 */
public class SesameEngine extends AbstractSesameEngine {

	private static final Logger log = Logger.getLogger( SesameEngine.class );
	private Repository insights;
	private RepositoryConnection data;

	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		String url = props.getProperty( REPOSITORY_KEY );
		String ins = props.getProperty( INSIGHTS_KEY );
		boolean remote = Boolean.parseBoolean( props.getProperty( REMOTE_KEY,
				Boolean.FALSE.toString() ) );

		if ( remote ) {
			Pattern pat = Pattern.compile( "^(.*)/repositories/(.*)" );
			Matcher m = pat.matcher( url );
			if ( m.find() ) {
				for ( int i = 0; i < m.groupCount(); i++ ) {
					log.debug( m.group( i ) );
				}
			}

			String username = props.getProperty( "username", "" );
			String password = props.getProperty( "password", "" );

			HTTPRepository repo = ( m.find()
					? new HTTPRepository( m.group( 1 ), m.group( 2 ) )
					: new HTTPRepository( url ) );
			if ( !username.isEmpty() ) {
				repo.setUsernameAndPassword( username, password );
			}
			repo.initialize();

			data = repo.getConnection();

			m.reset( ins );
			HTTPRepository tmp = ( m.find()
					? new HTTPRepository( m.group( 1 ), m.group( 2 ) )
					: new HTTPRepository( url ) );
			if ( !username.isEmpty() ) {
				tmp.setUsernameAndPassword( username, password );
			}
			insights = tmp;
		}
		else {
			Repository repo = new SailRepository( new ForwardChainingRDFSInferencer(
					new NativeStore( new File( url ) ) ) );
			insights = new SailRepository( new ForwardChainingRDFSInferencer(
					new NativeStore( new File( ins ) ) ) );
			repo.initialize();

			data = repo.getConnection();
		}
	}

	public static Properties generateProperties( File dir ) {
		Properties props = new Properties();
		props.setProperty( SesameEngine.REPOSITORY_KEY, new File( dir, "repo" ).toString() );
		props.setProperty( SesameEngine.INSIGHTS_KEY, new File( dir, "insights" ).toString() );
		props.setProperty( SesameEngine.REMOTE_KEY, Boolean.FALSE.toString() );
		props.setProperty( Constants.SMSS_LOCATION, dir.toString() );

		return props;
	}

	@Override
	protected RepositoryConnection getRawConnection() {
		return data;
	}

	@Override
	protected InsightManager createInsightManager() {
		return InsightManagerImpl.createFromRepository( insights );
	}

	@Override
	public void closeDB() {
		try {
			insights.shutDown();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
		super.closeDB();
	}

	@Override
	public void updateInsights( InsightManager im ) throws EngineManagementException {
		List<Statement> stmts = new ArrayList<>();
		User user = Security.getSecurity().getAssociatedUser( this );
		stmts.addAll( InsightManagerImpl.getModel( im, user ) );
		Collections.sort( stmts, new StatementSorter() );

		RepositoryConnection rc = null;
		try {
			rc = insights.getConnection();
			rc.begin();
			rc.clear();
			rc.add( stmts );
			rc.commit();
		}
		catch ( UnauthorizedException ue ) {
			throw new EngineManagementException( ErrorCode.ACCESS_DENIED, ue );
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
			if ( null != rc ) {
				try {
					rc.rollback();
				}
				catch ( RepositoryException re2 ) {
					log.error( re2, re2 );
				}
			}
			throw new EngineManagementException( ErrorCode.UNKNOWN, re );
		}
		finally {
			if ( null != rc ) {
				try {
					rc.close();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}
		}
	}
}
