/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ryan
 * @param <T>
 * @param <V>
 */
public class MultiSetMap<T, V> extends HashMap<T, Set<V>> {

	public static <K, X> MultiSetMap<K, X> deepCopy( Map<K, Set<X>> model ) {
		MultiSetMap<K, X> newmap = new MultiSetMap<>();
		for ( Map.Entry<K, Set<X>> en : model.entrySet() ) {
			Set<X> newv = new LinkedHashSet<>();
			for ( X v : en.getValue() ) {
				newv.add( v );
			}

			newmap.put( en.getKey(), newv );
		}
		return newmap;
	}

	@Override
	public Set<V> put( T key, Set<V> value ) {
		return super.put( key, value );
	}

	public Set<V> add( T key, V value ) {
		Set<V> list;
		list = ( containsKey( key ) ? get( key ) : new LinkedHashSet<>() );
		list.add( value );
		return put( key, list );
	}

	public Set<V> addAll( T key, Collection<V> vals ) {
		if ( containsKey( key ) ) {
			get( key ).addAll( vals );
		}
		else {
			put( key, new LinkedHashSet<>( vals ) );
		}

		return get( key );
	}

	/**
	 * Gets a guaranteed not-null (but possibly empty) set of Vs. This set has a
	 * consistent iteration order. This is different from 
	 * {@link #get(java.lang.Object) } in that it will never return
	 * <code>null</code>. If the key *is* present, the list is a reference to the
	 * set of values, so changes to it will be reflected in this map. If the key
	 * is *not* present, the changes to the returned set will not be reflected in
	 * the map.
	 *
	 * @param key the key to get
	 * @return a list
	 */
	public Set<V> getNN( T key ) {
		Set<V> v = get( key );
		return ( null == v ? new LinkedHashSet<>() : v );
	}

	public static <T, V> MultiSetMap<T, V> flip( Map<V, T> map ) {
		MultiSetMap<T, V> multi = new MultiSetMap<>();
		for ( Map.Entry<V, T> en : map.entrySet() ) {
			multi.add( en.getValue(), en.getKey() );
		}

		return multi;
	}
}
