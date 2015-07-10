package gov.va.semoss.ui.components;

import gov.va.semoss.model.vocabulary.VAS;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.AbstractSesameEngine;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridPlaySheet;
import gov.va.semoss.ui.helpers.NonLegacyQueryBuilder;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.util.QuestionPlaySheetStore;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;

import java.util.HashMap;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public class ExecuteQueryProcessor {

	private static final Logger logger = Logger.getLogger( ExecuteQueryProcessor.class );
	private boolean append = false;
	private IPlaySheet playSheet = null;
	private Perspective perspective = null;

	public void setAppendBoolean( boolean append ) {
		this.append = append;
	}

	public void setPlaySheet( IPlaySheet playSheet ) {
		this.playSheet = playSheet;
	}

	public IPlaySheet getPlaySheet() {
		return playSheet;
	}

	/**
	 * This property enables the "ProcessQueryListener" to set the Perspective
	 * Name, which is then passed on to the "AbstractRDFPlaySheet".
	 *
	 * @param perValue -- (Perspective) the currently selected Perspective.
	 */
	public void setPerspective( Perspective perValue ) {
		this.perspective = perValue;
	}

	public Perspective getPerspectiveName() {
		return perspective;
	}

	/**
	 * Processes queries from the Custom Sparql Query window having play-sheets or
	 * database-modification queries. (If the passed-in "playSheetString" is
	 * "Update Query", then no playsheet is activated, but the database is
	 * modified instead.)
	 *
	 * @param query -- (String) Sparql query as entered into the Custom Sparql
	 * Query window.
	 *
	 * @param playSheetString -- (String) An option as displayed in the play-sheet
	 * dropdown.
	 * @param appending
	 * @return
	 */
	public IPlaySheet processCustomQuery( String query, String playSheetString,
			boolean appending ) {
		if ( !"Update Query".equalsIgnoreCase( playSheetString ) ) {
			QuestionPlaySheetStore.getInstance().customIDcount++;

			IEngine engine = DIHelper.getInstance().getRdfEngine();
			String playSheetClassName = PlaySheetEnum.getClassFromName( playSheetString );
			String playSheetTitle = "Custom Query - " + QuestionPlaySheetStore.getInstance().getCustomCount();
			String insightID = QuestionPlaySheetStore.getInstance().getIDCount() + "custom";

			Insight insight = new Insight();
			UriBuilder urib = UriBuilder.getBuilder( VAS.NAMESPACE );
			insight.setId( urib.add( insightID ).build() );
			insight.setSparql( query );
			//Make sure that a playsheet class name is set for a custom query,
			//so that an appropriate icon can be displayed on the playsheet header:
			insight.setOutput( playSheetClassName );

			return prepareQueryOutputPlaySheet( engine, query, playSheetClassName,
					playSheetTitle, insight, appending );
		}
		else {
			return processUpdateQuery( query );
		}
	}

	/**
	 * Attempts to process a database-modification query (usually a Sparql INSERT
	 * or DELETE query). First, displays a warning to the user that the attempted
	 * action cannot be undone, and offers an option to cancel out.
	 *
	 * @param query -- (String) Sparql query as entered into the Custom Sparql
	 * Query window.
	 */
	private IPlaySheet processUpdateQuery( String query ) {
		//create UpdateProcessor class.  Set the query.  Let it run.
		UpdateProcessor processor = new UpdateProcessor();
        
        //Add a line of namespace prefixes to the top of the query for processing:
        query = AbstractSesameEngine.processNamespaces(query, new HashMap<>() );

		processor.setQuery( query );
		processor.processQuery();
		
		return playSheet;
	}

	public IPlaySheet processQuestionQuery( IEngine engine, Insight insight,
			Map<String, String> paramHash, boolean appending ) {
		//get engine
		//prepare insight and fill in paramHash into query
		//create title...need to parse out the params to add into the question title then add title...what a pain
		//create ID
		//add count to playsheetstore
		//paramFill for query
		//feed to prepare playsheet
		//Insight insight = engine.getInsight( insightId );
		StringBuilder playSheetTitle = new StringBuilder();
		if ( !( null == paramHash || paramHash.isEmpty() ) ) {
			for ( String value : paramHash.values() ) {
				URI uri = new URIImpl( value );
				playSheetTitle.append( Utility.getInstanceLabel( uri, engine ) ).append( " - " );
			}
		}
		logger.debug( "Param Hash is " + paramHash );
		playSheetTitle.append( insight.getLabel() );
		QuestionPlaySheetStore.getInstance().idCount++;
		playSheetTitle.append( " (" ).
				append( QuestionPlaySheetStore.getInstance().getIDCount() ).append( ")" );
		//When preparing Sparql to execute, we must remove all new-line characters:
		String sparql = getSparql( insight, paramHash ).replace( '\n', ' ' );
		return prepareQueryOutputPlaySheet( engine, sparql, insight.getOutput(),
				playSheetTitle.toString(), insight, appending );
	}

	/**
	 * Returns the Insight's Sparql with the selected parameters applied to
	 * various internal variables. If the Insight is legacy, then the
	 * ideosyncratic strings (e.g.: "<@...@>") must first be removed.
	 *
	 * @param insight -- (Insight) The current Insight value object.
	 *
	 * @param paramHash -- (Map<String, String>) The selected parameters to build
	 * into the Insight query.
	 *
	 * @return getSparql -- (String) Described above.
	 */
	public static String getSparql( Insight insight, Map<String, String> paramHash ) {
		String sparql = Utility.normalizeParam( insight.getSparql() );
		logger.debug( "SPARQL " + sparql );
		if ( insight.getIsLegacy() == true ) {
			sparql = Utility.fillParam( sparql, paramHash );
		}
		else {
			sparql = NonLegacyQueryBuilder.buildNonLegacyQuery( insight.getSparql(), paramHash );
		}
		return sparql;
	}

	public static String getTitle( Insight insight, Map<String, String> paramHash,
			IEngine engine ) {
		StringBuilder playSheetTitle = new StringBuilder();
		if ( !( null == paramHash || paramHash.isEmpty() ) ) {
			for ( String value : paramHash.values() ) {
				URI uri = new URIImpl( value );
				playSheetTitle.append( Utility.getInstanceLabel( uri, engine ) ).append( " - " );
			}
		}
		playSheetTitle.append( insight.getLabel() );
		QuestionPlaySheetStore.getInstance().idCount++;
		playSheetTitle.append( " (" ).
				append( QuestionPlaySheetStore.getInstance().getIDCount() ).append( ")" );
		return playSheetTitle.toString();
	}

	public IPlaySheet prepareQueryOutputPlaySheet( IEngine engine, String sparql,
			String playSheetClassName, String playSheetTitle, Insight insight,
			boolean appending ) {
		logger.debug( "SPARQL is " + sparql );
		//if append, dont need to set all the other playsheet stuff besides query
		if ( append ) {
			logger.debug( "Appending " );
			boolean oldstyle = true;

			if ( playSheet == null ) {
				JDesktopPane desktop = DIHelper.getInstance().getDesktop();
				JInternalFrame oldframe = desktop.getSelectedFrame();
				if ( oldframe instanceof IPlaySheet ) {
					playSheet = IPlaySheet.class.cast( desktop.getSelectedFrame() );
				}
				else if ( oldframe instanceof PlaySheetFrame ) {
					oldstyle = false;
				}
			}

			if ( oldstyle ) {
				//Set insight even for appends, because the tab's name 
				//on Grid playsheets is deriven from it:
				playSheet.setInsight( insight );
				playSheet.setQuery( sparql );
			}
		}
		else {
			try {
				playSheet = (IPlaySheet) Class.forName( playSheetClassName ).getConstructor().newInstance();
			}
			catch ( ClassNotFoundException | NoSuchMethodException | SecurityException |
					InstantiationException | IllegalAccessException | IllegalArgumentException |
					InvocationTargetException ex ) {
				logger.error( "Unknown PlaySheet...trying to find failsafe for: "
						+ playSheetClassName, ex );

				//If a sophisticated playsheet fails to load, try a simpler playsheet (Grid or Graph):
				if ( sparql.toUpperCase().startsWith( "SELECT" ) ) {
					playSheet = new GridPlaySheet();
				}
				else if ( sparql.toUpperCase().startsWith( "CONSTRUCT" ) ) {
					playSheet = new GraphPlaySheet();
				}
				else {
					logger.fatal( "No failsafe playsheet for sparql: " + sparql );
				}
			}

			QuestionPlaySheetStore.getInstance().put( insight.getIdStr(), playSheet );

			playSheet.setQuery( sparql );
			playSheet.setEngine( engine );
			playSheet.setInsight( insight );
			//playSheet.setQuestionID( insightID );
			playSheet.setTitle( playSheetTitle );
			playSheet.setPerspective( perspective );
		}

		return playSheet;
	}
}
