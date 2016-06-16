/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
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
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.ui.components.playsheets.GridPlaySheet;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.UriBuilder;

import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;

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
	DefaultListModel<URI> subsetmodel = new DefaultListModel<>();

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
		fieldlkp.put( MetadataConstants.DCT_CREATED, created );
		fieldlkp.put( MetadataConstants.DCT_MODIFIED, update );
		fieldlkp.put( SEMTOOL.ReificationModel, edgemodel );
		fieldlkp.put( SEMTOOL.Database, voiduri );

		voiduri.setEditable( null == baseuri );
		voiduri.setBackground( null == baseuri ? title.getBackground()
				: created.getBackground() );

		smss.setText( engine.getProperty( Constants.SMSS_LOCATION ) );

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyTyped( KeyEvent e ) {
				URI suri = null;
				URI duri = null;
				try {
					suri = new URIImpl( schemans.getText() );
					duri = new URIImpl( datans.getText() );
				}
				catch ( Exception ex ) {
					// don't care
				}

				loadable = ( !( null == suri || null == duri ) );
			}
		};

		schemans.addKeyListener( ka );
		datans.addKeyListener( ka );

		if ( null != engine ) {
			refresh();
		}

		subsets.addMouseListener( new MouseAdapter() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				final URI uri = subsets.getSelectedValue();
				GridPlaySheet gps = new GridPlaySheet();

				ListQueryAdapter<Value[]> q = new ListQueryAdapter( "SELECT ?p ?o { ?s ?p ?o }" ) {

					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						Value data[] = { set.getValue( "p" ), set.getValue( "o" ) };
						add( data );
					}
				};

				q.bind( "s", uri );
				try {
					List<Value[]> rows = engine.query( q );
					gps.create( rows, Arrays.asList( "Property", "Value" ), engine );

					JOptionPane.showMessageDialog( created, gps,
							"Properties of " + uri, JOptionPane.INFORMATION_MESSAGE
					);
				}
				catch ( RepositoryException | MalformedQueryException | QueryEvaluationException | HeadlessException ex ) {
					log.error( ex, ex );
				}
			}
		} );
	}

	private void doSave() {
		int i = 1;
		for ( Map.Entry<URI, JTextField> entry : fieldlkp.entrySet() ) {
			if ( !( SEMTOOL.Database.equals( entry.getKey() )
					|| SEMTOOL.ReificationModel.equals( entry.getKey() ) ) ) {
				DbMetadataPanel.this.actionPerformed( new ActionEvent( entry.getValue(),
						i++, entry.getKey().stringValue() ) );
			}
		}

		engine.setSchemaBuilder( UriBuilder.getBuilder( schemans.getText() ) );
		engine.setDataBuilder( UriBuilder.getBuilder( datans.getText() ) );
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
					JOptionPane.showMessageDialog( f, "You must specify a schema and data namespace URIs to continue",
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
		schemans.setText( null );
		datans.setText( null );

		if ( null == eng ) {
			return;
		}

		schemans.setText( engine.getSchemaBuilder().toString() );
		datans.setText( engine.getDataBuilder().toString() );

		try {
			MetadataQuery mq = new MetadataQuery();
			Map<URI, Value> metadata = eng.query( mq );
			if ( metadata.containsKey( SEMTOOL.Database ) ) {
				baseuri = URI.class.cast( metadata.get( SEMTOOL.Database ) );
			}

			if ( metadata.containsKey( SEMTOOL.ReificationModel ) ) {
				URI reif = URI.class.cast( metadata.get( SEMTOOL.ReificationModel ) );
				metadata.put( SEMTOOL.ReificationModel,
						new LiteralImpl( Utility.getInstanceLabel( reif, eng ) ) );
			}

			for ( Map.Entry<URI, String> en : mq.asStrings().entrySet() ) {
				URI pred = en.getKey();
				String val = en.getValue();

				if ( fieldlkp.containsKey( pred ) ) {
					fieldlkp.get( pred ).setText( val );
				}
			}

			subsetmodel.clear();
			OneVarListQueryAdapter<URI> q
					= OneVarListQueryAdapter.getUriList( "SELECT ?o { ?base ?subset ?o }",
							"o" );
			q.bind( "base", engine.getBaseUri() );
			q.bind( "subset", MetadataConstants.VOID_SUBSET );
			List<URI> subsetUris = engine.query( q );
			for ( URI u : subsetUris ) {
				subsetmodel.addElement( u );
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

    jScrollPane1 = new javax.swing.JScrollPane();
    jList1 = new javax.swing.JList();
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
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    subsets = new javax.swing.JList<URI>();
    jLabel3 = new javax.swing.JLabel();
    schemans = new javax.swing.JTextField();
    datans = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    edgemodel = new javax.swing.JTextField();
    jButton1 = new javax.swing.JButton();

    jList1.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane1.setViewportView(jList1);

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

    voiduri.setEditable(false);
    voiduri.setBackground(java.awt.Color.lightGray);

    mlbl.setLabelFor(smss);
    mlbl.setText("Location");
    mlbl.setPreferredSize(new java.awt.Dimension(132, 25));

    smss.setEditable(false);
    smss.setBackground(java.awt.Color.lightGray);

    jLabel1.setText("Edge Model");

    jLabel2.setText("Datasets");

    subsets.setModel(subsetmodel);
    subsets.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane2.setViewportView(subsets);

    jLabel3.setText("Schema Namespace");

    jLabel4.setText("Data Namespace");

    jPanel1.setLayout(new java.awt.BorderLayout());

    edgemodel.setEditable(false);
    edgemodel.setBackground(java.awt.Color.lightGray);
    jPanel1.add(edgemodel, java.awt.BorderLayout.CENTER);

    jButton1.setText("Customize");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });
    jPanel1.add(jButton1, java.awt.BorderLayout.EAST);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(llbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(blbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(mlbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel2)
          .addComponent(clbl, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(tlbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(slbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(olbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(plbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel3)
          .addComponent(jLabel4)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(title)
          .addComponent(summary)
          .addComponent(organization)
          .addComponent(poc)
          .addComponent(schemans)
          .addComponent(datans)
          .addComponent(created)
          .addComponent(update)
          .addComponent(voiduri)
          .addComponent(smss)
          .addComponent(jScrollPane2)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
          .addComponent(jLabel3)
          .addComponent(schemans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(datans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel4))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(clbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(created, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(llbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(blbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(voiduri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(mlbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(smss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(31, 31, 31))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		Frame frame = JOptionPane.getFrameForComponent( this );
		URI reif = CustomReificationPanel.showDialog( frame, engine );

		if ( null != reif ) {
			edgemodel.setText( Utility.getInstanceLabel( reif, engine ) );
		}
  }//GEN-LAST:event_jButton1ActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel blbl;
  private javax.swing.JLabel clbl;
  private javax.swing.JTextField created;
  private javax.swing.JTextField datans;
  private javax.swing.JTextField edgemodel;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JList jList1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JLabel llbl;
  private javax.swing.JLabel mlbl;
  private javax.swing.JLabel olbl;
  private javax.swing.JTextField organization;
  private javax.swing.JLabel plbl;
  private javax.swing.JTextField poc;
  private javax.swing.JTextField schemans;
  private javax.swing.JLabel slbl;
  private javax.swing.JTextField smss;
  private javax.swing.JList<URI> subsets;
  private javax.swing.JTextField summary;
  private javax.swing.JTextField title;
  private javax.swing.JLabel tlbl;
  private javax.swing.JTextField update;
  private javax.swing.JTextField voiduri;
  // End of variables declaration//GEN-END:variables

	@Override
	public void actionPerformed( ActionEvent ae ) {
		final IEngine eng
				= ( null == engine ? DIHelper.getInstance().getRdfEngine() : engine );
		final URI uri = new URIImpl( ae.getActionCommand() );
		final String val = fieldlkp.get( uri ).getText();

		try {
			eng.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					ValueFactory fac = conn.getValueFactory();
					if ( uri.equals( SEMTOOL.Database ) ) {
						baseuri = fac.createURI( val );
						conn.add( baseuri, RDF.TYPE, SEMTOOL.Database );
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
			} );

			if ( RDFS.LABEL.equals( uri ) ) {
				eng.setEngineName( val );
			}
		}
		catch ( Exception e ) {
			log.error( "could not update db metadata", e );
			GuiUtility.showError( "Could not update the metadata" );
		}
	}
}
