/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.datastore;

import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.web.datastore.vocabulary.WEBDS;
import gov.va.semoss.web.io.DbInfo;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class DbInfoMapper implements DataMapper<DbInfo, String> {

	private static final Logger log = Logger.getLogger(DbInfoMapper.class );
	private static final URI DATA_PREDICATE
			= new URIImpl( WEBDS.NAMESPACE + "dbinfo/dataurl" );
	private static final URI INSIGHTS_PREDICATE
			= new URIImpl( WEBDS.NAMESPACE + "dbinfo/insightsurl" );

	private DataStore store;

	public DataStore getDataStore() {
		return store;
	}

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
	public DbInfo create( DbInfo t ) {
		RepositoryConnection rc = store.getConnection();
		UriBuilder urib = UriBuilder.getBuilder( WEBDS.NAMESPACE + "dbinfo" );

		try {
			rc.add( getCreateStatements( urib.uniqueUri(), t, rc.getValueFactory() ) );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return t;
	}

	@Override
	public void remove( DbInfo t ) {
		RepositoryConnection rc = store.getConnection();

		try {
			Resource idToRemove = getId( t, rc );
			if ( null != idToRemove ) {
				rc.remove( idToRemove, null, null );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
	}

	@Override
	public void update( DbInfo data ) {
		RepositoryConnection rc = store.getConnection();
		try {
			Resource id = getId( data, rc );
			if ( null != id ) {
				rc.remove( id, null, null );
				rc.add( getCreateStatements( id, data, rc.getValueFactory() ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
	}

	private static DbInfo getDbInfo( Resource id, RepositoryConnection rc )
			throws RepositoryException {

		DbInfo dbi = new DbInfo();
		for ( Statement stmt : Iterations.asList( rc.getStatements( id, null, null, false ) ) ) {
			URI pred = stmt.getPredicate();
			if ( DATA_PREDICATE.equals( pred ) ) {
				dbi.setDataUrl( stmt.getObject().stringValue() );
			}
			if ( INSIGHTS_PREDICATE.equals( pred ) ) {
				dbi.setInsightsUrl( stmt.getObject().stringValue() );
			}
			if ( RDFS.LABEL.equals( pred ) ) {
				dbi.setName( stmt.getObject().stringValue() );
			}
		}

		return dbi;
	}

	private static Resource getId( DbInfo t, RepositoryConnection rc )
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
