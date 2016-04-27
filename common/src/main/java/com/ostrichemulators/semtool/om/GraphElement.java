/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
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
	
	public Color getColor();

	public String getLabel();

	public void setLabel( String name );

	public Map<URI, Object> getProperties();

	public Object getProperty( URI prop );

	public URI getType();

	public URI getURI();

	public Value getValue( URI prop );

	public Map<URI, Value> getValues();

	public Set<URI> getPropertyKeys();

	public boolean hasProperty( URI prop );

	public boolean isMarked( URI prop );

	public boolean isVisible();

	public void mark( URI prop, boolean makeMark );

	public void removeProperty( URI prop );

	public void setColor( Color _color );

	public void setType( URI type );

	public void setURI( URI uri );

	public void setValue( URI prop, Value val );

	public void setVisible( boolean b );

	public void addPropertyChangeListener( PropertyChangeListener pcl );

	public void removePropertyChangeListener( PropertyChangeListener pcl );

	public Collection<PropertyChangeListener> getPropertyChangeListeners();
}
