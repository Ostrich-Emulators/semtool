package gov.va.semoss.ui.components.tabbedqueries;

import gov.va.semoss.ui.components.CloseableTab;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

public class TabbedQueries extends JTabbedPane {

	private static final Logger log = Logger.getLogger( TabbedQueries.class );
	private static final long serialVersionUID = -490582079689204119L;

	private final TabRenameAction renamer = new TabRenameAction();

	public TabbedQueries() {
		/**
		 * Listener for the TabbedPane's change event. Selects the clicked tab. If
		 * the tab is labeled "*", then adds a new tab
		 */
		ChangeListener changer = new ChangeListener() {
			@Override
			public void stateChanged( ChangeEvent changeEvent ) {
				int index = getSelectedIndex();

				if ( getTitleAt( index ).equals( "*" ) ) {
					// we've selected the "create" tab, so change
					// it to a regular tab, and make a new "create" tab

					String newTabName = getNextTabName();
					setTitleAt( getSelectedIndex(), newTabName );

					setTabComponentAt( index, new QueryCloseableTab( TabbedQueries.this ) );
					setToolTipTextAt( getTabCount() - 1, "" );

					//Create a new "*" tab:
					addNewTab();
				}
			}
		};

		addChangeListener( changer );

		// NOTE: this will add a "*" tab, but the change listener we just
		// registered will rename it to Query-1, and create a new "*" tab
		addNewTab();
	}

	/**
	 * Exposes the Tag of the currently selected SyntaxTextArea as a public
	 * property. The "Custom Sparql Query" window will use this property to
	 * preselect the playsheet dropdown
	 *
	 * @return getTagOfSelectedTab The tag of the currently displayed
 SparqlTextArea.
	 */
	public String getTagOfSelectedTab() {
		return getEditorOfSelectedTab().getTag();
	}

	/**
	 * Sets the Tag property of the currently selected SyntaxTextArea. The "Custom
	 * Sparql Query" window will use this property to preselect the playsheet
	 * dropdown.
	 *
	 * @param strTag Described above.
	 */
	public void setTagOfSelectedTab( String strTag ) {
		getEditorOfSelectedTab().setTag( strTag );
	}

	/**
	 * Exposes the text of the currently selected tab as a public property. The
	 * "Custom Sparql Query" window will use this property to extract, run, and
	 * display queries.
	 *
	 * @return getTextOfSelectedTab The contents of the currently displayed
 SparqlTextArea.
	 */
	public String getTextOfSelectedTab() {
		return getEditorOfSelectedTab().getText();
	}

	/**
	 * Sets the text of the currently selected tab as a public property. The
	 * "Custom Sparql Query" window will use this property to extract, run, and
	 * display queries.
	 *
	 * @param strText A string of text to display in the currently selected
 SparqlTextArea.
	 */
	public void setTextOfSelectedTab( String strText ) {
		getEditorOfSelectedTab().setText( strText );
	}

	/**
	 * Exposes the SparqlTextArea of the selected tab as a public property, to
 be used for keyboard and mouse-click handlers:
	 *
	 * @return getEditorOfSelectedTab The currently displayed SparqlTextArea.
	 */
	public SparqlTextArea getEditorOfSelectedTab() {
		RTextScrollPane sp = RTextScrollPane.class.cast( getSelectedComponent() );
		return SparqlTextArea.class.cast( sp.getViewport().getView() );
	}

