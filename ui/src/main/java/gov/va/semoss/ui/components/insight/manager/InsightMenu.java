/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
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
			DefaultMutableTreeNode dmtn
					= DefaultMutableTreeNode.class.cast( path.getLastPathComponent() );
			Object pia = dmtn.getUserObject();
			if ( null == pia ) {
				return;
			}

			JPopupMenu menu = new JPopupMenu();
			if ( pia instanceof Insight ) {
				init( menu, Insight.class.cast( pia ) );
			}
			else if ( pia instanceof Parameter ) {
				init( menu, Parameter.class.cast( pia ) );
			}
			else {
				// perspective
				init( menu, Perspective.class.cast( pia ) );
			}

			menu.addSeparator();
			menu.add( new AbstractAction( "Remove This Node" ) {

				@Override
				public void actionPerformed( ActionEvent e ) {
					model.removeNodeFromParent( dmtn );
				}
			} );

			menu.show( tree, p.x, p.y );
		}
	}

	private void init( JPopupMenu menu, Insight i ) {

	}

	private void init( JPopupMenu menu, Parameter a ) {

	}

	private void init( JPopupMenu menu, Perspective p ) {

	}
}
