/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValue;
import gov.va.semoss.ui.components.models.ValueTableModel;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {
	private final URI property;
	private final AbstractNodeEdgeBase node;
	private final String dlgtext;
	private final GraphicalQueryPanel panel;
	private final Map<URI, String> labels;
	private Object currval;

	public OneVariableDialogItem( AbstractNodeEdgeBase node,
			GraphicalQueryPanel panel, URI prop,
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
			GraphicalQueryPanel panel, URI prop,
			String label, String tooltip, String dlgtext, Map<URI, String> labels ) {

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
		ConstraintValue newval = null;

		if ( null == labels ) {
			newval = ConstraintPanel.getValue( property, dlgtext, currval,
					node.isMarked( property ) );
		}
		else {
			if ( null == property ) {
				newval = ConstraintPanel.getValue( dlgtext, null, labels );
			}
			else {
				newval = ConstraintPanel.getValue( property, dlgtext, URI.class.cast( currval ),
						labels, node.isMarked( property ) );
			}
		}

		if ( null != newval ) {
			Object value = ( newval.val instanceof URI ? newval.val
					: ValueTableModel.getValueFromLiteral( Literal.class.cast( newval.val ) ) );
			node.setProperty( newval.property, value );
			node.mark( newval.property, newval.included );
			currval = newval;
			panel.update();
		}
	}
}
