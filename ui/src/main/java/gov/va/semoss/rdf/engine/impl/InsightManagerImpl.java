/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import info.aduna.iteration.Iterations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

import gov.va.semoss.model.vocabulary.ARG;
import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;

import org.openrdf.repository.Repository;

/**
 *
 * @author ryan
 */
public class InsightManagerImpl implements InsightManager {

	private static final Logger log = Logger.getLogger( InsightManagerImpl.class );
	private RepositoryConnection rc;
	private Repository repo = null;
	private final Pattern pattern = Pattern.compile( "^(\\w+)(.*)$" );
	private boolean closeRcOnRelease = false;

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

	public final void loadAllPerspectives( Properties dreamerProp ) {
		try {
			// this should load the properties from the specified as opposed to
			// loading from core prop
			// lastly the localprop needs to set up so that it can be swapped
			String persps = dreamerProp.getProperty( Constants.PERSPECTIVE, "" );
			rc.begin();

			// tag this data as an Insights dataset
			rc.add( MetadataConstants.VA_INSIGHTS, RDF.TYPE,
					MetadataConstants.INSIGHT_CORE_TYPE );

			log.debug( "Perspectives " + persps );
			if ( !persps.isEmpty() ) {
				ValueFactory insightVF = rc.getValueFactory();

				Literal now = insightVF.createLiteral( new Date() );
				Literal creator = insightVF.createLiteral( "Imported By "
						+ System.getProperty( "release.nameVersion", "VA SEMOSS" ) );

				for ( String perspective : persps.split( ";" ) ) {
					URI perspectiveURI
							= insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, perspective );
					// rc.add( engine, UI.element, perspectiveURI );
					rc.add( perspectiveURI, RDF.TYPE, VAS.Perspective );
					rc.add( perspectiveURI, RDFS.LABEL, insightVF.createLiteral( perspective ) );

					rc.add( perspectiveURI, DCTERMS.CREATED, now );
					rc.add( perspectiveURI, DCTERMS.MODIFIED, now );
					rc.add( perspectiveURI, DCTERMS.CREATOR, creator );

					//REMOVE THIS Line for Production:
//					rc.add( perspectiveURI, DCTERMS.DESCRIPTION,
//							insightVF.createLiteral( "Test Description: " + perspective ) );
					loadQuestions( perspective, perspectiveURI, dreamerProp, now, creator );
				}

				dreamerProp.remove( Constants.PERSPECTIVE );
			}

			rc.commit();

			if ( log.isTraceEnabled() ) {
				File dumpfile
						= new File( FileUtils.getTempDirectory(), "semoss-outsights-dump.ttl" );
				try ( Writer w = new BufferedWriter( new FileWriter( dumpfile ) ) ) {
					w.write(
							"# baseURI: http://va.gov/vcamp/data/insights/dump\n# imports: http://va.gov/vcamp/semoss/tool\n" );
					ValueFactory vf = rc.getValueFactory();
					URI base = vf.createURI( "http://va.gov/vcamp/data/insights/dump" );
					rc.add( base, RDF.TYPE, OWL.ONTOLOGY );
					rc.add( base, OWL.IMPORTS,
							vf.createURI( "http://va.gov/vcamp/semoss/tool" ) );
					TurtleWriter tw = new TurtleWriter( w );
					tw.handleNamespace( "insights", "http://va.gov/vcamp/data/insights#" );
					tw.handleNamespace( VAS.PREFIX, VAS.NAMESPACE );
					tw.handleNamespace( OWL.PREFIX, OWL.NAMESPACE );
					tw.handleNamespace( RDF.PREFIX, RDF.NAMESPACE );
					tw.handleNamespace( RDFS.PREFIX, RDFS.NAMESPACE );
					tw.handleNamespace( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
					tw.handleNamespace( SP.PREFIX, SP.NAMESPACE );
					tw.handleNamespace( SPL.PREFIX, SPL.NAMESPACE );
					tw.handleNamespace( SPIN.PREFIX, SPIN.NAMESPACE );
					tw.handleNamespace( ARG.PREFIX, ARG.NAMESPACE );
					rc.export( tw );
				}
				catch ( RDFHandlerException | IOException ioe ) {
					log.trace( "could not write insights dump", ioe );
				}
			}

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
		//This is a private utility method of this class, that is only being run
		//to remove old PlaySheet triples, left over before changes to the Insight
		//KB were made on 4 April 2015. This call may be removed in the future:
//    GuiUtility.showMessage(String.valueOf(deleteInsightData()));    
	}

	private void loadQuestions( String perspectiveKey, URI perspectiveURI,
			Properties dreamerProp, Literal now, Literal creator ) {
		try {
			String insightList = dreamerProp.getProperty( perspectiveKey ); // get the ; delimited

			// probably not referenced again:
			dreamerProp.remove( perspectiveKey );
			dreamerProp.put( perspectiveURI, insightList );

			// questions
			if ( insightList != null ) {
				ValueFactory vf = rc.getValueFactory();

				int order = 1;
				for ( String insightKey : insightList.split( ";" ) ) {
					String insightLabel = dreamerProp.getProperty( insightKey );
					String legacyDataViewName
							= dreamerProp.getProperty( insightKey + "_" + Constants.LAYOUT );
					String sparql
							= dreamerProp.getProperty( insightKey + "_" + Constants.QUERY );

					dreamerProp.remove( insightKey );
					dreamerProp.remove( insightKey + "_" + Constants.LAYOUT );
					dreamerProp.remove( insightKey + "_" + Constants.QUERY );

					UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
					URI insightURI = urib.add( insightKey ).build();

					URI slot = vf.createURI( MetadataConstants.VA_INSIGHTS_NS,
							perspectiveKey + "-slot-" + order );
					rc.add( perspectiveURI, OLO.slot, slot );
					rc.add( slot, OLO.item, insightURI );
					rc.add( slot, OLO.index, vf.createLiteral( order ) );

					String dataViewName = legacyDataViewName.replaceFirst( "prerna", "gov.va.semoss" );
					dataViewName = dataViewName.replaceFirst( "veera", "gov.va.vcamp" );
					URI dataViewURI = vf.createURI( VAS.NAMESPACE, dataViewName );

					String type = "SELECT";
					Matcher matcher = pattern.matcher( sparql );
					if ( matcher.find() ) {
						type = matcher.group( 1 );
					}

					URI spinBody = vf.
							createURI( MetadataConstants.VA_INSIGHTS_NS, insightKey + "-" + type );
					// The *_Questions.properties files have only SELECT and CONSTRUCT queries:
					if ( "SELECT".equals( type.toUpperCase() ) ) {
						rc.add( spinBody, RDF.TYPE, SP.Select );
					}
					else {
						rc.add( spinBody, RDF.TYPE, SP.Construct );
					}
					// TODO: The following works fine and the query text is correct in RDF (verified via Insights export)
					// However, following retrieval from the insights-kb, the quotation marks are being stripped away
					// which then makes the query text invalid.  Trace this down and address.  IO5 is a good example to
					// work with, change 'M' to "M" for testing.
					rc.add( spinBody, SP.text, vf.createLiteral( sparql.replaceAll( "\"", "\\\"" ) ) ); // verify this

					rc.add( insightURI, RDF.TYPE, SPIN.MagicProperty );
					rc.add( insightURI, RDFS.SUBCLASSOF, VAS.InsightProperties );
					rc.add( insightURI, RDFS.LABEL, vf.createLiteral( insightLabel ) );
					rc.add( insightURI, SPIN.body, spinBody );
					rc.add( insightURI, UI.dataView, dataViewURI );
					rc.add( insightURI, VAS.rendererClass, vf.createLiteral( "" ) );
					rc.add( insightURI, VAS.isLegacy, vf.createLiteral( true ) );

					//REMOVE THIS Line for Production:
					rc.add( insightURI, DCTERMS.DESCRIPTION, vf.createLiteral( "Test Description: " + insightURI.toString() ) );

					rc.add( insightURI, DCTERMS.CREATED, now );
					rc.add( insightURI, DCTERMS.MODIFIED, now );
					rc.add( insightURI, DCTERMS.CREATOR, creator );

					// load it with the entity keys
					Map<String, String> paramHash = Utility.getParams( sparql );

					// need to find a way to handle multiple param types
					for ( String param : paramHash.keySet() ) {
						String paramKey = param.substring( 0, param.indexOf( "-" ) );
						String paramType = param.substring( param.indexOf( "-" ) + 1 );

						URI argumentURI = vf.createURI( MetadataConstants.VA_INSIGHTS_NS,
								insightKey + "-" + paramKey );
						rc.add( insightURI, SPIN.constraint, argumentURI );

						rc.add( argumentURI, RDF.TYPE, SPL.Argument );
						rc.add( argumentURI, RDFS.LABEL, vf.createLiteral( paramKey.replaceAll( "([a-z])([A-Z])", "$1 $2" ) ) );

						URI parameterURI = vf.createURI( ARG.NAMESPACE, paramKey );
						rc.add( parameterURI, RDF.TYPE, RDF.PROPERTY );
						rc.add( parameterURI, RDFS.LABEL, vf.createLiteral( paramKey ) );
						rc.add( argumentURI, SPL.predicate, parameterURI );
						rc.add( argumentURI, SPL.valueType, vf.createURI( paramType ) );

						/*
						 * Add the default query that retrieves parameter options:
						 */
						URI paramQuery = vf.
								createURI( MetadataConstants.VA_INSIGHTS_NS, insightKey + "-" + paramKey + "-Query" );
						rc.add( paramQuery, RDF.TYPE, SP.Select );
						rc.add( paramQuery, SP.text, vf.createLiteral( "SELECT ?" + paramKey + " ?label WHERE{ ?" + paramKey + " a <" + paramType + "> ; rdfs:label ?label }" ) );
						rc.add( argumentURI, SP.query, paramQuery );

						/*
						 * Special case for the Generic-Perspective which has two dependent
						 * queries
						 */
						if ( insightKey.equals( "GQ1" ) ) {
							String queryParamText
									= dreamerProp.getProperty( "GQ1_" + paramKey + "_QUERY" );

							URI queryParamURI = vf.createURI( MetadataConstants.VA_INSIGHTS_NS,
									insightKey + "-" + paramKey + "-Query" );
							rc.add( argumentURI, SPL.defaultValue, queryParamURI );
							rc.add( queryParamURI, RDF.TYPE, SP.Select );
							rc.add( queryParamURI, SP.text, vf.createLiteral( queryParamText ) );
						}
					}

					order++;
				}
				log.info( "Loaded Perspective " + perspectiveKey );
			}
		}
		catch ( RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}

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

	/**
	 * Gets all Parameter objects under the passed-in Insight URI.
	 *
	 * @param insightURI -- (URI) An Insight URI.
	 *
	 * @return -- (Collection<Parameter>) Described above.
	 */
	@Override
	public Collection<Parameter> getInsightParameters( Insight insight ) {
		List<Parameter> colInsightParameters = new ArrayList<>();

		try {
			// get this insight's constraints/parameters
			Collection<Statement> paramIds = Iterations.asList( rc.getStatements( insight.getId(),
					SPIN.constraint, null, false ) );
			for ( Statement s : paramIds ) {
				log.debug( s );

				URI paramId = URI.class.cast( s.getObject() );
				Parameter parameter = new Parameter();
				parameter.setParameterId( paramId );

				// get data about this parameter
				Collection<Statement> data
						= Iterations.asList( rc.getStatements( paramId, null, null, false ) );
				for ( Statement d : data ) {
					log.debug( "    " + d );

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
							parameter.setVariable( preddata.get( 0 ).getObject().stringValue() );
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
			orderquery.bind( "perspective", perspective.getUri() );
			orderquery.addNamespace( OLO.PREFIX, OLO.NAMESPACE );

			List<URI> insightUris
					= AbstractSesameEngine.getSelectNoEx( orderquery, rc, true );

			for ( URI id : insightUris ) {
				insights.add( getInsight( perspective.getUri(), id ) );
			}
		}

		return insights;
	}

	@Override
	public Insight getInsight( URI perspectivexsURI, URI insightURI ) {
		return getInsight( insightURI );
	}
	
	@Override
	public Insight getInsight( URI insightURI ){
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

			// dataview
			Insight insight = insightFromStatements( stmts );

			// finally, set the parameters
			Collection<Parameter> params = getInsightParameters( insight );
			for ( Parameter p : params ) {
				insight.setParameter( p.getVariable(), p.getLabel(), p.getParameterType(),
						p.getDefaultQuery() );
			}

			return insight;
		}
		catch ( RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}

		throw new IllegalArgumentException( "unknown insight: " + insightURI );
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

	/**
	 * Returns a collection of data about the playsheets used to render Insights.
	 *
	 * @return -- (Collection<PlaySheet>) Described above.
	 */
	@Override
	public Collection<PlaySheet> getPlaySheets() {
		final Collection<PlaySheet> colPlaysheet = new ArrayList<>();

		try {
			String query = "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
					+ "PREFIX " + UI.PREFIX + ": <" + UI.NAMESPACE + "> "
					+ "SELECT DISTINCT ?viewClass ?icon ?label ?description "
					+ "WHERE{ ?dataView a vas:DataView . "
					+ "?dataView ui:viewClass ?viewClass . "
					+ "OPTIONAL{ ?dataView vas:icon ?icon } "
					+ "OPTIONAL{ ?dataView rdfs:label ?label } "
					+ "OPTIONAL{ ?dataView <http://va.gov/ontologies/core#description> ?description } } "
					+ "ORDER BY ASC(?label)";

			ListQueryAdapter<PlaySheet> lqa = new ListQueryAdapter<PlaySheet>( query ) {
				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					PlaySheet playsheet = new PlaySheet();
					playsheet.setFromResultSet( set );
					add( playsheet );
				}
			};
			log.debug( "Playsheet Query... " + query );
			colPlaysheet.addAll( AbstractSesameEngine.getSelect( lqa, rc, true ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}

		return colPlaysheet;
	}

	/**
	 * Returns a collection of Parameter Types from the main KB, for use in the
	 * "Parameter Types" combo-box on the "Parameter" tab of the Insight Manager.
	 *
	 * @return -- (Collection<ParameterType>) Described above.
	 */
	@Override
	public Collection<ParameterType> getParameterTypes() {
		final Collection<ParameterType> colParameterType = new ArrayList<>();
		IEngine engine = DIHelper.getInstance().getRdfEngine();
		SesameJenaSelectWrapper wrapper = new SesameJenaSelectWrapper();

		wrapper.setEngine( engine );

		String query = "SELECT DISTINCT ?parameterClass ?parameterLabel WHERE { {"
				+ "?parameterClass rdfs:subClassOf <http://semoss.org/ontologies/Concept> . "
				+ "?parameterClass rdfs:label ?parameterLabel "
				+ "FILTER( ?parameterClass != <http://semoss.org/ontologies/Concept> && "
				+ "?parameterClass != <http://www.w3.org/2004/02/skos/core#Concept>) } "
				+ "UNION { BIND(owl:Nothing AS ?parameterClass) . "
				+ "BIND(\"(Unselected)\" AS ?parameterLabel) } "
				+ "UNION{ BIND(<http://semoss.org/ontologies/Concept> AS ?parameterClass) . "
				+ "BIND(\"*Concept\" AS ?parameterLabel) } } ORDER BY ?parameterLabel";

		wrapper.setQuery( query );
		wrapper.executeQuery();
		engine.commit();
		String[] vars = wrapper.getVariables();
		while ( wrapper.hasNext() ) {
			SesameJenaSelectStatement stmt = wrapper.next();
			String parameterClass = stmt.getRawVar( vars[0] ) + "";
			String parameterLabel = stmt.getVar( vars[1] ).toString();
			ParameterType parameterType = new ParameterType( parameterLabel, parameterClass );
			colParameterType.add( parameterType );
		}
		log.debug( "ParameterType Query... " + query );

		return colParameterType;
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
				else if ( VAS.isLegacy.equals( pred ) ) {
					insight.setLegacy( obj.booleanValue() );
				}
				else if ( DCTERMS.CREATOR.equals( pred ) ) {
					insight.setCreator( obj.stringValue() );
				}
				else if ( DCTERMS.CREATED.equals( pred ) ) {
					insight.setCreated( obj.stringValue() );
				}
				else if ( DCTERMS.MODIFIED.equals( pred ) ) {
					insight.setModified( obj.stringValue() );
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
}
