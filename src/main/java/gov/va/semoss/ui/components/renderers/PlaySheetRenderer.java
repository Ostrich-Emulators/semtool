/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import org.apache.log4j.Logger;

import gov.va.semoss.util.DefaultPlaySheetIcons;

/**   Renders icon + text for items in the Playsheet ComboBox, of the 
 * "Custom Sparql Query" window.
 *
 * @author Thomas
 */
public class PlaySheetRenderer extends DefaultListCellRenderer {

   private static final long serialVersionUID = 1L;
   private static final Logger log = Logger.getLogger( PlaySheetRenderer.class );

 
  public PlaySheetRenderer() {
  }

  @Override
  public Component getListCellRendererComponent( JList<?> list, Object val, int idx,
      boolean sel, boolean hasfocus ) {
      String text = val.toString();
 
      super.getListCellRendererComponent( list, text, idx, sel, hasfocus );

      Icon icon = DefaultPlaySheetIcons.defaultIcons.get("("+text+")");
      if(icon == null){
    	 icon = DefaultPlaySheetIcons.blank;
      }
      setIcon(icon);
      
      return this;
  }
}
