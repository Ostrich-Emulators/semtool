/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.util.ExportUtility;
import gov.va.semoss.util.Utility;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

/**
 *
 * @author ryan
 */
public abstract class PlaySheetCentralComponent extends JComponent implements IPlaySheet {

	private static final long serialVersionUID = 3695436192569801799L;
	private static final Logger log
			= Logger.getLogger( PlaySheetCentralComponent.class );

	private PlaySheetFrame playframe;
	private String title;
	private final List<String> headers = new ArrayList<>();

	public void setFrame( PlaySheetFrame f ) {
		playframe = f;
	}

	@Override
	public void setTitle( String t ) {
		title = t;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void addSibling( PlaySheetCentralComponent pscc ){
		getPlaySheetFrame().addTab( pscc );
	}
	
	public void addSibling( String title, PlaySheetCentralComponent pscc ){
		getPlaySheetFrame().addTab( title, pscc );
	}

	public boolean hasChanges() {
		return false;
	}

	protected void setHeaders( List<String> newheaders ) {
		headers.clear();
		headers.addAll( newheaders );
	}

	/**
	 * Gets a map of String-to-Actions from this tab.
	 *
	 * @see PlaySheetFrame#getActions()
	 * @return a (by default, empty) mapping of actions
	 */
	public Map<String, Action> getActions() {
		return new HashMap<>();
	}

	/**
	 * Gets headers for this playsheet. The definition of "header" is
	 * implementation specific, but if {@link #getTabularData()} returns non-null,
	 * this function should return the headers for the data returned by that
	 * function
	 *
	 * @return
	 */
	public List<String> getHeaders() {
		return new ArrayList<>( headers );
	}

	public PlaySheetFrame getPlaySheetFrame() {
		return playframe;
	}

	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		log.error( "into create: " + data.size() + " items" );
	}

	public void incrementFont( float incr ) {
		Deque<Component> components = new ArrayDeque<>();
		components.addAll( Arrays.asList( getComponents() ) );
		Component c = null;
		while ( null != ( c = components.poll() ) ) {
			Font f = c.getFont();
			c.setFont( f.deriveFont( f.getSize2D() + incr ) );

			if ( c instanceof Container ) {
				components.addAll( Arrays.asList( Container.class.cast( c ).getComponents() ) );
			}
		}
	}

	public void create( Model m, IEngine engine ) {
		List<Value[]> valdata = new ArrayList<>();
		for ( Statement s : m ) {
			valdata.add( new Value[]{ s.getSubject(), s.getPredicate(),
				s.getObject() } );
		}

		create( valdata, Arrays.asList( "Subject", "Predicate", "Object" ), engine );
	}

	public void overlay( Model m, IEngine engine ) {
		List<Value[]> valdata = new ArrayList<>();
		for ( Statement s : m ) {
			valdata.add( new Value[]{ s.getSubject(), s.getPredicate(),
				s.getObject() } );
		}

		overlay( valdata, Arrays.asList( "Subject", "Predicate", "Object" ), engine );
	}

	public void populateToolBar( JToolBar toolBar, final String tabTitle ) {
		AbstractAction savePDFAction = new AbstractAction( "Export PDF", DbAction.getIcon( "save_pdf" ) ) {
			private static final long serialVersionUID = 3936319423436805397L;

			@Override
			public void actionPerformed( ActionEvent e ) {
				ExportUtility.doGraphExportPDFWithDialogue( PlaySheetCentralComponent.this );
			}

		};

		savePDFAction.putValue( Action.SHORT_DESCRIPTION, "Export PDF" );
		toolBar.add( savePDFAction );

		AbstractAction savePNGAction = new AbstractAction( "Export PNG", DbAction.getIcon( "save_png" ) ) {
			private static final long serialVersionUID = 3936319423436805398L;

			@Override
			public void actionPerformed( ActionEvent e ) {
				ExportUtility.doGraphExportPNGWithDialogue( PlaySheetCentralComponent.this );
			}

		};

		savePNGAction.putValue( Action.SHORT_DESCRIPTION, "Export PNG" );
		toolBar.add( savePNGAction );
	}

