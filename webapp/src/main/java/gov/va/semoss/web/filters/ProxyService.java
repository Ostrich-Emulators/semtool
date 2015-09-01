package gov.va.semoss.web.filters;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;

/**
 * The Proxy Service is designed to allow the forwarding of requests,
 * complete with query strings, headers, etc., to a given host and 
 * application context.  Adapted from code found at: https://github.com/mitre/HTTP-Proxy-Servlet
 * @author Wayne Warren
 *
 */
public class ProxyService {
	/** The logger for this class */
	private static final Logger log = Logger.getLogger(ProxyService.class);
	/** The The target host of the proxy call */
	protected HttpHost targetHost;
	/** The Http client used to make the proxy call */
	private HttpClient proxyClient;
	/** The service name, used for its cookies' keys */
	public static final String SERVICE_NAME = "SEMOSS_DB_SERVER";
	
	/** The parameter name for the target (destination) URI to proxy to. */
	protected static final String P_TARGET_URI = "targetUri";
	/** The attribute key for the target URI */
	protected static final String ATTR_TARGET_URI = ProxyService.class
			.getSimpleName() + ".targetUri";
	/** The attribute key for the target host */
	protected static final String ATTR_TARGET_HOST = ProxyService.class
			.getSimpleName() + ".targetHost";
	/** The Access Control List instance that handles predicate-related privileges */
	protected static final PredicateACL predicateACL = new PredicateACL();
	/** The Access Control List instance that handles predicate-related privileges */
	protected static final ServerACL serverACL = new ServerACL();
	
	/**
	 * Initializes the http client to be ready to make calls 
	 */
	public void initialize() {
		proxyClient = new DefaultHttpClient(new ThreadSafeClientConnManager());
	}

	/**
	 * Get the client used for proxy calls
	 * @return The initialized http client
	 */
	protected HttpClient getProxyClient() {
		return proxyClient;
	}

