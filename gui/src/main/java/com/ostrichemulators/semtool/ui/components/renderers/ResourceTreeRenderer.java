package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import com.ostrichemulators.semtool.util.IconBuilder;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openrdf.model.URI;

/**
 * A renderer for DefaultMutableTreeNodes that have a Resource as a UserObject
 *
 * @author ryan
 */
public class ResourceTreeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 100304L;
	private RetrievingLabelCache rlc;
	private boolean useLabels = false;
	private GraphColorShapeRepository shapefactory
			= new DefaultColorShapeRepository();

	public ResourceTreeRenderer( RetrievingLabelCache rlc ) {
		this.rlc = rlc;
		useLabels = true;
	}

	public ResourceTreeRenderer() {
		rlc = null;
	}

	public ResourceTreeRenderer( RetrievingLabelCache rlc, GraphColorShapeRepository repo ) {
		this.rlc = rlc;
		this.shapefactory = repo;
	}

	public void setLabelCache( RetrievingLabelCache rlc ) {
		this.rlc = rlc;
	}

	public void setColorShapeRepository( GraphColorShapeRepository repo ) {
		this.shapefactory = repo;
	}

	public void setUseLabels( boolean b ) {
		this.useLabels = b;
	}

	@Override
	public Component getTreeCellRendererComponent( JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus ) {

		DefaultMutableTreeNode dmtNode = DefaultMutableTreeNode.class.cast( value );
		Object o = dmtNode.getUserObject();

		if ( null == o || !( o instanceof URI ) ) {
			return super.getTreeCellRendererComponent( tree, o, selected, expanded,
					leaf, row, hasFocus );
		}

		URI uri = URI.class.cast( dmtNode.getUserObject() );
		String text = ( useLabels && null != rlc
				? rlc.get( uri )
				: uri.getLocalName() );

		super.getTreeCellRendererComponent( tree, text, selected, expanded,
				leaf, row, hasFocus );

		if ( !dmtNode.isLeaf() ) {
			NamedShape shape = shapefactory.getShape( uri );
			setIcon( new IconBuilder( shape ).setStroke( Color.BLACK )
					.setPadding( 2 ).setIconSize( 18 ).build() );
		}

		return this;
	}
}
