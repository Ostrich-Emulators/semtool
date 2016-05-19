/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.model.vocabulary.SEMPERS;
import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;

/**
 *
 * @author ryan
 */
public class InsightManagerImpl implements InsightManager {

	private static final Logger log = Logger.getLogger( InsightManagerImpl.class );

	private UriBuilder urib = UriBuilder.getBuilder( SEMPERS.NAMESPACE );
	private final Map<URI, Insight> insights = new HashMap<>();
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
				repo.initialize();
			}
			rc = repo.getConnection();
			imi.loadFromRepository( rc );
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

	public void loadFromRepository( RepositoryConnection rc ) {
		List<Perspective> persps = new ArrayList<>();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					RDF.TYPE, SEMPERS.Perspective, true ) );
			Map<Perspective, Integer> ordering = new HashMap<>();
			for ( Statement s : stmts ) {
				Perspective p = loadPerspective( URI.class.cast( s.getSubject() ), rc, urib );

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
	public Perspective getSystemPerspective( IEngine eng ) {
		Perspective persps = new Perspective( urib.uniqueUri(), "Generic Perspective",
				"System Generated Generic Perspective" );
		URI conceptUri = eng.getSchemaBuilder().getConceptUri().build();
		URI relUri = eng.getSchemaBuilder().getRelationUri().build();

		String mmspql = "CONSTRUCT{ ?source ?relation ?target } WHERE {"
				+ "  ?relation a owl:ObjectProperty ."
				+ "  ?s ?relation ?o ."
				+ "  ?s a ?source ."
				+ "  ?o a ?target ."
				+ "  FILTER( ?source != <" + conceptUri + ">"
				+ "    && ?source != owl:Class"
				+ "    && ?source != rdfs:Resource"
				+ "    && ?source != <" + relUri + "> )"
				+ "  FILTER( ?target != <" + conceptUri + ">"
				+ "    && ?target != owl:Class"
				+ "    && ?target != rdfs:Resource )"
				+ "}";
		Insight metamodel = new Insight( "View the Database Metamodel", mmspql,
				InsightOutputType.GRAPH_METAMODEL );

		Insight explore = new Insight( "Explore an instance of a selected node type",
				"SELECT DISTINCT ?instance WHERE { ?instance ?s ?o }",
				InsightOutputType.GRAPH );
		explore.setId( urib.uniqueUri() );

		Parameter concept = new Parameter( "Concept",
				NodeDerivationTools.getConceptQuery( eng ) );
		concept.setId( urib.uniqueUri() );

		Parameter instance = new Parameter( "Instance",
				"SELECT ?instance WHERE { ?instance a ?concept }" );
		instance.setId( urib.uniqueUri() );

		explore.setParameters( Arrays.asList( concept, instance ) );

		String nespql = "CONSTRUCT { ?instance ?step ?neighbor } WHERE {"
				+ "  ?instance ?step ?neighbor ."
				+ "  ?step a ?stepType ."
				+ "  FILTER ( ?stepType = owl:ObjectProperty )"
				+ "}";
		Insight neighbor = new Insight( "Show One Neighbor Away from Selected Node",
				nespql, InsightOutputType.GRAPH );
		neighbor.setId( urib.uniqueUri() );
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
				URI paramId = URI.class.cast( s.getObject() );
				Parameter parameter = new Parameter();
				parameter.setId( paramId );

				// get data about this parameter
				Collection<Statement> data
						= Iterations.asList( rc.getStatements( paramId, null, null, false ) );
				for ( Statement d : data ) {
					URI pred = d.getPredicate();
					Value val = d.getObject();

					if ( RDFS.LABEL.equals( pred ) ) {
						parameter.setLabel( val.stringValue() );
					}
					else if ( SPL.predicate.equals( pred ) ) {
						List<Statement> preddata
								= Iterations.asList( rc.getStatements( URI.class.cast( val ),
												RDFS.LABEL, null, true ) );
						if ( !preddata.isEmpty() ) {
							// parameter.setVariable( preddata.get( 0 ).getObject().stringValue() );
						}
					}
					else if ( SP.query.equals( pred ) ) {
						List<Statement> preddata
								= Iterations.asList( rc.getStatements( URI.class.cast( val ),
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
			OneVarListQueryAdapter<URI> orderquery
					= OneVarListQueryAdapter.getUriList( query );
			orderquery.bind( "perspective", perspective.getId() );
			orderquery.addNamespace( OLO.PREFIX, OLO.NAMESPACE );

			List<URI> insightUris
					= AbstractSesameEngine.getSelectNoEx( orderquery, rc, true );

			for ( URI id : insightUris ) {
				list.add( loadInsight( id, rc, urib ) );
			}
		}

		return list;
	}

	@Override
	public Insight getInsight( URI insightURI ) {
		return insights.get( insightURI );
	}

	private static Insight loadInsight( URI insightURI, RepositoryConnection rc,
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
				URI body = URI.class.cast( qstmts.get( 0 ).getObject() );
				List<Statement> querys
						= Iterations.asList( rc.getStatements( body, SP.text, null, true ) );
				stmts.addAll( querys );
			}
			// the data view
			List<Statement> dvstmts
					= Iterations.asList( rc.getStatements( insightURI, UI.dataView, null, true ) );
			if ( !dvstmts.isEmpty() ) {
				URI view = URI.class.cast( dvstmts.get( 0 ).getObject() );
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
	public Perspective getPerspective( URI perspectiveURI ) {
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
	public URI add( Perspective p ) {
		p.setId( urib.uniqueUri() );
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
	public URI add( Insight p ) {
		p.setId( urib.uniqueUri() );
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

	private static Perspective loadPerspective( URI perspectiveURI, RepositoryConnection rc,
			UriBuilder urib ) {
		try {
			Perspective perspective = new Perspective( perspectiveURI );
			Collection<Statement> stmts
					= Iterations.asList( rc.getStatements( perspectiveURI, null, null, false ) );
			for ( Statement s : stmts ) {
				URI pred = s.getPredicate();
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
			URI pred = stmt.getPredicate();
			Value val = stmt.getObject();
			if ( val instanceof Literal ) {
				Literal obj = Literal.class.cast( val );

				if ( RDFS.LABEL.equals( pred ) ) {
					insight.setId( URI.class.cast( stmt.getSubject() ) );

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
					URI uri = obj.getDatatype();
					if ( XMLSchema.DATE.equals( uri ) || XMLSchema.DATETIME.equals( uri ) ) {
						insight.setCreated( getDate( Literal.class.cast( obj ).calendarValue() ) );
					}
				}
				else if ( DCTERMS.MODIFIED.equals( pred ) ) {
					URI uri = obj.getDatatype();
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

	public static Collection<Statement> getStatements( InsightManager im, User user ) {
		List<Statement> statements = new ArrayList<>();
		int idx = 0;

		RDFParser parser = new TurtleParser();
		try ( InputStream is = IEngine.class.getResourceAsStream( "/models/sempers.ttl" ) ) {
			parser.parse( is, SEMPERS.BASE_URI );
		}
		catch ( Exception e ) {
			log.warn( "could not include sempers.ttl ontology in statements", e );
		}

		ValueFactory vf = new ValueFactoryImpl();
		for ( Perspective p : im.getPerspectives() ) {
			statements.addAll( getStatements( p, user ) );
			statements.add( new StatementImpl( p.getId(), OLO.index,
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
	public static Collection<Statement> getStatements( Perspective p, User user ) {
		List<Statement> statements = new ArrayList<>();
		UriBuilder urib = UriBuilder.getBuilder( SEMPERS.NAMESPACE );

		// if we're creating statements, mark our repository as an insights db
		statements.add( new StatementImpl( SEMPERS.INSIGHT_DB, RDF.TYPE,
				SEMPERS.INSIGHT_CORE_TYPE ) );

		ValueFactory vf = new ValueFactoryImpl();

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

				URI predicateUri = urib.build( pianame + "-pred" );
				URI queryUri = urib.build( pianame + "-query" );

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

	protected static Collection<Statement> getPerspectiveStatements( Perspective p,
			ValueFactory vf, UriBuilder urib, User user ) {

		List<Statement> statements = new ArrayList<>();
		URI pid = p.getId();
		Date now = new Date();

		statements.add( new StatementImpl( pid, RDF.TYPE, SEMPERS.Perspective ) );
		statements.add( new StatementImpl( pid, RDFS.LABEL,
				vf.createLiteral( p.getLabel() ) ) );
		if ( null != p.getDescription() ) {
			statements.add( new StatementImpl( pid, DCTERMS.DESCRIPTION,
					vf.createLiteral( p.getDescription() ) ) );
		}

		statements.add( new StatementImpl( pid, DCTERMS.CREATED,
				vf.createLiteral( now ) ) );
		statements.add( new StatementImpl( pid, DCTERMS.MODIFIED,
				vf.createLiteral( now ) ) );
		statements.add( new StatementImpl( pid, DCTERMS.CREATOR,
				vf.createLiteral( getAuthorInfo( user ) ) ) );

		return statements;
	}

	protected static Collection<Statement> getInsightStatements( Insight insight,
			ValueFactory vf, UriBuilder urib, User user ) {

		List<Statement> statements = new ArrayList<>();
		URI iid = insight.getId();

		statements.add( new StatementImpl( iid, RDF.TYPE, SPIN.MagicProperty ) );
		statements.add( new StatementImpl( iid, RDFS.LABEL,
				vf.createLiteral( insight.getLabel() ) ) );
		if ( null != insight.getDescription() ) {
			statements.add( new StatementImpl( iid, DCTERMS.DESCRIPTION,
					vf.createLiteral( insight.getDescription() ) ) );
		}

		if ( null != insight.getOutput() ) {
			statements.add( new StatementImpl( iid, SEMPERS.INSIGHT_OUTPUT_TYPE,
					vf.createLiteral( insight.getOutput().toString() ) ) );
		}

		statements.add( new StatementImpl( iid, RDFS.SUBCLASSOF, SEMPERS.InsightProperties ) );
		statements.add( new StatementImpl( iid, DCTERMS.CREATED,
				vf.createLiteral( null == insight.getCreated() ? new Date()
								: insight.getCreated() ) ) );
		statements.add( new StatementImpl( iid, DCTERMS.MODIFIED,
				vf.createLiteral( new Date() ) ) );
		statements.add( new StatementImpl( iid, DCTERMS.CREATOR,
				vf.createLiteral( getAuthorInfo( user ) ) ) );

		String sparql = insight.getSparql();

		URI spinid = urib.build( insight.getLabel() + "-query" );
		statements.add( new StatementImpl( iid, SPIN.body, spinid ) );
		statements.add( new StatementImpl( spinid, SP.text,
				vf.createLiteral( sparql ) ) );

		// Insights can only have SELECT, CONSTRUCT, or DESCRIBE queries:
		URI bodytype;
		if ( sparql.toUpperCase().startsWith( "DESCRIBE" ) ) {
			bodytype = SP.Describe;
		}
		else if ( sparql.toUpperCase().startsWith( "CONSTRUCT" ) ) {
			bodytype = SP.Construct;
		}
		else {
			bodytype = SP.Select;
		}
		statements.add( new StatementImpl( spinid, RDF.TYPE, bodytype ) );

		return statements;
	}

	protected static Collection<Statement> getParameterStatements( Parameter parameter,
			URI predicateUri, URI queryUri, ValueFactory vf, UriBuilder urib,
			User user ) {

		List<Statement> statements = new ArrayList<>();

		URI pid = parameter.getId();

		statements.add( new StatementImpl( pid, RDFS.LABEL,
				vf.createLiteral( parameter.getLabel() ) ) );

		statements.add( new StatementImpl( pid, SPL.predicate, predicateUri ) );
		statements.add( new StatementImpl( pid, SP.query, queryUri ) );

		statements.add( new StatementImpl( predicateUri, RDFS.LABEL,
				vf.createLiteral( parameter.getLabel() ) ) );
		statements.add( new StatementImpl( queryUri, SP.text,
				vf.createLiteral( parameter.getDefaultQuery() ) ) );

		return statements;
	}

	protected static Collection<Statement> getOrderingStatements( Perspective p,
			List<Insight> insights, ValueFactory vf, UriBuilder urib ) {
		List<Statement> statements = new ArrayList<>();
		int idx = 0;
		for ( Insight i : insights ) {
			URI slot = urib.build( p.getLabel() + "-slot-" + Integer.toString( ++idx ) );
			statements.add( new StatementImpl( p.getId(), OLO.slot, slot ) );
			statements.add( new StatementImpl( slot, OLO.index, vf.createLiteral( idx ) ) );
			statements.add( new StatementImpl( slot, OLO.item, i.getId() ) );
		}

		return statements;
	}

	protected static Collection<Statement> getConstraintStatements( Insight ins,
			Collection<Parameter> params ) {
		List<Statement> statements = new ArrayList<>();
		for ( Parameter p : params ) {
			statements.add( new StatementImpl( ins.getId(), SPIN.constraint, p.getId() ) );
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
