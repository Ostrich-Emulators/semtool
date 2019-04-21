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

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.util.MultiMap;
import java.util.List;

import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 * Transforms what is displayed on the tooltip when a vertex/node is selected on
 * a graph.
 */
public class TooltipTransformer<T extends GraphElement> extends LabelTransformer<T> {

	public TooltipTransformer( MultiMap<URI, URI> data ) {
		super( data );
	}

	public TooltipTransformer() {
	}

	@Override
	public String apply( GraphElement vertex ) {
		List<URI> propertiesList = super.getDisplayableProperties( vertex.getType() );

		String propertiesHTMLString = buildPropertyHTMLString( propertiesList, vertex );
		if ( 0 == propertiesHTMLString.length() ) {
			return null;
		}

		String popup = "<html><body style=\"border:0px solid white; box-shadow:1px 1px 1px #000; padding:2px; background-color:white;\">"
				+ "<font size=\"3\" color=\"black\"><i>" + propertiesHTMLString + "</i></font></body></html>";
		return popup;
	}

	private String buildPropertyHTMLString( List<URI> properties, GraphElement vertex ) {
		StringBuilder propertiesHTMLString = new StringBuilder();

		for ( URI prop : properties ) {
			if ( vertex.hasProperty( prop ) ) {
				Value val = vertex.getValue( prop );

				if ( 0 != propertiesHTMLString.length() ) {
					propertiesHTMLString.append( "<br>" );
				}

				if ( displayLabelFor( prop ) ) {
					propertiesHTMLString.append( getLabel( prop ) ).append( ": " );
				}

				String str = ( RDF.TYPE.equals( prop )
						? getLabel( URI.class.cast( val ) ) : val.stringValue() );

				propertiesHTMLString.append( str );
			}
		}

		return propertiesHTMLString.toString();
	}
}
