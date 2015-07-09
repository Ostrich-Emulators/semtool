/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import gov.va.semoss.om.Perspective;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;

/**
 *
 * @author ryan
 */
public class PerspectiveRenderer extends DefaultListCellRenderer {

  private static final Logger log = Logger.getLogger( PerspectiveRenderer.class );
  private final Map<URI, String> nameCache = new HashMap<>();
  private InsightManager engine;

  public PerspectiveRenderer() {
  }

  public PerspectiveRenderer( IEngine eng ) {
		setEngine( eng );
  }

	public void setEngine( IEngine eng ) {
    engine = ( null == eng ? null : eng.getInsightManager() );
    nameCache.clear();
  }

  @Override
  public Component getListCellRendererComponent( JList list, Object val, int idx,
      boolean sel, boolean hasfocus ) {
    // figure out 
    
    String text = "";
    if ( null == val ) {
      return super.getListCellRendererComponent( list, text, idx, sel, hasfocus );
    }

    Perspective p = Perspective.class.cast( val );
    URI q = p.getUri();

    if ( !( nameCache.containsKey( q ) || null == engine ) ) {
      text += engine.getLabel( q );
      nameCache.put( q, text );
    }

    return super.getListCellRendererComponent( list, nameCache.get( q ), idx,
        sel, hasfocus );
  }
}