	/**
	 * Replaces all instances of URI values with their label (as a string)
	 *
	 * @param data
	 * @param eng
	 * @return the data list (the <code>data</code> argument), for convenience
	 */
	public static List<Value[]> convertUrisToLabels( List<Value[]> data, IEngine eng ) {
		Set<URI> needLabels = new HashSet<>();

		for ( Value[] valarr : data ) {
			for ( Value v : valarr ) {
				if ( v instanceof URI ) {
					needLabels.add( URI.class.cast( v ) );
				}
			}
		}

		Map<URI, String> labels = Utility.getInstanceLabels( needLabels, eng );
		ListIterator<Value[]> valit = data.listIterator();
		while ( valit.hasNext() ) {
			Value[] valarr = valit.next();
			for ( int i = 0; i < valarr.length; i++ ) {
				if ( valarr[i] instanceof URI ) {
					valarr[i] = new LiteralImpl( labels.get( URI.class.cast( valarr[i] ) ) );
				}
			}
			valit.set( valarr );
		}

		return data;
	}

	public static List<String[]> convertEverythingToStrings( List<Value[]> newdata, IEngine eng ) {
		List<String[]> list = new ArrayList<>();

		convertUrisToLabels( newdata, eng );
		for ( Value[] val : newdata ) {
			String[] newvals = new String[val.length];
			for ( int i = 0; i < val.length; i++ ) {
				newvals[i] = val[i].stringValue().replace( "\"", "" );
			}
			list.add( newvals );
		}
		return list;
	}

	public void overlay( List<Value[]> data, List<String> headers, IEngine eng ) {
		log.error( "into overlay: " + data.size() + " items" );
	}

	public void remove( List<Value[]> data ) {
		log.error( "into remove: " + data.size() + " items" );
	}

	public void refine() {
		log.error( "into refine" );
	}

	// all this stuff is temporary until we get rid of the IPlaySheet interface
	@Override
	public void runAnalytics() {
		log.error( "runAnalytics does nothing" );
	}

	@Override
	public Object getData() {
		log.error( "getdata is not yet implemented" );
		return null;
	}

	@Override
	public void createData() {
		log.error( "createData is not yet implemented" );
	}

	@Override
	public IEngine getEngine() {
		return playframe.getEngine();
	}

	@Override
	public void setEngine( IEngine engine ) {
		playframe.setEngine( engine );
	}

	@Override
	public void overlayView() {
		log.error( "overlayView is not yet implemented" );
	}

	@Override
	public void refineView() {
		refine();
	}

	@Override
	public void createView() {
		log.error( "createView does nothing" );
	}

	@Override
	public Insight getInsight() {
		log.error( "insight doesn't mean anything here" );
		return null;
	}

	@Override
	public void setInsight( Insight insight ) {
		log.error( "insight doesn't mean anything here" );
	}

	@Override
	public void setJDesktopPane( JDesktopPane pane ) {
		log.error( "this function is meaningless. use pane.add(this) instead" );
	}

	@Override
	public String getQuery() {
		log.error( "query doesn't mean anything here" );
		return "";
	}

	@Override
	public void setQuery( String query ) {
		log.error( "query doesn't mean anything here" );
	}

	@Override
	public Perspective getPerspective() {
		log.error( "setPerspective doesn't make sense here" );
		return null;
	}

	@Override
	public void setPerspective( Perspective p ) {
		log.error( "getPerspective doesn't make sense here" );
	}

	public boolean canAcceptDataWithHeaders( List<String> newheaders ) {
		return headers.equals( newheaders );
	}

	public boolean canAcceptModelData() {
		return canAcceptDataWithHeaders( Arrays.asList( "Subject", "Predicate", "Object" ) );
	}

	/**
	 * Gets the tabular data from this playsheet, or null if this playsheet does
	 * not support tabular data.
	 *
	 * @return tabular data, or null if this playsheet does not support tables
	 * @see @link #getHeaders()}
	 */
	public List<Object[]> getTabularData() {
		return null;
	}

	/**
	 * Should this component's PlaySheetFrame open tabs if this is the initial
	 * component? By default, return false
	 *
	 * @return true, if the frame should have visible tabs initially
	 */
	public boolean prefersTabs() {
		return false;
	}
}
