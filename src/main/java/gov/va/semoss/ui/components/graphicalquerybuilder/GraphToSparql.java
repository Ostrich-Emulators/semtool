/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * A class to convert a directed graph from the GQB to valid Sparql
 *
 * @author ryan
 */
public class GraphToSparql {

	private final Map<AbstractNodeEdgeBase, Integer> ids = new HashMap<>();
	private final Map<NodeType, Integer> valIds = new HashMap<>();
	private final static Map<URI, String> shortcuts = new HashMap<>();

	static {
		shortcuts.put( RDF.TYPE, "a" );
		shortcuts.put( RDFS.LABEL, "rdfs:label" );
	}

	public GraphToSparql() {
	}

	public String construct( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		assignIds( graph );
		// return convert( graph, false );
		throw new UnsupportedOperationException( "not yet implemented" );
	}

	public String select( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		assignIds( graph );
		return buildSelect( graph ) + buildWhere( graph );
	}

	private String buildSelect( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		StringBuilder select = new StringBuilder( "SELECT" );
		List<AbstractNodeEdgeBase> todo = new ArrayList<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		for ( AbstractNodeEdgeBase v : todo ) {
			for ( Map.Entry<URI, Object> en : v.getProperties().entrySet() ) {
				int objid = valIds.get( new NodeType( v.getURI(), en.getKey() ) );
				String objvar = "obj" + objid;

				if ( v.isMarked( en.getKey() ) ) {
					// special handling when the user wants a URI back
					select.append( " ?" ).append( en.getKey().equals( RDF.SUBJECT )
							? "node" + ids.get( v ) : objvar );
				}
			}
		}

		return select.toString();
	}

	private Map<URI, Object> getWhereProps( AbstractNodeEdgeBase v ) {
		Map<URI, Object> props = new HashMap<>( v.getProperties() );
		props.remove( RDF.SUBJECT );
		props.remove( Constants.IN_EDGE_CNT );
		props.remove( Constants.OUT_EDGE_CNT );
		props.remove( AbstractNodeEdgeBase.LEVEL );

		if ( Constants.ANYNODE.equals( v.getType() ) && !v.isMarked( RDF.TYPE ) ) {
			props.remove( RDF.TYPE );
		}

		return props;
	}

	private String buildWhere( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		StringBuilder sb = new StringBuilder( " WHERE  {\n" );
		for ( AbstractNodeEdgeBase v : graph.getVertices() ) {
			Map<URI, Object> props = getWhereProps( v );

			String nodevar = "?node" + ids.get( v );
			for ( Map.Entry<URI, Object> en : props.entrySet() ) {
				URI type = en.getKey();
				Object val = en.getValue();

				sb.append( "  " ).append( nodevar ).append( " " );
				if ( shortcuts.containsKey( type ) ) {
					sb.append( shortcuts.get( type ) );
				}
				else {
					sb.append( "<" ).append( type ).append( ">" );
				}
				sb.append( " " );

				if ( v.isMarked( type ) ) {
					int objid = valIds.get( new NodeType( v.getURI(), type ) );
					String objvar = "?obj" + objid;
					sb.append( objvar );
					if ( !( val.toString().isEmpty() || Constants.ANYNODE.equals( val ) ) ) {
						sb.append( ". FILTER( " ).append( objvar ).append( " = " );
					}
				}

				if ( !Constants.ANYNODE.equals( val ) ) {
					if ( val instanceof URI ) {
						sb.append( " <" ).append( val ).append( ">" );
					}
					else if ( val instanceof String && !val.toString().isEmpty() ) {
						sb.append( "\"" ).append( val ).append( "\"" );
					}
				}

				if ( v.isMarked( type )
						&& ( !( val.toString().isEmpty() || Constants.ANYNODE.equals( val ) ) ) ) {
					sb.append( ")" );
				}

				sb.append( ".\n" );
			}
		}

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			SEMOSSVertex src = graph.getSource( edge );
			SEMOSSVertex dst = graph.getDest( edge );

			String fromvar = "?node" + ids.get( src );
			String linkvar = "?link" + ids.get( edge );
			String tovar = "?node" + ids.get( dst );

			sb.append( "  " ).append( fromvar ).append( " " );
			if ( Constants.ANYNODE.equals( edge.getType() ) ) {
				sb.append( linkvar ).append( " " );
			}
			else {
				sb.append( "<" ).append( edge.getType() ).append( "> " );
			}
			sb.append( tovar ).append( " .\n" );
		}

		return sb.append( "}" ).toString();
	}

	private void assignIds( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		int nodecount = 0;
		int propcount = 0;

		List<AbstractNodeEdgeBase> todo = new ArrayList<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		for ( AbstractNodeEdgeBase v : todo ) {
			ids.put( v, nodecount++ );

			for ( Map.Entry<URI, Object> en : v.getProperties().entrySet() ) {
				valIds.put( new NodeType( v.getURI(), en.getKey() ), propcount++ );
			}
		}
	}

	private class NodeType {

		public final URI node;
		public final URI type;

		public NodeType( URI node, URI type ) {
			this.node = node;
			this.type = type;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 19 * hash + Objects.hashCode( this.node );
			hash = 19 * hash + Objects.hashCode( this.type );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final NodeType other = (NodeType) obj;
			if ( !Objects.equals( this.node, other.node ) ) {
				return false;
			}
			if ( !Objects.equals( this.type, other.type ) ) {
				return false;
			}
			return true;
		}

	}
}
