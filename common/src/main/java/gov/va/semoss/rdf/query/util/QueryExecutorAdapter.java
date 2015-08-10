/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import gov.va.semoss.rdf.engine.api.QueryExecutor;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 * @param <T> the type of the result
 */
public abstract class QueryExecutorAdapter<T> extends AbstractBindable
		implements QueryExecutor<T> {

	private static final Logger log = Logger.getLogger( QueryExecutorAdapter.class );

	private final List<String> bindNames = new ArrayList<>();
	protected T result;

	public QueryExecutorAdapter() {
	}

	public QueryExecutorAdapter( String sparq ) {
		super( sparq );
	}

	@Override
	public T getResults() {
		return result;
	}

	@Override
	public void start( List<String> bnames ) {
		bindNames.clear();
		bindNames.addAll( bnames );
	}

	public int getNumBindings() {
		return bindNames.size();
	}

	public List<String> getBindingNames() {
		return bindNames;
	}

	@Override
	public void done() {
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, URI uri ) {
		return (QueryExecutorAdapter<T>) super.bind( var, uri );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, boolean d ) {
		return (QueryExecutorAdapter<T>) super.bind( var, d );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, Date d ) {
		return (QueryExecutorAdapter<T>) super.bind( var, d );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, int d ) {
		return (QueryExecutorAdapter<T>) super.bind( var, d );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, double d ) {
		return (QueryExecutorAdapter<T>) super.bind( var, d );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, String s, String lang ) {
		return (QueryExecutorAdapter<T>) super.bind( var, s, lang );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, Resource r ) {
		return (QueryExecutorAdapter<T>) super.bind( var, r );
	}

	@Override
	public QueryExecutorAdapter<T> bind( String var, String s ) {
		return (QueryExecutorAdapter<T>) super.bind( var, s );
	}

	@Override
	public QueryExecutorAdapter<T> bindURI( String var, String basename, String localname ) {
		return (QueryExecutorAdapter<T>) super.bindURI( var, basename, localname );
	}

	@Override
	public QueryExecutorAdapter<T> bindURI( String var, String uri ) {
		return (QueryExecutorAdapter<T>) super.bindURI( var, uri );
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
