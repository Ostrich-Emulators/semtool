package gov.va.semoss.rdf.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.openrdf.repository.RepositoryConnection;

import gov.va.semoss.om.Insight;
import gov.va.semoss.rdf.engine.api.IEngine;

public class NameServer {

	// for every class type and relation it tells you which
	// database does this type belong in
	// database-URL has-entity classtype
	// database-URL has-entity relation
	// database-URL search-index URL for search index
	// entity has insight - How do we do entity combinations
	// insight has:label question description
	// insight has:sparql sparql
	// insight uses-database InMemory-DB / URL

	// keeps an in memory store which would be utilized for traverse freely
	RepositoryConnection rc = null;

	// hashtable to keep questions for a given entity type
	// the output is always a vector of questions
	// Vector of strings
	Hashtable<String, Vector> insightHash = new Hashtable<String, Vector>();

	// question id to insight 
	Hashtable <String, Insight> idInsightHash = new Hashtable <String, Insight>();
	
	// label to id
	Hashtable <String, String> labelIdHash = new Hashtable<String, String>();
	
	// database names
	Hashtable <String, IEngine> engineHash = new Hashtable<String, IEngine>();
	
	// You register a database with the name server
	// in this case you register an engine
	// when registered all the questions are parsed out
	// and saved in the above format
	// to be queried

	// there is a separate pane which starts giving you the interested questions
	// very similar to the amazon piece where it says
	// you might be interested in these questions too
	// the question needs to be replaced with actual pieces
	
	

	public void addEngine(IEngine engine) {
		// put it in the hash
		engineHash.put(engine.getEngineName(), engine);
		// get the name of the engine
		// get the ontology / base DB for this engine
		// load it into the in memory
	}	
	

	// gets all the questions tagged for this question type
	public Collection<String> getQuestions(String... entityType) 
	{
    throw new UnsupportedOperationException( "this function was refactored away" );
    
		// get the insights and convert
//		List<String> finalVector = new ArrayList<>();
//    for ( String entityType1 : entityType ) {
//      for( IEngine eng : engineHash.values() ){
//        Collection<String> engineInsights = eng.getInsight4Type( entityType1 );
//        finalVector.addAll( engineInsights );
//      }
//    }
//		return finalVector;
	}
	
	// gets the insight for the given question
	public Insight getInsight(String question)
	{
		// go to the labelIdHash and get the id
		// use the id to get the insight
		
		String id = labelIdHash.get(question);
		Insight reti = idInsightHash.get(id);
		
		return reti;
	}
}
