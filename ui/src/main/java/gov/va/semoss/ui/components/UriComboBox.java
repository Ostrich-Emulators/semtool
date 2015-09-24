/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.ui.components.renderers.LabeledPairRenderer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComboBox.KeySelectionManager;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import gov.va.semoss.util.Constants;

/**
 *
 * @author ryan
 */
public class UriComboBox extends JComboBox<URI> {

  public static final URI MISSING_CONCEPT = new URIImpl( "error://missing_concept" );
  public static final URI BAD_FILL = new URIImpl( "error://invalid_fill" );
  public static final URI FETCHING = new URIImpl( "error://doing_fetch" );
  protected UriComboBox.UriModel model = new UriComboBox.UriModel();
  protected LabeledPairRenderer urirenderer = LabeledPairRenderer.getUriPairRenderer();

  public UriComboBox( URI[] array ) {
    this( Arrays.asList( array ) );
  }

  public UriComboBox( Collection<URI> array ) {
    this();
    model.reset( array );
  }

  public UriComboBox() {
    setModel( model );
    setRenderer( urirenderer );
    setKeySelectionManager(new UriComboBoxKeySelectionManager());
  }

  public Map<URI, String> getLabelMap() {
    return urirenderer.getCachedLabels();
  }

  public void clear() {
    model.removeAllElements();
  }

  public void setData( Collection<URI> uris ) {
    model.reset( uris );
  }

  public void setData( Map<URI, String> uris ) {
    List<UriLabelPair> list = new ArrayList<>();
    for ( Map.Entry<URI, String> en : uris.entrySet() ) {
      list.add( new UriLabelPair( en.getKey(), en.getValue() ) );
    }
    Collections.sort( list );

    List<URI> urilist = new ArrayList<>();
    for ( UriLabelPair p : list ) {
      urilist.add( p.getUri() );
    }

    model.reset( urilist );
    urirenderer.cache( uris );

    urirenderer.cache( MISSING_CONCEPT, "Concept Doesn't Exist in DB" );
    urirenderer.cache( BAD_FILL, "Invalid Fill Param" );
    urirenderer.cache( FETCHING, "Fetching" );
    urirenderer.cache( Constants.ANYNODE, "None" );
  }

  public void addData( URI uri, String label ) {
    model.addElement( uri );
    urirenderer.cache( uri, label );
  }

  public URI getSelectedUri() {
    int idx = getSelectedIndex();
    return ( idx >= 0 ? getItemAt( idx ) : null );
  }

  public UriModel getUriModel() {
    return model;
  }

  public static class UriModel extends DefaultComboBoxModel<URI> {

    private final List<URI> uris = new ArrayList<>();
    private URI selected;

    public UriModel() {
    }

    public UriModel( URI[] items ) {
      this( Arrays.asList( items ) );
    }

    public UriModel( Collection<URI> col ) {
      uris.addAll( col );
    }

    @Override
    public void removeAllElements() {
      uris.clear();
    }

    @Override
    public void removeElementAt( int index ) {
      uris.remove( index );
    }

    @Override
    public void insertElementAt( URI anObject, int index ) {
      uris.add( index, anObject );
    }

    @Override
    public void addElement( URI anObject ) {
      uris.add( anObject );
    }

    @Override
    public int getIndexOf( Object anObject ) {
      return uris.indexOf( anObject );
    }

    @Override
    public URI getElementAt( int index ) {
      return uris.get( index );
    }

    @Override
    public int getSize() {
      return uris.size();
    }

    @Override
    public Object getSelectedItem() {
      return selected;
    }

    @Override
    public void setSelectedItem( Object anObject ) {
      selected = URI.class.cast( anObject );
    }

    public void reset( Collection<URI> urilist ) {
      uris.clear();
      uris.addAll( urilist );
      if ( !urilist.isEmpty() ) {
        setSelectedItem( uris.get( 0 ) );
      }

      fireContentsChanged( this, 0, urilist.size() );
    }

    public Collection<URI> elements() {
      return new ArrayList<>( uris );
    }
  }

  public static class UriLabelPair implements Comparable<UriLabelPair> {

    private final URI uri;
    private final String label;

    public UriLabelPair( URI uri, String label ) {
      this.uri = uri;
      this.label = label;
    }

    @Override
    public int compareTo( UriLabelPair t ) {
      return getLabel().compareTo( t.getLabel() );
    }

    public String getLabel() {
      return ( null == label ? uri.getLocalName() : label );
    }

    public URI getUri() {
      return uri;
    }
  }

  class UriComboBoxKeySelectionManager implements KeySelectionManager, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int selectionForKey(char aKey,ComboBoxModel aModel) {
          int i,c;
          int currentSelection = -1;
          Object selectedItem = aModel.getSelectedItem();
          String v;
          String pattern;

          if ( selectedItem != null ) {
              for ( i=0,c=aModel.getSize();i<c;i++ ) {
                  if ( selectedItem == aModel.getElementAt(i) ) {
                      currentSelection  =  i;
                      break;
                  }
              }
          }

          pattern = ("" + aKey).toLowerCase();
          aKey = pattern.charAt(0);

          for ( i = ++currentSelection, c = aModel.getSize() ; i < c ; i++ ) {
              Object elem = aModel.getElementAt(i);
              
              if (!(elem instanceof URI)) {
            	  return -1;
              }
              URI uri = URI.class.cast(elem);
              
              if (uri != null && uri.getLocalName() != null) {
                  v = uri.getLocalName().toLowerCase();
                  if ( v.length() > 0 && v.charAt(0) == aKey )
                      return i;
              }
          }

          for ( i = 0 ; i < currentSelection ; i ++ ) {
              Object elem = aModel.getElementAt(i);
              
              if (!(elem instanceof URI)) {
            	  return -1;
              }
              URI uri = URI.class.cast(elem);
              
              if (uri != null && uri.getLocalName() != null) {
                  v = uri.getLocalName().toLowerCase();
                  if ( v.length() > 0 && v.charAt(0) == aKey )
                      return i;
              }
          }
          return -1;
      }
  }
}
