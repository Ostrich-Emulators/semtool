/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public interface GraphElement {

	public boolean isNode();

	public String getLabel();

	public void setLabel( String name );

	public URI getType();

	public URI getURI();

	public Value getValue( URI prop );

	public Map<URI, Value> getValues();

	public Set<URI> getPropertyKeys();

	public boolean hasProperty( URI prop );

	public void removeProperty( URI prop );

	public void setType( URI type );

	public void setURI( URI uri );

	public void setValue( URI prop, Value val );

	/**
	 * Duplicates this graph element
	 * @param <X> the type of element (used for subclasses)
	 * @return a new object initialized exactly like this one
	 */
	public <X extends GraphElement> X duplicate();

	/**
	 * Sets an ID to be used by the graphing system (and
	 * <code>equals(java.lang.Object)</code> and <code>hashCode()</code>. JUNG
	 * requires each node/edge to have a unique id, which isn't always the case
	 * for us. For example, many edges may have the same ID, but we still want
	 * them displayed on the graph.
	 *
	 * @param graphid
	 */
	public void setGraphId( URI graphid );

	/**
	 * Gets the ID for the graphing system.
	 *
	 * @return
	 */
	public URI getGraphId();

	public void addPropertyChangeListener( PropertyChangeListener pcl );

	public void removePropertyChangeListener( PropertyChangeListener pcl );

	public Collection<PropertyChangeListener> getPropertyChangeListeners();
}
