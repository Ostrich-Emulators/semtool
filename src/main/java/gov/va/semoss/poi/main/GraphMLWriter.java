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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.io.IOException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.output.WriterOutputStream;
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
		Graph graph = getGraph( data );
		com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter writer
				= new com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter( graph );
		writer.outputGraph( output );
	}

	public void write( ImportData data, Writer writer ) throws IOException {
		write( data, new WriterOutputStream( writer ) );
	}

	public static Graph getGraph( ImportData data ) {
		Graph graph = new TinkerGraph();

		Map<String, Vertex> nodes = new HashMap<>();
		Map<String, Edge> edges = new HashMap<>();
		for ( LoadingSheetData lsd : data.getNodes() ) {
			for ( LoadingNodeAndPropertyValues nap : lsd.getData() ) {
				Vertex v = graph.addVertex( null );
				v.setProperty( "label", nap.getSubject() );
				v.setProperty( "type", nap.getSubjectType() );

				nodes.put( nap.getSubjectType() + nap.getSubject(), v );
				for ( Map.Entry<String, Value> en : nap.entrySet() ) {
					v.setProperty( en.getKey(), en.getValue().stringValue() );
				}
			}
		}

		for ( LoadingSheetData lsd : data.getRels() ) {
			for ( LoadingNodeAndPropertyValues nap : lsd.getData() ) {
				String sid = nap.getSubjectType() + nap.getSubject();
				String oid = nap.getObjectType() + nap.getObject();

				if ( !nodes.containsKey( sid ) ) {
					Vertex v = graph.addVertex( null );
					v.setProperty( "label", nap.getSubject() );
					v.setProperty( "type", nap.getSubjectType() );
					nodes.put( sid, v );
				}
				if ( !nodes.containsKey( oid ) ) {
					Vertex v = graph.addVertex( null );
					v.setProperty( "label", nap.getObject() );
					v.setProperty( "type", nap.getObjectType() );
					nodes.put( oid, v );
				}

				Edge edge = graph.addEdge( null, nodes.get( sid ), nodes.get( oid ),
						nap.getRelname() );
				edges.put( edgeid( nap ), edge );
				
				for ( Map.Entry<String, Value> en : nap.entrySet() ) {
					edge.setProperty( en.getKey(), en.getValue().stringValue() );
				}
			}
		}

		return graph;
	}

	private static String edgeid( LoadingNodeAndPropertyValues nap ) {
		return nap.getSubject() + " " + nap.getRelname() + " " + nap.getObject() + " ("
				+ nap.getSubjectType() + ":" + nap.getObjectType() + ")";
	}
}
