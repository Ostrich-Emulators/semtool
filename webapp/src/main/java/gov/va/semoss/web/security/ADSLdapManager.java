package gov.va.semoss.web.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The ADS LDAP Manager is responsible for providing several 
 * access and authentication methods compatible with an Apache 
 * DS Server instance, or other compatible LDAP server.
 * @author Wayne Warren
 *
 */

public class ADSLdapManager {
	
	private static final String LDAP_SERVER_URL = "ldap://localhost";
	
	private static final int LDAP_SERVER_PORT = 10389;
	
	private static final String LDAP_SYSTEM_USERNAME = "admin";
	
	private static final String LDAP_SYSTEM_PASSWORD = "secret";
	
	private static LdapContext SERVER_CTX;
	
	private static final String LDAP_SEARCH_BASE = "dc=ad,dc=my-domain,dc=com";
	
	private static final Logger log = Logger.getLogger( ADSLdapManager.class );
	
	private static ADSLdapManager instance;
	
	private static final SecureRandom random = new SecureRandom();
	
	@Autowired
	@Qualifier("authenticationManager")
	protected AuthenticationManager authenticationManager;
	
	
	public static ADSLdapManager instance(){
		if (instance == null){
			instance = new ADSLdapManager();
		}
		return instance;
	}
	
	
    /**
     * @param args the command line arguments
     */
    private ADSLdapManager() {
        try {
        	Hashtable<String, String> environment = getEnvironment(LDAP_SYSTEM_USERNAME, LDAP_SYSTEM_PASSWORD);
        	SERVER_CTX = new InitialLdapContext(environment, null);
		}
		catch (AuthenticationException ae){
			log.error("Error in LDAP authentication.", ae);
		}
		catch (NamingException ne){
			log.error("Error in LDAP naming framework.", ne);
		}
        catch (Exception e){
        	log.error("Error in instantiating the ADS LDAP Manager.", e);
        }
    }

    
    public LDAPUser getUser(String username, String password, String[] attributeIDs){
    	LDAPUser user=null;
    	try {
        	Hashtable<String, String> environment = getEnvironment(username, password);
    		InitialLdapContext userContext = new InitialLdapContext(environment, null);
    		SearchControls constraints = new SearchControls();
    		NamingEnumeration<SearchResult> answer = userContext.search(LDAP_SEARCH_BASE, "sAMAccountName="
                     + username, constraints);
    		if (answer.hasMore()) {
                Attributes attrs = answer.next().getAttributes();
                user = new LDAPUser(username);
                user.setAttributes(attrs);
                if (answer.hasMore()){
                	log.warn("More than one user found with name '" + username + "'");
                }
                return user;
            } else {
            	log.warn("No user found with name '" + username + "'" );
                return null;
            } 
		}
		catch (AuthenticationException ae){
			log.error("Error in LDAP authentication.", ae);
			
		}
    	catch (CommunicationException ce){
    		log.error("Error in connecting to LDAP server.", ce);

    	}
		catch (NamingException ne){
			log.error("Error in LDAP naming framework.", ne);

		}
    	// Purely for testing purposes until we have the LDAP up and running
    	String saltedPassword = saltPassword("123456");
		String hashedSaltedPassword = hashPassword(saltedPassword);
    	if (username.equals("ryan") && password.equals(hashedSaltedPassword)){
    		user = new LDAPUser(username);
        	user.grantAuthority("ROLE_ADMIN");
    	}
    	else if (username.equals("john") && password.equals(hashedSaltedPassword)){
    		user = new LDAPUser(username);
        	user.grantAuthority("ROLE_DATA_ADMIN");
    	}
    	else if (username.equals("wayne") && password.equals(hashedSaltedPassword)){
    		user = new LDAPUser(username);
        	user.grantAuthority("ROLE_DATA_ADMIN");
    	}
    	return user;
    }
    
    private Hashtable<String, String> getEnvironment(String username, String password){
    	Hashtable<String, String> environment = new Hashtable<String, String>();
		
		environment.put(Context.INITIAL_CONTEXT_FACTORY, 
				"com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, LDAP_SERVER_URL + ":" + LDAP_SERVER_PORT);
		
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, username); 
		environment.put(Context.SECURITY_CREDENTIALS, password);
		return environment;
    }
    
    public Collection<LDAPUser> getUsers(String username,  String[] attributeIDs) {
    	ArrayList<LDAPUser> users = new ArrayList<LDAPUser>();
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attributeIDs);
            NamingEnumeration<SearchResult> answer = SERVER_CTX.search(LDAP_SEARCH_BASE, "sAMAccountName="
                    + username, constraints);
            if (answer.hasMore()) {
            	while (answer.hasMore()){
            		Attributes attrs = answer.next().getAttributes();
                    LDAPUser user = new LDAPUser(username);
                    user.setAttributes(attrs);
                    users.add(user);
            	}
                return users;
            } else {
            	log.warn("No user found (by proxy) with name '" + username + "'" );
                return null;
            } 
        } catch (Exception ex) {
        	log.error("Errors retrieving user with name '" + username + "'", ex);
        	return null;
        }
    }
    
    public LDAPUser getUserByProxy(String username,  String[] attributeIDs) {
    	LDAPUser user = null;
    	try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attributeIDs);
            NamingEnumeration<SearchResult> answer = SERVER_CTX.search(LDAP_SEARCH_BASE, "sAMAccountName="
                    + username, constraints);
            if (answer.hasMore()) {
            	Attributes attrs = answer.next().getAttributes();
                user = new LDAPUser(username);
                user.setAttributes(attrs);
                if (answer.hasMore()){
                	log.warn("More than one user found (by proxy) with name '" + username + "'");
                }
                return user;
            } else {
            	log.warn("No user found (by proxy) with name '" + username + "'" );
                return null;
            } 
        } catch (Exception ex) {
        	log.error("Errors retrieving user with name '" + username + "'", ex);
        	return null;
        }
    }
    
	public String hashPassword(String password){
		MessageDigest digest;
		byte[] input;
		try {			
			digest = MessageDigest.getInstance("sha-256");
			digest.reset();
			input = digest.digest(password.getBytes("UTF-8"));
			byte[] output = digest.digest(input);
			return new String(output);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String saltPassword(String password){
		String salt = "1239100393939";
		return salt + password;
	}
}