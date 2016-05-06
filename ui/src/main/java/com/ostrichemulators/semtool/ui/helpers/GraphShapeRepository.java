package com.ostrichemulators.semtool.ui.helpers;

import com.ostrichemulators.semtool.util.DIHelper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
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

	public static enum Shapes {

		CIRCLE( 0 ), SQUARE( -1 ), STAR3( 3, true ), DIAMOND( 4 ), STAR5( 5, true ),
		HEXAGON( 6 ), STAR6( 6, true ), PENTAGON( 5 ), TRIANGLE( 3 ), STAR4( 4, true );

		int points;
		boolean star;

		Shapes( int points, boolean star ) {
			this.star = star;
			this.points = points;
		}

		Shapes( int points ) {
			this( points, false );
		}

		public boolean isStar() {
			return star;
		}

		public boolean isSquare() {
			return points < 0;
		}

		public int numPoints() {
			return points;
		}

		public Shape getShape( double size ) {
			if ( star ) {
				return createStar( size, points );
			}
			else if ( isSquare() ) {
				// the square looks too big compared to the other shapes, so make it a little smaller
				return new Rectangle2D.Double( 2, 2, size - 4, size - 4 );
			}
			else if ( 0 == points ) {
				return new Ellipse2D.Double( 0, 0, size, size );
			}
			else {
				return createPolygon( size, points );
			}
		}
	};
	private static final double DEFAULT_SIZE = 24;

	private final Map<URI, Shape> vertexShapeHash = new HashMap<>();
	private final Logger logger = Logger.getLogger( GraphShapeRepository.class );
	private int padding = 2;
	private double defaultSize;

	/**
	 * The last shape index chosen when nextShape() was called
	 */
	private Shapes lastShapeChosen = Shapes.CIRCLE;

	/**
	 * Default constructor
	 */
	public GraphShapeRepository() {
		this( DEFAULT_SIZE );
	}

	public GraphShapeRepository( double sz ) {
		defaultSize = sz;
	}

	public void setDefaultSize( double sz ) {
		defaultSize = sz;
	}

	public void setIconPadding( int padding ) {
		this.padding = padding;
	}

	public Shape getShape( URI type, URI instance ) {
		return getShape( type );
	}

	public Shape getShape( Shapes s ) {
		return s.getShape( defaultSize );
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
			newShape = Shapes.CIRCLE.getShape( defaultSize );
		}
		else {
			String shapename
					= DIHelper.getInstance().getProperty( typeURI.getLocalName() + "_SHAPE" );
			// This is the last surviving call to DIHelper in the shape/color area, 
			// mostly because a full analysis hasn't been completed regarding what this 
			// properties-style lookup might entail
			if ( null != shapename ) {
				try {
					newShape = Shapes.valueOf( shapename ).getShape( defaultSize );
				}
				catch ( Exception x ) {
					// don't care...just pick a new shape
				}
			}

			if ( null == newShape && vertexShapeHash.containsKey( typeURI ) ) {
				newShape = vertexShapeHash.get( typeURI );
			}

			if ( null == newShape ) {
				newShape = nextShape().getShape( defaultSize );
				vertexShapeHash.put( typeURI, newShape );
			}

			if ( newShape == null ) {
				logger.warn( "Warning - Derived shape was null: " + typeURI.toString() );
			}
		}
		return newShape;
	}

	public ImageIcon createIcon( Shapes type ) {
		return createIcon( type.getShape( defaultSize - ( 2 * padding ) ),
				defaultSize, padding );
	}

	public ImageIcon createIcon( Shape shape ) {
		return createIcon( shape, defaultSize, padding );
	}

	public static ImageIcon createIcon( Shapes type, double size, double padding ) {
		return createIcon( type.getShape( size - ( 2 * padding ) ), size, padding );
	}

	public static ImageIcon createIcon( Shape shape, double size, double padding ) {
		BufferedImage bi = new BufferedImage( (int) size, (int) size,
				BufferedImage.TYPE_INT_ARGB );

		// Get the buffered image's graphics context
		Graphics2D g = bi.createGraphics();
		g.translate( padding, padding );

		// make it look nice (hopefully)
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g.setPaint( Color.BLACK );
		g.draw( shape );
		g.dispose();

		return new ImageIcon( bi );
	}

	public void setShape( URI instanceOrType, Shape shape ) {
		vertexShapeHash.put( instanceOrType, shape );
	}

	/**
	 * Get the next shape in the queue
	 *
	 * @return The next shape to associate with a vertex
	 */
	public Shapes nextShape() {
		int next = ( lastShapeChosen.ordinal() + 1 ) % Shapes.values().length;
		lastShapeChosen = Shapes.values()[next];
		return lastShapeChosen;
	}

	/**
	 * Creates a star shape.
	 *
	 * @return Star
	 *
	 */
	private static Shape createStar( double size, int points ) {
		// we're (imagining) drawing two concentric circles
		// and walking around both, while putting points.
		// then just connect the inner points to the outer ones,
		// and we have our star
		//Ellipse2D outer = new Ellipse2D.Double( 0, 0, size, size );
		//Ellipse2D inner = new Ellipse2D.Double( size / 3, size / 3, size / 3, size / 3 );

		final double center = size / 2;
		final double outerRadius = size / 2;
		final double innerRadius = size / 4;
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

	private static Shape createPolygon( double size, int points ) {
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
}
