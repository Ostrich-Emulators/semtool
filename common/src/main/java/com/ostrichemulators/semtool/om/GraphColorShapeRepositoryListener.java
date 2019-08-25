/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
import java.net.URL;
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public interface GraphColorShapeRepositoryListener {
	public void dataChanged( IRI uri, NamedShape ns, Color c, URL img );
}
