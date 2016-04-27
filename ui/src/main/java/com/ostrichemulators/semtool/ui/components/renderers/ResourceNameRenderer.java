package com.ostrichemulators.semtool.ui.components.renderers;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ResourceNameRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 100304L;

	@Override
	public Component getTableCellRendererComponent( JTable table, Object val,
			boolean isSelected, boolean hasFocus, int row, int column ) {
		JComponent c = (JComponent)super.getTableCellRendererComponent( table, val, isSelected,
				hasFocus, row, column );
		c.setToolTipText(val.toString());
		
		return c;
	}
}
