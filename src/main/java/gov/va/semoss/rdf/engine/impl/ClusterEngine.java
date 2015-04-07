package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import java.util.List;

public class ClusterEngine extends AbstractEngine {

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
	RepositoryConnection owlRc = null;
	private WriteableInsightManager insights;

	// database names
	Map<String, IEngine> engineHash = new HashMap<>();

	public ClusterEngine() {
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository owlRepo = new SailRepository( inferencer );
		try {
			owlRepo.initialize();
			owlRc = owlRepo.getConnection();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

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

		// get the base owl file
		// get the name of the engine
		// get the ontology / base DB for this engine
		// load it into the in memory
		Collection<Statement> stmts = engine.getOwlData();
		try {
			owlRc.add( stmts );
		}
		catch ( RepositoryException ex ) {
			//ignored
		}

		// do the same with insights
		initializeInsightBase();
		InsightManager ie = engine.getInsightManager();
		if ( ie != null ) {
			for ( Perspective p : ie.getPerspectives() ) {
				insights.add( p );
				List<Insight> ins = ie.getInsights( p );
				insights.setInsights( p, ins );
			}
			insights.commit();
		}
	}

	public void initializeInsightBase() {
		if ( null == insights ) {
			insights = new WriteableInsightManagerImpl( getInsightManager() ) {
				@Override
				public void commit() {
					log.warn( "commit means nothing here" );
				}
			};
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

	@Override
	public Collection<URI> getEntityOfType( String sparqlQuery ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean execAskQuery( String query ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Object execGraphQuery( String query ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Object execSelectQuery( String query ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public ENGINE_TYPE getEngineType() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public InsightManager getInsightManager() {
		return insights;
	}
}
