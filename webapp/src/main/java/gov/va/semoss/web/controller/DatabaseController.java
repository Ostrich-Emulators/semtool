package gov.va.semoss.web.controller;

import gov.va.semoss.web.datastore.DbInfoMapper;

import javax.servlet.http.HttpServletResponse;

import gov.va.semoss.web.io.DbInfo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller serves pages secured by Spring Security
 *
 * @author Wayne Warren
 *
 */
@Controller
@RequestMapping( "/databases" )
public class DatabaseController extends SemossControllerBase {

	private static final Logger log = Logger.getLogger( DatabaseController.class );

	@Autowired
	private DbInfoMapper datastore;

	@RequestMapping( params = "names" )
	@ResponseBody
	public String[] getAllDatabaseIDs() {
		log.debug( "Getting all database IDs." );
		Collection<DbInfo> testDbs = datastore.getAll();

		int i = 0;
		String[] testDbIDs = new String[testDbs.size()];
		for ( DbInfo dbi : testDbs ) {
			testDbIDs[i++] = dbi.getName();
		}

		return testDbIDs;
	}

	@RequestMapping( "/{id}" )
	@ResponseBody
	public DbInfo getOneDatabaseWithID( @PathVariable( "id" ) String id,
			HttpServletResponse response ) {
		log.debug( "Getting database with ID " + id + "." );
		DbInfo test = datastore.getOne( id );
		if ( null == test ) {
			throw new UnauthorizedException();
		}
		return test;
	}

	@RequestMapping( value = "/list", method = RequestMethod.GET )
	@ResponseBody
	public DbInfo[] getAllDatabases( HttpServletRequest req ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		log.debug( "Getting all databases (user: " + auth.getName() + ")" );
		DbInfo[] testDbs = datastore.getAll().toArray( new DbInfo[0] );

		String reqpath = req.getRequestURL().toString() + "/";

		for ( DbInfo dbi : testDbs ) {
			try {
				String serverpath = reqpath + URLEncoder.encode( dbi.getName(), "UTF-8" );
				dbi.setServerUrl( serverpath );
				dbi.setDataUrl( serverpath + "/data" );
				dbi.setInsightsUrl( serverpath + "/insights" );
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}

		return testDbs;
	}

	@RequestMapping( "/{id}/{type}" )
	public void getRepo( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
		forward( id, type, "", request, response );
	}

	@RequestMapping( "/{id}/{type}/statements" )
	public void getStatements( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
		forward( id, type, "/statements", request, response );
	}

	@RequestMapping( "/{id}/{type}/contexts" )
	public void getContexts( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		forward( id, type, "/contexts", request, response );
	}

	@RequestMapping( "/{id}/{type}/size" )
	public void getSize( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		forward( id, type, "/size", request, response );
	}

	@RequestMapping( "/{id}/{type}/rdf-graphs" )
	public void getGraphs( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		forward( id, type, "/rdf-graphs", request, response );
	}

	@RequestMapping( "/{id}/{type}/rdf-graphs/service" )
	public void getGraphsService( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		forward( id, type, "/rdf-graphs/service", request, response );
	}

	@RequestMapping( "/{id}/{type}/rdf-graphs/{name}" )
	public void getGraph( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, @PathVariable String name,
			HttpServletRequest request, HttpServletResponse response ) throws IOException {
		forward( id, type, "/rdf-graphs/" + name, request, response );
	}

	@RequestMapping( "/{id}/{type}/namespaces" )
	public void getNamespaces( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, HttpServletRequest request,
			HttpServletResponse response ) throws IOException {
		forward( id, type, "/namespaces", request, response );
	}

	@RequestMapping( "/{id}/{type}/namespaces/{prefix}" )
	public void getNamespace( @PathVariable( "id" ) String id,
			@PathVariable( "type" ) String type, @PathVariable String prefix,
			HttpServletRequest request, HttpServletResponse response ) throws IOException {
		forward( id, type, "/namespaces/" + prefix, request, response );
	}

	private void forward( String id, String type, String extra,
			HttpServletRequest request, HttpServletResponse response ) throws IOException {
		DbInfo dbi = datastore.getOne( id );

		String realurl = ( "data".equalsIgnoreCase( type )
				? dbi.getDataUrl() : dbi.getInsightsUrl() );
		String requestUrl = realurl + extra;

		try ( CloseableHttpClient httpclient = HttpClients.createDefault() ) {
			RequestBuilder builder = null;

			switch ( request.getMethod() ) {
				case "POST":
					builder = RequestBuilder.post( requestUrl );
					break;
				case "DELETE":
					builder = RequestBuilder.delete( requestUrl );
					break;
				case "PUT":
					builder = RequestBuilder.put( requestUrl );
					break;
				default: // "GET"
					builder = RequestBuilder.get( requestUrl );
			}

			Map<String, String[]> params = request.getParameterMap();
			for ( Map.Entry<String, String[]> en : params.entrySet() ) {
				for ( String val : en.getValue() ) {
					builder.addParameter( en.getKey(), val );
				}
			}

			Enumeration<String> headers = request.getHeaderNames();
			while ( headers.hasMoreElements() ) {
				String header = headers.nextElement();
				if ( !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase( header ) ) {
					Enumeration<String> valen = request.getHeaders( header );
					while ( valen.hasMoreElements() ) {
						builder.addHeader( header, valen.nextElement() );
					}
				}
			}

			// Create a custom response handler
			ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {

				@Override
				public Void handleResponse( final HttpResponse ws )
						throws ClientProtocolException, IOException {
					int status = ws.getStatusLine().getStatusCode();
					response.setStatus( status );
					HttpEntity entity = ws.getEntity();
					if ( null == entity ) {
						return null;
					}

					response.addHeader( HttpHeaders.CONTENT_TYPE,
							ws.getFirstHeader( HttpHeaders.CONTENT_TYPE ).getValue() );
					response.addHeader( HttpHeaders.VARY, "Accept" );
					if ( entity.isChunked() ) {
						response.addHeader( HttpHeaders.TRANSFER_ENCODING, "chunked" );
					}

					response.addHeader( HttpHeaders.SERVER,
							ws.getFirstHeader( HttpHeaders.SERVER ).getValue() );
					response.addHeader( HttpHeaders.DATE,
							ws.getFirstHeader( HttpHeaders.DATE ).getValue() );

					for ( Header header : ws.getAllHeaders() ) {
						log.debug( "header: " + header.getName() + "--->" + header.getValue() );
						response.setHeader( header.getName(), header.getValue() );
					}
//
//					CsrfToken csrf = CsrfToken.class.cast( request.getAttribute( "_csrf" ) );
//					if ( null != csrf ) {
//						response.setHeader( "X-CSRF-HEADER", csrf.getHeaderName() );
//						// Spring Security will allow the token to be included in this parameter name
//						response.setHeader( "X-CSRF-PARAM", csrf.getParameterName() );
//						// this is the value of the token to be included as either a header or an HTTP parameter
//						response.setHeader( "X-CSRF-TOKEN", csrf.getToken() );
//					}

					if ( entity.isStreaming() ) {
						IOUtils.copy( ws.getEntity().getContent(), response.getOutputStream() );
					}
					else {
						response.getWriter().write( EntityUtils.toString( entity ) );
					}
					response.flushBuffer();

					return null;
				}
			};

			HttpUriRequest forwarded = builder.build();
			httpclient.execute( forwarded, responseHandler );
		}
	}
}
