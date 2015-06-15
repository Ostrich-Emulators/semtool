package gov.va.semoss.rdf.engine.api;

import java.util.ArrayList;
import java.util.Collection;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;

public interface WriteableInsightTab {
	  /**   Adds a dummy Parameter to the current Insight in the database.
	   * 
	   * @param insight -- (Insight) Insight to which new Parameter will be added.
	   */
	  public boolean addParameter(Insight insight);
	  
	  /**   Deletes a Parameter from an Insight in the triple-store on disk.
	   * 
       * @param insight -- (Insight) Insight containing the Parameter to delete.
       * 
       * @param parameter -- (Parameter) Parameter to delete.
       */
	  public boolean deleteParameter(Insight insight, Parameter parameter);
	  
	  /**   Deletes the current Insight from from the database, and removes its
	   * reference from all Perspectives.
       * 
       * @param arylInsights - (ArrayList<Insight>) All Insights under the current
       *     Perspective.
	   * 
	   * @param insight -- (Insight) Insight to remove from the Perspective.
	   * 
	   * @param perspective -- (Perspective) Current Perspective
	   */
	  public boolean deleteInsight(ArrayList<Insight> arylInsights, Insight insight, Perspective perspective);
	  
	  /**   Saves the current Insight to the database.
       * 
       * @param arylInsights - (ArrayList<Insight>) All Insights under the current
       *     Perspective.
	   * 
	   * @param insight -- (Insight) Insight to be saved
	   * 
	   * @param colPerspectivesToAddInsight -- (Collection<Perspective>) A collection of Perspectives
	   *    to which the passed-in Insight must be added.
	   * 
	   * @param colPerspectivesToRemoveInsight -- (Collection<Perspective>) A collection of Perspectives
	   *    to which the passed-in Insight must be removed.
	   * 
	   * @return saveInsight -- (boolean) Whether the Insight was saved ok.
	   */
	  public boolean saveInsight(ArrayList<Insight> arylInsights, Insight insight,
		  Collection<Perspective> colPerspectivesToAddInsight, Collection<Perspective> colPerspectivesToRemoveInsight);

	  /**   Adds an existing Insight to the passed-in Perspective and to the triple-store on disk.
	   * 
	   * @param insight -- (Insight) An Insight to add to the passed-in Perspective.
	   *
	   * @param perspective -- (Perspective) Perspective to which the Insight must be added.
	   * 
	   * @return addPerspective -- (boolean) Whether the save to disk succeeded.
	   */
	  public boolean addExistingInsight(Insight insight, Perspective perspective);
}
