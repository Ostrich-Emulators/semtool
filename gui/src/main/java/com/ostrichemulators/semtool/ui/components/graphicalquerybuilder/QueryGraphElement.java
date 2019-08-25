/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.om.GraphElement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author ryan
 */
public interface QueryGraphElement extends GraphElement {

	public boolean isSelected( IRI prop );

	public void setSelected( IRI prop, boolean selected );

	public boolean isOptional( IRI prop );

	public void setOptional( IRI prop, boolean optional );

	public Map<IRI, Set<Value>> getAllValues();

	public Set<Value> getValues( IRI prop );

	public void setProperty( IRI prop, Object propValue );

	public void setProperty( IRI prop, Value v, boolean add );

	public void setProperties( IRI prop, Collection<Value> vals );

	public void setLabel( IRI prop, String label );

	public String getLabel( IRI prop );

	public void setQueryId( String id );

	public String getQueryId();

	public void setFilter( IRI prop, String str );

	public String getFilter( IRI prop );
	
	public boolean hasFilter( IRI prop );
}
