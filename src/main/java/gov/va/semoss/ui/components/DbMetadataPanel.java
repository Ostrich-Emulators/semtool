/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class DbMetadataPanel extends javax.swing.JPanel implements ActionListener {

	private static final Logger log = Logger.getLogger( DbMetadataPanel.class );
	private IEngine engine;
	private final Map<URI, JTextField> fieldlkp = new HashMap<>();
	private URI baseuri = null;
	private boolean loadable = false;

	/**
	 * Creates new form DbPropertiesPanel
	 */
	public DbMetadataPanel() {
		this( null );
	}

	public DbMetadataPanel( IEngine eng ) {
		engine = eng;
		initComponents();

		fieldlkp.put( RDFS.LABEL, title );
		fieldlkp.put( MetadataConstants.DCT_DESC, summary );
		fieldlkp.put( MetadataConstants.DCT_CREATOR, organization );
		fieldlkp.put( MetadataConstants.DCT_PUBLISHER, poc );
		fieldlkp.put(MetadataConstants.DCT_CREATED, created );
		fieldlkp.put(MetadataConstants.DCT_MODIFIED, update );
		fieldlkp.put( MetadataConstants.VOID_DS, voiduri );
		fieldlkp.put( OWL.VERSIONINFO, owlinfo );
		fieldlkp.put( OWL.VERSIONIRI, owliri );

		voiduri.setEditable( null == baseuri );
		voiduri.setBackground( null == baseuri ? title.getBackground()
				: created.getBackground() );

		smss.setText( engine.getProperty( Constants.SMSS_LOCATION ) );

		voiduri.addKeyListener( new KeyAdapter() {

			@Override
			public void keyTyped( KeyEvent e ) {
				loadable = ( !voiduri.getText().isEmpty() );
			}
		} );

		if ( null != engine ) {
			refresh();
		}
	}

	private void doSave() {
		int i = 1;
		// if it's enabled, we HAVE to save the baseuri first so the 
		// other fields have a URI to save to
		JTextField base = voiduri;
		DbMetadataPanel.this.actionPerformed(
				new ActionEvent( base, i++, MetadataConstants.VOID_DS.stringValue() ) );

		for ( Map.Entry<URI, JTextField> entry : fieldlkp.entrySet() ) {
			if ( !MetadataConstants.VOID_DS.equals( entry.getKey() ) ) {
				DbMetadataPanel.this.actionPerformed( new ActionEvent( entry.getValue(),
						i++, entry.getKey().stringValue() ) );
			}
		}
	}

	public boolean isLoadable() {
		return loadable;
	}

	public static void showDialog( Frame f, IEngine engine ) {
		DbMetadataPanel dbdata = new DbMetadataPanel( engine );

		Object options[] = { "Save", "Cancel" };

		boolean ok = false;
		boolean dosave = false;
		while ( !ok ) {

			int opt = JOptionPane.showOptionDialog( f, dbdata,
					"Properties of " + engine.getEngineName(), JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] );
			ok = dbdata.isLoadable();
			if ( 0 == opt ) {
				if ( ok ) {
					dosave = true;
				}
				else {
					JOptionPane.showMessageDialog( f, "You must specify a base URI to continue",
							"No base URI set!", JOptionPane.ERROR_MESSAGE );
				}
			}
			else {
				ok = true;
			}
		}

		if ( dosave ) {
			dbdata.doSave();
		}
	}

	public final void refresh() {
		IEngine eng
				= ( null == engine ? DIHelper.getInstance().getRdfEngine() : engine );
		baseuri = null;

		for ( JTextField jtf : fieldlkp.values() ) {
			jtf.setText( null );
		}

		if ( null == eng ) {
			return;
		}

		try {
			Map<URI, String> metadata = eng.query( new MetadataQuery() );
			if ( metadata.containsKey( MetadataConstants.VOID_DS ) ) {
				baseuri = new URIImpl( metadata.get( MetadataConstants.VOID_DS ) );
			}

			for ( Map.Entry<URI, String> en : metadata.entrySet() ) {
				URI pred = en.getKey();
				String val = en.getValue();

				if ( fieldlkp.containsKey( pred ) ) {
					fieldlkp.get( pred ).setText( val );
				}
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}

		// if we don't have a baseuri, you can make one
		voiduri.setEditable( null == baseuri );
		voiduri.setBackground( null == baseuri ? title.getBackground()
				: created.getBackground() );

		loadable = ( null != baseuri );
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    tlbl = new javax.swing.JLabel();
    title = new javax.swing.JTextField();
    slbl = new javax.swing.JLabel();
    summary = new javax.swing.JTextField();
    olbl = new javax.swing.JLabel();
    organization = new javax.swing.JTextField();
    plbl = new javax.swing.JLabel();
    poc = new javax.swing.JTextField();
    clbl = new javax.swing.JLabel();
    created = new javax.swing.JTextField();
    llbl = new javax.swing.JLabel();
    update = new javax.swing.JTextField();
    blbl = new javax.swing.JLabel();
    voiduri = new javax.swing.JTextField();
    mlbl = new javax.swing.JLabel();
    smss = new javax.swing.JTextField();
    wlbl = new javax.swing.JLabel();
    owlinfo = new javax.swing.JTextField();
    w2lbl = new javax.swing.JLabel();
    owliri = new javax.swing.JTextField();

    tlbl.setLabelFor(title);
    tlbl.setText("Title");
    tlbl.setPreferredSize(new java.awt.Dimension(132, 25));

    slbl.setLabelFor(summary);
    slbl.setText("Summary");
    slbl.setPreferredSize(new java.awt.Dimension(132, 25));

    olbl.setLabelFor(organization);
    olbl.setText("Organization");
    olbl.setPreferredSize(new java.awt.Dimension(132, 25));

    plbl.setLabelFor(poc);
    plbl.setText("Point of Contact");
    plbl.setPreferredSize(new java.awt.Dimension(132, 25));

    clbl.setLabelFor(created);
    clbl.setText("Created On");
    clbl.setPreferredSize(new java.awt.Dimension(132, 25));

    created.setEditable(false);
    created.setBackground(java.awt.Color.lightGray);

    llbl.setLabelFor(update);
    llbl.setText("Last Update");
    llbl.setPreferredSize(new java.awt.Dimension(132, 25));

    update.setEditable(false);
    update.setBackground(java.awt.Color.lightGray);

    blbl.setLabelFor(voiduri);
    blbl.setText("Base URI");
    blbl.setPreferredSize(new java.awt.Dimension(132, 25));

    voiduri.setBackground(new java.awt.Color(238, 238, 238));

    mlbl.setLabelFor(smss);
    mlbl.setText("SMSS File");
    mlbl.setPreferredSize(new java.awt.Dimension(132, 25));

    smss.setEditable(false);
    smss.setBackground(java.awt.Color.lightGray);

    wlbl.setText("Version Info");

    owlinfo.setEditable(false);
    owlinfo.setBackground(java.awt.Color.lightGray);

    w2lbl.setText("Version IRI");

    owliri.setEditable(false);
    owliri.setBackground(java.awt.Color.lightGray);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(w2lbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(wlbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(tlbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(slbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(olbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(clbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(llbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(blbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(mlbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(plbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(summary, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
          .addComponent(organization)
          .addComponent(created)
          .addComponent(update)
          .addComponent(voiduri)
          .addComponent(smss)
          .addComponent(poc)
          .addComponent(title)
          .addComponent(owlinfo)
          .addComponent(owliri))
        .addGap(0, 0, 0))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(tlbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(slbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(summary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(olbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(organization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(plbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(poc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(wlbl)
          .addComponent(owlinfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(clbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(created, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(llbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(blbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(voiduri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(2, 2, 2)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(w2lbl)
          .addComponent(owliri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(2, 2, 2)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(mlbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(smss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel blbl;
  private javax.swing.JLabel clbl;
  private javax.swing.JTextField created;
  private javax.swing.JLabel llbl;
  private javax.swing.JLabel mlbl;
  private javax.swing.JLabel olbl;
  private javax.swing.JTextField organization;
  private javax.swing.JTextField owlinfo;
  private javax.swing.JTextField owliri;
  private javax.swing.JLabel plbl;
  private javax.swing.JTextField poc;
  private javax.swing.JLabel slbl;
  private javax.swing.JTextField smss;
  private javax.swing.JTextField summary;
  private javax.swing.JTextField title;
  private javax.swing.JLabel tlbl;
  private javax.swing.JTextField update;
  private javax.swing.JTextField voiduri;
  private javax.swing.JLabel w2lbl;
  private javax.swing.JLabel wlbl;
  // End of variables declaration//GEN-END:variables

	@Override
	public void actionPerformed( ActionEvent ae ) {
		final IEngine eng
				= ( null == engine ? DIHelper.getInstance().getRdfEngine() : engine );
		final URI uri = new URIImpl( ae.getActionCommand() );
		final String val = fieldlkp.get( uri ).getText();

		try {
			eng.execute( new ModificationExecutor() {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					ValueFactory fac = conn.getValueFactory();
					if ( uri.equals( MetadataConstants.VOID_DS ) ) {
						baseuri = fac.createURI( val );
						conn.add( baseuri, RDF.TYPE, MetadataConstants.VOID_DS );
					}
					else {
						if ( conn.hasStatement( baseuri, uri, null, false ) ) {
							// remove the old value, and add the new one
							// (we don't want multiple values for these URIs)
							conn.remove( baseuri, uri, null );
						}
						if ( !( null == val || val.isEmpty() ) ) {
							conn.add( baseuri, uri, fac.createLiteral( val ) );
						}
					}
				}

				@Override

				public boolean execInTransaction() {
					return true;
				}
			} );
		}
		catch ( Exception e ) {
			log.error( "could not update db metadata", e );
		}

	}
}
