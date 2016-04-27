/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import java.util.Collection;

/**
 *
 * @author ryan
 */
public abstract class EngineOperationAdapter implements EngineOperationListener {

	@Override
	public void engineClosed( IEngine eng ) {
	}

	@Override
	public void engineOpened( IEngine eng ) {
	}

	@Override
	public void insightsModified( IEngine eng, Collection<Perspective> perspectives ) {
	}

	@Override
	public void handleError( IEngine eng, EngineManagementException eme ) {
	}
}
