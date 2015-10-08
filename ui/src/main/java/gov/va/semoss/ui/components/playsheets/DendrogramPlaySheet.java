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

import gov.va.semoss.rdf.engine.api.IEngine;

import gov.va.semoss.util.TreeNode;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;

/**
 * The Play Sheet for creating a Dendrogram diagram using names and children.
 */
public class DendrogramPlaySheet extends BrowserPlaySheet2 {

	private static final long serialVersionUID = 3037305730873876699L;
	private static final Logger log = Logger.getLogger( DendrogramPlaySheet.class );

	/**
	 * Constructor for DendrogramPlaySheet.
	 */
	public DendrogramPlaySheet() {
		super( "/html/RDFSemossCharts/app/dendrogram.html" );
	}

	@Override
	public void create( List<Value[]> valdata, List<String> headers, IEngine engine ) {
		setHeaders( headers );
		List<String[]> stringdata = convertEverythingToStrings( valdata, engine );

		int row = 0;
		String newdata[][] = new String[stringdata.size()][getHeaders().size()];
		for ( String[] rowdata : stringdata ) {
			newdata[row++] = rowdata;
		}

		TreeNode<String> root = new TreeNode<>( "VA" );
		buildTree( root, newdata, 0, newdata.length, 0 );
		// printTree( root, 0 );
		addDataHash( buildAllHash( root ) );
		createView();
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}

	private static void printTree( TreeNode<String> root, int depth ) {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < depth; i++ ) {
			sb.append( "  " );
		}
		sb.append( root.getNode() );

		log.debug( sb );

		List<TreeNode<String>> children = root.getChildNodes();
		for ( TreeNode<String> child : children ) {
			printTree( child, depth + 1 );
		}
	}

	private static Map<String, Object> buildAllHash( TreeNode<String> root ) {
		Map<String, Object> hash = new LinkedHashMap<>();
		hash.put( "name", root.getNode() );

		if ( !root.getChildNodes().isEmpty() ) {
			List<Map<String, Object>> children = new ArrayList<>();
			hash.put( "children", children );

			for ( TreeNode<String> child : root.getChildNodes() ) {
				children.add( buildAllHash( child ) );
			}
		}

		return hash;
	}

	private static void buildTree( TreeNode<String> root, String[][] data,
			int startrow, int endrow, int col ) {

		if ( startrow > endrow || col >= data[0].length ) {
			return;
		}

		String lastval = data[startrow][col];
		int nodestart = startrow;
		int nodeend = endrow;
		for ( int row = startrow; row < endrow; row++ ) {
			String nodename = data[row][col];
			if ( !nodename.equals( lastval ) ) {
				TreeNode<String> child = root.addChild( lastval );
				nodeend = row;
				// recurse to fill in our children
				buildTree( child, data, nodestart, nodeend, col + 1 );

				lastval = nodename;
				nodestart = row;
				nodeend = endrow;
			}
		}

		// make our last child
		TreeNode<String> lastchild = root.addChild( lastval );
		buildTree( lastchild, data, nodestart, nodeend, col + 1 );
	}
}
