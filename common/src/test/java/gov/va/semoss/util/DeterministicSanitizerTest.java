package gov.va.semoss.util;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.xerces.util.URI.MalformedURIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeterministicSanitizerTest {

	private final DeterministicSanitizer santizer = new DeterministicSanitizer();
	
	private String[] uris;
	
	private String badURI = "server.domain.com/%%%%%";
	
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
		URI uri = null;
		for (String s : uris){
			String sanitized = santizer.sanitize(s);
			uri = new URI(sanitized);
		}
	}
	


}
