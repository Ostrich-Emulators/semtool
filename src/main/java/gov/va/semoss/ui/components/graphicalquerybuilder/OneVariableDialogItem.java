/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.models.ValueTableModel;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {

	private final URI property;
	private final AbstractNodeEdgeBase node;
	private final String dlgtext;
	private final GraphicalQueryBuilderPanel panel;
	private final Map<URI, String> labels;
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
		Value newval = null;
		boolean included[] = { false };

		if ( null == labels ) {
			newval = ConstraintPanel.getValue( property, dlgtext, currval,
					node.isMarked( property ), included );
		}
		else {
			newval = ConstraintPanel.getValue( property, dlgtext, URI.class.cast( currval ),
					labels, node.isMarked( property ), included );
		}

		if ( null != newval ) {
			Object value = ( newval instanceof URI ? newval
					: ValueTableModel.getValueFromLiteral( Literal.class.cast( newval ) ) );
			node.setProperty( property, value );
			node.mark( property, included[0] );
			currval = newval;
			panel.update();
		}
	}
}
