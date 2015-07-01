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
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * A class to convert a directed graph from the GQB to valid Sparql
 *
 * @author ryan
 */
public class GraphToSparql {

    private final Map<URI, Integer> ids = new HashMap<>();
    private final Map<NodeType, Integer> valIds = new HashMap<>();

    public GraphToSparql() {
    }

    public String construct(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
        assignIds(graph);
        // return convert( graph, false );
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String select(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
        assignIds(graph);
        return buildSelect(graph) + buildWhere(graph);
    }

    private String buildSelect(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
        StringBuilder select = new StringBuilder("SELECT");
        List<AbstractNodeEdgeBase> todo = new ArrayList<>();
        todo.addAll(graph.getVertices());
        todo.addAll(graph.getEdges());

        for (AbstractNodeEdgeBase v : todo) {
            for (Map.Entry<URI, Object> en : v.getProperties().entrySet()) {
                int objid = valIds.get(new NodeType(v));
                String objvar = "obj" + objid;

                if (v.isMarked(en.getKey())) {
                    select.append(" ?").append(objvar);
                }
            }
        }

        return select.toString();
    }

    private String buildWhere(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
        StringBuilder sb = new StringBuilder(" WHERE  {");
        for (AbstractNodeEdgeBase v : graph.getVertices()) {
            Map<URI, Object> props = new HashMap<>(v.getProperties());
            props.remove(RDF.SUBJECT);
            props.remove(Constants.IN_EDGE_CNT);
            props.remove(Constants.OUT_EDGE_CNT);
            props.remove(AbstractNodeEdgeBase.LEVEL);

            String nodevar = "?node" + ids.get(v.getURI());
            for (Map.Entry<URI, Object> en : props.entrySet()) {
                URI type = en.getKey();
                Object val = en.getValue();

                sb.append(nodevar);
                sb.append(" <").append(type).append("> ");

                if (v.isMarked(type)) {
                    int objid = valIds.get(new NodeType(v));
                    String objvar = "?obj" + objid;
                    sb.append(objvar);
                    if (!val.toString().isEmpty()) {
                        sb.append(". FILTER( ").append(objvar).append(" = ");
                    }
                }

                if (val instanceof URI) {
                    sb.append(" <").append(val).append(">");
                } else if (val instanceof String && !val.toString().isEmpty()) {
                    sb.append("\"").append(val).append("\"");
                }
                if (v.isMarked(type) && !val.toString().isEmpty()) {
                    sb.append(")");
                }

                sb.append(".\n");
            }
        }

        return sb.append("}").toString();
    }

    private void assignIds(DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph) {
        int nodecount = 0;
        int propcount = 0;
        for (SEMOSSVertex v : graph.getVertices()) {
            ids.put(v.getURI(), nodecount++);
            valIds.put(new NodeType(v.getURI(), v.getType()), propcount++);
        }

        for (SEMOSSEdge v : graph.getEdges()) {
            ids.put(v.getURI(), nodecount++);
            valIds.put(new NodeType(v.getURI(), v.getType()), propcount++);
        }
    }

    private class NodeType {

        public final URI node;
        public final URI type;

        public NodeType(URI node, URI type) {
            this.node = node;
            this.type = type;
        }

        public NodeType(AbstractNodeEdgeBase b) {
            this(b.getURI(), b.getType());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 19 * hash + Objects.hashCode(this.node);
            hash = 19 * hash + Objects.hashCode(this.type);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NodeType other = (NodeType) obj;
            if (!Objects.equals(this.node, other.node)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }

    }
}
