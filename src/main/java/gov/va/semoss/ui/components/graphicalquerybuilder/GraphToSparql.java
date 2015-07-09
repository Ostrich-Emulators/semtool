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
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * A class to convert a directed graph from the GQB to valid Sparql
 *
 * @author ryan
 */
public class GraphToSparql {

	private final Map<String, String> namespaces;

	public GraphToSparql() {
		this( new HashMap<>() );
	}

	public GraphToSparql( Map<String, String> ns ) {
		namespaces = new HashMap<>( ns );
	}

	public void setNamespaces( Map<String, String> ns ) {
		namespaces.clear();
		namespaces.putAll( ns );
	}

	public String construct( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		throw new UnsupportedOperationException( "not yet implemented" );
	}

	public String select( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config ) {
		return buildSelect( graph, config ) + buildWhere( graph, config );
	}

	private String buildSelect( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config ) {
		StringBuilder select = new StringBuilder( "SELECT" );
		List<AbstractNodeEdgeBase> todo = new ArrayList<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		for ( AbstractNodeEdgeBase v : todo ) {
			Map<URI, String> valIds = SparqlResultConfig.asMap( config.getNN( v ) );
			for ( Map.Entry<URI, Object> en : v.getProperties().entrySet() ) {
				if ( v.isMarked( en.getKey() ) ) {
					// special handling when the user wants a URI back
					select.append( " ?" ).append( valIds.get( en.getKey() ) );
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

	private String buildWhere( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config ) {
		StringBuilder sb = new StringBuilder( " WHERE  {\n" );
		for ( AbstractNodeEdgeBase v : graph.getVertices() ) {
			Map<URI, String> labelmap = SparqlResultConfig.asMap( config.getNN( v ) );
			Map<URI, Object> props = getWhereProps( v );

			String nodevar = "?" + labelmap.get( RDF.SUBJECT );
			for ( Map.Entry<URI, Object> en : props.entrySet() ) {
				URI type = en.getKey();
				Object val = en.getValue();

				// ignore empty variables (mostly, this is just for not returning a
				// label, but it's generally good not to add unmarked predicates to a 
				// query). If the predicate is marked, we must include it no matter what
				if ( "".equals( v.getProperty( type ).toString() ) && !v.isMarked( type ) ) {
					continue;
				}

				sb.append( "  " ).append( nodevar ).append( " " );
				sb.append( shortcut( type ) );
				sb.append( " " );

				if ( v.isMarked( type ) ) {
					String objvar = "?" + labelmap.get( type );
					sb.append( objvar );
					if ( !( val.toString().isEmpty() || Constants.ANYNODE.equals( val ) ) ) {
						sb.append( " VALUES " ).append( objvar ).append( " { " );
					}
				}

				if ( !Constants.ANYNODE.equals( val ) ) {
					if ( val instanceof URI ) {
						sb.append( shortcut( URI.class.cast( val ) ) );
					}
					else if ( val instanceof String && !val.toString().isEmpty() ) {
						sb.append( "\"" ).append( val ).append( "\"" );
					}
					else if ( val instanceof Double ) {
						sb.append( new LiteralImpl( val.toString(), XMLSchema.DOUBLE ) );
					}
					else if ( val instanceof Integer ) {
						sb.append( new LiteralImpl( val.toString(), XMLSchema.INTEGER ) );
					}
					else if ( val instanceof Boolean ) {
						sb.append( new LiteralImpl( val.toString(), XMLSchema.BOOLEAN ) );
					}
					else if ( val instanceof Date ) {
						sb.append( new ValueFactoryImpl().createLiteral( Date.class.cast( val ) ) );
					}
				}

				if ( v.isMarked( type )
						&& ( !( val.toString().isEmpty() || Constants.ANYNODE.equals( val ) ) ) ) {
					sb.append( "}" );
				}

				sb.append( " .\n" );
			}
		}

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			SEMOSSVertex src = graph.getSource( edge );
			SEMOSSVertex dst = graph.getDest( edge );

			Map<URI, String> edgemap = SparqlResultConfig.asMap( config.getNN( edge ) );
			Map<URI, String> srcmap = SparqlResultConfig.asMap( config.getNN( src ) );
			Map<URI, String> dstmap = SparqlResultConfig.asMap( config.getNN( dst ) );

			String fromvar = "?" + srcmap.get( RDF.SUBJECT );
			String linkvar = "?" + edgemap.get( RDF.SUBJECT );
			String tovar = "?" + dstmap.get( RDF.SUBJECT );

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

	private String shortcut( URI type ) {
		for ( Map.Entry<String, String> ns : namespaces.entrySet() ) {
			if ( type.getNamespace().equals( ns.getValue() ) ) {
				return ns.getKey() + ":" + type.getLocalName();
			}
		}

		return "<" + type + ">";
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
