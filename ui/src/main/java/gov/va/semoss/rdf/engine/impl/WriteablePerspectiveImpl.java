package gov.va.semoss.rdf.engine.impl;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;

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
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.api.WriteablePerspective;
import gov.va.semoss.rdf.engine.util.EngineUtil;

public class WriteablePerspectiveImpl implements WriteablePerspective {
	  private final WriteableInsightManager wim;
	  private final RepositoryConnection rc;
	  private static final Logger log = Logger.getLogger( WriteablePerspective.class );
	  private final Pattern pattern = Pattern.compile( "^(\\w+)(.*)$" );
	  private static long lngUniqueIdentifier = System.currentTimeMillis();
		
	  //These are necessary to make sure that the Insight Manager 
	  //loads after the left-pane is completely loaded:
	  //---------------------------------------------------------
	  private static Object guiUpdateMonitor = new Object();
	  private static boolean boolLeftPaneUpdated = false;
	  @Override
	  public Object getGuiUpdateMonitor(){
		  return guiUpdateMonitor;
	  }
	  @Override
	  public boolean getLeftPaneUpdated(){
		  return boolLeftPaneUpdated;
	  }
	  @Override
	  public void setLeftPaneUpdated(boolean boolLeftPaneUpdated){
		  WriteablePerspectiveImpl.boolLeftPaneUpdated = boolLeftPaneUpdated;
	  }

	  /**    Class constructor. Sets the WriteableInsightManagerObject (passed in), and
	   * the raw connection from it.
	   * 
	   * @param wim -- (WriteableInsightManager).
	   */
	  public WriteablePerspectiveImpl( WriteableInsightManagerImpl wim ) {
		  this.wim = wim;
		  this.rc = wim.getRawConnection();
	  }

	  /**   Loops through each Perspective of the Insight Manager's TreeView, and persists
	   * data (Perspectives, Insights, and Parameters) to the Insights KB on disk. This method
	   * first deletes all Perspectives, Insights, and Parameters from the KB. Then it attempts
	   * to Insert the TreeView data. Should this method encounter an object that cannot be 
	   * persisted, false is returned, and further processing is halted. Otherwise, true is returned.
	   * 
	   * @param olstPerspective -- (ObservableList<TreeItem<Object>>) Children of the root of
	   *    the TreeView.
	   */
	  @Override
	  public boolean persistenceWrapper(ObservableList<TreeItem<Object>> olstPerspectives){
		  boolean boolReturnValue = true;

          if(!deleteAllParameters() || !deleteAllInsights() || !deleteAllPerspectives()){
        	  boolReturnValue = false;
        	  
          }else{
		     for(TreeItem<Object> treeItem: olstPerspectives){
			     Perspective perspective = (Perspective) treeItem.getValue();
			     if(!savePerspective(perspective)){
			    	 boolReturnValue = false;
			    	 break;
			     }
			     if(!perspective.getInsights().isEmpty()
			    	&& !perspective.getInsights().get(0).toString().equals("")){
				     for(Insight insight: perspective.getInsights()){
				    	 if(!saveInsight(perspective, insight)){
					    	 boolReturnValue = false;
					    	 break;
					     }
				    	 if(!insight.getInsightParameters().isEmpty() 
				    		&& !insight.getInsightParameters().iterator().next().toString().equals("")){
				    	    for(Parameter parameter: insight.getInsightParameters()){
				    		    if(!saveParameter(insight, parameter)){
				    		 	    boolReturnValue = false;
				    			    break;
				    		    }
				    	    }
				            if(!boolReturnValue){
				        	   break;
				            }
				    	 }
				     }
			         if(!boolReturnValue){
			        	 break;
			         }
			     }
		     }
          }
          if(boolReturnValue){
		     boolReturnValue = importInsights();
          }
		  return boolReturnValue;
	  }
//---------------------------------------------------------------------------------------------------------
//   D e l e t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
//---------------------------------------------------------------------------------------------------------
	  /**   Deletes all Parameters from all Insights in the database.
       * 
       * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
       */
	  private boolean deleteAllParameters(){
		  boolean boolReturnValue = false;
		  
		  String query_1 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
			  + "DELETE{ ?query ?p ?o .} "
			  + "WHERE{ ?parameter sp:query ?query . "
			  + "?insight spin:constraint ?parameter . " 
			  + "?query ?p ?o .}";
				  	  		  
		  String query_2 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			  + "DELETE{ ?predicate ?p ?o .} "
			  + "WHERE{ ?parameter spl:predicate ?predicate . "
			  + "?insight spin:constraint ?parameter . " 
			  + "?predicate ?p ?o .}";
				  	  
		  String query_3 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "DELETE{ ?parameter ?p ?o .} "
		      + "WHERE{ ?insight spin:constraint ?parameter . "
		      + "?parameter ?p ?o .}";

		  String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
			  + "DELETE{ ?insight spin:constraint ?parameter .} "
			  + "WHERE{ ?insight spin:constraint ?parameter .}";

		  try{
	         rc.begin();
	         
	         Update uq_1 = rc.prepareUpdate(QueryLanguage.SPARQL, query_1);
	         Update uq_2 = rc.prepareUpdate(QueryLanguage.SPARQL, query_2);
	         Update uq_3 = rc.prepareUpdate(QueryLanguage.SPARQL, query_3);
	         Update uq_4 = rc.prepareUpdate(QueryLanguage.SPARQL, query_4);
	         uq_1.execute();
	         uq_2.execute();
	         uq_3.execute();
	         uq_4.execute();
	         
	         rc.commit();
             boolReturnValue = true;
	         
		  }catch(Exception e){
		     log.error( e, e );
		     try{
		        rc.rollback();
		     }catch(Exception ee){
		        log.warn( ee, ee );
		     }
		  }
	 	  return boolReturnValue;
	  }

