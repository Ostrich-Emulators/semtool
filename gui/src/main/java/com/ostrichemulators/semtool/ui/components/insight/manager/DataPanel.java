/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.insight.manager;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author ryan
 */
public abstract class DataPanel<T> extends JPanel implements DocumentListener {

	public static final String CHANGE_PROPERTY = "change";
	private IEngine engine;
	private T element;
	private boolean hasChanges = false;
	private DefaultMutableTreeNode node;

	public void setEngine( IEngine engine ) {
		this.engine = engine;
		clear();
		hasChanges = false;
	}

	protected IEngine getEngine() {
		return engine;
	}

	public final void setElement( T ele, DefaultMutableTreeNode nod ) {
		element = ele;
		node = nod;
		isetElement( ele, nod );
		setChanges( false );
	}

	protected T getElement() {
		return element;
	}

	/**
	 * Clears out the data elements
	 */
	protected abstract void clear();

	protected abstract void isetElement( T ele, DefaultMutableTreeNode node );

	public T applyChanges() {
		if ( hasChanges() ) {
			updateElement( element );
		}
		hasChanges = false;
		return element;
	}

	protected abstract void updateElement( T ele );

	protected void setChanges( boolean c ) {
		boolean hadChanges = hasChanges;
		hasChanges = c;

		firePropertyChange( CHANGE_PROPERTY, hadChanges, hasChanges );
	}

	public boolean hasChanges() {
		return hasChanges;
	}

	private void docupdated() {
		boolean hadChanges = hasChanges;
		hasChanges = true;
		firePropertyChange( CHANGE_PROPERTY, hadChanges, hasChanges );
	}

	@Override
	public void insertUpdate( DocumentEvent e ) {
		docupdated();
	}

	@Override
	public void removeUpdate( DocumentEvent e ) {
		docupdated();
	}

	@Override
	public void changedUpdate( DocumentEvent e ) {
		docupdated();
	}

	protected void listenTo( JTextComponent a ) {
		a.getDocument().addDocumentListener( this );
	}

	public void setNode( DefaultMutableTreeNode node ) {
		this.node = node;
	}

	protected DefaultMutableTreeNode getNode() {
		return node;
	}
}
