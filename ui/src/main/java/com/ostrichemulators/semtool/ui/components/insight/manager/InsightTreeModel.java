/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.insight.manager;

import com.ostrichemulators.semtool.om.Insight;
import com.ostrichemulators.semtool.om.Parameter;
import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author ryan
 */
public class InsightTreeModel extends DefaultTreeModel {

	public InsightTreeModel() {
		super( new DefaultMutableTreeNode() );
	}

	public void refresh( InsightManager wim ) {
		DefaultMutableTreeNode top = DefaultMutableTreeNode.class.cast( getRoot() );
		top.removeAllChildren();

		for ( Perspective p : wim.getPerspectives() ) {
			DefaultMutableTreeNode perspectiveItem = new DefaultMutableTreeNode( p );
			top.add( perspectiveItem );

			for ( Insight i : p.getInsights() ) {
				DefaultMutableTreeNode insightItem = new DefaultMutableTreeNode( i );
				perspectiveItem.add( insightItem );

				for ( Parameter a : i.getInsightParameters() ) {
					DefaultMutableTreeNode parameterItem = new DefaultMutableTreeNode( a );
					insightItem.add( parameterItem );
				}
			}
		}
		nodeStructureChanged( root );
	}
}
