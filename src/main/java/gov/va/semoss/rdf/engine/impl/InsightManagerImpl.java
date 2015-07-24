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
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.util.UriBuilder;

import java.util.Collections;
import java.util.Comparator;

import java.util.HashMap;
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
					rc.add( perspectiveURI, DCTERMS.DESCRIPTION,
							insightVF.createLiteral( "Test Description: " + perspective ) );

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
//    Utility.showMessage(String.valueOf(deleteInsightData()));    
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

					String type = null;
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
	public String getLabel( URI uri ) {
		String label = uri.toString();
		try {
			List<Statement> stmts
					= Iterations.asList( rc.getStatements( uri, RDFS.LABEL, null, false ) );
			if ( !stmts.isEmpty() ) {
				Value obj = stmts.get( 0 ).getObject();
				label = obj.stringValue();
			}
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}

		return label;
	}

	@Override
	// presently this method has no callers
	public String getOrderedLabel( URI perspectiveURI, URI insightURI ) {
		final StringBuffer label = new StringBuffer( insightURI.toString() );
		try {
			String q = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
					+ "SELECT ?label ?order WHERE { "
					+ "<" + perspectiveURI.toString() + "> olo:slot [ "
					+ "olo:item ?uri ; olo:order ] . "
					+ "<" + insightURI.toString() + "> rdfs:label ?label }";

			QueryExecutor<Void> qea = new QueryExecutorAdapter<Void>( q ) {
				@Override
				public void handleTuple( BindingSet resultSet, ValueFactory fac ) {
					label.setLength( 0 ); // flush
					label.append( resultSet.getBinding( "order" ).getValue().stringValue() );
					label.append( ". " );
					label.append( resultSet.getBinding( "label" ).getValue().stringValue() );
				}
			};
			AbstractSesameEngine.getSelect( qea, rc, false );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}

		return label.toString();
	}

	@Override
	public Collection<Perspective> getPerspectives() {
		List<Perspective> uris = new ArrayList<>();
		try {
			String q = "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
					+ "SELECT ?uri ?label ?description WHERE { "
					+ "?uri a <" + VAS.Perspective + "> . "
					+ "?uri rdfs:label ?label ."
					+ "OPTIONAL { ?uri dcterms:description ?description} } ORDER BY UCASE(?label)";
			ListQueryAdapter<Perspective> lqa = new ListQueryAdapter<Perspective>( q ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					String description = "";
					if ( set.hasBinding( "description" ) ) {
						description = set.getValue( "description" ).stringValue();
					}
					add( new Perspective( URI.class.cast( set.getValue( "uri" ) ),
							set.getValue( "label" ).stringValue(), description ) );
				}
			};

			uris.addAll( AbstractSesameEngine.getSelect( lqa, rc, true ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}

		return uris;
	}

	/**
	 * Gets all Parameter objects under the passed-in Insight URI.
	 *
	 * @param insightURI -- (URI) An Insight URI.
	 *
	 * @return -- (Collection<Parameter>) Described above.
	 */
	@Override
	public Collection<Parameter> getInsightParameters( URI insightURI ) {
		List<Parameter> colInsightParameters = new ArrayList<>();
		String insightUriString = "<" + insightURI.toString() + ">";

		try {
			String query = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
					+ "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
					+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
					+ "SELECT DISTINCT ?parameter ?parameterLabel ?parameterVariable ?parameterValueType ?parameterQuery WHERE { "
					+ "BIND(" + insightUriString + " AS ?uri) . "
					+ "OPTIONAL{ ?uri spin:constraint ?parameter . "
					+ "?parameter spl:valueType ?parameterValueType ; rdfs:label ?parameterLabel ; spl:predicate [ rdfs:label ?parameterVariable ] . "
					+ "OPTIONAL{?parameter sp:query [ sp:text ?parameterQuery ] } } }";

			ListQueryAdapter<Parameter> lqa = new ListQueryAdapter<Parameter>( query ) {
				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					Parameter parameter = new Parameter();
					parameter.setFromResultSet( set );
					add( parameter );
				}
			};
			log.debug( "Parameter query... " + query );
			colInsightParameters.addAll( AbstractSesameEngine.getSelect( lqa, rc, true ) );

		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
		return colInsightParameters;
	}

	@Override
	public List<Insight> getInsights( Perspective perspective ) {
		List<Insight> uris = new ArrayList<>();
		if ( perspective != null ) {
			try {
				List<Statement> stmts
						= Iterations.asList( rc.getStatements( perspective.getUri(),
										OLO.slot, null, true ) );
				for ( Statement st : stmts ) {
					URI slot = URI.class.cast( st.getObject() );
					List<Statement> uriStmts
							= Iterations.asList( rc.getStatements( slot, OLO.item, null, true ) );
					for ( Statement uriSt : uriStmts ) {
						URI uri = URI.class.cast( uriSt.getObject() );
						uris.add( getInsight( perspective.getUri(), uri ) );
					}
				}
			}
			catch ( RepositoryException e ) {
				log.error( e, e );
			}

			final URI perspectiveURI = perspective.getUri();
			Collections.sort( uris, new Comparator<Insight>() {

				@Override
				public int compare( Insight t, Insight t1 ) {
					return t.getOrder() - t1.getOrder();
				}
			} );
		}
		return uris;
	}

	@Override
	public Insight getInsight( URI perspectiveURI, URI insightURI ) {
		final Insight insight = new Insight();
		try {
			insight.setId( insightURI );

			Map<String, String> namespaces = new HashMap<>();
			namespaces.put( UI.PREFIX, UI.NAMESPACE );
			namespaces.put( SPIN.PREFIX, SPIN.NAMESPACE );
			namespaces.put( SP.PREFIX, SP.NAMESPACE );
			namespaces.put( SPL.PREFIX, SPL.NAMESPACE );
			namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
			namespaces.put( OLO.PREFIX, OLO.NAMESPACE );
			namespaces.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
			
			String isp = "SELECT ?insightLabel ?sparql ?viewClass  ?parameterVariable ?parameterLabel ?parameterValueType ?parameterQuery ?rendererClass ?isLegacy ?perspective ?description ?creator ?created ?modified ?order WHERE { "
					+ "?insightUriString rdfs:label ?insightLabel ; ui:dataView [ ui:viewClass ?viewClass ] . "
					+ "OPTIONAL{ ?insightUriString spin:body [ sp:text ?sparql ] } "
					+ "OPTIONAL{ ?insightUriString spin:constraint ?parameter . "
					+ "?parameter spl:valueType ?parameterValueType ; rdfs:label ?parameterLabel ; spl:predicate [ rdfs:label ?parameterVariable ] . OPTIONAL{?parameter sp:query [ sp:text ?parameterQuery ] }} "
					+ "OPTIONAL{ ?insightUriString vas:rendererClass ?rendererClass } "
					+ "OPTIONAL{ ?insightUriString vas:isLegacy ?isLegacy } "
					+ "OPTIONAL{ ?insightUriString dcterms:description ?description } "
					+ "OPTIONAL{ ?insightUriString dcterms:creator ?creator } "
					+ "OPTIONAL{ ?insightUriString dcterms:created ?created } "
					+ "OPTIONAL{ ?insightUriString dcterms:modified ?modified } "
					+ "OPTIONAL{ ?perspective olo:slot [ olo:item ?insightUriString; olo:index ?order ] } "
					+ "}";
			QueryExecutor<Void> qea = new QueryExecutorAdapter<Void>( isp ) {

				@Override
				public void handleTuple( BindingSet resultSet, ValueFactory fac ) {
					insight.setFromResultSet( resultSet );
					log.debug( insight );
				}
			};

			log.debug( "Insighter... " + isp + " / " + insightURI );
			qea.bind( "insightUriString", insightURI );
			qea.bind( "perspective", perspectiveURI );
			qea.setNamespaces( namespaces );

			log.debug( AbstractSesameEngine.processNamespaces( qea.bindAndGetSparql(), namespaces) );
			
			AbstractSesameEngine.getSelect( qea, rc, false );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			// TODO Auto-generated catch block
			log.error( e, e );
		}
		if ( null == insight.getId() ) {
			// didn't set anything from our SparQL
			insight.setLabel( insightURI.toString() );
			insight.setOutput( "Unknown" );
			insight.setId( Constants.ANYNODE ); // error, so we could use any URI here
			insight.setSparql( "This will not work" );
			log.debug( "Using Label ID Hash " );
		}
		return insight;
	}

	/**
	 * Returns a collection of data about the playsheets used to render Insights.
	 *
	 * @return -- (Collection<PlaySheet>) Described above.
	 */
	@Override
	public Collection<PlaySheet> getPlaySheets() {
		final Collection<PlaySheet> colPlaysheet = new ArrayList<PlaySheet>();

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
}
