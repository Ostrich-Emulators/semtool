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
 *****************************************************************************
 */
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.OutputTypeRegistry;
import com.ostrichemulators.semtool.ui.components.PlayPane;
import com.ostrichemulators.semtool.ui.components.RepositoryList;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDesktopPane;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * DIHelper is used throughout SEMOSS to obtain property names from core
 * propfiles and engine core propfiles.
 */
public class DIHelper {

	// helps with all of the dependency injection
	public static DIHelper helper = null;

	Properties rdfMap = null;

	// core properties file
	Properties coreProp = new Properties();
	// engine core prop file
	Properties engineCoreProp = null;
	// extended properties
	Properties extendProp = null;
	// Hashtable of local properties
	// will have the following keys
	// Perspective -<Hashtable of questions and identifier> - Possibly change this over to vector
	// Question-ID Key
	Map<String, Object> localProp = new HashMap<>();

	// localprop for engine
	Map<String, Object> engineLocalProp = new HashMap<>();

	// cached questions for an engine
	Map<String, Map<String, Object>> engineQHash = new HashMap<>();
	// Logger
	private static final Logger logger = Logger.getLogger( DIHelper.class );

	private final Map<String, IEngine> engines = new HashMap<>();

	private RepositoryList repolist;
	private JDesktopPane desktop;
	private PlayPane playpane;
	private ApplicationContext appctx;
	private final OutputTypeRegistry registry = new OutputTypeRegistry();

	/**
	 * Constructor for DIHelper.
	 */
	protected DIHelper() {
		// do nothing
	}

	public OutputTypeRegistry getOutputTypeRegistry(){
		return registry;
	}
	
	/**
	 * Set up shapes, colors, and layouts. Put properties for each in a hashtable
	 * of local properties.
	 *
	 * @return DIHelper Properties.
	 */
	public static DIHelper getInstance() {
		if ( helper == null ) {
			helper = new DIHelper();
			Color blue = new Color( 31, 119, 180 );
			Color green = new Color( 44, 160, 44 );
			Color red = new Color( 214, 39, 40 );
			Color brown = new Color( 143, 99, 42 );
			Color yellow = new Color( 254, 208, 2 );
			Color orange = new Color( 255, 127, 14 );
			Color purple = new Color( 148, 103, 189 );
			Color aqua = new Color( 23, 190, 207 );
			Color pink = new Color( 241, 47, 158 );

			helper.localProp.put( Constants.BLUE, blue );
			helper.localProp.put( Constants.GREEN, green );
			helper.localProp.put( Constants.RED, red );
			helper.localProp.put( Constants.BROWN, brown );
			helper.localProp.put( Constants.MAGENTA, pink );
			helper.localProp.put( Constants.YELLOW, yellow );
			helper.localProp.put( Constants.ORANGE, orange );
			helper.localProp.put( Constants.PURPLE, purple );
			helper.localProp.put( Constants.AQUA, aqua );
		}
		return helper;
	}

	/**
	 * Obtains a specific RDF engine.
	 *
	 * @return RDF engine.
	 */
	public IEngine getRdfEngine() {
		IEngine eng = repolist.getSelectedValue();
		if ( null != eng ) {
			return eng;
		}

		logger.debug( "No connected engine found." );
		return null;
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	public void setDesktop( JDesktopPane p ) {
		desktop = p;
	}

	/**
	 * Gets core properties.
	 *
	 * @return Properties	List of core properties.
	 */
	public Properties getCoreProp() {
		return coreProp;
	}

	/**
	 * Retrieves properties from hashtable.
	 *
	 * @param name Key used to retrieve properties
	 * @return	Property name
	 */
	public String getProperty( String name ) {
		if ( Constants.BASE_FOLDER.equals( name ) ) {
      // this is a very common call, but we're deprecating it
			// make sure it always returns something
			return coreProp.getProperty( name, System.getProperty( "user.dir" ) );
		}

		String retName = coreProp.getProperty( name );
		if (retName != null){
			return retName;
		}

		if ( repolist != null && getRdfEngine() != null) {
			return getRdfEngine().getProperty( name );
		}

		return null;
	}

	/**
	 * Puts properties in the core property hashtable.
	 *
	 * @param name String	Hash key.
	 * @param value String	Value mapped to specific key.
	 */
	public void putProperty( String name, String value ) {
		coreProp.put( name, value );
	}

	public void registerEngine( IEngine engine ) {
		engines.put( engine.getEngineName(), engine );
	}

	public void unregisterEngine( IEngine engine ) {
		engines.remove( engine.getEngineName() );
	}

	public IEngine getEngine( String name ) {
		return engines.get( name );
	}

	public Map<String, IEngine> getEngineMap() {
		return new HashMap<>( engines );
	}

	public RepositoryList getRepoList() {
		return repolist;
	}

	public void setRepoList( RepositoryList l ) {
		repolist = l;
	}

	public void setPlayPane( PlayPane pp ) {
		playpane = pp;
	}

	public PlayPane getPlayPane() {
		return playpane;
	}

	public void setAppCtx( ApplicationContext ctx ) {
		appctx = ctx;
	}

	public ApplicationContext getAppCtx() {
		return appctx;
	}
}
