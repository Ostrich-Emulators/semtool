package gov.va.semoss.com;

import gov.va.semoss.com.calls.APICall;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

public class WebServerAPI {

	private static final Logger log = Logger.getLogger(WebServerAPI.class);
	
	private static String HOST;
	
	private static int PORT;
	
	private static boolean initialized = false;
	
	private static Properties urls = null;
	
	private static HttpClient client = null;
	
	private static WebServerAPI instance;

	private WebServerAPI(){

	}
	
	public static WebServerAPI instance(){
		if (!initialized){
			log.error("Web Server API called, but not initialized.");
			return null;
		}
		else if (instance == null){
			instance = new WebServerAPI();
		}
		return instance;
	}
	
	public static void initialize(String host, int port){
		client = new HttpClient();
		loadProperties();
		HOST = host;
		PORT = port;
		initialized = true;
	}
	
	public boolean execute(APICall apiCall){
		String endpoint = urls.getProperty(apiCall.getName());
		return apiCall.execute(client, HOST, PORT, endpoint);
	}
	
	private static void loadProperties(){
		urls = new Properties();
		InputStream input = null;

		try {
			URL propertiesFileURL = WebServerAPI.class.getResource("REST_API.properties");
			input = propertiesFileURL.openStream();
			urls.load(input);

		} catch (IOException ex) {
			log.error("", ex);
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}
}
