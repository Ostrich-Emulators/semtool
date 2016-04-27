/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

import com.ostrichemulators.semtool.ui.components.OperationsProgress.OperationsProgressListener;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;

/**
 *
 * @author ryan
 */
public class StatusBar extends JPanel {

	private final OperationsProgress opprog;
	private final JLabel message;
	private final List<String> messages = new ArrayList<>();
	private int limit = 50;
	private final Timer timer;

	public StatusBar() {
		this( "UI" );
	}

	public StatusBar( String progressId ) {
		super( new BorderLayout() );

		ActionListener disappearer = new ActionListener() {
			private int skips = 0;

			@Override
			public void actionPerformed( ActionEvent ae ) {
				// skip the first X actions (give the user a little time to read the status)
				if ( skips < 35 ) {
					skips++;
					return;
				}

				Color c = message.getForeground();
				int alpha = c.getAlpha();
				if ( 0 == alpha ) {
					// we're totally hidden, so stop our timer
					timer.stop();
					skips = 0;
				}
				else {
					alpha = ( alpha < 10 ? 0 : alpha - 10 );
					Color newcol
							= new Color( c.getRed(), c.getGreen(), c.getBlue(), alpha );
					message.setForeground( newcol );
				}
			}
		};
		timer = new Timer( 100, disappearer );

		message = new JLabel( "starting up..." );
		message.setHorizontalAlignment( SwingConstants.RIGHT );
		message.setVerticalAlignment( SwingConstants.CENTER );
		Border lowered = BorderFactory.createBevelBorder( BevelBorder.LOWERED );
		Border padding = BorderFactory.createEmptyBorder( 0, 0, 0, 4 );
		message.setBorder( BorderFactory.createCompoundBorder( lowered, padding ) );
		//message.setBorder( padding );

		addMessageListener();

		opprog = OperationsProgress.getInstance( progressId );
		opprog.setHideBehavior( OperationsProgress.HideBehavior.BAR_WHEN_EMPTY );
		opprog.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );

		JSplitPane splitter
				= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, message, opprog );
		splitter.setDividerSize( 3 );
		splitter.setDividerLocation( 0.50 );
		add( splitter );

		message.setPreferredSize( new Dimension( 800, 20 ) );
		opprog.setPreferredSize( new Dimension( 200, 20 ) );

		opprog.addOperationsProgressListener(new OperationsProgressListener() {
			@Override
			public void taskComplete( ProgressTask pt ) {
				addStatus("Completed: " + pt.getLabel()
						+ " [in " + Utility.getDuration( pt.getStartTime(), pt.getStopTime() ) + "]" );
			}
		} );

		setMinimumSize( new Dimension( 150, 20 ) );
	}

	public OperationsProgress getTasker() {
		return opprog;
	}

	public void addStatus( String raw ) {
		if ( messages.size() > limit ) {
			messages.remove( messages.size() - 1 );
		}

		SimpleDateFormat SDF = new SimpleDateFormat( "(hh:mm:ss a) " );
		String msg = SDF.format( new Date() ) + raw;
		messages.add( msg );
		message.setText( msg );
		Color c = message.getForeground();
		Color newcol = new Color( c.getRed(), c.getGreen(), c.getBlue(), 255 );
		message.setForeground( newcol );
		timer.restart();
	}

	public void setMessageLimit( int lim ) {
		limit = lim;

		// clean up the messages
		int msz = messages.size();
		if ( msz > limit ) {
			ListIterator<String> li = messages.listIterator( limit );
			while ( li.hasNext() ) {
				li.next();
				li.remove();
			}
		}
	}

	public int getMessageLimit() {
		return limit;
	}

	private void addMessageListener() {
		message.addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed( MouseEvent e ) {
				JPopupMenu pop = new JPopupMenu();
				for ( String msg : messages ) {
					pop.add( msg );
				}
				pop.show( message, e.getX(), e.getY() );
			}
		} );
	}
}
