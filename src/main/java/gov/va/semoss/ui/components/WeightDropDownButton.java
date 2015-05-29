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

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.EdgeStrokeTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
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

	private JPopupMenu popupMenu;
	private JTree edgePropTree, nodePropTree;
	private boolean listsPopulated = false;
	private GraphPlaySheet playSheet;
	private static Map<String,URI> localNameToURIHash = new HashMap<String,URI>();

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

		initMenus(nodePropTree, 2, playSheet.getFilterData().getNodeTypeMap());
		initMenus(edgePropTree, 1, playSheet.getFilterData().getEdgeTypeMap());

		popupMenu.pack();
		popupMenu.revalidate();
		popupMenu.repaint();
		
		listsPopulated = true;
	}

	private <X extends AbstractNodeEdgeBase> void initMenus(JTree tree, int selectNum, Map<URI, List<X>> nodesOrEdgesMapByType) {
		tree.addTreeSelectionListener( getTreeSelectionListener( selectNum ) );
		DefaultMutableTreeNode invisibleRoot = new DefaultMutableTreeNode( "not visible" );
		tree.setModel( new DefaultTreeModel( invisibleRoot ) );
		
		Map<String, Set<String>> propertiesToAdd = buildPropertyDataset(nodesOrEdgesMapByType);
		addPropertiesToTreeNode( propertiesToAdd, invisibleRoot );
		
		tree.expandRow( 0 );
		tree.setRootVisible( false );
	}
	
	/**
	 * Method buildPropertyDataset. Create a dataset of node types and their properties to later add to the JTree.
	 * We use this intermediary data structure because it gives us uniqueness and order for the elements. 
	 * 
	 * @param Map<URI, List<X>> nodesOrEdgesMapByType - the map of nodes or edges keyed by type
	 * @return Map<String, Set<String>> maps of the types of the nodes or edges to the names of their numerical properties
	 */
	private <X extends AbstractNodeEdgeBase> Map<String, Set<String>> buildPropertyDataset(Map<URI, List<X>> nodesOrEdgesMapByType) {
		Map<String, Set<String>> propertiesToAdd = new HashMap<>();
		for (Map.Entry<URI, List<X>> entry : nodesOrEdgesMapByType.entrySet()) {
			if (entry.getValue().size() < 2) {
				//we don't want to list items that are the only one of their type
				continue;
			}
			
			Set<String> propertiesForThisType = new TreeSet<String>();
			propertiesToAdd.put( entry.getKey().getLocalName(), propertiesForThisType );
			for ( X nodeOrEdge : entry.getValue() ) {
				for ( Map.Entry<URI, Object> propEntry : nodeOrEdge.getProperties().entrySet() ) {
					if ( getDoubleIfPossibleFrom(propEntry.getValue()) > 0 ) {
						propertiesForThisType.add( propEntry.getKey().getLocalName() );
						localNameToURIHash.put(propEntry.getKey().getLocalName(), propEntry.getKey());
					}
				}
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

		popupMenu = new JPopupMenu();
		popupMenu.setLayout( new GridBagLayout() );

		popupMenu.add( new JLabel( "Node Properties" ), getGridBagContraints() );
		popupMenu.add( createJScrollPane( nodePropTree ), getGridBagContraints() );

		popupMenu.add( new JLabel( "Edge Properties" ), getGridBagContraints() );
		popupMenu.add( createJScrollPane( edgePropTree ), getGridBagContraints() );

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

	private JScrollPane createJScrollPane( JComponent contents ) {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView( contents );
		scrollPane.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		scrollPane.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );

		return scrollPane;
	}

	int currentY = 0;

	private GridBagConstraints getGridBagContraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = currentY++;
		c.insets = new Insets( 5, 5, 0, 5 );
		return c;
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
		vst.setVertexSizeHash( getWeightHash( playSheet.getForest().getVertices(),
				selectedValue, vst.getDefaultScale() ) );

		playSheet.getView().repaint();
	}

	private void rescaleEdges( String selectedValue ) {
		EdgeStrokeTransformer est = (EdgeStrokeTransformer) playSheet.getView().getRenderContext().getEdgeStrokeTransformer();
		est.setEdges( getWeightHash(playSheet.getForest().getEdges(), selectedValue, 1.0) );

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
	@SuppressWarnings("unchecked")
	public static <X extends AbstractNodeEdgeBase> Map<X, Double>
			getWeightHash( Collection<X> collection, String selectedValue,
					double defaultScale ) {

		double minimumValue = .5, multiplier = 3;

		if ( checkForUnselectionEvent(selectedValue) ) {
			return new HashMap<>();
		}

		Double highValue = null, lowValue = null;
		Map<X, Double> weightHash = new HashMap<>();

		for ( AbstractNodeEdgeBase nodeOrEdge : collection ) {
			Object propertyValue = null;

			URI selectedURI = localNameToURIHash.get(selectedValue);
			propertyValue = nodeOrEdge.getProperty( selectedURI );
			double propertyDouble = getDoubleIfPossibleFrom(propertyValue);

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
			return new Hashtable<>();
		}

		for ( X key : weightHash.keySet() ) {
			double value = ( ( weightHash.get(key) - lowValue ) / ( highValue - lowValue ) ) * multiplier * defaultScale + minimumValue;
			weightHash.put( key, value );
		}

		return weightHash;
	}

	private static String lastSelectedValue;
	private static boolean checkForUnselectionEvent(String selectedValue) {
		if (selectedValue == null) {
			//i don't think this should happen, but just in case
			lastSelectedValue = null;
			return true;
		}

		if (selectedValue.equals(lastSelectedValue)) {
			lastSelectedValue = null;
			return true;
		}

		lastSelectedValue = selectedValue;
		return false;
	}

	private static double getDoubleIfPossibleFrom(Object propertyValue) {
		if (propertyValue == null)
			return -1;
		
		if (propertyValue instanceof URI) {
			URI uri = (URI) propertyValue;
			try {
				return Double.parseDouble(uri.getLocalName());
			}
			catch (NumberFormatException e) {
				return -1;
			}
		}
		
		try {
			return Double.parseDouble(propertyValue.toString());
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}
}
