/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

}
