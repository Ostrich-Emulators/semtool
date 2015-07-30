/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.NodeEdgeBase;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public interface QueryNodeEdgeBase extends NodeEdgeBase {

	public boolean isSelected( URI prop );

	public void setSelected( URI prop, boolean selected );

	public boolean isOptional( URI prop );

	public void setOptional( URI prop, boolean optional );

	public Map<URI, Set<Value>> getAllValues();

	public Set<Value> getValues( URI prop );

	public void setProperty( URI prop, Object propValue );

	public void setProperty( URI prop, Value v, boolean add );

	public void setProperties( URI prop, Collection<Value> vals );

	public void setLabel( URI prop, String label );

	public String getLabel( URI prop );

	public void setQueryId( String id );

	public String getQueryId();

	public void setFilter( URI prop, String str );

	public String getFilter( URI prop );
	
	public boolean hasFilter( URI prop );
}
