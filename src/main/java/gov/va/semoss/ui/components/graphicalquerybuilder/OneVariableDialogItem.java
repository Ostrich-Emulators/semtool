/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValueSet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class OneVariableDialogItem extends AbstractAction {

	private final URI property;
	private final QueryNodeEdgeBase node;
	private final String dlgtext;
	private final GraphicalQueryPanel panel;
	private final Set<Value> currvals;
	private Map<URI, String> propTypeChoices;
	private ListQueryAdapter<URI> choicesQuery;

	public OneVariableDialogItem( QueryNodeEdgeBase node, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext ) {

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

	public OneVariableDialogItem( QueryNodeEdgeBase nod, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext, Map<URI, String> labels ) {

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

	public OneVariableDialogItem( QueryNodeEdgeBase nod, GraphicalQueryPanel panel,
			URI prop, String label, String tooltip, String dlgtext,
			ListQueryAdapter<URI> choicesQ ) {

		super( label );
		putValue( Action.SHORT_DESCRIPTION, tooltip );

		this.node = nod;
		this.dlgtext = dlgtext;
		this.panel = panel;
		property = prop;
		currvals = ( null == node.getValues( property )
				? new HashSet<>() : node.getValues( property ) );
		choicesQuery = choicesQ;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		ConstraintValueSet values = null;

		if ( null != choicesQuery ) {
			try {
				List<URI> uris = panel.getEngine().query( choicesQuery );
				propTypeChoices = Utility.getInstanceLabels( uris, panel.getEngine() );

				// if there are no property choices, don't show the dialog
				if ( propTypeChoices.isEmpty() ) {
					JOptionPane.showMessageDialog( panel,
							"There are no properties for this entity type", "No Properties",
							JOptionPane.INFORMATION_MESSAGE );
					return;
				}

				propTypeChoices.put( Constants.ANYNODE, "<Any>" );
				propTypeChoices = Utility.sortUrisByLabel( propTypeChoices );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException ex ) {
				Logger.getLogger( getClass() ).error( ex, ex );
			}
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
			node.setPropertyMetadata( property, values.raw );
			panel.update();
		}
	}
}
