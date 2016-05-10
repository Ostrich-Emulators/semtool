/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util;

import com.ostrichemulators.semtool.rdf.engine.api.Bindable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Operation;

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 */
public abstract class AbstractBindable implements Bindable {

	private static final Logger log = Logger.getLogger( AbstractBindable.class );
	private final Map<String, Value> vmap = new LinkedHashMap<>();
	private final Map<String, String> namespaces = new LinkedHashMap<>();
	private final ValueFactory vf = new ValueFactoryImpl();
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
		return getBoundSparql( sparql, vmap );
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
		for ( Map.Entry<String, Value> en : vmap.entrySet() ) {
			tq.setBinding( en.getKey(), en.getValue() );
		}
	}

	@Override
	public AbstractBindable bind( String var, String s ) {
		vmap.put( var, vf.createLiteral( s ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, Resource r ) {
		vmap.put( var, r );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, String s, String lang ) {
		vmap.put( var, vf.createLiteral( s, lang ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, double d ) {
		vmap.put( var, vf.createLiteral( d ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, int d ) {
		vmap.put( var, vf.createLiteral( d ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, Date d ) {
		vmap.put( var, vf.createLiteral( getCal( d ) ) );
		return this;
	}

	@Override
	public AbstractBindable bind( String var, boolean d ) {
		vmap.put( var, vf.createLiteral( d ) );
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

	@Override
	public Map<String, Value> getBindingMap() {
		return new LinkedHashMap<>( vmap );
	}

	@Override
	public void setBindings( Map<String, Value> vals ) {
		vmap.clear();
		addBindings( vals );
	}

	@Override
	public void addBindings( Map<String, Value> map ) {
		vmap.putAll( map );
	}

	@Override
	public String toString() {
		return getSparql();
	}

	/**
	 * Gets the given sparql with VALUES clauses appended for the given bindings.
	 * This function does not cover all possible SPARQL, so please use with care
	 *
	 * @param sparql
	 * @param bindings
	 * @return
	 */
	public static String getBoundSparql( String sparql, Map<String, Value> bindings ) {
		StringBuilder binds = new StringBuilder();

		for ( Map.Entry<String, Value> en : bindings.entrySet() ) {
			String var = en.getKey();

			//Skip making a "VALUES" clause if the variable isn't in the query
			Pattern pattern = Pattern.compile( "\\?" + var + "\\b" );
			Matcher matcher = pattern.matcher( sparql );
			if ( !matcher.find() ) {
				continue;
			}

			Value v = en.getValue();
			binds.append( " VALUES ?" ).append( var );
			if ( v instanceof Literal ) {
				binds.append( " {" ).append( en.getValue() ).append( "}" );
			}
			else {
				binds.append( " {<" ).append( en.getValue() ).append( ">}" );
			}
		}

		// I don't really want to write a Sparql parser, so just replace the last }
		// with our VALUES statements. This will break on just about any complicated
		// Sparql
		int last = sparql.lastIndexOf( '}' );
		if( last > -1 ){
			String select = sparql.substring( 0, last );
			String end = sparql.substring( last );
			return select + binds.toString() + end;
		}
		else{
			// no }, so just append the bindings (is this even valid?)
			return sparql + binds.toString();
		}
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
}
