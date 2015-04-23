/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;

/**
 *
 * @author ryan
 */
public class ListQueryAdapterTest {

	private static final InMemorySesameEngine engine;
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
	private static final Date date = new Date();

	private final ListQueryAdapter<IdObj> queryer
			= new ListQueryAdapter<IdObj>( "SELECT ?id ?obj WHERE { ?id ?pred ?obj }" ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					String id = set.getValue( "id" ).stringValue();
					String obj = set.getValue( "obj" ).stringValue();
					add( new IdObj( id, obj ) );
				}
			};

	static {
		engine = new InMemorySesameEngine();
		RepositoryConnection rc = engine.getRawConnection();
		try {
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

	@Test
	public void testClear() throws Exception {
		queryer.bind( "pred", TYPES );
		engine.query( queryer );
		queryer.clear();
		assertTrue( queryer.getResults().isEmpty() );
	}

	@Test
	public void testQuery1() throws Exception {
		queryer.bind( "pred", TYPES );
		List<IdObj> list = engine.query( queryer );
		assertTrue( 2 == list.size() );
	}

	@Test
	public void testQuery2() throws Exception {
		queryer.bind( "pred", TYPEONE ); // we use TYPEONE as an object, not a predicate
		List<IdObj> list = engine.query( queryer );
		assertTrue( list.isEmpty() );
	}

	@Test
	public void testQuery3() throws Exception {
		queryer.bind( "pred", "we shouldn't find this" );
		List<IdObj> list = engine.query( queryer );
		assertTrue( list.isEmpty() );
	}

	@Test
	public void testQuery4() throws Exception {
		ListQueryAdapter<IdObj> q = new ListQueryAdapter<IdObj>() {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				// nothing to do
			}
		};
		q.setSparql( queryer.getSparql() );
		q.bind( "pred", "we shouldn't find this" );
		List<IdObj> list = engine.query( q );
		assertTrue( list.isEmpty() );
	}

	class IdObj {

		String id;
		String obj;

		public IdObj( String id, String obj ) {
			this.id = id;
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 59 * hash + Objects.hashCode( this.id );
			hash = 59 * hash + Objects.hashCode( this.obj );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final IdObj other = (IdObj) obj;
			if ( !Objects.equals( this.id, other.id ) ) {
				return false;
			}
			return Objects.equals( this.obj, other.obj );
		}
	}
}
