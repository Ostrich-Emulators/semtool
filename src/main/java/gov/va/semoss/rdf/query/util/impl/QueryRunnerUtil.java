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
package gov.va.semoss.rdf.query.util.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.DIHelper;

/**
 * This is a convenience class for quickly running debug queries
 */
public class QueryRunnerUtil {
	private static final Logger logger = Logger.getLogger(QueryRunnerUtil.class);

	public static void runQueryForSubAndPredGivenAnObj(String object) {
		String query = 
				"SELECT DISTINCT ?subject ?predicate WHERE {" +
				"  {?subject ?predicate \"" + object + "\";}" +
				"} ORDER BY ?subject ?predicate";

		runQueryAndDisplayResults(query);
	}
	
	public static void runQueryForPredAndObjGivenAnSub(String subject) {
		String query = 
				"SELECT DISTINCT ?predicate ?object WHERE {" +
				"  {\"" + subject + "\" ?predicate ?object  ;}" +
				"} ORDER BY ?predicate ?object";

		runQueryAndDisplayResults(query);
	}
	
	public static void runQueryAndDisplayResults(String query) {
		int resultNumber = 1;
		List<Map<String,String>> theResults = runQuery(query);
		for (Map<String,String> thisRow:theResults) {
			logger.debug("Query result #" + resultNumber++ + ":");
			for (String variableName:thisRow.keySet()) {
				String value = thisRow.get(variableName);
				logger.debug(variableName + ": " + value);
			}
		}
	}
	
	public static List<Map<String,String>> runQuery(String query) {
		try {
			IEngine engine = DIHelper.getInstance().getRdfEngine();
			if( null == engine){
				logger.warn("Could not get engine ");
				return null;
			}
	
			logger.debug("running query: " + query);
			return engine.query( ListOfMapsQueryAdapterImpl.forStrings( query) );
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			logger.warn("Could not run query: " + e, e);
			return null;
		}
	}
}
