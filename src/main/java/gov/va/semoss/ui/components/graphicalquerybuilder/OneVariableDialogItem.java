/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private final Value currval;
	private Set<Value> currvals;

	public OneVariableDialogItem( QueryNodeEdgeBase node, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;

		currval = this.node.getValue( property );
		currvals = this.node.getValues( property );
		propTypeChoices = null;
	}

	public OneVariableDialogItem( QueryNodeEdgeBase nod, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext, Map<URI, String> labels ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = nod;
		this.dlgtext = dlgtext;
		this.panel = panel;
		this.propTypeChoices = labels;
		property = prop;
		currval = node.getValue( property );
		currvals = ( null == node.getValues( property )
				? new HashSet<>() : node.getValues( property ) );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Collection<ConstraintValue> values = null;

		if ( null == propTypeChoices ) {
			// set a specific property
			values = ConstraintPanel.getValues( property, dlgtext, currvals,
					node.isSelected( property ) );
		}
		else {
			if ( null == property ) {
				// "add constraint" where you don't know what property the user will select
				values = ConstraintPanel.getValues( dlgtext, propTypeChoices );
			}
			else {
				// "type" constraint
				values = ConstraintPanel.getValues( property, dlgtext, currvals,
						propTypeChoices, node.isSelected( property ) );
			}
		}

		if ( null != values ) {
			currvals.clear();
			List<Value> vals = new ArrayList<>();
			URI prop = null;
			boolean incl = false;
			for ( ConstraintValue cv : values ) {
				vals.add( cv.val );
				if ( null == prop ) {
					prop = cv.property;
					incl = cv.included;
				}
			}

			currvals.addAll( vals );
			node.setProperties( prop, currvals );
			node.setSelected( prop, incl );
			panel.update();
		}
	}
}
