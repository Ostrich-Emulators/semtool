package gov.va.semoss.com.calls;

import static org.junit.Assert.*;
import gov.va.semoss.com.RestAuthenticator;
import gov.va.semoss.com.WebServerAPI;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LoginTest {

	//@Test
	public void testScreenScrapeLogin() {
		GenericLoginCall loginCall = new GenericLoginCall("ryan", "123456");
		WebServerAPI.initialize("localhost", 8080);
		if (!WebServerAPI.instance().execute(loginCall)){
			fail("Unable to log in");
		}
	}
	
	//@Test
	public void testRestTemplateLogin() {
		String username = "ryan";
		String password = "123456";
		String url = "http://localhost:8080/semoss-webserver/admin";
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = RestAuthenticator.instance().getEntity(username, password);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		Object object = response.getBody();
	}
	

}
