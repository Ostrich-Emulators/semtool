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
import gov.va.semoss.user.User;
import gov.va.semoss.util.UriBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
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
	// strictly for upgrading old insights
	private static final Map<String, InsightOutputType> UPGRADETYPEMAP = new HashMap<>();
	private UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
	private final Map<URI, Insight> insights = new HashMap<>();
	private final List<Perspective> perspectives = new ArrayList<>();

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
				InsightOutputType.GRAPH_METAMODEL );
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

	/**
	 * Loads data in the (legacy) properties format. Automatically upgrades the
	 * legacy parameter style, too
	 *
	 * @param dreamerProp the properties containing the perspective trees
	 */
	public final void loadLegacyData( Properties dreamerProp ) {
		String persps = dreamerProp.getProperty( Constants.PERSPECTIVE, "" );

		perspectives.clear();

		log.debug( "Legacy Perspectives: " + persps );
		if ( !persps.isEmpty() ) {
			ValueFactory insightVF = new ValueFactoryImpl();

			Date now = new Date();
			Literal creator = insightVF.createLiteral( "Imported By "
					+ System.getProperty( "release.nameVersion", "VA SEMOSS" ) );

			for ( String pname : persps.split( ";" ) ) {
				Perspective p = new Perspective( pname );
				p.setId( urib.build( pname ) );

				List<Insight> insightlist = loadLegacyQuestions( dreamerProp.getProperty( pname ),
						pname, dreamerProp, now, creator, urib );
				for ( Insight i : insightlist ) {
					insights.put( i.getId(), i );
				}

				p.setInsights( insightlist );

				perspectives.add( p );
			}

			dreamerProp.remove( Constants.PERSPECTIVE );
		}
	}

	private List<Insight> loadLegacyQuestions( String insightnames, String pname,
			Properties dreamerProp, Date now, Literal creator, UriBuilder urib ) {
		List<Insight> list = new ArrayList<>();

		// questions
		if ( insightnames != null ) {
			for ( String insightKey : insightnames.split( ";" ) ) {

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

				ins.setOutput( upgradeOutput( legacyDataViewName ) );
				ins.setCreator( creator.stringValue() );

				Matcher m = LEGACYPAT.matcher( sparql );
				while ( m.find() ) {
					String var = m.group( 3 );
					String psql
							= dreamerProp.getProperty( insightKey + "_" + var + "_Query", "" );
					if ( !psql.isEmpty() ) {
						Parameter p = new Parameter( var );
						p.setId( urib.build( pname + "-" + insightKey + "-" + var ) );
						p.setDefaultQuery( psql );
						ins.getInsightParameters().add( p );
					}
				}

				upgradeIfLegacy( ins, urib );

				list.add( ins );
			}
		}
		return list;
	}

	@Override
	public Collection<Perspective> getPerspectives() {
		return new ArrayList<>( perspectives );
	}

	public static InsightManager createFromRepository( Repository repo ) {
		InsightManagerImpl imi = new InsightManagerImpl();
		try {
			if ( !repo.isInitialized() ) {
				repo.initialize();
			}
			RepositoryConnection rc = repo.getConnection();
			imi.loadFromRepository( rc );
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}
		return imi;
	}

	public void loadFromRepository( RepositoryConnection rc ) {
		List<Perspective> persps = new ArrayList<>();
		try {
			List<Statement> stmts = Iterations.asList( rc.getStatements( null,
					RDF.TYPE, VAS.Perspective, true ) );
			Map<Perspective, Integer> ordering = new HashMap<>();
			for ( Statement s : stmts ) {
				Perspective p = loadPerspective( URI.class.cast( s.getSubject() ), rc );

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
				"SELECT ?concept WHERE { ?concept rdfs:subClassOf <" + conceptUri + ">} " );
		concept.setId( urib.uniqueUri() );

		Parameter instance = new Parameter( "Instance", "SELECT ?instance WHERE { ?instance a ?concept }" );
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
	private Collection<Parameter> loadParameters( Insight insight, RepositoryConnection rc ) {
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
		return new ArrayList<>();
	}

	public List<Insight> loadInsights( Perspective perspective, RepositoryConnection rc ) {
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
				list.add( loadInsight( id, rc ) );
			}
		}

		return list;
	}

	@Override
	public Insight getInsight( URI insightURI ) {
		return insights.get( insightURI );
	}

	public Insight loadInsight( URI insightURI, RepositoryConnection rc ) {

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
				upgradeIfLegacy( insight, urib );
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

	public Perspective loadPerspective( URI perspectiveURI, RepositoryConnection rc ) {
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

			perspective.setInsights( loadInsights( perspective, rc ) );
			return perspective;
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
		throw new IllegalArgumentException( "unknown perspective: " + perspectiveURI );
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
				else if ( VAS.INSIGHT_OUTPUT_TYPE.equals( pred ) ) {
					insight.setOutput( InsightOutputType.valueOf( obj.stringValue() ) );
				}
				else if( UI.viewClass.equals( pred ) && null == insight.getOutput() ){
					insight.setOutput( upgradeOutput( obj.stringValue() ) );
				}
			}
		}

		// make sure every insight has an output type
		if ( null == insight.getOutput() ) {
			insight.setOutput( InsightOutputType.GRID );
		}

		return insight;
	}

	private static InsightOutputType upgradeOutput( String legacyoutput ) {
		// first, make sure we reference the current package names

		legacyoutput = legacyoutput.replaceFirst( "prerna", "gov.va.semoss" );
		legacyoutput = legacyoutput.replaceFirst( "veera", "gov.va.vcamp" );

		// second, make sure our InsightOutputType matches our "Output" string
		// (use grid for a default)
		return UPGRADETYPEMAP.getOrDefault( legacyoutput, InsightOutputType.GRID );
	}

	private static void upgradeIfLegacy( Insight insight, UriBuilder urib ) {

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
						p.setId( urib.build( insight.getLabel() + "-" + var ) );
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

	public static Collection<Statement> getStatements( InsightManager im, User user ) {
		List<Statement> statements = new ArrayList<>();
		int idx = 0;
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
	 * @return a list of statements that completely represent the perspective tree
	 */
	public static Collection<Statement> getStatements( Perspective p, User user ) {
		List<Statement> statements = new ArrayList<>();
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );

		// if we're creating statements, mark our repository as an insights db
		statements.add( new StatementImpl( MetadataConstants.VA_INSIGHTS, RDF.TYPE,
				MetadataConstants.INSIGHT_CORE_TYPE ) );

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

		if ( null != insight.getOutput() ) {
			statements.add( new StatementImpl( iid, VAS.INSIGHT_OUTPUT_TYPE,
					vf.createLiteral( insight.getOutput().toString() ) ) );
		}

		statements.add( new StatementImpl( iid, RDFS.SUBCLASSOF, VAS.InsightProperties ) );
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
