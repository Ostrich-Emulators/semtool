/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.ui.components.models.ValueTableModel;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ryan
 */
public class AddTabPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( AddTabPanel.class );
	private static final ValueFactory vf = SimpleValueFactory.getInstance();
  private final ValueTableModel model = new ValueTableModel( false );


	/**
	 * Creates new form AddTabPanel
	 */
	public AddTabPanel() {
		model.setReadOnly( false );
		model.setAllowInsertsInPlace( true );
		model.setHeaders( Arrays.asList( "Name", "Datatype" ) );

		initComponents();
	}

	public boolean isNodeSheet() {
		return nodetype.isSelected();
	}

	public LoadingSheetData getSheet() {
		String subj = subjectClass.getText();

		String obj = objectClass.getText();
		String relname = relClass.getText();

		LoadingSheetData lsd = ( reltype.isSelected()
				? LoadingSheetData.relsheet( subj, obj, relname )
				: LoadingSheetData.nodesheet( subj ) );

		for ( int r = 0; r < model.getRealRowCount(); r++ ) {
			IRI dt = null;
			Object pval = model.getValueAt( r, 0 );
			Object dtval = null;
			try {
				dtval = model.getValueAt( r, 1 );
				if ( null != dtval ) {
					dt = vf.createIRI( dtval.toString() );
				}
			}
			catch ( Exception e ) {
				log.error( "could not parse datatype: " + dtval, e );
			}

			if ( null != pval ) {
				lsd.addProperty( pval.toString(), dt );
			}
		}

		return lsd;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    tabTypeGroup = new javax.swing.ButtonGroup();
    nodetype = new javax.swing.JRadioButton();
    reltype = new javax.swing.JRadioButton();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    subjectClass = new javax.swing.JTextField();
    relpanel = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    objectClass = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    relClass = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    proptable = new javax.swing.JTable();

    tabTypeGroup.add(nodetype);
    nodetype.setText("Node");
    nodetype.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nodetypeActionPerformed(evt);
      }
    });

    tabTypeGroup.add(reltype);
    reltype.setSelected(true);
    reltype.setText("Relationship");
    reltype.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        reltypeActionPerformed(evt);
      }
    });

    jLabel1.setText("Type");

    jLabel2.setText("Subject Class");

    jLabel3.setText("Object Class");

    jLabel4.setText("Relationship");

    javax.swing.GroupLayout relpanelLayout = new javax.swing.GroupLayout(relpanel);
    relpanel.setLayout(relpanelLayout);
    relpanelLayout.setHorizontalGroup(
      relpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(relpanelLayout.createSequentialGroup()
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(objectClass))
      .addGroup(relpanelLayout.createSequentialGroup()
        .addComponent(jLabel4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(relClass))
    );
    relpanelLayout.setVerticalGroup(
      relpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(relpanelLayout.createSequentialGroup()
        .addGroup(relpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(objectClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(relpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(relClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(java.awt.Color.gray, 1, true), "Properties"));

    proptable.setAutoCreateRowSorter(true);
    proptable.setModel(model);
    jScrollPane1.setViewportView(proptable);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(relpanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel2)
              .addComponent(jLabel1))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(nodetype)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reltype)
                .addGap(0, 0, Short.MAX_VALUE))
              .addComponent(subjectClass)))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(nodetype)
          .addComponent(reltype)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(subjectClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(relpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void nodetypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodetypeActionPerformed
		relpanel.setVisible( false );
  }//GEN-LAST:event_nodetypeActionPerformed

  private void reltypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reltypeActionPerformed
		relpanel.setVisible( true );
  }//GEN-LAST:event_reltypeActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JRadioButton nodetype;
  private javax.swing.JTextField objectClass;
  private javax.swing.JTable proptable;
  private javax.swing.JTextField relClass;
  private javax.swing.JPanel relpanel;
  private javax.swing.JRadioButton reltype;
  private javax.swing.JTextField subjectClass;
  private javax.swing.ButtonGroup tabTypeGroup;
  // End of variables declaration//GEN-END:variables
}
