/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValue;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {

	private final URI property;
	private final QueryNodeEdgeBase node;
	private final String dlgtext;
	private final GraphicalQueryPanel panel;
	private final Map<URI, String> propTypeChoices;
	private Value currval;

	public OneVariableDialogItem( QueryNodeEdgeBase node, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;

		currval = this.node.getValue( property );
		propTypeChoices = null;
	}

	public OneVariableDialogItem( QueryNodeEdgeBase node, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext, Map<URI, String> labels ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		this.propTypeChoices = labels;
		property = prop;
		currval = this.node.getValue( property );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		ConstraintValue newval = null;

		if ( null == propTypeChoices ) {
			// set a specific property
			newval = ConstraintPanel.getValue( property, dlgtext, currval,
					node.isSelected( property ) );
		}
		else {
			if ( null == property ) {
				// "add constraint" where you don't know what property the user will select
				newval = ConstraintPanel.getValue( dlgtext, propTypeChoices );
			}
			else {
				// "type" constraint
				newval = ConstraintPanel.getValue( property, dlgtext, URI.class.cast( currval ),
						propTypeChoices, node.isSelected( property ) );
			}
		}

		if ( null != newval ) {
			currval = newval.val;
			node.setValue( newval.property, currval );
			node.setSelected( newval.property, newval.included );
			panel.update();
		}
	}
}
