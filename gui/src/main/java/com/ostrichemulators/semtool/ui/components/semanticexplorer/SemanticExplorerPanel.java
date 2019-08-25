/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManager;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManagerFactory;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTableCellRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.ResourceTreeRenderer;
import com.ostrichemulators.semtool.ui.preferences.SemtoolPreferences;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 *
 * @author john
 */
public class SemanticExplorerPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = -9040079407021692942L;
	private static final Logger log = Logger.getLogger( SemanticExplorerPanel.class );
	private final JTree nodeClassesAndInstances;
	private IEngine engine;
	private final DefaultMutableTreeNode invisibleRoot
			= new DefaultMutableTreeNode( "Please wait while classes and instances populate..." );
	private final JScrollPane leftSide, rightSide;
	private final JSplitPane jSplitPane;
	private final ResourceTreeRenderer renderer = new ResourceTreeRenderer();
	private final InstancePropertyTableModel tablemodel
			= new InstancePropertyTableModel();
	private final JTable propertyTable = new JTable( tablemodel );

	/**
	 * Creates new SemanticExplorerPanel
	 */
	public SemanticExplorerPanel() {
		nodeClassesAndInstances = new JTree();
		nodeClassesAndInstances.setSelectionModel( getDeselectableTreeSelectionModel() );
		nodeClassesAndInstances.addTreeSelectionListener( getTreeSelectionListener() );
		nodeClassesAndInstances.setModel( new DefaultTreeModel( invisibleRoot ) );
		nodeClassesAndInstances.setCellRenderer( renderer );

		leftSide = new JScrollPane();
		leftSide.setViewportView( nodeClassesAndInstances );

		propertyTable.setAutoCreateRowSorter( true );

		rightSide = new JScrollPane();
		rightSide.setViewportView( propertyTable );

		propertyTable.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( MouseEvent e ) {
				int col = propertyTable.columnAtPoint( e.getPoint() );
				if ( 2 == col ) {
					int row = propertyTable.rowAtPoint( e.getPoint() );
					Object obj = tablemodel.getValueAt( row, col );
					if ( obj instanceof IRI ) {
						IRI uri = IRI.class.cast( obj );

						Enumeration<DefaultMutableTreeNode> enumer = invisibleRoot.depthFirstEnumeration();
						while ( enumer.hasMoreElements() ) {
							DefaultMutableTreeNode node = enumer.nextElement();
							if ( node.getUserObject().equals( uri ) ) {
								TreePath tp = new TreePath( node.getPath() );
								nodeClassesAndInstances.getSelectionModel().
										setSelectionPath( tp );
								nodeClassesAndInstances.scrollPathToVisible( tp );
							}
						}
					}
				}
			}
		} );

		jSplitPane = new JSplitPane();
		jSplitPane.setDividerLocation( 250 );
		jSplitPane.setLeftComponent( leftSide );
		jSplitPane.setRightComponent( rightSide );

		GroupLayout layout = new GroupLayout( this );
		setLayout( layout );

		layout.setHorizontalGroup(
				layout.createParallelGroup( GroupLayout.Alignment.LEADING )
				.addComponent( jSplitPane, GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE )
		);

		layout.setVerticalGroup(
				layout.createParallelGroup( GroupLayout.Alignment.LEADING )
				.addComponent( jSplitPane, GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE )
		);
	}

	private DefaultTreeSelectionModel getDeselectableTreeSelectionModel() {
		DefaultTreeSelectionModel treeModel = new DefaultTreeSelectionModel() {
			private static final long serialVersionUID = 891655885831831594L;

			@Override
			public void addSelectionPath( TreePath path ) {
				if ( isPathSelected( path ) ) {
					removeSelectionPath( path );
					return;
				}
				super.addSelectionPath( path );
			}

			@Override
			public void setSelectionPath( TreePath path ) {
				if ( isPathSelected( path ) ) {
					removeSelectionPath( path );
					return;
				}
				super.addSelectionPath( path );
			}
		};

		treeModel.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		return treeModel;
	}

	private TreeSelectionListener getTreeSelectionListener() {
		return new TreeSelectionListener() {
			@Override
			public void valueChanged( TreeSelectionEvent e ) {
				TreePath path = e.getPath();

				switch ( path.getPathCount() ) {
					case 1:
						//This shouldn't be possible because the root node is invisible.
						break;
					case 2:
						tablemodel.clear();
						break;
					case 3:
						DefaultMutableTreeNode dmtn = DefaultMutableTreeNode.class.cast( e.getPath().getLastPathComponent() );
						IRI instance = IRI.class.cast( dmtn.getUserObject() );
						ModelQueryAdapter mqa = ModelQueryAdapter.describe( instance );
						mqa.useInferred( false );
						tablemodel.setModel( engine.constructNoEx( mqa ) );
						break;
					default:
						break;
				}
			}
		};
	}

	public void populateDataForThisDB() {
		if ( engine == null ) {
			return;
		}

		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				invisibleRoot.removeAllChildren();

				RetrievingLabelCache rlc = new RetrievingLabelCache( engine );

				renderer.setLabelCache( rlc );
				renderer.setColorShapeRepository( DIHelper.getInstance().
						getMetadataStore().getCSRepo( engine.getBaseIri() ) );
				renderer.setUseLabels( SemtoolPreferences.get()
						.getBoolean( Constants.SEMEX_USE_LABELS_PREF, true ) );

				StructureManager sm = StructureManagerFactory.getStructureManager( engine );

				Set<IRI> concepts = sm.getTopLevelConcepts();
				Map<IRI, String> clbls = Utility.getInstanceLabels( concepts, engine );
				Map<IRI, String> sortedconcepts = Utility.sortUrisByLabel( clbls );
				rlc.putAll( clbls );

				for ( IRI concept : sortedconcepts.keySet() ) {
					DefaultMutableTreeNode conceptNode = new DefaultMutableTreeNode( concept );
					invisibleRoot.add( conceptNode );

					List<IRI> instances = NodeDerivationTools.createInstanceList( concept, engine );
					Map<IRI, String> labels = Utility.getInstanceLabels( instances, engine );
					labels = Utility.sortUrisByLabel( labels );
					rlc.putAll( labels );

					for ( IRI instance : labels.keySet() ) {
						DefaultMutableTreeNode instNode = new DefaultMutableTreeNode( instance );
						conceptNode.add( instNode );
					}
				}

				nodeClassesAndInstances.setModel( new DefaultTreeModel( invisibleRoot ) );
				nodeClassesAndInstances.setRootVisible( false );

				LabeledPairTableCellRenderer<Value> tablerenderer
						= LabeledPairTableCellRenderer.getValuePairRenderer( rlc );
				propertyTable.setDefaultRenderer( IRI.class, tablerenderer );
				propertyTable.setDefaultRenderer( Value.class, tablerenderer );
				tablerenderer.cache( XMLSchema.ANYURI, "IRI" );
			}
		} );
	}

	public void setEngine( IEngine engine ) {
		this.engine = engine;
		populateDataForThisDB();
	}
}
