/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import java.util.Collection;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public class UriSelectorComboBox extends JComboBox<IRI> {

  public UriSelectorComboBox( ComboBoxModel<IRI> aModel ) {
    super( aModel );
  }

  public UriSelectorComboBox( IRI[] items ) {
    super( items );
  }

  public UriSelectorComboBox( Collection<IRI> items ) {
    this( items.toArray( new IRI[]{} ) );
  }

  public UriSelectorComboBox() {
  }

  public void reset( Collection<IRI> newitems ) {
    removeAllItems();
    for ( IRI u : newitems ) {
      this.addItem( u );
    }
  }
}
