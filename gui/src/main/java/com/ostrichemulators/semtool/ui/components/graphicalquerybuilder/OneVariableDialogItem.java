/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValueSet;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {

	private final IRI property;
	private final QueryGraphElement node;
	private final String dlgtext;
	private final GraphicalQueryPanel panel;
	private final Set<Value> currvals;
	private Map<IRI, String> propTypeChoices;
	private Collection<IRI> choicesModel;

	public OneVariableDialogItem( QueryGraphElement node, GraphicalQueryPanel panel,
			IRI prop, String label, String tooltip, String dlgtext ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = node;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;

		currvals = ( null == node.getValues( property )
				? new HashSet<>() : node.getValues( property ) );
		propTypeChoices = null;
	}

	public OneVariableDialogItem( QueryGraphElement nod, GraphicalQueryPanel panel,
			IRI prop, String label, String tooltip, String dlgtext, Map<IRI, String> labels ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = nod;
		this.dlgtext = dlgtext;
		this.panel = panel;
		this.propTypeChoices = labels;
		property = prop;
		currvals = ( null == node.getValues( property )
				? new HashSet<>() : node.getValues( property ) );
	}

	public OneVariableDialogItem( QueryGraphElement nod, GraphicalQueryPanel panel,
			IRI prop, String label, String tooltip, String dlgtext,
			Collection<IRI> choicesQ ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = nod;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;
		currvals = ( null == node.getValues( property )
				? new HashSet<>() : node.getValues( property ) );
		choicesModel = choicesQ;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		ConstraintValueSet values = null;

		if ( null != choicesModel ) {
			Set<IRI> IRIs = new HashSet<>( choicesModel );
			propTypeChoices = Utility.getInstanceLabels( IRIs, panel.getEngine() );

			// if there are no property choices, don't show the dialog
			if ( propTypeChoices.isEmpty() ) {
				JOptionPane.showMessageDialog( panel,
						"There are no properties for this entity type", "No Properties",
						JOptionPane.INFORMATION_MESSAGE );
				return;
			}

			propTypeChoices.put( Constants.ANYNODE, "<Any>" );
			propTypeChoices = Utility.sortIrisByLabel( propTypeChoices );
		}

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
			currvals.addAll( values );
			node.setProperties( values.property, currvals );
			node.setSelected( values.property, values.included );
			if ( null != values.raw ) {
				node.setFilter( property, values.raw );
			}
			panel.update();
		}
	}
}
