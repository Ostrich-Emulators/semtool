/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;
import com.ostrichemulators.semtool.ui.components.tabbedqueries.SparqlTextArea;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class CustomReificationPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( CustomReificationPanel.class );
	private final SparqlTextArea sparqlarea = new SparqlTextArea();
	private final DefaultListModel<IRI> resultsmodel = new DefaultListModel<>();
	private final IEngine engine;
	private String conceptquery;
	private String edgequery;

	/**
	 * Creates new form CustomReificationPanel
	 *
	 * @param eng
	 */
	public CustomReificationPanel( IEngine eng ) {
		engine = eng;
		initComponents();
		resultslist.setCellRenderer( LabeledPairRenderer.getUriPairRenderer( engine ) );

		RTextScrollPane cp = new RTextScrollPane( sparqlarea );
		sparqlpanel.add( cp, BorderLayout.CENTER );

		sparqlarea.getDocument().addDocumentListener( new DocumentListener() {

			@Override
			public void insertUpdate( DocumentEvent e ) {
				update();
			}

			@Override
			public void removeUpdate( DocumentEvent e ) {
				update();
			}

			@Override
			public void changedUpdate( DocumentEvent e ) {
				update();
			}

			private void update() {
				if ( conceptbtn.isSelected() ) {
					conceptquery = sparqlarea.getText();
				}
				else {
					edgequery = sparqlarea.getText();
				}
			}
		} );

		scroller.setVisible( false );
	}

	public static IRI showDialog( Frame frame, IEngine engine ) {
		CustomReificationPanel crp = new CustomReificationPanel( engine );

		OneValueQueryAdapter<String> qa
				= OneValueQueryAdapter.getString( "SELECT ?val WHERE { ?base ?pred ?val }" );
		qa.bind( "base", engine.getBaseIri() );
		qa.bind("pred", SEMTOOL.ConceptsSparql );
		String concept = engine.queryNoEx( qa );

		qa.bind("pred", SEMTOOL.EdgesSparql );
		String edge = engine.queryNoEx( qa );

		crp.setConceptSparql( concept );
		crp.setEdgeSparql( edge );

		String options[] = { "Customize", "Use SEMTOOL Model", "Cancel" };
		int ans = JOptionPane.showOptionDialog( frame, crp, "Customize Reification",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0] );

		IRI model[] = { null };
		if ( JOptionPane.CANCEL_OPTION != ans ) {

			try {
				engine.execute(new ModificationExecutorAdapter() {

					@Override
					public void exec( RepositoryConnection conn ) throws RepositoryException {
						ValueFactory vf = conn.getValueFactory();
						IRI base = engine.getBaseIri();

						conn.remove(base, SEMTOOL.ReificationModel, null );
						conn.remove(base, SEMTOOL.ConceptsSparql, null );
						conn.remove(base, SEMTOOL.EdgesSparql, null );

						if ( JOptionPane.YES_OPTION == ans ) {
							model[0] = SEMTOOL.Custom_Reification;

							String cpc = crp.getConceptSparql();
							if ( !( null == cpc || cpc.isEmpty() ) ) {
								conn.add(base, SEMTOOL.ConceptsSparql, vf.createLiteral( cpc ) );
							}

							cpc = crp.getEdgeSparql();
							if ( !( null == cpc || cpc.isEmpty() ) ) {
								conn.add(base, SEMTOOL.EdgesSparql, vf.createLiteral( cpc ) );
							}
						}
						else {
							model[0] = SEMTOOL.SEMTOOL_Reification;
						}

						conn.add(base, SEMTOOL.ReificationModel, model[0] );
					}
				} );
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}

		return model[0];
	}

	public void setConceptSparql( String sparql ) {
		if( null == sparql || sparql.isEmpty() ){
			sparql = "SELECT ?concept WHERE {\n\n}";
		}

		conceptquery = sparql;
		if ( conceptbtn.isSelected() ) {
			sparqlarea.setText( sparql );
		}
	}

	public void setEdgeSparql( String sparql ) {
		if( null == sparql || sparql.isEmpty() ){
			sparql = "SELECT ?edge WHERE {\n\n}";
		}
		edgequery = sparql;
		if ( edgebtn.isSelected() ) {
			sparqlarea.setText( sparql );
		}
	}

	public String getConceptSparql() {
		return conceptquery;
	}

	public String getEdgeSparql() {
		return edgequery;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    buttonGroup1 = new javax.swing.ButtonGroup();
    testbtn = new javax.swing.JButton();
    conceptbtn = new javax.swing.JToggleButton();
    edgebtn = new javax.swing.JToggleButton();
    splitter = new javax.swing.JSplitPane();
    sparqlpanel = new javax.swing.JPanel();
    scroller = new javax.swing.JScrollPane();
    resultslist = new javax.swing.JList();

    testbtn.setText("Test SPARQL");
    testbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        testbtnActionPerformed(evt);
      }
    });

    buttonGroup1.add(conceptbtn);
    conceptbtn.setSelected(true);
    conceptbtn.setText("Concepts");
    conceptbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        conceptbtnActionPerformed(evt);
      }
    });

    buttonGroup1.add(edgebtn);
    edgebtn.setText("Edges");
    edgebtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edgebtnActionPerformed(evt);
      }
    });

    splitter.setDividerLocation(600);
    splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    splitter.setOneTouchExpandable(true);

    sparqlpanel.setPreferredSize(new java.awt.Dimension(450, 500));
    sparqlpanel.setLayout(new java.awt.BorderLayout());
    splitter.setLeftComponent(sparqlpanel);

    scroller.setPreferredSize(new java.awt.Dimension(100, 100));

    resultslist.setModel(resultsmodel);
    scroller.setViewportView(resultslist);

    splitter.setRightComponent(scroller);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(testbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(edgebtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(conceptbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(splitter))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(conceptbtn)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(edgebtn)
        .addGap(30, 30, 30)
        .addComponent(testbtn)
        .addContainerGap(333, Short.MAX_VALUE))
      .addComponent(splitter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

	private String getSparql() {
		return ( conceptbtn.isSelected() ? getConceptSparql() : getEdgeSparql() );
	}

  private void conceptbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conceptbtnActionPerformed
		sparqlarea.setText( conceptquery );
		resultsmodel.clear();
		splitter.setDividerLocation( 1.0 );
  }//GEN-LAST:event_conceptbtnActionPerformed

  private void testbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testbtnActionPerformed
		String sparql = getSparql();
		resultsmodel.clear();
		scroller.setVisible( true );

		try {
			for ( IRI u : engine.query( OneVarListQueryAdapter.getIriList( sparql ) ) ) {
				resultsmodel.addElement( u );
			}
			splitter.setDividerLocation( 0.67 );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			GuiUtility.showError( e.getLocalizedMessage() );
		}
  }//GEN-LAST:event_testbtnActionPerformed

  private void edgebtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgebtnActionPerformed
		sparqlarea.setText( edgequery );
		resultsmodel.clear();
		splitter.setDividerLocation( 1.0 );
  }//GEN-LAST:event_edgebtnActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JToggleButton conceptbtn;
  private javax.swing.JToggleButton edgebtn;
  private javax.swing.JList resultslist;
  private javax.swing.JScrollPane scroller;
  private javax.swing.JPanel sparqlpanel;
  private javax.swing.JSplitPane splitter;
  private javax.swing.JButton testbtn;
  // End of variables declaration//GEN-END:variables
}
