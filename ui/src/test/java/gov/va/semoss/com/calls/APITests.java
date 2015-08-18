package gov.va.semoss.com.calls;

import static org.junit.Assert.*;
import gov.va.semoss.web.io.DbInfo;
import gov.va.semoss.web.io.ServiceClient;

import org.junit.Test;

public class APITests {

	@Test
	public void test() {
		ServiceClient client = new ServiceClient();
		String endpoint = "/semoss/allDatabases";
		String username = "john";
		String pass = "123456";
		String url = ServiceClient.PROTOCOL + "://" + ServiceClient.HOST + ":" +
				ServiceClient.PORT + "/" + ServiceClient.APPLICATION_CONTEXT + endpoint;
		client.setAuthentication(url, username, pass.toCharArray());
		DbInfo[] dbs = client.getDbs(url);
		if (dbs.length == 0){
			fail("No databases returned.");
		}
	}

}
