/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
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
	private static final Pattern PAT = Pattern.compile( "^(.*)/repositories/(.*)" );
	private String insightsloc;
	private RepositoryConnection data;

	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		String url = props.getProperty( REPOSITORY_KEY );
		insightsloc = props.getProperty( INSIGHTS_KEY );
		Repository repo = getRawRepository( url );
		repo.initialize();
		data = repo.getConnection();
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

	private Repository getRawRepository( String loc ) throws RepositoryException {
		Properties props = getProperties();

		if ( null == loc ) {
			return null;
		}

		boolean remote = Boolean.parseBoolean( props.getProperty( REMOTE_KEY,
				Boolean.FALSE.toString() ) );

		Repository insightsrepo = null;

		if ( remote ) {
			Matcher m = PAT.matcher( loc );
			String username = props.getProperty( "username", "" );
			String password = props.getProperty( "password", "" );

			HTTPRepository tmp = ( m.find()
					? new HTTPRepository( m.group( 1 ), m.group( 2 ) )
					: new HTTPRepository( loc ) );
			if ( !username.isEmpty() ) {
				tmp.setUsernameAndPassword( username, password );
			}
			insightsrepo = tmp;
		}
		else {
			insightsrepo = new SailRepository( new ForwardChainingRDFSInferencer(
					new NativeStore( new File( loc ) ) ) );
		}

		return insightsrepo;
	}

	@Override
	protected InsightManager createInsightManager() {
		if ( null == insightsloc ) {
			return new InsightManagerImpl();
		}

		InsightManager im;
		Repository repo = null;
		try {
			repo = getRawRepository( insightsloc );
			repo.initialize();
			im = InsightManagerImpl.createFromRepository( repo );
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
			im = new InsightManagerImpl();
		}
		catch ( NullPointerException npe ) {
			im = new InsightManagerImpl();
		}
		finally {
			if ( null != repo ) {
				try {
					repo.shutDown();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}
		}

		return im;
	}

	@Override
	public void updateInsights( InsightManager im ) throws EngineManagementException {
		if ( null == insightsloc ) {
			log.warn( "No Insights location defined with this engine" );
			return;
		}

		User user = Security.getSecurity().getAssociatedUser( this );
		Repository repo = null;
		RepositoryConnection rc = null;
		try {
			repo = getRawRepository( insightsloc );
			repo.initialize();
			rc = repo.getConnection();
			rc.begin();
			rc.clear();
			rc.add( InsightManagerImpl.getModel( im, user ) );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
			im = new InsightManagerImpl();
		}
		catch ( NullPointerException npe ) {
			im = new InsightManagerImpl();
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
			if ( null != repo ) {
				try {
					repo.shutDown();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}
		}
	}
}