	  /**   Deletes all Insights from all Perspectives in the database.
       * 
       * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
	   */
	  private boolean deleteAllInsights(){
		  boolean boolReturnValue = false;
		  
		  String query_1 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
		      + "DELETE{ ?perspective olo:slot ?slot .} "
		      + "WHERE{ ?perspective olo:slot ?slot .}";

	      String query_2 = "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
		      + "DELETE{ ?query ?p ?o .} "
		   	  + "WHERE{ ?constraint sp:query ?query . "
		   	  + "?query ?p ?o .}";

	      String query_3 = "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			  + "DELETE{ ?predicate ?p ?o .} "
			  + "WHERE{ ?constraint spl:predicate ?predicate . "
			  + "?predicate ?p ?o .} ";

	      String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
	       	  + "DELETE{ ?constraint ?p ?o .} "
	      	  + "WHERE{ ?insight spin:constraint ?constraint . "
	      	  + "?constraint ?p ?o .} ";

	      String query_5 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
	    	  + "DELETE{ ?body ?p ?o .} "
	   	      + "WHERE{ ?insight spin:body ?body . "
	   	      + "?body ?p ?o .} ";
	      
		  String query_6 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
			  + "DELETE{ ?insight ?p ?o .} "
			  + "WHERE{ ?slot olo:item ?insight . "
			  + "?insight ?p ?o .} ";

		  String query_7 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
			  + "DELETE{ ?slot ?p ?o .} "
			  + "WHERE{ ?slot olo:item ?insight . "
			  + "?slot ?p ?o .} ";

		  try{
	         rc.begin();
	         Update uq_1 = rc.prepareUpdate(QueryLanguage.SPARQL, query_1);
	         Update uq_2 = rc.prepareUpdate(QueryLanguage.SPARQL, query_2);
	         Update uq_3 = rc.prepareUpdate(QueryLanguage.SPARQL, query_3);
	         Update uq_4 = rc.prepareUpdate(QueryLanguage.SPARQL, query_4);
	         Update uq_5 = rc.prepareUpdate(QueryLanguage.SPARQL, query_5);
	         Update uq_6 = rc.prepareUpdate(QueryLanguage.SPARQL, query_6);
	         Update uq_7 = rc.prepareUpdate(QueryLanguage.SPARQL, query_7);
	         uq_1.execute();
	         uq_2.execute();
	         uq_3.execute();
	         uq_4.execute();
	         uq_5.execute();
	         uq_6.execute();
	         uq_7.execute();
	         
	         rc.commit();
             boolReturnValue = true;

		  }catch(Exception e){
		     log.error( e, e );
		     try{
		        rc.rollback();
		     }catch(Exception ee){
		        log.warn( ee, ee );
		     }
		  }
	 	  return boolReturnValue;
	  }
	  

