package com.ostrichemulators.semtool.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class LegacySanitizerTest {

	private final LegacySanitizer santizer = new LegacySanitizer();

	private String[] uris;

	private final String badURI = "server.domain.com/%%%%%";

	@Before
	public void setUp() throws Exception {
		uris = new String[4];
		uris[0] = "http://va.semoss.gov/ontology/terms/term1";
		uris[1] = "http://va.semoss.gov/ontology/terms/term1 2";
		uris[2] = "http://va.semoss.gov/ontology/terms/term1**2";
		uris[3] = "http://va.semoss.gov/ontology/terms/term%201";
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSanitize() throws URISyntaxException {
		for ( String s : uris ) {
			String sanitized = santizer.sanitize( s );
			URI uri = new URI( sanitized );
			assertFalse( uri.toASCIIString().contains(  ":" ) );
			assertFalse( uri.toASCIIString().contains(  "/" ) );
		}
	}

	@Test( expected = URISyntaxException.class )
	public void testBadSanitize() throws URISyntaxException {
		String sanitized = santizer.sanitize( badURI );
		URI uri = new URI( sanitized );

		// we'll never get here (hopefully)
		assertNotNull( uri );
	}
}
