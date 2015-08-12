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
package gov.va.semoss.ui.components;

import gov.va.semoss.om.GraphElement;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.NodeEdgeNumberedPropertyUtility;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openrdf.model.URI;

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
			Map<URI, List<X>> nodesOrEdgesMapByType ) {
		tree.addTreeSelectionListener( getTreeSelectionListener( selectNum ) );
		DefaultMutableTreeNode invisibleRoot = new DefaultMutableTreeNode( "not visible" );
		tree.setModel( new DefaultTreeModel( invisibleRoot ) );

		Map<String, Set<String>> propertiesToAdd = buildPropertyDataset( nodesOrEdgesMapByType );
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
	private <X extends GraphElement> Map<String, Set<String>> buildPropertyDataset( Map<URI, List<X>> nodesOrEdgesMapByType ) {
		Map<String, Set<String>> propertiesToAdd = new HashMap<>();
		for ( Map.Entry<URI, List<X>> entry : nodesOrEdgesMapByType.entrySet() ) {
			if ( entry.getValue().size() < 2 ) {
				//we don't want to list items that are the only one of their type
				continue;
			}

			Set<String> propertiesForThisType = new TreeSet<>();
			propertiesToAdd.put( entry.getKey().getLocalName(), propertiesForThisType );
			for ( X nodeOrEdge : entry.getValue() ) {
				Map<String, Object> props = NodeEdgeNumberedPropertyUtility.transformProperties( nodeOrEdge.getProperties(), false );
				propertiesForThisType.addAll( props.keySet() );
			}
		}

		return propertiesToAdd;
	}

	private void addPropertiesToTreeNode( Map<String, Set<String>> propertiesToAdd,
			DefaultMutableTreeNode invisibleNodeRoot ) {
		for ( String key : propertiesToAdd.keySet() ) {
			Set<String> propertiesForThisNodeType = propertiesToAdd.get( key );
			if ( propertiesForThisNodeType.isEmpty() ) {
				continue;
			}

			DefaultMutableTreeNode nodeType = new DefaultMutableTreeNode( key );
			invisibleNodeRoot.add( nodeType );
			for ( String property : propertiesForThisNodeType ) {
				nodeType.add( new DefaultMutableTreeNode( property ) );
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

		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setClosedIcon( null );
		renderer.setOpenIcon( null );
		renderer.setLeafIcon( null );

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
			int edgeMode = 1, vertMode = 2;
			int thisMode = mode;

			@Override
			public void valueChanged( TreeSelectionEvent e ) {
				if ( e.getPath().getPathCount() == 2 ) {
					//this is not a property, this is a node type
					return;
				}

				String tselectedValue = e.getPath().getLastPathComponent().toString();

				if ( thisMode == edgeMode ) {
					rescaleEdges( tselectedValue );
				}
				else if ( thisMode == vertMode ) {
					rescaleVertices( tselectedValue );
				}
			}
		};
	}

	private void rescaleVertices( String selectedValue ) {
		VertexShapeTransformer vst
				= (VertexShapeTransformer) playSheet.getView().getRenderContext().getVertexShapeTransformer();
		vst.setSizeMap( getWeightHash( playSheet.getGraphData().getGraph().getVertices(),
				selectedValue, vst.getDefaultSize() ) );

		playSheet.getView().repaint();
	}

	private void rescaleEdges( String selectedValue ) {
		EdgeStrokeTransformer est = (EdgeStrokeTransformer) playSheet.getView().getRenderContext().getEdgeStrokeTransformer();
		est.setEdges( getWeightHash( playSheet.getGraphData().getGraph().getEdges(),
				selectedValue, 1.0 ) );

		playSheet.getView().repaint();
	}

	/**
	 * Method getNodeWeightHash. Builds up the hash of the nodes and weights of
	 * the selected item
	 *
	 * @param JList<String> list - the list of items which may or may not be
	 * selected
	 * @return Hashtable<String, Double> of the nodes and weights
	 */
	@SuppressWarnings( "unchecked" )
	public static <X extends GraphElement> Map<X, Double>
			getWeightHash( Collection<X> collection, String selectedValue,
					double defaultScale ) {

		double minimumValue = .5, multiplier = 3;

		if ( checkForUnselectionEvent( selectedValue ) ) {
			return new HashMap<>();
		}

		Double highValue = null, lowValue = null;
		Map<X, Double> weightHash = new HashMap<>();

		for ( GraphElement nodeOrEdge : collection ) {
			Object propertyValue = null;

			URI selectedURI = NodeEdgeNumberedPropertyUtility.getURI( selectedValue );
			propertyValue = nodeOrEdge.getProperty( selectedURI );
			double propertyDouble = NodeEdgeNumberedPropertyUtility.getDoubleIfPossibleFrom( propertyValue );

			if ( propertyDouble > 0 ) {
				double value = Double.parseDouble( propertyValue.toString() );
				if ( highValue == null ) {
					highValue = value;
				}
				if ( lowValue == null ) {
					lowValue = value;
				}
				if ( value > highValue ) {
					highValue = value;
				}
				if ( value < lowValue ) {
					lowValue = value;
				}

				weightHash.put( (X) nodeOrEdge, value );
			}
		}

		if ( highValue == null || highValue.equals( lowValue ) ) {
			//we have no resize data
			return new HashMap<>();
		}

		for ( X key : weightHash.keySet() ) {
			double value = ( ( weightHash.get( key ) - lowValue ) / ( highValue - lowValue ) ) * multiplier * defaultScale + minimumValue;
			weightHash.put( key, value );
		}

		return weightHash;
	}

	private static String lastSelectedValue;

	private static boolean checkForUnselectionEvent( String selectedValue ) {
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
