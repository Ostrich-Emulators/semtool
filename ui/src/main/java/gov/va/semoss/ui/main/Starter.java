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
import java.io.File;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import gov.va.semoss.ui.components.PlayPane;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.util.PinningEngineListener;
import gov.va.semoss.util.Utility;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.Painter;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The Starter class is run to start the SEMOSS application. This launches the
 * Splash Screen and the base user interface.
 */
public class Starter {

	final Object monitor = new Object();

	/**
	 * Method main. Starts the SEMOSS application. read the properties file -
	 * DBCM_RDF_Map.Prop Creates the PlayPane 1. Read the perspective properties
	 * to get all the perspectives. 2. For each of the perspectives, read all the
	 * question numbers. 3. For each question, get the description. 4. Convert
	 * this into a 2 dimensional Hashtable Hash1 - Perspective Questions, Hash2
	 * for each question description and layout classes 5. Set this information
	 * into the util DIHelper class 6. Populate the perspective combo-boxes with
	 * all the information retrieved on perspectives 7. Create the User Interface.
	 *
	 * @param args String[] - the Main method.
	 * @throws java.lang.Exception
	 */
	public static void main( String[] args ) throws Exception {

		final Logger logger = Logger.getLogger( Starter.class );

		final String WORKINGDIR = System.getProperty( "user.dir" );
		final File propFile = new File( WORKINGDIR, "RDF_Map.prop" );

		final EngineUtil engineutil = EngineUtil.getInstance();
		new Thread( engineutil ).start();

		// Nimbus me
		try {
			for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
				if ( "Nimbus".equals( info.getName() ) ) {
					logger.info( "Got the nimbus" );
					UIManager.setLookAndFeel( info.getClassName() );
					//Pretty Colors.
					UIManager.put( "nimbusSelectionBackground", new Color( 67, 144, 212 ) ); //Light blue for selection
					UIManager.put( "controlDkShadow", new Color( 100, 100, 100 ) ); //Color of scroll icons arrows
					UIManager.put( "controlHighlight", new Color( 100, 100, 100 ) ); //Color of scroll icons highlights 
					UIManager.put( "ProgressBar.repaintInterval", 250 );//speed of indeterminate progress bar
					UIManager.put( "ProgressBar.cycleTime", 1300 );

					UIDefaults defaults = UIManager.getLookAndFeelDefaults();

					UIDefaults tabPaneDefaults = new UIDefaults();
					tabPaneDefaults.put( "TabbedPane.background", new ColorUIResource( Color.red ) );

					//defaults.put( "ToolTip.background", Color.LIGHT_GRAY );
					//defaults.put( "ToolTip[Enabled].backgroundPainter", null );
					//defaults.put( "ToolTip.contentMargins", new Insets( 3, 3, 3, 3 ) );
					defaults.put( "nimbusOrange", defaults.get( "nimbusInfoBlue" ) );
					Painter blue = (Painter) defaults.get( "Button[Default+Focused+Pressed].backgroundPainter" );
					defaults.put( "ProgressBar[Enabled].foregroundPainter", blue );
					break;
				}
			}
		}
		catch ( ClassNotFoundException | InstantiationException |
				IllegalAccessException | UnsupportedLookAndFeelException e ) {
			// handle exception
			logger.warn( e, e );
		}

		// load order: semoss properties, vcamp properties, RDF_Map prop (if exists)
		Properties props = DIHelper.getInstance().getCoreProp();
		props.load( Starter.class.getResourceAsStream( "/semoss.properties" ) );

		File configs[] = { new File( WORKINGDIR, "semoss.properties" ), propFile };
		for ( File f : configs ) {
			if ( f.exists() ) {
				try ( Reader r = new FileReader( f ) ) {
					logger.debug( "reading properties from " + f );
					Properties fsprops = new Properties();
					fsprops.load( r );
					Utility.mergeProperties( props, fsprops, false, null );
				}
				catch ( IOException ioe ) {
					logger.error( ioe, ioe );
				}
			}
		}

		File baseFolderCheckFile
				= new File( DIHelper.getInstance().getProperty( Constants.BASE_FOLDER ) );
		if ( !( baseFolderCheckFile.exists() && baseFolderCheckFile.isDirectory() ) ) {
			DIHelper.getInstance().putProperty( Constants.BASE_FOLDER, WORKINGDIR );
		}

		if ( null != DIHelper.getInstance().getProperty( "LOG4J" ) ) {
			File logCheckFile = new File( DIHelper.getInstance().getProperty( "LOG4J" ) );
			if ( logCheckFile.exists() ) {
				PropertyConfigurator.configure( logCheckFile.toString() );
			}
		}

		final PlayPane frame = new PlayPane();
		final SplashScreen ss = new SplashScreen();
		java.awt.EventQueue.invokeLater( new Runnable() {
			@Override
			public void run() {
				try {
					frame.start();
				}
				catch ( Exception e ) {
					logger.error( "error connecting the frame", e );
				}
				frame.setVisible( true );
			}
		} );

		// don't let the splash screen go for more than 10 seconds
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
		scheduler.schedule( new Runnable() {

			@Override
			public void run() {
				if ( ss.isVisible() ) {
					ss.setVisible( false );
					ss.dispose();
				}
			}
		}, 10, TimeUnit.SECONDS );

		final RepositoryList repolist = frame.getRepoList();
		repolist.addListSelectionListener( new ListSelectionListener() {

			@Override
			public void valueChanged( ListSelectionEvent e ) {
				// close the splash screen
				ss.setVisible( false );
				ss.dispose();
			}
		} );

		// if we're given an SMSS file to start with, don't bother with the 
		// file watcher, just open that repository
		if ( args.length > 0 ) {
			// we have some smss files on the command line
			logger.info( "only opening command line engines" );
			for ( String smss : args ) {
				File smssfile = new File( smss );
				try {
					engineutil.mount( smssfile, true );
				}
				catch ( EngineManagementException ioe ) {
					logger.error( "cannot open repository smss/dir: "
							+ smss + " (code: " + ioe.getCode() + ")", ioe );
				}
			}
		}
		else {
			String ext = DIHelper.getInstance().getProperty( Constants.ENGINE_EXT );
			PinningEngineListener watcherInstance = new PinningEngineListener();
			engineutil.addEngineOpListener( watcherInstance );
			watcherInstance.setExtensions( Arrays.asList( ext.split( ";" ) ) );
			watcherInstance.loadFirst();
		}
	}
}
