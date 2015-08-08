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
import java.util.Map;
import org.openrdf.model.Value;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class TreeGraphDataModel extends GraphDataModel {

	private static final Logger log = Logger.getLogger( TreeGraphDataModel.class );

	protected MultiSetMap<URI, URI> nodeDupeLkp = new MultiSetMap<>();
	protected MultiSetMap<URI, URI> edgeDupeLkp = new MultiSetMap<>();

	public TreeGraphDataModel() {
		super( new DelegateForest<>() );
	}

	public TreeGraphDataModel( Forest<DuplicateVertex, DuplicateEdge> graph ) {
		super( DirectedGraph.class.cast( graph ) );
	}

	public TreeGraphDataModel( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots ) {
		setGraph( ( DirectedGraph.class.cast( makeDuplicateForest( graph, roots,
				new GraphToTreeConverter() ) ) ) );
	}

	public Forest<DuplicateVertex, DuplicateEdge> getForest() {
		return Forest.class.cast( getGraph() );
	}

	@Override
	protected SEMOSSEdge createEdge( SEMOSSVertex src, SEMOSSVertex dst, URI uri ) {
		DuplicateEdge de = new DuplicateEdge( src, dst,
				UriBuilder.getBuilder( uri.getNamespace() ).uniqueUri() );
		de.setRealUri( uri );
		return de;
	}

	@Override
	protected SEMOSSVertex createVertex( URI uri ) {
		DuplicateVertex dv
				= new DuplicateVertex( UriBuilder.getBuilder( uri.getNamespace() ).uniqueUri() );
		dv.setRealUri( uri );
		return dv;
	}

	private static DirectedGraph<DuplicateVertex, DuplicateEdge> makeDuplicateForest(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots,
			GraphToTreeConverter gttc ) {

		DelegateForest<DuplicateVertex, DuplicateEdge> newforest = new DelegateForest<>();
		for ( SEMOSSVertex root : roots ) {
			Tree<SEMOSSVertex, SEMOSSEdge> tree = gttc.convert( graph, root );

			Map<SEMOSSVertex, DuplicateVertex> dupeVlkp = new HashMap<>();
			Map<SEMOSSEdge, DuplicateEdge> dupeElkp = new HashMap<>();

			DelegateTree<DuplicateVertex, DuplicateEdge> dupetree = new DelegateTree<>();

			dupeVlkp.put( root, duplicate( root ) );
			dupetree.setRoot( dupeVlkp.get( root ) );

			Deque<SEMOSSVertex> todo = new ArrayDeque<>( tree.getChildren( root ) );
			while ( !todo.isEmpty() ) {
				SEMOSSVertex child = todo.poll();
				dupeVlkp.put( child, duplicate( child ) );

				SEMOSSEdge edge = tree.getParentEdge( child );
				SEMOSSVertex parent = tree.getParent( child );

				DuplicateVertex dp = dupeVlkp.get( parent );
				DuplicateVertex dc = duplicate( child );
				DuplicateEdge de = duplicate( edge, dp, dc );

				dupetree.addChild( de, dp, dc );
				dupeVlkp.put( child, dc );
				dupeElkp.put( edge, de );

				todo.addAll( tree.getChildren( child ) );
			}

			newforest.addTree( dupetree );
		}

		return newforest;
	}

	private static DuplicateVertex duplicate( SEMOSSVertex old ) {
		URI uri = UriBuilder.getBuilder( old.getURI().getNamespace() ).uniqueUri();
		DuplicateVertex c2 = new DuplicateVertex( uri, old.getType(), old.getLabel() );
		c2.setRealUri( old.getURI() );

		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		return c2;
	}

	private static DuplicateEdge duplicate( SEMOSSEdge old, DuplicateVertex src,
			DuplicateVertex dst ) {
		DuplicateEdge c2 = new DuplicateEdge( src, dst, old.getURI() );
		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		return c2;
	}

}
