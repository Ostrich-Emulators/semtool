package gov.va.semoss.ui.components;

import gov.va.semoss.ui.components.renderers.PlaySheetRenderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import aurelienribon.ui.css.Style;

import com.ibm.icu.util.StringTokenizer;

import gov.va.semoss.om.Insight;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.main.listener.impl.SubmitSparqlQueryListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.util.Utility;
import gov.va.semoss.tabbedqueries.TabbedQueries;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JDesktopPane;
import javax.swing.event.InternalFrameEvent;

/**
 * Class to move the "Custom Sparql Query" window and related controls out of
 * "PlayPane.java".
 *
 * @author Thomas
 *
 */
public class CustomSparqlPanel extends JPanel {

	public JComboBox<String> playSheetComboBox;
	public JButton btnGetQuestionSparql, btnShowHint, btnSubmitSparqlQuery;
	public JCheckBox appendSparqlQueryChkBox;
	public JCheckBox mainTabOverlayChkBox;
	public TabbedQueries sparqlArea;
	private static final Logger logger = Logger.getLogger( CustomSparqlPanel.class );

	public CustomSparqlPanel() {
		setLayout( new GridBagLayout() );

		Style.registerTargetClassName( btnGetQuestionSparql, ".standardButton" );
		Style.registerTargetClassName( btnShowHint, ".standardButton" );
		Style.registerTargetClassName( btnSubmitSparqlQuery, ".standardButton" );

		JLabel lblSectionCCustomize = new JLabel( "Custom SPARQL Query" );
		lblSectionCCustomize.setForeground( Color.DARK_GRAY );
		lblSectionCCustomize.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		GridBagConstraints gbc_lblSectionCCustomize = new GridBagConstraints();
		gbc_lblSectionCCustomize.anchor = GridBagConstraints.WEST;
		gbc_lblSectionCCustomize.gridwidth = 5;
		gbc_lblSectionCCustomize.insets = new Insets( 5, 8, 5, 5 );
		gbc_lblSectionCCustomize.gridx = 0;
		gbc_lblSectionCCustomize.gridy = 0;
		add( lblSectionCCustomize, gbc_lblSectionCCustomize );

		playSheetComboBox = new JComboBox<>();
		playSheetComboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				String strSelectedPlaySheet = (String) ( (JComboBox) ae.getSource() ).getSelectedItem();
				//Store the currently selected playsheet with the currently
				//selected query tab:
				sparqlArea.setTagOfSelectedTab( strSelectedPlaySheet );
			}
		} );
		final PlaySheetRenderer pr = new PlaySheetRenderer();
		playSheetComboBox.setRenderer( pr );
		playSheetComboBox.setToolTipText( "Display response formats for custom query" );
		playSheetComboBox.setFont( new Font( "Tahoma", Font.PLAIN, 11 ) );
		playSheetComboBox.setBackground( new Color( 119, 136, 153 ) );
		playSheetComboBox.setMinimumSize( new Dimension( 125, 25 ) );
		playSheetComboBox.setPreferredSize( new Dimension( 140, 25 ) );
		// entries in combobox specified in question listener
		GridBagConstraints gbc_playSheetComboBox = new GridBagConstraints();
		gbc_playSheetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_playSheetComboBox.gridwidth = 5;
		gbc_playSheetComboBox.anchor = GridBagConstraints.EAST;
		gbc_playSheetComboBox.insets = new Insets( 5, 5, 5, 5 );
		gbc_playSheetComboBox.gridx = 0;
		gbc_playSheetComboBox.gridy = 2;
		add( playSheetComboBox, gbc_playSheetComboBox );
		// set the model each time a question is choosen to include playsheets that are not in PlaySheetEnum
		playSheetComboBox.setModel( new DefaultComboBoxModel( PlaySheetEnum.getAllSheetNames().toArray() ) );

		btnShowHint = new JButton();
		btnShowHint.setToolTipText( "Display Hint for PlaySheet" );
		Image img3 = Utility.loadImage( "questionMark.png" );
		if ( null != img3 ) {
			Image newimg = img3.getScaledInstance( 15, 15, java.awt.Image.SCALE_SMOOTH );
			btnShowHint.setIcon( new ImageIcon( newimg ) );
		}
		GridBagConstraints gbc_btnShowHint = new GridBagConstraints();
		gbc_btnShowHint.weightx = 1.0;
		gbc_btnShowHint.anchor = GridBagConstraints.EAST;
		btnShowHint.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		gbc_btnShowHint.fill = GridBagConstraints.NONE;
		gbc_btnShowHint.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnShowHint.gridx = 3;
		gbc_btnShowHint.gridy = 3;
		add( btnShowHint, gbc_btnShowHint );

		btnGetQuestionSparql = new JButton();
		btnGetQuestionSparql.setToolTipText( "Display SPARQL Query for Current Question" );
		Image img2 = Utility.loadImage( "download.png" );
		if ( null != img2 ) {
			Image newimg = img2.getScaledInstance( 15, 15, java.awt.Image.SCALE_SMOOTH );
			btnGetQuestionSparql.setIcon( new ImageIcon( newimg ) );
		}
		GridBagConstraints gbc_btnGetQuestionSparql = new GridBagConstraints();
		gbc_btnGetQuestionSparql.anchor = GridBagConstraints.EAST;
		btnGetQuestionSparql.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		gbc_btnGetQuestionSparql.fill = GridBagConstraints.NONE;
		gbc_btnGetQuestionSparql.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnGetQuestionSparql.gridx = 4;
		gbc_btnGetQuestionSparql.gridy = 3;
		add( btnGetQuestionSparql, gbc_btnGetQuestionSparql );

		btnSubmitSparqlQuery = new JButton();
		btnSubmitSparqlQuery.setEnabled( false );
		btnSubmitSparqlQuery.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		btnSubmitSparqlQuery.setText( "Submit Query" );
		btnSubmitSparqlQuery.setToolTipText( "Execute SPARQL query for selected question and display results in Display Pane" );
		GridBagConstraints gbc_btnSubmitSparqlQuery = new GridBagConstraints();
		gbc_btnSubmitSparqlQuery.fill = GridBagConstraints.NONE;
		gbc_btnSubmitSparqlQuery.insets = new Insets( 5, 5, 5, 5 );
		gbc_btnSubmitSparqlQuery.gridwidth = 5;
		gbc_btnSubmitSparqlQuery.gridx = 0;
		gbc_btnSubmitSparqlQuery.gridy = 4;
		gbc_btnSubmitSparqlQuery.anchor = GridBagConstraints.EAST;
		add( btnSubmitSparqlQuery, gbc_btnSubmitSparqlQuery );

		appendSparqlQueryChkBox = new JCheckBox( "Overlay" );
		appendSparqlQueryChkBox.setToolTipText( "Add results to currently selected grid or graph" );
		appendSparqlQueryChkBox.setHorizontalTextPosition( SwingConstants.LEFT );
		appendSparqlQueryChkBox.setEnabled( false );
		appendSparqlQueryChkBox.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		GridBagConstraints gbc_appendSparqlQueryChkBox = new GridBagConstraints();
		gbc_appendSparqlQueryChkBox.fill = GridBagConstraints.NONE;
		gbc_appendSparqlQueryChkBox.insets = new Insets( 5, 50, 5, 5 );
		gbc_appendSparqlQueryChkBox.gridwidth = 5;
		gbc_appendSparqlQueryChkBox.gridx = 0;
		gbc_appendSparqlQueryChkBox.gridy = 5;
		gbc_appendSparqlQueryChkBox.anchor = GridBagConstraints.EAST;
		add( appendSparqlQueryChkBox, gbc_appendSparqlQueryChkBox );

		sparqlArea = new TabbedQueries();
		/**
		 * Handles the assignment of keyboard shortcuts when changing the tab in
		 * TabbedQueries .
		 */
		sparqlArea.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged( ChangeEvent arg0 ) {
				//Pre-select the "playSheetComboBox" with the Tag
				//of the selected query tab:
				String strPlaySheet = sparqlArea.getTagOfSelectedTab();
				if ( strPlaySheet != null ) {
					playSheetComboBox.setSelectedItem( strPlaySheet );
				}
				//When the tab changes, check the contents of the displayed tab.
				//Only enable the "Submit Query" button if characters are shown:
				if ( sparqlArea.getTextOfSelectedTab().length() > 0 ) {
					btnSubmitSparqlQuery.setEnabled( true );
				}
				else {
					btnSubmitSparqlQuery.setEnabled( false );
				}
				//Submit Sparql query via the keystrokes <Ctrl><Enter>, from any selected component in the tool,
				//and show status in the status-bar:
				Action handleQueryKeys = new AbstractAction() {
					@Override
					public void actionPerformed( final ActionEvent e ) {
						Runnable runner = new Runnable() {
							SubmitSparqlQueryListener submitSparqlQueryListener = new SubmitSparqlQueryListener();

							@Override
							public void run() {
								submitSparqlQueryListener.actionPerformed( e );
							}
						};
						ProgressTask pt = getProgressTask( runner, btnSubmitSparqlQuery );
						OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
					}
				};
				try {
					if ( sparqlArea.getEditorOfSelectedTab() != null ) {
						sparqlArea.getEditorOfSelectedTab().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ), "handleQueryKeys" );
						sparqlArea.getEditorOfSelectedTab().getInputMap( JComponent.WHEN_FOCUSED ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ), "handleQueryKeys" );
						sparqlArea.getEditorOfSelectedTab().getActionMap().put( "handleQueryKeys", handleQueryKeys );
						//Only enables the "Submit Query" button if text (including spaces) 
						//has been entered into the currently selected editor:
						sparqlArea.getEditorOfSelectedTab().getDocument().addDocumentListener( new DocumentListener() {
							@Override
							public void changedUpdate( DocumentEvent cu ) {
								if ( sparqlArea.getTextOfSelectedTab().length() > 0 ) {
									btnSubmitSparqlQuery.setEnabled( true );
								}
								else {
									btnSubmitSparqlQuery.setEnabled( false );
								}
							}

							@Override
							public void insertUpdate( DocumentEvent iu ) {
							}

							@Override
							public void removeUpdate( DocumentEvent ru ) {
							}
						} );
					}
				}
				catch ( NullPointerException e ) {
					logger.error( e, e );
				}
			}
		} );
		//Initialize the keyboard listener:
		sparqlArea.getChangeListeners()[0].stateChanged( new ChangeEvent( sparqlArea ) );

		GridBagConstraints gbc_sparqlArea = new GridBagConstraints();
		gbc_sparqlArea.weightx = 50.0;
		gbc_sparqlArea.weighty = 5.0;
		gbc_sparqlArea.insets = new Insets( 5, 0, 0, 5 );
		gbc_sparqlArea.fill = GridBagConstraints.BOTH;
		gbc_sparqlArea.gridwidth = 10;
		gbc_sparqlArea.gridheight = 15;
		gbc_sparqlArea.anchor = GridBagConstraints.PAGE_START;
		gbc_sparqlArea.gridx = 5;
		gbc_sparqlArea.gridy = 0;
		add( sparqlArea, gbc_sparqlArea );
		sparqlArea.setPreferredSize( new Dimension( 200, 80 ) );
		
		SubmitSparqlQueryListener ssql = new SubmitSparqlQueryListener();
		btnSubmitSparqlQuery.addActionListener( ssql );
	}
	
	/**
	 * All public UI components declared above must be assigned listeners.
	 *
	 * NOTE: This method must be called near the beginning of "start()" in
	 * "PlayPane.java".
	 */
	public void loadCustomSparqlPanelListeners() {
		try {
			// load all the listeners
			// cast it to IChakraListener
			// for each listener specify what is the view field - Listener_VIEW
			// for each listener specify the right panel field -
			// Listener_RIGHT_PANEL
			// utilize reflection to get all the fields
			// for each field go into the properties file and find any of the
			// listeners
			// Drop down scrollbars

			java.lang.reflect.Field[] fields = CustomSparqlPanel.class.getFields();

			// run through the view components
			for ( Field field : fields ) {
				// logger.info(fields[fieldIndex].getName());
				Object obj = field.get( this );
				logger.debug( "Object set to " + obj );
				String fieldName = field.getName();
				if ( obj instanceof JComboBox || obj instanceof JButton
						|| obj instanceof JToggleButton || obj instanceof JSlider
						|| obj instanceof JInternalFrame
						|| obj instanceof JRadioButton || obj instanceof JTextArea ) {
					// load the controllers
					// find the view
					// right view and listener
					String ctrlNames
							= DIHelper.getInstance().getProperty( fieldName + "_" + Constants.CONTROL );
					if ( !( ctrlNames == null || ctrlNames.isEmpty() ) ) {
						logger.debug( "Listeners >>>>  " + ctrlNames + "   for field " + fieldName );
						StringTokenizer listenerTokens = new StringTokenizer( ctrlNames, ";" );
						while ( listenerTokens.hasMoreTokens() ) {
							String ctrlName = listenerTokens.nextToken();
							logger.debug( "Processing widget " + ctrlName );
							String className = DIHelper.getInstance().getProperty( ctrlName );
							final IChakraListener listener
									= IChakraListener.class.cast( Class.forName( className ).
											getConstructor().newInstance() );
							// in the future this could be a list
							// add it to this object
							logger.debug( "Listener " + ctrlName + "<>" + listener );
							// check to if this is a combobox or button
							if ( obj instanceof JComboBox<?> ) {
								( (JComboBox<?>) obj ).addActionListener( listener );
							}
							else if ( obj instanceof JButton ) {
								final JButton btn = JButton.class.cast( obj );
								btn.addActionListener( new ActionListener() {
									@Override
									public void actionPerformed( final ActionEvent e ) {
										Runnable runner = new Runnable() {
											@Override
											public void run() {
												listener.actionPerformed( e );
											}
										};
										ProgressTask pt = getProgressTask( runner, btn );
										OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
									}
								} );
							}
							else if ( obj instanceof JRadioButton ) {
								( (JRadioButton) obj ).addActionListener( listener );
							}
							else if ( obj instanceof JToggleButton ) {
								( (JToggleButton) obj ).addActionListener( listener );
							}
							else if ( obj instanceof JSlider ) {
								( (JSlider) obj ).addChangeListener( (ChangeListener) listener );
							}
							else if ( obj instanceof JTextArea ) {
								( (JTextArea) obj ).addFocusListener( (FocusListener) listener );
							}
							else {
								( (JInternalFrame) obj ).addInternalFrameListener( (InternalFrameListener) listener );
							}
							logger.debug( ctrlName + ":" + listener );
							DIHelper.getInstance().setLocalProperty( ctrlName, listener );
						}
					}
				}
				logger.debug( "Loading <" + fieldName + "> <> " + obj );
				DIHelper.getInstance().setLocalProperty( fieldName, obj );
			}
		}
		catch ( SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException e ) {
			logger.error( "Some listeners could not be attached to UI components", e );
		}
	}

	public InternalFrameListener makeDesktopListener() {
		return new InternalFrameListener() {

			@Override
			public void internalFrameOpened( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameClosing( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameClosed( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameIconified( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameDeiconified( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameActivated( InternalFrameEvent e ) {
				JComboBox<Insight> cmb = (JComboBox) DIHelper.getInstance().getLocalProp( Constants.QUESTION_LIST_FIELD );
				Insight insight = cmb.getItemAt( cmb.getSelectedIndex() );
				if ( null == insight ) {
					return;
				}

				//Determine whether to enable/disable the "Overlay" CheckBox, based upon
				//how the renderer of the selected visualization compares with that of the 
				//currently selected question:
				JDesktopPane pane = DIHelper.getInstance().getDesktop();
				PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );

				String output = insight.getOutput();
				JCheckBox appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );

				// the frame will be activated before there's a playsheet attached to it
				// make sure we have a playsheet before continuing
				PlaySheetCentralComponent pscc = psf.getActivePlaySheet();
				if ( null != pscc && output.equals( pscc.getClass().getCanonicalName() ) ) {
					appendChkBox.setEnabled( true );
				}
				else {
					appendChkBox.setEnabled( false );
				}
			}

			@Override
			public void internalFrameDeactivated( InternalFrameEvent e ) {
				//Disable "Overlay" CheckBox. (Note: The Activated method may re-enable this CheckBox):
				JCheckBox appendChkBox = (JCheckBox) DIHelper.getInstance().getLocalProp( Constants.APPEND );
				appendChkBox.setEnabled( false );
			}
		};
	}

	/**
	 * Displays a warning dialog to the user, indicating that the attempted
	 * database-update query cannot be undone by a simple keystroke, and offers an
	 * option to cancel out.
	 *
	 * @return showWarning -- (int) Corresponds to the "JOptionPane.YES_OPTION" or
	 * the "JOptionPane.NO_OPTION".
	 */
	private boolean okToUpdate() {
		Object[] buttons = { "Continue", "Cancel" };
		int response = JOptionPane.showOptionDialog( null,
				"The update query you are about to run \ncannot be undone.  Would you like to continue?",
				"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[1] );
		return ( JOptionPane.YES_OPTION == response );
	}

	/**
	 * Returns a ProgressTask object for running the current custom Sparql query,
	 * based upon the Runnable object passed in. Displays status in the progress
	 * bar at the bottom of the tool. Also, handles dialogs concerning SQL-UPDATE
	 * queries.
	 *
	 * @param runner -- (Runnable) The "actionPerformed(...)" method of a
	 * listener.
	 *
	 * @return -- (ProgressTask) Described above.
	 */
	private ProgressTask getProgressTask( Runnable runner, final JButton btn ) {
		ProgressTask pt = new ProgressTask( "Executing Query", runner ) {
			private boolean boolCancelUpdate = false;
			private String strSelectedPlaySheet = "";

			@Override
			public void runOp() {
				strSelectedPlaySheet = playSheetComboBox.getItemAt( playSheetComboBox.getSelectedIndex() );
				//Store the currently selected playsheet with the currently
				//selected query tab:
				sparqlArea.setTagOfSelectedTab( strSelectedPlaySheet );

				if ( strSelectedPlaySheet.equals( "Update Query" ) && btn == btnSubmitSparqlQuery ) {
					if( okToUpdate() ){
						boolCancelUpdate = false;
						setStartTime( new Date() );
						getOp().run();
					}
					else {
						boolCancelUpdate = true;
					}
				}
				else {
					boolCancelUpdate = true;
					setStartTime( new Date() );
					getOp().run();
				}
			}

			@Override
			public void done() {
				super.done();
				//Don't show row counts for "Hint" and "Copy Down":
				if ( btn.equals( btnShowHint ) == true || btn.equals( btnGetQuestionSparql ) == true ) {
					return;
				}
				//Display row count in status bar for Grid and Raw Grid queries:
				//--------------------------------------------------------------
				if ( strSelectedPlaySheet.equals( "Grid" ) || strSelectedPlaySheet.equals( "Raw Grid" ) ) {
					try {
						Thread.sleep( 500 );
					}
					catch ( InterruptedException e ) {
					}

					if ( Utility.getRowCount() > 0 ) {
						setLabel( "Fetched " + Integer.toString( Utility.getRowCount() ) + " rows" );

					}
				}
				if ( strSelectedPlaySheet.equals( "Update Query" )
						&& boolCancelUpdate == false ) {
					JOptionPane.showMessageDialog( sparqlArea, "Completed Execution in "
							+ Utility.getDuration( getStartTime(), getStopTime() ), "Update Query", JOptionPane.PLAIN_MESSAGE );
				}
			}
		};

		return pt;
	}

}
