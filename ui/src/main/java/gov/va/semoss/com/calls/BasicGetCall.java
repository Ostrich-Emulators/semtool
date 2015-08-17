package gov.va.semoss.com.calls;

import org.apache.commons.httpclient.HttpClient;
import org.springframework.web.client.RestTemplate;

public class BasicGetCall extends APICall {

	protected BasicGetCall(String name) {
		super(name);
	}

	@Override
	public boolean execute(HttpClient client, String host, int port,
			String endpoint) {
		// TODO Auto-generated method stub
		return false;
	}



}
