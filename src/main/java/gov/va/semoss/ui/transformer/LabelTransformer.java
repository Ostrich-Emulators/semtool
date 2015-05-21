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

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.util.PropComparator;

import java.util.Collections;

import java.util.List;
import org.apache.commons.collections15.Transformer;
import org.openrdf.model.URI;

/**
 * Transforms the property label on a node vertex in the graph.
 */
public class LabelTransformer<T extends AbstractNodeEdgeBase> implements Transformer<T, String> {

	private ControlData data;

	/**
	 * Constructor for VertexLabelTransformer.
	 *
	 * @param data ControlData
	 */
	public LabelTransformer( ControlData data ) {
		this.data = data;
	}

	/**
	 * Method transform. Transforms the label on a node vertex in the graph
	 *
	 * @param vertex DBCMVertex - the vertex to be transformed
	 *
	 * @return String - the property name of the vertex
	 */
	@Override
	public String transform( AbstractNodeEdgeBase vertex ) {
		List<URI> properties = data.getSelectedProperties( vertex.getType() );
		if ( properties.isEmpty() ) {
			return "";
		}

		//order the props so the order is the same from run to run
		Collections.sort( properties, new PropComparator() );

		//uri required for uniqueness, need these font tags so that when you increase 
		//font through font transformer, the label doesn't get really far away from the vertex
		StringBuilder html = new StringBuilder();
		html.append( "<html><!--" ).append( vertex.getURI() ).append( "-->" );
		html.append( "<font size='1'>" );
		if ( vertex instanceof SEMOSSVertex ) {
			html.append( "<br><br><br>" ); // so the text goes under the node icon (?)
		}
		html.append( "</font>" );
		boolean first = true;
		for ( URI property : properties ) {
			if ( !first ) {
				html.append( "<font size='1'><br></font>" );
			}

			if ( vertex.hasProperty( property ) ) {
				html.append( vertex.getProperty( property ) );
			}
			first = false;
		}

		html.append( " lev: " ).append( vertex.getLevel() );

		html.append( "</html>" );

		return html.toString();
	}
}
