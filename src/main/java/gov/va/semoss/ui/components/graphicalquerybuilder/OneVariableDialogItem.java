/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.util.MultiMap;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {

	private final URI property;
	private final AbstractNodeEdgeBase node;
	private final String dlgtext;
	private final GraphicalQueryBuilderPanel panel;
	private final Map<?, String> labels;
	private Object currval;

	public OneVariableDialogItem( AbstractNodeEdgeBase node,
			GraphicalQueryBuilderPanel panel, URI prop,
			String label, String tooltip, String dlgtext ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;

		currval = this.node.getProperty( property );
		labels = null;
	}

	public OneVariableDialogItem( AbstractNodeEdgeBase node,
			GraphicalQueryBuilderPanel panel, URI prop,
			String label, String tooltip, String dlgtext, Map<?, String> labels ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		this.labels = labels;
		property = prop;

		currval = this.node.getProperty( property );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Object newval = null;
		boolean ok = false;
		if ( null == labels ) {
			newval = JOptionPane.showInputDialog( null, dlgtext, currval );
			ok = ( null != newval );
		}
		else {
			Map<String, ?> lossy = MultiMap.lossyflip( labels );
			currval = labels.get( currval );

			newval = JOptionPane.showInputDialog( null, dlgtext, dlgtext,
					JOptionPane.PLAIN_MESSAGE, null, lossy.keySet().toArray(), currval );
			if ( null != newval ) {
				newval = lossy.get( newval.toString() );
				ok = true;
			}
		}

		if ( ok ) {
			node.setProperty( property, newval );
			currval = newval;
			panel.update();
		}
	}
}
