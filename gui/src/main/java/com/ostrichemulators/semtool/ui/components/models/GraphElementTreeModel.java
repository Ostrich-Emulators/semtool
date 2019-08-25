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
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public final class GraphElementTreeModel extends DefaultTreeModel {

	public static final IRI FETCHING = Utility.makeInternalIRI( "fetching" );
	private static final Logger log = Logger.getLogger( GraphElementTreeModel.class );
	private final IEngine engine;
	private final Set<IRI> fetched = new HashSet<>();
	private final List<IRI> concepts = new ArrayList<>();

	public GraphElementTreeModel( IEngine eng ) {
		super( new DefaultMutableTreeNode() );
		this.engine = eng;

		populateConcepts();
	}

	private void populateConcepts() {
		DefaultMutableTreeNode rootnode = DefaultMutableTreeNode.class.cast( getRoot() );
		StructureManager sm = StructureManagerFactory.getStructureManager( engine );

		concepts.addAll( sm.getTopLevelConcepts() );
		for ( IRI concept : concepts ) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode( concept );
			DefaultMutableTreeNode fetching = new DefaultMutableTreeNode( FETCHING );
			node.add( fetching );

			rootnode.add( node );
		}
	}

	public void populateInstances( DefaultMutableTreeNode parent ) {
		IRI IRI = IRI.class.cast( parent.getUserObject() );
		if ( fetched.contains( IRI ) ) {
			return;
		}

		// okay, fetch the instances
		fetched.add( IRI );

		new SwingWorker<Void, Void>() {
			private final List<IRI> instances = new ArrayList<>();

			@Override
			protected Void doInBackground() throws Exception {
				instances.addAll( NodeDerivationTools.createInstanceList( IRI, engine ) );
				return null;
			}

			@Override
			protected void done() {
				DefaultMutableTreeNode fetching = DefaultMutableTreeNode.class.cast( parent.getChildAt( 0 ) );
				parent.remove( fetching ); // this is the "fetching" label

				int i=0;
				DefaultMutableTreeNode[] children = new DefaultMutableTreeNode[instances.size()];
				int [] indexes = new int[children.length];

				for ( IRI u : instances ) {
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
