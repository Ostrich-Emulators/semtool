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
package gov.va.semoss.ui.components.api;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.openrdf.model.Model;
import org.openrdf.model.Value;

/**
 * This interface class is used to define the basic functionality for all play
 * sheet classes. A play sheet is loosely defined as any class that displays
 * data on the the main PlayPane Desktop. This serves as the primary interface
 * for all of the play sheets.
 * <p>
 * The functions associated with this interface revolve around specifying the
 * data to display and creating the visualization.
 *
 * @author karverma
 * @version $Revision: 1.0 $
 */
public interface IPlaySheet {

	/**
	 * Gets the RDF engine for the play sheet to run its query against. Can be any
	 * of the active engines, all of which are stored in DIHelper
	 *
	 * @return IEngine
	 */
	public IEngine getEngine();

	/**
	 * Sets the title of the play sheet. The title is displayed as the text on top
	 * of the internal frame that is the play sheet.
	 *
	 * @param title representative name for the play sheet. Often a concatenation
	 * of the question ID and question text
	 */
	public void setTitle( String title );

	/**
	 * Gets the title of the play sheet. The title is displayed as the text on top
	 * of the internal frame that is the play sheet.
	 *
	 * @return the title
	 */
	public String getTitle();

	public boolean hasChanges();

	/**
	 * Gets a map of String-to-Actions from this tab.
	 *
	 * @see PlaySheetFrame#getActions()
	 * @return a (by default, empty) mapping of actions
	 */
	public Map<String, Action> getActions();

	/**
	 * Gets headers for this playsheet. The definition of "header" is
	 * implementation specific, but if {@link #getTabularData()} returns non-null,
	 * this function should return the headers for the data returned by that
	 * function
	 *
	 * @return
	 */
	public List<String> getHeaders();
	/**
	 * Signals when this playsheet's tab is selected in the frame. By default,
	 * does nothing
	 */
	public void activated();

	public void create( List<Value[]> data, List<String> headers, IEngine engine );

	public void create( Model m, IEngine engine );

	public void overlay( List<Value[]> data, List<String> headers, IEngine eng );

	public void overlay( Model m, IEngine engine );

	public void incrementFont( float incr );

	/**
	 * Does this playsheet accept data with the given headers? If no, callers
	 * should not call
	 * {@link #overlay(java.util.List, java.util.List, gov.va.semoss.rdf.engine.api.IEngine)}
	 *
	 * @param newheaders
	 * @return
	 */
	public boolean canAcceptDataWithHeaders( List<String> newheaders );

	/**
	 * Does this playsheet accept model data? If no, callers should not call
	 * {@link #overlay(java.util.List, java.util.List, gov.va.semoss.rdf.engine.api.IEngine)}
	 *
	 * @return
	 */
	public boolean canAcceptModelData();

	/**
	 * Gets the tabular data from this playsheet, or null if this playsheet does
	 * not support tabular data.
	 *
	 * @return tabular data, or null if this playsheet does not support tables
	 * @see @link #getHeaders()}
	 */
	public List<Object[]> getTabularData();

	/**
	 * Should this component's PlaySheetFrame open tabs if this is the initial
	 * component (or last remaining)? By default, return false
	 *
	 * @return true, if the frame should have visible tabs initially
	 */
	public boolean prefersTabs();
}
