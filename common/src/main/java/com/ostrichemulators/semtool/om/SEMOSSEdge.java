/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

/**
 *
 * @author ryan
 */
public interface SEMOSSEdge extends GraphElement {

	public void setVerticesVisible( boolean visible );

	/**
	 * Get whether all of the vertices for this edge are visible
	 *
	 * @return True if all vertices are visible, false otherwise
	 */
	public boolean getVerticesVisible();	

}
