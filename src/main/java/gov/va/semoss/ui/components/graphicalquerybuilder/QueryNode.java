/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.util.MultiSetMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryNode extends SEMOSSVertex implements QueryNodeEdgeBase {

	private final MultiSetMap<URI, Value> properties = new MultiSetMap<>();
	private final Set<URI> selecteds = new HashSet<>();
	private final Set<URI> optionals = new HashSet<>();
	private final Map<URI, String> labels = new HashMap<>();
	private String queryId;

	public QueryNode( URI id ) {
		super( id );

		for ( Map.Entry<URI, Value> en : super.getValues().entrySet() ) {
			properties.add( en.getKey(), en.getValue() );
		}
	}

	public QueryNode( URI id, URI type, String label ) {
		super( id, type, label );

		for ( Map.Entry<URI, Value> en : super.getValues().entrySet() ) {
			properties.add( en.getKey(), en.getValue() );
		}
	}

	@Override
	public boolean isSelected( URI prop ) {
		return selecteds.contains( prop );
	}

	@Override
	public void setSelected( URI prop, boolean selected ) {
		if ( selected ) {
			selecteds.add( prop );
		}
		else {
			selecteds.remove( prop );
		}
	}

	@Override
	public boolean isOptional( URI prop ) {
		return optionals.contains( prop );
	}

	@Override
	public void setOptional( URI prop, boolean optional ) {
		if ( optional ) {
			optionals.add( prop );
		}
		else {
			optionals.remove( prop );
		}
	}

	@Override
	public void removeProperty( URI prop ) {
		properties.remove( prop );
	}

	@Override
	public Map<URI, Value> getValues() {
		Map<URI, Value> map = new HashMap<>();
		for ( Map.Entry<URI, Set<Value>> en : properties.entrySet() ) {
			map.put( en.getKey(), en.getValue().iterator().next() );
		}

		return map;
	}

	@Override
	public Map<URI, Object> getProperties() {
		Map<URI, Object> map = new HashMap<>();
		for ( Map.Entry<URI, Set<Value>> en : properties.entrySet() ) {
			if ( en.getValue().isEmpty() ) {
				Logger.getLogger( getClass() ).debug( "empty property!" + en.getKey() );
			}

			map.put( en.getKey(),
					ValueTableModel.getObjectFromValue( en.getValue().iterator().next() ) );
		}
		return map;
	}

	@Override
	public Value getValue( URI prop ) {
		if ( properties.containsKey( prop ) ) {
			return properties.get( prop ).iterator().next();
		}

		return null;
	}

	@Override
	public Object getProperty( URI prop ) {
		return ValueTableModel.getObjectFromValue( getValue( prop ) );
	}

	@Override
	public void setProperty( URI prop, Object propValue ) {
		properties.remove( prop );
		setProperty( prop, ValueTableModel.getValueFromObject( propValue ), true );
	}

	@Override
	public void setProperty( URI prop, Value v, boolean add ) {
		if ( add ) {
			properties.add( prop, v );
		}
		else {
			if ( properties.containsKey( prop ) ) {
				properties.get( prop ).remove( v );

				if ( properties.get( prop ).isEmpty() ) {
					properties.remove( prop );
				}
			}
		}
	}

	@Override
	public void setProperties( URI prop, Collection<Value> vals ) {
		properties.remove( prop );
		properties.addAll( prop, vals );
	}

	@Override
	public Map<URI, Set<Value>> getAllValues() {
		return properties;
	}

	@Override
	public Set<Value> getValues( URI prop ) {
		return properties.get( prop );
	}

	@Override
	public void setLabel( URI prop, String label ) {
		labels.put( prop, label );
	}

	@Override
	public String getLabel( URI prop ) {
		return labels.get( prop );
	}

	@Override
	public void setQueryId( String id ) {
		queryId = id;
	}

	@Override
	public String getQueryId() {
		return queryId;
	}

	@Override
	public URI getType() {
		return ( null == properties ? super.getType()
				: URI.class.cast( getValue( RDF.TYPE ) ) );
	}
}
