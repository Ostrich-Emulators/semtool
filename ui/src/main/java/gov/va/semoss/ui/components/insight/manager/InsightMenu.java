/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.ui.components.playsheets.GridPlaySheet;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author ryan
 */
public class InsightMenu extends MouseAdapter {

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

			Point p = e.getPoint();
			TreePath path = tree.getPathForLocation( e.getX(), e.getY() );
			DefaultMutableTreeNode node
					= DefaultMutableTreeNode.class.cast( path.getLastPathComponent() );
			Object pia = node.getUserObject();
			if ( null == pia ) {
				return;
			}

			JPopupMenu menu = new JPopupMenu();
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

			menu.add( new AbstractAction( "Remove This " + pia.getClass().getSimpleName() ) {

				@Override
				public void actionPerformed( ActionEvent e ) {
					model.removeNodeFromParent( node );
				}
			} );

			menu.show( tree, p.x, p.y );
		}
	}

	private void init( JPopupMenu menu, Insight i, DefaultMutableTreeNode node ) {
		menu.add( new AbstractAction( "Add Parameter" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Parameter param = new Parameter( "New Parameter" );
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( param );
				model.insertNodeInto( newnode, node, 0 );
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
			}
		} );
		menu.add( new AbstractAction( "Add Insight" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Insight insight = new Insight( "New Insight", "", GridPlaySheet.class );
				DefaultMutableTreeNode newnode = new DefaultMutableTreeNode( insight );
				model.insertNodeInto( newnode, node, 0 );
			}
		} );
	}
}
