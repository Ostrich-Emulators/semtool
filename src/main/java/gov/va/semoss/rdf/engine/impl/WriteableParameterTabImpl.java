package gov.va.semoss.rdf.engine.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;

import gov.va.semoss.model.vocabulary.ARG;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.api.WriteableParameterTab;
import gov.va.semoss.rdf.engine.api.WriteablePerspectiveTab;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;

public class WriteableParameterTabImpl implements WriteableParameterTab {
	  private WriteableInsightManager wim;
	  private RepositoryConnection rc;
	  private static final Logger log = Logger.getLogger(WriteablePerspectiveTab.class);
	  
	  public WriteableParameterTabImpl(WriteableInsightManagerImpl wim){
		  this.wim = wim;
		  this.rc = wim.getRawConnection();
	  }

	  /**   Saves the current Parameter to the database.
	   * 
       * @param insight -- (Insight) Insight containing the Parameter to delete.
	   * @param parameter -- (Parameter) Parameter to be saved
	   * 
	   * @return saveParameter -- (boolean) Whether the Parameter was saved ok.
	   */
	  @Override
	  public boolean saveParameter(Insight insight, Parameter parameter){
		  boolean boolParameterSaved = false;
		  
		  String insightUriString = "<" + insight.getId().stringValue().trim() + ">";
		  String parameterUriString = "<" + parameter.getParameterURI().trim() + ">";
		  String label = parameter.getLabel().trim();
		  String variable = parameter.getVariable().trim();
		  String valueType = parameter.getValueType().trim();
		  String defaultQuery = parameter.getDefaultQuery().trim();

		  //Make sure that embedded new-line characters can be persisted:
          defaultQuery = defaultQuery.replace("\n", "\\n"); 
          
	      ValueFactory insightVF = rc.getValueFactory();
	      String strUniqueIdentifier = String.valueOf(System.currentTimeMillis());
	      UriBuilder uriBuilder;  		  
          String predicateUriString = "<" + insightVF.createURI(ARG.NAMESPACE + "predicate-" + 
             strUniqueIdentifier) + ">";
		  uriBuilder = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
          String queryUriString = "<" + uriBuilder.add("query-" + strUniqueIdentifier).build() + ">";				
		  
		  String query_1 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
		      + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
			  + "DELETE{ ?queryURI ?p ?o .} "
			  + "WHERE{ " + parameterUriString + " sp:query ?queryURI . "
			  + insightUriString + " spin:constraint " + parameterUriString + " . " 
			  + "?queryURI ?p ?o .}";
				  	  		  
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
			  + "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
			  + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
			  + "INSERT{ " + parameterUriString + " rdfs:label \"" + label + "\" . " 
			  + parameterUriString + " spl:predicate " + predicateUriString + " . "
			  + predicateUriString + " rdfs:label \"" + variable + "\" . "
			  + parameterUriString + " spl:valueType <" + valueType + "> . "
			  + parameterUriString + " a spl:Argument .} "
			  + "WHERE { " + insightUriString + " spin:constraint " + parameterUriString + " .}";
          
           String query_5 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
    		  + "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
    		  + "INSERT{ " + parameterUriString + " sp:query " + queryUriString + " . "
    		  + queryUriString + " sp:text \"" + defaultQuery + "\" .} "
			  + "WHERE { " + insightUriString + " spin:constraint " + parameterUriString + " .}";


		  try{
		      rc.begin();
		      
	          if(label.equals("") == false && variable.equals("") == false && 
	        	 valueType.equals("") == false){
	             Update uq_1 = rc.prepareUpdate(QueryLanguage.SPARQL, query_1);
	             Update uq_2 = rc.prepareUpdate(QueryLanguage.SPARQL, query_2);
		         Update uq_3 = rc.prepareUpdate(QueryLanguage.SPARQL, query_3);
	   	         Update uq_4 = rc.prepareUpdate(QueryLanguage.SPARQL, query_4);
	             uq_1.execute();
	             uq_2.execute();	          
	             uq_3.execute();	             
	             uq_4.execute();
	             
	             if(defaultQuery.equals("") == false){
		   	        Update uq_5 = rc.prepareUpdate(QueryLanguage.SPARQL, query_5);
		            uq_5.execute();	            	
	             }
	          }	          
	          rc.commit();

	          //Import Parameters into the repository:
	          boolParameterSaved = EngineUtil.getInstance().importInsightsFromList(rc.getStatements(null, null, null, false));
			        
		  }catch(Exception e){
			  e.printStackTrace();
			  try{
			      rc.rollback();
			  }catch ( Exception ee ) {
			      log.warn( ee, ee );
			  }
		  }		  
		  return boolParameterSaved;
	  }

}//End "WriteableParameterTabImpl" class.
