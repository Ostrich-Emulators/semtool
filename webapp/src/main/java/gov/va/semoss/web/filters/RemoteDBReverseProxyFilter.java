package gov.va.semoss.web.filters;

import gov.va.semoss.web.datastore.DbInfoMapper;
import gov.va.semoss.web.io.DbInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This RP Service intercepts all commands, searching for calls to remote
 * databases and if found, proceeds to functions as a Reverse Proxy (RP) for
 * those databases.
 *
 * @author Wayne Warren
 *
 */
@WebFilter( "/*" )
public class RemoteDBReverseProxyFilter implements Filter {

	/**
	 * The Object's logger
	 */
	private static final Logger log = Logger.getLogger( RemoteDBReverseProxyFilter.class );
	/**
	 * The Service that will use the httpClient to actually make the proxied call
	 */
	private static final ProxyService proxyService = new ProxyService();
	/**
	 * Set to true if you want this filter to initiate a proxy to the DBs, false
	 * if you simply want a redirect
	 */
	private static final boolean doProxy = true;

	/**
	 * The static flag signifying the 'server' service or URL
	 */
	public static final String SERVER_URL = "server";
	/**
	 * The static flag signifying the 'data' service or URL
	 */
	public static final String DATA_URL = "data";
	/**
	 * The static flag signifying the 'insight' service or URL
	 */
	public static final String INSIGHT_URL = "insights";
	/**
	 * The Access Control List security implementation for Predicate reading and
	 * writing
	 */
	private final PredicateACL predicateACL = new PredicateACL();
	/**
	 * The Access Control List security implementation for Server access
	 */
	private final ServerACL serverACL = new ServerACL();

	private ServletContext servletContext = null;

	private ApplicationContext applicationContext = null;

	private DbInfoMapper datastore;

	public RemoteDBReverseProxyFilter() {
		proxyService.initialize();
	}

	@Override
	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String path = request.getServletPath();

