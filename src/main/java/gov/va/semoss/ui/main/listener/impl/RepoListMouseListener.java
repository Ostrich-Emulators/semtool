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
	private final ExportLoadingSheetAction exporterwhole;
	private final ExportLoadingSheetAction exporternodes;
	private final ExportLoadingSheetAction exporterrels;
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
		exporternodes = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				true, false );
		exporterwhole = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				true, true );
		exporterrels = new ExportLoadingSheetAction( PlayPane.UIPROGRESS, frame,
				false, true );
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

		JPopupMenu popup = new JPopupMenu();

		if ( null != engine ) {
			for ( DbAction dba : new DbAction[]{ toggler, proper, cloner,
				clearer, exportttl, exportnt, exportrdf, exportinsights, importls,
				unmounter, sparqler, mounter, exporternodes, exporterrels, expSpecNodes,
				expSpecRels, exporterwhole, creater, resetInsights, importInsights,
				consistencyCheck } ) {
				dba.setEngine( opEngine );
			}

			popup.add( toggler );
			popup.add( proper );
			popup.addSeparator();

			popup.add( cloner );
			popup.add( clearer );
			popup.add( consistencyCheck );

			JMenu exptop = new JMenu( "Export" );
			exptop.add( exportttl );
			exptop.add( exportnt );
			exptop.add( exportrdf );
			JMenu loadingsheets = new JMenu( "Loading Sheets" );
			exptop.setIcon( DbAction.getIcon( "exportdb" ) );
			exptop.add( loadingsheets );
			loadingsheets.add( exporternodes );
			loadingsheets.add( expSpecNodes );
			loadingsheets.add( exporterrels );
			loadingsheets.add( expSpecRels );
			loadingsheets.add( exporterwhole );
			exptop.add( exportinsights );
			popup.add( exptop );

			JMenu importtop = new JMenu( "Import" );
			importtop.setIcon( DbAction.getIcon( "importdb" ) );
			popup.add( importtop );
			importtop.add( importls );
			JMenu insights = new JMenu( "Insights" );
			insights.add( resetInsights );
			insights.add( importInsights );
			importtop.add( insights );

			popup.add( mergeroot );
			mergeroot.removeAll();
			mergeroot.setEnabled( repoList.getRepositoryModel().size() > 1 );

			for ( IEngine eng : repoList.getRepositoryModel().getElements() ) {
				if ( !eng.equals( engine ) ) {
					mergeroot.add( new MergeAction( PlayPane.UIPROGRESS, engine, eng,
							JOptionPane.getFrameForComponent( repoList ) ) );
				}
			}

			popup.add( unmounter );

			if ( engine.isServerSupported() ) {
				popup.add( sparqler );
			}

			popup.addSeparator();
		}

		popup.add( mounter );
		popup.add( creater );

		return popup;
	}
}
