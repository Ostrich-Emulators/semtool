package gov.va.semoss.rdf.engine.impl;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
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
import gov.va.semoss.rdf.engine.api.WriteableInsightTab;
import gov.va.semoss.rdf.engine.api.WriteablePerspectiveTab;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.util.GuiUtility;
import java.util.List;

public class WriteableInsightTabImpl implements WriteableInsightTab {
	  private WriteableInsightManager wim;
	  private RepositoryConnection rc;
	  private static final Logger log = Logger.getLogger(WriteablePerspectiveTab.class);
	  
	  public WriteableInsightTabImpl(WriteableInsightManagerImpl wim){
		  this.wim = wim;
		  this.rc = wim.getRawConnection();
	  }

	  /**   Adds a dummy Parameter to the current Insight in the database.
	   * 
	   * @param insight -- (Insight) Insight to which new Parameter will be added.
	   */
	  @Override
	  public boolean addParameter(Insight insight){
		  boolean boolReturnValue = false;
	      ValueFactory insightVF = rc.getValueFactory();
	      String strUniqueIdentifier = String.valueOf(System.currentTimeMillis());
	      
		  try{
	          rc.begin();
	          
	          URI insightURI = insight.getId();				
              String constraintUriName = "constraint-" + strUniqueIdentifier;
			  URI constraintURI = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, constraintUriName);				
			  String valueTypeUriName = "valueType-" + strUniqueIdentifier;
	          URI valueTypeURI = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, valueTypeUriName);
	          String predicateUriName = "predicate-" + strUniqueIdentifier;
              URI predicateURI = insightVF.createURI(ARG.NAMESPACE + predicateUriName);
			  String queryUriName = "query-" + strUniqueIdentifier;
	          URI queryURI = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, queryUriName);				
	   
              rc.add( insightURI, SPIN.constraint, constraintURI );	          
	          rc.add( constraintURI, RDFS.LABEL, insightVF.createLiteral("New Parameter " + strUniqueIdentifier) );
	          rc.add( constraintURI, SPL.valueType, valueTypeURI);
	          rc.add( constraintURI, SPL.predicate, predicateURI);
	          rc.add( predicateURI, RDFS.LABEL, insightVF.createLiteral("newParameter-" + strUniqueIdentifier));
	          rc.add( constraintURI, SP.query, queryURI);
	          rc.add( queryURI, SP.text, insightVF.createLiteral(""));

	          rc.commit();
	          
	          //Import Insights into the repository:
	          boolReturnValue = EngineUtil.getInstance().importInsights( wim );
	          //Give the left-pane drop-downs enough time to refresh from the import:
		      Thread.sleep(2000);
		         
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
	  
	  /**   Deletes a Parameter from an Insight in the triple-store on disk.
	   * 
       * @param insight -- (Insight) Insight containing the Parameter to delete.
       * 
       * @param parameter -- (Parameter) Parameter to delete.
       */
	  @Override
	  public boolean deleteParameter(Insight insight, Parameter parameter){
		  boolean boolReturnValue = false;
		  
		  String insightUriString = "<" + insight.getId() + ">";
		  String parameterUriString = "<" + parameter.getParameterURI() + ">";
		  
		  String query_1 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
			  + "DELETE{ ?query ?p ?o .} "
			  + "WHERE{ " + parameterUriString + " sp:query ?query . "
			  + insightUriString + " spin:constraint " + parameterUriString + " . " 
			  + "?query ?p ?o .}";
				  	  		  
		  String query_2 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			  + "DELETE{ ?predicate ?p ?o .} "
			  + "WHERE{ " + parameterUriString + " spl:predicate ?predicate . "
			  + insightUriString + " spin:constraint " + parameterUriString + " . " 
			  + "?predicate ?p ?o .}";
				  	  
		  String query_3 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "DELETE{ " + parameterUriString + " ?p ?o .} "
		      + "WHERE{ " + insightUriString + " spin:constraint " + parameterUriString + " . "
		      + parameterUriString + " ?p ?o .}";

		  String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
			  + "DELETE{ " + insightUriString + " spin:constraint " + parameterUriString + " .} "
			  + "WHERE{ " + insightUriString + " spin:constraint " + parameterUriString + " .}";

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
	                  
             //Import Insights into the repository:
	          boolReturnValue = EngineUtil.getInstance().importInsights( wim );
	         //Give the left-pane drop-downs enough time to refresh from the import:
		     Thread.sleep(2000);

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
	  @Override
	  public boolean deleteInsight(List<Insight> arylInsights, Insight insight, Perspective perspective){
		  boolean boolReturnValue = false;

		  //Adjust orders of all Insights under the current Perspective
	      //as if this Insight has been removed:
		  arylInsights.remove(insight);
		  for(int i = 0; i < arylInsights.size(); i++){
			 arylInsights.get(i).setOrder(perspective.getUri().toString(), i + 1);
		  }
		  
		  String query_1 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
		      + "DELETE{ ?perspective olo:slot ?slot .} "
		      + "WHERE{ ?slot olo:item <" + insight.getIdStr() + "> . "
		      + "?perspective olo:slot ?slot .}";

	      String query_2 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
	    	  + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
		      + "DELETE{ ?query ?p ?o .} "
		   	  + "WHERE{ <" + insight.getIdStr() + "> spin:constraint ?constraint . "
		   	  + "?constraint sp:query ?query . "
		   	  + "?query ?p ?o .}";

	      String query_3 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			  + "DELETE{ ?predicate ?p ?o .} "
			  + "WHERE{ <" + insight.getIdStr() + "> spin:constraint ?constraint . "
			  + "?constraint spl:predicate ?predicate . "
			  + "?predicate ?p ?o .} ";

	      String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
	       	  + "DELETE{ ?constraint ?p ?o .} "
	      	  + "WHERE{ <" + insight.getIdStr() + "> spin:constraint ?constraint . "
	      	  + "?constraint ?p ?o .} ";

	      String query_5 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
	    	  + "DELETE{ ?body ?p ?o .} "
	   	      + "WHERE{ <" + insight.getIdStr() + "> spin:body ?body . "
	   	      + "?body ?p ?o .} ";
	   	       
		  String query_6 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
			  + "DELETE{ <" + insight.getIdStr() + "> ?p ?o .} "
			  + "WHERE{ <" + insight.getIdStr() + "> ?p ?o .} ";

		  String query_7 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
			  + "DELETE{ ?slot ?p ?o .} "
			  + "WHERE{ ?slot olo:item <" + insight.getIdStr() + "> . "
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
	         
	         //Reorder Insights under the current Perspective:
	         boolReturnValue = wim.getWriteablePerspectiveTab().reorderInsights(perspective.getUri(), arylInsights);
	         
	         //Import Insights into the repository if deletion succeeded
	         //and Insights have been reordered:
	         if(boolReturnValue){
	          boolReturnValue = EngineUtil.getInstance().importInsights( wim );
	         }
	         //Give the left-pane drop-downs enough time to refresh from the import:
		     Thread.sleep(2000);
	         
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
	  @Override
	  public boolean saveInsight(List<Insight> arylInsights, Insight insight,
		Collection<Perspective> colPerspectivesToAddInsight, Collection<Perspective> colPerspectivesToRemoveInsight){
		  boolean boolTriplesImportedToDb = false;
		  boolean boolInsightPerspectivesSaved = false;
		  String insightURI_String = "<" + insight.getIdStr() + ">";
		  String question = insight.getLabel();
		  String dataViewOutput = insight.getOutput();
		  String rendererClass = insight.getRendererClass();
		  String isLegacy = String.valueOf(insight.isLegacy());
		  String sparql = insight.getSparql();
		  String description = insight.getDescription();

		  //Make sure that embedded new-line characters can be persisted:
		  sparql = sparql.replace("\n", "\\n"); 
		  description = description.replace("\n", "\\n");
				  
		  String creator = wim.userInfoFromToolPreferences(insight.getCreator());		  
	      ValueFactory insightVF = rc.getValueFactory();
	      String modified = insightVF.createLiteral(new Date()).stringValue();
		  		  
		  String query = "PREFIX " + UI.PREFIX + ": <" + UI.NAMESPACE + "> "
             + "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
             + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
             + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
             + "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
             + "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
             + "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
             + "DELETE{ ?insightURI rdfs:label ?question . "
             + "?insightURI ui:dataView ?dataViewOutput . "
             + "?insightURI vas:rendererClass ?rendererClass . "
             + "?insightURI vas:isLegacy ?isLegacy . "
             + "?body sp:text ?sparql . "
             + "?insightURI dcterms:description ?description . "
             + "?insightURI dcterms:creator ?creator . "
             + "?insightURI dcterms:modified ?modified .} "
             + "INSERT{ ?insightURI rdfs:label \"" + question + "\" . "
             + "?insightURI ui:dataView vas:" + dataViewOutput + " . "
             + "?insightURI vas:rendererClass \"" + rendererClass + "\" . "
             + "?insightURI vas:isLegacy " + isLegacy + " . "
             + "?body sp:text \"" + sparql + "\" . "
             + "?insightURI dcterms:description \"" + description + "\" . "
             + "?insightURI dcterms:creator \"" + creator + "\" . "
             + "?insightURI dcterms:modified \"" + modified + "\"^^xsd:dateTime . } "
             + "WHERE { BIND(" + insightURI_String + " AS ?insightURI) . "
             + "?insightURI rdfs:label ?question . "
             + "?insightURI ui:dataView ?dataViewOutput . "
             + "OPTIONAL{ ?insightURI vas:rendererClass ?rendererClass } "
             + "OPTIONAL{ ?insightURI vas:isLegacy ?isLegacy } "
             + "OPTIONAL{ ?insightURI spin:body ?body . "
             + "?body sp:text ?sparql } "
             + "OPTIONAL{ ?insightURI dcterms:description ?description } "
             + "OPTIONAL{ ?insightURI dcterms:creator ?creator } "
             + "OPTIONAL{ ?insightURI dcterms:modified ?modified } } ";

          try{
		      rc.begin();
	          Update uq = rc.prepareUpdate(QueryLanguage.SPARQL, query);
	          uq.execute();
	          rc.commit();

			  //Save Perspective selections for this Insight:
	          boolInsightPerspectivesSaved = saveInsightPerspectives(arylInsights, insight, 
	        	 colPerspectivesToAddInsight, colPerspectivesToRemoveInsight);

	          //Import Insights into the repository:
	          boolTriplesImportedToDb = EngineUtil.getInstance().importInsights( wim );
	          //Give the left-pane drop-downs enough time to refresh from the import:
		      Thread.sleep(2000);
			        
		  }catch(Exception e){
			  e.printStackTrace();
			  try{
			      rc.rollback();
			  }catch ( Exception ee ) {
			      log.warn( ee, ee );
			  }
		  }
		  return boolInsightPerspectivesSaved && boolTriplesImportedToDb;
	  }
	  
	  /**   Persists selections on the "Perspective" list-view on the "Insight" tab. If several items are selected
	   * upon load, then de-selecting an item, causes the Insight to be removed from that Perspective (if it is not
	   * the only Insight under the Perspective). Selecting a new item causes the Insight to be added to that 
	   * Perspective.
	   * 
	   * Note: This method is intended to be called from "saveInsight(...)", and therefore does not update the 
	   *       database on disk, leaving that up to the caller.
       * 
       * @param arylInsights - (ArrayList<Insight>) All Insights under the current
       *     Perspective.
	   * 
	   * @param insight -- (Insight) The current Insight being veiwed on the "Insight" tab.
	   * 
	   * @param colPerspectivesToAddInsight -- (Collection<Perspective>) A collection of Perspectives to which the
	   *   above Insight must be added.
	   *   
	   * @param colPerspectivesToRemoveInsight -- (Collection<Perspective>) A collection of Perspectives where the
	   *   above Insight must be removed.
	   *   
	   * @return saveInsightPerspectives -- (boolean) Whether the Insight could be moved between the specified
	   *    Perspectives.
	   */
	  private boolean saveInsightPerspectives(List<Insight> arylInsights, Insight insight, 
		Collection<Perspective> colPerspectivesToAddInsight, Collection<Perspective> colPerspectivesToRemoveInsight){
		  boolean boolReturnValue = true;
		  
		  for(Perspective perspective: colPerspectivesToRemoveInsight){
			  if(wim.getWriteablePerspectiveTab().removeInsight(arylInsights, insight, perspective, false) == false){
                  GuiUtility.showWarningOkCancel("WARNING: Insight, \""+insight.getLabel()
                	 +"\",\ncould not be removed from Perspective, \""+perspective.getLabel()+"\"");
                  boolReturnValue = false;
			  }
		  }
		  for(Perspective perspective: colPerspectivesToAddInsight){
			  if(addExistingInsight(insight, perspective) == false){
                  GuiUtility.showWarningOkCancel("WARNING: Insight, \""+insight.getLabel()
                	 +"\",\ncould not be added to the Perspective, \""+perspective.getLabel()+"\"");
                  boolReturnValue = false;
			  }
		  }		  
		  return boolReturnValue;
	  }
	  
	  /**   Adds an existing Insight to the passed-in Perspective and to the triple-store on disk.
	   *
	   * @param insight -- (Insight) An Insight to add to the passed-in Perspective.
	   *
	   * @param perspective -- (Perspective) Perspective to which the Insight must be added.
	   * 
	   * @return addPerspective -- (boolean) Whether the save to disk succeeded.
	   */
	  @Override
	  public boolean addExistingInsight(Insight insight, Perspective perspective){
		  boolean boolReturnValue = false;
	      ValueFactory insightVF = rc.getValueFactory();
	      String strUniqueIdentifier = String.valueOf(System.currentTimeMillis());

		  try{
	          rc.begin();
	          
	          URI perspectiveURI = perspective.getUri();				
			  String slotUriName = perspective.getLabel() + "-slot-" + strUniqueIdentifier;
	          URI slot = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, slotUriName);				
	          rc.add( perspectiveURI, OLO.slot, slot );

	          rc.add( slot, OLO.item, insight.getId() );
	          int newIndex = wim.getWriteablePerspectiveTab().getNextInsightIndex(perspectiveURI);
	          rc.add( slot, OLO.index, insightVF.createLiteral(newIndex));          
	          
	          rc.commit();
              boolReturnValue = true;
	          
		  }catch(Exception e){
			  e.printStackTrace();
		      try{
		          rc.rollback();
		      }catch ( Exception ee ) {
		          log.warn( ee, ee );
		      }
		  }
	 	  return boolReturnValue;
	  }
	  
}//End WriteableInsightManager class.
