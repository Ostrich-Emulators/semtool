package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * The Graph Shape Repository is responsible for serving as a single point of
 * storage and retrieval for Shapes throughout the SEMOSS system (shapes in
 * graphs and in graph-legends, such as lists and combo-boxes).
 *
 * @author Wayne Warren
 *
 */
public class GraphShapeRepository {

	/**
	 * A hash which tracks URIs that have been assigned shapes, so that we can
	 * consistently assign shapes through recall
	 */
	private final Map<URI, Shape> vertexShapeHash = new HashMap<>();
	/**
	 * A lookup for legend shapes, with actual shapes as keys and their
	 * corresponding legend shapes as values
	 */
	private final Map<Shape, Shape> vertexShapeLegendHash = new HashMap<>();
	/**
	 * The utility class instance responsible for shape generation
	 */
	private ShapeGenerator shapeGenerator;
	/**
	 * The singleton instance of this class
	 */
	private static GraphShapeRepository instance = null;
	/**
	 * The logger that this class will use
	 */
	private final Logger logger = Logger.getLogger( GraphShapeRepository.class );

	private final Map<Shape, String> shapeNameLkp = new HashMap<>();

	/**
	 * Default constructor
	 */
	private GraphShapeRepository() {
		shapeGenerator = new ShapeGenerator();
	}

	/**
	 * Singleton access method
	 *
	 * @return The singleton instance
	 */
	public static GraphShapeRepository instance() {
		if ( instance == null ) {
			instance = new GraphShapeRepository();
		}
		return instance;
	}

	/**
	 * Retrieve a shape by handle/key
	 *
	 * @param name The name of the shape (from the Constants class)
	 * @return The shape identified by the supplied name, null otherwise
	 */
	public Shape getShapeByName( String name ) {
		Shape shape = shapeGenerator.getNamedShape( name );
		if ( shape == null ) {
			logger.warn( "Shape was not found using key = " + name );
		}
		return shape;
	}

	public String getShapeName( Shape s ) {
		return shapeNameLkp.get( s );
	}

	/**
	 * Get the shape appropriate for the supplied "type" URI
	 *
	 * @param typeURI The resource URI representing the "type" of a Vertex
	 * @return An appropriate shape
	 */
	public Shape getShape( URI typeURI ) {
		// next check if it is specified in the properties file
		Shape newShape = null;
		if ( typeURI == null ) {
			newShape = shapeGenerator.getNamedShape( Constants.CIRCLE );
		}
		else {
			String shapename
					= DIHelper.getInstance().getProperty( typeURI.getLocalName() + "_SHAPE" );
			// This is the last surviving call to DIHelper in the shape/color area, 
			// mostly because a full analysis hasn't been completed regarding what this 
			// properties-style lookup might entail
			if ( null != shapename ) {
				newShape = shapeGenerator.getNamedShape( shapename );
			}

			if ( null == newShape && vertexShapeHash.containsKey( typeURI ) ) {
				newShape = vertexShapeHash.get( typeURI );
			}

			if ( null == newShape ) {
				newShape = shapeGenerator.nextShape();
				vertexShapeHash.put( typeURI, newShape );
			}

			if ( newShape == null ) {
				logger.warn( "Warning - Derived shape was null: " + typeURI.toString() );
			}
		}
		return newShape;
	}

	/**
	 * Get the correlated Legend shape for a particular shape
	 *
	 * @param svshape The shape to which the legend shape is correlated
	 * @return The associated legend shape, null if it is not found
	 */
	public Shape getLegendShape( Shape svshape ) {
		Shape legendShape = vertexShapeLegendHash.get( svshape );
		if ( legendShape == null ) {
			logger.warn( "Warning - Requested legend shape not found for shape: "
					+ shapeNameLkp.get( svshape ) );
		}
		return legendShape;
	}

	/**
	 * Clears all initialized shape data
	 */
	public void clearAll() {
		vertexShapeHash.clear();
		shapeGenerator = new ShapeGenerator();
	}

	/**
	 * Get all shape names in the system
	 *
	 * @return A list of the keys/handles of the shapes
	 */
	public String[] getAllShapeNames() {
		return shapeGenerator.shapeNames;
	}

	public Map<String, Shape> getNamedShapeMap() {
		Map<String, Shape> map = new HashMap<>();
		for ( String name : shapeGenerator.shapeNames ) {
			map.put( name, shapeGenerator.getNamedShape( name ) );
		}
		return map;
	}