	/**
	 * Dispose of resources used by the http client
	 */
	public void tearDown() {
		if (proxyClient instanceof Closeable) {
			try {
				((Closeable) proxyClient).close();
			} catch (IOException e) {
				log.error(
						"While destroying servlet, shutting down HttpClient: ",
						e);
			}
		} else {
			if (proxyClient != null)
				proxyClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Proxy a given request to the desired host/context
	 * @param destinationURL The full destination URL stub
	 * @param servletRequest The servlet request to be proxied
	 * @param servletResponse The servlet response to which we can write the destination's response
	 * @throws ServletException Thrown exception in case something goes awry
	 * @throws IOException Thrown exception in case something goes awry
	 */
	public void proxyRequest(String destinationURL,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws ServletException,
			IOException {
		// Initiate the attributes
		if (servletRequest.getAttribute(ATTR_TARGET_URI) == null) {
			servletRequest.setAttribute(ATTR_TARGET_URI, destinationURL);
		}
		if (servletRequest.getAttribute(ATTR_TARGET_HOST) == null) {
			servletRequest.setAttribute(ATTR_TARGET_HOST, targetHost);
		}
		// Begin proxying the request
		String method = servletRequest.getMethod();
		String proxyRequestUri = rewriteUrlFromRequest(destinationURL,
				servletRequest);
		HttpRequest proxyRequest;
		// Signal, via the headers, that there's a message body
		if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
				|| servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
			HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(
					method, proxyRequestUri);
			// Add the input entity (streamed)
			// note: we don't bother ensuring we close the servletInputStream
			// since the container handles it
			eProxyRequest.setEntity(new InputStreamEntity(servletRequest
					.getInputStream(), servletRequest.getContentLength()));
			proxyRequest = eProxyRequest;
		} else
			proxyRequest = new BasicHttpRequest(method, proxyRequestUri);

		copyRequestHeaders(servletRequest, proxyRequest);

		setXForwardedForHeader(servletRequest, proxyRequest);

		HttpResponse proxyResponse = null;
		try {

			log.info("proxy " + method + " uri: "
					+ servletRequest.getRequestURI() + " -- "
					+ proxyRequest.getRequestLine().getUri());

			proxyResponse = proxyClient.execute(getTargetHost(servletRequest),
					proxyRequest);

			// Process the response
			int statusCode = proxyResponse.getStatusLine().getStatusCode();

			// Copy all response headers
			copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

			if (doResponseRedirectOrNotModifiedLogic(destinationURL,
					servletRequest, servletResponse, proxyResponse, statusCode)) {
				// the response is already "committed" now without any body to
				// send
				return;
			}

			// Set the response code. 
			servletResponse.setStatus(statusCode);

			// Send the content to the client
			copyResponseEntity(proxyResponse, servletResponse);

		} catch (Exception e) {
			// abort request, according to best practice with HttpClient
			if (proxyRequest instanceof AbortableHttpRequest) {
				AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
				abortableHttpRequest.abort();
			}
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			if (e instanceof ServletException)
				throw (ServletException) e;
			// noinspection ConstantConditions
			if (e instanceof IOException)
				throw (IOException) e;
			throw new RuntimeException(e);

		} finally {
			// make sure the entire entity was consumed, so the connection is
			// released
			if (proxyResponse != null)
				consumeQuietly(proxyResponse.getEntity());
			// Note: Don't need to close servlet outputStream:
			// http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
		}
	}

	protected boolean doResponseRedirectOrNotModifiedLogic(
			String destinationURL, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, HttpResponse proxyResponse,
			int statusCode) throws ServletException, IOException {
		// Check if the proxy response is a redirect
		if (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES 
				&& statusCode < HttpServletResponse.SC_NOT_MODIFIED ) {
			Header locationHeader = proxyResponse
					.getLastHeader(HttpHeaders.LOCATION);
			if (locationHeader == null) {
				throw new ServletException("Received status code: "
						+ statusCode + " but no " + HttpHeaders.LOCATION
						+ " header was found in the response");
			}
			// Modify the redirect to go to this proxy servlet rather that the
			// proxied host
			String locStr = rewriteUrlFromResponse(destinationURL,
					servletRequest, locationHeader.getValue());

			servletResponse.sendRedirect(locStr);
			return true;
		}
		// In the case of a 304, nothing needs to be done, since no modified 
		// resources must be transferred
		if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
			servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
			servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		return false;
	}

	protected void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	protected void consumeQuietly(HttpEntity entity) {
		try {
			EntityUtils.consume(entity);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	protected static final HeaderGroup hopByHopHeaders;
	static {
		hopByHopHeaders = new HeaderGroup();
		String[] headers = new String[] { "Connection", "Keep-Alive",
				"Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailers",
				"Transfer-Encoding", "Upgrade" };
		for (String header : headers) {
			hopByHopHeaders.addHeader(new BasicHeader(header, null));
		}
	}

	/** Copy request headers from the servlet client to the proxy request. */
	protected void copyRequestHeaders(HttpServletRequest servletRequest,
			HttpRequest proxyRequest) {
		// Get an Enumeration of all of the header names sent by the client
		Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();
		while (enumerationOfHeaderNames.hasMoreElements()) {
			String headerName = (String) enumerationOfHeaderNames.nextElement();
			// Instead the content-length is effectively set via
			// InputStreamEntity
			if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
				continue;
			if (hopByHopHeaders.containsHeader(headerName))
				continue;

			Enumeration headers = servletRequest.getHeaders(headerName);
			while (headers.hasMoreElements()) {// sometimes more than one value
				String headerValue = (String) headers.nextElement();
				// In case the proxy host is running multiple virtual servers,
				// rewrite the Host header to ensure that we get content from
				// the correct virtual server
				if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
					HttpHost host = getTargetHost(servletRequest);
					headerValue = host.getHostName();
					if (host.getPort() != -1)
						headerValue += ":" + host.getPort();
				} else if (headerName
						.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
					headerValue = getRealCookie(headerValue);
				}
				proxyRequest.addHeader(headerName, headerValue);
			}
		}
	}

	private void setXForwardedForHeader(HttpServletRequest servletRequest,
			HttpRequest proxyRequest) {
		String headerName = "X-Forwarded-For";
		String newHeader = servletRequest.getRemoteAddr();
		String existingHeader = servletRequest.getHeader(headerName);
		if (existingHeader != null) {
			newHeader = existingHeader + ", " + newHeader;
		}
		proxyRequest.setHeader(headerName, newHeader);

	}

	/** Copy proxied response headers back to the servlet client. */
	protected void copyResponseHeaders(HttpResponse proxyResponse,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		for (Header header : proxyResponse.getAllHeaders()) {
			if (hopByHopHeaders.containsHeader(header.getName()))
				continue;
			if (header.getName().equalsIgnoreCase(
					org.apache.http.cookie.SM.SET_COOKIE)
					|| header.getName().equalsIgnoreCase(
							org.apache.http.cookie.SM.SET_COOKIE2)) {
				copyProxyCookie(servletRequest, servletResponse, header);
			} else {
				servletResponse.addHeader(header.getName(), header.getValue());
			}
		}
	}

	/**
	 * Copy cookie from the proxy to the servlet client. Replaces cookie path to
	 * local path and renames cookie to avoid collisions.
	 */
	protected void copyProxyCookie(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, Header header) {
		List<HttpCookie> cookies = HttpCookie.parse(header.getValue());
		String path = servletRequest.getContextPath(); // path starts with / or
														// is empty string
		path += servletRequest.getServletPath(); // servlet path starts with /
													// or is empty string

		for (HttpCookie cookie : cookies) {
			// set cookie name prefixed w/ a proxy value so it won't collide w/
			// other cookies
			String proxyCookieName = getCookieNamePrefix() + cookie.getName();
			Cookie servletCookie = new Cookie(proxyCookieName,
					cookie.getValue());
			servletCookie.setComment(cookie.getComment());
			servletCookie.setMaxAge((int) cookie.getMaxAge());
			servletCookie.setPath(path); // set to the path of the proxy servlet
			// don't set cookie domain
			servletCookie.setSecure(cookie.getSecure());
			servletCookie.setVersion(cookie.getVersion());
			servletResponse.addCookie(servletCookie);
		}
	}

	/**
	 * Take any client cookies that were originally from the proxy and prepare
	 * them to send to the proxy. This relies on cookie headers being set
	 * correctly according to RFC 6265 Sec 5.4. This also blocks any local
	 * cookies from being sent to the proxy.
	 */
	protected String getRealCookie(String cookieValue) {
		StringBuilder escapedCookie = new StringBuilder();
		String cookies[] = cookieValue.split("; ");
		for (String cookie : cookies) {
			String cookieSplit[] = cookie.split("=");
			if (cookieSplit.length == 2) {
				String cookieName = cookieSplit[0];
				if (cookieName.startsWith(getCookieNamePrefix())) {
					cookieName = cookieName.substring(getCookieNamePrefix()
							.length());
					if (escapedCookie.length() > 0) {
						escapedCookie.append("; ");
					}
					escapedCookie.append(cookieName).append("=")
							.append(cookieSplit[1]);
				}
			}

			cookieValue = escapedCookie.toString();
		}
		return cookieValue;
	}

	/** The string prefixing rewritten cookies. */
	protected String getCookieNamePrefix() {
		return "!Proxy!" + SERVICE_NAME;
	}

	/**
	 * Copy response body data (the entity) from the proxy to the servlet
	 * client.
	 */
	protected void copyResponseEntity(HttpResponse proxyResponse,
			HttpServletResponse servletResponse) throws IOException {
		HttpEntity entity = proxyResponse.getEntity();
		if (entity != null) {
			OutputStream servletOutputStream = servletResponse
					.getOutputStream();
			entity.writeTo(servletOutputStream);
		}
	}

	/**
	 * Reads the request URI from {@code servletRequest} and rewrites it,
	 * considering targetUri. It's used to make the new request.
	 */
	protected String rewriteUrlFromRequest(String destinationURL,
			HttpServletRequest servletRequest) {
		StringBuilder uri = new StringBuilder(500);
		uri.append(destinationURL);
		// Handle the path given to the servlet
		if (servletRequest.getPathInfo() != null) {// ex: /my/path.html
			uri.append(encodeUriQuery(servletRequest.getPathInfo()));
		}
		// Handle the query string & fragment
		String queryString = servletRequest.getQueryString();// ex:(following
																// '?'):
																// name=value&foo=bar#fragment
		String fragment = null;
		// split off fragment from queryString, updating queryString if found
		if (queryString != null) {
			int fragIdx = queryString.indexOf('#');
			if (fragIdx >= 0) {
				fragment = queryString.substring(fragIdx + 1);
				queryString = queryString.substring(0, fragIdx);
			}
		}

		queryString = rewriteQueryStringFromRequest(servletRequest, queryString);
		if (queryString != null && queryString.length() > 0) {
			uri.append('?');
			uri.append(encodeUriQuery(queryString));
		}
		boolean sendFragement = true;
		if (sendFragement) {
			uri.append('#');
			uri.append(encodeUriQuery(fragment));
		}
		return uri.toString();
	}

	protected String rewriteQueryStringFromRequest(
			HttpServletRequest servletRequest, String queryString) {
		return queryString;
	}

	/**
	 * For a redirect response from the target server, this translates
	 * {@code theUrl} to redirect to and translates it to one the original client
	 * can use.
	 */
	protected String rewriteUrlFromResponse(String destinationURL,
			HttpServletRequest servletRequest, String theUrl) {
		// TODO document example paths
		final String targetUri = getTargetUri(servletRequest);
		if (theUrl.startsWith(targetUri)) {
			String curUrl = servletRequest.getRequestURL().toString();// no
																		// query
			String pathInfo = servletRequest.getPathInfo();
			if (pathInfo != null) {
				assert curUrl.endsWith(pathInfo);
				curUrl = curUrl.substring(0,
						curUrl.length() - pathInfo.length());// take pathInfo
																// off
			}
			theUrl = curUrl + theUrl.substring(targetUri.length());
		}
		return theUrl;
	}

	/**
	 * Encodes characters in the query or fragment part of the URI.
	 *
	 * <p>
	 * Unfortunately, an incoming URI sometimes has characters disallowed by the
	 * spec. HttpClient insists that the outgoing proxied request has a valid
	 * URI because it uses Java's {@link URI}. To be more forgiving, we must
	 * escape the problematic characters. See the URI class for the spec.
	 *
	 * @param in
	 *            example: name=value&foo=bar#fragment
	 */
	protected static CharSequence encodeUriQuery(CharSequence in) {
		// Note that I can't simply use URI.java to encode because it will
		// escape pre-existing escaped things.
		StringBuilder outBuf = null;
		Formatter formatter = null;
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			boolean escape = true;
			if (c < 128) {
				if (asciiQueryChars.get((int) c)) {
					escape = false;
				}
			} else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {// not-ascii
				escape = false;
			}
			if (!escape) {
				if (outBuf != null)
					outBuf.append(c);
			} else {
				// escape
				if (outBuf == null) {
					outBuf = new StringBuilder(in.length() + 5 * 3);
					outBuf.append(in, 0, i);
					formatter = new Formatter(outBuf);
				}
				// leading %, 0 padded, width 2, capital hex
				formatter.format("%%%02X", (int) c);// TODO
			}
		}
		return outBuf != null ? outBuf : in;
	}

	protected static final BitSet asciiQueryChars;
	static {
		char[] c_unreserved = "_-!.~'()*".toCharArray();// plus alphanum
		char[] c_punct = ",;:$&+=".toCharArray();
		char[] c_reserved = "?/[]@".toCharArray();// plus punct

		asciiQueryChars = new BitSet(128);
		for (char c = 'a'; c <= 'z'; c++)
			asciiQueryChars.set((int) c);
		for (char c = 'A'; c <= 'Z'; c++)
			asciiQueryChars.set((int) c);
		for (char c = '0'; c <= '9'; c++)
			asciiQueryChars.set((int) c);
		for (char c : c_unreserved)
			asciiQueryChars.set((int) c);
		for (char c : c_punct)
			asciiQueryChars.set((int) c);
		for (char c : c_reserved)
			asciiQueryChars.set((int) c);

		asciiQueryChars.set((int) '%');// leave existing percent escapes in
										// place
	}

	protected String getTargetUri(HttpServletRequest servletRequest) {
		return (String) servletRequest.getAttribute(ATTR_TARGET_URI);
	}

	private HttpHost getTargetHost(HttpServletRequest servletRequest) {
		return (HttpHost) servletRequest.getAttribute(ATTR_TARGET_HOST);
	}
}