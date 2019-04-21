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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author ryan
 */
public interface GraphElement {

	public boolean isNode();

	public String getLabel();

	public void setLabel( String name );

	public IRI getType();

	public IRI getIRI();

	public Value getValue( IRI prop );

	public Map<IRI, Value> getValues();

	public Set<IRI> getPropertyKeys();

	public boolean hasProperty( IRI prop );

	public void removeProperty( IRI prop );

	public void setType( IRI type );

	public void setIRI( IRI IRI );

	public void setValue( IRI prop, Value val );

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
	public void setGraphId( IRI graphid );

	/**
	 * Gets the ID for the graphing system.
	 *
	 * @return
	 */
	public IRI getGraphId();

	public void addPropertyChangeListener( PropertyChangeListener pcl );

	public void removePropertyChangeListener( PropertyChangeListener pcl );

	public Collection<PropertyChangeListener> getPropertyChangeListeners();
}
