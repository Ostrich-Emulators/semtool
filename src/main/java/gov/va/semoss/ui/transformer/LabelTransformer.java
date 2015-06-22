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
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.util.PropComparator;

import java.util.Collections;

import java.util.List;
import org.openrdf.model.URI;

/**
 * Transforms the property label on a node vertex in the graph.
 */
public class LabelTransformer<T extends AbstractNodeEdgeBase> extends SelectingTransformer<T, String> {

	private final ControlData data;

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
	public String getText( T vertex ) {
		List<URI> properties = data.getSelectedProperties( vertex );
		
		if ( properties.isEmpty() ) {
			return "";
		}

		//order the props so the order is the same from run to run
		Collections.sort( properties, new PropComparator() );

		//uri required for uniqueness, need these font tags so that when you increase 
		//font through font transformer, the label doesn't get really far away from the vertex
		StringBuilder html = new StringBuilder();
		html.append( "<html><!--" ).append( vertex.getURI() ).append( "-->" );
		boolean first = true;
		for ( URI property : properties ) {
			if ( !first ) {
				html.append( "<font size='1'><br></font>" );
			}

			if ( vertex.hasProperty( property ) ) {
				String propval = vertex.getProperty( property ).toString();
				html.append( chop( propval, 50 ) );
			}
			first = false;
		}

		// html.append( " lev: " ).append( vertex.getLevel() );
		html.append( "</html>" );

		return html.toString();
	}

	/**
	 * Chops a string to a smaller size. We try to break on a word boundary, even
	 * if that makes us go over <code>maxsize</code>
	 *
	 * @param longstring
	 * @param maxsize
	 * @return
	 */
	public static String chop( String longstring, int maxsize ) {
		if ( longstring.length() <= maxsize ) {
			return longstring;
		}

		StringBuilder newstring = new StringBuilder();
		for ( String word : longstring.split( "[\\s]+" ) ) {
			if ( newstring.length() < maxsize ) {
				if ( 0 != newstring.length() ) {
					newstring.append( " " );
				}
				newstring.append( word );
			}
			else {
				newstring.append( "..." );
				break;
			}
		}

		return newstring.toString();
	}

	/**
	 * Converts long text into a block of text about <code>maxsize</code>
	 * characters wide
	 *
	 * @param longstring
	 * @param maxsize
	 * @return
	 */
	public static String paragraph( String longstring, int maxsize ) {
		if ( longstring.length() <= maxsize ) {
			return longstring;
		}

		StringBuilder newstring = new StringBuilder();
		StringBuilder line = new StringBuilder();
		for ( String word : longstring.split( "[\\s]+" ) ) {
			if ( line.length() > maxsize ) {
				if ( 0 != newstring.length() ) {
					newstring.append( "<br>" );
				}

				newstring.append( line );
				line = new StringBuilder();
			}

			if ( 0 != line.length() ) {
				line.append( " " );
			}

			line.append( word );
		}
		newstring.append( "<br>" ).append( line );

		return newstring.toString();
	}

	@Override
	protected String transformNormal( T t ) {
		return getText( t );
	}

	@Override
	protected String transformSelected( T t ) {
		return getText( t );
	}

	@Override
	protected String transformNotSelected( T t, boolean inSkeletonMode ) {
		return ( inSkeletonMode ? "" : getText( t ) );
	}
}
