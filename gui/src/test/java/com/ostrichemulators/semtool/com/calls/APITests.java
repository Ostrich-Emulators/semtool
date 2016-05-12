package com.ostrichemulators.semtool.com.calls;

import static org.junit.Assert.*;

import com.ostrichemulators.semtool.web.io.DbInfo;
import com.ostrichemulators.semtool.web.io.SemossServiceImpl;
import com.ostrichemulators.semtool.web.io.ServiceClient;
import com.ostrichemulators.semtool.web.io.ServiceClientImpl;

public class APITests {

	//@Test
	public void test() {
		ServiceClient client = new ServiceClientImpl();
		String username = "john";
		String pass = "123456";
		SemossServiceImpl ssi = new SemossServiceImpl( "http://localhost:8080/semoss" );
		client.setAuthentication( ssi, username, pass.toCharArray() );
		DbInfo[] dbs = client.getDbs( ssi );
		if ( dbs.length == 0 ) {
			fail( "No databases returned." );
		}
	}

}
