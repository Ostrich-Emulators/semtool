/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import java.util.Date;

/**
 *
 * @author ryan
 */
public class ProgressTask {

	private String label;
	private Runnable op;
	private Date starttime;
	private Date stoptime;

	public ProgressTask( String label, Runnable op ) {
		this.label = label;
		this.op = op;
	}

	public ProgressTask( String label ) {
		this.label = label;
	}

	public ProgressTask( Runnable op ) {
		this( "unlabeled task", op );
	}

	public void setOp( Runnable op ){
		this.op = op;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel( String l ) {
		label = l;
	}

	public Runnable getOp() {
		return op;
	}

	/**
	 * Runs this task's operation and records the start time
	 */
	public void runOp() {
		setStartTime( new Date() );
		op.run();
	}

	/**
	 * Gets called on the UI thread once {@link #getOp() } has completed. The
	 * default is to track the stop time
	 */
	public void done() {
		setStopTime( new Date() );
	}

	protected void setStartTime( Date d ) {
		starttime = d;
	}

	protected void setStopTime( Date d ) {
		stoptime = d;
	}

	public Date getStartTime() {
		return new Date( starttime.getTime() );
	}

	public Date getStopTime() {
		return new Date( stoptime.getTime() );
	}

}