	/**
	 * Get a set of all Shapes used in this system
	 *
	 * @return An array of keys/handles, used to identify the shapes
	 */
	public Shape[] getAllShapes() {
		return shapeGenerator.getAllShapes();
	}

	/**
	 * The Shape Generator class is responsible for producing the shapes that
	 * serve as visual metaphors for the Vertices in a graph
	 *
	 * @author Wayne Warren
	 *
	 */
	private class ShapeGenerator {

		/**
		 * A listing of standard vertex shapes using the name/tag of the shape as
		 * the key and the composite shape class as the value
		 */
		private final Map<String, Shape> shapes = new HashMap<>();
		/**
		 * The translation applied to the triangle shape when it is drawn
		 */
		private static final int UP_TRIANGLE_ORIGIN = 6;
		/**
		 * The translation applied to the diamond shape when it is drawn
		 */
		private static final int RHOMBUS_ORIGIN = 7;
		/**
		 * The translation applied to the hexagon shape when it is drawn
		 */
		private static final int HEX_ORIGIN = 7;
		/**
		 * The translation applied to the pentagon shape when it is drawn
		 */
		private static final int PENT_ORIGIN = 7;
		/**
		 * The last shape index chosen when nextShape() was called
		 */
		private int lastIndexChosen = -1;
		/**
		 * A complete listing of the shape names/tags
		 */
		public final String[] shapeNames = {
			Constants.STAR,
			Constants.HEXAGON,
			Constants.PENTAGON,
			Constants.DIAMOND,
			Constants.TRIANGLE,
			Constants.CIRCLE,
			Constants.SQUARE, };

		/**
		 * Default constructor
		 */
		public ShapeGenerator() {
			// Create the shapes to populate the shapes hash
			Shape star = createStar();
			Shape hex = createHex( HEX_ORIGIN );
			Shape pent = createPent( PENT_ORIGIN );
			Shape diamond = createRhombus( RHOMBUS_ORIGIN );
			Shape triangle = createUpTriangle( UP_TRIANGLE_ORIGIN );
			Shape circle = new Ellipse2D.Double( -6, -6, 12, 12 );
			Shape square = new Rectangle2D.Double( -6, -6, 12, 12 );
			shapes.put( Constants.STAR, star );
			shapes.put( Constants.HEXAGON, hex );
			shapes.put( Constants.PENTAGON, pent );
			shapes.put( Constants.DIAMOND, diamond );
			shapes.put( Constants.TRIANGLE, triangle );
			shapes.put( Constants.CIRCLE, circle );
			shapes.put( Constants.SQUARE, square );

			// Populate the legend hash, the "legend" shapes are correlated with their "normal"
			// shape types
			vertexShapeLegendHash.put( star, createStarL() );
			vertexShapeLegendHash.put( hex, createHexL() );
			vertexShapeLegendHash.put( pent, createPentL() );
			vertexShapeLegendHash.put( diamond, createRhombusL() );
			vertexShapeLegendHash.put( triangle, createUpTriangleL() );
			vertexShapeLegendHash.put( circle, new Ellipse2D.Double( 0, 0, 20, 20 ) );
			vertexShapeLegendHash.put( square, new Rectangle2D.Double( 0, 0, 40, 40 ) );

			// make our shape-to-name hash
			for ( String shapename : shapeNames ) {
				Shape shape = shapes.get( shapename );
				Shape shapel = vertexShapeLegendHash.get( shape );
				shapes.put( shapename + Constants.LEGEND, shapel );
				shapeNameLkp.put( shape, shapename );
				shapeNameLkp.put( shapel, shapename + Constants.LEGEND );
			}
		}

		/**
		 * Get the "legend" shape, associated with a given normal shape
		 *
		 * @param shape The normal shape
		 * @return The legend shape, or null if it is not found
		 */
		public Shape getLegendShape( Shape shape ) {
			Shape legendShape = vertexShapeLegendHash.get( shape );
			if ( legendShape == null ) {
				logger.warn( "Legend shape not found." );
			}
			return legendShape;
		}

		/**
		 * Get the next shape in the queue
		 *
		 * @return The next shape to associate with a vertex
		 */
		public Shape nextShape() {
			lastIndexChosen++;
			if ( lastIndexChosen == shapeNames.length ) {
				lastIndexChosen = 0;
			}
			String shapeName = this.shapeNames[lastIndexChosen];
			return shapes.get( shapeName );
		}

