/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.model.vocabulary.SEMPERS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import com.ostrichemulators.semtool.model.vocabulary.OLO;
import com.ostrichemulators.semtool.model.vocabulary.SP;
import com.ostrichemulators.semtool.model.vocabulary.SPIN;
import com.ostrichemulators.semtool.model.vocabulary.SPL;
import com.ostrichemulators.semtool.model.vocabulary.UI;
import com.ostrichemulators.semtool.om.Insight;
import com.ostrichemulators.semtool.om.InsightOutputType;
import com.ostrichemulators.semtool.om.Parameter;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.om.Perspective;
import static com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine.INFER;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import static com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter.getDate;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.user.User;

import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InsightManagerImpl implements InsightManager {

	private static final Logger log = Logger.getLogger( InsightManagerImpl.class );

	private UriBuilder urib = UriBuilder.getBuilder( SEMPERS.NAMESPACE );
	private final Map<IRI, Insight> insights = new HashMap<>();
	private final List<Perspective> perspectives = new ArrayList<>();

	public InsightManagerImpl() {

	}

	public InsightManagerImpl( InsightManager im ) {
		urib = UriBuilder.getBuilder( im.getInsightNamespace() );
		perspectives.addAll( deepcopy( im.getPerspectives() ) );

		for ( Perspective p : perspectives ) {
			for ( Insight i : p.getInsights() ) {
				insights.put( i.getId(), i );
			}
		}
	}

	@Override
	public void setInsightNamespace( String ns ) {
		urib = UriBuilder.getBuilder( ns );
	}

	@Override
	public String getInsightNamespace() {
		return urib.toString();
	}

	@Override
	public void addAll( Collection<Perspective> newdata, boolean clearfirst ) {
		if ( newdata.equals( perspectives ) ) {
			return; // nothing to copy
		}

		if ( clearfirst ) {
			perspectives.clear();
			insights.clear();
		}

		perspectives.addAll( deepcopy( newdata ) );
		for ( Perspective p : perspectives ) {
			for ( Insight i : p.getInsights() ) {
				insights.put( i.getId(), i );
			}
		}
	}

	@Override
	public List<Perspective> getPerspectives() {
		return new ArrayList<>( perspectives );
	}

	public static InsightManager createFromRepository( Repository repo ) {
		InsightManagerImpl imi = new InsightManagerImpl();
		RepositoryConnection rc = null;
		try {
			if ( !repo.isInitialized() ) {
				repo.init();
			}
			rc = repo.getConnection();
			Model m = QueryResults.asModel( rc.getStatements( null, null, null ) );
			imi.loadFromModel( m );
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}
		finally {
			if ( null != rc ) {
				try {
					rc.close();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}

		}
		return imi;
	}

	public void loadFromModel( Model model ) {
		// FIXME: we really don't need to create a new Repository for this, do we?
		MemoryStore memstore = new MemoryStore();
		Sail sail = new SchemaCachingRDFSInferencer( memstore );
		Repository repo = new SailRepository( sail );
		repo.init();
		RepositoryConnection rc = repo.getConnection();
		rc.begin();
		rc.add( model );
		rc.commit();

		List<Perspective> persps = new ArrayList<>();
		try {
			RepositoryResult<Statement> rrs = rc.getStatements( null, RDF.TYPE,
					SEMPERS.Perspective, true );
			List<Statement> stmts = Iterations.asList( rrs );
			Map<Perspective, Integer> ordering = new HashMap<>();
			for ( Statement s : stmts ) {
				Perspective p = loadPerspective( IRI.class.cast( s.getSubject() ), rc, urib );

				List<Statement> slotstmts = Iterations.asList(
						rc.getStatements( p.getId(), OLO.index, null, false ) );
				if ( !slotstmts.isEmpty() ) {
					Literal slotval = Literal.class.cast( slotstmts.get( 0 ).getObject() );
					ordering.put( p, slotval.intValue() );
				}

				persps.add( p );
			}

			persps.sort( new Comparator<Perspective>() {

				@Override
				public int compare( Perspective o1, Perspective o2 ) {
					int o1slot = ordering.getOrDefault( o1, Integer.MAX_VALUE );
					int o2slot = ordering.getOrDefault( o2, Integer.MAX_VALUE );
					return o1slot - o2slot;
				}
			} );

		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		perspectives.addAll( persps );

		for ( Perspective p : persps ) {
			for ( Insight i : p.getInsights() ) {
				insights.put( i.getId(), i );
			}
		}
	}

	@Override
	public boolean isEmpty() {
		return ( perspectives.isEmpty() && insights.isEmpty() );
	}

	@Override
	public Perspective getSystemPerspective( IEngine eng ) {
		Perspective persps = new Perspective( urib.uniqueIri(), "Generic Perspective",
				"System Generated Generic Perspective" );

		Insight metamodel = new Insight( "View the Database Metamodel", null,
				InsightOutputType.GRAPH_METAMODEL );

		Insight explore = new Insight( "Explore an instance of a selected node type",
				"SELECT DISTINCT ?instance WHERE { ?instance ?s ?o }",
				InsightOutputType.GRAPH );
		explore.setId( urib.uniqueIri() );

		Parameter concept = new Parameter( "Concept",
				NodeDerivationTools.getConceptQuery( eng ) );
		concept.setId( urib.uniqueIri() );

		Parameter instance = new Parameter( "Instance",
				"SELECT ?instance WHERE { ?instance a ?concept }" );
		instance.setId( urib.uniqueIri() );

		explore.setParameters( Arrays.asList( concept, instance ) );

		String nespql = "CONSTRUCT { ?instance ?step ?neighbor } WHERE {"
				+ "  ?instance ?step ?neighbor ."
				+ "  ?step a ?stepType ."
				+ "  FILTER ( ?stepType = owl:ObjectProperty )"
				+ "}";
		Insight neighbor = new Insight( "Show One Neighbor Away from Selected Node",
				nespql, InsightOutputType.GRAPH );
		neighbor.setId( urib.uniqueIri() );
		neighbor.setParameters( Arrays.asList( concept, instance ) );

		persps.setInsights( Arrays.asList( metamodel, explore, neighbor ) );
		return persps;
	}

	/**
	 * Gets all Parameter objects under the passed-in Insight URI.
	 *
	 * @param insightURI -- (URI) An Insight URI.
	 *
	 * @return -- (Collection<Parameter>) Described above.
	 */
	private static Collection<Parameter> loadParameters( Insight insight,
			RepositoryConnection rc ) {
		List<Parameter> colInsightParameters = new ArrayList<>();

		try {
			// get this insight's constraints/parameters
			Collection<Statement> paramIds = Iterations.asList( rc.getStatements( insight.getId(),
					SPIN.constraint, null, false ) );
			for ( Statement s : paramIds ) {
				IRI paramId = IRI.class.cast( s.getObject() );
				Parameter parameter = new Parameter();
				parameter.setId( paramId );

				// get data about this parameter
				Collection<Statement> data
						= Iterations.asList( rc.getStatements( paramId, null, null, false ) );
				for ( Statement d : data ) {
					IRI pred = d.getPredicate();
					Value val = d.getObject();

					if ( RDFS.LABEL.equals( pred ) ) {
						parameter.setLabel( val.stringValue() );
					}
					else if ( SPL.predicate.equals( pred ) ) {
						List<Statement> preddata
								= Iterations.asList( rc.getStatements( IRI.class.cast( val ),
										RDFS.LABEL, null, true ) );
						if ( !preddata.isEmpty() ) {
							// parameter.setVariable( preddata.get( 0 ).getObject().stringValue() );
						}
					}
					else if ( SP.query.equals( pred ) ) {
						List<Statement> preddata
								= Iterations.asList( rc.getStatements( IRI.class.cast( val ),
										SP.text, null, true ) );
						if ( !preddata.isEmpty() ) {
							parameter.setDefaultQuery( preddata.get( 0 ).getObject().stringValue() );
						}
					}
					else if ( SPL.valueType.equals( pred ) ) {
						parameter.setParameterType( val.stringValue() );
					}
				}

				colInsightParameters.add( parameter );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		return colInsightParameters;
	}

	private static List<Insight> loadInsights( Perspective perspective,
			RepositoryConnection rc, UriBuilder urib ) {
		List<Insight> list = new ArrayList<>();
		if ( perspective != null ) {
			String query = "SELECT ?item "
					+ "WHERE {"
					+ "  ?perspective olo:slot ?slot ."
					+ "  ?slot olo:item ?item ."
					+ "  ?slot olo:index ?index . "
					+ "} ORDER BY ?index";
			OneVarListQueryAdapter<IRI> orderquery
					= OneVarListQueryAdapter.getIriList( query );
			orderquery.bind( "perspective", perspective.getId() );
			orderquery.addNamespace( OLO.PREFIX, OLO.NAMESPACE );

			List<IRI> insightUris
					= AbstractSesameEngine.getSelectNoEx( orderquery, rc, true );

			for ( IRI id : insightUris ) {
				list.add( loadInsight( id, rc, urib ) );
			}
		}

		return list;
	}

	@Override
	public Insight getInsight( IRI insightURI ) {
		return insights.get( insightURI );
	}

	private static Insight loadInsight( IRI insightURI, RepositoryConnection rc,
			UriBuilder urib ) {

		Insight insight = null;
		try {
			// need a couple things here...the insight data, the query, 
			// and view data (the playsheet)
			List<Statement> stmts
					= Iterations.asList( rc.getStatements( insightURI, null, null, true ) );

			// the query itself
			List<Statement> qstmts
					= Iterations.asList( rc.getStatements( insightURI, SPIN.body, null, true ) );
			if ( !qstmts.isEmpty() ) {
				IRI body = IRI.class.cast( qstmts.get( 0 ).getObject() );
				List<Statement> querys
						= Iterations.asList( rc.getStatements( body, SP.text, null, true ) );
				stmts.addAll( querys );
			}
			// the data view
			List<Statement> dvstmts
					= Iterations.asList( rc.getStatements( insightURI, UI.dataView, null, true ) );
			if ( !dvstmts.isEmpty() ) {
				IRI view = IRI.class.cast( dvstmts.get( 0 ).getObject() );
				List<Statement> dvs
						= Iterations.asList( rc.getStatements( view, UI.viewClass, null, true ) );
				stmts.addAll( dvs );
			}

			if ( !stmts.isEmpty() ) {
				insight = insightFromStatements( stmts );

				// finally, set the parameters
				Collection<Parameter> params = loadParameters( insight, rc );
				insight.setParameters( params );
			}
		}
		catch ( RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}

		if ( null == insight ) {
			throw new IllegalArgumentException( "unknown insight: " + insightURI );
		}
		return insight;
	}

	@Override
	public Perspective getPerspective( IRI perspectiveURI ) {
		ListIterator<Perspective> lit = perspectives.listIterator();
		while ( lit.hasNext() ) {
			Perspective old = lit.next();
			if ( old.getId().equals( perspectiveURI ) ) {
				return old;
			}
		}

		throw new IllegalArgumentException( "unknown perspective: " + perspectiveURI );
	}

	@Override
	public IRI add( Perspective p ) {
		p.setId( urib.uniqueIri() );
		perspectives.add( p );
		return p.getId();
	}

	@Override
	public void update( Perspective p ) {
		ListIterator<Perspective> lit = perspectives.listIterator();
		while ( lit.hasNext() ) {
			Perspective old = lit.next();
			if ( old.getId().equals( p.getId() ) ) {
				lit.set( p );
			}
		}
	}

	@Override
	public void addInsight( Perspective p, Insight i, int pos ) {
		insights.put( i.getId(), i );
		if ( pos < 0 ) {
			pos = p.getInsights().size();
		}

		p.getInsights().add( pos, i );
	}

	@Override
	public void remove( Perspective p ) {
		perspectives.remove( p );
	}

	@Override
	public IRI add( Insight p ) {
		p.setId( urib.uniqueIri() );
		insights.put( p.getId(), p );
		return p.getId();
	}

	@Override
	public void update( Insight p ) {
		insights.put( p.getId(), p );
	}

	@Override
	public void remove( Insight p ) {
		insights.remove( p.getId() );
	}

	private static Perspective loadPerspective( IRI perspectiveURI, RepositoryConnection rc,
			UriBuilder urib ) {
		try {
			Perspective perspective = new Perspective( perspectiveURI );
			Collection<Statement> stmts
					= Iterations.asList( rc.getStatements( perspectiveURI, null, null, false ) );
			for ( Statement s : stmts ) {
				IRI pred = s.getPredicate();
				Value val = s.getObject();

				if ( val instanceof Literal ) {
					if ( RDFS.LABEL.equals( pred ) ) {
						perspective.setLabel( val.stringValue() );
					}
					else if ( DCTERMS.DESCRIPTION.equals( pred ) ) {
						perspective.setDescription( val.stringValue() );
					}
				}
			}

			perspective.setInsights( loadInsights( perspective, rc, urib ) );
			return perspective;
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		throw new IllegalArgumentException( "unknown perspective: " + perspectiveURI );
	}

	private static Insight insightFromStatements( Collection<Statement> stmts ) {
		Insight insight = new Insight();

		for ( Statement stmt : stmts ) {
			IRI pred = stmt.getPredicate();
			Value val = stmt.getObject();
			if ( val instanceof Literal ) {
				Literal obj = Literal.class.cast( val );

				if ( RDFS.LABEL.equals( pred ) ) {
					insight.setId( IRI.class.cast( stmt.getSubject() ) );

					insight.setLabel( obj.stringValue() );
				}
				else if ( SEMPERS.INSIGHT_OUTPUT_TYPE.equals( pred ) ) {
					try {
						insight.setOutput( InsightOutputType.valueOf( obj.stringValue() ) );
					}
					catch ( IllegalArgumentException iae ) {
						log.warn( "unknown insight output type: " + obj.stringValue()
								+ "(using grid instead)" );
						insight.setOutput( InsightOutputType.GRID );
					}
				}
				else if ( DCTERMS.CREATOR.equals( pred ) ) {
					insight.setCreator( obj.stringValue() );
				}
				else if ( DCTERMS.CREATED.equals( pred ) ) {
					IRI uri = obj.getDatatype();
					if ( XMLSchema.DATE.equals( uri ) || XMLSchema.DATETIME.equals( uri ) ) {
						insight.setCreated( getDate( Literal.class.cast( obj ).calendarValue() ) );
					}
				}
				else if ( DCTERMS.MODIFIED.equals( pred ) ) {
					IRI uri = obj.getDatatype();
					if ( XMLSchema.DATE.equals( uri ) || XMLSchema.DATETIME.equals( uri ) ) {
						insight.setModified( getDate( Literal.class.cast( obj ).calendarValue() ) );
					}
				}
				else if ( DCTERMS.DESCRIPTION.equals( pred ) ) {
					insight.setDescription( obj.stringValue() );
				}
				else if ( SP.text.equals( pred ) ) {
					insight.setSparql( obj.stringValue() );
				}
				else if ( SEMPERS.INSIGHT_OUTPUT_TYPE.equals( pred ) ) {
					insight.setOutput( InsightOutputType.valueOf( obj.stringValue() ) );
				}
			}
		}

		// make sure every insight has an output type
		if ( null == insight.getOutput() ) {
			insight.setOutput( InsightOutputType.GRID );
		}

		return insight;
	}

	public static Model getModel( InsightManager im, User user ) {
		Model statements = new LinkedHashModel();
		int idx = 0;

		RDFParser parser = new TurtleParser();
		StatementCollector coll = new StatementCollector();
		parser.setRDFHandler( coll );
		try ( InputStream is = IEngine.class.getResourceAsStream( "/models/sempers.ttl" ) ) {
			parser.parse( is, SEMPERS.BASE_URI );
		}
		catch ( Exception e ) {
			log.warn( "could not include sempers.ttl ontology in statements", e );
		}

		statements.addAll( coll.getStatements() );

		ValueFactory vf = SimpleValueFactory.getInstance();
		for ( Perspective p : im.getPerspectives() ) {
			statements.addAll( getStatements( p, user ) );
			statements.add( vf.createStatement( p.getId(), OLO.index,
					vf.createLiteral( idx++ ) ) );
		}

		return statements;
	}

	/**
	 * Converts the Perspective, its Insights and Parameters (and ordering!) to
	 * Statements for adding to a Repository. If any Perspective, Insight, or
	 * Parameter is missing an ID, a new one will be generated for it, and set on
	 * the object
	 *
	 * @param p the perspective to convert.
	 * @param user
	 * @return a list of statements that completely represent the perspective tree
	 */
	public static Model getStatements( Perspective p, User user ) {
		Model statements = new LinkedHashModel();
		UriBuilder urib = UriBuilder.getBuilder( SEMPERS.NAMESPACE );
		ValueFactory vf = SimpleValueFactory.getInstance();

		// if we're creating statements, mark our repository as an insights db
		statements.add( vf.createStatement( SEMPERS.INSIGHT_DB, RDF.TYPE,
				SEMPERS.INSIGHT_CORE_TYPE ) );

		if ( null == p.getId() ) {
			p.setId( urib.build( p.getLabel() ) );
		}
		statements.addAll( getPerspectiveStatements( p, vf, urib, user ) );

		for ( Insight i : p.getInsights() ) {
			final String piname = p.getLabel() + "-" + i.getLabel();

			if ( null == i.getId() ) {
				i.setId( urib.build( piname ) );
			}
			statements.addAll( getInsightStatements( i, vf, urib, user ) );

			for ( Parameter a : i.getInsightParameters() ) {
				final String pianame = piname + "-" + a.getLabel();

				IRI predicateUri = urib.build( pianame + "-pred" );
				IRI queryUri = urib.build( pianame + "-query" );

				if ( null == a.getId() ) {
					a.setId( urib.build( pianame ) );
				}

				statements.addAll( getParameterStatements( a, predicateUri, queryUri,
						vf, urib, user ) );
			}

			statements.addAll( getConstraintStatements( i, i.getInsightParameters() ) );
		}

		statements.addAll( getOrderingStatements( p, p.getInsights(), vf, urib ) );

		return statements;
	}

	protected static Model getPerspectiveStatements( Perspective p,
			ValueFactory vf, UriBuilder urib, User user ) {

		Model statements = new LinkedHashModel();
		IRI pid = p.getId();
		Date now = new Date();

		statements.add( vf.createStatement( pid, RDF.TYPE, SEMPERS.Perspective ) );
		statements.add( vf.createStatement( pid, RDFS.LABEL,
				vf.createLiteral( p.getLabel() ) ) );
		if ( null != p.getDescription() ) {
			statements.add( vf.createStatement( pid, DCTERMS.DESCRIPTION,
					vf.createLiteral( p.getDescription() ) ) );
		}

		statements.add( vf.createStatement( pid, DCTERMS.CREATED,
				vf.createLiteral( now ) ) );
		statements.add( vf.createStatement( pid, DCTERMS.MODIFIED,
				vf.createLiteral( now ) ) );
		statements.add( vf.createStatement( pid, DCTERMS.CREATOR,
				vf.createLiteral( getAuthorInfo( user ) ) ) );

		return statements;
	}

	protected static Model getInsightStatements( Insight insight,
			ValueFactory vf, UriBuilder urib, User user ) {

		Model statements = new LinkedHashModel();
		IRI iid = insight.getId();

		statements.add( vf.createStatement( iid, RDF.TYPE, SPIN.MagicProperty ) );
		statements.add( vf.createStatement( iid, RDFS.LABEL,
				vf.createLiteral( insight.getLabel() ) ) );
		if ( null != insight.getDescription() ) {
			statements.add( vf.createStatement( iid, DCTERMS.DESCRIPTION,
					vf.createLiteral( insight.getDescription() ) ) );
		}

		if ( null != insight.getOutput() ) {
			statements.add( vf.createStatement( iid, SEMPERS.INSIGHT_OUTPUT_TYPE,
					vf.createLiteral( insight.getOutput().toString() ) ) );
		}

		statements.add( vf.createStatement( iid, RDFS.SUBCLASSOF, SEMPERS.InsightProperties ) );
		statements.add( vf.createStatement( iid, DCTERMS.CREATED,
				vf.createLiteral( null == insight.getCreated() ? new Date()
						: insight.getCreated() ) ) );
		statements.add( vf.createStatement( iid, DCTERMS.MODIFIED,
				vf.createLiteral( new Date() ) ) );
		statements.add( vf.createStatement( iid, DCTERMS.CREATOR,
				vf.createLiteral( getAuthorInfo( user ) ) ) );

		String sparql = insight.getSparql();

		IRI spinid = urib.build( insight.getLabel() + "-query" );
		statements.add( vf.createStatement( iid, SPIN.body, spinid ) );
		statements.add( vf.createStatement( spinid, SP.text,
				vf.createLiteral( sparql ) ) );

		// Insights can only have SELECT, CONSTRUCT, or DESCRIBE queries:
		IRI bodytype;
		if ( sparql.toUpperCase().startsWith( "DESCRIBE" ) ) {
			bodytype = SP.Describe;
		}
		else if ( sparql.toUpperCase().startsWith( "CONSTRUCT" ) ) {
			bodytype = SP.Construct;
		}
		else {
			bodytype = SP.Select;
		}
		statements.add( vf.createStatement( spinid, RDF.TYPE, bodytype ) );

		return statements;
	}

	protected static Model getParameterStatements( Parameter parameter,
			IRI predicateUri, IRI queryUri, ValueFactory vf, UriBuilder urib,
			User user ) {

		Model statements = new LinkedHashModel();

		IRI pid = parameter.getId();

		statements.add( vf.createStatement( pid, RDFS.LABEL,
				vf.createLiteral( parameter.getLabel() ) ) );

		statements.add( vf.createStatement( pid, SPL.predicate, predicateUri ) );
		statements.add( vf.createStatement( pid, SP.query, queryUri ) );

		statements.add( vf.createStatement( predicateUri, RDFS.LABEL,
				vf.createLiteral( parameter.getLabel() ) ) );
		statements.add( vf.createStatement( queryUri, SP.text,
				vf.createLiteral( parameter.getDefaultQuery() ) ) );

		return statements;
	}

	protected static Model getOrderingStatements( Perspective p,
			List<Insight> insights, ValueFactory vf, UriBuilder urib ) {
		Model statements = new LinkedHashModel();
		int idx = 0;
		for ( Insight i : insights ) {
			IRI slot = urib.build( p.getLabel() + "-slot-" + Integer.toString( ++idx ) );
			statements.add( vf.createStatement( p.getId(), OLO.slot, slot ) );
			statements.add( vf.createStatement( slot, OLO.index, vf.createLiteral( idx ) ) );
			statements.add( vf.createStatement( slot, OLO.item, i.getId() ) );
		}

		return statements;
	}

	protected static Collection<Statement> getConstraintStatements( Insight ins,
			Collection<Parameter> params ) {
		Model statements = new LinkedHashModel();
		ValueFactory vf = SimpleValueFactory.getInstance();
		for ( Parameter p : params ) {
			statements.add( vf.createStatement( ins.getId(), SPIN.constraint, p.getId() ) );
		}
		return statements;
	}

	protected static String getAuthorInfo( User user ) {
		return ( null == user || null == user.getAuthorInfo()
				? "Created By Insight Manager, "
				+ System.getProperty( "release.nameVersion", "VA SEMOSS" )
				: user.getAuthorInfo() );
	}

	public static Collection<Perspective> deepcopy( Collection<Perspective> oldps ) {
		List<Perspective> perspectives = new ArrayList<>();
		for ( Perspective oldp : oldps ) {
			Perspective newp
					= new Perspective( oldp.getId(), oldp.getLabel(), oldp.getDescription() );
			List<Insight> newpInsights = new ArrayList<>();
			for ( Insight oldi : oldp.getInsights() ) {
				Insight newi = new Insight( oldi );
				newpInsights.add( newi );
			}
			newp.setInsights( newpInsights );

			perspectives.add( newp );
		}

		return perspectives;
	}
}
