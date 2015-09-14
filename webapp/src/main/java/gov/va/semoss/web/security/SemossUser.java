/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.security;

import gov.va.semoss.user.AbstractUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.util.Assert;

/**
 *
 * @author ryan
 */
public class SemossUser extends LdapUserDetailsImpl implements gov.va.semoss.user.User {

	private Collection<GrantedAuthority> authorities = AuthorityUtils.NO_AUTHORITIES;

	private final Map<UserProperty, String> properties = new HashMap<>();
	private final Map<String, String> namespaces = new HashMap<>();

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
	public Map<UserProperty, String> getProperties() {
		return new EnumMap<>( properties );
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

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getProperty( String prop ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setProperty( String prop, String value ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String getAuthorInfo() {
		return AbstractUser.getAuthorInformation( this );
	}

	public static class SemossEssence extends LdapUserDetailsImpl.Essence {

		private List<GrantedAuthority> mutableAuthorities;

		public SemossEssence() {
		}

		public SemossEssence( DirContextOperations ctx ) {
			super( ctx );
		}

		public SemossEssence( LdapUserDetails copyMe ) {
			super( copyMe );
		}

		@Override
		protected SemossUser createTarget() {
			return new SemossUser();
		}

		@Override
		public void setAuthorities( Collection<? extends GrantedAuthority> authorities ) {
			mutableAuthorities = new ArrayList<>();
			mutableAuthorities.addAll( authorities );
		}

		@Override
		public SemossUser createUserDetails() {
			Assert.notNull( instance,
					"Essence can only be used to create a single instance" );
			Assert.notNull( instance.getUsername(), "username must not be null" );
			Assert.notNull( instance.getDn(), "Distinguished name must not be null" );

			SemossUser newInstance = SemossUser.class.cast( instance );
			newInstance.authorities = Collections.unmodifiableList( mutableAuthorities );

			instance = null;

			return newInstance;
		}
	}
}
