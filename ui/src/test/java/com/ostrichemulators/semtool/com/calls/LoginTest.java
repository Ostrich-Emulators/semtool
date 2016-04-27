package com.ostrichemulators.semtool.com.calls;

import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.web.io.SemossService;
import com.ostrichemulators.semtool.web.io.SemossServiceImpl;
import com.ostrichemulators.semtool.web.io.ServiceClient;
import com.ostrichemulators.semtool.web.io.ServiceClientImpl;
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
