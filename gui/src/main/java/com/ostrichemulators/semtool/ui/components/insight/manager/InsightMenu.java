/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.insight.manager;

import com.ostrichemulators.semtool.om.Insight;
import com.ostrichemulators.semtool.om.InsightOutputType;
import com.ostrichemulators.semtool.om.Parameter;
import com.ostrichemulators.semtool.om.Perspective;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class InsightMenu extends MouseAdapter {
	private static final Logger log = Logger.getLogger( InsightMenu.class );
	private final JTree tree;
	private final DefaultTreeModel model;

	public InsightMenu( JTree tre, DefaultTreeModel m ) {
		tree = tre;
		model = m;
	}

	@Override
	public void mouseReleased( MouseEvent e ) {
		if ( SwingUtilities.isRightMouseButton( e ) ) {
			e.consume();
			JPopupMenu menu = new JPopupMenu();

			Point p = e.getPoint();
			TreePath path = tree.getPathForLocation( e.getX(), e.getY() );
			if ( null == path ) {
				menu.add( new AbstractAction( "Create Perspective" ) {

					@Override
					public void actionPerformed( ActionEvent e ) {
						Perspective persp = new Perspective( "New Perspective" );
						DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( persp );

						model.insertNodeInto( newnode,
								DefaultMutableTreeNode.class.cast( model.getRoot() ), 0 );
						model.reload();
						tree.scrollRowToVisible( 0 );
						tree.setSelectionRow( 0 );
					}
				} );
			}
			else {
				DefaultMutableTreeNode node
						= DefaultMutableTreeNode.class.cast( path.getLastPathComponent() );
				DefaultMutableTreeNode parent
						= DefaultMutableTreeNode.class.cast( path.getParentPath().getLastPathComponent() );

				Object pia = node.getUserObject();

				NodeMover up = new NodeMover( node, parent, true );
				NodeMover down = new NodeMover( node, parent, false );
				menu.add( up );
				menu.add( down );

				if ( pia instanceof Insight ) {
					init( menu, Insight.class.cast( pia ), node );
					menu.addSeparator();
				}
				else if ( pia instanceof Parameter ) {
					init( menu, Parameter.class.cast( pia ), node );
				}
				else {
					// perspective
					init( menu, Perspective.class.cast( pia ), node );
					menu.addSeparator();
				}

				menu.add( new AbstractAction( "Remove this " + pia.getClass().getSimpleName() ) {

					@Override
					public void actionPerformed( ActionEvent e ) {
						TreePath selected = tree.getSelectionPath();
						boolean fixSelection
								= ( path.equals( selected ) || path.isDescendant( selected ) );
						int pathrow = tree.getRowForPath( path );

						model.removeNodeFromParent( node );

						if ( fixSelection ) {
							int rowcount = tree.getRowCount();
							if ( pathrow < rowcount ) {
								tree.setSelectionRow( pathrow );
							}
							else {
								tree.setSelectionRow( pathrow - 1 );
							}
						}
					}
				} );
			}
			menu.show( tree, p.x, p.y );
		}
	}

	private void init( JPopupMenu menu, Insight i, DefaultMutableTreeNode node ) {
		menu.add( new AbstractAction( "Clone this Insight" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Insight insight = new Insight( "Clone of " + i.getLabel() );
				insight.setSparql( i.getSparql() );
				insight.setOutput( i.getOutput() );
				insight.setDescription( i.getDescription() );

				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( insight );
				DefaultMutableTreeNode parent
						= DefaultMutableTreeNode.class.cast( node.getParent() );

				int idx = 0;
				Enumeration<DefaultMutableTreeNode> en = node.children();
				while ( en.hasMoreElements() ) {
					DefaultMutableTreeNode childnode = en.nextElement();
					Parameter oldp = Parameter.class.cast( childnode.getUserObject() );
					Parameter pchild = new Parameter( oldp.getLabel() );
					pchild.setDefaultQuery( oldp.getDefaultQuery() );

					model.insertNodeInto( new DefaultMutableTreeNode( pchild ),
							newnode, idx++ );
				}

				model.insertNodeInto( newnode, parent, parent.getIndex( node ) );
				tree.expandPath( new TreePath( newnode.getPath() ) );
				tree.setSelectionPath( new TreePath( newnode.getPath() ) );
			}
		} );

		menu.add( new AbstractAction( "Add Parameter" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Parameter param = new Parameter( "New Parameter" );
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( param );
				model.insertNodeInto( newnode, node, 0 );
				tree.setSelectionPath( new TreePath( newnode.getPath() ) );
			}
		} );
	}

	private void init( JPopupMenu menu, Parameter a, DefaultMutableTreeNode node ) {
	}

	private void init( JPopupMenu menu, Perspective p, DefaultMutableTreeNode node ) {
		menu.add( new AbstractAction( "Create Perspective" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Perspective persp = new Perspective( "New Perspective" );
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( persp );
				model.insertNodeInto( newnode,
						DefaultMutableTreeNode.class.cast( model.getRoot() ), 0 );
				tree.scrollRowToVisible( 0 );
				tree.setSelectionRow( 0 );
			}
		} );
		menu.add( new AbstractAction( "Add Insight" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Insight insight = new Insight( "New Insight", "", InsightOutputType.GRID );
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( insight );
				model.insertNodeInto( newnode, node, 0 );
				tree.setSelectionPath( new TreePath( newnode.getPath() ) );
			}
		} );
	}

	private class NodeMover extends AbstractAction {

		private final int delta;
		private final int idx;
		private final DefaultMutableTreeNode node;
		private final DefaultMutableTreeNode parent;

		public NodeMover( DefaultMutableTreeNode node, DefaultMutableTreeNode parent,
				boolean up ) {
			super( up ? "Move Up" : "Move Down" );
			delta = ( up ? -1 : 1 );
			this.node = node;
			this.parent = parent;

			idx = model.getIndexOfChild( parent, node );
			setEnabled( up ? idx > 0 : idx < model.getChildCount( parent ) - 1 );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			model.removeNodeFromParent( node );
			model.insertNodeInto( node, parent, idx + delta );

			TreePath path = new TreePath( node.getPath() );
			int row = tree.getRowForPath( path );
			Rectangle bounds = tree.getPathBounds( path );
			bounds.setLocation( 0, bounds.y );
			Logger.getLogger( getClass() ).debug( bounds );

			tree.scrollRectToVisible( bounds );
			tree.setSelectionRow( row );
		}
	}
}
