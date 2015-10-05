/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.filters;

import com.hp.hpl.jena.util.FileUtils;
import gov.va.semoss.util.MultiMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * A class that caches its input stream, so it can be read as many times as
 * needed. The source input stream is not read (consumed!) until the first call
 * to {@link #getInputStream()}
 *
 * @author ryan
 */
public class CachingServletRequest extends HttpServletRequestWrapper {

	private static final Logger log = Logger.getLogger( CachingServletRequest.class );
	private final ByteArrayOutputStream cache = new ByteArrayOutputStream();
	private MultiMap<String, String> parameters = null;

	public CachingServletRequest( HttpServletRequest request ) {
		super( request );
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader( new InputStreamReader( getInputStream() ) );
	}

	private MultiMap<String, String> parseParameters() {
		if ( null == parameters ) {
			parameters = new MultiMap<>();
			String data = asString();

			try {
				Pattern pat = Pattern.compile( "([^=]+)=(.*)" );
				for ( String nameval : data.split( "&" ) ) {
					Matcher m = pat.matcher( nameval );
					if ( m.matches() ) {
						String name = m.group( 1 );
						String val = URLDecoder.decode( m.group( 2 ), "UTF-8" );
						parameters.add( name, val);
					}
				}
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}

		return parameters;
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration( getParameterMap().keySet() );
	}

	@Override
	public String[] getParameterValues( String name ) {
		return parseParameters().getNN( name ).toArray( new String[0] );
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> map = new HashMap<>();
		for( Map.Entry<String, List<String>> en : parseParameters().entrySet() ){
			map.put( en.getKey(), en.getValue().get( 0 ) );
		}

		return map;
	}

	@Override
	public String getParameter( String name ) {
		return ( parseParameters().containsKey( name )
				? parseParameters().getNN( name ).get( 0 ) : null );
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if ( 0 == cache.size() ) {
			IOUtils.copy( super.getInputStream(), cache );
		}

		return new ServletInputStream() {
			private final ByteArrayInputStream stream
					= new ByteArrayInputStream( cache.toByteArray() );

			@Override
			public int read() throws IOException {
				return stream.read();
			}
		};
	}

	/**
	 * Gets the this request as a single string, or null if something goes wrong
	 *
	 * @return this Request's inputstream as a String, or null if error
	 */
	public String asString() {
		try {
			return FileUtils.readWholeFileAsUTF8( getInputStream() );
		}
		catch ( Exception x ) {
			log.warn( x, x );
		}
		return null;
	}
}
