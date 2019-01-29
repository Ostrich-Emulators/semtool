/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import org.openrdf.model.IRI;

/**
 *
 * @author ryan
 */
public interface SEMOSSEdge extends GraphElement {

	public void setSpecificType( IRI st );

	public IRI getSpecificType();
}
