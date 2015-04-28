/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.rdf.engine.util.EngineConsistencyChecker;
import gov.va.semoss.rdf.engine.util.EngineConsistencyChecker.Hit;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.CheckWhatPanel;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.playsheets.ConsistencyPlaySheet;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.MultiMap;

import java.awt.HeadlessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class CheckConsistencyAction extends DbAction {

	private static final Logger log = Logger.getLogger( CheckConsistencyAction.class );
	private static final int WARNING_LIMIT = 1500;
	private final Frame frame;
	private final CheckWhatPanel checkwhat = new CheckWhatPanel( true, true );

	public CheckConsistencyAction( String optg, Frame frame ) {
		super( optg, CONSISTENCYCHECK, "conformance-check" );
		this.frame = frame;
		putValue( AbstractAction.SHORT_DESCRIPTION, "Check Database Consistency" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_Q );
	}

	@Override
	public boolean preAction( ActionEvent ae ) {
		checkwhat.setEngine( getEngine() );
		int retval = JOptionPane.showConfirmDialog( frame, checkwhat,
				"Check what data in " + getEngineName() + "?",
				JOptionPane.OK_CANCEL_OPTION );
		return ( JOptionPane.OK_OPTION == retval );
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		String title = "Checking Consistency of " + getEngineName();
		ProgressTask pt = new ProgressTask( title, new Runnable() {
			@Override
			public void run() {
				List<URI> nodes = new ArrayList<>();
				List<URI> rels = new ArrayList<>();

				try {
					EngineConsistencyChecker checker = new EngineConsistencyChecker( getEngine(),
							checkwhat.isCheckAcrossSelected(), checkwhat.getDistanceAlg() );

					if ( checkwhat.isNodesSelected() ) {
						nodes.addAll( checkwhat.getSelectedNodes() );
						checker.add( nodes, EngineConsistencyChecker.Type.CONCEPT );
					}
					if ( checkwhat.isRelsSelected() ) {
						rels.addAll( checkwhat.getSelectedRelations() );
						checker.add( rels, EngineConsistencyChecker.Type.RELATIONSHIP );
					}

					// it's easier if we just add everything to the same list
					nodes.addAll( rels );
					rels.clear();

					// things will melt down if we have lots of one type of item,
					// or if we're checking across all types. Warn the user just in case
					boolean across = checkwhat.isCheckAcrossSelected();
					int checklimit = 0;
					for ( URI uri : nodes ) {
						int pos = checker.getItemsForType( uri );
						if ( across ) {
							checklimit += pos;
						}
						else if ( pos > checklimit ) {
							checklimit = pos;
						}
					}

					if ( checklimit > WARNING_LIMIT ) {
						int ans = JOptionPane.showConfirmDialog( frame,
								"This is a large dataset, and may take considerable time to resolve."
								+ "\nContinue anyway?", "Lots of Data", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE );
						if ( JOptionPane.YES_OPTION != ans ) {
							return;
						}
					}

					PlaySheetFrame psf = null;

					Map<URI, String> labels = Utility.getInstanceLabels( nodes, getEngine() );
					int progressPer = 100 / nodes.size();
					int hiddenProgress = 0;
					for ( URI concept : nodes ) {
						int count = checker.getItemsForType( concept );
						String msg = labels.get( concept ) + " (" + count + " items)";
						log.debug( "checking " + msg );

						if ( null == psf ) {
							// since we don't show the frame until we have something to display,
							// we need to keep track of how much progress we've made but
							// haven't shown
							hiddenProgress += progressPer;
						}
						else {
							psf.addProgress( "Checking " + msg, progressPer + hiddenProgress );
							hiddenProgress = 0; // nothing to hide anymore
						}

						MultiMap<URI, Hit> hits = checker.check( concept, 0.7f );
						if ( !hits.isEmpty() ) {
							if ( null == psf ) {
								psf = new PlaySheetFrame( getEngine() );
								psf.setTitle( "Consistency Check Results" );
								DIHelper.getInstance().getDesktop().add( psf );
							}

							ConsistencyPlaySheet cps
									= new ConsistencyPlaySheet( concept, hits, labels, getEngine() );
							cps.setThreshhold( checkwhat.getPercentage() );
							psf.addTab( cps );
						}
					}

					if ( null == psf ) {
						// never showed anything...tell the user
						JOptionPane.showMessageDialog( frame, "No Consistency Errors Detected",
								"Consistency Results", JOptionPane.INFORMATION_MESSAGE );
					}
					else {
						psf.hideProgress();
					}
					checker.release();
				}
				catch ( IOException | HeadlessException e ) {
					log.error( e, e );
				}
			}
		} );

		return pt;
	}
}
