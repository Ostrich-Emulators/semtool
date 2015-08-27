package gov.va.semoss.web.filters;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebFilter("/*")
public class ReverseProxyFilter implements Filter {
	
	private static final Set<String> VALID_PATHS = new HashSet();
	
	private static final Logger log = Logger.getLogger( ReverseProxyFilter.class );

	private static final ProxyController proxyController = new ProxyController();
	/** Set to true if you want this filter to initiate a proxy to the DBs, false if you
	 * simply want a redirect */
	private static final boolean doProxy = true;
	
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
    	
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestUrl = request.getRequestURL().toString();
        String path = getPath(requestUrl);
        
        if (validUrl(path)) {
            //allowed, continue navigation
            chain.doFilter(req, res);
        } else {
        	log.warn("Invalid URL is a candidate for forwarding: " + path);
        	if (doProxy){
        		proxyRequest(req, res, chain);
        	}
        	else {
        		String destination = getDestination(req, res);
        		response.sendRedirect(destination);
        	}
        }
    }
    
    private String getDestination(ServletRequest req,
			ServletResponse res){
    	return null;
    }
    
    private void proxyRequest(ServletRequest req,
			ServletResponse res, FilterChain chain) {
    	HttpServletRequest request = (HttpServletRequest) req;
    	HttpServletResponse response = (HttpServletResponse) res;
    	try {
    		proxyController.proxyRequest(request, response);
		} catch (IOException | ServletException e) {
			log.error("Error handling proxy request.", e);
		}
	}

	private String getPath(String requestURL){
    	int endOfDomainIndex = requestURL.indexOf("8080/");
    	int pathStartIndex = requestURL.indexOf("/", endOfDomainIndex + 5);
    	String requestPath = requestURL.substring(pathStartIndex + 1); 
    	return requestPath;
    }

    private boolean validUrl(String path) {
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
    	if (VALID_PATHS.contains(path)){
    		return true;
    	}
    	else {
    		return false;
    	}
    }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		VALID_PATHS.add("databases");
		VALID_PATHS.add("login");
		VALID_PATHS.add("css");
		VALID_PATHS.add("images");
		VALID_PATHS.add("js");
		VALID_PATHS.add("lib");
		VALID_PATHS.add("admin");
		VALID_PATHS.add("");
	}
}
