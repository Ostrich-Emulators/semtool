package gov.va.semoss.rdf.query.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.PropertyConfigurator;
import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.repository.RepositoryConnection;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import java.util.TimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class QueryExecutorAdapterTest {

	private static InMemorySesameEngine engine;
	private static final URI ENTITYONE
			= new URIImpl( "http://www.7delta.com/entities#one" );
	private static final URI TYPEONE
			= new URIImpl( "http://www.7delta.com/types#one" );
	private static final URI TYPEB
			= new URIImpl( "http://www.7delta.com/booltype" );
	private static final URI TYPED
			= new URIImpl( "http://www.7delta.com/doubletype" );
	private static final URI TYPES
			= new URIImpl( "http://www.7delta.com/stringtype" );
	private static final URI TYPEI
			= new URIImpl( "http://www.7delta.com/inttype" );
	private static final URI TYPEA
			= new URIImpl( "http://www.7delta.com/datetype" );
	private static final Date date = new Date( 1412795456361l );

	private final QueryExecutorAdapter<String> queryer = new QueryExecutorAdapter<String>() {

		@Override
		public void handleTuple( BindingSet set, ValueFactory fac ) {
			result = set.getValue( "id" ).stringValue();
		}
	};

	static {
		Properties props = new Properties();
		props.setProperty( "log4j.rootLogger", "INFO, stdout" );
		props.setProperty( "log4j.appender.stdout", "org.apache.log4j.ConsoleAppender" );
		props.setProperty( "log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout" );
		props.setProperty( "log4j.appender.stdout.layout.ConversionPattern",
				"%d{yyyy-MMM-dd HH:mm:ss,SSS} [%-5p] %c:%L - %m%n" );

		PropertyConfigurator.configure( props );
	}

	@BeforeClass
	public static void setupClass() {
		TimeZone.setDefault( TimeZone.getTimeZone( "GMT-04:00" ) );
		engine = new InMemorySesameEngine();
		try {
			RepositoryConnection rc = engine.getRawConnection();

			GregorianCalendar gCalendar = new GregorianCalendar();
			gCalendar.setTime( date );
			XMLGregorianCalendar xmlcal = null;
			try {
				xmlcal = DatatypeFactory.newInstance().newXMLGregorianCalendar( gCalendar );
			}
			catch ( DatatypeConfigurationException ex ) {
			}

			ValueFactory vf = rc.getValueFactory();
			rc.add( new StatementImpl( ENTITYONE, RDF.TYPE, TYPEONE ) );
			rc.add( new StatementImpl( ENTITYONE, TYPEB, vf.createLiteral( true ) ) );
			rc.add( new StatementImpl( ENTITYONE, TYPED, vf.createLiteral( 1.0 ) ) );
			rc.add( new StatementImpl( ENTITYONE, TYPEI, vf.createLiteral( 1 ) ) );
			rc.add( new StatementImpl( ENTITYONE, TYPES, vf.createLiteral( "string" ) ) );
			rc.add( new StatementImpl( ENTITYONE, TYPES, vf.createLiteral( "cuerda", "es" ) ) );
			rc.add( new StatementImpl( ENTITYONE, TYPEA, vf.createLiteral( xmlcal ) ) );
		}
		catch ( Exception e ) {

		}
	}

	@AfterClass
	public static void tearDownClass() {
		engine.closeDB();
	}

	@Test
	public void testGetSetSparql() {
		final String SPARQL = "SELECT * WHERE { ?s ?p ?o }";
		queryer.setSparql( SPARQL );
		assertEquals( SPARQL, queryer.getSparql() );
	}

	@Test
	public void testGetResults() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id a <" + TYPEONE + "> }" );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindURIStringString() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id a <" + TYPEONE + "> }" );
		queryer.bind( "type", TYPEONE );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindURIStringStringString() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id a ?type }" );
		queryer.bindURI( "type", TYPEONE.getNamespace(), TYPEONE.getLocalName() );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringString() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPES );
		queryer.bind( "type", "cuerda", "es" );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringStringString() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPES );
		queryer.bind( "type", "string" );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringDouble() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPED );
		queryer.bind( "type", 1.0 );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringInt() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPEI );
		queryer.bind( "type", 1 );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test( expected = MalformedQueryException.class )
	public void testBadSparql() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type" ); // NOTE: missing }
		queryer.bind( "pred", TYPEI );
		queryer.bind( "type", 1 );
		engine.query( queryer );
		// we'll never get this far
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringDate() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPEA );
		queryer.bind( "type", date );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindStringBoolean() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPEB );
		queryer.bind( "type", true );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	@Test
	public void testBindBadBoolean() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", TYPEB );
		queryer.bind( "type", false ); // this isn't in the database
		engine.query( queryer );
		assertNull( queryer.getResults() );
	}

	@Test
	public void testBindStringURI() throws Exception {
		queryer.setSparql( "SELECT * WHERE { ?id ?pred ?type }" );
		queryer.bind( "pred", RDF.TYPE );
		queryer.bindURI( "type", TYPEONE.stringValue() );
		engine.query( queryer );
		assertEquals( ENTITYONE.stringValue(), queryer.getResults() );
	}

	//@Test
	public void testBindAndGetSparql() throws Exception {
		// NOTE: this SparQL is a bit non-sensical, but we're just checking the
		// string replacement logic
		String expected = "SELECT ?id ?pred WHERE {"
				+ " ?id ?pred ?type ."
				+ " ?id ?pred ?text ."
				+ " ?id ?pred2 ?int ."
				+ " ?id ?pred2 ?dbl ."
				+ " ?id ?pred2 ?date ."
				+ " ?id ?pred22 ?bool "
				+ " VALUES ?int {\"6\"^^<http://www.w3.org/2001/XMLSchema#int>}"
				+ " VALUES ?dbl {\"5.2\"^^<http://www.w3.org/2001/XMLSchema#double>}"
				+ " VALUES ?bool {\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>}"
				+ " VALUES ?date {\"2014-10-08T15:10:56.361-04:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>}"
				+ " VALUES ?text {\"some text\"@en}"
				+ " VALUES ?pred {<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>}"
				+ " VALUES ?type {<http://www.7delta.com/types#one>}"
				+ " VALUES ?pred2 {<http://www.7delta.com/stringtype>}"
				+ "} ORDER BY ?id";
		queryer.setSparql( "SELECT ?id ?pred WHERE { ?id ?pred ?type . ?id ?pred ?text . "
				+ "?id ?pred2 ?int . ?id ?pred2 ?dbl . ?id ?pred2 ?date . ?id ?pred22 ?bool } ORDER BY ?id" );
		queryer.bind( "pred", RDF.TYPE );
		queryer.bind( "pred2", TYPES );
		queryer.bindURI( "type", TYPEONE.stringValue() );
		queryer.bind( "text", "some text", "en" );
		queryer.bind( "int", 6 );
		queryer.bind( "dbl", 5.2 );
		queryer.bind( "date", date );
		queryer.bind( "bool", true );

		String result = queryer.bindAndGetSparql();		
		assertEquals( expected, result );
	}
}
