/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.main.listener.impl;

import static gov.va.semoss.ui.actions.DbAction.MERGE;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.actions.CheckConsistencyAction;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.RepositoryList.RepositoryListModel;
import gov.va.semoss.ui.actions.ClearAction;
import gov.va.semoss.ui.actions.CloneAction;
import gov.va.semoss.ui.actions.CreateDbAction;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.actions.EndpointAction;
import gov.va.semoss.ui.actions.ExportGraphAction;
import gov.va.semoss.ui.actions.ExportInsightsAction;
import gov.va.semoss.ui.actions.ExportLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportSpecificNodesToLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportSpecificRelationshipsToLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportTtlAction;
import gov.va.semoss.ui.actions.ImportInsightsAction;
// import gov.va.vcamp.ui.actions.ImportRtmAction;
import gov.va.semoss.ui.actions.ImportLoadingSheetAction;
import gov.va.semoss.ui.actions.MergeAction;
import gov.va.semoss.ui.actions.MountAction;
import gov.va.semoss.ui.actions.PinAction;
import gov.va.semoss.ui.actions.PropertiesAction;
import gov.va.semoss.ui.actions.UnmountAction;
import gov.va.semoss.ui.components.PlayPane;
import static gov.va.semoss.ui.components.PlayPane.UIPROGRESS;
import java.awt.event.KeyEvent;

/**
 *
 * @author ryan
 */
public class RepoListMouseListener extends MouseAdapter {

	private static final Logger log = Logger.getLogger(
			RepoListMouseListener.class );

	private final RepositoryList repoList;
	private IEngine opEngine;
	private final ImportLoadingSheetAction importls;
	// private final ImportRtmAction importrtm;
	private final MountAction mounter;
	private final CreateDbAction creater;
	private final PinAction toggler;
	private final PropertiesAction proper;
	private final CloneAction cloner;
	private final ClearAction clearer;
	private final ExportTtlAction exportttl;
	private final ExportTtlAction exportnt;
	private final ExportTtlAction exportrdf;
	private final ExportInsightsAction exportinsights;
	private final ExportLoadingSheetAction expall;
	private final ExportLoadingSheetAction expnodes;
	private final ExportLoadingSheetAction exprels;
	private final DbAction expgraphml;
	private final DbAction expgson;
	private final DbAction expSpecNodes;
	private final DbAction expSpecRels;
	private final UnmountAction unmounter;
	private final EndpointAction sparqler;
	private final JMenu mergeroot;
	private final ImportInsightsAction resetInsights;
	private final ImportInsightsAction importInsights;
	private final CheckConsistencyAction consistencyCheck;

