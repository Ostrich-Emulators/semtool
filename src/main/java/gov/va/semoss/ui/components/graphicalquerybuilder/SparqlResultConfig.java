/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openrdf.model.URI;

/**
 * A class to handle custom sparql result labels and such (to avoid our query
 * having things like ?obj1 or ?node4).
 *
 * @author ryan
 */
public class SparqlResultConfig implements Comparable<SparqlResultConfig> {

	private final AbstractNodeEdgeBase id;
	private URI property;
	private String label;
	private boolean optional = false;

	public SparqlResultConfig( AbstractNodeEdgeBase id ) {
		this.id = id;
	}

	public SparqlResultConfig( AbstractNodeEdgeBase id, URI property, String name ) {
		this.id = id;
		this.property = property;
		label = name;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional( boolean b ) {
		optional = b;
	}

	public URI getProperty() {
		return property;
	}

	/**
	 * Gets the result label for this property, or null if nothing is set
	 *
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	public void setLabel( String s ) {
		label = s;
	}

	public AbstractNodeEdgeBase getId() {
		return id;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + Objects.hashCode( this.id );
		hash = 61 * hash + Objects.hashCode( this.property );
		hash = 61 * hash + Objects.hashCode( this.label );
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
		final SparqlResultConfig other = (SparqlResultConfig) obj;
		if ( !Objects.equals( this.id, other.id ) ) {
			return false;
		}
		if ( !Objects.equals( this.property, other.property ) ) {
			return false;
		}
		if ( !Objects.equals( this.label, other.label ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo( SparqlResultConfig o ) {
		int diff = id.getURI().stringValue().compareTo( o.getId().getURI().stringValue() );

		if ( 0 == diff ) {
			diff = property.stringValue().compareTo( o.getProperty().stringValue() );

			if ( 0 == diff ) {
				return label.compareTo( o.getLabel() );
			}
		}

		return diff;
	}

	public static Map<URI, String> asMap( List<SparqlResultConfig> list ) {
		Map<URI, String> labels = new HashMap<>();
		for ( SparqlResultConfig src : list ) {
			labels.put( src.getProperty(), src.getLabel() );
		}
		return labels;
	}

	public static SparqlResultConfig getOne( List<SparqlResultConfig> list, URI type ) {
		for( SparqlResultConfig src : list ){
			if( src.getProperty().equals(  type ) ){
				return src;
			}
		}
		
		return null;
	}

	public static Set<URI> getProperties( Map<AbstractNodeEdgeBase, List<SparqlResultConfig>> map ) {
		Set<URI> props = new HashSet<>();
		for ( Map.Entry<AbstractNodeEdgeBase, List<SparqlResultConfig>> en : map.entrySet() ) {
			for ( SparqlResultConfig src : en.getValue() ) {
				props.add( src.getProperty() );
			}
		}

		return props;
	}
}
