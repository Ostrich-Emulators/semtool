/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.user;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public final class RemoteUserImpl extends AbstractUser {

	private final Map<UserProperty, String> properties = new HashMap<>();
	private final Map<String, String> namespaces = new HashMap<>();

	public RemoteUserImpl( String name ) {
		super( name );
	}

	public RemoteUserImpl() {
	}

	public RemoteUserImpl( User user ) {
		super( user.getUsername() );
		setProperties( user.getProperties() );
		setNamespaces( user.getNamespaces() );
	}

	@Override
	public Map<UserProperty, String> getProperties() {
		return new EnumMap<>( properties );
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
	public void setNamespaces( Map<String, String> ns ) {
		namespaces.clear();
		namespaces.putAll( ns );
	}

	@Override
	public void setProperty( UserProperty prop, String value ) {
		if ( null == value ) {
			properties.remove( prop );
		}
		else {
			properties.put( prop, value.trim() );
		}
	}

	@Override
	public String getProperty( UserProperty prop ) {
		return properties.getOrDefault( prop, "" );
	}

	@Override
	public void setProperties( Map<UserProperty, String> props ) {
		properties.clear();
		properties.putAll( props );
	}

	@Override
	public boolean isLocal() {
		return false;
	}
}
