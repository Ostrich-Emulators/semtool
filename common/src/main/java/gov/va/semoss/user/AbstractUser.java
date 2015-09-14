/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.user;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public abstract class AbstractUser implements User {

	private String username = null;
	private final Map<String, String> props = new HashMap<>();

	public AbstractUser() {
	}

	public AbstractUser( String username ) {
		this.username = username;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getProperty( String prop ) {
		return props.get( prop );
	}

	@Override
	public void setProperty( String prop, String value ) {
		props.put( prop, value );
	}

	/**
	 * Gets information in a consistent manner for keeping track of provenance
	 * items. This should be: <code>FullName &lt;email&gt;, organization</code>
	 *
	 * @return
	 */
	@Override
	public String getAuthorInfo() {
		return getAuthorInformation( this );
	}

	public static String getAuthorInformation( User author ) {
		String userInfo = "";

		String userPrefName = author.getProperty( UserProperty.USER_FULLNAME );
		String userPrefEmail = author.getProperty( UserProperty.USER_EMAIL );
		userPrefEmail = ( !userPrefEmail.isEmpty() ? " <" + userPrefEmail + ">" : "" );
		String userPrefOrg = author.getProperty( UserProperty.USER_ORG );

		if ( !( userPrefName.isEmpty() && userPrefEmail.isEmpty() && userPrefOrg.isEmpty() ) ) {
			if ( userPrefName.isEmpty() || userPrefOrg.isEmpty() ) {
				userInfo = userPrefName + userPrefEmail + " " + userPrefOrg;
			}
			else {
				userInfo = userPrefName + userPrefEmail + ", " + userPrefOrg;
			}
			return userInfo;
		}

		return author.getUsername();
	}
}
