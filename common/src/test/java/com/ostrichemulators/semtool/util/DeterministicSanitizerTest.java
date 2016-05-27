package com.ostrichemulators.semtool.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DeterministicSanitizerTest {

	private final DeterministicSanitizer santizer = new DeterministicSanitizer();

	@Test
	public void testSanitize() throws URISyntaxException {
		Map<String, String> map = new HashMap<>();
		map.put( "http://va.semoss.gov/ontology/terms/term1", "Ec5d065097a5859ca4abdbc11123c8b43" );
		map.put( "http://va.semoss.gov/ontology/terms/term1 2", "N682a7974602128c1639319c37d2c6696" );
		map.put( "http://va.semoss.gov/ontology/terms/term1**2", "Q554f72d2c49364d4a6ac24507aa1a100" );
		map.put( "http://va.semoss.gov/ontology/terms/term%201", "A18bc4f5c18d450894a4c8ede4756e6b9" );

		for ( String s : map.keySet() ) {
			String sanitized = santizer.sanitize( s );
			URI uri = new URI( sanitized );
			//System.out.println( "map.put( \"" + s + "\", \"" + uri.toASCIIString() + "\" );" );
			assertEquals( map.get( s ), uri.toASCIIString() );
		}
	}
}
