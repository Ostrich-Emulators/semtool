/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ryan
 * @param <T>
 * @param <V>
 */
public class MultiMap<T, V> extends HashMap<T, List<V>> {

	@Override
	public List<V> put( T key, List<V> value ) {
		return super.put( key, value );
	}

	public List<V> add( T key, V value ) {
		List<V> list;
		list = ( containsKey( key ) ? get( key ) : new ArrayList<>() );
		list.add( value );
		return put( key, list );
	}

	/**
	 * Gets a guaranteed not-null list of Vs. This is different from 
	 * {@link #get(java.lang.Object) } in that it will never return
	 * <code>null</code>. If the key *is* present, the list is a reference to the
	 * list of values, so changes to it will be reflected in this map. If the key
	 * is *not* present, the changes to the returned list will not be reflected in
	 * the map.
	 *
	 * @param key the key to get
	 * @return a list
	 */
	public List<V> getNN( T key ) {
		List<V> v = get( key );
		return ( null == v ? new ArrayList<>() : v );
	}

	/**
	 * Flips the map such that keys become values, and values, keys. If there are
	 * multiple keys with the same value, the values are added to the MultiMap's
	 * list
	 *
	 * @param <T>
	 * @param <V>
	 * @param map
	 * @return
	 */
	public static <T, V> MultiMap<T, V> flip( Map<V, T> map ) {
		MultiMap<T, V> multi = new MultiMap<>();
		for ( Map.Entry<V, T> en : map.entrySet() ) {
			multi.add( en.getValue(), en.getKey() );
		}

		return multi;
	}

	/**
	 * Flips the given mapping. This will result in loss of information if the
	 * original map's values are not unique.
	 *
	 * @param <T>
	 * @param <V>
	 * @param map
	 * @return
	 */
	public static <T, V> Map<T, V> lossyflip( Map<V, T> map ) {
		Map<T, V> multi = new HashMap<>();

		for ( Map.Entry<V, T> en : map.entrySet() ) {
			multi.put( en.getValue(), en.getKey() );
		}

		return multi;
	}
}
