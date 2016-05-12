/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * A class to handle ongoing tasks. Tasks can be submitted here, and run in the
 * background (away from the UI thread).
 *
 * @author ryan
 */
public class OperationsProgress extends JPanel {

	public enum HideBehavior {

		ALL_WHEN_EMPTY, BAR_WHEN_EMPTY
	};
	private static final Map<String, OperationsProgress> instances = new HashMap<>();
	private final List<ProgressTask> ops = new ArrayList<>();
	private final JProgressBar bar = new JProgressBar();
	private final JButton more = new JButton( "+" );
	private JPopupMenu popup = new JPopupMenu();
	private HideBehavior hider = HideBehavior.ALL_WHEN_EMPTY;
	private final List<OperationsProgressListener> listeners = new ArrayList<>();

	public static OperationsProgress getInstance( String name ) {
		if ( !instances.containsKey( name ) ) {
			instances.put( name, new OperationsProgress() );
		}
		return instances.get( name );
	}

	public OperationsProgress() {
		super( new BorderLayout() );
		add( bar, BorderLayout.CENTER );
		bar.setIndeterminate( true );
		bar.setStringPainted( true );
		more.setVisible( false );
		add( more, BorderLayout.EAST );

		more.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent me ) {
				if ( popup.isVisible() ) {
					popup.setVisible( false );
				}
				else {
					popup.removeAll();

					for ( ProgressTask t : ops ) {
						JProgressBar smallbar = new JProgressBar();
						smallbar.setStringPainted( true );
						smallbar.setString( t.getLabel() );
						smallbar.setIndeterminate( true );
						popup.add( smallbar );
					}
					popup.pack();
					popup.show( bar, bar.getX(), bar.getY() + bar.getHeight() );
				}
			}
		} );

		bar.setVisible( false );
	}

	public void add( final ProgressTask task ) {
		if ( null == task ) {
			return;
		}

		ops.add( task );
		refreshProgress();

		if ( !isVisible() ) {
			setVisible( true );
		}

		if ( !bar.isVisible() ) {
			bar.setVisible( true );
		}

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				task.runOp();
				return null;
			}

			@Override
			protected void done() {
				ops.remove( task );
				task.done();
				refreshProgress();

				for ( OperationsProgressListener opl : listeners ) {
					opl.taskComplete( task );
				}
			}
		};

		sw.execute();
	}

	private void refreshProgress() {
		if ( popup.isVisible() ) {
			popup.setVisible( false );
		}

		switch ( ops.size() ) {
			case 0:
				if ( HideBehavior.ALL_WHEN_EMPTY == hider ) {
					setVisible( false );
				}
				else if ( HideBehavior.BAR_WHEN_EMPTY == hider ) {
					bar.setVisible( false );
				}
				setToolTipText( "No tasks are running" );

				more.setVisible( false );
				break;
			case 1:
				bar.setString( ops.get( 0 ).getLabel() );
				more.setVisible( false );
				setToolTipText( bar.getString() );
				bar.setToolTipText( getToolTipText() );
				break;
			default:
				bar.setString( ops.size() + " tasks running" );
				more.setVisible( true );
				setToolTipText( bar.getString() );
				bar.setToolTipText( getToolTipText() );
		}
	}

	public void addOperationsProgressListener( OperationsProgressListener opl ) {
		listeners.add( opl );
	}

	public void removeOperationsProgressListener( OperationsProgressListener opl ) {
		listeners.remove( opl );
	}

	/**
	 * Should the progress bar be hidden when no tasks are running?
	 *
	 * @param hb how to handle an empty task list
	 */
	public void setHideBehavior( HideBehavior hb ) {
		hider = hb;
	}

	public static interface OperationsProgressListener {

		public void taskComplete( ProgressTask pt );
	}
}
