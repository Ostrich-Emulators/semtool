/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;

/**
 * A class that pretends to have a tab bar that only shows tabs when needed
 *
 * @author ryan
 */
public class HideableTabbedPane extends JPanel {

	private static final long serialVersionUID = 1149198288527605328L;

	private static final String NOTABS = "no-tabs-component";
	private static final String TABS = "tabs-component";

	private final JPanel notabs = new JPanel( new BorderLayout() );
	private PlaySheetCentralComponent pscc;
	private final CardLayout cards = new CardLayout();
	private final JTabbedPane tabs = new ReorderableTabbedPane();
	private ContainerListener containerListener;
	private final List<ChangeListener> changeListenees = new ArrayList<>();
	private boolean movingStuff = false;

	public HideableTabbedPane( LayoutManager layout, boolean isDoubleBuffered ) {
		super( layout, isDoubleBuffered );
		init();
	}

	public HideableTabbedPane( boolean isDoubleBuffered ) {
		super( isDoubleBuffered );
		init();
	}

	public HideableTabbedPane() {
		init();
	}

	private void init() {
		setLayout( cards );
		notabs.setBorder( null );
		tabs.setBorder( null );
		add( notabs, NOTABS );
		add( tabs, TABS );

		tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

		containerListener = new ContainerListener() {

			@Override
			public void componentAdded( ContainerEvent e ) {
				if ( !movingStuff ) {
					for ( ContainerListener cl : HideableTabbedPane.this.getContainerListeners() ) {
						cl.componentAdded( e );
					}
				}
			}

			@Override
			public void componentRemoved( ContainerEvent e ) {
				Logger.getLogger( getClass() ).debug( "component removed" );
				if ( 1 == tabs.getTabCount() ) {
					pscc = getPsccAt( 0 );
					showTabs( pscc.prefersTabs() );
				}

				if ( !movingStuff ) {
					// need to alert the parent that we lost a playsheet
					for ( ContainerListener cl : HideableTabbedPane.this.getContainerListeners() ) {
						cl.componentRemoved( e );
					}
				}
			}
		};

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e ) {
				if ( !movingStuff ) {
					PlaySheetCentralComponent pscc = getPsccAt( tabs.getSelectedIndex() );
					pscc.activated();

					for ( ChangeListener cl : changeListenees ) {
						cl.stateChanged( e );
					}
				}
			}
		};

		tabs.addContainerListener( containerListener );
		tabs.addChangeListener( changeListener );
	}

	public void add( String title, PlaySheetCentralComponent c ) {
		if ( getTabCount() > 0 || c.prefersTabs() ) {
			showTabs( true );

			tabs.add( title, c );
			tabs.setSelectedComponent( c );
			int idx = tabs.indexOfComponent( c );
			tabs.setTabComponentAt( idx, new CloseableTab( tabs ) );
		}
		else {
			pscc = c;
			notabs.add( pscc, BorderLayout.CENTER );
			showTabs( false );
			c.activated();

			if ( tabs.getTabCount() > 1 ) {
				ChangeEvent ce = new ChangeEvent( tabs );
				for ( ChangeListener cl : tabs.getChangeListeners() ) {
					cl.stateChanged( ce );
				}
			}
		}
	}

	private PlaySheetCentralComponent getPsccAt( int idx ) {
		if ( tabs.isVisible() ) {
			return ( idx >= tabs.getTabCount() || idx < 0 ? null
					: PlaySheetCentralComponent.class.cast( tabs.getComponentAt( idx ) ) );
		}
		else {
			return pscc;
		}
	}

	private void showTabs( boolean showtab ) {
		Logger.getLogger( getClass() ).debug( "showTabs " + showtab );

		boolean oldshowing = tabs.isVisible();

		movingStuff = ( oldshowing != showtab );

		if ( oldshowing && !showtab ) {
			Logger.getLogger( getClass() ).debug( "moving visible tab to panel" );
			// move our first tab to the main area
			int tc = tabs.getTabCount();
			if ( tc > 0 ) {
				pscc = getPsccAt( 0 );
				notabs.add( pscc );
			}
		}
		else if ( !oldshowing && showtab ) {
			Logger.getLogger( getClass() ).debug( "moving panel to first tab" );
			// move whatever we were showing to the first tab
			Component cmps[] = notabs.getComponents();
			if ( cmps.length > 0 ) {
				pscc = PlaySheetCentralComponent.class.cast( cmps[0] );
				tabs.addTab( pscc.getTitle(), pscc );
				tabs.setTabComponentAt( 0, new CloseableTab( tabs ) );
			}
		}

		movingStuff = false;

		cards.show( this, showtab ? TABS : NOTABS );
	}

	public void setTabPlacement( int tablocation ) {
		tabs.setTabPlacement( tablocation );
	}

	public void addChangeListener( ChangeListener cl ) {
		changeListenees.add( cl );
	}

	public int getTabCount() {
		if ( tabs.isVisible() ) {
			return tabs.getTabCount();
		}
		return ( null == pscc ? 0 : 1 );
	}

	public void setSelectedComponent( Component c ) {
		if ( tabs.isVisible() ) {
			tabs.setSelectedComponent( c );
		}
	}

	public int getSelectedIndex() {
		if ( tabs.isVisible() ) {
			return tabs.getSelectedIndex();
		}
		return ( null == pscc ? -1 : 0 );
	}

	public Component getSelectedComponent() {
		return ( tabs.isVisible() ? tabs.getSelectedComponent() : pscc );
	}

	public Component getComponentAt( int idx ) {
		return ( tabs.isVisible() ? tabs.getComponentAt( idx ) : pscc );
	}

	public String getTitleAt( int idx ) {
		if ( tabs.isVisible() ) {
			return tabs.getTitleAt( idx );
		}
		return ( null == pscc ? null : pscc.getTitle() );
	}

	public int indexOfComponent( Component c ) {
		if ( tabs.isVisible() ) {
			return tabs.indexOfComponent( c );
		}
		return ( c == pscc ? 0 : -1 );
	}

	public CloseableTab getTabComponentAt( int idx ) {
		return CloseableTab.class.cast( tabs.getTabComponentAt( idx ) );
	}
}
