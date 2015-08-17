/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.security;

import gov.va.semoss.security.permissions.SemossPermission;
import java.security.Permission;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
	private final Map<UserProperty, String> propmap = new EnumMap<>( UserProperty.class );
	private final Map<String, String> namespaces = new LinkedHashMap<>();
	private final Preferences prefs = Preferences.userNodeForPackage( User.class );
	private final Set<Permission> permissions = new HashSet<>();

	public static User getUser() {
		if ( null == user ) {
			user = new UserImpl();
		}
		return user;
	}

	private UserImpl() {
		String ns = prefs.get( NAMESPACE_KEY, "" );
		for ( String s : ns.split( ";" ) ) {
			int idx = s.indexOf( ":" );
			if ( idx > 0 ) {
				namespaces.put( s.substring( 0, idx ), s.substring( idx + 1 ) );
			}
		}

		permissions.add( SemossPermission.ADMIN );
	}

	@Override
	public Map<String, String> getNamespaces() {
		return new HashMap<>( namespaces );
	}

	@Override
	public void addNamespace( String prefix, String ns ) {
		namespaces.put( prefix, ns );
	}

	@Override
	public void setNamespaces( Map<String, String> nsmap ) {
		namespaces.clear();
		namespaces.putAll( nsmap );

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
		propmap.put( prop, value.trim() );
		prefs.put( prop.toString(), value.trim() );
	}

	@Override
	public String getProperty( UserProperty prop ) {
		return ( propmap.containsKey( prop ) ? propmap.get( prop ) : "" );
	}

	@Override
	public void setProperties( Map<UserProperty, String> props ) {
		propmap.clear();
		propmap.putAll( props );

		for ( Map.Entry<UserProperty, String> en : propmap.entrySet() ) {
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
}
