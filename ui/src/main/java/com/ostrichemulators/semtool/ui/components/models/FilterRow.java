/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.util.Constants;
import org.openrdf.model.URI;

/**
 * A class for populating the filter, color table models
 *
 * @author ryan
 */
public class FilterRow<T extends GraphElement> implements Comparable<FilterRow> {
	public final URI type;
	public final T instance;

	public FilterRow( URI type, T nodeedge ) {
		this.type = type;
		this.instance = nodeedge;
	}

	public boolean isHeader() {
		return ( null == instance || Constants.ANYNODE.equals( instance ) );
	}

	@Override
	public int compareTo( FilterRow o ) {
		// if we have the same type, sort on the instance label
		// if we don't have the same type, sort by type

		if ( type.equals( o.type ) ) {
			if ( isHeader() ) {
				return -1;
			}
			if ( o.isHeader() ) {
				return 1;
			}
			return instance.getLabel().compareTo( o.instance.getLabel() );
		}

		// types aren't the same, so just worry about sorting on type
		return type.stringValue().compareTo( o.type.stringValue() );
	}
}
