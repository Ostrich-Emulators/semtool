/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.DirectedGraph;
import com.ostrichemulators.semtool.ui.components.graphicalquerybuilder.GraphicalQueryPanel.QueryOrder;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.MultiSetMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

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

	public String select( DirectedGraph<QueryNode, QueryEdge> graph,
			List<QueryOrder> ordering ) {

		List<QueryGraphElement> todo = new ArrayList<>();
		todo.addAll( graph.getVertices() );
		todo.addAll( graph.getEdges() );

		Set<QueryOrder> linkedOrderSet = new LinkedHashSet<>( ordering );
		for ( QueryGraphElement v : todo ) {
			for ( IRI prop : v.getAllValues().keySet() ) {
				QueryOrder qo = new QueryOrder( v, prop );
				if ( !linkedOrderSet.contains( qo ) ) {
					linkedOrderSet.add( qo );
				}
			}
		}

		List<QueryOrder> fullOrdering = new ArrayList<>( linkedOrderSet );
		return buildSelect( fullOrdering ) + buildWhere( graph, fullOrdering );
	}

	private String buildSelect( List<QueryOrder> ordering ) {
		StringBuilder select = new StringBuilder( "SELECT" );

		boolean hasone = false;
		for ( QueryOrder qo : ordering ) {
			QueryGraphElement v = qo.base;
			IRI prop = qo.property;

			boolean issubj = RDF.SUBJECT.equals( prop );

			if ( v.isSelected( prop ) ) {
				select.append( " ?" ).
						append( issubj ? v.getQueryId() : v.getLabel( prop ) );
				hasone = true;
			}
		}

		if ( !hasone ) {
			select.append( " *" );
		}

		return select.toString();
	}

	private MultiSetMap<IRI, Value> getWhereProps( QueryGraphElement v ) {
		MultiSetMap<IRI, Value> nodeEdgeVals = MultiSetMap.deepCopy( v.getAllValues() );
		nodeEdgeVals.remove( RDF.SUBJECT );

		// ANYNODE and "" are placeholders, and we don't need to hold their places
		// anymore at this point, so remove them
		List<IRI> toremove = new ArrayList<>();
		for ( Map.Entry<IRI, Set<Value>> en : nodeEdgeVals.entrySet() ) {
			IRI prop = en.getKey();
			Set<Value> values = en.getValue();

			if ( v.isNode() ) {
				values.remove( Constants.ANYNODE );
			}

			for ( Value val : new ArrayList<>( values ) ) {
				if ( val.stringValue().isEmpty() ) {
					values.remove( val );
				}
			}

			if ( values.isEmpty() && !v.isSelected( prop ) ) {
				toremove.add( prop );
			}
		}

		for ( IRI remover : toremove ) {
			nodeEdgeVals.remove( remover );
		}

		return nodeEdgeVals;
	}

	private String makeOneValue( Value v ) {
		if ( v instanceof IRI ) {
			return shortcut( IRI.class.cast( v ) );
		}

		Literal lit = Literal.class.cast( v );
		StringBuilder sb = new StringBuilder().append( '"' );
		sb.append( lit.getLabel() );
		sb.append( '"' );

		URI dt = lit.getDatatype();
		if ( null != dt ) {
			sb.append( "^^" );

			boolean found = false;
			for ( Map.Entry<String, String> ns : namespaces.entrySet() ) {
				if ( dt.getNamespace().equals( ns.getValue() ) ) {
					sb.append( ns.getKey() ).append( ":" ).append( dt.getLocalName() );
					found = true;
				}
			}

			if ( !found ) {
				sb.append( '<' ).append( dt.toString() ).append( '>' );
			}
		}

		return sb.toString();
	}

	private String makeFilter( QueryGraphElement nodeedge, IRI type, String nodevar,
			String filterVal ) {
		StringBuilder sb = new StringBuilder( "FILTER ( " );
		sb.append( filterVal );
		sb.append( " )" );
		return sb.toString();
	}

	private String buildOneConstraint( QueryGraphElement v, IRI type,
			Set<Value> vals ) {
		StringBuilder sb = new StringBuilder();

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

			if ( v.hasFilter( type ) ) {
				sb.append( " " );
				sb.append( makeFilter( v, type, nodevar, v.getFilter( type ) ) );
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

	private String buildWhere( DirectedGraph<QueryNode, QueryEdge> graph,
			List<QueryOrder> ordering ) {

		StringBuilder sb = new StringBuilder( " WHERE  {\n" );
		for ( QueryOrder qo : ordering ) {
			QueryGraphElement v = qo.base;
			IRI prop = qo.property;
			MultiSetMap<IRI, Value> props = getWhereProps( v );

			if ( !props.containsKey( qo.property ) ) {
				continue;
			}

			if ( v.isNode() ) {
				sb.append( buildOneConstraint( v, prop, props.get( prop ) ) );
			}
			else {
				QueryEdge edge = QueryEdge.class.cast( v );

				Set<Value> edgevals = props.get( prop );
				if ( RDF.TYPE.equals( prop ) ) {
					// handle endpoints and types a little differently
					QueryNode src = graph.getSource( edge );
					QueryNode dst = graph.getDest( edge );
					if ( !( null == src || null == dst ) ) {
						sb.append( buildEdgeTypeAndEndpoints( edge, props.getNN( RDF.TYPE ),
								src, dst, props.keySet() ) );
					}
					else {
						Logger.getLogger( getClass() ).warn( "BUG: how did we get here?" );
					}
				}
				else {
					sb.append( buildOneConstraint( edge, prop, edgevals ) );
				}
			}
		}

		return sb.append( "}" ).toString();
	}

	private String buildEdgeTypeAndEndpoints( QueryGraphElement edge,
			Set<Value> tvals, QueryNode src, QueryNode dst, Set<IRI> otherprops ) {
		String fromvar = "?" + src.getQueryId();
		String linkvar = "?" + edge.getQueryId();
		String tovar = "?" + dst.getQueryId();

		StringBuilder sb = new StringBuilder( "  " ).append( fromvar ).append( " " );

		// if we have a non-generic edge between these two nodes, the sparql changes
		Set<IRI> specialprops = new HashSet<>( otherprops );
		specialprops.removeAll( Arrays.asList( RDF.TYPE, RDFS.LABEL ) );
		boolean useCustomEdge = !specialprops.isEmpty();

		// make sure we don't treat a generic edge as a special VALUES clause
		Set<Value> vals = new HashSet<>( tvals );
		vals.remove( Constants.ANYNODE );

		// we need to use a variable if:
		// 1) our edge is selected to be returned in the SELECT part
		// 2) we have other properties to hang on this edge
		// Note: if 1 & 2 aren't true, we can handle multiple values without a variable
		boolean useLinkVar = ( edge.isSelected( RDF.TYPE ) || otherprops.size() > 1
				|| vals.isEmpty() || edge.isSelected( RDF.SUBJECT ) || useCustomEdge );
		if ( useLinkVar ) {
			sb.append( linkvar ).append( " " );
		}
		else {
			// our edge isn't selected, and we don't have to use a variable
			boolean first = true;
			for ( Value v : vals ) {
				if ( first ) {
					first = false;
				}
				else {
					sb.append( "| " );
				}

				sb.append( shortcut( IRI.class.cast( v ) ) ).append( " " );
			}
		}
		sb.append( tovar );

		if ( useLinkVar && !vals.isEmpty() ) {
			if ( useCustomEdge ) {
				sb.append( " .\n  " ).append( linkvar ).append( " " );
				sb.append( shortcut( RDF.PREDICATE ) ).append( " ?" );
				sb.append( edge.getLabel( RDF.TYPE ) );

				// our VALUES clause (below) needs to work on our base edge type,
				// not the custom one
				linkvar = "?" + edge.getLabel( RDF.TYPE );
			}

			sb.append( " VALUES " ).append( linkvar ).append( " { " );
			for ( Value v : vals ) {
				sb.append( shortcut( IRI.class.cast( v ) ) ).append( " " );
			}
			sb.append( "}" );
		}

		sb.append( " .\n" );

		if ( edge.isSelected( RDF.TYPE ) && !useCustomEdge ) {
			sb.append( "  " ).append( linkvar ).append( " " ).
					append( shortcut( RDF.TYPE ) ).append( "+ ?" ).
					append( edge.getLabel( RDF.TYPE ) ).append( " .\n" );
		}

		return sb.toString();
	}

	private String shortcut( IRI type ) {
		if ( null == type ) {
			return null;
		}

		for ( Map.Entry<String, String> ns : namespaces.entrySet() ) {
			if ( type.getNamespace().equals( ns.getValue() ) ) {
				return ns.getKey() + ":" + type.getLocalName();
			}
		}

		return "<" + type + ">";
	}
}
