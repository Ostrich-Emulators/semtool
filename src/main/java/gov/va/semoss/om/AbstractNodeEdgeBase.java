/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.util.Constants;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import java.util.Set;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class AbstractNodeEdgeBase implements NodeEdgeBase {

	private final transient Map<URI, Value> properties = new HashMap<>();
	private transient boolean visible = true;
	private transient URI id;
	private transient Color color;
	private transient String colorString;
	// callers can "mark" properties for their own use
	private final Set<URI> markedProperties = new HashSet<>();

	private Map<String, Object> propHash = new HashMap<>(); //this is sent to the js (ChartIt)

	public AbstractNodeEdgeBase( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public AbstractNodeEdgeBase( URI id, URI type, String label ) {
		this.id = id;

		properties.put( RDF.SUBJECT, id );
		properties.put( RDFS.LABEL, new LiteralImpl( label ) );
		properties.put( RDF.TYPE, null == type ? Constants.ANYNODE : type );
	}

	@Override
	public final void setColor( Color _color ) {
		color = _color;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public void setColorString( String _colorString ) {
		colorString = _colorString;
	}

	public String getColorString() {
		return colorString;
	}

	@Override
	public void setVisible( boolean b ) {
		visible = b;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Method setProperty.
	 *
	 * @param propNameURI String
	 * @param propValue Object
	 */
	public void setProperty( String propNameURI, Object propValue ) {
		setProperty( new URIImpl( propNameURI ), propValue );
	}

	/**
	 * Sets a new label for this vertex. This function is really a convenience to
	 * {@link #putProperty(java.lang.String, java.lang.String)}, but doesn't go
	 * through the same error-checking. Any name is acceptable. We can always
	 * rename a label.
	 *
	 * @param label the new label to set
	 */
	@Override
	public void setLabel( String label ) {
		setProperty( RDFS.LABEL, label );
	}

	@Override
	public String getLabel() {
		return ( properties.containsKey( RDFS.LABEL )
				? properties.get( RDFS.LABEL ).toString() : "" );
	}

	public void setProperty( URI prop, Object propValue ) {
		setValue( prop, ValueTableModel.getValueFromObject( propValue ) );
	}

	@Override
	public void setValue( URI prop, Value val ) {
		properties.put( prop, val );
	}

	public void setPropHash( Map<String, Object> _propHash ) {
		propHash = _propHash;
	}

	public Map<String, Object> getPropHash() {
		return propHash;
	}

	@Override
	public Object getProperty( URI prop ) {
		return ValueTableModel.getObjectFromValue( getValue( prop ) );
	}

	@Override
	public Value getValue( URI prop ) {
		return properties.get( prop );
	}

	@Override
	public Map<URI, Object> getProperties() {
		Map<URI, Object> map = new HashMap<>();
		for ( Map.Entry<URI, Value> en : properties.entrySet() ) {
			map.put( en.getKey(), ValueTableModel.getObjectFromValue( en.getValue() ) );
		}
		return map;
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
	 * property from a call to {@link #getProperty(org.openrdf.model.URI) }
	 *
	 * @param prop the property to find
	 * @return the datatype, or {@link XMLSchema#ANYURI} if the value is a URI, or
	 * {@link XMLSChema#ENTITY} for a BNode.
	 */
	public URI getDataType( URI prop ) {
		if ( properties.containsKey( prop ) ) {
			Value data = properties.get( prop );
			if ( data instanceof URI ) {
				return XMLSchema.ANYURI;
			}
			else if ( data instanceof BNode ) {
				return XMLSchema.ENTITY;
			}
			return Literal.class.cast( data ).getDatatype();
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
		hash = 41 * hash + Objects.hashCode( this.id );
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
		final AbstractNodeEdgeBase other = (AbstractNodeEdgeBase) obj;
		if ( !Objects.equals( this.id, other.id ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return id + "; " + getType().getLocalName() + "; " + getLabel();
	}
}
