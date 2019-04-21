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
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;

import com.ostrichemulators.semtool.ui.transformer.LabelTransformer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.PropComparator;

import com.ostrichemulators.semtool.util.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 * Transforms the property label on a node vertex in the graph.
 * @param <T>
 */
public class GqbLabelTransformer<T extends QueryGraphElement> extends LabelTransformer<T> {

	private final Map<URI, String> labels = new HashMap<>();
	private final Comparator<URI> comparator
			= new PropComparator( RDF.SUBJECT, RDFS.LABEL, RDF.TYPE );
	private IEngine engine;

	/**
	 * Constructor for VertexLabelTransformer.
	 *
	 * @param engine
	 */
	public GqbLabelTransformer( IEngine engine ) {
		super();
		this.engine = engine;
	}

	public void setEngine( IEngine eng ) {
		labels.clear();
		labels.put( Constants.ANYNODE, "&lt;Any&gt;" );
		labels.put( RDF.SUBJECT, "Query ID" );
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
	public String getText( QueryGraphElement vertex ) {
		Map<URI, Set<Value>> properties = new HashMap<>( vertex.getAllValues() );
		properties.remove( RDF.SUBJECT );

		if ( properties.isEmpty() ) {
			return "";
		}

		// make sure we display the sparql id
		properties.put( RDF.SUBJECT,
				new HashSet<>( Arrays.asList( new LiteralImpl( vertex.getQueryId() ) ) ) );

		updateLabels( properties );

		StringBuilder html = new StringBuilder();
		html.append( "<html><!--" ).append( vertex.getIRI() ).append( "-->" );
		boolean first = true;

		List<URI> orderedProps = new ArrayList<>( properties.keySet() );
		Collections.sort( orderedProps, comparator );

		for ( URI property : orderedProps ) {
			Set<Value> values = properties.get( property );

			for ( Value value : values ) {
				String propval = ( null == value ? "" : value.stringValue() );
				if ( null == propval || propval.isEmpty() ) {
					propval = "&lt;Any&gt;";
				}
				if ( value instanceof URI ) {
					propval = labels.get( URI.class.cast( value ) );
				}

				if ( !first ) {
					html.append( "<font size='1'><br></font>" );
				}

				if ( vertex.hasProperty( property ) || RDF.SUBJECT.equals( property ) ) {
					if ( vertex.isSelected( property ) ) {
						html.append( "<b>" );
					}

					if ( property.equals( RDF.SUBJECT ) ) {
						// special handling for the query name...italics and no label part
						html.append( "<i>" ).append( chop( propval, 50 ) ).append( "</i>" );
					}
					else {
						html.append( labels.get( property ) ).append( ": " ).
								append( chop( propval, 50 ) );
					}

					if ( vertex.isSelected( property ) ) {
						html.append( "</b>" );
					}
				}
			}
			first = false;
		}

		// html.append( " lev: " ).append( vertex.getLevel() );
		html.append( "</html>" );

		return html.toString();
	}

	private void updateLabels( Map<URI, Set<Value>> properties ) {
		Set<URI> props = new HashSet<>( properties.keySet() );
		for ( Set<Value> os : properties.values() ) {
			for ( Value o : os ) {
				if ( o instanceof URI ) {
					props.add( URI.class.cast( o ) );
				}
			}
		}

		props.removeAll( labels.keySet() );
		if ( !( props.isEmpty() || null == engine ) ) {
			labels.putAll( Utility.getInstanceLabels( props, engine ) );
		}
	}
}
