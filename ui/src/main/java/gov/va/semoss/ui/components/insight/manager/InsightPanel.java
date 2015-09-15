/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.ui.components.BindingPanel;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.components.renderers.PlaySheetEnumRenderer;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class InsightPanel extends DataPanel<Insight> {

	private final JTree tree;
	private final DefaultTreeModel model;

	/**
	 * Creates new form InsightPanel
	 */
	public InsightPanel( JTree tree, DefaultTreeModel model ) {
		this.tree = tree;
		this.model = model;
		initComponents();

		playsheet.setModel( new DefaultComboBoxModel<>( PlaySheetEnum.valuesNoUpdate() ) );
		playsheet.setRenderer( new PlaySheetEnumRenderer() );

		listenTo( insightDesc );
		listenTo( insightName );
		listenTo( insightQuery );

		insightQuery.getDocument().addDocumentListener( new DocumentListener() {
			@Override
			public void insertUpdate( DocumentEvent e ) {
				setParameterHelper();
			}

			@Override
			public void removeUpdate( DocumentEvent e ) {
				setParameterHelper();
			}

			@Override
			public void changedUpdate( DocumentEvent e ) {
				setParameterHelper();
			}
		} );
	}

	public InsightPanel() {
		this( null, null );
	}

	@Override
	protected void isetElement( Insight i, DefaultMutableTreeNode node ) {
		insightName.setText( i.getLabel() );
		insightQuery.setText( i.getSparql() );
		insightDesc.setText( i.getDescription() );
		playsheet.setSelectedItem( PlaySheetEnum.valueFor( i ) );
		setParameterHelper();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel2 = new javax.swing.JLabel();
    insightName = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    playsheet = new javax.swing.JComboBox<PlaySheetEnum>();
    jLabel5 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    insightQuery = new gov.va.semoss.ui.components.tabbedqueries.SyntaxTextEditor();
    jLabel6 = new javax.swing.JLabel();
    jScrollPane4 = new javax.swing.JScrollPane();
    insightDesc = new javax.swing.JTextArea();
    testbtn = new javax.swing.JButton();
    paramLabel = new javax.swing.JLabel();

    jLabel2.setText("Insight Name");

    jLabel3.setText("Display With");

    playsheet.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jLabel5.setText("Query");

    insightQuery.setColumns(20);
    insightQuery.setRows(5);
    jScrollPane3.setViewportView(insightQuery);

    jLabel6.setText("Description");

    insightDesc.setColumns(20);
    insightDesc.setLineWrap(true);
    insightDesc.setRows(5);
    insightDesc.setWrapStyleWord(true);
    jScrollPane4.setViewportView(insightDesc);

    testbtn.setText("Test Query");
    testbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        testbtnActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(jLabel3)
          .addComponent(jLabel6)
          .addComponent(jLabel5))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(paramLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(testbtn))
          .addComponent(jScrollPane3)
          .addComponent(playsheet, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
          .addComponent(insightName, javax.swing.GroupLayout.Alignment.TRAILING))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(insightName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(playsheet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel6)
          .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel5)
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(testbtn)
          .addComponent(paramLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(46, 46, 46))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void testbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testbtnActionPerformed
		PlaySheetEnum pse = playsheet.getItemAt( playsheet.getSelectedIndex() );

		Insight insight
				= new Insight( insightName.getText(), insightQuery.getText(),
						(Class<? extends PlaySheetCentralComponent>) ( pse.getSheetClass() ) );
		insight.setDescription( insightDesc.getText() );

		List<Parameter> params = new ArrayList<>();
		DefaultMutableTreeNode node = getNode();
		Enumeration<DefaultMutableTreeNode> en = node.children();
		while ( en.hasMoreElements() ) {
			DefaultMutableTreeNode child = en.nextElement();
			params.add( Parameter.class.cast( child.getUserObject() ) );
		}

		Map<String, Value> bindings = new HashMap<>();
		if ( !params.isEmpty() ) {
			insight.setParameters( params );
			BindingPanel pnl = new BindingPanel( getEngine() );
			pnl.setParameters( params );

			String opts[] = { "OK", "Cancel" };

			int ans = JOptionPane.showOptionDialog( this, pnl, "Select Parameters",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] );
			if ( JOptionPane.YES_OPTION != ans ) {
				return;
			}

			for ( Map.Entry<Parameter, Value> bind : pnl.getBindings().entrySet() ) {
				bindings.put( bind.getKey().getVariable(), bind.getValue() );
			}
		}

		PlaySheetFrame psf = new PlaySheetFrame( getEngine() );
		psf.setTitle( "Insight Manager Query Test" );
		DIHelper.getInstance().getDesktop().add( psf );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add(
				psf.getCreateTask( insight, bindings ) );
		DIHelper.getInstance().getPlayPane().showDesktop();
  }//GEN-LAST:event_testbtnActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextArea insightDesc;
  private javax.swing.JTextField insightName;
  private gov.va.semoss.ui.components.tabbedqueries.SyntaxTextEditor insightQuery;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JLabel paramLabel;
  private javax.swing.JComboBox<PlaySheetEnum> playsheet;
  private javax.swing.JButton testbtn;
  // End of variables declaration//GEN-END:variables

	@Override
	public void updateElement( Insight i ) {
		i.setLabel( insightName.getText() );
		i.setDescription( insightDesc.getText() );
		i.setSparql( insightQuery.getText() );
		i.setOutput( playsheet.getItemAt( playsheet.getSelectedIndex() ).
				getSheetClass().getCanonicalName() );
	}

	public void setParameterHelper() {
		Map<String, Parameter> map = new HashMap<>();
		Enumeration<DefaultMutableTreeNode> en = getNode().children();
		while ( en.hasMoreElements() ) {
			DefaultMutableTreeNode n = en.nextElement();
			Parameter p = Parameter.class.cast( n.getUserObject() );
			map.put( p.getVariable(), p );
		}

		StringBuilder sb = new StringBuilder();
		String txt = insightQuery.getText();
		for ( Map.Entry<String, Parameter> it : map.entrySet() ) {
			Pattern pat = Pattern.compile( "\\?" + it.getKey() + "(\\b|$)" );
			Matcher m = pat.matcher( txt );
			if ( m.find() ) {
				if ( 0 == sb.length() ) {
					sb.append( "<html>" );
				}
				else {
					sb.append( "<br/>" );
				}
				sb.append( "<strong>?" ).append( it.getKey() ).append( "</strong> value from \"" ).
						append( it.getValue().getLabel() ).append( "\" Parameter" );
			}

		}
		paramLabel.setText( sb.toString() );

	}
}