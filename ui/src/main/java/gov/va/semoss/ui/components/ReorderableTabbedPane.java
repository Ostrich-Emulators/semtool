/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author ryan
 */
public class ReorderableTabbedPane extends JTabbedPane {

	public ReorderableTabbedPane() {
		initDnD();
	}

	public ReorderableTabbedPane( int tabPlacement ) {
		super( tabPlacement );
		initDnD();
	}

	public ReorderableTabbedPane( int tabPlacement, int tabLayoutPolicy ) {
		super( tabPlacement, tabLayoutPolicy );
		initDnD();
	}

	private void initDnD() {
		TabMoveHandler handler = new TabMoveHandler();
		addMouseListener( handler );
		addMouseMotionListener( handler );
	}

	private class TabMoveHandler extends MouseInputAdapter {

		private int draggedTabIndex;

		protected TabMoveHandler() {
			draggedTabIndex = -1;
		}

		@Override
		public void mouseReleased( MouseEvent e ) {
			draggedTabIndex = -1;
		}

		@Override
		public void mousePressed( MouseEvent e ) {
			JTabbedPane tabPane = ReorderableTabbedPane.this;
			draggedTabIndex = tabPane.getUI().tabForCoordinate( tabPane,
					e.getX(), e.getY() );
		}

		@Override
		public void mouseDragged( MouseEvent e ) {
			if ( draggedTabIndex == -1 ) {
				return;
			}

			JTabbedPane tabPane = ReorderableTabbedPane.this;

			int targetTabIndex = tabPane.getUI().tabForCoordinate( tabPane,
					e.getX(), e.getY() );
			if ( targetTabIndex != -1 && targetTabIndex != draggedTabIndex ) {
				boolean isForwardDrag = targetTabIndex > draggedTabIndex;
				Component tabc = tabPane.getTabComponentAt( draggedTabIndex );
				
				tabPane.insertTab( tabPane.getTitleAt( draggedTabIndex ),
						tabPane.getIconAt( draggedTabIndex ),
						tabPane.getComponentAt( draggedTabIndex ),
						tabPane.getToolTipTextAt( draggedTabIndex ),
						isForwardDrag ? targetTabIndex + 1 : targetTabIndex );
				draggedTabIndex = targetTabIndex;

				tabPane.setSelectedIndex( draggedTabIndex );
				tabPane.setTabComponentAt( draggedTabIndex, tabc );
			}
		}
	}
}
