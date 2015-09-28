/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.Collection;

/**
 *
 * @author ryan
 */
public class OneShotEngineAdapter extends EngineOperationAdapter {

	public static enum ShotOp {

		OPENED, CLOSED, INSIGHTS
	};

	private final IEngine engine;
	private final ShotOp shotop;

	/**
	 * Creates a new EngineAdapter that automatically closes itself when the given
	 * ShotOp is called with the given IEngine
	 *
	 * @param eng
	 * @param op
	 */
	public OneShotEngineAdapter( IEngine eng, ShotOp op ) {
		engine = eng;
		shotop = op;
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

	public void removeIfOp( IEngine eng, ShotOp op ) {
		if ( isMyEngine( eng ) && op == shotop ) {
			EngineUtil.getInstance().removeEngineOpListener( this );
		}
	}

	public boolean isMyEngine( IEngine eng ) {
		return engine.equals( eng );
	}
}
