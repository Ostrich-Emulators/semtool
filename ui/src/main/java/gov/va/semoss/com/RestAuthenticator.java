package gov.va.semoss.com;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.codec.binary.Base64;

public class RestAuthenticator {
	
	private static RestAuthenticator instance;
	
	private RestAuthenticator(){}
	
	public static RestAuthenticator instance(){
		if (instance == null){
			instance = new RestAuthenticator();
		}
		return instance;
	}

	public HttpEntity<String> getEntity(String username, String password){
		String plainCreds = username + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		return request;
	}
}

