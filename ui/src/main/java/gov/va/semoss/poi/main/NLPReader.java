package gov.va.semoss.poi.main;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;

public class NLPReader { //extends AbstractFileReader {

  private static final Logger log = Logger.getLogger( NLPReader.class );

  public void createNLPrelationships( ArrayList<TripleWrapper> Triples,
      RepositoryConnection rc ) throws Exception {

		throw new UnsupportedOperationException( "This function needs a complete refactor" );
		
//    Map<String, Value> temp = new HashMap<>();
//    Map<String, Value> temp2 = new HashMap<>();
//    Map<String, Value> temp3 = new HashMap<>();
//
//    ValueFactory vf = rc.getValueFactory();
//    for ( TripleWrapper Triple : Triples ) {
//      temp.put( "occurance", vf.createLiteral( Triple.getObj1num() ) );
//      temp2.put( "occurance", vf.createLiteral( Triple.getPrednum() ) );
//      temp3.put( "occurance", vf.createLiteral( Triple.getObj2num() ) );
//      createRelationship( "subject", "predicate", Triple.getObj1(), Triple.getPred(), "subjectofpredicate", temp, rc );
//      createRelationship( "predicate", "object", Triple.getPred(), Triple.getObj2(), "predicateofobject", temp, rc );
//      createRelationship( "object", "subject", Triple.getObj2(), Triple.getObj1(), "objectofsubject", temp2, rc );
//      addNodeWithProperties( "subject", Triple.getObj1(), temp, rc );
//      addNodeWithProperties( "predicate", Triple.getPred(), temp2, rc );
//      addNodeWithProperties( "object", Triple.getObj2(), temp3, rc );
//      temp.remove( "occurance" );
//      temp2.remove( "occurance" );
//      temp3.remove( "occurance" );
//      createRelationship( "subject", "subjectexpanded", Triple.getObj1(), Triple.getObj1exp(), "expandedofsubject", temp, rc );
//      createRelationship( "predicate", "predicateexpanded", Triple.getPred(), Triple.getPredexp(), "expandedofpredicate", temp, rc );
//      createRelationship( "object", "objectexpanded", Triple.getObj2(), Triple.getObj2exp(), "expandedofobject", temp, rc );
//      createRelationship( "subject", "articlenum", Triple.getObj1(), Triple.getArticleNum(), "articleofsubject", temp, rc );
//      createRelationship( "predicate", "articlenum", Triple.getPred(), Triple.getArticleNum(), "articleofpredicate", temp, rc );
//      createRelationship( "object", "articlenum", Triple.getObj2(), Triple.getArticleNum(), "articleofobject", temp, rc );
      //	createRelationship("subject", "sobjectcount", Triples.get(i).getObj1(), Triples.get(i).getObj1num(),"numofsubject", temp);
      //	createRelationship("predicate", "predicatecount", Triples.get(i).getPred(), Triples.get(i).getPrednum(),"numofpredicate", temp);
      //	createRelationship("object", "objectcount", Triples.get(i).getObj2(), Triples.get(i).getObj2num(),"numofobject", temp);
//    }
  }
}
