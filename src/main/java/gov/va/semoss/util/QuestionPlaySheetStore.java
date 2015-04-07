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
 *****************************************************************************
 */
package gov.va.semoss.util;

import gov.va.semoss.ui.components.PlaySheetFrame;
import java.util.HashMap;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.api.IPlaySheet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * This class is used to store question playsheets.
 */
public class QuestionPlaySheetStore extends HashMap<String, IPlaySheet> {

	private static final Logger logger = Logger.getLogger( QuestionPlaySheetStore.class );

	public static QuestionPlaySheetStore store = null;
	//public Vector 
	public int idCount = 0;
	public int customIDcount = 0;

	/**
	 * Constructor for QuestionPlaySheetStore.
	 */
	protected QuestionPlaySheetStore() {
		// do nothing
	}

	/**
	 * Gets an instance from a specific playsheet.
	 *
	 * @return QuestionPlaySheetStore
	 */
	public static QuestionPlaySheetStore getInstance() {
		if ( store == null ) {
			store = new QuestionPlaySheetStore();
		}
		return store;
	}

	/**
	 * Gets the active sheet.
	 *
	 * @return Active sheet
	 */
	public IPlaySheet getActiveSheet() {
		// no need to clear this anymore
		JDesktopPane pane = DIHelper.getInstance().getDesktop();
		JInternalFrame jif = pane.getSelectedFrame();
		if( jif instanceof PlaySheetFrame ){
			return PlaySheetFrame.class.cast( jif ).getActivePlaySheet();
		}
		
		return null;
	}

	/**
	 * Gets the count of all the sheets in the question store.
	 *
	 * @return Count
	 */
	public int getIDCount() {
		int total = idCount + customIDcount;
		return total;
	}

	/**
	 * Gets the count of all the custom-query sheets in the question store.
	 *
	 * @return The number of custom sheets in the question store
	 */
	public int getCustomCount() {
		return customIDcount;
	}
}
