/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTreeCellRenderer;
import com.ostrichemulators.semtool.ui.helpers.NodeEdgeNumberedPropertyUtility;
import com.ostrichemulators.semtool.ui.transformer.EdgeStrokeTransformer;
import com.ostrichemulators.semtool.ui.transformer.VertexShapeTransformer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.MultiSetMap;
import com.ostrichemulators.semtool.util.RDFDatatypeTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * This class is used to create the button that allows the weight to be
 * adjusted.
 */
public class WeightDropDownButton extends JButton {

	private static final long serialVersionUID = 2981760820784735327L;

	private final static int scrollPaneWidth = 180;
	private final static int scrollPaneHeightMinimum = 35;
	private final static int scrollPaneHeightMaximum = 300;

	private JPopupMenu popupMenu;
	private JTree edgePropTree, nodePropTree;
	private JScrollPane nodeScrollPane, edgeScrollPane;
	private boolean listsPopulated = false;
	private GraphPlaySheet playSheet;
	private LabeledPairTreeCellRenderer renderer;
	private URI lastSelectedValue;

	public WeightDropDownButton( ImageIcon icon ) {
		setIcon( icon );
		initializeButton();
	}

	public void setPlaySheet( GraphPlaySheet gps ) {
		playSheet = gps;
	}

	public void showPopup() {
		if ( listsPopulated ) {
			return;
		}

		renderer = LabeledPairTreeCellRenderer.getValuePairRenderer( playSheet.getEngine() );
		renderer.setClosedIcon( null );
		renderer.setOpenIcon( null );
		renderer.setLeafIcon( null );
		
		Map<URI, String> displayNames = NodeEdgeNumberedPropertyUtility.getDisplayNameMap();
		for (URI key:displayNames.keySet()) {
			renderer.cache( key, displayNames.get(key) );
		}
		
		edgePropTree.setCellRenderer( renderer );
		nodePropTree.setCellRenderer( renderer );

		initMenus( nodePropTree, 2, playSheet.getVerticesByType() );
		initMenus( edgePropTree, 1, playSheet.getEdgesByType() );

		setScrollPaneSize( nodeScrollPane, nodePropTree );
		setScrollPaneSize( edgeScrollPane, edgePropTree );

		popupMenu.pack();
		popupMenu.repaint();

		listsPopulated = true;
	}

	private void setScrollPaneSize( JScrollPane scrollPane, JTree tree ) {
		int scrollPaneHeight = 10;
		for ( int i = 0; i < tree.getRowCount(); i++ ) {
			scrollPaneHeight += tree.getRowBounds( i ).getHeight();
		}

		Dimension preferredSize = new Dimension( scrollPaneWidth, scrollPaneHeight );
		Dimension minimumSize = new Dimension( scrollPaneWidth, scrollPaneHeightMinimum );
		Dimension maximumSize = new Dimension( scrollPaneWidth, scrollPaneHeightMaximum );

		if ( preferredSize.getHeight() < minimumSize.getHeight() ) {
			preferredSize = minimumSize;
		}

		if ( preferredSize.getHeight() > maximumSize.getHeight() ) {
			preferredSize = maximumSize;
		}

		scrollPane.setPreferredSize( preferredSize );
		scrollPane.setMinimumSize( minimumSize );
		scrollPane.setMaximumSize( maximumSize );
	}

	private <X extends GraphElement> void initMenus( JTree tree, int selectNum,
			MultiMap<URI, X> nodesOrEdgesMapByType ) {
		tree.addTreeSelectionListener( getTreeSelectionListener( selectNum ) );
		DefaultMutableTreeNode invisibleRoot = new DefaultMutableTreeNode( "not visible" );
		tree.setModel( new DefaultTreeModel( invisibleRoot ) );

		MultiSetMap<URI, URI> propertiesToAdd = buildPropertyDataset( nodesOrEdgesMapByType );
		addPropertiesToTreeNode( propertiesToAdd, invisibleRoot );

		for ( int i = 0; i < tree.getRowCount(); i++ ) {
			tree.expandRow( i );
		}
		tree.setRootVisible( false );
	}

