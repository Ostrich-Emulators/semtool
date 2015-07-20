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

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 * @param <T> the type of the result
 */
public abstract class QueryExecutorAdapter<T> extends AbstractBindable<T>
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
	
	public List<String> getBindingNames(){
		return bindNames;
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
}
