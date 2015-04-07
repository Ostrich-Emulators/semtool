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
package gov.va.semoss.ui.components.playsheets;

import java.util.HashSet;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Value;

/**
 * The Play Sheet for creating a Sankey diagram using nodes and relationships.
 */
public class SankeyPlaySheet extends BrowserPlaySheet2 {

	private static final Logger log = Logger.getLogger( SankeyPlaySheet.class );

	/**
	 * Constructor for SankeyPlaySheet.
	 */
	public SankeyPlaySheet() {
		super( "/html/RDFSemossCharts/app/sankey.html" );
	}

	@Override
	public void create( List<Value[]> newdata, List<String> headers ) {
		setHeaders( headers );
		convertUrisToLabels( newdata, getPlaySheetFrame().getEngine() );

		Set<Map<String, Object>> links = new HashSet<>();
		Set<Map<String, String>> nodes = new HashSet<>();
		String[] var = headers.toArray( new String[0] );

		for ( Value[] listElement : newdata ) {
			String src = listElement[0].stringValue();
			String tgt = listElement[1].stringValue();
			String valstr = listElement[2].stringValue();

			addTripleMaps( src, tgt, valstr, links, nodes );

			if ( var.length > 3 ) {
				for ( int j = 1; j < ( var.length - 2 ); j = j + 2 ) {
					String nsrc = listElement[j].stringValue();
					String ntgt = listElement[j + 2].stringValue();
					String nvalstr = listElement[j + 3].stringValue();

					addTripleMaps( nsrc, ntgt, nvalstr, links, nodes );
				}
			}
		}

		Map<String, Object> allHash = new HashMap<>();
		allHash.put( "nodes", nodes );
		allHash.put( "links", links );

		addDataHash( allHash );
		createView();
	}

	private void addTripleMaps( String src, String tgt, String valstr,
			Set<Map<String, Object>> links, Set<Map<String, String>> nodes ) {

		Map<String, Object> elementLinks = new LinkedHashMap<>();
		Map<String, String> elementSource = new LinkedHashMap<>();
		Map<String, String> elementTarget = new LinkedHashMap<>();

		elementLinks.put( "source", src );
		elementLinks.put( "target", tgt );
		elementSource.put( "name", src );
		elementTarget.put( "name", tgt );

		double val = ( valstr.isEmpty() ? 1 : Double.parseDouble( valstr ) );
		elementLinks.put( "value", val );

		links.add( elementLinks );
		nodes.add( elementSource );
		nodes.add( elementTarget );
	}
}
