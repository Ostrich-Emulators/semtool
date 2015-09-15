/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import gov.va.semoss.ui.main.listener.impl.FileBrowseListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.components.models.NamespaceTableModel;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.user.Security;

/**
 *
 * @author ryan
 */
public class SettingsPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( SettingsPanel.class );
	private final NamespaceTableModel namespacemodel = new NamespaceTableModel(
			Security.getSecurity().getAssociatedUser( DIHelper.getInstance().getRdfEngine() ).isLocal() );

	protected SettingsPanel( Class<?> preferenceRoot ) {
		initComponents();

		final Preferences prefs = Preferences.userNodeForPackage( preferenceRoot );

		ActionListener preflistener = new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				JCheckBox item = JCheckBox.class.cast( e.getSource() );
				boolean ischecked = item.isSelected();
				String cmd = e.getActionCommand();

				prefs.putBoolean( cmd, ischecked );

				DIHelper.getInstance().getCoreProp()
						.setProperty( cmd, Boolean.toString( ischecked ) );
			}
		};

		Map<JCheckBox, String> map = new HashMap<>();
		map.put( calcInfers, Constants.CALC_INFERENCES_PREF );

		for ( Map.Entry<JCheckBox, String> e : map.entrySet() ) {
			JCheckBox c = e.getKey();
			String val = e.getValue();
			c.setActionCommand( val );
			c.addActionListener( preflistener );
			c.setSelected( PlayPane.getProp( prefs, val ) );
		}

		User user = Security.getSecurity().getAssociatedUser( DIHelper.getInstance().getRdfEngine() );
		fullname.setText( user.getProperty( UserProperty.USER_FULLNAME ) );
		email.setText( user.getProperty( UserProperty.USER_EMAIL ) );
		organization.setText( user.getProperty( UserProperty.USER_ORG ) );
		namespacemodel.setNamespaces( user.getNamespaces() );

		fullname.setEditable( user.isLocal() );
		email.setEditable( user.isLocal() );
		organization.setEditable( user.isLocal() );

	}

	public static void showDialog( Frame frame ) {
		SettingsPanel sp = new SettingsPanel( SemossPreferences.class );
		String opts[] = { "Close" };
		int ans = JOptionPane.showOptionDialog( frame, sp, "Options",
				JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] );

		if ( 0 == ans ) {
			User user = Security.getSecurity().getAssociatedUser( DIHelper.getInstance().getRdfEngine() );
			user.setProperty( UserProperty.USER_FULLNAME, sp.fullname.getText() );
			user.setProperty( UserProperty.USER_ORG, sp.organization.getText() );
			user.setProperty( UserProperty.USER_EMAIL, sp.email.getText() );
			user.setNamespaces( sp.namespacemodel.getNamespaces() );
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    calcInfers = new javax.swing.JCheckBox();
    jButton1 = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    fullname = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    email = new javax.swing.JTextField();
    organization = new javax.swing.JTextField();
    jPanel2 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    namespaces = new javax.swing.JTable();

    calcInfers.setText("Compute dependent relationships following load");

    jButton1.setText("Reset All Preferences...");
    jButton1.setToolTipText("Resets all preferences and saved file locations");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(java.awt.Color.gray, 1, true), "User Information"));

    jLabel1.setText("Full Name");

    jLabel2.setText("Email");

    jLabel3.setText("Organization");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel3)
          .addComponent(jLabel2)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(email, javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(fullname)
          .addComponent(organization))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(fullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(organization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 6, Short.MAX_VALUE))
    );

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(java.awt.Color.gray, 1, true), "Namespaces"));

    namespaces.setModel(namespacemodel);
    jScrollPane2.setViewportView(namespaces);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(276, 276, 276))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(calcInfers)
              .addComponent(jButton1))
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(calcInfers)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jButton1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		int ok = JOptionPane.showConfirmDialog( this,
				"This will reset all preferences and saved locations. \nYou must restart to see all the changes. Really reset?",
				"Confirm Reset", JOptionPane.WARNING_MESSAGE );
		if ( JOptionPane.YES_OPTION == ok ) {
			Class<?> classesToClear[] = { DbAction.class, FileBrowsePanel.class,
				PlayPane.class, SemossPreferences.class, FileBrowseListener.class,
				BindingPanel.class
			};

			for ( Class<?> c : classesToClear ) {
				Preferences prefs = Preferences.userNodeForPackage( c );
				try {
					prefs.clear();
				}
				catch ( BackingStoreException bse ) {
					log.error( bse, bse );
				}
			}
		}
  }//GEN-LAST:event_jButton1ActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox calcInfers;
  private javax.swing.JTextField email;
  private javax.swing.JTextField fullname;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JTable namespaces;
  private javax.swing.JTextField organization;
  // End of variables declaration//GEN-END:variables
}
