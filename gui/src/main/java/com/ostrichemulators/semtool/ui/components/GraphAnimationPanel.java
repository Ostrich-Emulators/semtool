/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.AbstractGraphElement;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.MultiSetMap;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Frame;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import org.eclipse.rdf4j.model.URI;

/**
 *
 * @author ryan
 */
public class GraphAnimationPanel extends javax.swing.JPanel {

	private final LabeledPairRenderer renderer;
	private final MultiSetMap<URI, URI> elements = new MultiSetMap<>();

	/**
	 * Creates new form GraphAnimationInput
	 *
	 * @param rlc
	 * @param engine
	 * @param modelvals
	 */
	public GraphAnimationPanel( RetrievingLabelCache rlc, IEngine engine,
			MultiMap<URI, URI> modelvals ) {

		for ( Map.Entry<URI, List<URI>> en : modelvals.entrySet() ) {
			elements.addAll( en.getKey(), en.getValue() );
		}

		renderer = LabeledPairRenderer.getValuePairRenderer( rlc );
		initComponents();

		edgetype.setRenderer( renderer );
		predicate.setRenderer( renderer );

		Map<URI, String> labels = Utility.getInstanceLabels( modelvals.keySet(), engine );
		labels = Utility.sortUrisByLabel( labels );

		for ( URI u : labels.keySet() ) {
			edgetype.addItem( u );
		}

		edgetype.setSelectedIndex( 0 );
	}

	/**
	 * Gets the selected edge type and predicate on which to animate
	 *
	 * @return
	 */
	public Map<URI, URI> getAnimationInputs() {
		Map<URI, URI> map = new HashMap<>();
		map.put( edgetype.getItemAt( edgetype.getSelectedIndex() ),
				predicate.getItemAt( predicate.getSelectedIndex() ) );
		return map;
	}

	public static boolean hasAnimationCandidates( Collection<? extends GraphElement> gelements ) {

		MultiMap<URI, URI> modelvals = new MultiMap<>();
		for ( GraphElement ge : gelements ) {
			List<URI> countables = AbstractGraphElement.getCountablePropertyKeys( ge );
			if ( !countables.isEmpty() ) {
				modelvals.addAll( ge.getType(), countables );
			}
		}

		return !modelvals.isEmpty();
	}

	public static Map<URI, URI> getAnimationInput( Frame frame, RetrievingLabelCache rlc,
			IEngine engine, Collection<? extends GraphElement> gelements ) {

		if ( !hasAnimationCandidates( gelements ) ) {
			JOptionPane.showMessageDialog( frame, "No Animation Candidates",
					"Nothing to Animate", JOptionPane.INFORMATION_MESSAGE );
			return new HashMap<>();
		}

		MultiMap<URI, URI> modelvals = new MultiMap<>();
		for ( GraphElement ge : gelements ) {
			List<URI> countables = AbstractGraphElement.getCountablePropertyKeys( ge );
			if ( !countables.isEmpty() ) {
				modelvals.addAll( ge.getType(), countables );
			}
		}

		String options[] = { "OK", "Cancel" };
		GraphAnimationPanel inputs = new GraphAnimationPanel( rlc, engine, modelvals );

		int ans = JOptionPane.showOptionDialog( frame, inputs, "Animate On...",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0] );
		return ( 0 == ans ? inputs.getAnimationInputs() : new HashMap<>() );
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    predicate = new javax.swing.JComboBox<URI>();
    edgetype = new javax.swing.JComboBox<URI>();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();

    predicate.setModel(new DefaultComboBoxModel<>());

    edgetype.setModel(new DefaultComboBoxModel<>());
    edgetype.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edgetypeActionPerformed(evt);
      }
    });

    jLabel2.setText("Type");

    jLabel3.setText("Field");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(edgetype, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(predicate, 0, 294, Short.MAX_VALUE)))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(edgetype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(predicate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3))
        .addGap(0, 0, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void edgetypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgetypeActionPerformed
		URI edger = edgetype.getItemAt( edgetype.getSelectedIndex() );
		Set<URI> fields = elements.getNN( edger );

		predicate.removeAllItems();
		for ( URI u : fields ) {
			predicate.addItem( u );
		}
  }//GEN-LAST:event_edgetypeActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox<URI> edgetype;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JComboBox<URI> predicate;
  // End of variables declaration//GEN-END:variables
}