		/**
		 * Get the shape associated with a given name/key
		 *
		 * @param key The name or key
		 * @return The appropriate shape instance, or null if not found
		 */
		public Shape getNamedShape( String key ) {
			if ( shapes.containsKey( key ) ) {
				return shapes.get( key );
			}
			else {
				logger.warn( "Warning - No named shape exists for the key : " + key );
				return null;
			}
		}

		/**
		 * Get all of the Shapes which are prescribed within this generator
		 *
		 * @return An array composed of each shape
		 */
		public Shape[] getAllShapes() {
			Shape array[] = new Shape[shapeNames.length];
			for ( int i = 0; i < shapeNames.length; i++ ) {
				array[i] = shapes.get( shapeNames[i] );
			}
			return array;
		}

		/**
		 * Creates a star shape.
		 *
		 * @return Star
		 *
		 */
		private Shape createStar() {
			double x = .5;
			double points[][] = { { 0 * x, -15 * x }, { 4.5 * x, -5 * x },
			{ 14.5 * x, -5 * x }, { 7.5 * x, 3 * x },
			{ 10.5 * x, 13 * x }, { 0 * x, 7 * x },
			{ -10.5 * x, 13 * x }, { -7.5 * x, 3 * x },
			{ -14.5 * x, -5 * x }, { -4.5 * x, -5 * x }, { 0, -15 * x } };
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
		private Shape createStarL() {
			double points[][] = { { 10, 0 }, { 13, 6.66 }, { 20, 6.66 },
			{ 14.66, 12 }, { 16.66, 18.66 }, { 10, 14 },
			{ 3.33, 18.66 }, { 5.33, 12 }, { 0, 6.66 }, { 7, 6.66 },
			{ 10, 0 } };

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
		 * @return Hexagon
		 */
		private Shape createHex( final double s ) {
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
		private Shape createHexL() {
			double points[][] = { { 20, 10 }, { 15, 0 }, { 5, 0 }, { 0, 10 },
			{ 5, 20 }, { 15, 20 } };

			final GeneralPath pent = new GeneralPath();
			pent.moveTo( points[0][0], points[0][1] );

			for ( int k = 1; k < points.length; k++ ) {
				pent.lineTo( points[k][0], points[k][1] );
			}

			pent.closePath();
			return pent;
		}

		/**
		 * Creates a pentagon shape
		 *
		 * @param s start position (X-coordinate) for drawing the pentagon
		 * @return Pentagon
		 */
		private Shape createPent( final double s ) {
			GeneralPath hexagon = new GeneralPath();
			hexagon.moveTo( (float) Math.cos( Math.PI / 10 ) * s,
					(float) Math.sin( Math.PI / 10 ) * ( -s ) );
			for ( int i = 0; i < 5; i++ ) {
				hexagon.lineTo(
						(float) Math.cos( i * 2 * Math.PI / 5 + Math.PI / 10 )
						* s,
						(float) Math.sin( i * 2 * Math.PI / 5 + Math.PI / 10 )
						* ( -s ) );
			}
			hexagon.closePath();
			return hexagon;
		}

		/**
		 * Creates a pentagon shape for the legend.
		 *
		 * @return Pentagon
		 */
		private Shape createPentL() {
			double points[][] = { { 10, 0 }, { 19.510565163, 6.90983005625 },
			{ 15.8778525229, 18.0901699437 },
			{ 4.12214747708, 18.0901699437 },
			{ 0.48943483704, 6.90983005625 } };

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
		 * @return Rhombus
		 */
		private Shape createRhombus( final double s ) {
			double points[][] = { { 0, -s }, { -s, 0 }, { 0, s }, { s, 0 }, };
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
		private Shape createRhombusL() {
			double points2[][] = { { 10, 0 }, { 0, 10 }, { 10, 20 },
			{ 20, 10 }, };
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
		private Shape createUpTriangle( final double s ) {
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
		private Shape createUpTriangleL() {
			GeneralPath p0 = new GeneralPath(); // triangle

			p0.moveTo( 10, 0 );
			p0.lineTo( 20, 20 );
			p0.lineTo( 0, 20 );
			p0.closePath();
			return p0;
		}

	}
}
