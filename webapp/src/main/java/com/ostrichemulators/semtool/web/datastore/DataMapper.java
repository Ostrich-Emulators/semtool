/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.datastore;

import java.util.Collection;

/**
 * @param <T> the type of mapper
 * @param <I> the type of T's id field
 * @author ryan
 */
public interface DataMapper<T, I> {

	public DataStore getDataStore();

	public void setDataStore( DataStore store );

	public Collection<T> getAll();

	public T getOne( I id );

	public T create( T t ) throws Exception;

	public void remove( T t ) throws Exception;

	public void update( T data ) throws Exception;
}
