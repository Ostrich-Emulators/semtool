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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

/**
 * A class that pretends to have a tab bar that only shows tabs when needed
 *
 * @author ryan
 */
public class HideableTabbedPane extends JPanel {

	private static final String NOTABS = "no-tabs-component";
	private static final String TABS = "tabs-component";

	private final JPanel notabs = new JPanel( new BorderLayout() );
	private PlaySheetCentralComponent pscc;
	private final CardLayout cards = new CardLayout();
	private final JTabbedPane tabs = new ReorderableTabbedPane();
	private boolean showingTabs = false;
	private ContainerListener containerListener;

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
				for ( ContainerListener cl : HideableTabbedPane.this.getContainerListeners() ) {
					cl.componentAdded( e );
				}
			}

			@Override
			public void componentRemoved( ContainerEvent e ) {
				if ( 1 == tabs.getTabCount() ) {
					pscc = PlaySheetCentralComponent.class.cast( tabs.getComponentAt( 0 ) );
					showTabs( pscc.prefersTabs() );
				}
				else if ( 0 == tabs.getTabCount() ) {
					// need to alert the parent that we don't have any more playsheets
					for ( ContainerListener cl : HideableTabbedPane.this.getContainerListeners() ) {
						cl.componentRemoved( e );
					}
				}
			}
		};

		tabs.addContainerListener( containerListener );
	}

	public void add( String title, PlaySheetCentralComponent c ) {
		if ( tabs.getTabCount() > 0 || c.prefersTabs() ) {
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
		}
	}

	private void showTabs( boolean showtab ) {
		if ( showingTabs && !showtab ) {
			// move our first tab to the main area
			int tc = tabs.getTabCount();
			if ( tc > 0 ) {
				tabs.removeContainerListener( containerListener );
				pscc = PlaySheetCentralComponent.class.cast( tabs.getComponentAt( 0 ) );
				notabs.add( pscc );
				tabs.addContainerListener( containerListener );
			}
		}
		else if ( !showingTabs && showtab ) {
			// move whatever we were showing to the first tab
			Component cmps[] = notabs.getComponents();
			if ( cmps.length > 0 ) {
				pscc = PlaySheetCentralComponent.class.cast( cmps[0] );
				tabs.addTab( pscc.getTitle(), pscc );
				tabs.setTabComponentAt( 0, new CloseableTab( tabs ) );
			}
		}

		cards.show( this, showtab ? TABS : NOTABS );
		showingTabs = showtab;
	}

	public void setTabPlacement( int tablocation ) {
		tabs.setTabPlacement( tablocation );
	}

	public void addChangeListener( ChangeListener cl ) {
		tabs.addChangeListener( cl );
	}

	public int getTabCount() {
		if ( showingTabs ) {
			return tabs.getTabCount();
		}
		return ( null == pscc ? 0 : 1 );
	}

	public void setSelectedComponent( Component c ) {
		if ( showingTabs ) {
			tabs.setSelectedComponent( c );
		}
	}

	public int getSelectedIndex() {
		if ( showingTabs ) {
			return tabs.getSelectedIndex();
		}
		return ( null == pscc ? -1 : 0 );
	}

	public Component getSelectedComponent() {
		return ( showingTabs ? tabs.getSelectedComponent() : pscc );
	}

	public Component getComponentAt( int idx ) {
		return ( showingTabs ? tabs.getComponentAt( idx ) : pscc );
	}

	public String getTitleAt( int idx ) {
		if ( showingTabs ) {
			return tabs.getTitleAt( idx );
		}
		return ( null == pscc ? null : pscc.getTitle() );
	}

	public int indexOfComponent( Component c ) {
		if ( showingTabs ) {
			return tabs.indexOfComponent( c );
		}
		return ( c == pscc ? 0 : -1 );
	}

	public CloseableTab getTabComponentAt( int idx ) {
		return CloseableTab.class.cast( tabs.getTabComponentAt( idx ) );
	}
	
	
	
}
