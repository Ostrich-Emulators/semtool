package gov.va.semoss.rdf.engine.api;

import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;

public interface WriteablePerspectiveTab {
  /**   Adds a new Insight to the current Perspective and to the triple-store on disk.
   * 
   * @param newOrder -- (int) Order value for the new Insight.
   *
   * @param perspective -- (Perspective) Perspective to remove from database.
   * 
   * @return addPerspective -- (boolean) Whether the save to disk succeeded.
   */
	public boolean addInsight(int newOrder, Perspective perspective);
	
  /**   Removes an Insight from a Perspective in the triple-store on disk.
   * 
   * @param insight -- (Insight) Insight to remove from the Perspective.
   * 
   * @param perspective -- (Perspective) Perspective containing the above Insight.
   * 
   * @param doImport -- (boolean) Whether to import memory database to disk.
   */
	public boolean removeInsight(Insight insight, Perspective perspective, boolean doImport);
  
  /**   Adds a new Perspective to the triple-store on disk.
   * 
   * @param strTitle -- (String) Title of Perspective (rdfs:label).
   * 
   * @param strUriTitle -- (String) Title of Perspective suitable for a URI (no whitespace).
   * 
   * @param strDescription -- (String) Description of Perspective (dcterms:description).
   * 
   * @param addDummyInsight -- (boolean) Whether to add a dummy Insight to this Perspective.
   * 
   * @return addPerspective -- (boolean) Whether the save to disk succeeded.
   */
	public boolean addPerspective(String strTitle, String strUriTitle, String strDescription, boolean addDummyInsight);
	
  /**   Deletes a Perspective, and associated Insights, from the triple-store on disk.
   * 
   * @param perspective -- (Perspective) Perspective to remove from database.
   */
	public boolean deletePerspective(Perspective perspective);
	
  /**   Saves a Perspective's Title and Description into the triple-store on disk,
   * where the passed-in URI is the subject.
   * 
   * @param uri -- (String) URI of Perspective.
   * 
   * @param strTitle -- (String) Title of Perspective (rdfs:label).
   * 
   * @param strDescription (String) Description of Perspective (dcterms:description).
   * 
   * @return savePerspective -- (boolean) Whether the save to disk succeeded.
   */
	public boolean savePerspective(String uri, String strTitle, String strDescription);
	
	/**   Searches the database for Insights that are not associated with any Perspective,
	 * and places them under the "Detached-Insight-Perspective". If no "Detached-Insight-Perspective"
	 * exists, then this method creates one.
	 * 
	 * @return saveDetachedInsights -- (boolean) Whether the operations described above succeeded.
	 *    Note: Should there be no detached Insights in the database, then true will be returned.
	 */
	public boolean saveDetachedInsights();

    /**   Obtains the next available Insight ordering index from the passed-in Perspective.
     * 
     * @param perspectiveURI -- (URI) URI of a Perspective.
     * 
     * @return getNextInsightIndex -- (int) Described above.
     */
     public int getNextInsightIndex(URI perspectiveURI);
}
