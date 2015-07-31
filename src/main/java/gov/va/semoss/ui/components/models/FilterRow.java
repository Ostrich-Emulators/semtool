/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.models;

import gov.va.semoss.om.NodeEdgeBase;
import org.openrdf.model.URI;

/**
 * A class for populating the filter, color table models
 *
 * @author ryan
 */
public class FilterRow<T extends NodeEdgeBase> {

	public final URI type;
	public final T instance;

	public FilterRow( URI type, T nodeedge ) {
		this.type = type;
		this.instance = nodeedge;
	}

	public boolean isHeader() {
		return ( null == instance );
	}
}
