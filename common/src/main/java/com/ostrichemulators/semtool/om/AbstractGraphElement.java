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

import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public abstract class AbstractGraphElement
		implements GraphElement, Comparable<AbstractGraphElement> {

	private static final Set<IRI> COUNTABLES = new HashSet<>();
	private final transient List<PropertyChangeListener> listeners = new ArrayList<>();
	private final transient Map<IRI, Value> properties = new HashMap<>();
	private IRI graphid;
	private Map<String, Object> propHash = new HashMap<>(); //this is sent to the js (ChartIt)

	static {
		COUNTABLES.add( XMLSchema.INT );
		COUNTABLES.add( XMLSchema.INTEGER );
		COUNTABLES.add( XMLSchema.FLOAT );
		COUNTABLES.add( XMLSchema.DOUBLE );
		COUNTABLES.add( XMLSchema.DECIMAL );
		COUNTABLES.add( XMLSchema.DATE );
		COUNTABLES.add( XMLSchema.DATETIME );
		COUNTABLES.add( XMLSchema.TIME );
		COUNTABLES.add( XMLSchema.LONG );
		COUNTABLES.add( XMLSchema.SHORT );
	}

	public AbstractGraphElement( IRI id ) {
		this( id, null, id.getLocalName() );
	}

	public AbstractGraphElement( IRI id, IRI type, String label ) {
		properties.put( RDF.SUBJECT, id );
		properties.put( RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( label ) );
		properties.put( RDF.TYPE, null == type ? Constants.ANYNODE : type );
		graphid = id;
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
	 * Sets a new label for this vertex. This function is a convenience to null
	 * null	null	null	null	null	 {@link #setValue(org.openrdf.model.IRI, org.openrdf.model.Value)
	 * }
	 * Any name is acceptable. We can always rename a vertex or edge.
	 *
	 * @param label the new label to set
	 */
	@Override
	public void setLabel( String label ) {
		setValue( RDFS.LABEL, RDFDatatypeTools.getValueFromObject( label ) );
	}

	@Override
	public Set<IRI> getPropertyKeys() {
		return new HashSet<>( properties.keySet() );
	}

	@Override
	public String getLabel() {
		return ( properties.containsKey( RDFS.LABEL )
				? properties.get( RDFS.LABEL ).stringValue() : "" );
	}

	@Override
	public void setValue( IRI prop, Value val ) {
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
	public Value getValue( IRI prop ) {
		return properties.get( prop );
	}

	@Override
	public Map<IRI, Value> getValues() {
		return properties;
	}

	@Override
	public boolean hasProperty( IRI prop ) {
		return properties.containsKey( prop );
	}

	@Override
	public void setIRI( IRI IRI ) {
		setValue( RDF.SUBJECT, IRI );
	}

	@Override
	public IRI getIRI() {
		return IRI.class.cast( getValue( RDF.SUBJECT ) );
	}

	@Override
	public IRI getType() {
		return IRI.class.cast( getValue( RDF.TYPE ) );
	}

	@Override
	public void setType( IRI type ) {
		setValue( RDF.TYPE, type );
	}

	@Override
	public void removeProperty( IRI prop ) {
		properties.remove( prop );
	}

	/**
	 * Gets the datatype for the value that would be returned for the given
	 * property from a call to {@link #getValue(org.openrdf.model.IRI) }
	 *
	 * @param prop the property to find
	 * @return the datatype, or {@link XMLSchema#ANYIRI} if the value is a IRI, or
	 * {@link XMLSchema#ENTITY} for a BNode.
	 */
	public IRI getDataType( IRI prop ) {
		if ( properties.containsKey( prop ) ) {
			Value data = properties.get( prop );
			return RDFDatatypeTools.getDatatype( data );
		}
		return null;
	}

	@Override
	public IRI getGraphId() {
		return graphid;
	}

	@Override
	public void setGraphId( IRI graphid ) {
		this.graphid = graphid;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode( this.getGraphId() );
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

		return Objects.equals( this.getGraphId(), other.getGraphId() );
	}

	@Override
	public String toString() {
		return getIRI() + "; " + getType().getLocalName() + "; " + getLabel();
	}

	@Override
	public int compareTo( AbstractGraphElement o ) {
		return toString().compareTo( o.toString() );
	}

	/**
	 * Gets all the property keys that have "countable" values (int, double, date)
	 *
	 * @param age
	 * @return
	 */
	public static List<IRI> getCountablePropertyKeys( GraphElement age ) {
		List<IRI> keys = new ArrayList<>();
		for ( Map.Entry<IRI, Value> en : age.getValues().entrySet() ) {
			Value val = en.getValue();
			if ( val instanceof Literal ) {
				Literal lit = Literal.class.cast( val );
				IRI dt = lit.getDatatype();
				if ( COUNTABLES.contains( dt ) ) {
					keys.add( en.getKey() );
				}
			}
		}
		return keys;
	}
}
