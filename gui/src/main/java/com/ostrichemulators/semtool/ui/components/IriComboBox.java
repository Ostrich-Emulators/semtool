/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;
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

import com.ostrichemulators.semtool.util.Constants;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 *
 * @author ryan
 */
public class IriComboBox extends JComboBox<IRI> {

  private static final ValueFactory vf = SimpleValueFactory.getInstance();
  public static final IRI MISSING_CONCEPT = vf.createIRI( "error://missing_concept" );
  public static final IRI BAD_FILL = vf.createIRI( "error://invalid_fill" );
  public static final IRI FETCHING = vf.createIRI( "error://doing_fetch" );
  protected IriComboBox.UriModel model = new IriComboBox.UriModel();
  protected LabeledPairRenderer urirenderer = LabeledPairRenderer.getUriPairRenderer();

  public IriComboBox( IRI[] array ) {
    this( Arrays.asList( array ) );
  }

  public IriComboBox( Collection<IRI> array ) {
    this();
    model.reset( array );
  }

  public IriComboBox() {
    setModel( model );
    setRenderer( urirenderer );
    setKeySelectionManager( new IriComboBoxKeySelectionManager() );
  }

  public Map<IRI, String> getLabelMap() {
    return urirenderer.getCachedLabels();
  }

  public void clear() {
    model.removeAllElements();
  }

  public void setData( Collection<IRI> uris ) {
    model.reset( uris );
  }

  public void setData( Map<IRI, String> uris ) {
    List<IriLabelPair> list = new ArrayList<>();
    for ( Map.Entry<IRI, String> en : uris.entrySet() ) {
      list.add( new IriLabelPair( en.getKey(), en.getValue() ) );
    }
    Collections.sort( list );

    List<IRI> urilist = new ArrayList<>();
    for ( IriLabelPair p : list ) {
      urilist.add( p.getUri() );
    }

    model.reset( urilist );
    urirenderer.cache( uris );

    urirenderer.cache( MISSING_CONCEPT, "Concept Doesn't Exist in DB" );
    urirenderer.cache( BAD_FILL, "Invalid Fill Param" );
    urirenderer.cache( FETCHING, "Fetching" );
    urirenderer.cache( Constants.ANYNODE, "None" );
  }

  public void addData( IRI uri, String label ) {
    model.addElement( uri );
    urirenderer.cache( uri, label );
  }

  public IRI getSelectedIri() {
    int idx = getSelectedIndex();
    return ( idx >= 0 ? getItemAt( idx ) : null );
  }

  public UriModel getUriModel() {
    return model;
  }

  public static class UriModel extends DefaultComboBoxModel<IRI> {

    private final List<IRI> uris = new ArrayList<>();
    private IRI selected;

    public UriModel() {
    }

    public UriModel( IRI[] items ) {
      this( Arrays.asList( items ) );
    }

    public UriModel( Collection<IRI> col ) {
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
    public void insertElementAt( IRI anObject, int index ) {
      uris.add( index, anObject );
    }

    @Override
    public void addElement( IRI anObject ) {
      uris.add( anObject );
    }

    @Override
    public int getIndexOf( Object anObject ) {
      return uris.indexOf( anObject );
    }

    @Override
    public IRI getElementAt( int index ) {
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
      selected = IRI.class.cast( anObject );
    }

    public void reset( Collection<IRI> urilist ) {
      uris.clear();
      uris.addAll( urilist );
      if ( !urilist.isEmpty() ) {
        setSelectedItem( uris.get( 0 ) );
      }

      fireContentsChanged( this, 0, urilist.size() );
    }

    public Collection<IRI> elements() {
      return new ArrayList<>( uris );
    }
  }

  public static class IriLabelPair implements Comparable<IriLabelPair> {

    private final IRI uri;
    private final String label;

    public IriLabelPair( IRI uri, String label ) {
      this.uri = uri;
      this.label = label;
    }

    @Override
    public int compareTo( IriLabelPair t ) {
      return getLabel().compareTo( t.getLabel() );
    }

    public String getLabel() {
      return ( null == label ? uri.getLocalName() : label );
    }

    public IRI getUri() {
      return uri;
    }
  }

  class IriComboBoxKeySelectionManager implements KeySelectionManager, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int selectionForKey( char aKey, ComboBoxModel aModel ) {
      int i, c;
      int currentSelection = -1;
      Object selectedItem = aModel.getSelectedItem();
      String v;
      String pattern;

      if ( selectedItem != null ) {
        for ( i = 0, c = aModel.getSize(); i < c; i++ ) {
          if ( selectedItem == aModel.getElementAt( i ) ) {
            currentSelection = i;
            break;
          }
        }
      }

      pattern = ( "" + aKey ).toLowerCase();
      aKey = pattern.charAt( 0 );

      for ( i = ++currentSelection, c = aModel.getSize(); i < c; i++ ) {
        Object elem = aModel.getElementAt( i );

        if ( !( elem instanceof IRI ) ) {
          return -1;
        }
        IRI uri = IRI.class.cast( elem );

        if ( uri != null && uri.getLocalName() != null ) {
          v = uri.getLocalName().toLowerCase();
          if ( v.length() > 0 && v.charAt( 0 ) == aKey ) {
            return i;
          }
        }
      }

      for ( i = 0; i < currentSelection; i++ ) {
        Object elem = aModel.getElementAt( i );

        if ( !( elem instanceof IRI ) ) {
          return -1;
        }
        IRI uri = IRI.class.cast( elem );

        if ( uri != null && uri.getLocalName() != null ) {
          v = uri.getLocalName().toLowerCase();
          if ( v.length() > 0 && v.charAt( 0 ) == aKey ) {
            return i;
          }
        }
      }
      return -1;
    }
  }
}
