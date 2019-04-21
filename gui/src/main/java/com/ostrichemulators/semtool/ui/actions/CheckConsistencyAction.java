/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.rdf.engine.util.EngineConsistencyChecker;
import com.ostrichemulators.semtool.rdf.engine.util.EngineConsistencyChecker.Hit;
import com.ostrichemulators.semtool.ui.components.CheckConsistencyPanel;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.playsheets.ConsistencyPlaySheet;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.Utility;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.URI;

/**
 *
 * @author ryan
 */
public class CheckConsistencyAction extends DbAction {
	private static final long serialVersionUID = -5683699811295003443L;
	private static final Logger log = Logger.getLogger( CheckConsistencyAction.class );

	private static final int WARNING_LIMIT = 1500;
	private final Frame frame;
	private final CheckConsistencyPanel checkwhat = new CheckConsistencyPanel( true, true );

	public CheckConsistencyAction( String optg, Frame frame ) {
		super( optg, CONSISTENCYCHECK, "conformance-check" );
		this.frame = frame;
		putValue( AbstractAction.SHORT_DESCRIPTION, "Check Database Consistency" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_Q );
	}

	private boolean buildQuestionFrame() {
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
				if (!buildQuestionFrame())
					return;
				
				checkConsistency();
			}
		} );

		return pt;
	}
	
	private void checkConsistency() {
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
		catch ( HeadlessException e ) {
			log.error( e, e );
		}
	}
}
