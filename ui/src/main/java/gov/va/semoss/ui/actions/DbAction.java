/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public abstract class DbAction extends AbstractAction {

  public static final String MERGE = "Database";
  public static final String PROPS = "About Database";
  public static final String CLONE = "Copy ";
  public static final String CLONECONF = "Clone Setup (no data) ";
  public static final String MOUNT = "Attach DB";
  public static final String CREATE = "Create DB";
  public static final String IMPORTRTM = "RTM Loading Sheet Generator";
  public static final String UNMOUNT = "Detach ";
  public static final String PIN = "Pin ";
  public static final String CLEAR = "Clear";
  public static final String IMPORTLS = "External File(s)";
  public static final String EXPORTTTL = "Turtle (.ttl)";
  public static final String EXPORTNT = "N-Triples (.nt)";
  public static final String EXPORTRDF = "RDF/XML (.rdf)";
  public static final String EXPORTLS = "Complete Database";
  public static final String EXPORTLSNODES = "All Nodes";
  public static final String EXPORTLSSOMENODES = "Specific Nodes";
  public static final String EXPORTLSRELS = "All Relationships";
  public static final String EXPORTLSSOMERELS = "Specific Relationships";
  public static final String ENDPOINT = "Start SparQL Endpoint";
	public static final String CONSISTENCYCHECK = "Quality Checks";

  public final String opprogName;
  private IEngine engine;

  public DbAction( String opprog ) {
    opprogName = opprog;
  }

  public DbAction( String opprog, String name ) {
    super( name );
    opprogName = opprog;
  }

  public DbAction( String opprog, String name, Icon icon ) {
    super( name, icon );
    opprogName = opprog;
  }

  public DbAction( String opprog, String name, String iconname ) {
    this( opprog, name, null == iconname || iconname.isEmpty() ? null
        : getIcon( iconname ) );
  }

  public void setEngine( IEngine eng ) {
    engine = eng;
  }

  public IEngine getEngine() {
    return engine;
  }

  public String getEngineName() {
    return ( null == engine ? "" : EngineUtil.getEngineLabel( engine ) );
  }

  @Override
  public void actionPerformed( ActionEvent ae ) {
    if ( preAction( ae ) ) {
      OperationsProgress.getInstance( opprogName ).add( getTask( ae ) );
    }
  }

  protected abstract ProgressTask getTask( ActionEvent ae );

  protected boolean preAction( ActionEvent ae ) {
    return true;
  }

  public static Icon getIcon( String middle ) {
    return new ImageIcon( DbAction.class.getResource( "/images/icons16/"
        + middle + "_16.png" ) );
  }
}