	public RepoListMouseListener( RepositoryList repos ) {
		repoList = repos;
		Frame frame = JOptionPane.getFrameForComponent( repos );
		mounter = new MountAction( PlayPane.UIPROGRESS, frame );
		toggler = new PinAction( PlayPane.UIPROGRESS );
		proper = new PropertiesAction( frame );
		cloner = new CloneAction( PlayPane.UIPROGRESS, frame );
		clearer = new ClearAction( PlayPane.UIPROGRESS, frame );
		exportttl = new ExportTtlAction( PlayPane.UIPROGRESS,
				ExportTtlAction.Style.TTL, frame );
		exportnt = new ExportTtlAction( PlayPane.UIPROGRESS,
				ExportTtlAction.Style.NT, frame );
		exportrdf = new ExportTtlAction( PlayPane.UIPROGRESS,
				ExportTtlAction.Style.RDF, frame );
		unmounter = new UnmountAction( frame );
		sparqler = new EndpointAction( PlayPane.UIPROGRESS, frame );
		expnodes = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				true, false );
		expall = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				true, true );
		exprels = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				false, true );
		expgraphml = new ExportGraphAction( PlayPane.UIPROGRESS, frame, 
				ExportGraphAction.Style.GRAPHML );
		expgson = new ExportGraphAction( PlayPane.UIPROGRESS, frame, 
				ExportGraphAction.Style.GSON );
		expSpecNodes = new ExportSpecificNodesToLoadingSheetAction(
				PlayPane.UIPROGRESS, frame );
		expSpecRels = new ExportSpecificRelationshipsToLoadingSheetAction(
				PlayPane.UIPROGRESS, frame );
		importls = new ImportLoadingSheetAction( PlayPane.UIPROGRESS, frame );
		// importrtm = new ImportRtmAction( PlayPane.UIPROGRESS, frame );
		creater = new CreateDbAction( PlayPane.UIPROGRESS, frame );
		exportinsights = new ExportInsightsAction( PlayPane.UIPROGRESS, frame );
		resetInsights = new ImportInsightsAction( UIPROGRESS, true, frame );
		importInsights = new ImportInsightsAction( UIPROGRESS, false, frame );

		consistencyCheck = new CheckConsistencyAction( UIPROGRESS, frame );

		mergeroot = new JMenu( MERGE );
	}

	@Override
	public void mousePressed( MouseEvent me ) {
		RepositoryListModel model = repoList.getRepositoryModel();

		if ( model.isEmpty() ) {
			me.consume();

			Point p = me.getPoint();

			// figure out if we pressed the "attach db" or "create db" icon
			if ( p.y <= repoList.getFontMetrics( repoList.getFont() ).getHeight() ) {
				mounter.actionPerformed( null );
			}
			else {
				creater.actionPerformed( null );
			}
		}
		else if ( SwingUtilities.isRightMouseButton( me ) ) {
			me.consume();
			Point p = me.getPoint();
			int index = repoList.locationToIndex( p );
			Rectangle rect = repoList.getCellBounds( index, index );

			opEngine = ( rect.contains( p ) ? model.getElementAt( index ) : null );

			JPopupMenu menu = makeMenu( repoList, opEngine );
			menu.show( me.getComponent(), me.getX(), me.getY() );
		}
	}

	private JPopupMenu makeMenu( RepositoryList repoList, final IEngine engine ) {

		JPopupMenu db = new JPopupMenu();

		if ( null != engine ) {
			for ( DbAction dba : new DbAction[]{ toggler, proper, cloner,
				clearer, exportttl, exportnt, exportrdf, exportinsights, importls,
				unmounter, sparqler, mounter, expnodes, exprels, expSpecNodes,
				expSpecRels, expall, creater, resetInsights, importInsights,
				consistencyCheck, expgraphml, expgson } ) {
				dba.setEngine( opEngine );
			}

			db.add( toggler );
			db.add( unmounter );
			//Quality Check
			db.add( consistencyCheck );
			db.addSeparator();
			//Export
			JMenu exptop = new JMenu( "Export" );
			exptop.setToolTipText( "Export Database Activities" );
			exptop.setMnemonic( KeyEvent.VK_E );
			//exptop.add( exportttl );
			//exptop.add( exportnt );
			//exptop.add( exportrdf );
			exptop.setIcon( DbAction.getIcon( "exportdb" ) );

			//db.add( cloneconfer );
			//db.add( clearer );
			//Loading Sheets
			JMenu loadingsheets = new JMenu( "Loading Sheets" );
			loadingsheets.setToolTipText( "Export the Loading Sheets" );
			loadingsheets.setMnemonic( KeyEvent.VK_L );
			loadingsheets.setIcon( DbAction.getIcon( "import_data_review" ) );

			exptop.add( loadingsheets );
			//Semantic Web
			JMenu semsheets = new JMenu( "Semantic Web" );
			semsheets.setToolTipText( "Export the Semantic Web" );
			semsheets.setMnemonic( KeyEvent.VK_S );
			semsheets.setIcon( DbAction.getIcon( "semantic_dataset1" ) );
			exptop.add( semsheets );
			semsheets.add( exportttl );
			semsheets.add( exportnt );
			semsheets.add( exportrdf );
			//Nodes
			JMenu nodes = new JMenu( "Nodes" );
			nodes.setToolTipText( "Export the Nodes" );
			nodes.setMnemonic( KeyEvent.VK_N );
			nodes.setToolTipText( "Export the Nodes" );
			nodes.setIcon( DbAction.getIcon( "protege/individual" ) );

			loadingsheets.add( nodes );
			//Nodes SubMenu
			nodes.add( expnodes );
			nodes.add( expSpecNodes );
			//RelationShips
			JMenu relationS = new JMenu( "Relationships" );
			relationS.setToolTipText( "Export the Relations" );
			relationS.setMnemonic( KeyEvent.VK_R );
			relationS.setIcon( DbAction.getIcon( "relationship1" ) );
			loadingsheets.add( relationS );
			//Relationships SubMenu
			relationS.add( exprels );
			relationS.add( expSpecRels );

			loadingsheets.add( expall );
			exptop.add( exportinsights );
			
			JMenu gexp = new JMenu( "Graph" );
			gexp.add( expgraphml );
			gexp.add( expgson );
			
			exptop.add( gexp );			
			db.add( exptop );

			JMenu importtop = new JMenu( "Import" );
			importtop.setToolTipText( "Import Database Operations" );
			importtop.setMnemonic( KeyEvent.VK_I );

			importtop.setIcon( DbAction.getIcon( "importdb" ) );
			importtop.setToolTipText( "Import Database Operations" );
			importtop.setMnemonic( KeyEvent.VK_I );
			db.add( importtop );
			//JMenu iDatabase = new JMenu( "Database" );
			//iDatabase.setToolTipText("Import Database Operations");
			//iDatabase.setMnemonic(KeyEvent.VK_D);
			//importtop.add( iDatabase );

			mergeroot.setToolTipText( "Merge the Data between databases" );
			mergeroot.setMnemonic( KeyEvent.VK_D );
			mergeroot.setIcon( DbAction.getIcon( "semossjnl" ) );
			mergeroot.setEnabled( false );
			importtop.add( mergeroot );
			importtop.add( importls );

			JMenu insights = new JMenu( "Insights" );
			insights.setToolTipText( "Import Insight Operations" );
			insights.setMnemonic( KeyEvent.VK_I );

			//Ticket #792
			insights.add( importInsights );
			insights.add( resetInsights );
			importtop.add( insights );
			//Insite Manager Icon
			insights.setIcon( DbAction.getIcon( "insight_manager_tab1" ) );
			//importInsights
			db.setToolTipText( "Database operations" );

			db.add( cloner );
			db.add( clearer );
			db.addSeparator();
			db.add( sparqler );
			sparqler.setEnabled( false );

			db.add( proper );
			db.setEnabled( false );

			mergeroot.removeAll();
			mergeroot.setEnabled( repoList.getRepositoryModel().size() > 1 );

			for ( IEngine eng : repoList.getRepositoryModel().getElements() ) {
				if ( !eng.equals( engine ) ) {
					mergeroot.add( new MergeAction( PlayPane.UIPROGRESS, engine, eng,
							JOptionPane.getFrameForComponent( repoList ) ) );
				}
			}
		}

		return db;
	}
}
