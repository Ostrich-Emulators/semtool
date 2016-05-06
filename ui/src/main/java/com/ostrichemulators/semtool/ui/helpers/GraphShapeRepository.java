package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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

	private static final double DEFAULT_SIZE = 24d;
	/**
	 * A hash which tracks URIs that have been assigned shapes, so that we can
	 * consistently assign shapes through recall
	 */
	private final Map<URI, Shape> vertexShapeHash = new HashMap<>();

	/**
	 * The utility class instance responsible for shape generation
	 */
	private ShapeGenerator shapeGenerator;
	/**
	 * The logger that this class will use
	 */
	private final Logger logger = Logger.getLogger( GraphShapeRepository.class );

	private final Map<Shape, String> shapeNameLkp = new HashMap<>();

	/**
	 * Default constructor
	 */
	public GraphShapeRepository() {
		shapeGenerator = new ShapeGenerator( DEFAULT_SIZE );
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

	public Shape getShape( URI type, URI instance ) {
		return getShape( type );
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
		double side = svshape.getBounds2D().getWidth();
		return shapeGenerator.createHex( side * 1.25 );
	}

	/**
	 * Clears all initialized shape data
	 */
	public void clearAll() {
		vertexShapeHash.clear();
		shapeGenerator = new ShapeGenerator( DEFAULT_SIZE );
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

		public ShapeGenerator( double size ) {

			// Create the shapes to populate the shapes hash
			Shape star = createStar( size, 5 );
			Shape hex = createPolygon( size, 6 );
			Shape pent = createPolygon( size, 5 );
			Shape diamond = createPolygon( size, 4 );
			Shape triangle = createPolygon( size, 3 );
			Shape circle = new Ellipse2D.Double( 0, 0, size, size );
			Shape square = new Rectangle2D.Double( 0, 0, size, size );
			shapes.put( Constants.STAR, star );
			shapes.put( Constants.HEXAGON, hex );
			shapes.put( Constants.PENTAGON, pent );
			shapes.put( Constants.DIAMOND, diamond );
			shapes.put( Constants.TRIANGLE, triangle );
			shapes.put( Constants.CIRCLE, circle );
			shapes.put( Constants.SQUARE, square );

			// make our shape-to-name hash
			for ( String shapename : shapeNames ) {
				Shape shape = shapes.get( shapename );
				shapeNameLkp.put( shape, shapename );
			}
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
			Shape array[] = new Shape[shapes.size()];
			new ArrayList<>( shapes.values() ).toArray( array );
			return array;
		}

		/**
		 * Creates a star shape.
		 *
		 * @return Star
		 *
		 */
		private Shape createStar( double size, int points ) {
			// we're (imagining) drawing two concentric circles
			// and walking around both, while putting points.
			// then just connect the inner points to the outer ones,
			// and we have our star
			//Ellipse2D outer = new Ellipse2D.Double( 0, 0, size, size );
			//Ellipse2D inner = new Ellipse2D.Double( size / 3, size / 3, size / 3, size / 3 );

			final double center = size / 2;
			final double outerRadius = size / 2;
			final double innerRadius = size / 6;
			final double pointAngle = 360 / points;

			double x[] = new double[points * 2];
			double y[] = new double[points * 2];

			GeneralPath star = new GeneralPath();
			// star.append( outer, false );
			// star.append( inner, false );
			double offset = Math.PI / 2;

			for ( int i = 0; i < points; i++ ) {
				double innerDegree = ( Math.toRadians( i - 0.5 ) * pointAngle ) - offset;
				double innerX = innerRadius * Math.cos( innerDegree ) + center;
				double innerY = innerRadius * Math.sin( innerDegree ) + center;

				double outerDegree = ( Math.toRadians( i ) * pointAngle ) - offset;
				double outerX = outerRadius * Math.cos( outerDegree ) + center;
				double outerY = outerRadius * Math.sin( outerDegree ) + center;

				int pos = 2 * i;
				x[pos] = innerX;
				y[pos] = innerY;
				x[pos + 1] = outerX;
				y[pos + 1] = outerY;

				//star.append( new Ellipse2D.Double( x[pos], y[pos], 1, 1 ), false );
				//star.append( new Ellipse2D.Double( x[pos + 1], y[pos + 1], 1, 1 ), false );
			}

			star.moveTo( x[0], y[0] );
			for ( int i = 1; i < x.length; i++ ) {
				star.lineTo( x[i], y[i] );
			}
			star.closePath();
			return star;
		}

		private Shape createPolygon( double size, int points ) {
			// we're (imagining) drawing a circle, and walking aroun
			// walking around it while putting points.
			// Ellipse2D outer = new Ellipse2D.Double( 0, 0, size, size );

			final double center = size / 2;
			final double radius = size / 2;
			final double pointAngle = 360 / points;

			double x[] = new double[points];
			double y[] = new double[points];

			GeneralPath poly = new GeneralPath();
			// poly.append( outer, false );

			// back up our points by 90% so the shapes "point" up instead of to the right
			double offset = Math.PI / 2;

			for ( int i = 0; i < points; i++ ) {
				double outerDegree = ( Math.toRadians( i ) * pointAngle ) - offset;
				double outerX = radius * Math.cos( outerDegree ) + center;
				double outerY = radius * Math.sin( outerDegree ) + center;

				x[i] = outerX;
				y[i] = outerY;

				//poly.append( new Ellipse2D.Double( x[i], y[i], 1, 1 ), false );
			}

			poly.moveTo( x[0], y[0] );
			for ( int i = 1; i < x.length; i++ ) {
				poly.lineTo( x[i], y[i] );
			}
			poly.closePath();
			return poly;
		}

		/**
		 * Creates a shape.
		 *
		 * @param s the length of one side of the bounding box
		 * @return a shape
		 */
		private Shape createHex( double size ) {
			GeneralPath hexagon = new GeneralPath();
			hexagon.moveTo( 0, size / 2 );
			hexagon.lineTo( size / 4, size );
			hexagon.lineTo( size - size / 4, size );
			hexagon.lineTo( size, size / 2 );
			hexagon.lineTo( size - size / 4, 0 );
			hexagon.lineTo( size / 4, 0 );
			hexagon.closePath();
			return hexagon;
		}

		/**
		 * Creates a shape.
		 *
		 * @param s the length of one side of the bounding box
		 * @return a shape
		 */
		private Shape createPent( double size ) {
			final double degrees072 = Math.toRadians( 72 );
			double angle = 3 * degrees072; // starting angle

			GeneralPath hex = new GeneralPath();
			double x = size / 2;
			double y = 0;
			double side = size / 1.5;

			hex.moveTo( x, y );

			// draw from the top
			for ( int i = 0; i < 5; i++ ) {
				x = x + Math.cos( angle ) * side;
				y = y - Math.sin( angle ) * side;
				hex.lineTo( x, y );
				angle += degrees072;
			}

			return hex;
		}

		/**
		 * Creates a shape.
		 *
		 * @param s the length of one side of the bounding box
		 * @return a shape
		 */
		private Shape createRhombus( double size ) {
			final GeneralPath r = new GeneralPath();
			r.moveTo( 0, size / 2 );
			r.lineTo( size / 2, size );
			r.lineTo( size, size / 2 );
			r.lineTo( size / 2, 0 );
			r.closePath();
			return r;
		}

		/**
		 * Creates a shape.
		 *
		 * @param s the length of one side of the bounding box
		 * @return a shape
		 */
		private Shape createUpTriangle( double size ) {
			final GeneralPath p0 = new GeneralPath();
			p0.moveTo( 0, size );
			p0.lineTo( size, size );
			p0.lineTo( size / 2, 0 );
			p0.closePath();
			return p0;
		}
	}
}