      /**   Deletes all Perspectives from the database.
       * 
       * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded. 
	   */
	  private boolean deleteAllPerspectives() {
			boolean boolReturnValue = false;

			String query_1 = "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
				+ "DELETE{ ?perspective ?p ?o .} "
				+ "WHERE{ ?perspective a vas:Perspective . "
				+ "?perspective ?p ?o . }";
		   	  
		    String query_2 ="PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			    + "DELETE{ ?argument ?p ?o .} "
			   	+ "WHERE{ ?argument a spl:Argument . "
			    + "?argument ?p ?o .} ";

			try {
				rc.begin();

				Update uq_1 = rc.prepareUpdate( QueryLanguage.SPARQL, query_1 );
				Update uq_2 = rc.prepareUpdate( QueryLanguage.SPARQL, query_2 );

				uq_1.execute();
				uq_2.execute();

				rc.commit();
                boolReturnValue = true;
			}
			catch ( Exception e ) {
				log.error( e, e );
				try {
					rc.rollback();
				}
				catch (Exception ee) {
					log.warn(ee, ee);
				}
			}
			return boolReturnValue;
	   }

//---------------------------------------------------------------------------------------------------------
//  I n s e r t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
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
	  private boolean savePerspective(Perspective perspective) {
			boolean boolReturnValue = false;
			lngUniqueIdentifier += 1;
			String strUniqueIdentifier = String.valueOf(lngUniqueIdentifier);
			ValueFactory insightVF = rc.getValueFactory();
			String perspectiveUriName = "perspective-" + strUniqueIdentifier;
			URI perspectiveURI = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, perspectiveUriName);
			String perspectiveURI_String = "<" + perspectiveURI.toString() + ">";
			//Be sure to set the new Perspective URI, because this Perspective object 
			//is returned by side-effect, and it's URI is used to create Insight slots:
			perspective.setUri(perspectiveURI);
			Literal now = insightVF.createLiteral(new Date());
			String strCreator = wim.userInfoFromToolPreferences( "Created By Insight Manager, " + System.getProperty( "release.nameVersion", "VA SEMOSS" ) );
			Literal creator = insightVF.createLiteral( strCreator );
			//Make sure that embedded quotes and new-line characters can be persisted:
            String label = legalizeQuotes(perspective.getLabel());
			String strDescription = legalizeQuotes(perspective.getDescription());

