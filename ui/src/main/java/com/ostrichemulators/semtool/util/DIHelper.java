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

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.OutputTypeRegistry;
import com.ostrichemulators.semtool.ui.components.PlayPane;
import com.ostrichemulators.semtool.ui.components.RepositoryList;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
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
			// need to set up the shapes here
			//Shape square = new Rectangle2D.Double(-5,-5,10, 10);

			//new Graphics2D().dr
			//square = (Shape) g2;
			//Shape circle = new Ellipse2D.Double(-5, -5, 10, 10);
			Ellipse2D.Double circle = new Ellipse2D.Double( -6, -6, 12, 12 );

			Rectangle2D.Double square = new Rectangle2D.Double( -6, -6, 12, 12 );
			//RoundRectangle2D.Double round = new RoundRectangle2D.Double(-6,-6,12, 12, 6, 6);

			Shape triangle = helper.createUpTriangle( 6 );
			Shape star = helper.createStar();
			Shape rhom = helper.createRhombus( 7 );
			Shape hex = helper.createHex( 7 );
			Shape pent = helper.createPent( 7 );

			helper.localProp.put( Constants.SQUARE, square );
			helper.localProp.put( Constants.CIRCLE, circle );
			helper.localProp.put( Constants.TRIANGLE, triangle );
			helper.localProp.put( Constants.STAR, star );
			helper.localProp.put( Constants.DIAMOND, rhom );
			helper.localProp.put( Constants.HEXAGON, hex );
			helper.localProp.put( Constants.PENTAGON, pent );

			Shape squareL = new Rectangle2D.Double( 0, 0, 40, 40 );
			//Shape circleL = new Ellipse2D.Double(0, 0, 13, 13);
			Shape circleL = new Ellipse2D.Double( 0, 0, 20, 20 );
			Shape triangleL = helper.createUpTriangleL();
			Shape starL = helper.createStarL();
			Shape rhomL = helper.createRhombusL();
			Shape pentL = helper.createPentL();
			Shape hexL = helper.createHexL();

			helper.localProp.put( Constants.SQUARE + Constants.LEGEND, squareL );
			helper.localProp.put( Constants.CIRCLE + Constants.LEGEND, circleL );
			helper.localProp.put( Constants.TRIANGLE + Constants.LEGEND, triangleL );
			helper.localProp.put( Constants.STAR + Constants.LEGEND, starL );
			helper.localProp.put( Constants.HEXAGON + Constants.LEGEND, hex );
			helper.localProp.put( Constants.DIAMOND + Constants.LEGEND, rhomL );
			helper.localProp.put( Constants.PENTAGON + Constants.LEGEND, pentL );
			helper.localProp.put( Constants.HEXAGON + Constants.LEGEND, hexL );

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

			// put all the layouts as well
			helper.localProp.put( Constants.FR, FRLayout.class );
			helper.localProp.put( Constants.KK, KKLayout.class );
			helper.localProp.put( Constants.ISO, ISOMLayout.class );
			helper.localProp.put( Constants.SPRING, SpringLayout.class );
			helper.localProp.put( Constants.CIRCLE_LAYOUT, CircleLayout.class );
			helper.localProp.put( Constants.RADIAL_TREE_LAYOUT, RadialTreeLayout.class );
			helper.localProp.put( Constants.TREE_LAYOUT, TreeLayout.class );
			helper.localProp.put( Constants.BALLOON_LAYOUT, BalloonLayout.class );

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
	 * Gets engine properties.
	 *
	 * @return Properties List of engine properties.
	 */
	public Properties getEngineProp() {
		return engineCoreProp;
	}

	/**
	 * Sets core properties from list.
	 *
	 * @param coreProp Properties	Obtained list of core properties.
	 */
	public void setCoreProp( Properties coreProp ) {
		this.coreProp = coreProp;
	}

	/**
	 * Gets the RDF Map.
	 *
	 * @return List of properties in RDF map
	 */
	public Properties getRdfMap() {
		return engineCoreProp;
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

	/**
	 * Creates a star shape.
	 *
	 * @return Star
	 */
	public Shape createStar() {
		double x = .5;
		double points[][] = {
			{ 0 * x, -15 * x }, { 4.5 * x, -5 * x }, { 14.5 * x, -5 * x }, { 7.5 * x, 3 * x },
			{ 10.5 * x, 13 * x }, { 0 * x, 7 * x }, { -10.5 * x, 13 * x }, { -7.5 * x, 3 * x },
			{ -14.5 * x, -5 * x }, { -4.5 * x, -5 * x }, { 0, -15 * x }
		};
		final GeneralPath star = new GeneralPath();
		star.moveTo( points[0][0], points[0][1] );

		for ( int k = 1; k < points.length; k++ ) {
			star.lineTo( points[k][0], points[k][1] );
		}

		star.closePath();
		return star;
	}

	/**
	 * Creates a star shape for the legend.
	 *
	 * @return Star
	 */
	public Shape createStarL() {
		//double points[][] = {{7.5,0} ,{9,5} ,{14.5, 5}, {11, 9}, {12.5, 14}, {7.2, 10.5}, {2.2, 14}, {3.5, 9}, {0,5}, {5, 5}, {7.5, 0}};
		double points[][] = { { 10, 0 }, { 13, 6.66 }, { 20, 6.66 }, { 14.66, 12 }, { 16.66, 18.66 }, { 10, 14 }, { 3.33, 18.66 }, { 5.33, 12 }, { 0, 6.66 }, { 7, 6.66 }, { 10, 0 } };

		final GeneralPath star = new GeneralPath();
		star.moveTo( points[0][0], points[0][1] );

		for ( int k = 1; k < points.length; k++ ) {
			star.lineTo( points[k][0], points[k][1] );
		}

		star.closePath();
		return star;
	}

	/**
	 * Creates a hexagon shape.
	 *
	 * @param s start position (X-coordinate) for drawing the hexagon.
	 *
	 * @return Hexagon
	 */
	public Shape createHex( final double s ) {
		GeneralPath hexagon = new GeneralPath();
		hexagon.moveTo( s, 0 );
		for ( int i = 0; i < 6; i++ ) {
			hexagon.lineTo( (float) Math.cos( i * Math.PI / 3 ) * s,
					(float) Math.sin( i * Math.PI / 3 ) * s );
		}
		hexagon.closePath();
		return hexagon;
	}

	/**
	 * Creates a hexagon shape for the legend
	 *
	 * @return Hexagon
	 */
	public Shape createHexL() {
		double points[][] = { { 20, 10 }, { 15, 0 }, { 5, 0 }, { 0, 10 }, { 5, 20 }, { 15, 20 } };

		final GeneralPath pent = new GeneralPath();
		pent.moveTo( points[0][0], points[0][1] );

		for ( int k = 1; k < points.length; k++ ) {
			pent.lineTo( points[k][0], points[k][1] );
		}

		pent.closePath();
		return pent;
	}

	/**
	 * Creates a pentagon shape.
	 *
	 * @param s start position (X-coordinate) for drawing the pentagon
	 *
	 * @return Pentagon
	 */
	public Shape createPent( final double s ) {
		GeneralPath hexagon = new GeneralPath();
		hexagon.moveTo( (float) Math.cos( Math.PI / 10 ) * s, (float) Math.sin( Math.PI / 10 ) * ( -s ) );
		for ( int i = 0; i < 5; i++ ) {
			hexagon.lineTo( (float) Math.cos( i * 2 * Math.PI / 5 + Math.PI / 10 ) * s,
					(float) Math.sin( i * 2 * Math.PI / 5 + Math.PI / 10 ) * ( -s ) );
		}
		hexagon.closePath();
		return hexagon;
	}

	/**
	 * Creates a pentagon shape for the legend.
	 *
	 * @return Pentagon
	 */
	public Shape createPentL() {
		double points[][] = { { 10, 0 }, { 19.510565163, 6.90983005625 }, { 15.8778525229, 18.0901699437 }, { 4.12214747708, 18.0901699437 }, { 0.48943483704, 6.90983005625 } };

		final GeneralPath pent = new GeneralPath();
		pent.moveTo( points[0][0], points[0][1] );

		for ( int k = 1; k < points.length; k++ ) {
			pent.lineTo( points[k][0], points[k][1] );
		}

		pent.closePath();
		return pent;
	}

	/**
	 * Creates a rhombus shape.
	 *
	 * @param s start position (X-coordinate) for drawing the rhombus
	 *
	 * @return	Rhombus
	 */
	public Shape createRhombus( final double s ) {
		double points[][] = {
			{ 0, -s }, { -s, 0 }, { 0, s }, { s, 0 }, };
		final GeneralPath r = new GeneralPath();
		r.moveTo( points[0][0], points[0][1] );

		for ( int k = 1; k < points.length; k++ ) {
			r.lineTo( points[k][0], points[k][1] );
		}

		r.closePath();
		return r;
	}

	/**
	 * Creates a rhombus shape for the legend.
	 *
	 * @return Rhombus
	 */
	public Shape createRhombusL() {
		double points2[][] = {
			{ 10, 0 }, { 0, 10 }, { 10, 20 }, { 20, 10 }, };
		final GeneralPath r = new GeneralPath(); // rhombus
		r.moveTo( points2[0][0], points2[0][1] );

		for ( int k = 1; k < points2.length; k++ ) {
			r.lineTo( points2[k][0], points2[k][1] );
		}

		r.closePath();
		return r;
	}

	/**
	 * Creates a triangle.
	 *
	 * @param s start position (X-coordinate) for drawing the triangle.
	 *
	 * @return Triangle
	 */
	public Shape createUpTriangle( final double s ) {
		final GeneralPath p0 = new GeneralPath();
		p0.moveTo( 0, -s );
		p0.lineTo( s, s );
		p0.lineTo( -s, s );
		p0.closePath();
		return p0;
	}

	/**
	 * Creates a triangle for the legend.
	 *
	 * @return Triangle
	 */
	public Shape createUpTriangleL() {
		GeneralPath p0 = new GeneralPath(); // triangle

		p0.moveTo( 10, 0 );
		p0.lineTo( 20, 20 );
		p0.lineTo( 0, 20 );
		p0.closePath();
		return p0;
	}

	/**
	 * Retrieves local properties from the hashtable.
	 *
	 * @param key to the hashtable
	 *
	 * @return Property mapped to a specific key
	 */
	public Object getLocalProp( String key ) {
    // if we're dealing with engine things, use the engines map
		if ( engines.containsKey( key ) ) {
			logger.warn( "please use getEngine() instead" );
			return engines.get( key );
		}

		if ( localProp.containsKey( key ) ) {
			return localProp.get( key );
		}
		else {
			return engineLocalProp.get( key );
		}
	}

	/**
	 * Method getComboBox Convenience method to retrieve JComboBox<String> from
	 * the hashtable.
	 *
	 * @param key String - key to the hashtable
	 * @return JComboBox<String> - JComboBox mapped to a specific key
	 */
	@SuppressWarnings( "unchecked" )
	public static JComboBox<String> getComboBox( String key ) {
		Object property = getInstance().getLocalProp( key );
		if ( property instanceof JComboBox<?> ) {
			return (JComboBox<String>) property;
		}

		logger.warn( "Local Property with key '" + key + "' does not have a JComboBox as a value: " + property );
		return null;
	}

	public static JTable getJTable( String key ) {
		Object property = getInstance().getLocalProp( key );
		if ( property instanceof JTable ) {
			return (JTable) property;
		}

		logger.warn( "Local Property with key '" + key + "' does not have a JTable as a value: " + property );
		return null;
	}

	public static JCheckBox getJCheckBox( String key ) {
		Object property = getInstance().getLocalProp( key );
		if ( property instanceof JCheckBox ) {
			return (JCheckBox) property;
		}

		logger.warn( "Local Property with key '" + key + "' does not have a JCheckBox as a value: " + property );
		return null;
	}

	/**
	 * Method getColor Convenience method to retrieve Color from the hashtable.
	 *
	 * @param key String - key to the hashtable
	 * @return Color - Color mapped to a specific key
	 */
	public static Color getColor( String key ) {
		if ( "TRANSPARENT".equalsIgnoreCase( key ) ) {
			return new Color( 255, 255, 255, 0 );
		}

		Object property = getInstance().getLocalProp( key );
		if ( property instanceof Color ) {
			return (Color) property;
		}

		logger.warn( "Local Property with key '" + key + "' does not have a Color as a value: " + property );
		return null;
	}

	/**
	 * Method getShape Convenience method to retrieve Shape from the hashtable.
	 *
	 * @param key String - key to the hashtable
	 * @return Shape - Shape mapped to a specific key
	 */
	public static Shape getShape( String key ) {
		Object property = getInstance().getLocalProp( key );
		if ( property instanceof Shape ) {
			return (Shape) property;
		}

		logger.warn( "Local Property with key '" + key + "' does not have a Shape as a value: " + property );
		return null;
	}

	/**
	 * Puts local properties into a hashtable.
	 *
	 * @param property, serves as the hashtable key.
	 * @param value the thing to hash
	 */
	public void setLocalProperty( String property, Object value ) {
		localProp.put( property, value );
	}

	/**
	 * Gets the ID for a specific question.
	 *
	 * @param question.
	 *
	 * @return the question id
	 */
	public String getIDForQuestion( String question ) {
		return engineLocalProp.get( question ).toString();
	}

	/**
	 * Creates a new list and loads properties given a certain file name.
	 *
	 * @param rdr
	 */
	public void loadCoreProp( Reader rdr ) {
		coreProp.clear();
		addCoreProp( rdr );
	}

	/**
	 * Adds properties from the given reader to the current list of "core" props
	 *
	 * @param rdr
	 */
	public void addCoreProp( Reader rdr ) {
		coreProp.clear();
		try {
			coreProp.load( rdr );
		}
		catch ( IOException ioe ) {
			logger.error( ioe, ioe );
		}
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

	public static URI getConceptURI() {
		return getInstance().getRdfEngine().getSchemaBuilder().getConceptUri().build();
	}

	public static URI getConceptURI( String _concept ) {
		return getInstance().getRdfEngine().getSchemaBuilder().getConceptUri( _concept );
	}

	public static URI getRelationURI() {
		return getInstance().getRdfEngine().getSchemaBuilder().getRelationUri().build();
	}

	public static URI getContainsURI() {
		return getInstance().getRdfEngine().getSchemaBuilder().getContainsUri();
	}
}
