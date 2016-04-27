/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.ui.actions.DbAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author ryan
 */
public class CloseableTab extends JPanel implements ActionListener {

	public static enum MarkType {

		ERROR, CHECK, NONE
	};
	private final JTabbedPane tabs;
	private final JLabel mark;
	private static final Icon blank = DbAction.getIcon( "blank" );
	private static final Icon closeit = DbAction.getIcon( "close" );

	public CloseableTab( JTabbedPane parent ) {
		super( new BorderLayout() );
		tabs = parent;

		mark = new JLabel( " " );
		mark.setHorizontalTextPosition( SwingConstants.RIGHT );
		mark.setIconTextGap( 2 );

		JLabel lbl = new JLabel() {
			@Override
			public String getText() {
				int i = tabs.indexOfTabComponent( CloseableTab.this );
				if ( i != -1 ) {
					return tabs.getTitleAt( i );
				}
				return null;
			}
		};

		JButton closer = new JButton( blank );
		closer.setPreferredSize( new Dimension( 16, 16 ) );
		closer.setMargin( new Insets( 0, 0, 0, 0 ) );
		closer.setToolTipText( "Close this tab" );
		closer.addActionListener( this );

		add( mark, BorderLayout.WEST );
		add( lbl, BorderLayout.CENTER );
		add( closer, BorderLayout.EAST );

		setOpaque( false );
		mark.setOpaque( false );
		lbl.setOpaque( false );
		closer.setOpaque( false );

		//Make the button looks the same for all Laf's
		closer.setUI( new BasicButtonUI() );
		//Make it transparent
		closer.setContentAreaFilled( false );
		//No need to be focusable
		closer.setFocusable( false );
		closer.setBorder( BorderFactory.createEtchedBorder() );
		closer.setBorderPainted( false );
		//Making nice rollover effect
		closer.setRolloverEnabled( true );
		closer.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseEntered( MouseEvent e ) {
				closer.setBorderPainted( true );
				closer.setIcon( closeit );
			}

			@Override
			public void mouseExited( MouseEvent e ) {
				closer.setBorderPainted( false );
				closer.setIcon( parent.getSelectedIndex()
						== parent.indexOfTabComponent( CloseableTab.this ) ? closeit : blank );
			}
		} );

		parent.addChangeListener( new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e ) {
				closer.setIcon( parent.getSelectedIndex()
						== parent.indexOfTabComponent( CloseableTab.this ) ? closeit : blank );
			}
		} );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		int tabpos = tabs.indexOfTabComponent( this );
		tabs.remove( tabpos );
	}

	public void setMark( MarkType t ) {
		switch ( t ) {
			case ERROR:
				setIcon( DbAction.getIcon( "error" ) );
				break;
			case CHECK:
				setIcon( DbAction.getIcon( "check" ) );
				break;
			case NONE:
				setIcon( null );
				break;
			default:
				throw new IllegalArgumentException( "cannot handle mark type:" + t );
		}
	}

	public void setIcon( Icon ic ) {
		mark.setIcon( ic );
	}
}
