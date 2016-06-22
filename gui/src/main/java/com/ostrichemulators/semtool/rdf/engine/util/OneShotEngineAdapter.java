/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.io.File;
import java.util.Collection;

/**
 *
 * @author ryan
 */
public class OneShotEngineAdapter extends EngineOperationAdapter {

	public static enum ShotOp {

		OPENED, CLOSED, INSIGHTS
	};

	private final ShotOp shotop;
	private final String smssloc;

	/**
	 * Creates a new EngineAdapter that automatically closes itself when the given
	 * ShotOp is called with the given IEngine
	 *
	 * @param smss the database locator
	 * @param op
	 */
	public OneShotEngineAdapter( String smss, ShotOp op ) {
		smssloc = smss;
		shotop = op;
	}

	public OneShotEngineAdapter( IEngine eng, ShotOp op ) {
		this( eng.getProperty( Constants.SMSS_LOCATION ), op );
	}

	public OneShotEngineAdapter( File smss, ShotOp op ) {
		this( smss.getPath(), op );
	}

	@Override
	public final void engineClosed( IEngine eng ) {
		doEngineClosed( eng );
		removeIfOp( eng, ShotOp.CLOSED );
	}

	@Override
	public final void engineOpened( IEngine eng ) {
		doEngineOpened( eng );
		removeIfOp( eng, ShotOp.OPENED );
	}

	@Override
	public final void insightsModified( IEngine eng, Collection<Perspective> perspectives ) {
		doInsightsModified( eng, perspectives );
		removeIfOp( eng, ShotOp.INSIGHTS );
	}

	public void doEngineClosed( IEngine eng ) {
	}

	public void doEngineOpened( IEngine eng ) {
	}

	public void doInsightsModified( IEngine eng, Collection<Perspective> perspectives ) {
	}

	private void removeIfOp( IEngine eng, ShotOp op ) {
		if ( isMyEngine( eng ) && op == shotop ) {
			EngineUtil.getInstance().removeEngineOpListener( this );
		}
	}

	protected boolean isMyEngine( IEngine eng ) {
		return smssloc.equals( eng.getProperty( Constants.SMSS_LOCATION ) );
	}

	@Override
	public void handleError( IEngine eng, EngineManagementException eme ) {
		GuiUtility.showError( eme.getLocalizedMessage() );
	}

	@Override
	public void handleLoadingError( String smss, EngineManagementException eme ) {
		GuiUtility.showError( eme.getLocalizedMessage() );
	}
}
