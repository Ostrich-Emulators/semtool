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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.apache.log4j.Logger;

import gov.va.semoss.util.Utility;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * This generates a Splash Screen and progress bar when the SEMOSS application
 * is initially opened.
 */
public class SEMOSSSplashScreen extends Frame {

	private static final Logger log = Logger.getLogger( SEMOSSSplashScreen.class );

	//private final SplashScreen splashScreen;
	
	private static final JProgressBar progressBar = new JProgressBar();
	
	private static Graphics graphics;
	
	private static Dimension splashScreenSize;
	
	private static int splashOriginX = 0;
	
	private static int splashOriginY = 0;
	
	private static BufferedImage image = null;
	
	private static final int MESSAGE_GUTTER_HEIGHT = 30;
	
	private static final Color BACKGROUND_COLOR = new Color(0,0,0);
	
	private static final Color TEXT_COLOR = new Color(255,255,255);
	
	private static final Font font = new Font("TimesRoman", Font.BOLD + Font.ITALIC, 14);
	
	private int renderingCycles = 0;
	
	private boolean finished = true;

	public SEMOSSSplashScreen(String[] imagePaths){
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		Dimension currentScreenSize = new Dimension();
		currentScreenSize.width =  gd.getDisplayMode().getWidth();
		currentScreenSize.height = gd.getDisplayMode().getHeight();
		
		// Make the dimensions of our splash screen 50% of the screen size, for both x and y
		
		splashScreenSize = new Dimension(currentScreenSize.width/2, currentScreenSize.height/2 + MESSAGE_GUTTER_HEIGHT);
		splashOriginX = splashScreenSize.width/2;
		splashOriginY = splashScreenSize.height/2;


		// Get the image
		try {
			for (String path : imagePaths){
				URL url = SEMOSSSplashScreen.class.getResource(path);
				if (url != null){
					image = ImageIO.read(url.openStream());
					break;
				}
				if (image != null){
					break;
				}
			}
		} catch (IOException e) {
			log.error(e);
		}
		if (image == null ) {
			log.warn( "using a blank image for splash screen" );
			image = new BufferedImage( 20, 20, BufferedImage.TYPE_INT_RGB );
		}
		displaySplashScreen(currentScreenSize);
	}
	
	public void displaySplashScreen(Dimension currentScreenSize){
		this.setUndecorated(true);
		this.setSize(splashScreenSize);
		this.setLocation(splashOriginX, splashOriginY);
		this.toFront();
		toFront();
		validate();
		setVisible(true);
		graphics = this.getGraphics();
		if (graphics == null) {
			log.error("Graphics for splash screen are null");
		}
		for(int i=0; i<100; i++) {
			this.paintComponents(graphics);
			try {
				Thread.sleep(60);
			}
			catch(InterruptedException e) {
				log.error("Graphics for splash screen are null");
			}
		}
		// Now for the tasks
		while(!finished) {
			this.paintComponents(graphics);
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				log.error("Graphics for splash screen are null");
			}
		}
		setVisible(false);
	}
	
	@Override
	public void paintComponents(Graphics graphics){
		super.paintComponents(graphics);
		if (renderingCycles < 2){
			graphics.setFont(font);
			graphics.setColor(BACKGROUND_COLOR);
			graphics.fillRect(0, 0, splashScreenSize.width, splashScreenSize.height);
			//graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			graphics.drawImage(image, 0, 0, splashScreenSize.width, splashScreenSize.height - MESSAGE_GUTTER_HEIGHT, null);
			graphics.setColor(TEXT_COLOR);		
        	graphics.drawString("\u00A9 Distributed under the GNU General Public License", 50, splashScreenSize.height - 4);
		}
        renderingCycles++;
	}	
}