			String query = "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
					+ "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
					+ "INSERT{ ?uri rdfs:label \"" + label + "\" . "
					+ "?uri dcterms:description \"" + strDescription + "\" . "
					+ "?uri a vas:Perspective . "
					+ "?uri dcterms:created " + now + " . "
					+ "?uri dcterms:modified " + now + " . "
					+ "?uri dcterms:creator " + creator + " .} "
					+ "WHERE{ BIND(" + perspectiveURI_String.toString() + " AS ?uri) .}";
			try {
				rc.begin();
				Update uq = rc.prepareUpdate( QueryLanguage.SPARQL, query );
				uq.execute();
				rc.commit();
                boolReturnValue = true;
			}
			catch ( Exception e ) {
				e.printStackTrace();
				try {
					rc.rollback();
				}
				catch ( Exception ee ) {
					log.warn( ee, ee );
				}
			}
			return boolReturnValue;
		}

	    /**    Saves the various Insight fields to the triple-store on disk.
	     * 
	     * @param perspective -- (Perspective) A Perspective, extracted from the tree-view
	     *    of the Insight Manager.
	     * 
	     * @param insight -- (Insight) An Insight, belonging to the above Perspective.
	     *    
	     * @return saveInsight -- (boolean) Whether the save succeeded.
	     */
	    private boolean saveInsight(Perspective perspective, Insight insight){
			boolean boolReturnValue = false;
			lngUniqueIdentifier += 1;
			String strUniqueIdentifier = String.valueOf(lngUniqueIdentifier);
			ValueFactory insightVF = rc.getValueFactory();
			String perspectiveURI_String = "<" + perspective.getUri().toString() + ">";
		    String insightURI_String = "<" + insight.getIdStr() + ">";
			//Make sure that embedded quotes and new-line characters can be persisted:
		    String question = legalizeQuotes(insight.getLabel());
		    String dataViewOutput = insight.getOutput();
		    String isLegacy = String.valueOf(insight.isLegacy());
			//Make sure that embedded quotes and new-line characters can be persisted:
		    String sparql = legalizeQuotes(insight.getSparql().trim());
		    String description = legalizeQuotes(insight.getDescription().trim());
			String slotUriName = perspective.getUri().getLocalName() + "-slot-" + strUniqueIdentifier;
			String slotURI_String = "<" + insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, slotUriName).toString() + ">";
			Literal order = insightVF.createLiteral(insight.getOrder());
			String type = "";
			Matcher matcher = pattern.matcher(sparql);
			if (matcher.find()) {
				type = matcher.group(1);
			}
			String spinBodyURI_String = "<" + insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, "insight-" + strUniqueIdentifier + "-" + type ).toString() + ">";
			//Insights can only have only SELECT and CONSTRUCT queries:
			String spinBodyType = "";
			if ( "SELECT".equals( type.toUpperCase() ) ) {
			    spinBodyType = "sp:Select";
			}
			else {
			    spinBodyType = "sp:Construct";
			}
				
		    String query_1 =  "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
                    + "INSERT{ ?perspectiveURI olo:slot " + slotURI_String + " . "
                    + slotURI_String + "olo:item ?insightURI . "
                    + slotURI_String + "olo:index " + order + " .} "
                    + "WHERE{ BIND(" + perspectiveURI_String + " AS ?perspectiveURI) . "
                    + "BIND(" + insightURI_String + " AS ?insightURI) .}";

			String query_2 = "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
					+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
					+ "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
					+ "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
					+ "PREFIX " + UI.PREFIX + ": <" + UI.NAMESPACE + "> "
                    + "INSERT{ ?insightURI rdfs:label \"" + question + "\" . "
	                + "?insightURI ui:dataView vas:" + dataViewOutput + " . "
	                + "?insightURI rdfs:subclassof vas:InsightProperties . "
	                + "?insightURI ui:dataView vas:" + dataViewOutput + " . "
	                + "?insightURI vas:isLegacy " + isLegacy + " . "
	                + "?insightURI spin:body ?spinBodyURI . "
	                + "?spinBodyURI rdf:type " + spinBodyType + " . "
	                + "?spinBodyURI sp:text \"" + sparql + "\" . "
	                + "?insightURI dcterms:description \"" + description + "\" . "
	                + "?insightURI dcterms:creator \"" + insight.getCreator() + "\" . "
	                + "?insightURI dcterms:created \"" + insight.getCreated() + "\" . "
	                + "?insightURI dcterms:modified \"" + insight.getModified() + "\" . } "
	                + "WHERE { BIND(" + insightURI_String + " AS ?insightURI) . "
	                + "BIND(" + spinBodyURI_String + " AS ?spinBodyURI) .}";	                

			try {
				rc.begin();
				Update uq_1 = rc.prepareUpdate(QueryLanguage.SPARQL, query_1);
				Update uq_2 = rc.prepareUpdate(QueryLanguage.SPARQL, query_2);
				uq_1.execute();
				uq_2.execute();
				rc.commit();
                boolReturnValue = true;
			}
			catch ( Exception e ) {
				e.printStackTrace();
				try {
					rc.rollback();
				}
				catch ( Exception ee ) {
					log.warn( ee, ee );
				}
			}
	        return boolReturnValue;
	    }
	    
	    /**    Saves the various Parameter fields to the triple-store on disk.
	     * 
	     * @param insight -- (Insight) An Insight, extracted from the tree-view
	     *    of the Insight Manager.
	     * 
	     * @param parameter -- (Parameter) A Parameter, belonging to the above Insight.
	     *    
	     * @return saveParameter -- (boolean) Whether the save succeeded.
	     */
	    private boolean saveParameter(Insight insight, Parameter parameter){
	    	boolean boolReturnValue = false;
			lngUniqueIdentifier += 1;
			String strUniqueIdentifier = String.valueOf(lngUniqueIdentifier);	   		
		    ValueFactory insightVF = rc.getValueFactory();
            String insightURI_String = "<" + insight.getIdStr() + ">";		
            //We are rebuilding the Constraint URI here, because the designers of VA_MainDB, v20, 
            //decided to reuse Parameters, and we discourage that. No objects on the tree-view
            //should be reused. They all should be editable as unique items:
			String constraintUriName = "constraint-" + strUniqueIdentifier;
	        String constraintURI_String = "<"+ insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, constraintUriName).toString() + ">";				
			String valueTypeUriName = "valueType-" + strUniqueIdentifier;
			String valueTypeURI_String = "<"+ insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, valueTypeUriName).toString() + ">";
		    String predicateUriName = "predicate-" + strUniqueIdentifier;
	        String predicateURI_String = "<" + insightVF.createURI(ARG.NAMESPACE + predicateUriName).toString() + ">";
		    String queryUriName = "query-" + strUniqueIdentifier;
		    String queryURI_String = "<" + insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, queryUriName).toString() + ">";				
			//Make sure that embedded quotes and new-line characters can be persisted:
		    String label = legalizeQuotes(parameter.getLabel());
		    String variable = legalizeQuotes(parameter.getVariable());
	    	String defaultQuery = legalizeQuotes(parameter.getDefaultQuery());
	    	
			//Make sure that embedded new-line characters can be persisted:
			defaultQuery = defaultQuery.replace( "\n", "\\n" );

		   	String query = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		   	    + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
				+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
                + "INSERT{ ?insightURI spin:constraint ?constraintURI ."   
		   		+ "?constraintURI rdfs:label \"" + label + "\" . "
		   		+ "?constraintURI spl:valueType ?valueTypeURI . "
		   		+ "?constraintURI spl:predicate ?predicateURI . "
		   		+ "?predicateURI rdfs:label \"" + variable + "\" . "
		   		+ "?constraintURI sp:query ?queryURI . "
		   		+ "?queryURI sp:text \"" + defaultQuery + "\" .} "
		   		+ "WHERE{ BIND(" + insightURI_String + " AS ?insightURI) . "
		   		+ "BIND(" + constraintURI_String + " AS ?constraintURI) . "
		   		+ "BIND(" + predicateURI_String + " AS ?predicateURI) . "
		   		+ "BIND(" + queryURI_String + " AS ?queryURI) . "
		   		+ "BIND(" + valueTypeURI_String + "AS ?valueTypeURI) .}";

		   	try {
				rc.begin();
				Update uq = rc.prepareUpdate( QueryLanguage.SPARQL, query );
				uq.execute();
				rc.commit();
                boolReturnValue = true;
			}
			catch ( Exception e ) {
				e.printStackTrace();
				try {
					rc.rollback();
				}
				catch ( Exception ee ) {
					log.warn( ee, ee );
				}
			}	    	
	    	return boolReturnValue;
	    }
	    
