/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author ryan
 */
public class TreeNode<T> {

	private T node;
	private final List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode( T n ) {
		node = n;
	}

	public TreeNode() {
		this( null );
	}

	public List<TreeNode<T>> getChildNodes() {
		return new ArrayList<>( children );
	}

	public List<T> getChildren() {
		List<T> childs = new ArrayList<>();
		for ( TreeNode<T> t : children ) {
			childs.add( t.node );
		}

		return childs;
	}

	public void setChildren( Collection<T> c ) {
		children.clear();
		for ( T t : c ) {
			children.add( new TreeNode<>( t ) );
		}
	}

	public TreeNode<T> addChild( T e ) {
		TreeNode<T> n = new TreeNode( e );
		children.add( n );
		return n;
	}

	public TreeNode<T> removeChild( T e ) {
		int idx = -1;
		for ( TreeNode<T> child : children ) {
			idx++;

			if ( null == child.node ) {
				if ( null == e ) {
					break;
				}
			}
			if ( child.node.equals( e ) ) {
				break;
			}
		}

		return ( idx < 0 ? null : children.remove( idx ) );
	}

	public T getNode() {
		return node;
	}

	public void setNode( T t ) {
		node = t;
	}
}
