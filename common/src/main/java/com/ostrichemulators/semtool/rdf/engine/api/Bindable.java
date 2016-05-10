/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import java.util.Date;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Operation;

/**
 *
 * @author ryan
 */
public interface Bindable {

	public void setSparql( String sparql );

	/**
	 * Binds a URI represented by this string to a variable
	 *
	 * @param var the binding to set
	 * @param uri a string representation of the URI to set
	 */
	public Bindable bindURI( String var, String uri );

	/**
	 * Binds a URI represented by this string to a variable
	 *
	 * @param var the binding to set
	 * @param basename a string representation of the URI basename
	 * @param localname a string representation of the URI localname
	 */
	public Bindable bindURI( String var, String basename, String localname );

	/**
	 * Binds a string literal to a variable, without a language tag
	 *
	 * @param var
	 * @param s
	 */
	public Bindable bind( String var, String s );

	public Bindable bind( String var, Resource rsr );

	public Bindable bind( String var, String s, String lang );

	public Bindable bind( String var, double d );

	public Bindable bind( String var, int d );

	public Bindable bind( String var, Date d );

	public Bindable bind( String var, boolean d );

	public Bindable bind( String var, Value v );

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
   * {@link #setBindings(org.openrdf.query.Operation, org.openrdf.model.ValueFactory) }
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
	public void setBindings( Operation tq, ValueFactory fac );

	/**
	 * Finished iterating through the tuples
	 */
	public void done();

	/**
	 * Gets a reference to this Bindable's namespace map. Namespaces specifically
	 * set on the executor take precedence over any other namespace setting
	 *
	 * @return The reference (not a copy) to the namespace map
	 */
	public Map<String, String> getNamespaces();

	public void setNamespaces( Map<String, String> ns );

	public void addNamespaces( Map<String, String> ns );

	public void addNamespace( String prefix, String namespace );

	public void removeNamespace( String prefix );

	/**
	 * Resets all the bindings to the given map
	 * @param vals the source of the bindings
	 */
	public void setBindings( Map<String, Value> vals );

	/**
	 * Copies all the bindings from the given map into this one
	 *
	 * @param vals the source of the bindings
	 */
	public void addBindings( Map<String, Value> vals );

	/**
	 * Gets a mapping of variables to values
	 *
	 * @return the mapping
	 */
	public Map<String, Value> getBindingMap();
}
