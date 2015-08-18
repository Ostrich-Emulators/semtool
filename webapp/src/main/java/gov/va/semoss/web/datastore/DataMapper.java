/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.datastore;

import java.util.Collection;

/**
 * @param <T> the type of mapper
 * @param <I> the type of T's id field
 * @author ryan
 */
public interface DataMapper<T, I> {

	public Collection<T> getAll();

	public T getOne( I id );

	public T create( T t );

	public void remove( T t );

	public void update( T data );
}