	/**
	 * Method buildPropertyDataset. Create a dataset of node types and their
	 * properties to later add to the JTree. We use this intermediary data
	 * structure because it gives us uniqueness and order for the elements.
	 *
	 * @param Map<URI, List<X>> nodesOrEdgesMapByType - the map of nodes or edges
	 * keyed by type
	 * @return Map<String, Set<String>> maps of the types of the nodes or edges to
	 * the names of their numerical properties
	 */
	private <X extends GraphElement> MultiSetMap<URI, URI> buildPropertyDataset( Map<URI, List<X>> nodesOrEdgesMapByType ) {
		MultiSetMap<URI, URI> propertiesToAdd = new MultiSetMap<>();
		for ( Map.Entry<URI, List<X>> entry : nodesOrEdgesMapByType.entrySet() ) {
			if ( entry.getValue().size() < 2 ) {
				//we don't want to list items that are the only one of their type
				continue;
			}

			for ( X nodeOrEdge : entry.getValue() ) {
				propertiesToAdd.addAll( entry.getKey(), getNumberedProperties( nodeOrEdge ) );

				if ( nodeOrEdge.isNode() ) {
					// nodes always have degree properties
					propertiesToAdd.add( entry.getKey(), Constants.IN_EDGE_CNT );
					propertiesToAdd.add( entry.getKey(), Constants.OUT_EDGE_CNT );
					propertiesToAdd.add( entry.getKey(), Constants.EDGE_CNT );
				}
			}
		}

		return propertiesToAdd;
	}

	/**
	 * Gets this element's properties that contain literals that are numbers
	 *
	 * @param ge
	 * @return
	 */
	private static Set<URI> getNumberedProperties( GraphElement ge ) {
		Set<URI> numbers = new HashSet<>();
		for ( Map.Entry<URI, Value> en : ge.getValues().entrySet() ) {
			Value v = en.getValue();
			if ( RDFDatatypeTools.isNumericValue( v ) ) {
				numbers.add( en.getKey() );
			}
		}

		return numbers;
	}

	private void addPropertiesToTreeNode( MultiSetMap<URI, URI> propertiesToAdd,
			DefaultMutableTreeNode invisibleNodeRoot ) {
		for ( Map.Entry<URI, Set<URI>> en : propertiesToAdd.entrySet() ) {
			Set<URI> propertiesForThisNodeType = en.getValue();
			if ( !propertiesForThisNodeType.isEmpty() ) {
				DefaultMutableTreeNode nodeType = new DefaultMutableTreeNode( en.getKey() );
				invisibleNodeRoot.add( nodeType );
				for ( URI property : propertiesForThisNodeType ) {
					nodeType.add( new DefaultMutableTreeNode( property ) );
				}
			}
		}
	}

	private void initializeButton() {
		nodePropTree = initJTree();
		edgePropTree = initJTree();

		nodeScrollPane = new JScrollPane( nodePropTree );
		edgeScrollPane = new JScrollPane( edgePropTree );

		JPanel nodePanel = new JPanel( new BorderLayout() );
		nodePanel.add( new JLabel( "  Node Properties" ), BorderLayout.NORTH );
		nodePanel.add( nodeScrollPane, BorderLayout.SOUTH );

		JPanel edgePanel = new JPanel( new BorderLayout() );
		edgePanel.add( new JLabel( "  Edge Properties" ), BorderLayout.NORTH );
		edgePanel.add( edgeScrollPane, BorderLayout.SOUTH );

		popupMenu = new JPopupMenu();
		popupMenu.setLayout( new BorderLayout() );
		popupMenu.add( nodePanel, BorderLayout.NORTH );
		popupMenu.add( edgePanel, BorderLayout.SOUTH );

		initializeButtonListeners();
	}

