/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.graphicalquerybuilder.GraphicalQueryPanel.QueryOrder;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ryan
 */
public class FilterRenderer extends DefaultTableCellRenderer {

	private final SparqlResultTableModel model;

	public FilterRenderer( SparqlResultTableModel model ) {
		this.model = model;
	}

	@Override
	public Component getTableCellRendererComponent( JTable table, Object value,
			boolean sel, boolean focus, int row, int col ) {
		String text = "";
		if ( null != value ) {
			QueryOrder qo = model.getRawRow( row );
			String label = qo.base.getLabel( qo.property );

			text = FilterEditor.validToShorthand( value.toString(), label );
		}

		return super.getTableCellRendererComponent( table, text, sel, focus, row, col );
	}
}
