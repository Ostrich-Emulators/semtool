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
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class UserMapper implements DataMapper<User, String> {

	private static final Logger log = Logger.getLogger( UserMapper.class );
	private static final Map<UserProperty, URI> PROPMAP = new HashMap<>();

	static {
		PROPMAP.put( UserProperty.USER_ORG, FOAF.ORGANIZATION );
		PROPMAP.put( UserProperty.USER_EMAIL, FOAF.ONLINE_ACCOUNT );
		PROPMAP.put( UserProperty.USER_FULLNAME, FOAF.NAME );
	}

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
	public void update( User data ) throws Exception {
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

	private static User getUser( Resource id, RepositoryConnection rc )
			throws RepositoryException {

		RemoteUserImpl user = new RemoteUserImpl();

		for ( Statement stmt : Iterations.asList( rc.getStatements( id, null, null, false ) ) ) {
			URI pred = stmt.getPredicate();
			String val = stmt.getObject().stringValue();

			if ( FOAF.ACCOUNT.equals( pred ) ) {
				// user.setUsername( val );
			}
			else {
				for ( Map.Entry<UserProperty, URI> en : PROPMAP.entrySet() ) {
					if ( en.getValue().equals( pred ) ) {
						user.setProperty( en.getKey(), val );
					}
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
