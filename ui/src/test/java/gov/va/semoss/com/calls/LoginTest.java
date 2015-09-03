package gov.va.semoss.com.calls;

import gov.va.semoss.security.User;
import gov.va.semoss.web.io.SemossService;
import gov.va.semoss.web.io.SemossServiceImpl;
import gov.va.semoss.web.io.ServiceClient;
import gov.va.semoss.web.io.ServiceClientImpl;
import static org.junit.Assert.assertEquals;

public class LoginTest {

	//@Test
	public void testRestTemplateLogin() {
		SemossService svc = new SemossServiceImpl( "http://localhost:8080/semoss" );
		ServiceClient sc = new ServiceClientImpl();
				
		String username = "ryan";
		char[] password = "1234".toCharArray();

		sc.setAuthentication( svc, username, password );
		User user = sc.getUser( svc );
		assertEquals( username, user.getUsername() );
	}
}
