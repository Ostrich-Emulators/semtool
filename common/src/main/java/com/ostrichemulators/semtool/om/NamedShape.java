/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author ryan
 */
public enum NamedShape {

	CIRCLE( 0 ), SQUARE( -1 ), STAR3( 3, true ), DIAMOND( 4 ), STAR5( 5, true ),
	HEXAGON( 6 ), STAR6( 6, true ), PENTAGON( 5 ), TRIANGLE( 3 ), STAR4( 4, true );

	int points;
	boolean star;

	NamedShape( int points, boolean star ) {
		this.star = star;
		this.points = points;
	}

	NamedShape( int points ) {
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

	/**
	 * Creates a star shape
	 * @param size how big to make the star
	 * @param points how many points does it have?
	 * @return a star
	 */
	public static Shape createStar( double size, int points ) {
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

	public static Shape createPolygon( double size, int points ) {
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
};
