package gov.va.semoss.ui.helpers;

import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

/**   Builds selected parameter values into a non-legacy Insight query.
 * 
 * @author Thomas
 *
 */
public class NonLegacyQueryBuilder {
    /**   Builds selected parameter values into a non-legacy Insight query, 
     * and returns the filled query. If the "paramHash" is empty, then the
     * passed-in query will be returned unaltered.
     * 
     * @param query -- (String) Sparql query in the non-legacy format, where 
     *    certain variables may have external bindings (in "paramHash"), and
     *    contains no "<@...@>" expressionns.
     *    
     * @param paramHash (Map<String, String>) Key/value pairs where the key 
     *    is a variable name occurring in "query", and the value is a String
     *    URI.
     *    
     * @return buildNonLegacyInsightQuery -- (String) The "query" with certain
     *    variables replaced by URI's from "paramHash". 
     *    
     *    WARNING: A run-time exception will occur if a key from "paramHash"
     *             occurs in the SELECT or CONSTRUCT clause.
     */
	public static String buildNonLegacyInsightQuery(String query, Map<String, String> paramHash){
        QueryExecutorAdapter<String> queryExer = new QueryExecutorAdapter<String>(){
 	      @Override
 	      public void handleTuple(BindingSet set, ValueFactory fac){}
 	   };
        queryExer.setSparql(query);
 	   Map<String, String> map = new HashMap<>( paramHash );
 	   for ( Map.Entry<String, String> e : map.entrySet() ) {
 		  String key = e.getKey();
 		  String value = e.getValue();
 	      queryExer.bindURI(key, value);
 	   }
 	   return queryExer.bindAndGetSparql();
 	}

}
