package gov.va.semoss.com.calls;

import gov.va.semoss.web.io.DbInfo;

public class GetDBInfo extends SpringRESTCall {

	protected GetDBInfo() {
		super("GetDBInfo");
	}

	@Override
	public boolean execute(String host, int port, String endpoint) {
		String serviceURL = host + ":" + port + "/" + CONTEXT + "/" + endpoint;
		this.returnedData = rest.getForObject(serviceURL, DbInfo[].class );
		return true;
	}
	
}
