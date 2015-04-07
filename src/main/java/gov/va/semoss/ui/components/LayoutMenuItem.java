/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * This class is used to configure menu items in the layout.
 */
public class LayoutMenuItem extends AbstractAction {
	private static final long serialVersionUID = 3303474056533355632L;
	
	private GraphPlaySheet ps;
	private String layout;

	/**
	 * Constructor for LayoutMenuItem.
	 *
	 * @param layout String
	 * @param ps IPlaySheet
	 */
	public LayoutMenuItem( String layout, GraphPlaySheet ps ) {
		super( layout );
		this.ps = ps;
		this.layout = layout;
	}
	
	@Override
	public void actionPerformed( ActionEvent ae ){
		paintLayout();
	}

	/**
	 * Paints the specified layout.
	 */
	public void paintLayout() {
		String oldLayout = ps.getLayoutName();
		ps.setLayoutName( layout );
		boolean success = ps.createLayout();
		if ( success ) {
			ps.refreshView();
		}
		else {
			if ( layout.equals( Constants.RADIAL_TREE_LAYOUT )
					|| layout.equals( Constants.BALLOON_LAYOUT )
					|| layout.equals( Constants.TREE_LAYOUT ) ) {
				int response = showOptionPopup();
				if ( response == 1 ) {
					ps.getSearchPanel().clickTreeButton();
				}
				else {
					ps.setLayoutName( oldLayout );
				}
			}
			else {
				Utility.showError( "This layout cannot be used with the current graph" );
				ps.setLayoutName( oldLayout );
			}
		}
	}

	/**
	 * This displays options to the user in a popup menu about what type of layout
	 * they want to display.
	 *
	 * @return int User response.
	 */
	private int showOptionPopup() {
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp( Constants.MAIN_FRAME );
		Object[] buttons = { "Cancel Layout", "Continue With Tree" };
		int response = JOptionPane.showOptionDialog( playPane, "This layout requires the graph to be in the format of a tree.\nWould you like to duplicate nodes so that it is in the fromat of a tree?\n\nPreferred root node must already be selected",
				"Convert to Tree", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[1] );
		return response;
	}
}
