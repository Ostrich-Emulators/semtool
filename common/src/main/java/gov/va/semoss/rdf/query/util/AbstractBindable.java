/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import gov.va.semoss.rdf.engine.api.Bindable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Operation;

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 * @param <T> the type of the result
 */
public abstract class AbstractBindable implements Bindable {

	private static final Logger log = Logger.getLogger( AbstractBindable.class );
	private final Map<String, Double> dmap = new HashMap<>();
	private final Map<String, Integer> imap = new HashMap<>();
	private final Map<String, Date> amap = new HashMap<>();
	private final Map<String, StringPair> smap = new HashMap<>();
	private final Map<String, Boolean> bmap = new HashMap<>();
	private final Map<String, Value> vmap = new HashMap<>();
	private final Map<String, String> namespaces = new HashMap<>();
	private String sparql;
	private boolean infer = false;

	public AbstractBindable() {
	}

	public AbstractBindable( String sparq ) {
		sparql = sparq;
	}

	@Override
	public void setSparql( String sparq ) {
		sparql = sparq;
	}

	/**
	 * Gets a reference to this Executor's namespace map.
	 *
	 * @return The reference (not a copy) to the namespace map
	 */
	@Override
	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Sets custom namespaces for use with the query. These namespaces take
	 * precedence over system- and user- defined namespaces, but not over
	 * namespaces explicitly set in the query itself.
	 *
	 * @param ns
	 */
	@Override
	public void setNamespaces( Map<String, String> ns ) {
		namespaces.clear();
		addNamespaces( ns );
	}

	@Override
	public void addNamespaces( Map<String, String> ns ) {
		namespaces.putAll( ns );
	}

	@Override
	public void addNamespace( String prefix, String namespace ) {
		namespaces.put( prefix, namespace );
	}

	@Override
	public void removeNamespace( String prefix ) {
		namespaces.remove( prefix );
	}

	@Override
	public String bindAndGetSparql() {
		ValueFactory fac = new ValueFactoryImpl();

		StringBuilder binds = new StringBuilder();
		for ( Map.Entry<String, Integer> en : imap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {" ).
					append( fac.createLiteral( en.getValue() ).toString() ).append( "}" );
		}
		for ( Map.Entry<String, Double> en : dmap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {" ).
					append( fac.createLiteral( en.getValue() ) ).append( "}" );
		}
		for ( Map.Entry<String, Boolean> en : bmap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {" ).
					append( fac.createLiteral( en.getValue() ).toString() ).append( "}" );
		}
		for ( Map.Entry<String, Date> en : amap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {" ).
					append( fac.createLiteral( en.getValue() ).toString() ).append( "}" );
		}
		
		for ( Map.Entry<String, Value> en : vmap.entrySet() ) {
			Value v = en.getValue();
			binds.append( " VALUES ?" ).append( en.getKey() );
			if ( v instanceof Literal ) {
				binds.append( " {" ).append( en.getValue() ).append( "}" );
			}
			else {
				binds.append( " {<" ).append( en.getValue() ).append( ">}" );
			}
		}

		for ( Map.Entry<String, StringPair> en : smap.entrySet() ) {
			String val = en.getValue().val;
			String lang = en.getValue().lang;

			if ( null == lang ) {
				binds.append( " VALUES ?" ).append( en.getKey() ).append( " {\"" ).
						append( fac.createLiteral( val ) ).append( "\"}" );
			}
			else {
				binds.append( " VALUES ?" ).append( en.getKey() ).append( " {" ).
						append( fac.createLiteral( val, lang ) ).append( "}" );
			}
		}

		String spq = getSparql();
		// I don't really want to write a Sparql parser, so just replace the last }
		// with our VALUES statements. This will break on just about any complicated
		// Sparql
		int last = spq.lastIndexOf( '}' );
		String select = spq.substring( 0, last );
		String end = spq.substring( last );
		return select + binds.toString() + end;
	}

	@Override
	public AbstractBindable bindURI( String var, String uri ) {
		try {
			vmap.put( var, new URIImpl( uri ) );
		}
		catch ( Exception e ) {
			log.error( "Could not parse uri: " + uri, e );
		}
		return this;
	}

	@Override
	public AbstractBindable bindURI( String var, String basename, String localname ) {
		try {
			ValueFactory vfac = new ValueFactoryImpl();
			vmap.put( var, vfac.createURI( basename, localname ) );
		}
		catch ( Exception e ) {
			log.error( "Could not parse uri: " + basename + localname, e );
		}
		return this;
	}

	@Override
	public String getSparql() {
		return sparql;
	}

	@Override
	public void setBindings( Operation tq, ValueFactory fac ) {
		for ( Map.Entry<String, Integer> en : imap.entrySet() ) {
			tq.setBinding( en.getKey(), fac.createLiteral( en.getValue() ) );
		}
		for ( Map.Entry<String, Double> en : dmap.entrySet() ) {
			tq.setBinding( en.getKey(), fac.createLiteral( en.getValue() ) );
		}
		for ( Map.Entry<String, Boolean> en : bmap.entrySet() ) {
			tq.setBinding( en.getKey(), fac.createLiteral( en.getValue() ) );
		}
		for ( Map.Entry<String, Date> en : amap.entrySet() ) {
			Date d = en.getValue();
			XMLGregorianCalendar xcal = getCal( d );
			tq.setBinding( en.getKey(), fac.createLiteral( xcal ) );
		}
		for ( Map.Entry<String, Value> en : vmap.entrySet() ) {
			tq.setBinding( en.getKey(), en.getValue() );
		}

		for ( Map.Entry<String, StringPair> en : smap.entrySet() ) {
			String val = en.getValue().val;
			String lang = en.getValue().lang;

			if ( null == lang ) {
				tq.setBinding( en.getKey(), fac.createLiteral( val ) );
			}
			else {
				tq.setBinding( en.getKey(), fac.createLiteral( val, lang ) );
			}
		}
	}

	@Override
	public AbstractBindable bind( String var, String s ) {
		smap.put( var, new StringPair( s ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, Resource r ) {
		vmap.put( var, r );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, String s, String lang ) {
		smap.put( var, new StringPair( s, lang ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, double d ) {
		dmap.put( var, d );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, int d ) {
		imap.put( var, d );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, Date d ) {
		amap.put( var, d );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, boolean d ) {
		bmap.put( var, d );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, URI uri ) {
		vmap.put( var, uri );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, Value val ) {
		vmap.put( var, val );
		return this;
	}

	@Override
	public void useInferred( boolean b ) {
		infer = b;
	}

	@Override
	public boolean usesInferred() {
		return infer;
	}

	@Override
	public void done() {
	}

	public static XMLGregorianCalendar getCal( Date date ) {
		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.setTime( date );
		XMLGregorianCalendar xmlCalendar = null;
		try {
			xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar( gCalendar );
		}
		catch ( DatatypeConfigurationException ex ) {
			log.error( ex );
		}
		return xmlCalendar;
	}

	public static Date getDate( XMLGregorianCalendar cal ) {
		return cal.toGregorianCalendar().getTime();
	}

	private class StringPair {

		String val;
		String lang;

		public StringPair( String v ) {
			this( v, null );
		}

		public StringPair( String v, String l ) {
			val = v;
			lang = l;
		}
	}

	@Override
	public String toString() {
		return getSparql();
	}
}
