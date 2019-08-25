/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.NamedShape;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.models.GraphElementTreeModel;
import com.ostrichemulators.semtool.ui.components.renderers.ColorRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairTreeCellRenderer;
import com.ostrichemulators.semtool.ui.components.renderers.ShapeRenderer;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.IconBuilder;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Shape;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
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
import org.eclipse.rdf4j.model.IRI;

/**
 *
 * @author ryan
 */
public class GraphElementConfigPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( GraphElementConfigPanel.class );
	private final GraphElementTreeModel model;
	private final RetrievingLabelCache cache;
	private final DefaultListModel<Color> colormodel = new DefaultListModel<>();
	private final DefaultListModel<NamedShape> shapemodel = new DefaultListModel<>();
	private final DefaultColorShapeRepository shapefactory = new DefaultColorShapeRepository();
	private final DefaultComboBoxModel<IRI> dbmodel = new DefaultComboBoxModel<>();

	/**
	 * Creates new form GraphElementConfigPanel
	 *
	 * @param engine
	 * @param repo
	 */
	public GraphElementConfigPanel( IEngine engine, GraphColorShapeRepository repo ) {
		shapefactory.importFrom( repo );
		cache = new RetrievingLabelCache( engine );
		model = new GraphElementTreeModel( engine );
		initComponents();

		LabeledPairRenderer<IRI> dbrenderer
				= LabeledPairRenderer.getUriPairRenderer( engine );
		dbrenderer.cache( Constants.ANYNODE, "Default" );
		dbmodel.addElement( Constants.ANYNODE );
		Set<IRI> dbs = DIHelper.getInstance().getMetadataStore().getDatabases();

		// per-database graph settings don't work yet, so don't give a user the option
//		for ( IRI u : dbs ) {
//			dbmodel.addElement( u );
//		}
		for ( IEngine eng : DIHelper.getInstance().getEngineMap().values() ) {
			dbrenderer.cache( eng.getBaseIri(), eng.getEngineName() );
			if ( !dbs.contains( eng.getBaseIri() ) ) {
				//dbmodel.addElement( eng.getBaseIri() );
			}
		}

		dbchsr.setRenderer( dbrenderer );

		colors.setCellRenderer( new ColorRenderer() );
		shapes.setCellRenderer( new ShapeRenderer( 24 ) );

		for ( Color c : DefaultColorShapeRepository.COLORS ) {
			colormodel.addElement( c );
		}

		for ( NamedShape s : NamedShape.values() ) {
			shapemodel.addElement( s );
		}

		LabeledPairTreeCellRenderer renderer = LabeledPairTreeCellRenderer.getValuePairRenderer( cache );
		renderer.cache( GraphElementTreeModel.FETCHING, "fetching..." );
		model.addTreeModelListener( new TreeModelListener() {

			@Override
			public void treeNodesChanged( TreeModelEvent e ) {
			}

			@Override
			public void treeNodesInserted( TreeModelEvent e ) {
				Set<IRI> children = new HashSet<>();
				for ( Object o : e.getChildren() ) {
					Object u = DefaultMutableTreeNode.class.cast( o ).getUserObject();
					children.add( IRI.class.cast( u ) );
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

	public DefaultColorShapeRepository getRepository() {
		return shapefactory;
	}

	public static void showDialog( Frame frame, IEngine eng, GraphColorShapeRepository repo ) {
		String opts[] = { "Save", "Cancel" };
		GraphElementConfigPanel p = new GraphElementConfigPanel( eng, repo );
		int ans = JOptionPane.showOptionDialog( frame, p,
				"Graph Display Configuration", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] );
		if ( JOptionPane.YES_OPTION == ans ) {
			DIHelper.getInstance().getMetadataStore().set( p.getDb(), p.getRepository() );
			repo.importFrom( p.getRepository() );
		}
	}

	public IRI getDb() {
		IRI IRI = dbchsr.getItemAt( dbchsr.getSelectedIndex() );
		return ( Constants.ANYNODE.equals( IRI ) ? null : IRI );
	}

	public Map<IRI, Color> getColors() {
		Map<IRI, Color> cols = new HashMap<>();
		return cols;
	}

	public Map<IRI, Shape> getShape() {
		Map<IRI, Shape> shape = new HashMap<>();
		return shape;
	}

	public Map<IRI, String> getIcon() {
		Map<IRI, String> icons = new HashMap<>();
		return icons;
	}

	private void setPanel( IRI type, IRI instance ) {
		IRI me = instance;

		title.setText( String.format( cache.get( me ) ) );
		uri.setText( me.stringValue() );

		if ( null == type ) {
			// the user clicked on a type, not an instance, so switch the two
			// (if we only have one value, treat it as the type, not instance)
			type = instance;
			instance = null;
		}

		NamedShape shape = shapefactory.getShape( type, instance );
		Color color = shapefactory.getColor( type, instance );

		shapes.setSelectedValue( shape, true );
		colors.setSelectedValue( color, true );

		was.setIcon( new IconBuilder( shapes.getSelectedValue(), colors.getSelectedValue() )
				.setPadding( 2 ).setIconSize( 40 ).build() );
	}

	private void setNewPanel() {
		if ( null != tree.getSelectionPath() ) {
			IRI me = getUriFromPath( tree.getSelectionPath() );
			shapefactory.set( me, colors.getSelectedValue(), shapes.getSelectedValue() );
		}

		is.setText( null );
		is.setIcon( new IconBuilder( shapes.getSelectedValue(), colors.getSelectedValue() )
				.setPadding( 2 ).setIconSize( 40 ).build() );
	}

	private static IRI getUriFromPath( TreePath tp ) {
		DefaultMutableTreeNode node = DefaultMutableTreeNode.class.cast( tp.getLastPathComponent() );
		IRI uri = IRI.class.cast( node.getUserObject() );
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
    shapes = new javax.swing.JList<>();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    colors = new javax.swing.JList<>();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    was = new javax.swing.JLabel();
    is = new javax.swing.JLabel();
    uri = new javax.swing.JLabel();
    dbchsr = new javax.swing.JComboBox<>();
    jLabel5 = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();

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
    shapes.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
    shapes.setVisibleRowCount(-1);
    jScrollPane2.setViewportView(shapes);

    jLabel2.setText("Color");

    colors.setModel(colormodel);
    colors.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    colors.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
    colors.setVisibleRowCount(-1);
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

    dbchsr.setModel(dbmodel);

    jLabel5.setText("For Database");

    jButton1.setText("Remove Settings");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout Panel1Layout = new javax.swing.GroupLayout(Panel1);
    Panel1.setLayout(Panel1Layout);
    Panel1Layout.setHorizontalGroup(
      Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(Panel1Layout.createSequentialGroup()
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(Panel1Layout.createSequentialGroup()
            .addGap(24, 24, 24)
            .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(Panel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addGap(0, 110, Short.MAX_VALUE))
          .addGroup(Panel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel1Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbchsr, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
              .addComponent(uri, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        .addContainerGap())
    );
    Panel1Layout.setVerticalGroup(
      Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(Panel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(dbchsr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)
          .addComponent(jButton1))
        .addGap(4, 4, 4)
        .addComponent(title)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(uri)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(jLabel2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
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

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		IRI dbid = dbchsr.getItemAt( dbchsr.getSelectedIndex() );
		DIHelper.getInstance().getMetadataStore().clearGraphSettings( dbid );
  }//GEN-LAST:event_jButton1ActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel Panel1;
  private javax.swing.JList<Color> colors;
  private javax.swing.JComboBox<IRI> dbchsr;
  private javax.swing.JLabel is;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JList<NamedShape> shapes;
  private javax.swing.JLabel title;
  private javax.swing.JTree tree;
  private javax.swing.JLabel uri;
  private javax.swing.JLabel was;
  // End of variables declaration//GEN-END:variables
}
