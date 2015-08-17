package gov.va.semoss.com.calls;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class LogoutCall extends APICall {

	protected LogoutCall() {
		super("logout");
	}

	@Override
	public boolean execute(HttpClient client, String host, int port,
			String endpoint) {
		GetMethod get = new GetMethod(endpoint);
		try {
			// execute the GET
			int status = client.executeMethod(get);

			// print the status and response
			log.info(status + client.getState().toString() + "\n" + get.getStatusLine()
					+ get.getResponseBodyAsString());
			return true;
		}
		catch (Exception e){
			log.error("", e);
			return false;
		}
		finally {
			get.releaseConnection();
		}
	}

}
