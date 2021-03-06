/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.datastore;

import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.web.datastore.vocabulary.WEBDS;
import com.ostrichemulators.semtool.web.io.DbInfo;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author ryan
 */
public class DbInfoMapper implements DataMapper<DbInfo, String> {

	private static final Logger log = Logger.getLogger( DbInfoMapper.class );
	private static final URI DATA_PREDICATE
			= new URIImpl( WEBDS.NAMESPACE + "dbinfo/dataurl" );
	private static final URI INSIGHTS_PREDICATE
			= new URIImpl( WEBDS.NAMESPACE + "dbinfo/insightsurl" );

	@Autowired
	private DataStore store;

	@Override
	public DataStore getDataStore() {
		return store;
	}

	@Override
	public void setDataStore( DataStore store ) {
		this.store = store;
	}

	@Override
	public Collection<DbInfo> getAll() {
		RepositoryConnection rc = store.getConnection();
		List<DbInfo> databases = new ArrayList<>();
		try {
			for ( Statement stmt : Iterations.asList( rc.getStatements( null, RDF.TYPE,
					WEBDS.DBINFO, false ) ) ) {
				databases.add( getDbInfo( stmt.getSubject(), rc ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		return databases;
	}

	@Override
	public DbInfo getOne( String name ) {
		for ( DbInfo dbi : getAll() ) {
			if ( name.equals( dbi.getName() ) ) {
				return dbi;
			}
		}
		return null;
	}

	@Override
	public DbInfo create( DbInfo t ) throws Exception {
		RepositoryConnection rc = store.getConnection();
		UriBuilder urib = UriBuilder.getBuilder( WEBDS.NAMESPACE + "dbinfo" );
		URI uri = urib.uniqueUri();
		rc.begin();
		try{
			rc.add( getCreateStatements( uri, t, rc.getValueFactory() ) );
			rc.commit();
		}
		catch( RepositoryException re ){
			rc.rollback();
			throw re;
		}
		return t;
	}

	@Override
	public void remove( DbInfo t ) throws Exception {
		RepositoryConnection rc = store.getConnection();

		Resource idToRemove = getId( t, rc );
		if ( null != idToRemove ) {
			try{
				rc.begin();
				rc.remove( idToRemove, null, null );
				rc.commit();
			}
			catch( RepositoryException re ){
				rc.rollback();
				throw re;
			}
		}
	}

	@Override
	public void update( DbInfo data ) throws Exception {
		RepositoryConnection rc = store.getConnection();
		Resource id = getId( data, rc );
		if ( null != id ) {
			try{
				rc.begin();
				rc.remove( id, null, null );
				rc.add( getCreateStatements( id, data, rc.getValueFactory() ) );
				rc.commit();
			}
			catch( RepositoryException re ){
				rc.rollback();
				throw re;
			}
		}
	}

	public static DbInfo getDbInfo( Resource id, RepositoryConnection rc )
			throws RepositoryException {

		DbInfo dbi = new DbInfo();
		for ( Statement stmt : Iterations.asList( rc.getStatements( id, null, null, false ) ) ) {
			URI pred = stmt.getPredicate();
			String val = stmt.getObject().stringValue();
			if ( DATA_PREDICATE.equals( pred ) ) {
				dbi.setDataUrl( val );
			}
			else if ( INSIGHTS_PREDICATE.equals( pred ) ) {
				dbi.setInsightsUrl( val );
			}
			else if ( RDFS.LABEL.equals( pred ) ) {
				dbi.setName( val );
			}
		}

		return dbi;
	}

	public static Resource getId( DbInfo t, RepositoryConnection rc )
			throws RepositoryException {
		List<Statement> stmts = Iterations.asList( rc.getStatements( null, RDF.TYPE,
				WEBDS.DBINFO, false ) );
		Resource idToRemove = null;
		for ( Statement s : stmts ) {
			Resource sbj = s.getSubject();
			List<Statement> individuals
					= Iterations.asList( rc.getStatements( sbj, RDFS.LABEL, null, false ) );
			for ( Statement ind : individuals ) {
				if ( ind.getObject().stringValue().equals( t.getName() ) ) {
					idToRemove = sbj;
				}
			}
		}

		return idToRemove;
	}

	private static Collection<Statement> getCreateStatements( Resource id, DbInfo t,
			ValueFactory vf ) {
		List<Statement> stmts = new ArrayList<>();
		stmts.add( new StatementImpl( id, RDFS.LABEL, vf.createLiteral( t.getName() ) ) );
		stmts.add( new StatementImpl( id, RDF.TYPE, WEBDS.DBINFO ) );
		stmts.add( new StatementImpl( id, DATA_PREDICATE, vf.createLiteral( t.getDataUrl() ) ) );
		stmts.add( new StatementImpl( id, INSIGHTS_PREDICATE, vf.createLiteral( t.getInsightsUrl() ) ) );

		return stmts;
	}
}
