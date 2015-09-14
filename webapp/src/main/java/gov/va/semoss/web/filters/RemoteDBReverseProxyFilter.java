package gov.va.semoss.web.filters;


import gov.va.semoss.web.datastore.DbInfoMapper;
import gov.va.semoss.web.io.DbInfo;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This RP Service intercepts all commands, searching for calls to remote databases
 * and if found, proceeds to functions as a Reverse Proxy (RP) for those databases.
 * @author Wayne Warren
 *
 */
@WebFilter("/*") 
public class RemoteDBReverseProxyFilter implements Filter {
	/** The primary path nodes handled (proxied) by this filter */
	private static final Set<String> PROXIED_PATHS = new HashSet<String>();
	/** The Object's logger */
	private static final Logger log = Logger.getLogger( RemoteDBReverseProxyFilter.class );
	/** The Service that will use the httpClient to actually make the proxied call */
	private static final ProxyService proxyService = new ProxyService();
	/** Set to true if you want this filter to initiate a proxy to the DBs, false if you
	 * simply want a redirect */
	private static final boolean doProxy = true;
	/** A lookup table which contains a remote database names as keys, and the DbInfo as the value */
	private static final HashMap<String, DbInfo> remoteDatabases = new HashMap<String, DbInfo>();
	/** The address of this web server (i.e. 'http://semoss.va.gov/') */
	private static final String SERVER_ADDRESS = "localhost";
	/** The application context of this servlet instance (i.e. 'semoss') */
	private static final String SERVLET_CONTEXT = "semoss";
	/** The port of this servlet instance (i.e. '8080'), set to -1 if you'd like to leave the port 
	 * out of the address */
	private static final int SERVER_PORT = 8080;
	/** The store of remote DBInfo, managed by Spring */
	
	/** The static flag signifying the 'server' service or URL */
	public static final String SERVER_URL = "server";
	/** The static flag signifying the 'data' service or URL */
	public static final String DATA_URL = "data";
	/** The static flag signifying the 'insight' service or URL */
	public static final String INSIGHT_URL = "insights";
	/** The Access Control List security implementation for Predicate 
	 * reading and writing */
	private final PredicateACL predicateACL = new PredicateACL();
	/** The Access Control List security implementation for Server 
	 * access */
	private final ServerACL serverACL = new ServerACL();
	
	private ServletContext servletContext = null;
	
	private ApplicationContext applicationContext = null;

