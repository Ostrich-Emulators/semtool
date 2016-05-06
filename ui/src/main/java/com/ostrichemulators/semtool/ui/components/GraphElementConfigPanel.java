/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.GraphColorRepository;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.models.GraphElementTreeModel;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.PaintLabel;
import com.ostrichemulators.semtool.ui.components.renderers.ColorRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTreeCellRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.ShapeRenderer;
import com.ostrichemulators.semtool.ui.helpers.DynamicColorRepository;
import com.ostrichemulators.semtool.ui.helpers.GraphShapeRepository;
import com.ostrichemulators.semtool.ui.helpers.GraphShapeRepository.Shapes;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Shape;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class GraphElementConfigPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( GraphElementConfigPanel.class );
	private static final Dimension RESULT_DIM = new Dimension( 32, 32 );
	private final GraphElementTreeModel model;
	private final RetrievingLabelCache cache;
	private final DefaultListModel<Color> colormodel = new DefaultListModel<>();
	private final DefaultListModel<Shape> shapemodel = new DefaultListModel<>();
	private final GraphShapeRepository shapefactory = new GraphShapeRepository();

	/**
	 * Creates new form GraphElementConfigPanel
	 */
	public GraphElementConfigPanel( IEngine engine ) {
		cache = new RetrievingLabelCache( engine );
		model = new GraphElementTreeModel( engine );
		initComponents();

		colors.setCellRenderer( new ColorRenderer() );
		shapes.setCellRenderer( new ShapeRenderer( 30 ) );

		for ( Color c : GraphColorRepository.instance().getAllNamedColors() ) {
			colormodel.addElement( c );
		}

		for ( Shapes s : GraphShapeRepository.Shapes.values() ) {
			shapemodel.addElement( s.getShape( 24 ) );
		}

		LabeledPairTreeCellRenderer renderer = LabeledPairTreeCellRenderer.getValuePairRenderer( cache );
		renderer.cache( GraphElementTreeModel.FETCHING, "fetching..." );
		model.addTreeModelListener( new TreeModelListener() {

			@Override
			public void treeNodesChanged( TreeModelEvent e ) {
			}

			@Override
			public void treeNodesInserted( TreeModelEvent e ) {
				Set<URI> children = new HashSet<>();
				for ( Object o : e.getChildren() ) {
					Object u = DefaultMutableTreeNode.class.cast( o ).getUserObject();
					children.add( URI.class.cast( u ) );
				}

				// pre-fetch all the new labels
				renderer.cache( Utility.getInstanceLabels( children, engine ) );
			}

			@Override
			public void treeNodesRemoved( TreeModelEvent e ) {
			}

			@Override
			public void treeStructureChanged( TreeModelEvent e ) {
			}
		} );

		tree.setCellRenderer( renderer );
		tree.addTreeWillExpandListener( new TreeWillExpandListener() {

			@Override
			public void treeWillExpand( TreeExpansionEvent event ) throws ExpandVetoException {
				Object pc = event.getPath().getLastPathComponent();
				model.populateInstances( DefaultMutableTreeNode.class.cast( pc ) );
			}

			@Override
			public void treeWillCollapse( TreeExpansionEvent event ) throws ExpandVetoException {
				// nothing to do here
			}
		} );

		tree.addTreeSelectionListener( new TreeSelectionListener() {

			@Override
			public void valueChanged( TreeSelectionEvent e ) {
				TreePath path = e.getPath();
				setPanel( getUriFromPath( path.getParentPath() ), getUriFromPath( path ) );
			}
		} );

		ListSelectionListener lsl = new ListSelectionListener() {

			@Override
			public void valueChanged( ListSelectionEvent e ) {
				if ( !e.getValueIsAdjusting() ) {
					setNewPanel();
				}
			}
		};

		colors.addListSelectionListener( lsl );
		shapes.addListSelectionListener( lsl );
	}

	public static void showDialog( Frame frame, IEngine eng ) {
		String opts[] = { "Save", "Cancel" };
		GraphElementConfigPanel p = new GraphElementConfigPanel( eng );
		int ans = JOptionPane.showOptionDialog( frame, p,
				"Graph Display Configuration", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] );
		if ( JOptionPane.YES_OPTION == ans ) {
			// save stuff somewhere
		}
	}

	public Map<URI, Color> getColors() {
		Map<URI, Color> cols = new HashMap<>();
		return cols;
	}

	public Map<URI, Shape> getShape() {
		Map<URI, Shape> shape = new HashMap<>();
		return shape;
	}

	public Map<URI, String> getIcon() {
		Map<URI, String> icons = new HashMap<>();
		return icons;
	}

	private void setPanel( URI type, URI instance ) {
		URI me = instance;

		title.setText( String.format( cache.get( me ) ) );
		uri.setText( me.stringValue() );

		shapes.setSelectedValue( shapefactory.getShape( type, instance ), true );
		colors.setSelectedValue( DynamicColorRepository.instance().getColor( null == type
				? me : type ), true );

		was.setIcon( PaintLabel.makeShapeIcon( colors.getSelectedValue(),
				shapes.getSelectedValue(), RESULT_DIM ) );
	}

	private void setNewPanel() {
		is.setIcon( PaintLabel.makeShapeIcon( colors.getSelectedValue(),
				shapes.getSelectedValue(), RESULT_DIM ) );
	}

	private static URI getUriFromPath( TreePath tp ) {
		DefaultMutableTreeNode node = DefaultMutableTreeNode.class.cast( tp.getLastPathComponent() );
		URI uri = URI.class.cast( node.getUserObject() );
		return uri;
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
    Panel1 = new javax.swing.JPanel();
    title = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    shapes = new javax.swing.JList<Shape>();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    colors = new javax.swing.JList<Color>();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    was = new javax.swing.JLabel();
    is = new javax.swing.JLabel();
    uri = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    tree.setModel(model);
    jScrollPane1.setViewportView(tree);

    jSplitPane1.setLeftComponent(jScrollPane1);

    title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    title.setText(" ");

    jLabel1.setText("Shape");

    shapes.setModel(shapemodel);
    shapes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    shapes.setToolTipText("");
    jScrollPane2.setViewportView(shapes);

    jLabel2.setText("Color");

    colors.setModel(colormodel);
    colors.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane3.setViewportView(colors);

    jLabel3.setText("Original");

    jLabel4.setText("New");

    was.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    was.setText(" ");
    was.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    is.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    is.setText(" ");
    is.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    uri.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    uri.setText(" ");

    javax.swing.GroupLayout Panel1Layout = new javax.swing.GroupLayout(Panel1);
    Panel1.setLayout(Panel1Layout);
    Panel1Layout.setHorizontalGroup(
      Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(Panel1Layout.createSequentialGroup()
        .addGap(24, 24, 24)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(Panel1Layout.createSequentialGroup()
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(Panel1Layout.createSequentialGroup()
            .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(Panel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(151, 151, 151)
                .addComponent(jLabel2))
              .addGroup(Panel1Layout.createSequentialGroup()
                .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(was, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jLabel4)
                  .addComponent(is, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGap(0, 119, Short.MAX_VALUE)))
        .addGap(122, 122, 122))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(uri, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );
    Panel1Layout.setVerticalGroup(
      Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(Panel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(title)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(uri)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(jLabel2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        .addGap(18, 18, 18)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(jLabel4))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(was, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(is, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(177, 177, 177))
    );

    jSplitPane1.setRightComponent(Panel1);

    add(jSplitPane1, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel Panel1;
  private javax.swing.JList<Color> colors;
  private javax.swing.JLabel is;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JList<Shape> shapes;
  private javax.swing.JLabel title;
  private javax.swing.JTree tree;
  private javax.swing.JLabel uri;
  private javax.swing.JLabel was;
  // End of variables declaration//GEN-END:variables
}
