/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQuery;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 * @param <T> the type of the result
 */
public abstract class QueryExecutorAdapter<T> implements QueryExecutor<T> {

	private static final Logger log = Logger.getLogger( QueryExecutorAdapter.class );
	private final Map<String, Double> dmap = new HashMap<>();
	private final Map<String, Integer> imap = new HashMap<>();
	private final Map<String, Date> amap = new HashMap<>();
	private final Map<String, StringPair> smap = new HashMap<>();
	private final Map<String, Boolean> bmap = new HashMap<>();
	private final Map<String, URI> umap = new HashMap<>();
	private final Map<String, Resource> rmap = new HashMap<>();
	private final List<String> bindNames = new ArrayList<>();
	private String sparql;
	protected T result;
	private boolean infer = false;

	public QueryExecutorAdapter() {
	}

	public QueryExecutorAdapter( String sparq ) {
		sparql = sparq;
	}

	@Override
	public void setSparql( String sparq ) {
		sparql = sparq;
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
		for ( Map.Entry<String, Resource> en : rmap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {<" ).
					append( en.getValue() ).append( ">}" );
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

		for ( Map.Entry<String, URI> en : umap.entrySet() ) {
			binds.append( " VALUES ?" ).append( en.getKey() ).append( " {<" ).
					append( en.getValue() ).append( ">}" );
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
	public T getResults() {
		return result;
	}

	@Override
	public void bindURI( String var, String uri ) {
		try {
			umap.put( var, new URIImpl( uri ) );
		}
		catch ( Exception e ) {
			log.error( "Could not parse uri: " + uri, e );
		}
	}

	@Override
	public void bindURI( String var, String basename, String localname ) {
		try {
			ValueFactory vfac = new ValueFactoryImpl();
			umap.put( var, vfac.createURI( basename, localname ) );
		}
		catch ( Exception e ) {
			log.error( "Could not parse uri: " + basename + localname, e );
		}
	}

	@Override
	public String getSparql() {
		return sparql;
	}

	@Override
	public void setBindings( TupleQuery tq, ValueFactory fac ) {
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
		for ( Map.Entry<String, Resource> en : rmap.entrySet() ) {
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

		for ( Map.Entry<String, URI> en : umap.entrySet() ) {
			tq.setBinding( en.getKey(), en.getValue() );
		}
	}

	@Override
	public void bind( String var, String s ) {
		smap.put( var, new StringPair( s ) );
	}

	@Override
	public void bind( String var, Resource r ) {
		rmap.put( var, r );
	}

	@Override
	public void bind( String var, String s, String lang ) {
		smap.put( var, new StringPair( s, lang ) );
	}

	@Override
	public void bind( String var, double d ) {
		dmap.put( var, d );
	}

	@Override
	public void bind( String var, int d ) {
		imap.put( var, d );
	}

	@Override
	public void bind( String var, Date d ) {
		amap.put( var, d );
	}

	@Override
	public void bind( String var, boolean d ) {
		bmap.put( var, d );
	}

	@Override
	public void bind( String var, URI uri ) {
		umap.put( var, uri );
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
	public void start( List<String> bnames ) {
		bindNames.clear();
		bindNames.addAll( bnames );
	}

	public int getNumBindings() {
		return bindNames.size();
	}

	@Override
	public List<String> getBindingNames() {
		return new ArrayList<>( bindNames );
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
