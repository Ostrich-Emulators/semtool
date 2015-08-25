package gov.va.semoss.com.calls;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.io.ServiceClient;
import gov.va.semoss.web.io.ServiceClientImpl;

public class APITests {

	//@Test
	public void test() {
		ServiceClient client = new ServiceClientImpl();
		String username = "john";
		String pass = "123456";
		String url = "http://localhost:8080/semoss/databases";
		client.setAuthentication(url, username, pass.toCharArray());
		DbInfo[] dbs = client.getDbs(url);
		if (dbs.length == 0){
			fail("No databases returned.");
		}
	}

}
