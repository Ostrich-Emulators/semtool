package gov.va.semoss.web.filters;

import gov.va.semoss.web.datastore.DbInfoMapper;
import gov.va.semoss.web.datastore.UserMapper;
import gov.va.semoss.web.io.DbInfo;

import gov.va.semoss.web.security.DbAccess;
import gov.va.semoss.web.security.SemossUser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
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
import org.openrdf.model.impl.URIImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

	private DbInfoMapper dbmapper;
	private UserMapper usermapper;

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
			CachingServletRequest caching = new CachingServletRequest( request );
			DbInfo dbi = getRawDbInfoFromRequest( caching );
			String destinationURL = getProxyDestination( dbi, caching );
			if ( doProxy ) {
				proxyRequest( dbi, destinationURL, caching, response, chain );
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
	 * @param dbi the database locator
	 * @param destinationURL The destination URL that is to handle the proxy
	 * @param req The servlet request
	 * @param res The servlet response
	 * @param chain The filter chain of this application context
	 */
	private void proxyRequest( DbInfo dbi, String destinationURL,
			CachingServletRequest request, HttpServletResponse response, FilterChain chain )
			throws IOException {
		boolean isinsights = isInsightUrl( dbi, destinationURL );

		if ( shouldDenyRequest( dbi, request, isinsights ) ) {
			createFailedRequest( request, response );
			return;
		}

		try {
			URI uri = new URI( destinationURL );
			HttpHost host = new HttpHost( uri.getHost(), uri.getPort(), uri.getScheme() );
			request.setAttribute( ProxyService.ATTR_TARGET_HOST, host );

			CsrfToken token = CsrfToken.class.cast( request.getAttribute( "_csrf" ) );
			proxyService.proxyRequest( destinationURL, request, response );
			if ( null != token ) {
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

		for ( DbInfo dbi : dbmapper.getAll() ) {
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
		for ( DbInfo dbi : dbmapper.getAll() ) {
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
		this.dbmapper = applicationContext.getBean( DbInfoMapper.class );
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

	private static boolean isInsightUrl( DbInfo raw, String url ) {
		return url.contains( raw.getInsightsUrl() );
	}

	private DbAccess getAccess( DbInfo dbi, SemossUser user, boolean forInsightsDb ) {
		Map<org.openrdf.model.URI, DbAccess> accesses = usermapper.getAccesses( user );
		org.openrdf.model.URI insuri = new URIImpl( dbi.getInsightsUrl() );
		org.openrdf.model.URI dburi = new URIImpl( dbi.getDataUrl() );

		if ( forInsightsDb ) {
			if ( accesses.containsKey( insuri ) ) {
				return accesses.get( insuri );
			}

			// if we don't have specific access to the insights db, we must have
			// read access on it if we can read the database itself
			return ( accesses.containsKey( dburi ) ? DbAccess.READ : DbAccess.NONE );
		}

		return accesses.getOrDefault( dburi, DbAccess.NONE );
	}

	private void createFailedRequest( HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
	}

	private boolean shouldDenyRequest( DbInfo dbi, CachingServletRequest request,
			boolean isinsights ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		SemossUser smu = SemossUser.class.cast( auth.getPrincipal() );
		DbAccess access = getAccess( dbi, smu, isinsights );

		if ( DbAccess.WRITE == access ) {
			return false;
		}

		if ( DbAccess.NONE == access ) {
			return true;
		}

		// we know we have read access at this point...all GETs are ok
		if ( HttpMethod.GET == HttpMethod.valueOf( request.getMethod() ) ) {
			return false;
		}

		// if we get here, we need to figure out what the user is asking
		// before we can decide if they should get access. The Sesame API
		// uses POSTs sometimes instead of GETs.
		boolean fail = true;

		String query = request.getParameter( "query" );
		if ( null == query ) {
			String reqq = request.asString();
			log.debug( reqq );
			for ( String bad : new String[]{ "<transaction>", "<add>", "<clear>",
				"<delete>", "<update>" } ) {
				if ( reqq.contains( bad ) ) {
					fail = true;
					break;
				}
			}
		}
		else {
			query = query.toUpperCase();
			fail = !( query.contains( "SELECT" ) || query.contains( "DESCRIBE" )
					|| query.contains( "CONSTRUCT" ) );
		}
		return fail;
	}
}
