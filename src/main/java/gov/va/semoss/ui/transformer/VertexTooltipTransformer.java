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
package gov.va.semoss.ui.transformer;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.ControlData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Transforms what is displayed on the tooltip when a vertex/node is selected on
 * a graph.
 */
public class VertexTooltipTransformer implements Transformer<SEMOSSVertex, String> {

	private static final Logger logger = Logger.getLogger( VertexTooltipTransformer.class );
	ControlData data;

	/**
	 * Constructor for VertexTooltipTransformer.
	 *
	 * @param data ControlData
	 */
	public VertexTooltipTransformer( ControlData data ) {
		this.data = data;
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
	public String transform( SEMOSSVertex vertex ) {
		StringBuilder propName = new StringBuilder();
		// don't show property type for these properties
		Set<URI> mains = new HashSet<>( Arrays.asList( RDFS.LABEL, RDF.TYPE, RDF.SUBJECT ) );

		List<URI> props = data.getSelectedPropertiesTT( vertex.getType() );
		if ( !props.isEmpty() ) {

			for ( URI prop : props ) {
				if ( vertex.hasProperty( prop ) ) {
					Object val = vertex.getProperty( prop );

					if ( 0 != propName.length() ) {
						propName.append( "<br>" );
					}

					if ( !mains.contains( prop ) ) {
						propName.append( data.getLabel( prop ) ).append( ": " );
					}

					String str = ( RDF.TYPE.equals( prop )
							? data.getLabel( URI.class.cast( val ) ) : val.toString() );

					propName.append( str );
				}
			}
		}
		//logger.debug("Prop Name " + propName);

		if ( 0 == propName.length() ) {
			return null;
		}

		String popup = "<html><body style=\"border:0px solid white; box-shadow:1px 1px 1px #000; padding:2px; background-color:white;\">"
				+ "<font size=\"3\" color=\"black\"><i>" + propName + "</i></font></body></html>";
		return popup;
	}
}
