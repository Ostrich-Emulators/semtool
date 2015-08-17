package gov.va.semoss.com.calls;

import static org.junit.Assert.*;
import gov.va.semoss.com.WebServerAPI;

import org.junit.Test;

public class LoginTest {

	@Test
	public void test() {
		LoginCall loginCall = new LoginCall("ryan", "123456");
		WebServerAPI.initialize("localhost", 8080);
		if (!WebServerAPI.instance().execute(loginCall)){
			fail("Unable to log in");
		}
	}

}
