/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.api.IPlaySheet;

import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
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

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	private RetrievingLabelCache labelcache = new RetrievingLabelCache();

	public void setFrame( PlaySheetFrame f ) {
		playframe = f;
	}

	public void setLabelCache( RetrievingLabelCache r ) {
		labelcache = r;
	}

	public RetrievingLabelCache getLabelCache() {
		return labelcache;
	}

	@Override
	public void setTitle( String t ) {
		title = t;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void addSibling( PlaySheetCentralComponent pscc ) {
		pscc.setLabelCache( labelcache );
		getPlaySheetFrame().addTab( pscc );
	}

	public void addSibling( String title, PlaySheetCentralComponent pscc ) {
		pscc.setLabelCache( labelcache );
		getPlaySheetFrame().addTab( title, pscc );
	}

	@Override
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
	@Override
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
	@Override
	public List<String> getHeaders() {
		return new ArrayList<>( headers );
	}

	public PlaySheetFrame getPlaySheetFrame() {
		return playframe;
	}

	/**
	 * Signals when this playsheet's tab is selected in the frame. By default,
	 * does nothing
	 */
	@Override
	public void activated() {
	}

	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		log.warn( "create not supported in this playsheet (" + data.size() + " items)" );
	}

	@Override
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

	@Override
	public void create( Model m, IEngine engine ) {
		labelcache.setEngine( engine );
		List<Value[]> valdata = new ArrayList<>();
		for ( Statement s : m ) {
			valdata.add( new Value[]{ s.getSubject(), s.getPredicate(),
				s.getObject() } );
		}

		create( valdata, Arrays.asList( "Subject", "Predicate", "Object" ), engine );
	}

	@Override
	public void overlay( Model m, IEngine engine ) {
		labelcache.setEngine( engine );
		List<Value[]> valdata = new ArrayList<>();
		for ( Statement s : m ) {
			valdata.add( new Value[]{ s.getSubject(), s.getPredicate(),
				s.getObject() } );
		}

		overlay( valdata, Arrays.asList( "Subject", "Predicate", "Object" ), engine );
	}

	public void populateToolBar( JToolBar toolBar, final String tabTitle ) {
	}

	/**
	 * Replaces all instances of IRI values with their label (as a string)
	 *
	 * @param data
	 * @param eng
	 * @return the data list (the <code>data</code> argument), for convenience
	 */
	public static List<Value[]> convertUrisToLabels( List<Value[]> data, IEngine eng ) {
		Set<IRI> needLabels = new HashSet<>();

		for ( Value[] valarr : data ) {
			for ( Value v : valarr ) {
				if ( v instanceof IRI ) {
					needLabels.add( IRI.class.cast( v ) );
				}
			}
		}

		Map<IRI, String> labels = Utility.getInstanceLabels( needLabels, eng );
		ListIterator<Value[]> valit = data.listIterator();
		while ( valit.hasNext() ) {
			Value[] valarr = valit.next();
			for ( int i = 0; i < valarr.length; i++ ) {
				if ( valarr[i] instanceof IRI ) {
					valarr[i] = SimpleValueFactory.getInstance().createLiteral( labels.get( IRI.class.cast( valarr[i] ) ) );
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

	@Override
	public void overlay( List<Value[]> data, List<String> headers, IEngine eng ) {
		log.warn( "overlay not supported in this playsheet (" + data.size() + " items)" );
	}

	@Override
	public IEngine getEngine() {
		return playframe.getEngine();
	}

	@Override
	public boolean canAcceptDataWithHeaders( List<String> newheaders ) {
		return headers.equals( newheaders );
	}

	@Override
	public boolean canAcceptModelData() {
		return canAcceptDataWithHeaders( Arrays.asList( "Subject", "Predicate", "Object" ) );
	}

	/**
	 * Gets the tabular data from this playsheet, or null if this playsheet does
	 * not support tabular data.
	 *
	 * @return tabular data, or null if this playsheet does not support tables
	 * @see #getHeaders()
	 */
	@Override
	public List<Object[]> getTabularData() {
		return null;
	}

	/**
	 * Should this component's PlaySheetFrame open tabs if this is the initial
	 * component? By default, return false
	 *
	 * @return true, if the frame should have visible tabs initially
	 */
	@Override
	public boolean prefersTabs() {
		return false;
	}

	/**
	 * Returns whether or not the playsheet requires a user-defined query. This
	 * would normally be true, but for those cases where a custom playsheet has
	 * been written that defines its requiere queries, this function can be
	 * overridden to return false.
	 *
	 * @return
	 */
	public boolean requiresQuery() {
		return true;
	}
}
