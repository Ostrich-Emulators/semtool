/**
 * *****************************************************************************
 * Copyright 2015 MANTECH
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
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.io.IOException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.output.WriterOutputStream;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openrdf.model.Value;

/**
 * Creates a GraphML file from the given data
 */
public class GraphMLWriter implements GraphWriter {

	private static final Logger log = Logger.getLogger( GraphMLWriter.class );

	@Override
	public void write( ImportData data, File output ) throws IOException {
		output.mkdirs();
		try ( FileOutputStream fos = new FileOutputStream( output ) ) {
			write( data, fos );
		}
	}

	@Override
	public void write( ImportData data, OutputStream output ) throws IOException {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding( "UTF-8" );
		Element root = document.addElement( "graphml", "http://graphml.graphdrawing.org/xmlns" );
		root.addAttribute( QName.get( "schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance" ),
				"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" );

		Map<String, Element> propertyMap = new HashMap<>();
		Map<String, Element> nodeMap = new HashMap<>();
		Map<String, Element> relMap = new HashMap<>();

		addKey( root, "label", "all", "label", "string" );
		addKey( root, "type", "all", "type", "string" );

		for ( LoadingSheetData lsd : data.getSheets() ) {
			addPropKeys( lsd, root, propertyMap );
		}

		Element graph = root.addElement( "graph" )
				.addAttribute( "id", "G" ).addAttribute( "edgedefault", "directed" );

		for ( LoadingSheetData lsd : data.getNodes() ) {
			for ( LoadingNodeAndPropertyValues nap : lsd.getData() ) {
				addNode( graph, nap, nodeMap );
			}
		}

		for ( LoadingSheetData lsd : data.getRels() ) {
			for ( LoadingNodeAndPropertyValues nap : lsd.getData() ) {
				addRel( graph, nap, nodeMap, relMap );
			}
		}

		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter( output, format );
		writer.write( document );
	}

	public void write( ImportData data, Writer writer ) throws IOException {
		write( data, new WriterOutputStream( writer ) );
	}

	private static void addPropKeys( LoadingSheetData lsd, Element root,
			Map<String, Element> map ) {
		String sheetname = lsd.getName();
		Collection<String> props = lsd.getProperties();
		for ( String s : props ) {
			String name = sheetname + s;
			map.put( name, addKey( root, name, "node", s, "string" ) );
		}
	}

	private static String idify( String id ) {
		return id.replaceAll( "[\\s]+", "" );
	}

	private static Element addKey( Element root, String id, String _for,
			String name, String type ) {
		Element propkey = root.addElement( "key" );
		propkey.addAttribute( "id", idify( id ) )
				.addAttribute( "for", _for )
				.addAttribute( "attr.name", name )
				.addAttribute( "attr.type", type );
		return propkey;
	}

	private static Element addNode( Element root, LoadingNodeAndPropertyValues nap,
			Map<String, Element> nodemap ) {
		Element node = root.addElement( "node" );
		node.addAttribute( "id", idify( nap.getSubjectType() + nap.getSubject() ) );

		Element lbl = node.addElement( "data" );
		lbl.addAttribute( "key", "label" );
		lbl.setText( nap.getSubject() );

		Element type = node.addElement( "data" );
		type.addAttribute( "key", "type" );
		type.setText( nap.getSubjectType() );

		for ( Map.Entry<String, Value> en : nap.entrySet() ) {
			String propname = nap.getSheetName();
			Element prop = node.addElement( "data" );
			prop.addAttribute( "key", idify( propname + en.getKey() ) );
			prop.setText( en.getValue().stringValue() );
		}

		nodemap.put( nap.getSubjectType() + nap.getSubject(), node );

		return node;
	}

	private static Element addRel( Element root, LoadingNodeAndPropertyValues nap,
			Map<String, Element> nodemap, Map<String, Element> relmap ) {
		String name = nap.getSubjectType() + nap.getSubject() + nap.getRelname()
				+ nap.getObjectType() + nap.getObject();

		String sid = nap.getSubjectType() + nap.getSubject();
		String oid = nap.getObjectType() + nap.getObject();

		if ( !nodemap.containsKey( sid ) ) {
			LoadingSheetData lsd = LoadingSheetData.nodesheet( nap.getSubjectType() );
			addNode( root, lsd.add( nap.getSubject() ), nodemap );
		}
		if ( !nodemap.containsKey( oid ) ) {
			LoadingSheetData lsd = LoadingSheetData.nodesheet( nap.getObjectType() );
			addNode( root, lsd.add( nap.getObject() ), nodemap );
		}

		Element edge = root.addElement( "edge" );
		edge.addAttribute( "id", idify( name ) );
		edge.addAttribute( "source", idify( sid ) );
		edge.addAttribute( "target", idify( oid ) );

		Element lbl = edge.addElement( "data" );
		lbl.addAttribute( "key", "label" );
		lbl.setText( nap.getSubject() + " " + nap.getRelname() + " " + nap.getObject() );

		Element type = edge.addElement( "data" );
		type.addAttribute( "key", "type" );
		type.setText( nap.getRelname() );

		for ( Map.Entry<String, Value> en : nap.entrySet() ) {
			String propname = nap.getSheetName();
			Element prop = edge.addElement( "data" );
			prop.addAttribute( "key", idify( propname + en.getKey() ) );
			prop.setText( en.getValue().stringValue() );
		}

		nodemap.put( nap.getSubjectType() + nap.getSubject(), edge );

		relmap.put( name, edge );
		return edge;
	}
}
