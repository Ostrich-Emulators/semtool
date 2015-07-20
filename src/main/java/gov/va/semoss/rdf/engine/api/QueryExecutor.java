package gov.va.semoss.rdf.engine.api;

import java.util.List;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

/**
 * An interface to specify generic SPARQL against the triplestore. This handles
 * binding variables and results generation.
 *
 * @author ryan
 * @param <T> what gets returned after the execution of the query
 */
public interface QueryExecutor<T> extends Bindable<T>{

	/**
	 * Handles one tuple during the query execution. Generally, this will be the
	 * only function that subclasses must implement
	 *
	 * @param set
	 * @param fac
	 */
	public void handleTuple( BindingSet set, ValueFactory fac );

	/**
	 * Starting to iterate through the tuples
	 *
	 * @param bindingNames the names of this query
	 */
	public void start( List<String> bindingNames );

	/**
	 * Retrieves the results once all the tuples have been handled
	 *
	 * @return
	 */
	public T getResults();
}
