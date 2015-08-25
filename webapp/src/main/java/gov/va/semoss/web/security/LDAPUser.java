package gov.va.semoss.web.security;


import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class LDAPUser {

	private final Set<Permission> permissions = new HashSet<>();
	
	private String username = null;
	
	private final HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();

	private static final Logger log = Logger.getLogger( LDAPUser.class );
	
	private final ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
	
	public LDAPUser() {
	}

	public LDAPUser( String username ) {
		this.username = username;
	}

	public LDAPUser( String username, Collection<Permission> pp ) {
		this.username = username;
		permissions.addAll( pp );
	}

	public String getUsername() {
		return username;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public boolean hasPermission( Permission theirs ) {
		for ( Permission mine : permissions ) {
			if ( mine.implies( theirs ) ) {
				return true;
			}
		}
		return false;
	}

	public void resetPermissions( Collection<Permission> perms ) {
		permissions.clear();
		permissions.addAll( perms );
	}
	
	public Map<String, String> getNamespaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addNamespace(String prefix, String ns) {
		// TODO Auto-generated method stub
		
	}

	public void setNamespaces(Map<String, String> nsmap) {
		// TODO Auto-generated method stub
		
	}


	public Attribute getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}


	public void setAttribute(String attributeName, Attribute attribute) {
		attributes.put(attributeName, attribute);
	}
	
	public void setAttributes(Attributes attributes) {
		NamingEnumeration<String> ne = attributes.getIDs();
		try {
	        while (ne.hasMore()){
	        	String name = ne.next();
	        	Attribute attribute = attributes.get(name);
	        	setAttribute(name, attribute);
	        }
		}
		catch (Exception e){
			log.error("Error encountered while setting LDAP attributes for user.", e);
		}
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	public void grantAuthority(String roleName){
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
		authorities.add(authority);
	}
	
	

}
