package gov.va.semoss.rdf.engine.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;

/**
 * An interface to specify generic SPARQL against the triplestore. This handles
 * binding variables and results generation.
 *
 * @author ryan
 * @param <T> what gets returned after the execution of the query
 */
public interface QueryExecutor<T> {

	public void setSparql( String sparql );

	/**
	 * Binds a URI represented by this string to a variable
	 *
	 * @param var the binding to set
	 * @param uri a string representation of the URI to set
	 */
	public void bindURI( String var, String uri );

	/**
	 * Binds a URI represented by this string to a variable
	 *
	 * @param var the binding to set
	 * @param basename a string representation of the URI basename
	 * @param localname a string representation of the URI localname
	 */
	public void bindURI( String var, String basename, String localname );

	public void bind( String var, URI uri );

	/**
	 * Binds a string literal to a variable, without a language tag
	 *
	 * @param var
	 * @param s
	 */
	public void bind( String var, String s );

	public void bind( String var, Resource rsr );

	public void bind( String var, String s, String lang );

	public void bind( String var, double d );

	public void bind( String var, int d );

	public void bind( String var, Date d );

	public void bind( String var, boolean d );

	public void useInferred( boolean b );

	public boolean usesInferred();

	/**
	 * Gets the SparQL from this executor.
	 *
	 * @return
	 */
	public String getSparql();

	/**
	 * If the engine that will run this Executor doesn't support bindings, then
	 * this function can simulate bindings. It is strictly an alternate to calling
   * {@link #setBindings(org.openrdf.query.TupleQuery, org.openrdf.model.ValueFactory) }
	 * followed by {@link #getSparql()} when your engine doesn't support this
	 * behavior.
	 *
	 * @return the Sparql string, with all the bindings replaced
	 */
	public String bindAndGetSparql();

	/**
	 * Sets the bindings for the tuple immediately before evaluating it. This
	 * function is automatically called from the {@link IEngine}.
	 *
	 * @param tq
	 * @param fac
	 */
	public void setBindings( TupleQuery tq, ValueFactory fac );

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
	 * Finished iterating through the tuples
	 */
	public void done();

	/**
	 * Retrieves the results once all the tuples have been handled
	 *
	 * @return
	 */
	public T getResults();

	public List<String> getBindingNames();

	/**
	 * Gets a reference to this Executor's namespace map. Namespaces specifically
	 * set on the executor take precedence over any other namespace setting
	 *
	 * @return The reference (not a copy) to the namespace map
	 */
	public Map<String, String> getNamespaces();

	public void setNamespaces( Map<String, String> ns );

	public void addNamespaces( Map<String, String> ns );

	public void addNamespace( String prefix, String namespace );

	public void removeNamespace( String prefix );

}
