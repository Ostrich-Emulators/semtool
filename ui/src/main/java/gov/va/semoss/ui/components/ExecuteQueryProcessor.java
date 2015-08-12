package gov.va.semoss.ui.components;

import java.util.Map;

import org.apache.log4j.Logger;

import gov.va.semoss.om.Insight;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.AbstractSesameEngine;
import gov.va.semoss.rdf.query.util.UpdateExecutorAdapter;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.helpers.NonLegacyQueryBuilder;
import gov.va.semoss.util.DIHelper;

import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

public abstract class ExecuteQueryProcessor extends AbstractAction {

	private static final Logger logger = Logger.getLogger( ExecuteQueryProcessor.class );

	public ExecuteQueryProcessor() {
	}

	public ExecuteQueryProcessor( String name ) {
		super( name );
	}

	public ExecuteQueryProcessor( String name, Icon icon ) {
		super( name, icon );
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
		if ( insight.isLegacy() == true ) {
			sparql = Utility.fillParam( sparql, paramHash );
		}
		else {
			sparql = NonLegacyQueryBuilder.buildNonLegacyQuery( insight.getSparql(), paramHash );
		}
		return sparql;
	}

	/**
	 * Displays a warning dialog to the user, indicating that the attempted
	 * database-update query cannot be undone by a simple keystroke, and offers an
	 * option to cancel out.
	 *
	 * @return showWarning -- (int) Corresponds to the "JOptionPane.YES_OPTION" or
	 * the "JOptionPane.NO_OPTION".
	 */
	private boolean okToUpdate() {
		Object[] buttons = { "Continue", "Cancel" };
		int response = JOptionPane.showOptionDialog( null,
				"The update query you are about to run \ncannot be undone.  Would you like to continue?",
				"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[1] );
		return ( JOptionPane.YES_OPTION == response );
	}

	protected abstract String getTitle();

	protected String getFrameTitle() {
		return getTitle();
	}

	protected abstract String getQuery();

	protected abstract Class<? extends PlaySheetCentralComponent> getPlaySheetCentralComponent()
			throws ClassNotFoundException;

	protected abstract IEngine getEngine();

	protected abstract boolean isAppending();

	/**
	 * Gives subclasses a chance to prepare for running our query
	 *
	 * @param ae
	 */
	protected void prepare( ActionEvent ae ) {
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		String query = getQuery();
        
		Class<? extends PlaySheetCentralComponent> klass = null;
		try {
			klass = getPlaySheetCentralComponent();
		}
		catch ( ClassNotFoundException ex ) {
			logger.error( ex, ex );
			GuiUtility.showError( ex.getLocalizedMessage() );
			return;
		}

		IEngine engine = getEngine();

		ProgressTask pt = null;
		if ( null == klass ) {
			if ( okToUpdate() ) {
				pt = makeUpdateTask( query, engine );
			}
		}
		else {
			// run a regular query
			JDesktopPane pane = DIHelper.getInstance().getDesktop();
			DIHelper.getInstance().getPlayPane().showDesktop();
			String title = getTitle();
			boolean appending = isAppending();

			try {
				PlaySheetCentralComponent pscc = klass.newInstance();
				pscc.setTitle( title );
				pt = doitNewSkool( pscc, query, engine, pane, getFrameTitle(),
						appending );
			}
			catch ( InstantiationException | IllegalAccessException e ) {
				logger.error( e, e );
				GuiUtility.showError( e.getLocalizedMessage() );
			}
		}

		if ( null != pt ) {
			OperationsProgress op = OperationsProgress.getInstance( PlayPane.UIPROGRESS );
			op.add( pt );
		}
	}

	private static ProgressTask doitNewSkool( PlaySheetCentralComponent pscc,
			String query, IEngine eng, JDesktopPane pane, String frameTitle, 
			boolean appending ) {
		if ( appending ) {
			PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );
			return psf.getOverlayTask( query, eng, pscc.getTitle() );
		}
		else {
			PlaySheetFrame psf = new PlaySheetFrame( eng );
			psf.addTab( pscc );
			psf.setTitle( frameTitle );
			DIHelper.getInstance().getDesktop().add( psf );

			return psf.getCreateTask( query );
		}
	}

	private static ProgressTask makeUpdateTask( String query, IEngine engine ) {
		final Exception ok[] = { null };
		// this is an update query
		return new ProgressTask( "Executing Update", new Runnable() {

			@Override
			public void run() {
				try {
					engine.update( new UpdateExecutorAdapter( query ) );
				}
				catch ( RepositoryException | MalformedQueryException | UpdateExecutionException ex ) {
					logger.error( ex, ex );
					ok[0] = ex;
				}
			}
		} ) {

			@Override
			public void done() {
				super.done();
				if ( null != ok[0] ) {
					GuiUtility.showError( ok[0].getLocalizedMessage() );
				}
			}
		};
	}
}
