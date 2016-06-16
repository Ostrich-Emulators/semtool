/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.impl.InsightManagerImpl;
import com.ostrichemulators.semtool.rdf.engine.impl.SesameEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author ryan
 */
public class AttachRemoteInsightsAction extends RemoteEndpointAction {

	private static final Logger log = Logger.getLogger( AttachRemoteInsightsAction.class );

	public AttachRemoteInsightsAction( String optg, Frame frame ) {
		super( optg, frame, "Attach External Insights", "insight_replace1" );
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		String title = "Opening Remote Database";
		ProgressTask pt = new ProgressTask( title, new Runnable() {

			@Override
			public void run() {
				Properties props = getProperties();
				HTTPRepository repo = null;

				try {
					// the props always sets the repository key and *not* the insights key
					String insights = props.getProperty( SesameEngine.REPOSITORY_KEY );
					//String username = props.getProperty( "username", "" );
					//String password = props.getProperty( "password", "" );

					repo = new HTTPRepository( insights );
					repo.initialize();
					InsightManager imi = InsightManagerImpl.createFromRepository( repo );
					getEngine().setInsightManager( imi );
					EngineUtil.getInstance().notifyInsightsModified( getEngine(), imi );
				}
				catch ( RepositoryException e ) {
					log.error( e, e );
					GuiUtility.showError( e.getLocalizedMessage() );
				}
				finally {
					if ( null != repo ) {
						try {
							repo.shutDown();
						}
						catch ( RepositoryException re ) {
							log.warn( re, re );
						}
					}
				}
			}
		} );

		return pt;
	}

}
