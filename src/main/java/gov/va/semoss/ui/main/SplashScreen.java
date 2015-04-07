/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.apache.log4j.Logger;

import gov.va.semoss.util.Utility;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * This generates a Splash Screen and progress bar when the SEMOSS application
 * is initially opened.
 */
public class SplashScreen extends JWindow {

	private static final Logger log = Logger.getLogger( SplashScreen.class );

	private static final JProgressBar progressBar = new JProgressBar();
	private static int count;

	/**
	 * Constructor for SplashScreen.
	 */
	public SplashScreen() {
		JPanel pnl = new JPanel( new BorderLayout() );
		pnl.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		JPanel bottom = new JPanel( new BorderLayout() );

		BufferedImage image = Utility.loadImage( "V-CAMP-Splash.png" );
		if ( null == image ) {
			log.warn( "using a blank image for splash screen" );
			// something bad has happened, but carry on anyway
			image = new BufferedImage( 20, 20, BufferedImage.TYPE_INT_RGB );
		}

		JLabel picLabel = new JLabel( new ImageIcon( image ) );
		pnl.add( picLabel, BorderLayout.CENTER );
		pnl.add( bottom, BorderLayout.SOUTH );

		progressBar.setUI( new MyProgressUI() );
		progressBar.setForeground( Color.blue );
		progressBar.setMaximum( 10 );
		progressBar.setIndeterminate( true );
		progressBar.setBorder( BorderFactory.createEmptyBorder( 0, 150, 5, 150 ) );

		JLabel lblLicense = new JLabel( "\u00A9 Distributed under the GNU General Public License" );
		lblLicense.setHorizontalAlignment( JLabel.CENTER );
		bottom.add( progressBar, BorderLayout.NORTH );
		bottom.add( lblLicense, BorderLayout.SOUTH );
		lblLicense.setBorder( BorderFactory.createEmptyBorder( 15, 0, 15, 0 ) );

		getContentPane().add( pnl );
		pack();

		setSize( 660, 385 );
		setLocationRelativeTo( null );
		getContentPane().requestFocus();
		setVisible( true );
	}

	private class MyProgressUI extends BasicProgressBarUI {

		/**
		 * Method paintIndeterminate.
		 *
		 * @param g Graphics
		 * @param c JComponent
		 */
		@Override
		protected void paintIndeterminate( Graphics g, JComponent c ) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			Rectangle r = getBox( new Rectangle() );
			g.setColor( progressBar.getForeground() );
			g.fillRect( r.x, r.y, r.width, r.height );
		}
	}
}