//---------------------------------------------------------------------------------------------------------
//       I m p o r t   I n - M e m o r y   D a t a   t o   T r i p l e - S t o r e   o n   D i s k		
//---------------------------------------------------------------------------------------------------------
	  
	   /**   Imports all Perspectives, Insights, and Parameters into the database.
	    * 
	    * @return importInsights -- (boolean) Whether the import succeeded.
	    */
	   @Override
	   public boolean importInsights(){
			//Import Insights into the repository:
			boolean boolReturnValue = EngineUtil.getInstance().importInsights(wim);
			
			//This is necessary to make sure that the Insight Manager loads
			//after the left-pane is completely loaded:
	        synchronized(guiUpdateMonitor) {
	        	while(!boolLeftPaneUpdated){
 	               try {
	            	   guiUpdateMonitor.wait();
	            	   
	               }catch(InterruptedException e){
	   	        	   boolLeftPaneUpdated = false;
	               }
	        	}
	        	boolLeftPaneUpdated = false;
	          }
			return boolReturnValue;
	   }

//---------------------------------------------------------------------------------------------------------
//                         I n s i g h t   M a n a g e r   U t i l i t i e s
//---------------------------------------------------------------------------------------------------------

		/**   Prepares a string for use in a dynamic Sparql query, where " and ' are
		 * delimiters. The double-quote, ", is changed to ', and existing single-quotes
		 * are left alone. Also, replaces newline characters, "\n" with "\\n".This utility 
		 * is used thoughout the Insight Manager, where user-editable RDF strings are persisted.
		 *
		 * @param quotedString -- (String) The string containing double and single
		 * quotes.
		 *
		 * @return legalizeQuotes -- (String) The cleaned string, as described above.
		 */
	    @Override
		public String legalizeQuotes(String quotedString) {
			String strReturnValue = quotedString;
	
			strReturnValue = strReturnValue.replace( "\"", "'" ).replace("\n", "\\n");
	
			return strReturnValue;
		}
}
