package gov.va.semoss.web.io;

import gov.va.semoss.security.BasicAuthRequestFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractServiceClient implements ServiceClient {

	private static final Logger log = Logger.getLogger( AbstractServiceClient.class );
	private static final Pattern DECOMPOSE = Pattern.compile( "^([^:]+)://([^:/]+)([:]([0-9]+))?/" );

	@Autowired
	protected RestTemplate rest;
	@Autowired
	protected BasicAuthRequestFactory authorizer;

	public void setRestTemplate( RestTemplate rt ){
		rest = rt;
		authorizer = BasicAuthRequestFactory.class.cast( rest.getRequestFactory() );
	}
			
	@Override
	public final void setAuthentication( SemossService svc, String username, char[] pass ) {
		Matcher m = DECOMPOSE.matcher( svc.root() );
		if ( m.find() ) {
			String host = m.group( 2 );
			String portstr = m.group( 4 );
			String scheme = m.group( 1 );
			int port = ( null == portstr ? -1 : Integer.parseInt( portstr ) );

			HttpHost hh = new HttpHost( host, port, scheme );
			authorizer.cache( hh, username, pass );
		}
		else {
			log.error( "could not parse url: " + svc.root() );
		}
	}
}
