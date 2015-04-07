/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T> type of entity we'll store labels for
 */
public class LabeledPairRenderer<T> extends DefaultListCellRenderer {

  private static final Logger log = Logger.getLogger( LabeledPairRenderer.class );
  private final Map<T, String> labelCache = new HashMap<>();
  private boolean isfetching = false;

  public boolean isIsfetching() {
    return isfetching;
  }

  public void setIsfetching( boolean isfetching ) {
    this.isfetching = isfetching;
  }

  public Map<T, String> getCachedLabels() {
    return new HashMap<>( labelCache );
  }

  public void cache( Map<T, String> map ) {
    labelCache.putAll( map );
  }

  public void cache( T u, String label ) {
    labelCache.put( u, label );
  }

  @Override
  public Component getListCellRendererComponent( JList<?> list, Object value,
      int idx, boolean sel, boolean focused ) {

    if( value instanceof String ){
      // not sure how we're getting here
      return super.getListCellRendererComponent( list, value, idx, sel, focused );
    }
    
    String text = ( isfetching ? "fetching" : "" );
    if ( null != value ) {
      T val = (T) value;

      if ( !labelCache.containsKey( val ) ) {
        cache( val, getLabelForCacheMiss( val ) );
      }

      text = labelCache.get( val );
    }

    return super.getListCellRendererComponent( list, text, idx, sel, focused );
  }

  protected String getLabelForCacheMiss( T val ) {
    return val.toString();
  }

  public static LabeledPairRenderer<URI> getUriPairRenderer() {
    return new LabeledPairRenderer<URI>() {
      @Override
      protected String getLabelForCacheMiss( URI val ) {
        return val.getLocalName();
      }
    };
  }
	
	public void clearCache(){
		labelCache.clear();
	}
}
