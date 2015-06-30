/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.rdf.engine.api.IEngine;

import gov.va.semoss.ui.transformer.LabelTransformer;
import gov.va.semoss.util.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Transforms the property label on a node vertex in the graph.
 */
public class GqbLabelTransformer<T extends AbstractNodeEdgeBase> extends LabelTransformer<T> {

	private Map<URI, String> labels = new HashMap<>();
	private IEngine engine;

	/**
	 * Constructor for VertexLabelTransformer.
	 *
	 * @param data ControlData
	 */
	public GqbLabelTransformer( IEngine engine ) {
		super( null );
		this.engine = engine;
	}

	public void setEngine( IEngine eng ) {
		labels.clear();
		this.engine = eng;
	}

	/**
	 * Method transform. Transforms the label on a node vertex in the graph
	 *
	 * @param vertex DBCMVertex - the vertex to be transformed
	 *
	 * @return String - the property name of the vertex
	 */
	@Override
	public String getText( T vertex ) {
		Map<URI, Object> properties = vertex.getProperties();

		if ( properties.isEmpty() ) {
			return "";
		}

		updateLabels( properties );

		//uri required for uniqueness, need these font tags so that when you increase 
		//font through font transformer, the label doesn't get really far away from the vertex
		StringBuilder html = new StringBuilder();
		html.append( "<html><!--" ).append( vertex.getURI() ).append( "-->" );
		boolean first = true;
		for ( Map.Entry<URI, Object> en : properties.entrySet() ) {
			URI property = en.getKey();
			Object val = en.getValue();
			
			if ( RDF.SUBJECT.equals( property )
					|| AbstractNodeEdgeBase.LEVEL.equals( property ) ) {
				continue;
			}

			if( RDFS.LABEL.equals( property ) && val.toString().isEmpty() ){
				val = "&lt;Any&gt;";
			}
			if ( val instanceof URI ) {
				val = labels.get( URI.class.cast( val ) );
			}

			if ( !first ) {
				html.append( "<font size='1'><br></font>" );
			}

			if ( vertex.hasProperty( property ) ) {
				String propval = val.toString();
				html.append( labels.get( property ) ).append( ": " ).
						append( chop( propval, 50 ) );
			}
			first = false;
		}

		// html.append( " lev: " ).append( vertex.getLevel() );
		html.append( "</html>" );

		return html.toString();
	}

	private void updateLabels( Map<URI, Object> properties ) {
		Set<URI> props = new HashSet<>( properties.keySet() );
		for ( Object o : properties.values() ) {
			if ( o instanceof URI ) {
				props.add( URI.class.cast( o ) );
			}
		}

		props.removeAll( labels.keySet() );
		if ( !( props.isEmpty() || null == engine ) ) {
			labels.putAll( Utility.getInstanceLabels( props, engine ) );
			labels = Utility.sortUrisByLabel( labels );
		}
	}
}
