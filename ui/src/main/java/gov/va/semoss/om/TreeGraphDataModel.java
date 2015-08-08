package gov.va.semoss.om;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import gov.va.semoss.ui.components.GraphToTreeConverter;
import gov.va.semoss.util.MultiSetMap;
import gov.va.semoss.util.UriBuilder;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Value;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class TreeGraphDataModel extends GraphDataModel {

	private static final Logger log = Logger.getLogger( TreeGraphDataModel.class );

	private final MultiSetMap<URI, URI> dupesets = new MultiSetMap();
	private final Map<URI, SEMOSSVertex> uriToNode = new HashMap<>();
	private final Map<URI, SEMOSSEdge> uriToEdge = new HashMap<>();

	public TreeGraphDataModel() {
		super( new DelegateForest<>() );
	}

	public TreeGraphDataModel( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots ) {
		setGraph( ( DirectedGraph.class.cast( makeDuplicateForest( graph, roots,
				new GraphToTreeConverter() ) ) ) );
	}

	public Forest<SEMOSSVertex, SEMOSSEdge> getForest() {
		return Forest.class.cast( getGraph() );
	}

	public Set<SEMOSSVertex> getDuplicatesOf( SEMOSSVertex v ) {
		Set<SEMOSSVertex> dupes = new HashSet<>();
		Set<URI> uris = dupesets.getNN( v.getURI() );
		for ( URI u : uris ) {
			dupes.add( uriToNode.get( u ) );
		}

		return dupes;
	}

	@Override
	protected SEMOSSEdge createEdge( SEMOSSVertex src, SEMOSSVertex dst, URI uri ) {
		SEMOSSEdge de = new SEMOSSEdgeImpl( src, dst,
				UriBuilder.getBuilder( uri.getNamespace() ).uniqueUri() );
		// FIXME: need to keep track of the real URI and this guy's URI
		return de;
	}

	@Override
	protected SEMOSSVertex createVertex( URI uri ) {
		SEMOSSVertex dv
				= new SEMOSSVertexImpl( UriBuilder.getBuilder( uri.getNamespace() ).uniqueUri() );
		// FIXME: need to keep track of the real URI and this guy's URI
		return dv;
	}

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> makeDuplicateForest(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots,
			GraphToTreeConverter gttc ) {

		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();
		for ( SEMOSSVertex root : roots ) {
			Tree<SEMOSSVertex, SEMOSSEdge> tree = gttc.convert( graph, root );

			Map<SEMOSSVertex, SEMOSSVertex> dupeVlkp = new HashMap<>();
			Map<SEMOSSEdge, SEMOSSEdge> dupeElkp = new HashMap<>();

			DelegateTree<SEMOSSVertex, SEMOSSEdge> dupetree = new DelegateTree<>();

			dupeVlkp.put( root, duplicate( root ) );
			dupetree.setRoot( dupeVlkp.get( root ) );

			Deque<SEMOSSVertex> todo = new ArrayDeque<>( tree.getChildren( root ) );
			while ( !todo.isEmpty() ) {
				SEMOSSVertex child = todo.poll();
				dupeVlkp.put( child, duplicate( child ) );

				SEMOSSEdge edge = tree.getParentEdge( child );
				SEMOSSVertex parent = tree.getParent( child );

				SEMOSSVertex dp = dupeVlkp.get( parent );
				SEMOSSVertex dc = duplicate( child );
				SEMOSSEdge de = duplicate( edge, dp, dc );

				dupetree.addChild( de, dp, dc );
				dupeVlkp.put( child, dc );
				dupeElkp.put( edge, de );

				todo.addAll( tree.getChildren( child ) );
			}

			newforest.addTree( dupetree );
		}

		return newforest;
	}

	private SEMOSSVertex duplicate( SEMOSSVertex old ) {
		URI uri = UriBuilder.getBuilder( old.getURI().getNamespace() ).uniqueUri();
		SEMOSSVertex c2 = new SEMOSSVertexImpl( uri, old.getType(), old.getLabel() );
		dupesets.add( old.getURI(), c2.getURI() );
		dupesets.add( c2.getURI(), old.getURI() );
		uriToNode.put( c2.getURI(), c2 );
		uriToNode.put( old.getURI(), old );

		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		return c2;
	}

	private SEMOSSEdge duplicate( SEMOSSEdge old, SEMOSSVertex src,
			SEMOSSVertex dst ) {
		SEMOSSEdge c2 = new SEMOSSEdgeImpl( src, dst, old.getURI() );
		dupesets.add( old.getURI(), c2.getURI() );
		dupesets.add( c2.getURI(), old.getURI() );
		uriToEdge.put( c2.getURI(), c2 );
		uriToEdge.put( old.getURI(), old );

		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		return c2;
	}
}
