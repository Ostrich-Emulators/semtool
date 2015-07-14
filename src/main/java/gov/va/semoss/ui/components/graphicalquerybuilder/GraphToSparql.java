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
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
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
				if ( !RDF.SUBJECT.equals( en.getKey() ) && v.isMarked( en.getKey() ) ) {
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
		props.remove( GraphicalQueryPanel.SPARQLNAME );

		if ( Constants.ANYNODE.equals( v.getType() ) && !v.isMarked( RDF.TYPE ) ) {
			props.remove( RDF.TYPE );
		}

		if ( v.getLabel().isEmpty() && !v.isMarked( RDFS.LABEL ) ) {
			props.remove( RDFS.LABEL );
		}

		return props;
	}

	private String buildOneConstraint( AbstractNodeEdgeBase v, URI type, Object val,
			MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config ) {
		StringBuilder sb = new StringBuilder();

		// ignore empty variables (mostly, this is just for not returning a
		// label, but it's generally good not to add unmarked predicates to a 
		// query). If the predicate is marked, we must include it no matter what
		Object o = v.getProperty( type );
		String valstr = ( null == o ? "" : o.toString() );

		if ( valstr.isEmpty() && !v.isMarked( type ) ) {
			return "";
		}

		Map<URI, String> labelmap = SparqlResultConfig.asMap( config.getNN( v ) );
		String nodevar = "?" + labelmap.get( GraphicalQueryPanel.SPARQLNAME );

		SparqlResultConfig cfg = SparqlResultConfig.getOne( config.getNN( v ), type );

		sb.append( "  " );
		if ( cfg.isOptional() ) {
			sb.append( "OPTIONAL { " );
		}
		sb.append( nodevar ).append( " " );
		sb.append( shortcut( type ) );
		sb.append( " " );

		if ( v.isMarked( type ) ) {
			String objvar = "?" + labelmap.get( type );
			sb.append( objvar );
			if ( !( valstr.isEmpty() || Constants.ANYNODE.equals( val ) ) ) {
				sb.append( " VALUES " ).append( objvar ).append( " { " );
			}
		}

		if ( !Constants.ANYNODE.equals( val ) ) {
			if ( val instanceof URI ) {
				sb.append( shortcut( URI.class.cast( val ) ) );
			}
			else if ( val instanceof String && !valstr.isEmpty() ) {
				sb.append( "\"" ).append( val ).append( "\"" );
			}
			else if ( val instanceof Double ) {
				sb.append( new LiteralImpl( valstr, XMLSchema.DOUBLE ) );
			}
			else if ( val instanceof Integer ) {
				sb.append( new LiteralImpl( valstr, XMLSchema.INTEGER ) );
			}
			else if ( val instanceof Boolean ) {
				sb.append( new LiteralImpl( valstr, XMLSchema.BOOLEAN ) );
			}
			else if ( val instanceof Date ) {
				sb.append( new ValueFactoryImpl().createLiteral( Date.class.cast( val ) ) );
			}
		}

		if ( v.isMarked( type )
				&& ( !( valstr.isEmpty() || Constants.ANYNODE.equals( val ) ) ) ) {
			sb.append( "}" );
		}

		if ( cfg.isOptional() ) {
			sb.append( " }" );
		}

		sb.append( " .\n" );

		return sb.toString();
	}

	private String buildWhere( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> config ) {
		StringBuilder sb = new StringBuilder( " WHERE  {\n" );
		for ( AbstractNodeEdgeBase v : graph.getVertices() ) {
			Map<URI, Object> props = getWhereProps( v );

			for ( Map.Entry<URI, Object> en : props.entrySet() ) {
				sb.append( buildOneConstraint( v, en.getKey(), en.getValue(), config ) );
			}
		}

		for ( SEMOSSEdge edge : graph.getEdges() ) {
			Map<URI, Object> props = getWhereProps( edge );
			props.remove( RDF.TYPE );
			boolean useLinkVar = ( edge.isMarked( RDF.TYPE ) || !props.isEmpty()
					|| edge.getType().equals( Constants.ANYNODE ) );

			for ( Map.Entry<URI, Object> en : props.entrySet() ) {
				sb.append( buildOneConstraint( edge, en.getKey(), en.getValue(), config ) );
			}

			SEMOSSVertex src = graph.getSource( edge );
			SEMOSSVertex dst = graph.getDest( edge );

			Map<URI, String> edgemap = SparqlResultConfig.asMap( config.getNN( edge ) );
			Map<URI, String> srcmap = SparqlResultConfig.asMap( config.getNN( src ) );
			Map<URI, String> dstmap = SparqlResultConfig.asMap( config.getNN( dst ) );

			String fromvar = "?" + srcmap.get( GraphicalQueryPanel.SPARQLNAME );
			String linkvar = "?" + edgemap.get( GraphicalQueryPanel.SPARQLNAME );
			String tovar = "?" + dstmap.get( GraphicalQueryPanel.SPARQLNAME );

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

				if ( edge.isMarked( RDF.TYPE ) ) {
					sb.append( "\n  BIND ( " );
					if ( Constants.ANYNODE.equals( edge.getType() ) ) {
						sb.append( linkvar );
					}
					else {
						sb.append( "<" ).append( edge.getType() ).append( ">" );
					}
					sb.append( " AS ?" ).append( edgemap.get( RDF.TYPE ) ).append( ") " );
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
