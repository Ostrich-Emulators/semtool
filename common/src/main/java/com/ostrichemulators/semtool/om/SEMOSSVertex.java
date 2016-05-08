/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.util.Map;

/**
 *
 * @author ryan
 */
public interface SEMOSSVertex extends GraphElement {

	/**
	 * This is needed for the browser playsheets
	 *
	 * @param _propHash
	 */
	public void setPropHash( Map<String, Object> _propHash );
}
