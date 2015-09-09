/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

import gov.va.semoss.security.permissions.SemossPermission;
import java.security.Permission;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 *
 * @author ryan
 */
public class UserImpl implements User {

	private static User user;
	private static final String NAMESPACE_KEY = "USER_NAMESPACES";
	private final Preferences prefs = Preferences.userNodeForPackage( this.getClass() );
	private final Set<Permission> permissions = new HashSet<>();

	public static User getUser() {
		if ( null == user ) {
			user = new UserImpl();
		}
		return user;
	}

	private UserImpl() {
		permissions.add( SemossPermission.ADMIN );
	}

	@Override
	public Map<String, String> getNamespaces() {
		Map<String, String> namespaces = new HashMap<>();
		String ns = prefs.get( NAMESPACE_KEY, "" );
		for ( String s : ns.split( ";" ) ) {
			int idx = s.indexOf( ":" );
			if ( idx > 0 ) {
				namespaces.put( s.substring( 0, idx ), s.substring( idx + 1 ) );
			}
		}
		
		return namespaces;
	}

	@Override
	public void addNamespace( String prefix, String ns ) {
		Map<String, String> namespaces = getNamespaces();
		namespaces.put( prefix, ns );
		setNamespaces( namespaces );
	}

	@Override
	public void setNamespaces( Map<String, String> namespaces ) {
		StringBuilder sb = new StringBuilder();
		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			if ( sb.length() > 0 ) {
				sb.append( ";" );
			}

			sb.append( en.getKey() ).append( ":" ).append( en.getValue() );
		}

		prefs.put( NAMESPACE_KEY, sb.toString() );
	}


	@Override
	public void setProperty( UserProperty prop, String value ) {
		prefs.put( prop.toString(), value.trim() );
	}

	@Override
	public String getProperty( UserProperty prop ) {
		return prefs.get( prop.toString(), "" );
	}

	@Override
	public void setProperties( Map<UserProperty, String> props ) {
		for ( Map.Entry<UserProperty, String> en : props.entrySet() ) {
			prefs.put( en.getKey().toString(), en.getValue() );
		}
	}

	@Override
	public boolean hasPermission( Permission theirs ) {
		for ( Permission mine : permissions ) {
			if ( mine.implies( theirs ) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetPermissions( Collection<Permission> perms ) {
		permissions.clear();
		permissions.addAll( perms );
	}
	
	@Override
	public void setProperty( String prop, String value ) {
		prefs.put( prop, value.trim() );
		
	}

	@Override
	public String getProperty( String prop ) {
		return prefs.get( prop, null );
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<UserProperty, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLocal() {
		// TODO Auto-generated method stub
		return false;
	}
}
