/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import java.util.Collection;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class UriSelectorComboBox extends JComboBox<URI> {

  public UriSelectorComboBox( ComboBoxModel<URI> aModel ) {
    super( aModel );
  }

  public UriSelectorComboBox( URI[] items ) {
    super( items );
  }

  public UriSelectorComboBox( Collection<URI> items ) {
    this( items.toArray( new URI[]{} ) );
  }

  public UriSelectorComboBox() {
  }

  public void reset( Collection<URI> newitems ) {
    removeAllItems();
    for ( URI u : newitems ) {
      this.addItem( u );
    }
  }
}
