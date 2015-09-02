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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * This generates a Splash Screen and progress bar when the SEMOSS application
 * is initially opened.
 */
public class SEMOSSSplashScreen extends Frame {

	/**
	 * The logger for this class
	 */
	private static final Logger log = Logger.getLogger( SEMOSSSplashScreen.class );
	/**
	 * The bounds of the Splash Screen
	 */
	private static Dimension splashScreenSize;
	/**
	 * The image that occupies most of the real estate of the splash screen
	 */
	private static BufferedImage image = null;
	/**
	 * The height of the area reserved for a String displayed at the bottom of the
	 * SplashScreen, indicating what is happening, or other info
	 */
	private static final int MESSAGE_GUTTER_HEIGHT = 30;
	/**
	 * The height of the progress bar area
	 */
	private static final int PROGRESS_GUTTER_HEIGHT = 10;
	/**
	 * The color of the Splash Screen background
	 */
	private static final Color BACKGROUND_COLOR = new Color( 0, 0, 0 );
	/**
	 * The color of the progress bar
	 */
	private static final Color PROGRESS_COLOR = new Color( 255, 0, 0 );
	/**
	 * The color of the text in the message area
	 */
	private static final Color TEXT_COLOR = new Color( 255, 255, 255 );
	/**
	 * The font used to display the text in the message area
	 */
	private static final Font font = new Font( "TimesRoman", Font.BOLD + Font.ITALIC, 14 );
	/**
	 * The number of rendering cycles that have passed for this Splash Screen
	 */
	private static int renderingCycles = 0;
	/**
	 * The percentage of loading completion, from 0.0 (0%) to 1.0 (100%)
	 */
	private static float percentComplete = 0.0f;

	/**
	 * Default constructor
	 *
	 * @param imagePath The path of the internal resource image used for this
	 * splash screen
	 */
	public SEMOSSSplashScreen( String imagePath ) {
		// Get the graphics context for this screen
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		// Get the image
		try {
			URL url = SEMOSSSplashScreen.class.getResource( imagePath );
			if ( url != null ) {
				image = ImageIO.read( url.openStream() );
			}
		}
		catch ( IOException e ) {
			log.error( e );
		}
		if ( image == null ) {
			log.warn( "Could not find resource: " + imagePath
					+ ", using a blank image for splash screen" );
			image = new BufferedImage( 20, 20, BufferedImage.TYPE_INT_RGB );
		}

		// Figure out how large the splash should be, based on the image and the current screen size
		Dimension currentScreenSize = new Dimension();
		currentScreenSize.width = gd.getDisplayMode().getWidth();
		currentScreenSize.height = gd.getDisplayMode().getHeight();
		// If the screen size is small, use it to determine the splash screen dimensions
		int imageHeight = image.getHeight();
		int imageWidth = image.getWidth();
		if ( imageWidth > currentScreenSize.width && imageHeight > currentScreenSize.height ) {
			splashScreenSize = new Dimension( currentScreenSize.width / 2,
					currentScreenSize.height / 2 + PROGRESS_GUTTER_HEIGHT + MESSAGE_GUTTER_HEIGHT );
		}
		// Otherwise, use the image size to establish the splash screen bounds
		else {
			splashScreenSize = new Dimension( imageWidth,
					imageHeight + PROGRESS_GUTTER_HEIGHT + MESSAGE_GUTTER_HEIGHT );
		}
		// Derive the center of the splash screen
		int splashOriginX = ( currentScreenSize.width - splashScreenSize.width ) / 2;
		int splashOriginY = ( currentScreenSize.height - splashScreenSize.height ) / 2;
		// Make this a plain frame
		setUndecorated( true );
		// Set the size and location of the frame
		setSize( splashScreenSize );
		setLocation( splashOriginX, splashOriginY );
		toFront();
		//setAlwaysOnTop( true );
		//pack();
		validate();
	}

	/**
	 * Show the Splash Screen
	 *
	 * @param x The x coordinate of the screen on the screen
	 * @param y The y coordinate of the screen on the screen
	 */
	public void displaySplashScreen() {
		SplashRunner runner = new SplashRunner();
		runner.start();
	}

	@Override
	public void paintComponents( Graphics graphics ) {
		// Paint the parent components
		super.paintComponents( graphics );
		// The first two rendering cycles are enough to paint the main
		// components
		if ( renderingCycles < 2 ) {
			graphics.setFont( font );
			graphics.setColor( BACKGROUND_COLOR );
			graphics.fillRect( 0, 0, splashScreenSize.width, splashScreenSize.height );
			//graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			graphics.drawImage( image, 0, 0, splashScreenSize.width, 
					splashScreenSize.height - MESSAGE_GUTTER_HEIGHT, null );
			graphics.setColor( TEXT_COLOR );
			graphics.drawString( "\u00A9 Distributed under the GNU General Public License",
					100, PROGRESS_GUTTER_HEIGHT + splashScreenSize.height - MESSAGE_GUTTER_HEIGHT + 6 );
		}
		// The other cycles serve to paint the progress bar
		drawProgress( graphics );
		renderingCycles++;
	}

	/**
	 * Utility method to derive the percentage of "loading" that is complete
	 *
	 * @return A percentage from 0.0 to 1.0
	 */
	private float getPercentageComplete() {
		// For now, just increment it
		// TODO offer an interface to other objects to set this 
		// in a more intelligent way
		percentComplete += 0.01f;
		return percentComplete;
	}

	/**
	 * Draws the progress bar
	 *
	 * @param graphics The current graphics context
	 */
	private void drawProgress( Graphics graphics ) {
		int accomplished = (int) Math.floor( (double) ( (float) splashScreenSize.width ) * percentComplete );
		int remaining = splashScreenSize.width - accomplished;
		graphics.setColor( PROGRESS_COLOR );
		graphics.fillRect( 0, splashScreenSize.height - PROGRESS_GUTTER_HEIGHT - MESSAGE_GUTTER_HEIGHT, splashScreenSize.width, PROGRESS_GUTTER_HEIGHT );
		graphics.setColor( BACKGROUND_COLOR );
		graphics.fillRect( accomplished, splashScreenSize.height - PROGRESS_GUTTER_HEIGHT - MESSAGE_GUTTER_HEIGHT, remaining, PROGRESS_GUTTER_HEIGHT );
	}
	
	private class SplashRunner extends Thread {
		
		@Override
		public void run(){
			setVisible( true );
			Graphics graphics = getGraphics();
			if ( graphics == null ) {
				log.error( "Graphics for splash screen are null" );
			}
			while ( percentComplete < 1.0f ) {
				percentComplete = getPercentageComplete();
				paintComponents( graphics );
				try {
					Thread.sleep( 30 );
				}
				catch ( InterruptedException e ) {
					log.error( "Graphics for splash screen are null" );
				}
			}
			setVisible( false );
		}
	}
}
