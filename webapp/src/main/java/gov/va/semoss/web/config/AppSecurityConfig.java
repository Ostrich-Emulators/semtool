package gov.va.semoss.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This configures the SEMOSS Web Security settings using Spring Security
 * @author Wayne Warren
 *
 */
@Configuration
@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	  auth.inMemoryAuthentication().withUser("ryan").password("123456").roles(getRoleName(AuthProfile.ADMIN_ROLE));
	  auth.inMemoryAuthentication().withUser("john").password("234567").roles(getRoleName(AuthProfile.DATA_CONTRIBUTOR_ROLE));
	  auth.inMemoryAuthentication().withUser("thomas").password("345678").roles(getRoleName(AuthProfile.INSIGHT_CONTIBUTOR_ROLE));
	  auth.inMemoryAuthentication().withUser("harold").password("456789").roles(getRoleName(AuthProfile.USER_ROLE));
	  // TODO - Implement the real user attribute manager
	}
 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	  String admin = getRoleName(AuthProfile.ADMIN_ROLE);
	  String dataContributor = getRoleName(AuthProfile.DATA_CONTRIBUTOR_ROLE);
	  String insightContributor = getRoleName(AuthProfile.INSIGHT_CONTIBUTOR_ROLE);
	  String user = getRoleName(AuthProfile.USER_ROLE);
	  http.authorizeRequests()
		.antMatchers("/admin/**").access("hasRole('" + admin + "')")
		.antMatchers("/data/**").access("hasRole('" + dataContributor + "')")
		.antMatchers("/insights/**").access("hasRole('" + insightContributor + "')")
		.antMatchers("/user/**").access("hasRole('" + user + "')")
		.and().formLogin();
	}
	
	/**
	 * Translates the role integer into a displayable name
	 * @param role The int role
	 * @return The role name, null if it is outside the defined roles
	 */
	public String getRoleName(int role){
		switch (role){
		case AuthProfile.ADMIN_ROLE: return "Administrator";
		case AuthProfile.DATA_CONTRIBUTOR_ROLE: return "Data Contributor";
		case AuthProfile.INSIGHT_CONTIBUTOR_ROLE: return "Insight Contributor";
		case AuthProfile.USER_ROLE: return "User";
		}
		return null;
	}
	
	public class AuthProfile {
		/** The Admin role signifier */
		public static final int ADMIN_ROLE = 0;
		/** The Data Contributor role signifier */
		public static final int DATA_CONTRIBUTOR_ROLE = 1;
		/** The Insight Contributor role signifier */
		public static final int INSIGHT_CONTIBUTOR_ROLE = 2;
		/** The User role signifier */
		public static final int USER_ROLE = 3;
		/** The user's username */
		private String username = null;
		/** The user's password, salted, then hashed */
		private String hashedSaltedPassword = null;
		/** The user's role, referenced to the static int signifiers here*/
		private int role = -1;
		
		/**
		 * Default constructor
		 * @param username The user name
		 * @param hashedSaltedPassword The password, salted, then hashed
		 * @param role The user's role
		 */
		public AuthProfile(String username, String hashedSaltedPassword, int role){
			this.username = username;
			this.hashedSaltedPassword = hashedSaltedPassword;
			this.role = role;
		}
		
		/**
		 * Salts a String, such as a password
		 * @param content The String to be salted
		 * @return A salted String
		 */
		public String salt(String content){
			// TODO Finish this method
			return null;
		}

		/**
		 * Get the username of this profile
		 * @return The username
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * Get the salted, then hashed, password
		 * @return The salted hashed password
		 */
		public String getHashedSaltedPassword() {
			return hashedSaltedPassword;
		}

		/**
		 * Get the user's role
		 * @return The user's role
		 */
		public int getRole() {
			return role;
		}
	}
	

}
