/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.api;

import gov.va.semoss.model.vocabulary.ARG;
import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.util.Utility;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryException;

/**
 * A first pass at CRUD operations we might want for
 * {@link Perspective Perspectives} and {@link Insight Insights}
 *
 * @author ryan
 */
public interface WriteableInsightManager extends InsightManager {

	/**
	 * Commits any changes to the engine. If no changes were made, does nothing.
	 * Note that this function may do strange things with the {@link IEngine} that
	 * creates it, so care must be taken when calling it. For example, the
	 * {@link BigDataEngine} flips read-only and read-write handles, which will
	 * cause a hang if the connections aren't closed from the same thread that
	 * opened them.
	 *
	 * @throws SecurityException if the user does not have permission to write
	 * insights
	 */
	public void commit();

	/**
	 * Do we have any changes we need to commit?
	 *
	 * @return
	 */
	public boolean hasCommittableChanges();

	/**
	 * Discards any changes that have not been committed
	 */
	public void dispose();

	/**
	 * Adds a completely-new Insight
	 *
	 * @param ins the insight to add
	 *
	 * @return the URI of the new insight
	 */
	public URI add( Insight ins );

	public void remove( Insight ins );

	public void update( Insight ins );

	/**
	 * Adds a completely-new Perspective
	 *
	 * @param p the perspective to add
	 *
	 * @return the URI of the new perspective
	 */
	public URI add( Perspective p );

	public void remove( Perspective p );

	public void update( Perspective p );

	/**
	 * Sets the insights (in order) for this Perspective
	 *
	 * @param p the perspective
	 * @param insights the insights, in order, to set for this perspective
	 */
	public void setInsights( Perspective p, List<Insight> insights );

	public void addRawStatements( Collection<Statement> stmts ) throws RepositoryException;


  /**
   * Removes all perspectives and insights
   */
  public void clear();
  
   /**
	 * Extracts from V-CAMP/SEMOSS preferences the user's name, email, and
	 * organization, and returns a string of user-info for saving with Insights,
	 * based upon these. If these preferences have not been set, then the passe-in
	 * value is returned.
	 *
	 * @param strOldUserInfo -- (String) User-info that has been displayed from a
	 * database fetch.
	 *
	 * @return userInfoFromToolPreferences -- (String) Described above.
	 */  
	public String userInfoFromToolPreferences( String strOldUserInfo );

//---------------------------------------------------------------------------------------------------------
//  D e l e t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
//---------------------------------------------------------------------------------------------------------
	  
	  /**   Deletes all Parameters from all Insights in the database.
      * 
      * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
      */
	  public boolean deleteAllParameters();
	  
	  /**   Deletes all Insights from all Perspectives in the database.
       * 
       * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
	   */
	  public boolean deleteAllInsights();	  
	  
      /**   Deletes all Perspectives from the database.
       * 
       * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
	   */
	  public boolean deleteAllPerspectives();

//---------------------------------------------------------------------------------------------------------
// I n s e r t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
//---------------------------------------------------------------------------------------------------------
	  
	  /**   Saves the passed-in Perspective's Title and Description into the triple-store 
	   * on disk. 
	   * 
       * NOTE: The Perspective parameter is returned by side-effect, because its
 	   *       URI is used to create Insight slots.
       *
	   * @param perspective -- (Perspective) The Perspective to persist.
	   *
       * @return savePerspective -- (boolean) Whether the save to disk succeeded.
       */
	  public boolean savePerspective(Perspective perspective);	  
	  
	    /**    Saves the various Insight fields to the triple-store on disk.
	     * 
	     * @param perspective -- (Perspective) A Perspective, extracted from the tree-view
	     *    of the Insight Manager.
	     * 
	     * @param insight -- (Insight) An Insight, belonging to the above Perspective.
	     *    
	     * @return saveInsight -- (boolean) Whether the save succeeded.
	     */	  
	    public boolean saveInsight(Perspective perspective, Insight insight);
	    
	    /**    Saves the various Parameter fields to the triple-store on disk.
	     * 
	     * @param insight -- (Insight) An Insight, extracted from the tree-view
	     *    of the Insight Manager.
	     * 
	     * @param parameter -- (Parameter) A Parameter, belonging to the above Insight.
	     *    
	     * @return saveParameter -- (boolean) Whether the save succeeded.
	     */
	    public boolean saveParameter(Insight insight, Parameter parameter);	    

}//End "WriteableInsightManager" interface.
