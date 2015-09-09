/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

import java.security.Permission;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface User {

	public static enum UserProperty {

		USER_EMAIL, USER_FULLNAME, USER_ORG
	};

	public String getUsername();
	
	public Map<String, String> getNamespaces();

	public void addNamespace( String prefix, String ns );

	public void setNamespaces( Map<String, String> nsmap );

	public void setProperty( UserProperty prop, String value );

	/**
	 * Gets this user's property, or the empty string (not null)
	 * @param prop
	 * @return the value, or the empty string
	 */
	public String getProperty( UserProperty prop );

	public void setProperties( Map<UserProperty, String> props );
	
	public boolean hasPermission( Permission p );
	
	public void resetPermissions( Collection<Permission> perms );
	
	public String getProperty(String prop);
	
	public void setProperty(String prop, String value);
	
	public Map<UserProperty, String> getProperties();
	/**
	 * Is this user authorized remotely, or from the local computer?
	 * @return 
	 */
	public boolean isLocal();
}
