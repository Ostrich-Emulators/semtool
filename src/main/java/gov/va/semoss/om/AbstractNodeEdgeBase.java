/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import gov.va.semoss.util.Constants;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class AbstractNodeEdgeBase {

	private static final Logger log = Logger.getLogger( AbstractNodeEdgeBase.class );
	public static final URI LEVEL = new URIImpl( "semoss://graphing.level" );

	private Color color;
	private String colorString;
	private final Map<URI, Object> properties = new HashMap<>();
	private URI id;
	private boolean visible = true;

	public AbstractNodeEdgeBase( URI id ) {
		this( id, null, id.getLocalName() );
	}

	public AbstractNodeEdgeBase( URI id, URI type, String label ) {
		this.id = id;
		properties.put( RDF.SUBJECT, id );
		properties.put( RDFS.LABEL, label );
		properties.put( RDF.TYPE, null == type ? Constants.ANYNODE : type );
		properties.put( LEVEL, 1 );
	}

	public void setLevel( int lev ) {
		setProperty( LEVEL, lev );
	}

	public int getLevel() {
		return Integer.class.cast( getProperty( LEVEL ) );
	}

	public final void setColor( Color _color ) {
		color = _color;
	}

	public Color getColor() {
		return color;
	}

	public void setColorString( String _colorString ) {
		colorString = _colorString;
	}

	public String getColorString() {
		return colorString;
	}

	public void setVisible( boolean b ) {
		visible = b;
	}

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
	public void setLabel( String label ) {
		properties.put( RDFS.LABEL, label );
	}

	public String getLabel() {
		return properties.get( RDFS.LABEL ).toString();
	}

	public void setProperty( URI prop, Object propValue ) {
		properties.put( prop, propValue );
	}

	public Object getProperty( URI arg0 ) {
		return properties.get( arg0 );
	}

	public Map<URI, Object> getProperties() {
		return properties;
	}

	public boolean hasProperty( URI prop ) {
		return properties.containsKey( prop );
	}

	public void setURI( URI uri ) {
		setProperty( RDF.SUBJECT, uri );
	}

	public URI getURI() {
		return URI.class.cast( getProperty( RDF.SUBJECT ) );
	}

	public URI getType() {
		return URI.class.cast( getProperty( RDF.TYPE ) );
	}

	public void setType( URI type ) {
		setProperty( RDF.TYPE, type );
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
		return id + "; " + getType().getLocalName() + "; " + getLabel() + "; level "
				+ getLevel();
	}
}
