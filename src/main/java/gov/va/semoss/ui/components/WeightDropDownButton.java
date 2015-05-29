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
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
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

		listsPopulated = true;
		populateLists();
	}

	private void populateLists() {
		nodePropTree.addTreeSelectionListener( getTreeSelectionListener( 2 ) );
		edgePropTree.addTreeSelectionListener( getTreeSelectionListener( 1 ) );

		DefaultMutableTreeNode invisibleNodeRoot = new DefaultMutableTreeNode( "not visible" );
		DefaultMutableTreeNode invisibleEdgeRoot = new DefaultMutableTreeNode( "not visible" );

		nodePropTree.setModel( new DefaultTreeModel( invisibleNodeRoot ) );
		edgePropTree.setModel( new DefaultTreeModel( invisibleEdgeRoot ) );
		
		//Create a dataset of node types and their properties to the JTree
		//we use this intermediary data structure because it gives us uniqueness and order for the elements
		Map<String, Set<String>> nodePropertiesToAdd = new HashMap<>();
		Collection<SEMOSSVertex> nodeCollection = playSheet.getForest().getVertices();
		for ( SEMOSSVertex node : nodeCollection ) {
			Set<String> propertiesForThisNodeType = nodePropertiesToAdd.get( node.getType().getLocalName() );
			if ( propertiesForThisNodeType == null ) {
				propertiesForThisNodeType = new TreeSet<String>();
				nodePropertiesToAdd.put( node.getType().getLocalName(), propertiesForThisNodeType );
			}
			for ( Map.Entry<URI, Object> entry : node.getProperties().entrySet() ) {
				if ( getDoubleIfPossibleFrom(entry.getValue()) > 0 ) {
					propertiesForThisNodeType.add( entry.getKey().getLocalName() );
					localNameToURIHash.put(entry.getKey().getLocalName(), entry.getKey());
				}
			}
		}

		//Create a dataset of edge types and their properties to the JTree
		//we use this intermediary data structure because it gives us uniqueness and order for the elements
		Map<String, Set<String>> edgePropertiesToAdd = new HashMap<>();
		Collection<SEMOSSEdge> edgeCollection = playSheet.getForest().getEdges();
		for ( SEMOSSEdge edge : edgeCollection ) {
			Set<String> propertiesForThisEdgeType = edgePropertiesToAdd.get( edge.getEdgeType() );
			if ( propertiesForThisEdgeType == null ) {
				propertiesForThisEdgeType = new TreeSet<String>();
				edgePropertiesToAdd.put( edge.getEdgeType().getLocalName(), propertiesForThisEdgeType );
			}
			for ( Map.Entry<URI, Object> entry : edge.getProperties().entrySet() ) {
				if ( getDoubleIfPossibleFrom(entry.getValue()) > 0 ) {
					propertiesForThisEdgeType.add( entry.getKey().getLocalName() );
					localNameToURIHash.put(entry.getKey().getLocalName(), entry.getKey());
				}
			}
		}

		addPropertiesToTreeNode( nodePropertiesToAdd, invisibleNodeRoot );
		addPropertiesToTreeNode( edgePropertiesToAdd, invisibleEdgeRoot );

		nodePropTree.expandRow( 0 );
		edgePropTree.expandRow( 0 );

		nodePropTree.setRootVisible( false );
		edgePropTree.setRootVisible( false );

		popupMenu.pack();
		popupMenu.revalidate();
		popupMenu.repaint();
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
