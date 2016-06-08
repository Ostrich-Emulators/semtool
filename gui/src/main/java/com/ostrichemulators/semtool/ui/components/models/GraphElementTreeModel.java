/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManager;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManagerFactory;
import com.ostrichemulators.semtool.util.Utility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public final class GraphElementTreeModel extends DefaultTreeModel {

	public static final URI FETCHING = Utility.makeInternalUri( "fetching" );
	private static final Logger log = Logger.getLogger( GraphElementTreeModel.class );
	private final IEngine engine;
	private final Set<URI> fetched = new HashSet<>();
	private final List<URI> concepts = new ArrayList<>();

	public GraphElementTreeModel( IEngine eng ) {
		super( new DefaultMutableTreeNode() );
		this.engine = eng;

		populateConcepts();
	}

	private void populateConcepts() {
		DefaultMutableTreeNode rootnode = DefaultMutableTreeNode.class.cast( getRoot() );
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );

		concepts.addAll( sm.getTopLevelConcepts() );
		for ( URI concept : concepts ) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode( concept );
			DefaultMutableTreeNode fetching = new DefaultMutableTreeNode( FETCHING );
			node.add( fetching );

			rootnode.add( node );
		}
	}

	public void populateInstances( DefaultMutableTreeNode parent ) {
		URI uri = URI.class.cast( parent.getUserObject() );
		if ( fetched.contains( uri ) ) {
			return;
		}

		// okay, fetch the instances
		fetched.add( uri );

		new SwingWorker<Void, Void>() {
			private final List<URI> instances = new ArrayList<>();

			@Override
			protected Void doInBackground() throws Exception {
				instances.addAll( NodeDerivationTools.createInstanceList( uri, engine ) );
				return null;
			}

			@Override
			protected void done() {
				DefaultMutableTreeNode fetching = DefaultMutableTreeNode.class.cast( parent.getChildAt( 0 ) );
				parent.remove( fetching ); // this is the "fetching" label

				int i=0;
				DefaultMutableTreeNode[] children = new DefaultMutableTreeNode[instances.size()];
				int [] indexes = new int[children.length];

				for ( URI u : instances ) {
					children[i]=new DefaultMutableTreeNode( u );
					indexes[i]=i;
					parent.add( children[i++] );
				}
				GraphElementTreeModel.this.fireTreeNodesRemoved( GraphElementTreeModel.this,
						parent.getPath(), new int[]{ 0 }, new TreeNode[]{ fetching } );
				GraphElementTreeModel.this.fireTreeNodesInserted( GraphElementTreeModel.this,
						parent.getPath(), indexes, children );
			}
		}.execute();

	}
}
