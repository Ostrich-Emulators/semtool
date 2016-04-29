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
package com.ostrichemulators.semtool.ui.transformer;

import com.google.common.base.Function;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.ui.components.ControlData;
import com.ostrichemulators.semtool.util.PropComparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Transforms what is displayed on the tooltip when a vertex/node is selected on
 * a graph.
 */
public class TooltipTransformer<T extends GraphElement> implements Function<T, String> {
	private final ControlData data;
	private final Set<URI> mains;
	
	/**
	 * Constructor for VertexTooltipTransformer.
	 *
	 * @param data ControlData
	 */
	public TooltipTransformer( ControlData data ) {
		this.data = data;
		
		// don't show property type for these properties
		mains = new HashSet<>( Arrays.asList( RDFS.LABEL, RDF.TYPE, RDF.SUBJECT ) );
	}

	/**
	 * Method transform. Get the DI Helper to find what is needed to get for
	 * vertex
	 *
	 * @param vertex DBCMVertex - The edge of which this returns the properties.
	 *
	 * @return String - The name of the property.
	 */
	@Override
	public String apply( GraphElement vertex ) {
		List<URI> propertiesList = data.getSelectedPropertiesTT( vertex.getType() );
		Collections.sort( propertiesList, new PropComparator() );

		String propertiesHTMLString = buildPropertyHTMLString(propertiesList, vertex);
		if ( 0 == propertiesHTMLString.length() ) {
			return null;
		}

		String popup = "<html><body style=\"border:0px solid white; box-shadow:1px 1px 1px #000; padding:2px; background-color:white;\">"
				+ "<font size=\"3\" color=\"black\"><i>" + propertiesHTMLString + "</i></font></body></html>";
		return popup;
	}
	
	private String buildPropertyHTMLString(List<URI> properties, GraphElement vertex) {
		StringBuilder propertiesHTMLString = new StringBuilder();
		
		for ( URI prop : properties ) {
			if ( vertex.hasProperty( prop ) ) {
				Object val = vertex.getProperty( prop );

				if ( 0 != propertiesHTMLString.length() ) {
					propertiesHTMLString.append( "<br>" );
				}

				if ( !mains.contains( prop ) ) {
					propertiesHTMLString.append( data.getLabel( prop ) ).append( ": " );
				}

				String str = ( RDF.TYPE.equals( prop )
						? data.getLabel( URI.class.cast( val ) ) : val.toString() );

				propertiesHTMLString.append( str );
			}
		}
		
		return propertiesHTMLString.toString();
	}
}
