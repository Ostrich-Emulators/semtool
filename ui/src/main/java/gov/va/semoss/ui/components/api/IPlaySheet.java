/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.components.api;

import javax.swing.JDesktopPane;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;

/**
 * This interface class is used to define the basic functionality for all play sheet classes.  A play sheet is loosely defined
 * as any class that displays data on the the main PlayPane Desktop.  This serves as the primary interface for all of the play sheets.
 * The data for a play sheet can be pulled directly from an engine or it can be set in the form of a Jena Model.
 * <p>
 * The functions associated with this interface revolve around specifying the data to display, creating the visualization, 
 * and defining the play sheet.
 * @author karverma
 * @version $Revision: 1.0 $
 */
public interface IPlaySheet extends Runnable{

	/**
	 * Sets the String version of the SPARQL query on the play sheet. <p> The query must be set before creating the model for
	 * visualization.  Thus, this function is called before createView(), extendView(), overlayView()--everything that 
	 * requires the play sheet to pull data through a SPARQL query.
	 * @param query the full SPARQL query to be set on the play sheet
	 * @see	#createView()
	 * @see #extendView()
	 * @see #overlayView()
	 */
	public void setQuery(String query);
	
	/**
	 * Gets the latest query set to the play sheet.  <p> If multiple queries have been set to the specific play sheet through
	 * Extend or Overlay, the function will return the last query set to the play sheet.
	
	 * @see #extendView()
	 * @see #overlayView()
	 * @return the SPARQL query previously set to this play sheet */
	public String getQuery();

	/**
	 * Sets the JDesktopPane to display the play sheet on. <p> This must be set before calling functions like 
	 * {@link #createView()} or {@link #extendView()}, as functions like these add the panel to the desktop pane set in this
	 * function.
	 * @param pane the desktop pane that the play sheet is to be displayed on
	 */
	public void setJDesktopPane(JDesktopPane pane);
	
  /**   Sets the Perspective for this playsheet.
   * 
   * @param perValue -- (Perspective)
   */
  public void setPerspective(Perspective perValue);
	
	public Perspective getPerspective();
	
  /**
   * Sets the insight for this playsheet.
   * @param insight 
   */
  public void setInsight( Insight insight );
  
  /**
   * Gets the insight of this playsheet
   * @return 
   */
  public Insight getInsight();
  
	/**
	 * This is the function that is used to create the first view 
	 * of any play sheet.  It often uses a lot of the variables previously set on the play sheet, such as {@link #setQuery(String)},
	 * {@link #setJDesktopPane(JDesktopPane)}, {@link #setEngine(IEngine)}, and {@link #setTitle(String)} so that the play 
	 * sheet is displayed correctly when the view is first created.  It generally creates the model for visualization from 
	 * the specified engine, then creates the visualization, and finally displays it on the specified desktop pane
	 * 
	 * <p>This is the function called by the PlaysheetCreateRunner.  PlaysheetCreateRunner is the runner used whenever a play 
	 * sheet is to first be created, most notably in ProcessQueryListener.
	 */
	public void createView();

	/**
	 * Recreates the visualizer and the repaints the play sheet without recreating the model or pulling anything 
	 * from the specified engine.
	 * This function is used when the model to be displayed has not been changed, but rather the visualization itself 
	 * must be redone.
	 * <p>This function takes into account the nodes that have been filtered either through FilterData or Hide Nodes so that these 
	 * do not get included in the recreation of the visualization.
	 */
	public void refineView();

	/**
	 * This function is very similar to {@link #extendView()}.  Its adds additional data to the model already associated with 
	 * the play sheet.  The main difference between these two functions, though is {@link #overlayView()} is used to overlay 
	 * a query that could be unrelated to the data that currently exists in the play sheet's model whereas {@link #extendView()} 
	 * uses FilterNames to limit the results of the additional data so that it only add data relevant to the play sheet's current
	 *  model.
	 *  <p>This function is used by PlaysheetOverlayRunner which is called when the Overlay button is selected.
	 */
	public void overlayView();

	/**
	 * Removes from the play sheet's current model everything that was returned from the query the last time the 
	 * model was augmented.
	 * 
	 * TODO: Uncomment when exposing to other playsheets. Currently just graphs have the functionality in the UI
	 * @param engine IEngine
	 */
//	public void undoView();
	
	// redo the view - this is useful when the user selects custom data properties and object properties
	/**
	 * Uses the query and the engine associated with the play sheet to create the model, the visualization, and repaint the graph.  
	 * This is very similar to {@link #createView()} with the main difference being that this function does not add the panel or 
	 * do any of the visibility functionality that is necessary when first creating the play sheet.
	 * 
	 * TODO: Uncomment when exposing to other playsheets. Currently just graphs have the functionality in the UI
	 */
//	public void redoView();

	/**
	 * Sets the RDF engine for the play sheet to run its query against.  Can be any of the active engines, all of which are 
	 * stored in DIHelper
	 * @param engine the active engine for the play sheet to run its query against.
	 */
	public void setEngine(IEngine engine);

	/**
	 * Gets the RDF engine for the play sheet to run its query against.  Can be any of the active engines, all of which are 
	 * stored in DIHelper
	
	
	 * @return IEngine */
	public IEngine getEngine();
	
	/**
	 * Sets the title of the play sheet.  The title is displayed as the text on top of the internal frame that is the play sheet.
	 * @param title representative name for the play sheet.  Often a concatenation of the question ID and question text
	 */
	public void setTitle(String title);
	
	// gets the title
	/**
	 * Gets the title of the play sheet.  The title is displayed as the text on top of the internal frame that is the play sheet.
   * @return the title
	 */
	public String getTitle();
		
	
	// Interim call to create the data
	public void createData();
	
	// get the data
	public Object getData();
	
	// Interim call to run analytics
	public void runAnalytics();
		
}