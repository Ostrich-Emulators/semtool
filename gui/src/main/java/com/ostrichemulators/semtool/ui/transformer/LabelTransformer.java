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
import com.ostrichemulators.semtool.util.PropComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Transforms the property label on a node vertex in the graph.
 */
public class LabelTransformer<T extends GraphElement> extends SelectingTransformer<T, String> {

	// node type -> what properties to display
	private final MultiMap<URI, URI> displayables = new MultiMap<>();
	// don't show property type for these properties
	private final Set<URI> nolabels;
	private Map<Value, String> labelmap = new HashMap<>();
	private final Set<URI> defaults = new HashSet<>();

	/**
	 * Constructor for VertexLabelTransformer.
	 *
	 * @param data ControlData
	 */
	public LabelTransformer( MultiMap<URI, URI> data ) {
		this();
		displayables.putAll( data );
	}

	public LabelTransformer() {
		nolabels = new HashSet<>( Arrays.asList( RDFS.LABEL, RDF.TYPE, RDF.SUBJECT ) );
		defaults.addAll( nolabels );
	}

	public void setDefaultDisplayables( Collection<URI> propsToDisplay ) {
		defaults.clear();
		defaults.addAll( propsToDisplay );
	}

	public void setLabelCache( Map<Value, String> map ) {
		labelmap = map;
	}

	/**
	 * Sets what properties should be displayed for what node/edge types
	 *
	 * @param type
	 * @param propsToDisplay
	 */
	public void setDisplay( URI type, Collection<URI> propsToDisplay ) {
		displayables.remove( type );
		displayables.addAll( type, propsToDisplay );
	}

	public void setDisplay( URI type, URI prop, boolean showit ) {
		if ( showit ) {
			displayables.add( type, prop );
		}
		else {
			displayables.get( type ).remove( prop );
		}
	}

	public List<URI> getDisplayableProperties( URI type ) {
		List<URI> list = displayables.get( type );
		if ( null == list ) {
			displayables.addAll( type, defaults );
			list = new ArrayList<>( defaults );
		}
		Collections.sort( list, new PropComparator() );
		return list;
	}

	public String getLabel( URI prop ) {
		return labelmap.get( prop );
	}

	public boolean displayLabelFor( URI prop ) {
		return !nolabels.contains( prop );
	}

	public void setLabels( Map<URI, String> propToLabel ) {
		labelmap.putAll( propToLabel );
	}

	public void resetLabels( Map<URI, String> propToLabel ) {
		labelmap.clear();
		setLabels( propToLabel );
	}

	public void addLabel( URI uri, String label ) {
		labelmap.put( uri, label );
	}

	/**
	 * Method transform. Transforms the label on a node vertex in the graph
	 *
	 * @param vertex DBCMVertex - the vertex to be transformed
	 *
	 * @return String - the property name of the vertex
	 */
	public String getText( T vertex ) {
		List<URI> propertiesList = getDisplayableProperties( vertex.getType() );

		//uri required for uniqueness, need these font tags so that when you increase 
		//font through font transformer, the label doesn't get really far away from the vertex
		StringBuilder html = new StringBuilder();
		html.append( "<html><!--" ).append( vertex.getURI() ).append( "-->" );
		boolean first = true;
		for ( URI property : propertiesList ) {
			if ( !first ) {
				html.append( "<font size='1'><br></font>" );
			}

			if ( vertex.hasProperty( property ) ) {
				if ( displayLabelFor( property ) ) {
					String label = getLabel( property );
					html.append( label ).append( ": " );
				}

				Value val = vertex.getValue( property );
				String propval = ( RDF.TYPE.equals( property )
						? getLabel( URI.class.cast( val ) ) : val.stringValue() );
				html.append( chop( propval, 50 ) );
			}
			first = false;
		}

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
