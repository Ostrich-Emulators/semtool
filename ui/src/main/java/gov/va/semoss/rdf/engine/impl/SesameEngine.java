/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.engine.api.InsightManager;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author ryan
 */
public class SesameEngine extends AbstractSesameEngine {
	private static final Logger log = Logger.getLogger( SesameEngine.class);
	private Repository insights;
	private RepositoryConnection data;

	
	public SesameEngine(Properties initProps){
		super(initProps);
		this.openDB(initProps);
	}
	
	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		String url = props.getProperty( REPOSITORY_KEY );
		String ins = props.getProperty( INSIGHTS_KEY );

		Pattern pat = Pattern.compile( "^(.*)/repositories/(.*)" );
		Matcher m = pat.matcher( url );
		if( m.find( ) ){
			for( int i =0; i < m.groupCount(); i++ ){
				log.debug( m.group( i ) );
			}
		}
		
		
		HTTPRepository repo =( m.find() ? new HTTPRepository( m.group( 1 ), m.group( 2 ))
				: new HTTPRepository( url ) );
		repo.setUsernameAndPassword( "ryan", "1234" );
		repo.initialize();

		data = repo.getConnection();
		
		m.reset( ins );
		HTTPRepository tmp = ( m.find() ? new HTTPRepository( m.group( 1 ), m.group( 2 ))
				: new HTTPRepository( url ) );
		tmp.setUsernameAndPassword( "ryan", "1234" );
		insights = tmp;
	}

	@Override
	protected RepositoryConnection getRawConnection() {
		return data;
	}

	@Override
	protected InsightManager createInsightManager() {
		InsightManagerImpl imi = new InsightManagerImpl( insights );
		return imi;
	}
}
