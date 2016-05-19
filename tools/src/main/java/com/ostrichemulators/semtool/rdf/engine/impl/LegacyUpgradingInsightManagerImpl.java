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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import com.ostrichemulators.semtool.om.Insight;
import com.ostrichemulators.semtool.om.InsightOutputType;
import com.ostrichemulators.semtool.om.Parameter;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.om.Perspective;

import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public class LegacyUpgradingInsightManagerImpl extends InsightManagerImpl {

	private static final Logger log = Logger.getLogger( LegacyUpgradingInsightManagerImpl.class );
	private static final Pattern LEGACYPAT
			= Pattern.compile( "((BIND\\s*\\(\\s*)?<@(\\w+)((?:-)([^@]+))?@>(\\s*AS\\s+\\?(\\w+)\\s*\\)\\s*\\.?\\s*)?)" );
	// strictly for upgrading old insights
	private static final Map<String, InsightOutputType> UPGRADETYPEMAP = new HashMap<>();
	private final UriBuilder urib = UriBuilder.getBuilder( SEMPERS.NAMESPACE );
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

	public LegacyUpgradingInsightManagerImpl() {

	}

	public LegacyUpgradingInsightManagerImpl( InsightManager im ) {
		super( im );
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
					+ Utility.getBuildProperties( Utility.class ).getProperty( "name",
							"OS-EM Semantic Toolkit" ) );

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
}
