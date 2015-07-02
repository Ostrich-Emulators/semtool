/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class EmptySpacePopup<T extends AbstractNodeEdgeBase> extends JPopupMenu {

	private static final Logger log = Logger.getLogger( EmptySpacePopup.class );

	public EmptySpacePopup( GraphicalQueryPanel pnl ) {
		add( new AbstractAction( "Clear Graph" ) {

			@Override
			public void actionPerformed( ActionEvent e ) {
				pnl.clear();
			}
		} );
	}
}
