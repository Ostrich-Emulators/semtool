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
import java.awt.Color;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class TreeGraphDataModel extends GraphDataModel implements PropertyChangeListener {

	private static final Logger log = Logger.getLogger( TreeGraphDataModel.class );

	private final MultiSetMap<URI, URI> dupesets = new MultiSetMap();
	private final Map<URI, SEMOSSVertex> uriToNode = new HashMap<>();
	private final Map<URI, SEMOSSEdge> uriToEdge = new HashMap<>();
	private final Map<SEMOSSVertex, SEMOSSVertex> dupeNodeToTrue = new HashMap<>();
	private final Map<SEMOSSEdge, SEMOSSEdge> dupeEdgeToTrue = new HashMap<>();

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
		// if we're looking at a duplicate, first figure out the true node
		// if we're looking at the true node, just get the duplicates from dupesets.
		URI realuri = ( dupeNodeToTrue.containsKey( v )
				? dupeNodeToTrue.get( v ).getURI() : v.getURI() );

		Set<SEMOSSVertex> dupes = new HashSet<>();
		Set<URI> uris = dupesets.getNN( realuri );
		for ( URI u : uris ) {
			dupes.add( uriToNode.get( u ) );
		}

		return dupes;
	}

	public Set<SEMOSSEdge> getDuplicatesOf( SEMOSSEdge v ) {
		// if we're looking at a duplicate, first figure out the true node
		// if we're looking at the true node, just get the duplicates from dupesets.
		URI realuri = ( dupeEdgeToTrue.containsKey( v )
				? dupeEdgeToTrue.get( v ).getURI() : v.getURI() );

		Set<SEMOSSEdge> dupes = new HashSet<>();
		Set<URI> uris = dupesets.getNN( realuri );
		for ( URI u : uris ) {
			dupes.add( uriToEdge.get( u ) );
		}

		return dupes;
	}

	public SEMOSSVertex getRealVertex( SEMOSSVertex v ) {
		return dupeNodeToTrue.get( v );
	}

	public SEMOSSEdge getRealEdge( SEMOSSEdge e ) {
		return dupeEdgeToTrue.get( e );
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
				if ( !dupeVlkp.containsKey( child ) ) {
					dupeVlkp.put( child, duplicate( child ) );
				}

				SEMOSSEdge edge = tree.getParentEdge( child );
				SEMOSSVertex parent = tree.getParent( child );

				SEMOSSVertex dp = dupeVlkp.get( parent );
				SEMOSSVertex dc = dupeVlkp.get( child );
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
		uriToNode.put( c2.getURI(), c2 );
		dupeNodeToTrue.put( c2, old );

		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		old.addPropertyChangeListener( this );

		return c2;
	}

	private SEMOSSEdge duplicate( SEMOSSEdge old, SEMOSSVertex src,
			SEMOSSVertex dst ) {
		SEMOSSEdge c2 = new SEMOSSEdgeImpl( src, dst, old.getURI() );
		dupesets.add( old.getURI(), c2.getURI() );
		uriToEdge.put( c2.getURI(), c2 );
		dupeEdgeToTrue.put( c2, old );

		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}

		old.addPropertyChangeListener( this );

		return c2;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt ) {
		// propagate changes from the "real" vertices to our duplicate vertices
		Object src = evt.getSource();
		String propname = evt.getPropertyName();
		Object newval = evt.getNewValue();

		if ( src instanceof SEMOSSVertex ) {
			for ( SEMOSSVertex v : this.getDuplicatesOf( SEMOSSVertex.class.cast( src ) ) ) {
				if ( null != propname ) switch ( propname ) {
					case AbstractGraphElement.CHANGE_COLOR:
						v.setColor( Color.class.cast( newval ) );
						break;
					case AbstractGraphElement.CHANGE_VISIBLE:
						v.setVisible( Boolean.class.cast( newval ) );
						break;
					case SEMOSSVertex.CHANGE_SHAPE:
						v.setShape( Shape.class.cast( newval ) );
						break;
					default:
						v.setValue( new URIImpl( propname ), Value.class.cast( newval ) );
						break;
				}
			}
		}
		else if ( src instanceof SEMOSSEdge ) {
			for ( SEMOSSEdge v : this.getDuplicatesOf( SEMOSSEdge.class.cast( src ) ) ) {
				if ( null != propname ) switch ( propname ) {
					case AbstractGraphElement.CHANGE_COLOR:
						v.setColor( Color.class.cast( newval ) );
						break;
					case AbstractGraphElement.CHANGE_VISIBLE:
						v.setVisible( Boolean.class.cast( newval ) );
						break;
					default:
						v.setValue( new URIImpl( propname ), Value.class.cast( newval ) );
						break;
				}
			}
		}
	}
}
