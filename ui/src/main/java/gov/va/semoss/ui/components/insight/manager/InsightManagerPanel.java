/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.util.EngineOperationListener;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.renderers.PerspectiveTreeCellRenderer;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class InsightManagerPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( InsightManagerPanel.class );
	private WriteableInsightManager wim;
	private final InsightTreeModel model = new InsightTreeModel();
	private IEngine engine;
	private DataPanel currentCard;
	private final PropertyChangeListener propChangeListener;

	/**
	 * Creates new form InsightManagerPanel
	 */
	public InsightManagerPanel() {
		initComponents();

		propChangeListener = new PropertyChangeListener() {

			@Override
			public void propertyChange( PropertyChangeEvent evt ) {
				if ( null != currentCard ) {
					applybtn.setEnabled( currentCard.hasChanges() );
					commitbtn.setEnabled( !applybtn.isEnabled() );
				}
			}
		};

		tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		tree.setCellRenderer( new PerspectiveTreeCellRenderer() );

		setupTreeListeners();
	}

	private void setupTreeListeners() {
		//tree.setTransferHandler( new TreeTransferHandler( model ) );
		model.addTreeModelListener( new TreeModelListener() {

			@Override
			public void treeNodesChanged( TreeModelEvent e ) {
				commitbtn.setEnabled( true );
			}

			@Override
			public void treeNodesInserted( TreeModelEvent e ) {
				commitbtn.setEnabled( true );
			}

			@Override
			public void treeNodesRemoved( TreeModelEvent e ) {
				commitbtn.setEnabled( true );
			}

			@Override
			public void treeStructureChanged( TreeModelEvent e ) {
				commitbtn.setEnabled( true );
			}
		} );

		tree.addMouseListener( new InsightMenu( tree, model ) );
		tree.addTreeSelectionListener( new TreeSelectionListener() {

			@Override
			public void valueChanged( TreeSelectionEvent e ) {
				DefaultMutableTreeNode node
						= DefaultMutableTreeNode.class.cast( tree.getLastSelectedPathComponent() );

				CardLayout layout = CardLayout.class.cast( dataArea.getLayout() );
				TreePath newpath = e.getNewLeadSelectionPath();

				if ( null == newpath ) {
					return;
				}

				if ( !( newpath.equals( e.getOldLeadSelectionPath() )
						|| null == currentCard ) ) {

					// don't need to listen to changes from the old panel anymore
					currentCard.removePropertyChangeListener( DataPanel.CHANGE_PROPERTY,
							propChangeListener );

					if ( currentCard.hasChanges() ) {
						int ans = JOptionPane.showConfirmDialog( currentCard,
								"This data has changed.\nApply changes before leaving?",
								"Apply Changes?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE );
						if ( JOptionPane.YES_OPTION == ans ) {
							DefaultMutableTreeNode dmtn
									= DefaultMutableTreeNode.class.cast( e.getOldLeadSelectionPath().getLastPathComponent() );
							dmtn.setUserObject( currentCard.applyChanges() );
							model.nodeChanged( dmtn );
							commitbtn.setEnabled( true );
						}
					}
				}

				switch ( e.getNewLeadSelectionPath().getPathCount() ) {
					case 4:
						// parameter
						parameterData.setInsight(
								Insight.class.cast( DefaultMutableTreeNode.class.cast( node.getParent() ).getUserObject() ) );
						parameterData.setElement( Parameter.class.cast( node.getUserObject() ), node );
						layout.show( dataArea, "parameter" );
						currentCard = parameterData;
						break;
					case 3:
						// insight;
						insightData.setElement( Insight.class.cast( node.getUserObject() ), node );
						layout.show( dataArea, "insight" );
						currentCard = insightData;
						break;
					default:
						// perspective
						perspectiveData.setElement( Perspective.class.cast( node.getUserObject() ), node );
						layout.show( dataArea, "perspective" );
						currentCard = perspectiveData;
				}

				applybtn.setEnabled( false );
				currentCard.addPropertyChangeListener( DataPanel.CHANGE_PROPERTY,
						propChangeListener );
			}
		} );
	}

	public void setEngine( IEngine eng ) {
		engine = eng;

		insightData.setEngine( engine );
		parameterData.setEngine( engine );
		perspectiveData.setEngine( engine );

		if ( null != wim ) {
			wim.release();
		}

		wim = engine.getWriteableInsightManager();
		model.refresh( wim );

		for ( int i = 0; i < tree.getRowCount(); i++ ) {
			tree.expandRow( i );
		}

		tree.setSelectionRow( 0 );
		commitbtn.setEnabled( false );
		applybtn.setEnabled( false );
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    tree = new javax.swing.JTree();
    rightside = new javax.swing.JPanel();
    dataArea = new javax.swing.JPanel();
    perspectiveData = new gov.va.semoss.ui.components.insight.manager.PerspectivePanel( tree, model );
    insightData = new gov.va.semoss.ui.components.insight.manager.InsightPanel( tree, model );
    parameterData = new gov.va.semoss.ui.components.insight.manager.ParameterPanel( tree, model );
    jPanel1 = new javax.swing.JPanel();
    applybtn = new javax.swing.JButton();
    commitbtn = new javax.swing.JButton();

    jSplitPane1.setDividerLocation(250);

    tree.setModel(model);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    jScrollPane1.setViewportView(tree);

    jSplitPane1.setLeftComponent(jScrollPane1);

    rightside.setLayout(new java.awt.BorderLayout());

    dataArea.setPreferredSize(new java.awt.Dimension(401, 438));
    dataArea.setLayout(new java.awt.CardLayout());
    dataArea.add(perspectiveData, "perspective");
    dataArea.add(insightData, "insight");
    dataArea.add(parameterData, "parameter");

    rightside.add(dataArea, java.awt.BorderLayout.CENTER);

    applybtn.setText("Apply Changes");
    applybtn.setEnabled(false);
    applybtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        applybtnActionPerformed(evt);
      }
    });

    commitbtn.setText("Commit to DB");
    commitbtn.setEnabled(false);
    commitbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        commitbtnActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap(116, Short.MAX_VALUE)
        .addComponent(commitbtn)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(applybtn)
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addGap(0, 0, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(applybtn)
          .addComponent(commitbtn))
        .addContainerGap())
    );

    rightside.add(jPanel1, java.awt.BorderLayout.PAGE_END);

    jSplitPane1.setRightComponent(rightside);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

  private void applybtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applybtnActionPerformed
		DefaultMutableTreeNode dmtn
				= DefaultMutableTreeNode.class.cast( tree.getLastSelectedPathComponent() );
		dmtn.setUserObject( currentCard.applyChanges() );
		model.nodeChanged( dmtn );
		applybtn.setEnabled( false );
		commitbtn.setEnabled( true );
  }//GEN-LAST:event_applybtnActionPerformed

	private List<Perspective> convertTreeToPerspectives() {
		List<Perspective> perspectives = new ArrayList<>();
		DefaultMutableTreeNode root
				= DefaultMutableTreeNode.class.cast( model.getRoot() );
		Enumeration<DefaultMutableTreeNode> perspIt = root.children();

		while ( perspIt.hasMoreElements() ) {
			DefaultMutableTreeNode perspnode = perspIt.nextElement();
			Perspective persp = Perspective.class.cast( perspnode.getUserObject() );
			perspectives.add( persp );

			List<Insight> insights = new ArrayList<>();
			Enumeration<DefaultMutableTreeNode> insIt = perspnode.children();
			while ( insIt.hasMoreElements() ) {
				DefaultMutableTreeNode insnode = insIt.nextElement();
				Insight ins = Insight.class.cast( insnode.getUserObject() );
				insights.add( ins );

				List<Parameter> params = new ArrayList<>();
				Enumeration<DefaultMutableTreeNode> parmIt = insnode.children();
				while ( parmIt.hasMoreElements() ) {
					DefaultMutableTreeNode parmnode = parmIt.nextElement();
					Parameter param = Parameter.class.cast( parmnode.getUserObject() );
					params.add( param );
				}
				ins.setParameters( params );
			}
			persp.setInsights( insights );
		}

		return perspectives;
	}

  private void commitbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commitbtnActionPerformed
		// rebuild all the perspectives from our tree nodes and 
		// write everything back to the database
		List<Perspective> perspectives = convertTreeToPerspectives();

		ProgressTask pt = new ProgressTask( "Committing Insights", new Runnable() {

			@Override
			public void run() {
				EngineOperationListener eol = new EngineOperationListener() {

					@Override
					public void engineOpened( IEngine eng ) {
					}

					@Override
					public void engineClosed( IEngine eng ) {
					}

					@Override
					public void insightsModified( IEngine eng, Collection<URI> perspectives,
							Collection<URI> numinsights ) {
						commitbtn.setEnabled( false );
						EngineUtil.getInstance().removeEngineOpListener( this );
					}
				};

				wim.setData( perspectives );
				EngineUtil.getInstance().addEngineOpListener( eol );
				EngineUtil.getInstance().importInsights( wim );
			}
		} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
  }//GEN-LAST:event_commitbtnActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton applybtn;
  private javax.swing.JButton commitbtn;
  private javax.swing.JPanel dataArea;
  private gov.va.semoss.ui.components.insight.manager.InsightPanel insightData;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSplitPane jSplitPane1;
  private gov.va.semoss.ui.components.insight.manager.ParameterPanel parameterData;
  private gov.va.semoss.ui.components.insight.manager.PerspectivePanel perspectiveData;
  private javax.swing.JPanel rightside;
  private javax.swing.JTree tree;
  // End of variables declaration//GEN-END:variables
}