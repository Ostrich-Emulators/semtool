/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.InsightOutputType;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.util.Constants;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import static gov.va.semoss.rdf.query.util.QueryExecutorAdapter.getDate;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.user.User;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.util.UriBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;

/**
 *
 * @author ryan
 */
public class InsightManagerImpl implements InsightManager {

	private static final Logger log = Logger.getLogger( InsightManagerImpl.class );
	private static final Pattern LEGACYPAT
			= Pattern.compile( "((BIND\\s*\\(\\s*)?<@(\\w+)((?:-)([^@]+))?@>(\\s*AS\\s+\\?(\\w+)\\s*\\)\\s*\\.?\\s*)?)" );
	private RepositoryConnection rc;
	private Repository repo = null;
	private boolean closeRcOnRelease = false;

	// strictly for upgrading old insights
	private static final Map<String, InsightOutputType> UPGRADETYPEMAP = new HashMap<>();

	static {
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.ColumnChartPlaySheet",
				InsightOutputType.COLUMN_CHART );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.DendrogramPlaySheet",
				InsightOutputType.DENDROGRAM );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.GraphPlaySheet",
				InsightOutputType.GRAPH );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.GridPlaySheet",
				InsightOutputType.GRID );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet",
				InsightOutputType.GRID_RAW );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.GridScatterSheet",
				InsightOutputType.GRID_SCATTER );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.HeatMapPlaySheet",
				InsightOutputType.HEATMAP );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.MetamodelGraphPlaySheet",
				InsightOutputType.GRAPH );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.ParallelCoordinatesPlaySheet",
				InsightOutputType.PARALLEL_COORDS );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.PieChartPlaySheet",
				InsightOutputType.PIE_CHART );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.SankeyPlaySheet",
				InsightOutputType.SANKEY );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.USHeatMapPlaySheet",
				InsightOutputType.HEATMAP_US );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.WorldHeatMapPlaySheet",
				InsightOutputType.HEATMAP_WORLD );
		UPGRADETYPEMAP.put( "gov.va.semoss.ui.components.playsheets.AppDupeHeatMapSheet",
				InsightOutputType.HEATMAP_APPDUPE );
	}

	public InsightManagerImpl( Repository _repo ) {
		repo = _repo;

		if ( null != repo ) {
			try {

				if ( !repo.isInitialized() ) {
					repo.initialize();
				}

				rc = repo.getConnection();
				closeRcOnRelease = true;
			}
			catch ( RepositoryException re ) {
				log.error( re, re );
			}
		}
	}

	/**
	 * Gets the current repository.
	 *
	 */
	@Override
	public Repository getRepository() {
		return this.repo;
	}

	/**
	 * Sets the connection and whether or not the insight manager should take
	 * responsibility for closing this it. If the previous connection was set with
	 * <code>closeable = TRUE</code>, then it is closed. If not, nothing happens
	 * to it and someone else is responsible for closing it
	 *
	 * @param rc the new connection
	 * @param closeable should this instance take responsibility for closing the
	 * connection later
	 */
	public void setConnection( RepositoryConnection rc, boolean closeable ) {
		if ( null != rc && closeRcOnRelease ) {
			try {
				rc.close();
			}
			catch ( RepositoryException re ) {
				log.warn( re, re );
			}
		}

		this.rc = rc;
		closeRcOnRelease = closeable;
	}

	protected RepositoryConnection getRawConnection() {
		return rc;
	}

	/**
	 * Loads data in the (legacy) properties format. Automatically upgrades the
	 * legacy parameter style, too
	 *
	 * @param dreamerProp the properties containing the perspective trees
	 */
	public final void loadLegacyData( Properties dreamerProp ) {
		String persps = dreamerProp.getProperty( Constants.PERSPECTIVE, "" );

		List<Perspective> perspectives = new ArrayList<>();

		log.debug( "Legacy Perspectives: " + persps );
		if ( !persps.isEmpty() ) {
			ValueFactory insightVF = rc.getValueFactory();

			Date now = new Date();
			Literal creator = insightVF.createLiteral( "Imported By "
					+ System.getProperty( "release.nameVersion", "VA SEMOSS" ) );

			UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
			for ( String pname : persps.split( ";" ) ) {
				Perspective p = new Perspective( pname );

				List<Insight> insights = loadLegacyQuestions( dreamerProp.getProperty( pname ),
						pname, dreamerProp, now, creator, urib );

				p.setInsights( insights );

				perspectives.add( p );
			}

			dreamerProp.remove( Constants.PERSPECTIVE );
		}

		try {
			rc.begin();
			// tag this data as an Insights dataset
			rc.add( MetadataConstants.VA_INSIGHTS, RDF.TYPE,
					MetadataConstants.INSIGHT_CORE_TYPE );
			for ( Perspective p : perspectives ) {
				rc.add( getStatements( p, null ) );
			}

			rc.commit();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.error( ee, ee );
			}
		}
	}

	private List<Insight> loadLegacyQuestions( String insightList, String pname,
			Properties dreamerProp, Date now, Literal creator, UriBuilder urib ) {
		List<Insight> insights = new ArrayList<>();

		// questions
		if ( insightList != null ) {
			for ( String insightKey : insightList.split( ";" ) ) {

				String insightLabel = dreamerProp.getProperty( insightKey );
				String legacyDataViewName
						= dreamerProp.getProperty( insightKey + "_" + Constants.LAYOUT );
				String sparql
						= dreamerProp.getProperty( insightKey + "_" + Constants.QUERY );

				dreamerProp.remove( insightKey );
				dreamerProp.remove( insightKey + "_" + Constants.LAYOUT );
				dreamerProp.remove( insightKey + "_" + Constants.QUERY );

				URI insightURI = urib.build( pname + "-" + insightKey );
				Insight ins = new Insight( insightURI, insightLabel );
				ins.setSparql( sparql );
				ins.setCreated( now );
				ins.setModified( now );
				ins.setOutput( legacyDataViewName );
				ins.setCreator( creator.stringValue() );

				Matcher m = LEGACYPAT.matcher( sparql );
				while ( m.find() ) {
					String var = m.group( 3 );
					String psql
							= dreamerProp.getProperty( insightKey + "_" + var + "_Query", "" );
					if ( !psql.isEmpty() ) {
						Parameter p = new Parameter( var );
						p.setDefaultQuery( psql );
						ins.getInsightParameters().add( p );
					}
				}

				upgradeIfLegacy( ins );

				insights.add( ins );
			}
		}
		return insights;
	}

	@Override
	public Collection<Perspective> getPerspectives() {
		List<Perspective> persps = new ArrayList<>();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					RDF.TYPE, VAS.Perspective, true ) );
			for ( Statement s : stmts ) {
				persps.add( getPerspective( URI.class.cast( s.getSubject() ) ) );
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return persps;
	}

	@Override
	public Perspective getSystemPerspective( IEngine eng ) {
		UriBuilder urib = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
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
				GraphPlaySheet.class );

		Insight explore = new Insight( "Explore an instance of a selected node type",
				"DESCRIBE ?instance", GraphPlaySheet.class );
		explore.setId( urib.uniqueUri() );

		Parameter concept = new Parameter( "Concept",
				"SELECT ?concept WHERE { ?concept rdfs:subClassOf <" + conceptUri + ">} " );
		concept.setId( urib.uniqueUri() );

		Parameter instance = new Parameter( "Instance", "SELECT ?instance WHERE { ?instance a ?concept }" );
		instance.setId( urib.uniqueUri() );

		explore.setParameters( Arrays.asList( concept, instance ) );

		String nespql = "CONSTRUCT { "
				+ "   ?instance ?p1 ?o1 ."
				+ "   ?o2 ?p2 ?instance ."
				+ "}"
				+ "WHERE {"
				+ "   OPTIONAL {"
				+ "     ?instance ?p1 ?o1 ."
				+ "   	?o1 rdfs:subClassOf <" + conceptUri + "> ."
				+ "   }"
				+ "   OPTIONAL {"
				+ "     ?o2 ?p2 ?instance ."
				+ "   	?o2 rdfs:subClassOf <" + conceptUri + "> ."
				+ "   }"
				+ "}";

		Insight neighbor = new Insight( "Show One Neighbor Away from Selected Node",
				nespql, GraphPlaySheet.class );
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
	@Override
	public Collection<Parameter> getParameters( Insight insight ) {
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

	@Override
	public List<Insight> getInsights( Perspective perspective ) {
		List<Insight> insights = new ArrayList<>();
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
				insights.add( getInsight( id ) );
			}
		}

		return insights;
	}

	@Override
	public Insight getInsight( URI insightURI ) {
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
				Collection<Parameter> params = getParameters( insight );
				insight.setParameters( params );
				upgradeIfLegacy( insight );
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

			perspective.setInsights( getInsights( perspective ) );
			return perspective;
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		throw new IllegalArgumentException( "unknown perspective: " + perspectiveURI );
	}

	@Override
	public Collection<Statement> getStatements() throws RepositoryException {
		return Iterations.asList( rc.getStatements( null, null, null, false ) );
	}

	@Override
	public void release() {
		if ( closeRcOnRelease ) {
			try {
				rc.close();
			}
			catch ( Exception e ) {
				log.error( "error releasing InsightEngine connection", e );
			}

			try {
				repo.shutDown();
			}
			catch ( Exception e ) {
				log.error( "error releasing InsightEngine repository", e );
			}
		}
	}

	protected Insight insightFromStatements( Collection<Statement> stmts ) {
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
				else if ( VAS.INSIGHT_OUTPUT_TYPE.equals( pred ) ) {
					try {
						insight.setOutputType( InsightOutputType.valueOf( obj.stringValue() ) );
					}
					catch ( IllegalArgumentException iae ) {
						log.warn( "unknown insight output type: " + obj.stringValue()
								+ "(using grid instead)" );
						insight.setOutputType( InsightOutputType.GRID );
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
				else if ( UI.viewClass.equals( pred ) ) {
					insight.setOutput( obj.stringValue() );
				}
			}
		}

		return insight;
	}

	private static void upgradeIfLegacy( Insight insight ) {
		// first, make sure we reference the current package names
		String legacyoutput = insight.getOutput();
		legacyoutput = legacyoutput.replaceFirst( "prerna", "gov.va.semoss" );
		insight.setOutput( legacyoutput.replaceFirst( "veera", "gov.va.vcamp" ) );

		// second, make sure our InsightOutputType matches our "Output" string
		// (use grid for a default [but how we'd ever have to use it is a mystery])
		if ( null == insight.getOutputType() ) {
			insight.setOutputType( UPGRADETYPEMAP.getOrDefault( insight.getOutput(),
					InsightOutputType.GRID ) );
		}

		// finally, see if we have super-legacy query data to worry about
		// there are two styles of legacy queries...
		// 1) <@X@> where X is {parameter name}-{URI}
		// 2) <@Y@> where Y is {parameter name}
		// in the first case, this insight has a parameter
		// in the second, the query is dependent on another parameter
		// since there's a bit of processing, we'll process the groups twice
		// once to see if we need to do it at all, and once to do the upgrade
		// (we can check all the sparql at once by concatenating them)
		StringBuilder legacychecker = new StringBuilder( insight.getSparql() );
		Collection<Parameter> oldparams = insight.getInsightParameters();

		for ( Parameter p : oldparams ) {
			legacychecker.append( p.getDefaultQuery() );
		}

		String oneline = legacychecker.toString().replaceAll( "\n", " " );
		Matcher m = LEGACYPAT.matcher( oneline );
		boolean islegacy = m.find();
		if ( islegacy ) {
			String legacySparql = insight.getSparql().replaceAll( "\n", " " );
			String newsparql = upgradeLegacySparql( legacySparql );
			insight.setSparql( newsparql );
			// some legacy insights specify parameters, but
			// some to rely on the legacy mappings instead
			Map<String, Parameter> pnames = new HashMap<>();
			for ( Parameter old : oldparams ) {
				pnames.put( old.getLabel(), old );
			}

			List<Parameter> newparams = new ArrayList<>();
			m.reset( legacySparql );
			while ( m.find() ) {
				String var = m.group( 3 );
				String type = m.group( 5 ); // can be null

				if ( null != type ) {
					// we have a type, so we can construct a sane Parameter if we don't
					// already have this variable covered					
					if ( pnames.containsKey( var ) ) {
						newparams.add( pnames.get( var ) );
					}
					else {
						Parameter p = new Parameter( var, "SELECT ?" + var
								+ " WHERE { ?" + var + " a <" + type + "> }" );
						newparams.add( p );
					}
				}
			}

			for ( Parameter p : newparams ) {
				m.reset( p.getDefaultQuery().replaceAll( "\n", " " ) );
				if ( m.find() ) {
					p.setDefaultQuery( upgradeLegacySparql( p.getDefaultQuery() ) );
				}
			}

			insight.setParameters( newparams );
		}
	}

	private static String upgradeLegacySparql( String oldsparql ) {
		Matcher m = LEGACYPAT.matcher( oldsparql.replaceAll( "\n", " " ) );
		// Matcher only supports StringBuffers, not StringBuilders
		StringBuffer insightSparql = new StringBuffer();
		while ( m.find() ) {
			boolean isbinding = ( null != m.group( 2 ) );
			String bindvar = ( m.group( 7 ) ); // can be null
			String var = m.group( 3 );

			if ( isbinding ) {
				if ( !var.equals( bindvar ) ) {
					m.appendReplacement( insightSparql, "BIND( ?" + var
							+ " AS ?" + bindvar + " )" );
				}
				else {
					// skip it...we'll use the parameter to do the binding
					m.appendReplacement( insightSparql, "" );
				}
			}
			else {
				m.appendReplacement( insightSparql, "?" + var );
			}
		}

		m.appendTail( insightSparql );

		return insightSparql.toString();
	}

	/**
	 * Converts the Perspective, its Insights and Parameters (and ordering!) to
	 * Statements for adding to a Repository. If any Perspective, Insight, or
	 * Parameter is missing an ID, a new one will be generated for it, and set on
	 * the object
	 *
	 * @param p the perspective to convert.
	 * @return a list of statements that completely represent the perspective tree
	 */
	public static Collection<Statement> getStatements( Perspective p, User user ) {
		List<Statement> statements = new ArrayList<>();
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );

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

		statements.add( new StatementImpl( pid, RDF.TYPE, VAS.Perspective ) );
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

		statements.add( new StatementImpl( iid, VAS.INSIGHT_OUTPUT_TYPE,
				vf.createLiteral( insight.getOutputType().toString() ) ) );

		statements.add( new StatementImpl( iid, RDFS.SUBCLASSOF, VAS.InsightProperties ) );
		statements.add( new StatementImpl( iid, DCTERMS.CREATED,
				vf.createLiteral( null == insight.getCreated() ? new Date()
								: insight.getCreated() ) ) );
		statements.add( new StatementImpl( iid, DCTERMS.MODIFIED,
				vf.createLiteral( new Date() ) ) );
		statements.add( new StatementImpl( iid, DCTERMS.CREATOR,
				vf.createLiteral( getAuthorInfo( user ) ) ) );
		URI outputuri = vf.createURI( VAS.NAMESPACE, insight.getOutput() );
		statements.add( new StatementImpl( iid, UI.dataView, outputuri ) );

		// add the dataview objects, too (these'll be added multiple times, but
		// all the values will be identical)
		PlaySheetEnum pse = PlaySheetEnum.valueForInsight( insight );
		statements.add( new StatementImpl( outputuri, RDF.TYPE, UI.dataView ) );
		statements.add( new StatementImpl( outputuri, UI.viewClass,
				vf.createLiteral( insight.getOutput() ) ) );
		statements.add( new StatementImpl( outputuri, VAS.icon,
				vf.createLiteral( pse.getSheetIconName() ) ) );
		statements.add( new StatementImpl( outputuri, RDFS.LABEL,
				vf.createLiteral( pse.getDisplayName() ) ) );

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
}