	private DbInfoMapper datastore;
	
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
    	
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestUrl = request.getRequestURL().toString();
        String path = getPath(requestUrl);
        if (handleCall(path)) {
        	log.warn("Current URL request is a candidate for RP-Handling: " + path);
        	ProxyCall call = new ProxyCall(path);
        	String destinationURL = call.getDestinationURL();
        	if (doProxy){
        		proxyRequest(destinationURL, req, res, chain);
        	}
        	else {
        		response.sendRedirect(destinationURL);
        	}
        } else {
        	// If not caught, process in the usual manner
            chain.doFilter(req, res);
        }
    }
    
    /**
     * Proxy a given request to a remote database
     * @param destinationURL The destination URL that is to handle the proxy
     * @param req The servlet request
     * @param res The servlet response
     * @param chain The filter chain of this application context
     */
    private void proxyRequest(String destinationURL, ServletRequest req,
			ServletResponse res, FilterChain chain) {
    	HttpServletRequest request = (HttpServletRequest) req;
    	HttpServletResponse response = (HttpServletResponse) res;
    	try {
    		proxyService.proxyRequest(destinationURL, request, response);
		} catch (IOException | ServletException e) {
			log.error("Error handling proxy request.", e);
		}
	}

    /**
     * Get the path of a url call for subsequent processing
     * @param requestURL The full request URL, including the protocol prefix
     * @return The query path, without the application context
     */
	private String getPath(String requestURL){
    	int endOfProtocolIndex = requestURL.indexOf("//");
    	int endOfDomainIndex = requestURL.indexOf("/", endOfProtocolIndex + 2);
    	int pathStartIndex = requestURL.indexOf("/", endOfDomainIndex + 5);
    	String requestPath = requestURL.substring(pathStartIndex + 1); 
    	return requestPath;
    }

	/**
	 * Decide whether or not the RP is to handle this call, since ALL
	 * calls will pass through this RP filter
	 * @param path The query path of the call
	 * @return True if this Filter is to step in and handle the call, false otherwise
	 */
    private boolean handleCall(String path) {
    	// Get the first path node in the path
    	if (path.contains("/")){
    		path = path.substring(0,path.indexOf("/"));
    	}
    	if (path.contains("?")){
    		path = path.substring(0,path.indexOf("?"));
    	}
    	if (path.contains("#")){
    		path = path.substring(0,path.indexOf("#"));
    	}
    	// Validate the first path node against those that are valid
    	if (PROXIED_PATHS.contains(path)){
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    /**
     * Release resources used by the http client
     */
	@Override
	public void destroy() {
		proxyService.tearDown();
	}

	/**
	 * Initialize this instance by populating the proxied paths
	 * and indexing the remote DBs by name.
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		PROXIED_PATHS.add("remoteDatabase");
		servletContext = filterConfig.getServletContext();
		applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		this.datastore = (DbInfoMapper) applicationContext.getBean("dbinfomapper");
		System.out.println("Got the bean");
	}
	
	/**
	 * Converts a Database Info record's url to a form compatible with
	 * the Reverse Proxy functions of this filter.  For example, a DbInfo
	 * with a server URL of http://someServer.org/sesame-openRDF named 
	 * remoteSesame1 will become http://semossServer.org/semoss/remoteDatabase/remoteSesame1/server 
	 * @param info The Database Info object seeking conversion
	 * @return A DbInfo object that has appropriate URLs for this RP
	 */
	public static DbInfo convertToRPStyle(DbInfo info){
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(SERVER_ADDRESS);
		urlBuilder.append(":" + SERVER_PORT);
		urlBuilder.append("/" + SERVLET_CONTEXT + "/");
		String proxyServerURL = urlBuilder.toString();
		try {
			URI serverURI = new URI(info.getServerUrl());
			String serverPath = serverURI.getRawPath();
			String serverQuery = serverURI.getRawQuery();
			info.setServerUrl(proxyServerURL + serverPath + "?" + serverQuery);
			URI dataURI = new URI(info.getDataUrl());
			String dataPath = dataURI.getRawPath();
			String dataQuery = dataURI.getRawQuery();
			info.setDataUrl(proxyServerURL + dataPath + "?" + dataQuery);
			URI insightsURI = new URI(info.getInsightsUrl());
			String insightsPath = insightsURI.getRawPath();
			String insightsQuery = insightsURI.getRawQuery();
			info.setDataUrl(proxyServerURL + insightsPath + "?" + insightsQuery);
			return info;
		}
		catch(Exception e){
			log.error("Error converting URL to RP-Style: " + info.getName(), e);
			return null;
		}
	}
	
	/**
	 * A convenience class providing functionality and attributes showing
	 * what a call is asking for, and where it should be directed.
	 * @author Wayne Warren
	 *
	 */
	private class ProxyCall {
		/** The name of the server to which the call is destined */
		public final String serverName;
		/** The type of service needed:  Server, Data, or Insight */
		public final String serviceNeeded;
		
		/**
		 * 
		 * @param requestPath
		 */
		public ProxyCall(String requestPath){
			// Get the first "Node" in the call, which should be the server name
			int firstDivider = requestPath.indexOf("/"); 
			serverName = requestPath.substring(0, firstDivider);
			// Get the second "Node" in the call, which should be the service needed
			int secondDivider = requestPath.indexOf("/", firstDivider + 1);	
			serviceNeeded= requestPath.substring(firstDivider + 1, secondDivider);
		}
		
		public String getDestinationURL(){
	    	DbInfo info = remoteDatabases.get(serverName);
	    	if (info != null && serviceNeeded != null){
	    		if (serviceNeeded.equals(SERVER_URL)){
	    			return info.getServerUrl();
	    		}
	    		else if (serviceNeeded.equals(DATA_URL)){
	    			return info.getDataUrl();
	    		}
	    		else if (serviceNeeded.equals(INSIGHT_URL)){
	    			return info.getInsightsUrl();
	    		}
	    		else {
	    			log.error("Unable to establish proper proxy handling for request, server name = " + 
	    					serverName + ", service needed = " + serviceNeeded);
	    			return null;
	    		}
	    	}
	    	else {
	    		log.error("Unable to execute proper proxy lookup for request, server name = " + 
    					serverName + ", service needed = " + serviceNeeded);
	    		return null;
	    	}
		}
	}
}