	private JTree initJTree() {
		JTree tree = new JTree();
		tree.setSelectionModel( getDeselectableTreeSelectionModel() );

		return tree;
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

	private void initializeButtonListeners() {
		final JButton button = this;

		this.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				popupMenu.show( button, 0,
						( button.getPreferredSize() ).height / 2 );
				button.setEnabled( false );
			}
		} );

		popupMenu.addPopupMenuListener( new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeInvisible( PopupMenuEvent e ) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						button.setEnabled( true );
					}
				} );
			}

			@Override
			public void popupMenuWillBecomeVisible( PopupMenuEvent e ) {
			}

			@Override
			public void popupMenuCanceled( PopupMenuEvent e ) {
			}
		} );
	}

	private TreeSelectionListener getTreeSelectionListener( int mode ) {
		return new TreeSelectionListener() {
			private static final int edgeMode = 1;
			private static final int vertMode = 2;
			private final int thisMode = mode;

			@Override
			public void valueChanged( TreeSelectionEvent e ) {
				if ( e.getPath().getPathCount() == 2 ) {
					//this is not a property, this is a node type
					return;
				}

				TreePath path = e.getPath();
				DefaultMutableTreeNode typenode
						= DefaultMutableTreeNode.class.cast( path.getPathComponent( 1 ) );
				URI type = URI.class.cast( typenode.getUserObject() );

				DefaultMutableTreeNode dmtn
						= DefaultMutableTreeNode.class.cast( e.getPath().getLastPathComponent() );
				URI prop = URI.class.cast( dmtn.getUserObject() );

				if ( thisMode == edgeMode ) {
					rescaleEdges( type, prop );
				}
				else if ( thisMode == vertMode ) {
					rescaleVertices( type, prop );
				}
			}
		};
	}

	private void rescaleVertices( URI type, URI prop ) {
		VertexShapeTransformer vst
				= (VertexShapeTransformer) playSheet.getView().getRenderContext().getVertexShapeTransformer();
		vst.setSizeMap( getWeightHash( playSheet.getVerticesByType().getNN( type ),
				prop, vst.getDefaultSize() ) );
		playSheet.getView().repaint();
	}

	private void rescaleEdges( URI type, URI prop ) {
		EdgeStrokeTransformer est
				= (EdgeStrokeTransformer) playSheet.getView().getRenderContext().getEdgeStrokeTransformer();
		est.setEdges( getWeightHash( playSheet.getEdgesByType().getNN( type ),
				prop, 1.0 ) );
		playSheet.getView().repaint();
	}

	/**
	 * Method getNodeWeightHash. Builds up the hash of the nodes and weights of
	 * the selected item
	 *
	 * @param Collection<X> collection - the list of items which may or may not be
	 * selected
	 * @param URI selectedURI - the URI which is currently selected
	 * @return <X extends GraphElement> Map<X, Double> of the nodes and weights
	 */
	@SuppressWarnings( "unchecked" )
	public <X extends GraphElement> Map<X, Double>
			getWeightHash( Collection<X> collection, URI selectedURI,
					double defaultScale ) {

		double minimumValue = .5, multiplier = 3;

		if ( isUnselectionEvent( selectedURI ) ) {
			return new HashMap<>();
		}

		Double highValue = null, lowValue = null;
		Map<X, Double> weightHash = new HashMap<>();

		for ( GraphElement nodeOrEdge : collection ) {
			Value propertyValue = null;

			propertyValue = nodeOrEdge.getValue( selectedURI );
			double propertyDouble;

			if ( Constants.IN_EDGE_CNT.equals( selectedURI ) ) {
				propertyDouble = playSheet.getGraphData().getGraph().
						getInEdges( SEMOSSVertex.class.cast( nodeOrEdge ) ).size();
			}
			else if ( Constants.OUT_EDGE_CNT.equals( selectedURI ) ) {
				propertyDouble = playSheet.getGraphData().getGraph().
						getOutEdges( SEMOSSVertex.class.cast( nodeOrEdge ) ).size();
			}
			else if ( Constants.EDGE_CNT.equals( selectedURI ) ) {
				propertyDouble = playSheet.getGraphData().getGraph().
						getIncidentEdges( SEMOSSVertex.class.cast( nodeOrEdge ) ).size();
			}
			else {
				propertyDouble
						= NodeEdgeNumberedPropertyUtility.getDoubleIfPossibleFrom( propertyValue );
			}

			if ( propertyDouble >= 0 ) {
				if ( highValue == null ) {
					highValue = propertyDouble;
				}
				if ( lowValue == null ) {
					lowValue = propertyDouble;
				}
				if ( propertyDouble > highValue ) {
					highValue = propertyDouble;
				}
				if ( propertyDouble < lowValue ) {
					lowValue = propertyDouble;
				}

				weightHash.put( (X) nodeOrEdge, propertyDouble );
			}
		}

		if ( highValue == null || highValue.equals( lowValue ) ) {
			//we have no resize data
			return new HashMap<>();
		}

		for ( X key : weightHash.keySet() ) {
			double rawval = ( weightHash.get( key ) - lowValue ) / ( highValue - lowValue );
			weightHash.put( key, rawval * multiplier * defaultScale + minimumValue );
		}

		return weightHash;
	}

	private boolean isUnselectionEvent( URI selectedValue ) {
		if ( selectedValue == null ) {
			//i don't think this should happen, but just in case
			lastSelectedValue = null;
			return true;
		}

		if ( selectedValue.equals( lastSelectedValue ) ) {
			lastSelectedValue = null;
			return true;
		}

		lastSelectedValue = selectedValue;
		return false;
	}
}
