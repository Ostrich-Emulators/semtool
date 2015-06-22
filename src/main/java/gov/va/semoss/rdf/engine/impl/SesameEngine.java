/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.engine.api.InsightManager;
import java.util.Properties;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author ryan
 */
public class SesameEngine extends AbstractSesameEngine {

	private Repository insights;
	private RepositoryConnection data;

	@Override
	protected void createRc( Properties props ) throws RepositoryException {
		String url = props.getProperty( REPOSITORY_KEY );
		String ins = props.getProperty( INSIGHTS_KEY );

		Repository repo = new HTTPRepository( url );
		repo.initialize();

		data = repo.getConnection();
		insights = new HTTPRepository( ins );
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
