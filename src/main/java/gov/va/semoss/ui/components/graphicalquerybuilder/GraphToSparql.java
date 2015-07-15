/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.util.Constants;
import java.util.ArrayList;
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

	public String select( DirectedGraph<QueryNode, QueryEdge> graph ) {
		return buildSelect( graph ) + buildWhere( graph );
	}

	private String buildSelect( DirectedGraph<QueryNode, QueryEdge> graph ) {
		StringBuilder select = new StringBuilder( "SELECT" );
		List<QueryNodeEdgeBase> todo = new ArrayList<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		for ( QueryNodeEdgeBase v : todo ) {
			for ( Map.Entry<URI, Set<Value>> en : v.getAllValues().entrySet() ) {
				URI prop = en.getKey();
				boolean issubj = RDF.SUBJECT.equals( prop );

				if ( v.isSelected( prop ) ) {
					select.append( " ?" ).
							append( issubj ? v.getQueryId() : v.getLabel( prop ) );
				}
			}
		}

		return select.toString();
	}

	private Set<URI> getWhereProps( QueryNodeEdgeBase v ) {
		Set<URI> props = new HashSet<>( v.getProperties().keySet() );
		props.remove( RDF.SUBJECT );

		if ( Constants.ANYNODE.equals( v.getValue( RDF.TYPE ) )
				&& !v.isSelected( RDF.TYPE ) ) {
			props.remove( RDF.TYPE );
		}

		if ( v.getLabel().isEmpty() && !v.isSelected( RDFS.LABEL ) ) {
			props.remove( RDFS.LABEL );
		}

		return props;
	}

	private String makeOneValue( Value v ) {
		if ( v instanceof URI ) {
			return shortcut( URI.class.cast( v ) );
		}

		return v.toString();
	}

	private String buildOneConstraint( QueryNodeEdgeBase v, URI type ) {
		StringBuilder sb = new StringBuilder();

		// ignore empty variables (mostly, this is just for not returning a
		// label, but it's generally good not to add unmarked predicates to a 
		// query). If the predicate is marked, we must include it no matter what
		Set<Value> realones = v.getValues( type );
		if ( ( null == realones || realones.isEmpty() ) && !v.isSelected( type ) ) {
			return "";
		}

		// ANYNODE and the empty string are just placeholder, 
		// but we don't need them anymore
		Set<Value> vals = new HashSet<>();
		if ( null != realones ) {
			vals.addAll( realones );
		}

		List<Value> toremove = new ArrayList<>();
		toremove.add( Constants.ANYNODE );
		for ( Value val : vals ) {
			if ( val.stringValue().isEmpty() ) {
				toremove.add( val );
			}
		}
		vals.removeAll( toremove );

		String nodevar = "?" + v.getQueryId();

		sb.append( "  " );
		if ( v.isOptional( type ) ) {
			sb.append( "OPTIONAL { " );
		}
		sb.append( nodevar ).append( " " );
		sb.append( shortcut( type ) );
		sb.append( " " );

		if ( v.isSelected( type ) ) {
			String objvar = "?" + v.getLabel( type );
			sb.append( objvar );

			if ( !vals.isEmpty() ) {
				sb.append( " VALUES " ).append( objvar ).append( " { " );
				for ( Value val : vals ) {
					sb.append( makeOneValue( val ) ).append( " " );
				}
				sb.append( "}" );
			}
		}
		else if ( !vals.isEmpty() ) {
			if ( vals.size() > 1 ) {
				String objvar = "?" + v.getLabel( type );
				sb.append( objvar );
				sb.append( " VALUES " ).append( objvar ).append( " { " );
			}
			for ( Value val : vals ) {
				sb.append( makeOneValue( val ) ).append( " " );
			}
			if ( vals.size() > 1 ) {
				sb.append( "}" );
			}
		}

		if ( v.isOptional( type ) ) {
			sb.append( " }" );
		}

		sb.append(
				" .\n" );

		return sb.toString();
	}

	private String buildWhere( DirectedGraph<QueryNode, QueryEdge> graph ) {
		StringBuilder sb = new StringBuilder( " WHERE  {\n" );
		for ( QueryNodeEdgeBase v : graph.getVertices() ) {
			Set<URI> props = getWhereProps( v );

			for ( URI prop : props ) {
				sb.append( buildOneConstraint( v, prop ) );
			}
		}

		for ( QueryEdge edge : graph.getEdges() ) {
			Set<URI> props = getWhereProps( edge );

			props.remove( RDF.TYPE );
			boolean useLinkVar = ( edge.isSelected( RDF.TYPE ) || !props.isEmpty()
					|| edge.getType().equals( Constants.ANYNODE ) );

			for ( URI prop : props ) {
				sb.append( buildOneConstraint( edge, prop ) );
			}

			QueryNode src = graph.getSource( edge );
			QueryNode dst = graph.getDest( edge );

			String fromvar = "?" + src.getQueryId();
			String linkvar = "?" + edge.getQueryId();
			String tovar = "?" + dst.getQueryId();

			sb.append( "  " ).append( fromvar ).append( " " );
			if ( useLinkVar ) {
				sb.append( linkvar ).append( " " );
			}
			else {
				sb.append( "<" ).append( edge.getType() ).append( "> " );
			}

			sb.append( tovar ).append( " " );

			if ( useLinkVar ) {
				if ( !edge.getType().equals( Constants.ANYNODE ) ) {
					sb.append( "BIND ( <" ).append( edge.getType() ).append( "> AS " ).
							append( linkvar ).append( " ) " );
				}

				if ( edge.isSelected( RDF.TYPE ) ) {
					sb.append( "\n  BIND ( " );
					if ( Constants.ANYNODE.equals( edge.getType() ) ) {
						sb.append( linkvar );
					}
					else {
						sb.append( "<" ).append( edge.getType() ).append( ">" );
					}
					sb.append( " AS ?" ).append( edge.getLabel( RDF.TYPE ) ).append( ") " );
				}

			}
			sb.append( ".\n" );
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
}
