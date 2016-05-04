/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSEdgeImpl;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.GraphCondensePanel;
import com.ostrichemulators.semtool.ui.components.GraphCondensePanel.EdgePropertySource;
import com.ostrichemulators.semtool.ui.components.OperationsProgress;
import com.ostrichemulators.semtool.ui.components.PlayPane;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.util.MultiMap;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class CondenseGraph extends AbstractAction {

	private static final Logger log = Logger.getLogger( CondenseGraph.class );
	private final GraphPlaySheet gps;

	public CondenseGraph( GraphPlaySheet ps ) {
		super( "Condense Graph" );
		putValue( Action.SHORT_DESCRIPTION,
				"Condense the graph by removing intermediate edges" );
		gps = ps;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		GraphCondensePanel gcp = new GraphCondensePanel( gps );
		String options[] = { "OK", "Cancel" };

		int opt = JOptionPane.showOptionDialog( null, gcp, "Condense Graph",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0] );
		if ( 0 == opt ) {

			int[] nodecount = new int[1];
			ProgressTask pt = new ProgressTask( "Condensing Graph", new Runnable() {

				@Override
				public void run() {
					DirectedGraph<SEMOSSVertex, SEMOSSEdge> oldg = gps.getVisibleGraph();
					nodecount[0] = oldg.getVertexCount();

					DirectedGraph<SEMOSSVertex, SEMOSSEdge> newg
							= condense( oldg, gcp.getEdgeTypeToRemove(),
									gcp.getEdgeEndpointType(), gcp.getPropertySource() );
					gps.getGraphData().setGraph( newg );
					gps.updateGraph();
				}
			} ) {

				@Override
				public void done() {
					super.done();
					int count = nodecount[0] - gps.getVisibleGraph().getVertexCount();
					String msg = "Graph condensed: " + count + " Nodes removed";
					this.setLabel( msg );
					JOptionPane.showMessageDialog( gps, msg, "Condenser Results",
							JOptionPane.INFORMATION_MESSAGE );
				}
			};

			OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
		}
	}

	public static DirectedGraph<SEMOSSVertex, SEMOSSEdge>
			condense( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					URI toremove, URI endpoint, EdgePropertySource strat ) {

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> newgraph = new DirectedSparseGraph<>();

		MultiMap<SEMOSSVertex, CondenserTuple> triples
				= findNodesToCondense( graph, toremove, endpoint );
		Set<SEMOSSVertex> middles = triples.keySet();
		Set<SEMOSSEdge> edges = new HashSet<>();
		for ( Collection<CondenserTuple> tlist : triples.values() ) {
			for ( CondenserTuple tup : tlist ) {
				edges.add( tup.in );
				edges.add( tup.out );
			}
		}

		// copy all the edges and their endpoints that we don't want to condense
		for ( SEMOSSEdge edge : graph.getEdges() ) {
			if ( !edges.contains( edge ) ) {
				Pair<SEMOSSVertex> endpoints = graph.getEndpoints( edge );

				if ( !graph.containsVertex( endpoints.getFirst() ) ) {
					newgraph.addVertex( endpoints.getFirst() );
				}
				if ( !graph.containsVertex( endpoints.getSecond() ) ) {
					newgraph.addVertex( endpoints.getSecond() );
				}

				newgraph.addEdge( edge, endpoints.getFirst(), endpoints.getSecond() );
			}
		}

		// copy any nodes that don't have any edges
		// (and also that we don't want to condense)
		Set<SEMOSSVertex> islands = new HashSet<>( graph.getVertices() );
		islands.removeAll( middles );
		for ( SEMOSSVertex v : islands ) {
			if ( !newgraph.containsVertex( v ) ) {
				newgraph.addVertex( v );
			}
		}

		// finally, condense everything in our condenser map
		for ( Map.Entry<SEMOSSVertex, List<CondenserTuple>> en : triples.entrySet() ) {
			SEMOSSVertex middle = en.getKey();

			for ( CondenserTuple tup : en.getValue() ) {
				SEMOSSVertex from = graph.getSource( tup.in );
				SEMOSSVertex to =  graph.getDest( tup.out );

				SEMOSSEdge edge = new SEMOSSEdgeImpl( from, to, middle.getURI() );

				Map<URI, Value> props;
				switch ( strat ) {
					case NODE:
						props = middle.getValues();
						break;
					case INEDGE:
						props = tup.in.getValues();
						break;
					case OUTEDGE:
						props = tup.out.getValues();
						break;
					default:
						throw new IllegalArgumentException( "no edge property source provided!" );
				}

				for ( Map.Entry<URI, Value> prop : props.entrySet() ) {
					edge.setValue( prop.getKey(), prop.getValue() );
				}
				edge.setType( middle.getType() );

				newgraph.addEdge( edge, from, to, EdgeType.DIRECTED );
			}
		}

		return newgraph;
	}

	/**
	 * Gets nodes that have both in- and out- edges and are of the given type
	 *
	 * @param graph the graph to inspect
	 * @param type the node type that has the edges
	 * @return
	 */
	public static MultiMap<SEMOSSVertex, CondenserTuple>
			findNodesToCondense( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
					URI type, URI endpoint ) {
		MultiMap<SEMOSSVertex, CondenserTuple> removers = new MultiMap<>();
		for ( SEMOSSVertex middle : graph.getVertices() ) {
			if ( type.equals( middle.getType() ) ) {
				SEMOSSEdge upstream = getEdge( endpoint, middle, graph, true );
				SEMOSSEdge downstream = getEdge( endpoint, middle, graph, false );

				// FIXME: we might have multiple pairs of
				// endpoints through our middle, so loop
				//while ( !( null == upstream || null == downstream ) ) {
				if( !( null==upstream || null == downstream ) ){
					removers.add( middle, new CondenserTuple( upstream, downstream ) );
				}

				// upstream = getVertex( endpoint, middle, graph, true );
				//downstream = getVertex( endpoint, middle, graph, false );
				//}
			}
		}

		return removers;
	}

	private static SEMOSSEdge getEdge( URI endpoint, SEMOSSVertex middle,
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, boolean upstream ) {
		Collection<SEMOSSEdge> edges = ( upstream ? graph.getInEdges( middle )
				: graph.getOutEdges( middle ) );

		for ( SEMOSSEdge edge : edges ) {
			SEMOSSVertex opp = graph.getOpposite( middle, edge );
			if ( opp.getType().equals( endpoint ) ) {
				return edge;
			}
		}

		return null;
	}

	public static final class CondenserTuple {

		public final SEMOSSEdge in;
		public final SEMOSSEdge out;

		public CondenserTuple( SEMOSSEdge in, SEMOSSEdge out ) {
			this.in = in;
			this.out = out;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 31 * hash + Objects.hashCode( this.in );
			hash = 31 * hash + Objects.hashCode( this.out );
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
			final CondenserTuple other = (CondenserTuple) obj;
			if ( !Objects.equals( this.in, other.in ) ) {
				return false;
			}
			if ( !Objects.equals( this.out, other.out ) ) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "CondenserTriple{" + in + "=>" + out + '}';
		}
	}
}