	/**
	 * Adds a tab labeled "*" to the TabbedPane. Also, adds a SparqlTextArea to
 the tab, and modifies the text-area's pop-up menu to show a "Rename Tab"
 option and removes the code-folding option.
	 */
	private void addNewTab() {
		SparqlTextArea textEditor = new SparqlTextArea();

		RTextScrollPane sp = new RTextScrollPane( textEditor );
		sp.setFoldIndicatorEnabled( false );
		this.addTab( "*", sp );
		this.setToolTipTextAt( this.getTabCount() - 1, "Add New Tab" );
		//If nothing is set to the text editor, when it is opened in V-CAMP/SEMOSS,
		//it may not respond to programmatic assignment, and the tool may hang:
		//textEditor.setText( "" );

		//Add an item to the context-popup menu that allows one to rename the current tab:
		JPopupMenu popup = textEditor.getPopupMenu();
		popup.add( renamer );

		//Place a black 1px border around the top component in TabbedQueries:
		sp.setBorder( BorderFactory.createMatteBorder( 0, 1, 1, 1, Color.BLACK ) );

		// Fix to make sure that bracket match highlighting follows component resize
		// (really small windows can make it ugly)
		textEditor.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized( ComponentEvent e ) {
				int caratPosistion = ( (SparqlTextArea) e.getSource() ).getCaretPosition();
				( (SparqlTextArea) e.getSource() ).setCaretPosition( 0 );
				( (SparqlTextArea) e.getSource() ).setCaretPosition( caratPosistion );
			}
		} );
	}

	/**
	 * @return Default name for the new tab.
	 */
	protected String getNextTabName() {
		Pattern pat = Pattern.compile( "Query-([0-9]+)" );
		int highestId = 0;
		for ( int i = 0; i < this.getTabCount() - 1; i++ ) {
			String title = getTitleAt( i );
			Matcher m = pat.matcher( title );
			if ( m.matches() ) {
				int num = Integer.parseInt( m.group( 1 ) );
				if ( highestId < num ) {
					highestId = num;
				}
			}
		}

		return "Query-" + ( highestId + 1 );
	}

	public void loadToEmptyTab( String sparql ) {
		if ( !getTextOfSelectedTab().trim().isEmpty() ) {
			// our current tab has text on it, so make a new tab and switch to its
			setSelectedIndex( getTabCount() - 1 );
		}

		setTextOfSelectedTab( sparql );
	}

	private class QueryCloseableTab extends CloseableTab {

		public QueryCloseableTab( JTabbedPane parent ) {
			super( parent );

			addMouseListener( new MouseAdapter() {

				@Override
				public void mousePressed( MouseEvent e ) {
					e.consume();
					if ( SwingUtilities.isRightMouseButton( e ) ) {
						JPopupMenu popup = new JPopupMenu();
						//popup.add( saver );
						popup.add( renamer );
						popup.show( e.getComponent(), e.getX(), e.getY() );
					}
					else {
						TabbedQueries.this.setSelectedIndex(
								TabbedQueries.this.indexOfTabComponent( QueryCloseableTab.this ) );
					}
				}
			} );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			int tabpos = indexOfTabComponent( this );
			String title = getTitleAt( tabpos );

			// user must confirm delete if the tab isn't empty
			if ( getEditorOfSelectedTab().getText().trim().isEmpty()
					|| JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
							getTabComponentAt( tabpos ),
							"Delete tab \"" + title + "\"?", "Remove tab",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE ) ) {

				// if we're the penultimate tab, set the tab on our left as selected,
				// so we can avoid creating a new tab (which happens when the last tab
				// becomes selected)
				int numtabs = TabbedQueries.this.getTabCount();
				if ( tabpos + 2 == numtabs && numtabs > 2 ) {
					TabbedQueries.this.setSelectedIndex( tabpos - 1 );
				}

				TabbedQueries.this.remove( tabpos );
			}
		}
	}

	/**
	 * Specifies a "Rename Tab" option on the SparqlTextArea's context menu, and
 provides a listener for that option's selection.
	 */
	private class TabRenameAction extends AbstractAction {

		private static final long serialVersionUID = -3273106660731070101L;

		public TabRenameAction() {
			super( "Rename Tab" );
		}

		/**
		 * Adds a new title to the currently selected tab. The user enters the
		 * title, which cannot be empty or the "*" character.
		 */
		@Override
		public void actionPerformed( ActionEvent e ) {
			int index = getSelectedIndex();
			String strOldTitle = getTitleAt( index );
			String strNewTitle = ( JOptionPane.showInputDialog( TabbedQueries.this,
					"Enter a Query Tab Title:", strOldTitle ) + "" ).trim();
			if ( !( "null".equals( strNewTitle ) || strNewTitle.isEmpty()
					|| "*".equals( strNewTitle ) ) ) {
				setTitleAt( index, strNewTitle );
			}
		}
	}
}
