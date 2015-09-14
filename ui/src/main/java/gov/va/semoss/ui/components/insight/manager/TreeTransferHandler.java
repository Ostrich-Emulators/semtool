/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author ryan
 */
public class TreeTransferHandler extends TransferHandler {

	private final InsightTreeModel model;

	public TreeTransferHandler( InsightTreeModel mo ) {
		model = mo;
	}

	@Override
	public boolean canImport( TransferHandler.TransferSupport support ) {
		if ( !support.isDataFlavorSupported( DataFlavor.stringFlavor )
				|| !support.isDrop() ) {
			return false;
		}

		JTree.DropLocation dropLocation
				= (JTree.DropLocation) support.getDropLocation();

		return dropLocation.getPath() != null;
	}

	@Override
	public boolean importData( TransferHandler.TransferSupport support ) {
		if ( !canImport( support ) ) {
			return false;
		}

		JTree.DropLocation dropLocation
				= (JTree.DropLocation) support.getDropLocation();

		TreePath path = dropLocation.getPath();

		Transferable transferable = support.getTransferable();

		String transferData;
		try {
			transferData = (String) transferable.getTransferData(
					DataFlavor.stringFlavor );
		}
		catch ( IOException | UnsupportedFlavorException e ) {
			return false;
		}

		int childIndex = dropLocation.getChildIndex();
		if ( childIndex == -1 ) {
			childIndex = model.getChildCount( path.getLastPathComponent() );
		}

		DefaultMutableTreeNode newNode
				= new DefaultMutableTreeNode( transferData );
		DefaultMutableTreeNode parentNode
				= (DefaultMutableTreeNode) path.getLastPathComponent();
		model.insertNodeInto( newNode, parentNode, childIndex );
		return true;
	}
}
