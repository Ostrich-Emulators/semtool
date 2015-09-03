package gov.va.semoss.ui.components.playsheets.helpers;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.impl.SesameJenaImposter;
import gov.va.semoss.util.MultiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

public class DupeFunctions {

	private static final Logger logger = Logger.getLogger( DupeFunctions.class );

	private final IEngine engine;

	public DupeFunctions( IEngine eng ) {
		engine = eng;
	}

	//method for setting query data
	private List<Object[]> createTableForQuery( String query ) {
		SesameJenaImposter qe = new SesameJenaImposter( query );
		try {
			List<Object[]> list = engine.query( qe );
			return list;
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}
		return new ArrayList<>();
	}

	//this duplication will first ask for comparison objectList
	public List<String> createComparisonObjectList( String query ) {
		List<String> retList = new ArrayList<>();
		//first query returns one column
		for ( Object[] listElement : createTableForQuery( query ) ) {
			String comparisonObjectName = (String) listElement[0];
			retList.add( comparisonObjectName );
		}
		Collections.sort( retList );

		return retList;
	}

	// this is basic comparison for duplication
	// if entity A is being compared to entity B on a given parameter,
	// if entity A has param a b and c and entity B has param a and c,
	// the duplication of entity B to entity A is 0.67
	public Hashtable<String, Hashtable<String, Double>> compareObjectParameterScore(
			String query, List<String> dupeObjectList ) {
		//create query results
		Hashtable<String, Hashtable<String, Double>> dataRetHash = new Hashtable<>();
		MultiMap<String, String> dataStoreHash = new MultiMap<>();
		//first store info into hashtable for each entity that is being evaluated for duplication
		for ( Object[] listRow : createTableForQuery( query ) ) {
			String objectName = (String) listRow[0];
			String elementName = (String) listRow[1];
			dataStoreHash.add( objectName, elementName );
		}

		//loop through the list and create the base hashtables for duplication data
		for ( String objectName1 : dupeObjectList ) {
			if ( dataStoreHash.containsKey( objectName1 ) ) {
				List<String> objectList1 = dataStoreHash.getNN( objectName1 );
				Hashtable<String, Double> objectCompareScoreHash = new Hashtable<>();
				double totalParamCount = objectList1.size();

				//use the hashtable that was arleady stored previously
				for ( Entry<String, List<String>> objectArrayEntry : dataStoreHash.entrySet() ) {
					//now compare all other entities with this given entity and come up with the score
					String objectName2 = objectArrayEntry.getKey();
					List<String> objectList2 = objectArrayEntry.getValue();

					double matchParamCount = 0;
					for ( String param : objectList1 ) {
						if ( objectList2.contains( param ) ) {
							matchParamCount++;
						}
					}
					//calculate score and put in hash
					objectCompareScoreHash.put( objectName2, matchParamCount / totalParamCount );
					dataRetHash.put( objectName1, objectCompareScoreHash );
				}
			}
		}
		return dataRetHash;
	}
}