		if ( shouldProxyRequest( path ) ) {
			log.warn( "Current URL request is a candidate for RP-Handling: " + path );
			DbInfo dbi = getRawDbInfoFromRequest( request );
			String destinationURL = getProxyDestination( dbi, request );
			if ( doProxy ) {
				proxyRequest( destinationURL, request, response, chain );
			}
			else {
				response.sendRedirect( destinationURL );
			}
		}
		else {
			// If not caught, process in the usual manner
			chain.doFilter( req, res );
		}
	}

	/**
	 * Proxy a given request to a remote database
	 *
	 * @param destinationURL The destination URL that is to handle the proxy
	 * @param req The servlet request
	 * @param res The servlet response
	 * @param chain The filter chain of this application context
	 */
	private void proxyRequest( String destinationURL, HttpServletRequest request,
			HttpServletResponse response, FilterChain chain ) {
		try {
			URI uri = new URI( destinationURL );
			HttpHost host = new HttpHost( uri.getHost(), uri.getPort(), uri.getScheme() );
			request.setAttribute( ProxyService.ATTR_TARGET_HOST, host );

			CsrfToken token = CsrfToken.class.cast( request.getAttribute( "_csrf" ) );
			proxyService.proxyRequest( destinationURL, request, response );
			if( null != token ){
				response.setHeader( "X-CSRF-HEADER", token.getHeaderName() );
				response.setHeader( "X-CSRF-PARAM", token.getParameterName() );
				response.setHeader( "X-CSRF-TOKEN", token.getToken() );
			}
		}
		catch ( URISyntaxException | ServletException | IOException e ) {
			log.error( "Error handling proxy request.", e );
		}
	}

	private DbInfo getRawDbInfoFromRequest( HttpServletRequest req ) {
		final String path = req.getServletPath();

		for ( DbInfo dbi : datastore.getAll() ) {
			if ( path.startsWith( "/databases/" + dbi.getName() + "/repositories/" ) ) {
				dbi.setServerUrl( getServerUrl( dbi, req ) );
				return dbi;
			}
		}

		throw new IllegalArgumentException( "unknown db info path: " + path );
	}

	/**
	 * Decides whether or not the RP is to handle this call, since ALL calls will
	 * pass through this RP filter
	 *
	 * @param path The query path of the call
	 * @return True if this Filter is to step in and handle the call, false
	 * otherwise
	 */
	private boolean shouldProxyRequest( String path ) {
		for ( DbInfo dbi : datastore.getAll() ) {
			String datapath = "/databases/" + dbi.getName() + "/repositories/" + DATA_URL;
			log.debug( "checking " + path + " against: " + datapath );
			if ( path.startsWith( datapath ) ) {
				log.debug( "match!" );
				return true;
			}

			String insightpath = "/databases/" + dbi.getName() + "/repositories/" + INSIGHT_URL;

			log.debug( "checking " + path + " against: " + insightpath );
			if ( path.startsWith( insightpath ) ) {
				log.debug( "match!" );
				return true;
			}
		}

		return false;
	}

	/**
	 * Release resources used by the http client
	 */
	@Override
	public void destroy() {
		proxyService.tearDown();
	}

	/**
	 * Initialize this instance by populating the proxied paths and indexing the
	 * remote DBs by name.
	 */
	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		servletContext = filterConfig.getServletContext();
		applicationContext = WebApplicationContextUtils.getWebApplicationContext( servletContext );
		this.datastore = applicationContext.getBean( DbInfoMapper.class );
		log.debug( "got the dbmapper bean" );
	}

	/**
	 * Converts a Database Info record's url to a form compatible with the Reverse
	 * Proxy functions of this filter. For example, a DbInfo with a server URL of
	 * http://someServer.org/sesame-openRDF named remoteSesame1 will become
	 * http://semossServer.org/semoss/remoteDatabase/remoteSesame1/server
	 *
	 * @param info The Database Info object seeking conversion
	 * @return A DbInfo object that has appropriate URLs for this RP
	 */
	public static DbInfo convertToRPStyle( DbInfo info, HttpServletRequest req ) {
		String serverurl = getServerUrl( info, req );
		info.setServerUrl( serverurl );
		info.setDataUrl( serverurl + "/repositories/" + DATA_URL );
		info.setInsightsUrl( serverurl + "/repositories/" + INSIGHT_URL );
		return info;
	}

	private static String getServerUrl( DbInfo raw, HttpServletRequest req ) {
		StringBuilder urlBuilder = new StringBuilder( req.getScheme() ).append( "://" ).
				append( req.getServerName() );
		if ( 80 != req.getServerPort() ) {
			urlBuilder.append( ":" ).append( req.getServerPort() );
		}
		urlBuilder.append( req.getContextPath() ).append( "/databases/" ).
				append( raw.getName() );
		return urlBuilder.toString();
	}

	/**
	 * Converts the incoming request to it's location behind the proxy
	 *
	 * @param raw
	 * @param req
	 * @return
	 */
	private static String getProxyDestination( DbInfo raw, HttpServletRequest req ) {
		Pattern pat = Pattern.compile( "^/databases/" + raw.getName()
				+ "/repositories/(data|insights)" );
		String reqpath = req.getServletPath();		
		
		Matcher m = pat.matcher( reqpath );
		m.find(); // we better know that this works!
		String serviceNeeded = m.group( 1 );
		
		m.reset();
		String proxyloc = m.replaceAll( serviceNeeded.equals( DATA_URL )
					? raw.getDataUrl() : raw.getInsightsUrl() );
		return proxyloc;
	}

	/**
	 * A convenience class providing functionality and attributes showing what a
	 * call is asking for, and where it should be directed.
	 *
	 * @author Wayne Warren
	 *
	 */
	private class ProxyCall {

		/**
		 * The type of service needed: Server, Data, or Insight
		 */
		public final String serviceNeeded;
		public final DbInfo info;

		/**
		 *
		 * @param requestPath
		 */
		public ProxyCall( DbInfo dbi, String requestPath ) {
			info = dbi;
			Pattern pat = Pattern.compile( "^/databases/" + dbi.getName()
					+ "/repositories/(data|insight)" );
			Matcher m = pat.matcher( requestPath );
			m.find(); // we better know that this works!
			serviceNeeded = m.group( 1 );
		}

		public String getDestinationURL() {
			return ( serviceNeeded.equals( DATA_URL )
					? info.getDataUrl() : info.getInsightsUrl() );
		}
	}
}
