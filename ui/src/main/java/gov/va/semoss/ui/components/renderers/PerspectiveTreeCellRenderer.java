/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.util.DefaultPlaySheetIcons;
import gov.va.semoss.util.PlaySheetEnum;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author ryan
 */
public class PerspectiveTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent( JTree tree, Object tvalue,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
		if ( null == tvalue ) {
			return super.getTreeCellRendererComponent( tree, tvalue, sel, expanded,
					leaf, row, hasFocus );
		}

		DefaultMutableTreeNode node = DefaultMutableTreeNode.class.cast( tvalue );
		Object value = node.getUserObject();

		if ( null == value ) {
			return super.getTreeCellRendererComponent( tree, null, sel, expanded,
					leaf, row, hasFocus );
		}

		String text = null;
		String ttip = null;
		Icon icon = null;
		if ( value instanceof Insight ) {
			Insight ins = Insight.class.cast( value );
			text = ins.getLabel();
			ttip = ins.getDescription();
			PlaySheetEnum pse = PlaySheetEnum.valueFor( ins );
			icon = DefaultPlaySheetIcons.getDefaultIcon( pse );
		}
		else if ( value instanceof Perspective ) {
			Perspective p = Perspective.class.cast( value );
			text = p.getLabel();
			ttip = p.getDescription();
		}
		else {
			Parameter p = Parameter.class.cast( value );
			text = p.getLabel();
			ttip = p.getDefaultQuery();
		}

		super.getTreeCellRendererComponent( tree, text, sel, expanded,
				leaf, row, hasFocus );
		setIcon( icon );
		setToolTipText( ttip );
		return this;
	}

}
