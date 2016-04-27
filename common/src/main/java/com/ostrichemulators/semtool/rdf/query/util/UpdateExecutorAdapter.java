/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util;

import com.ostrichemulators.semtool.rdf.engine.api.UpdateExecutor;

/**
 * A class that handles all the housekeeping of the QueryExecutor interface
 *
 * @author ryan
 *
 */
public class UpdateExecutorAdapter extends AbstractBindable
		implements UpdateExecutor {

	public UpdateExecutorAdapter() {
	}

	public UpdateExecutorAdapter( String sparq ) {
		super( sparq );
	}
}
