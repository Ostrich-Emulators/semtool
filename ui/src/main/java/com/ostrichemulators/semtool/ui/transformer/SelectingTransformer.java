/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.transformer;

import static com.ostrichemulators.semtool.ui.transformer.SelectingTransformer.SelectedState.NOTHING_SELECTED;
import static com.ostrichemulators.semtool.ui.transformer.SelectingTransformer.SelectedState.NOT_SELECTED;
import static com.ostrichemulators.semtool.ui.transformer.SelectingTransformer.SelectedState.SELECTED;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections15.Transformer;

/**
 * A super class to worry about what's selected, not selected, or normal
 *
 * @author ryan
 */
public abstract class SelectingTransformer<T, V> implements Transformer<T, V> {

	public enum SelectedState {

		SELECTED, NOT_SELECTED, NOTHING_SELECTED
	};
	private final Set<T> selecteds = new HashSet<>();
	private boolean skeleton = false;

	public void clearSelected() {
		selecteds.clear();
	}

	/**
	 * In skeleton mode, {@code NOTHING_SELECTED} will never be returned by
	 * {@link #getState(java.lang.Object)}; things will either be selected or not
	 *
	 * @param b
	 */
	public void setSkeletonMode( boolean b ) {
		skeleton = b;
	}

	public boolean isSkeletonMode() {
		return skeleton;
	}

	public void setSelected( Collection<T> s ) {
		clearSelected();
		select( s );
	}

	public void select( T s ) {
		selecteds.add( s );
	}

	public void select( Collection<T> s ) {
		if ( null != s ) {
			selecteds.addAll( s );
		}
	}

	public void deselect( T s ) {
		selecteds.remove( s );
	}

	public void deselect( Collection<T> s ) {
		if ( null != s ) {
			selecteds.removeAll( s );
		}
	}

	public Set<T> getSelected() {
		return selecteds;
	}

	public boolean isSelected( T t ) {
		return selecteds.contains( t );
	}

	public SelectedState getState( T t ) {
		if ( selecteds.isEmpty() ) {
			return ( skeleton ? NOT_SELECTED : NOTHING_SELECTED );
		}
		return ( selecteds.contains( t ) ? SELECTED : NOT_SELECTED );
	}

	@Override
	public V transform( T i ) {
		switch ( getState( i ) ) {
			case NOT_SELECTED:
				return transformNotSelected( i, skeleton );
			case SELECTED:
				return transformSelected( i );
			default:
				return transformNormal( i );
		}
	}

	/**
	 * Transforms the object with nothing special added
	 *
	 * @param t
	 * @return
	 */
	protected abstract V transformNormal( T t );

	/**
	 * Transforms the object to reflect the fact that it has been selected
	 *
	 * @param t
	 * @return
	 */
	protected abstract V transformSelected( T t );

	/**
	 * Transforms the object to reflect the fact that it has not been selected.
	 *
	 * @param t the object to transform
	 * @param inSkeletonMode are we in skeleton mode?
	 * @return
	 */
	protected abstract V transformNotSelected( T t, boolean inSkeletonMode );

}
