/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.om.AbstractGraphElement;
import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.util.MultiSetMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author ryan
 */
public abstract class AbstractQueryGraphElement extends AbstractGraphElement
		implements QueryGraphElement {

	private final MultiSetMap<IRI, Value> properties = new MultiSetMap<>();
	private final Set<IRI> selecteds = new HashSet<>();
	private final Set<IRI> optionals = new HashSet<>();
	private final Map<IRI, String> labels = new HashMap<>();
	private final Map<IRI, String> filters = new HashMap<>();
	private String queryId;

	public AbstractQueryGraphElement( IRI id ) {
		super( id );

		for ( Map.Entry<IRI, Value> en : super.getValues().entrySet() ) {
			properties.add( en.getKey(), en.getValue() );
		}
	}

	public AbstractQueryGraphElement( IRI id, IRI type, String label ) {
		super( id, type, label );
		for ( Map.Entry<IRI, Value> en : super.getValues().entrySet() ) {
			properties.add( en.getKey(), en.getValue() );
		}
	}

	@Override
	public AbstractQueryGraphElement duplicate() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean isSelected( IRI prop ) {
		return selecteds.contains( prop );
	}

	@Override
	public void setSelected( IRI prop, boolean selected ) {
		if ( selected ) {
			selecteds.add( prop );
		}
		else {
			selecteds.remove( prop );
		}
	}

	@Override
	public boolean isOptional( IRI prop ) {
		return optionals.contains( prop );
	}

	@Override
	public void setOptional( IRI prop, boolean optional ) {
		if ( optional ) {
			optionals.add( prop );
		}
		else {
			optionals.remove( prop );
		}
	}

	@Override
	public void removeProperty( IRI prop ) {
		setOptional( prop, false );
		setSelected( prop, false );
		properties.remove( prop );
	}

	@Override
	public Map<IRI, Value> getValues() {
		Map<IRI, Value> map = new HashMap<>();
		for ( Map.Entry<IRI, Set<Value>> en : properties.entrySet() ) {
			map.put( en.getKey(), en.getValue().iterator().next() );
		}

		return map;
	}

	@Override
	public Value getValue( IRI prop ) {
		if ( properties.containsKey( prop ) ) {
			return properties.get( prop ).iterator().next();
		}

		return null;
	}

	@Override
	public void setProperty( IRI prop, Object propValue ) {
		properties.remove( prop );
		setProperty( prop, RDFDatatypeTools.getValueFromObject( propValue ), true );
	}

	@Override
	public void setProperty( IRI prop, Value v, boolean add ) {
		if ( add ) {
			properties.add( prop, v );
		}
		else {
			if ( properties.containsKey( prop ) ) {
				properties.get( prop ).remove( v );
				filters.remove( prop );

				if ( properties.get( prop ).isEmpty() ) {
					properties.remove( prop );
					filters.remove( prop );
				}
			}
		}
	}

	@Override
	public void setProperties( IRI prop, Collection<Value> vals ) {
		properties.remove( prop );
		filters.remove( prop );
		properties.addAll( prop, vals );
	}

	@Override
	public void setValue( IRI prop, Value val ) {
		properties.remove( prop );
		filters.remove( prop );
		properties.add( prop, val );
	}

	@Override
	public Map<IRI, Set<Value>> getAllValues() {
		return properties;
	}

	@Override
	public Set<Value> getValues( IRI prop ) {
		return properties.get( prop );
	}

	@Override
	public void setLabel( IRI prop, String label ) {
		String oldlabel = labels.get( prop );
		if ( null != oldlabel && hasFilter( prop ) ) {
			// update the filter labels, if we have any
			String filter = getFilter( prop ).replaceAll( "\\?" + oldlabel + "\\b",
					"?" + label );
			setFilter( prop, filter );
		}

		labels.put( prop, label );
	}

	@Override
	public String getLabel( IRI prop ) {
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
	public boolean hasProperty( IRI prop ) {
		return properties.containsKey( prop );
	}

	@Override
	public void setFilter( IRI prop, String str ) {
		filters.put( prop, str );
	}

	@Override
	public String getFilter( IRI prop ) {
		return filters.get( prop );
	}

	@Override
	public boolean hasFilter( IRI prop ) {
		return filters.containsKey( prop );
	}
}
