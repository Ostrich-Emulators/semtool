/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.util.Constants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public abstract class AbstractGraphElement implements GraphElement {

	private final transient List<PropertyChangeListener> listeners = new ArrayList<>();
	private final transient Map<URI, Value> properties = new HashMap<>();

	// callers can "mark" properties for their own use
	private final Set<URI> markedProperties = new HashSet<>();

	private Map<String, Object> propHash = new HashMap<>(); //this is sent to the js (ChartIt)

	public AbstractGraphElement( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public AbstractGraphElement( URI id, URI type, String label ) {
		properties.put( RDF.SUBJECT, id );
		properties.put( RDFS.LABEL, new LiteralImpl( label ) );
		properties.put( RDF.TYPE, null == type ? Constants.ANYNODE : type );
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener pcl ) {
		listeners.add( pcl );
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener pcl ) {
		listeners.remove( pcl );
	}

	@Override
	public Collection<PropertyChangeListener> getPropertyChangeListeners() {
		return new ArrayList<>( listeners );
	}

	protected void fireIfPropertyChanged( String prop, Object oldval, Object newval ) {
		// we only want to fire if we need to, so check that something has 
		// actually changed
		boolean oldNull = ( null == oldval );
		boolean newNull = ( null == newval );

		if ( oldNull && newNull ) {
			return;
		}

		if ( !Objects.equals( oldval, newval ) ) {
			PropertyChangeEvent pce
					= new PropertyChangeEvent( this, prop, oldval, newval );
			for ( PropertyChangeListener pcl : listeners ) {
				pcl.propertyChange( pce );
			}
		}
	}

	/**
	 * Sets a new label for this vertex. This function is a convenience to
	 * {@link #setValue(org.openrdf.model.URI, org.openrdf.model.Value)  }
	 * Any name is acceptable. We can always rename a vertex or edge.
	 *
	 * @param label the new label to set
	 */
	@Override
	public void setLabel( String label ) {
		setValue( RDFS.LABEL, RDFDatatypeTools.getValueFromObject( label ) );
	}

	@Override
	public Set<URI> getPropertyKeys() {
		return new HashSet<>( properties.keySet() );
	}

	@Override
	public String getLabel() {
		return ( properties.containsKey( RDFS.LABEL )
				? properties.get( RDFS.LABEL ).stringValue() : "" );
	}

	@Override
	public void setValue( URI prop, Value val ) {
		Value oldval = properties.get( prop );
		properties.put( prop, val );
		fireIfPropertyChanged( prop.toString(), oldval, val );
	}

	public void setPropHash( Map<String, Object> _propHash ) {
		propHash = _propHash;
	}

	public Map<String, Object> getPropHash() {
		return propHash;
	}

	@Override
	public Value getValue( URI prop ) {
		return properties.get( prop );
	}

	@Override
	public Map<URI, Value> getValues() {
		return properties;
	}

	@Override
	public boolean hasProperty( URI prop ) {
		return properties.containsKey( prop );
	}

	@Override
	public void setURI( URI uri ) {
		setValue( RDF.SUBJECT, uri );
	}

	@Override
	public URI getURI() {
		return URI.class.cast( getValue( RDF.SUBJECT ) );
	}

	@Override
	public URI getType() {
		return URI.class.cast( getValue( RDF.TYPE ) );
	}

	@Override
	public void setType( URI type ) {
		setValue( RDF.TYPE, type );
	}

	@Override
	public void removeProperty( URI prop ) {
		properties.remove( prop );
	}

	/**
	 * Gets the datatype for the value that would be returned for the given
	 * property from a call to {@link #getValue(org.openrdf.model.URI)  }
	 *
	 * @param prop the property to find
	 * @return the datatype, or {@link XMLSchema#ANYURI} if the value is a URI, or
	 * {@link XMLSchema#ENTITY} for a BNode.
	 */
	public URI getDataType( URI prop ) {
		if ( properties.containsKey( prop ) ) {
			Value data = properties.get( prop );
			return RDFDatatypeTools.getDatatype( data );
		}
		return null;
	}

	@Override
	public void mark( URI prop, boolean makeMark ) {
		if ( makeMark && hasProperty( prop ) ) {
			markedProperties.add( prop );
		}
		else {
			markedProperties.remove( prop );
		}
	}

	public Set<URI> getMarkedProperties() {
		return new HashSet<>( markedProperties );
	}

	@Override
	public boolean isMarked( URI prop ) {
		return markedProperties.contains( prop );
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode( this.getURI() );
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		final AbstractGraphElement other = (AbstractGraphElement) obj;

		return Objects.equals( this.getURI(), other.getURI() );
	}

	@Override
	public String toString() {
		return getURI() + "; " + getType().getLocalName() + "; " + getLabel();
	}
}
