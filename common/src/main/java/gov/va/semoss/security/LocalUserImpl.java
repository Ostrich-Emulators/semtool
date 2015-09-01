/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.security;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 *
 * @author ryan
 */
public class LocalUserImpl extends AbstractUser {

	private static final String NAMESPACE_KEY = "USER_NAMESPACES";
	private final Preferences prefs = Preferences.userNodeForPackage( User.class );

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
		if ( null == value ) {
			prefs.remove( prop.toString() );
		}
		else {
			prefs.put( prop.toString(), value.trim() );
		}
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
	public Map<UserProperty, String> getProperties() {
		Map<UserProperty, String> props = new EnumMap<>( UserProperty.class );
		for ( UserProperty up : UserProperty.values() ) {
			String val = getProperty( up );
			if ( null != val ) {
				props.put( up, val );
			}
		}

		return props;
	}

	@Override
	public boolean isLocal() {
		return true;
	}
}
