/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.datastore;

import gov.va.semoss.user.RemoteUserImpl;
import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.web.datastore.vocabulary.WEBDS;
import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.security.DbAccess;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author ryan
 */
public class UserMapper implements DataMapper<User, String> {

	private static final Logger log = Logger.getLogger( UserMapper.class );
	private static final Map<UserProperty, URI> PROPMAP = new HashMap<>();
	private static final URI ACL_READ = new URIImpl( WEBDS.NAMESPACE + "acl/readonly" );
	private static final URI ACL_WRITE = new URIImpl( WEBDS.NAMESPACE + "acl/write" );

	static {
		PROPMAP.put( UserProperty.USER_ORG, FOAF.ORGANIZATION );
		PROPMAP.put( UserProperty.USER_EMAIL, FOAF.ONLINE_ACCOUNT );
		PROPMAP.put( UserProperty.USER_FULLNAME, FOAF.NAME );
	}

	@Autowired
	private DataStore store;

	/**
	 * Default constructor used for testing situations
	 */
	public UserMapper() {
		// TODO To support testing, will populate with a 
		// series of pre-populated users

	}

	@Override
	public DataStore getDataStore() {
		return store;
	}

	@Override
	public void setDataStore( DataStore store ) {
		this.store = store;
	}

	@Override
	public Collection<User> getAll() {
		RepositoryConnection rc = store.getConnection();
		List<User> databases = new ArrayList<>();
		try {
			for ( Statement stmt : Iterations.asList( rc.getStatements( null, RDF.TYPE,
					FOAF.PERSON, false ) ) ) {
				databases.add( getUser( stmt.getSubject(), rc ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		return databases;
	}

	@Override
	public User getOne( String name ) {
		for ( User usr : getAll() ) {
			if ( name.equals( usr.getUsername() ) ) {
				return usr;
			}
		}
		return null;
	}

	@Override
	public User create( User t ) throws Exception {
		RepositoryConnection rc = store.getConnection();
		UriBuilder urib = UriBuilder.getBuilder( WEBDS.NAMESPACE + "user" );

		try {
			rc.add( getCreateStatements( urib.uniqueUri(), t, rc.getValueFactory() ) );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return t;
	}

	public boolean exists( String username ) {
		RepositoryConnection rc = store.getConnection();

		try {
			return rc.hasStatement( null, FOAF.ACCOUNT,
					new LiteralImpl( username ), false );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return false;
	}

	@Override
	public void remove( User t ) throws Exception {
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
	public void update( User user ) throws Exception {
		RepositoryConnection rc = store.getConnection();
		try {
			Map<URI, DbAccess> accesses = getAccesses( user );

			Resource id = getId( user, rc );
			if ( null != id ) {
				rc.begin();
				rc.remove( id, null, null );
				rc.add( getCreateStatements( id, user, rc.getValueFactory() ) );

				addAccesses( id, accesses, rc );
				rc.commit();
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception x ) {
				log.warn( x, x );
			}
		}
	}

	public Map<URI, DbAccess> getAccesses( User user ) {
		Map<URI, DbAccess> accesses = new HashMap<>();

		RepositoryConnection rc = store.getConnection();
		try {
			Resource id = getId( user, rc );
			List<Statement> readstmts
					= Iterations.asList( rc.getStatements( id, ACL_READ, null, false ) );
			List<Statement> writestmts
					= Iterations.asList( rc.getStatements( id, ACL_WRITE, null, false ) );

			for ( Statement s : readstmts ) {
				accesses.put( URI.class.cast( s.getObject() ), DbAccess.READ );
			}
			for ( Statement s : writestmts ) {
				accesses.put( URI.class.cast( s.getObject() ), DbAccess.WRITE );
			}
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}

		return accesses;
	}

	public void setAccess( User user, DbInfo db, DbAccess dbaccess, DbAccess insaccess ) {
		Map<URI, DbAccess> map = new HashMap<>();
		map.put( new URIImpl( db.getDataUrl() ), dbaccess );
		map.put( new URIImpl( db.getInsightsUrl() ), dbaccess );
		setAccesses( user, map );
	}

	/**
	 * Sets the access level from the given map. To remove access from a DB, user
	 * {@link DbAccess#NONE}.
	 *
	 * @param user
	 * @param access a mapping of URIs (either {@link DbInfo#getDataUrl()} or
	 * {@link DbInfo#getInsightsUrl()}) and the access given
	 */
	public void setAccesses( User user, Map<URI, DbAccess> access ) {
		RepositoryConnection rc = store.getConnection();

		try {
			rc.begin();
			Resource id = getId( user, rc );
			addAccesses( id, access, rc );
			rc.commit();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception x ) {
				log.warn( x, x );
			}
		}
	}

	private static void addAccesses( Resource userid, Map<URI, DbAccess> accesses,
			RepositoryConnection rc ) throws RepositoryException {

		for ( Map.Entry<URI, DbAccess> en : accesses.entrySet() ) {
			URI dbid = en.getKey();
			if ( DbAccess.NONE == en.getValue() ) {
				rc.remove( userid, ACL_READ, dbid );
				rc.remove( userid, ACL_WRITE, dbid );
			}
			else {
				URI uri = ( DbAccess.READ == en.getValue() ? ACL_READ : ACL_WRITE );
				rc.add( new StatementImpl( userid, uri, dbid ) );
			}
		}
	}

	private static User getUser( Resource id, RepositoryConnection rc )
			throws RepositoryException {

		LinkedHashModel model = new LinkedHashModel( Iterations.asList(
				rc.getStatements( id, null, null, false ) ) );
		org.openrdf.model.Model namer = model.filter( id, FOAF.ACCOUNT, null );
		RemoteUserImpl user = new RemoteUserImpl( namer.objectString() );
		model.removeAll( namer );

		for ( Statement stmt : model ) {
			URI pred = stmt.getPredicate();
			String val = stmt.getObject().stringValue();

			for ( Map.Entry<UserProperty, URI> en : PROPMAP.entrySet() ) {
				if ( en.getValue().equals( pred ) ) {
					user.setProperty( en.getKey(), val );
				}
			}
		}

		return user;
	}

	private static Resource getId( User t, RepositoryConnection rc )
			throws RepositoryException {
		List<Statement> stmts = Iterations.asList( rc.getStatements( null, RDF.TYPE,
				FOAF.PERSON, false ) );
		Resource idToRemove = null;
		for ( Statement s : stmts ) {
			Resource sbj = s.getSubject();
			List<Statement> individuals
					= Iterations.asList( rc.getStatements( sbj, FOAF.ACCOUNT, null, false ) );
			for ( Statement ind : individuals ) {
				if ( ind.getObject().stringValue().equals( t.getUsername() ) ) {
					idToRemove = sbj;
				}
			}
		}

		return idToRemove;
	}

	private static Collection<Statement> getCreateStatements( Resource id, User t,
			ValueFactory vf ) {
		List<Statement> stmts = new ArrayList<>();
		stmts.add( new StatementImpl( id, RDF.TYPE, FOAF.PERSON ) );
		stmts.add( new StatementImpl( id, FOAF.ACCOUNT, vf.createLiteral( t.getUsername() ) ) );

		for ( Map.Entry<UserProperty, URI> en : PROPMAP.entrySet() ) {
			UserProperty prop = en.getKey();

			String str = t.getProperty( prop );
			if ( !str.trim().isEmpty() ) {
				stmts.add( new StatementImpl( id, en.getValue(), vf.createLiteral( str ) ) );
			}
		}

		return stmts;
	}

}
