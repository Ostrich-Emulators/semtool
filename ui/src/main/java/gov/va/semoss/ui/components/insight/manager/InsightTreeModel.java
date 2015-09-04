/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
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
	
	public void refresh( WriteableInsightManager wim ){
		DefaultMutableTreeNode top = DefaultMutableTreeNode.class.cast( getRoot() );
		top.removeAllChildren();
		
		for( Perspective p : wim.getPerspectives() ){
			DefaultMutableTreeNode perspectiveItem = new DefaultMutableTreeNode( p );
			top.add( perspectiveItem );
			
			for( Insight i : p.getInsights() ) {
				DefaultMutableTreeNode insightItem = new DefaultMutableTreeNode( i );
				perspectiveItem.add( insightItem );
				
				for( Parameter a : i.getInsightParameters() ){
					DefaultMutableTreeNode parameterItem = new DefaultMutableTreeNode( a );
					insightItem.add( parameterItem );			
				}
			}
		}
	}
}
