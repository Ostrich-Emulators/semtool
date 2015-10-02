/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.semanticexplorer;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.ui.components.PaintLabel;
import gov.va.semoss.ui.helpers.DynamicColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.util.Constants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author john
 */
public class SemanticExplorerPanel extends javax.swing.JPanel {
	private static final long serialVersionUID = -9040079407021692942L;
	private static final Logger log = Logger.getLogger( SemanticExplorerPanel.class );
	private JTree nodeClassesAndInstances;
	private IEngine engine;
	private DefaultMutableTreeNode invisibleRoot = new DefaultMutableTreeNode( "Please wait while classes and instances populate..." );
	
	private JScrollPane leftSide, rightSide;
	private JSplitPane jSplitPane;
	private JTable propertyTable;
	
	private boolean useLabels = false;

	/**
	 * Creates new SemanticExplorerPanel
	 */
	public SemanticExplorerPanel() {
		nodeClassesAndInstances = new JTree();
		nodeClassesAndInstances.setSelectionModel( getDeselectableTreeSelectionModel() );
		nodeClassesAndInstances.addTreeSelectionListener( getTreeSelectionListener() );
		nodeClassesAndInstances.setModel( new DefaultTreeModel( invisibleRoot ) );
		nodeClassesAndInstances.setCellRenderer( getTreeCellRenderer() );
		
		leftSide = new JScrollPane();
		leftSide.setViewportView(nodeClassesAndInstances);
	    
		propertyTable = new JTable();
		propertyTable.setAutoCreateRowSorter(true);

		rightSide = new JScrollPane();
		rightSide.setViewportView( propertyTable );

		jSplitPane = new JSplitPane();
		jSplitPane.setDividerLocation(250);
		jSplitPane.setLeftComponent(leftSide);
		jSplitPane.setRightComponent(rightSide);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jSplitPane, GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
		);
		
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(jSplitPane, GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
		);
	}

	private List<URI> runConceptsQuery() {
		String conceptsQuery
				= "SELECT DISTINCT ?returnVariable WHERE {"
				+ "  ?returnVariable a owl:Class . "
				+ "} ORDER BY ?returnVariable";
		
		OneVarListQueryAdapter<URI> queryer
				= OneVarListQueryAdapter.getUriList( conceptsQuery, "returnVariable" );
		
		try {
			return engine.query( queryer );
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			log.error("Could not query concepts: " + e, e);
			return null;
		}
	}
	
	private List<Value[]> runInstancesAndLabelsQuery(URI concept) {
		String instancesQuery
				= "SELECT DISTINCT ?instance ?label WHERE {"
				+ "  ?instance rdf:type ?concept . "
				+ "  OPTIONAL { ?instance rdfs:label ?label . }"
				+ "} ORDER BY ?instance";

		ListQueryAdapter<Value[]> instancesQA = new ListQueryAdapter<Value[]>( instancesQuery ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				Value values[] = {
						Value.class.cast( set.getValue( "instance" ) ),
						Value.class.cast( set.getValue( "label" ) )
				};
				add( values );
			}
		};
		instancesQA.bind( "concept", concept );

		try {
			return engine.query( instancesQA );
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException e) {
			log.error("Could not query concepts: " + e, e);
			return null;
		}
	}
		
	private List<Value[]> runPropertiesQuery(URI instance) {
		String propertiesQuery
				= "SELECT DISTINCT ?predicate ?object WHERE {"
				+ "  ?subject ?predicate ?object . "
				+ "} ORDER BY ?predicate";

		ListQueryAdapter<Value[]> propertiesQA = new ListQueryAdapter<Value[]>( propertiesQuery ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				Value triple[] = {
						Value.class.cast( set.getValue( "predicate" ) ),
						Value.class.cast( set.getValue( "object" ) )
				};
				add( triple );
			}
		};
		propertiesQA.bind( "subject", instance );

		try {
			return engine.query( propertiesQA );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
			return null;
		}
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
				
				if ( path.getPathCount() == 1 ) {
					;//This shouldn't be possible because the root node is invisible.
				} else if ( path.getPathCount() == 2 ) {
					;//We have no action here.
				} else if ( path.getPathCount() == 3 ) {
					//DefaultMutableTreeNode conceptNode = DefaultMutableTreeNode.class.cast( path.getPathComponent( 1 ) );
					//URI concept = URI.class.cast( conceptNode.getUserObject() );
					DefaultMutableTreeNode dmtn = DefaultMutableTreeNode.class.cast( e.getPath().getLastPathComponent() );
					URI instance = URI.class.cast( dmtn.getUserObject() );
					
					List<Value[]> propertyList = runPropertiesQuery(instance);
					propertyTable.setModel( new InstancePropertyTableModel(propertyList, engine) );
				}
			}
		};
	}
	
	private TreeCellRenderer getTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 4433791433874526433L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

				DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode) value;
				if ((dmtNode.getUserObject() instanceof URI) && (dmtNode.getChildCount() > 0)) {
					Color color = DynamicColorRepository.instance().getColor( (URI) dmtNode.getUserObject() );
					Shape shape =   GraphShapeRepository.instance().getShape( (URI) dmtNode.getUserObject() );
	
					setIcon( PaintLabel.makeShapeIcon( color, shape, new Dimension( 12, 12 ) ) );
				} else {
					//setIcon( null );
				}
				
				return this;
			}
		};
	}
	
	private void populateDataForThisDB() {
		Preferences prefs = Preferences.userNodeForPackage( SemossPreferences.class );
		useLabels = prefs.getBoolean(Constants.SEMEX_USE_LABELS_PREF, false);

		invisibleRoot.removeAllChildren();
		
		ArrayList<URITreeNode> conceptListURITreeNodes = new ArrayList<URITreeNode>();
		List<URI> concepts = runConceptsQuery();
		for (URI concept:concepts) {
			URITreeNode conceptNode = new URITreeNode( concept, useLabels );
			conceptListURITreeNodes.add( conceptNode );

			ArrayList<URITreeNode> instanceListURITreeNodes = new ArrayList<URITreeNode>();
			List<Value[]> instancesAndTheirLabels = runInstancesAndLabelsQuery(concept);
			for (Value[] values:instancesAndTheirLabels) {
				instanceListURITreeNodes.add( new URITreeNode(values[0], values[1], useLabels) );
			}
			
			Collections.sort(instanceListURITreeNodes);
			for (URITreeNode instanceNode:instanceListURITreeNodes) {
				conceptNode.add(instanceNode);
			}
		}
		
		Collections.sort(conceptListURITreeNodes);
		for (URITreeNode conceptNode:conceptListURITreeNodes) {
			invisibleRoot.add(conceptNode);
		}

		nodeClassesAndInstances.setModel( new DefaultTreeModel( invisibleRoot ) );
		nodeClassesAndInstances.setRootVisible( false );
		nodeClassesAndInstances.repaint();
	}

	public void setEngine(IEngine engine) {
		this.engine = engine;
		populateDataForThisDB();
	}
}