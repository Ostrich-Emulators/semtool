package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.om.Perspective;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import java.util.Properties;

public class ClusterEngine extends AbstractSesameEngine {

	private static final Logger log = Logger.getLogger( ClusterEngine.class );
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
	private InsightManagerImpl insights;

	// database names
	Map<String, IEngine> engineHash = new HashMap<>();

	public ClusterEngine() {
	}

	@Override
	protected void createRc( Properties props ) {
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository repo = new SailRepository( inferencer );
		try {
			repo.initialize();
			rc = repo.getConnection();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	protected RepositoryConnection getRawConnection() {
		return rc;
	}

	// You register a database with the name server
	// in this case you register an engine
	// when registered all the questions are parsed out
	// and saved in the above format
	// to be queried
	// there is a separate pane which starts giving you the interested questions
	// very similar to the amazon piece where it says
	// you might be interested in these questions too
	// the question needs to be replaced with actual pieces
	public void addEngine( IEngine engine ) {
		// put it in the hash
		engineHash.put( engine.getEngineName(), engine );

		// do the same with insights
		initializeInsightBase();
		InsightManager ie = engine.getInsightManager();
		if ( ie != null ) {
			for ( Perspective p : ie.getPerspectives() ) {
				insights.add( p );
			}
		}
	}

	public void initializeInsightBase() {
		if ( null == insights ) {
			insights = new InsightManagerImpl();
			setInsightManager( insights );
		}
	}

	// the only other thing I really need to be able to do is
	// say pull data from multiple of these engines
	// I will leave the question for now
	// gets all the questions tagged for this question type
	public Collection<String> getQuestions( String... entityType ) {
		throw new UnsupportedOperationException( "this function was refactored away" );

// get the insights and convert
//    List<String> finalVector = new ArrayList<>();
//    for ( String entityType1 : entityType ) {
//      for ( IEngine eng : engineHash.values() ) {
//        Collection<String> engineInsights = eng.getInsight4Type( entityType1 );
//        // need to capture this so that I can relate this back to the engine when selected
//        if ( engineInsights == null ) {
//          finalVector.addAll( engineInsights );
//        }
//      }
//    }
//    return finalVector;
	}
}
