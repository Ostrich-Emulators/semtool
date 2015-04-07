package gov.va.semoss.rdf.util;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.sparql.GraphPattern;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.repository.sparql.query.SPARQLQueryBindingSet;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectWrapper;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SPARQLParse {

	private static final Logger log = Logger.getLogger( SPARQLParse.class );
	List<StatementPattern> patterns = null;
	Map<String, Object> sourceTarget = new HashMap<>();
	Map<String, Object> constantHash = new HashMap<>();
	public RepositoryConnection rc = null;
	SailConnection sc = null;
	ValueFactory vf = null;
	IEngine engine = null;
	IEngine bdEngine = null;

	public void executeQuery( String query, IEngine engine ) {
		//Select logic goes here
		try {
			SesameJenaSelectWrapper wrapper = new SesameJenaSelectWrapper();
			wrapper.setEngine( engine );
			wrapper.setQuery( query );
			wrapper.executeQuery();
			wrapper.getVariables();
			while ( wrapper.hasNext() ) {
				SesameJenaSelectStatement stmt = wrapper.next();
				log.debug( "Binding " + stmt.getRawPropHash() );
				generateTriple( stmt.getRawPropHash() ); // recreate the stuff in the memory including putting the OWL into it
			}
		}
		catch ( SailException e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}
		//*/

	}

	public void exportToFile() {
		try {
			String output = DIHelper.getInstance().getProperty( Constants.BASE_FOLDER ) + "/" + "output.xml";
			rc.export( new RDFXMLPrettyWriter( new FileWriter( output ) ) );
		}
		catch ( IOException | RepositoryException | RDFHandlerException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}

		log.debug( "Export complete" );
	}

	public void loadBaseDB( String propFile ) {
		try {
			//Properties prop = new Properties();
			//prop.load(new FileInputStream(propFile));

			// get the owl file
			//String owler = DIHelper.getInstance().getProperty(Constants.BASE_FOLDER) + "/"
			//		+ prop.get(Constants.OWL) + "";
			String owler = "C:\\Users\\pkapaleeswaran\\workspacej\\FoxHole4/db/TAP_Core_Data/TAP_Core_Data_OWL.OWL";
			rc.add( new FileInputStream( owler ), "http://semoss.org",
					RDFFormat.RDFXML );

		}
		catch( IOException | RDFParseException | RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}

	}

	public void testIt( String query ) {
		SesameJenaSelectWrapper wrapper = new SesameJenaSelectWrapper();
		wrapper.setEngine( engine );
		wrapper.setQuery( query );
		wrapper.executeQuery();
		wrapper.getVariables();
		while ( wrapper.hasNext() ) {
			SesameJenaSelectStatement stmt = wrapper.next();
			log.debug( "Binding " + stmt.getRawPropHash() );
			// parse.generateTriple(stmt.rawPropHash);
		}

	}

	public void createRepository() {
		try {
			Repository myRepository2 = new SailRepository(
					new ForwardChainingRDFSInferencer( new MemoryStore() ) );
			myRepository2.initialize();
			rc = myRepository2.getConnection();
			sc = ( (SailRepositoryConnection) rc ).getSailConnection();

			engine = new InMemorySesameEngine();
			( (InMemorySesameEngine) engine ).setRepositoryConnection( rc );
			vf = rc.getValueFactory();
		}
		catch ( RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}
	}

	public void parseIt( String query ) {
		try {
			SPARQLParser parser = new SPARQLParser();

			log.debug( "Query is " + query );
			ParsedQuery query2 = parser.parseQuery( query, null );
			log.debug( ">>>" + query2.getTupleExpr() );
			StatementCollector collector = new StatementCollector();
			query2.getTupleExpr().visit( collector );

			patterns = collector.getPatterns();
			sourceTarget.clear();
			constantHash.clear();
			sourceTarget.putAll( collector.sourceTargetHash );
			constantHash.putAll( collector.constantHash );
			log.debug( "Constants " + constantHash );
		}
		catch ( Exception e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}

	}

	public void generateTriple( Map<String, Object> binds ) throws SailException {
		for ( int index = 0; index < patterns.size(); index++ ) {
			StatementPattern pattern = patterns.get( index );
			//log.debug("-----------");
			createTriple( pattern, binds, engine );
		}
	}

	// binds tells me all the current bindings
	public void createTriple( StatementPattern pattern, Map<String, Object> binds, IEngine rc )
			throws SailException {
		// get the pattern to get the subject, predicate and object
		Object subject, predicate, object;
		// get the subject
		// typically if the value is available then it should be the value
		// else it is the name of the pattern

		subject = pattern.getSubjectVar().getValue();
		if ( subject == null ) {
			subject = pattern.getSubjectVar().getName();
		}
		// log.debug(pattern.getSubjectVar().getName() + "<>" +
		// pattern.getSubjectVar().getValue() );
		// if(pattern.getSubjectVar().getValue() == null)
		// log.debug(subject.getClass());

		// predicate
		predicate = pattern.getPredicateVar().getValue();
		if ( predicate == null ) {
			predicate = pattern.getPredicateVar().getName();
		}
		// log.debug(pattern.getPredicateVar().getName() + "<>"
		// +pattern.getPredicateVar().getValue() + "<>" +
		// pattern.getPredicateVar().isAnonymous());
		// log.debug(predicate.getClass());

		object = pattern.getObjectVar().getValue();
		if ( object == null ) {
			object = pattern.getObjectVar().getName();
		}
		// log.debug(pattern.getObjectVar().getName()+ "<>" +
		// pattern.getObjectVar().getValue());
		// log.debug(object.getClass());

		// now I need to check to see if the subject, predicate or object
		Object subjectT = returnBinding( subject + "", binds );
		Object predicateT = returnBinding( predicate + "", binds );
		Object objectT = returnBinding( object + "", binds );
		if ( subjectT != null && predicateT != null && objectT != null ) {
			if ( !( subjectT.toString().startsWith( "http://" ) ) ) {
				subjectT = new URIImpl( "semoss:" + subjectT );
			}
			if ( !( predicateT.toString().startsWith( "http://" ) ) ) {
				predicateT = "semoss:" + predicateT;
			}
			if ( objectT.toString().startsWith( "http://" ) ) {
				objectT = new URIImpl( objectT + "" );
			}
			else if ( objectT.toString().equalsIgnoreCase( object + "" ) ) {
				objectT = new URIImpl( "semoss:" + objectT );
			}

			// if(objectT instanceof String)
			// objectT = new U
			/*log.debug("TRIPLE " + subjectT + "<>" + predicateT + "<>"
			 + objectT + "<>" + objectT.getClass()
			 + (objectT instanceof Literal));
			 */
			// Statement stmt = new StatementImpl(vf.createURI(subjectT+""),
			// vf.createURI(predicateT+""), (Value) objectT);
			// rc.
			// sc.addStatement(vf.createURI((String) subjectT),
			// vf.createURI((String) predicateT), (Value)objectT);
			rc.addStatement( subjectT + "", predicateT + "", objectT, true );
		}
	}

	public Object returnBinding( String source, Map<String, Object> binding ) {
		if ( sourceTarget.containsKey( source ) ) {
			String target = (String) sourceTarget.get( source );
			// get the value from binding
			if ( binding.containsKey( target ) ) {
				return binding.get( target );
			}
			else if ( constantHash.containsKey( source ) ) {
				return constantHash.get( source );
			}
		}
		return source;
	}

	public void testQueryGen() {

		try {
			Var systemName = new Var( "x" );
			Var typeRelation = new Var( "y", vf.createURI( "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" ) );// , factory.createURI(...));
			//URI y1 = vf
			//	.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Var systemType = new Var( "z", vf.createURI( "http://semoss.org/ontologies/Concept/System" ) );
			//z = (Var) vf.createURI("http://semoss.org/ontologies/Concept/System");
			Var p = new Var( "p" );
			Var w = new Var( "w" );
			Var t = new Var( "t" );
			Var h = new Var( "w" );
			Var bvType = new Var( "bvType", vf.createURI( "http://semoss.org/ontologies/Relation/Contains/BusinessValue" ) );

			GraphPattern gp = new GraphPattern();
			//gp.addConstraint(new Compare(p, new ValueConstant(vf.createURI("http://semoss.org/ontologies/Concept/System"))));
			//gp.addConstraint(new Compare(w, new ValueConstant(vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))));

			// Filter
			//gp.addConstraint(new Like(systemName,"*",false));
			// adding bindings
			BindingSetAssignment bsa = new BindingSetAssignment();
			SPARQLQueryBindingSet bs2 = new SPARQLQueryBindingSet();
			Collection<BindingSet> vbs = new ArrayList<>();
			vbs.add( bs2 );
			//bs2.addBinding("p", vf.createURI("http://semoss.org/ontologies/Concept/System")); // binding 1
			bs2.addBinding( "t", vf.createURI( "http://semoss.org/ontologies/Concept/System" ) ); // binding 2
			//bs2.addBinding("bvType", vf.createURI("http://semoss.org/ontologies/Relation/Contains/BusinessValue")); // binding 2
			bsa.setBindingSets( vbs );
			gp.addRequiredTE( bsa );

			// adding math portions
			ValueConstant arg1 = new ValueConstant( vf.createLiteral( 2.0 ) );
			ValueConstant arg2 = new ValueConstant( vf.createLiteral( 4.0 ) );
			MathExpr mathExpr = new MathExpr( p, arg2, MathExpr.MathOp.PLUS );

			Var m = new Var( "m" );
			// adding coalesce
			Coalesce c = new Coalesce();
			c.addArgument( m );
			//c.addArgument(new ValueConstant(vf.createURI("http://semoss.org")));
			c.addArgument( mathExpr );

			ExtensionElem cee = new ExtensionElem( c, "m" );
			Extension ce = new Extension( new GraphPattern().buildTupleExpr() );
			ce.addElement( cee );
			gp.addRequiredTE( ce );

			// adding triples
			gp.addRequiredSP( systemName, w, t );
			gp.addRequiredSP( systemName, bvType, p );

			log.debug( gp.buildTupleExpr() );

			Projection proj = new Projection( new GraphPattern().buildTupleExpr() );
			ProjectionElemList list = new ProjectionElemList();
			list.addElements( new ProjectionElem( "x", "k" ), new ProjectionElem( "t" ), new ProjectionElem( "p" ), new ProjectionElem( "m" ) );
			proj.setProjectionElemList( list );
			gp.addRequiredTE( proj );
			//log.debug(gp.buildTupleExpr());

			//gp.addRequiredTE()

			/*gp.addRequiredTE(new Projection(gp.buildTupleExpr(),
			 new ProjectionElemList(new ProjectionElem("x")
			 ,new ProjectionElem("t"))));
			 */
			//log.debug(gp.buildTupleExpr());
			//
			//gp.
			TupleExpr query2 = gp.buildTupleExpr(); /*new Projection(gp.buildTupleExpr(),
			 new ProjectionElemList(new ProjectionElem("x")
			 ,new ProjectionElem("t")
			 // new ProjectionElem("y")
			 ));*/


			//query2.addRequiredTE(ce);
			//gp.addRequiredTE(bindE);
			//gp.addRequiredTE(new )
			// gp.addRequiredSP(x, p, w);
			// gp.addRequiredSP(x, y, w);
			// gp.addConstraint(new Compare(x, new
			// ValueConstant(vf.createURI("http://semoss.org")));
			ParsedTupleQuery query3 = new ParsedTupleQuery( query2 );
			SailTupleQuery q = new MyTupleQuery( query3,
					(SailRepositoryConnection) rc );
					//(SailRepositoryConnection) ((BigDataEngine)bdEngine).rc);

			log.debug( "SPARQL: " + query3 );

			TupleQueryResult sparqlResults = q.evaluate();
			//tq.setIncludeInferred(true /* includeInferred */);
			//TupleQueryResult sparqlResults = tq.evaluate();

			log.debug( "Output is " );
			while ( sparqlResults.hasNext() ) {
				BindingSet bs = sparqlResults.next();
				log.debug( "Predicate >>> " + bs.getBinding( "x" ) + "  >>> " + bs.getBinding( "t" ) + " >>> " + bs.getBinding( "m" ) );
			}
		}
		catch ( Exception e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}
	}

}

class MyTupleQuery extends SailTupleQuery {

	public MyTupleQuery( ParsedTupleQuery query, SailRepositoryConnection sc ) {
		super( query, sc );

	}
}

class MyExtension extends Extension {

	@Override
	public Set<String> getBindingNames() {
		Logger.getLogger( MyExtension.class ).error( "Going to crash" );
		return super.getBindingNames();
	}
}
