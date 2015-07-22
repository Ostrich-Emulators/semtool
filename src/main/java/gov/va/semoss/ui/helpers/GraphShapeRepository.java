package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

public class GraphShapeRepository {

	private final Map<URI, SEMOSSVertexShape> vertexShapeHash = new HashMap<URI, SEMOSSVertexShape>();
	
	private final Map<SEMOSSVertexShape, SEMOSSVertexShape> vertexShapeLegendHash = new HashMap<SEMOSSVertexShape, SEMOSSVertexShape>();
	
	private ShapeGenerator shapeGenerator = new ShapeGenerator();
	
	// Do we still need a hash that correlates Vertex-type-URI to a shape string, rather than a shape?

	private static GraphShapeRepository instance = null;
	
	private final Logger logger = Logger.getLogger(GraphShapeRepository.class);
	
	private GraphShapeRepository(){
		
	}
	
	public static GraphShapeRepository instance(){
		if (instance == null){
			instance = new GraphShapeRepository();
		}
		return instance;
	}
	
	/**
	 * 
	 *
	 * @param vertex SEMOSSVertex - The type of the vertex (serves as a key in the
	 * hashtable)
	 * @param shapeString String the shape itself.
	 */
	public boolean setShape( String shapeString, SEMOSSVertex vertex ) {
		SEMOSSVertexShape shape = null;
		if (shapeString == null){
			shape = this.shapeGenerator.nextShape();
		}
		else {
			shape = this.shapeGenerator.getNamedShape(shapeString);
		}
		SEMOSSVertexShape legendShape = vertexShapeLegendHash.get(shape);
		vertex.setShape( shape.shape );
		vertex.setShapeLegend( legendShape.shape );
		vertex.setShapeString( shape.name );
		
		return true;
	}
	
	public SEMOSSVertexShape getShape(SEMOSSVertex vertex){
		URI typeURI =vertex.getType(); 
		return getShape(typeURI);
	}
	
	public SEMOSSVertexShape getShape(URI typeURI){
		// next check if it is specified in the properties file
		SEMOSSVertexShape newShape = null;
		try {
			String shapeStringSetInRDF_MapPropFile
				= DIHelper.getInstance().getProperty( typeURI.getLocalName() + "_SHAPE" );
			if ( shapeStringSetInRDF_MapPropFile != null
					&& DIHelper.getShape( shapeStringSetInRDF_MapPropFile ) != null ) {
				Shape shape = DIHelper.getShape( shapeStringSetInRDF_MapPropFile );
				newShape = new SEMOSSVertexShape(shapeStringSetInRDF_MapPropFile, shape);shape = shape;
			}
			else if (typeURI == null){
				newShape = shapeGenerator.nextShape();
			}
			else if (vertexShapeHash.containsKey(typeURI)){
				newShape = vertexShapeHash.get(typeURI);
			}
			else {
				newShape = shapeGenerator.nextShape();
				if (newShape == null){
					logger.warn("Warning - Derived shape was null for vertex: " + typeURI.toString());
				}
				else {
					vertexShapeHash.put(typeURI, newShape);
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return newShape;
	}
	
	public SEMOSSVertexShape getLegendShape(SEMOSSVertexShape svshape){
		return this.shapeGenerator.getLegendShape(svshape.shape);
	}
	
	public void clearAll(){
		vertexShapeHash.clear();
		shapeGenerator = new ShapeGenerator();
	}
	
	public String[] getAllShapeNames(){
		return shapeGenerator.shapeNames;
	}
	
	public Shape[] getAllShapes(){
		return shapeGenerator.getAllShapes();
	}
	
	private class ShapeGenerator {

		private HashMap<String, SEMOSSVertexShape> shapes = new HashMap<String, SEMOSSVertexShape>();
		
		private static final int UP_TRIANGLE_ORIGIN = 6;
		
		private static final int RHOMBUS_ORIGIN = 7;
		
		private static final int HEX_ORIGIN = 7;
		
		private static final int PENT_ORIGIN = 7;
		
		private int lastIndexChosen = -1;
		
		public final String[] shapeNames = {
				Constants.STAR,
				Constants.HEXAGON,
				Constants.PENTAGON,
				Constants.DIAMOND,
				Constants.TRIANGLE,
				Constants.CIRCLE,
				Constants.SQUARE,
		};
		
		public ShapeGenerator(){
			SEMOSSVertexShape star = new SEMOSSVertexShape(Constants.STAR, createStar());
			SEMOSSVertexShape hex = new SEMOSSVertexShape(Constants.HEXAGON, createHex(HEX_ORIGIN)); 
			SEMOSSVertexShape pent = new SEMOSSVertexShape(Constants.PENTAGON, createPent(PENT_ORIGIN));
			SEMOSSVertexShape diamond = new SEMOSSVertexShape(Constants.DIAMOND, createRhombus(RHOMBUS_ORIGIN));
			SEMOSSVertexShape triangle = new SEMOSSVertexShape(Constants.TRIANGLE, createUpTriangle(UP_TRIANGLE_ORIGIN));
			SEMOSSVertexShape circle = new SEMOSSVertexShape(Constants.CIRCLE, new Ellipse2D.Double(-6, -6, 12, 12));
			SEMOSSVertexShape square = new SEMOSSVertexShape(Constants.SQUARE, new Rectangle2D.Double(-6,-6,12, 12));
			shapes.put(Constants.STAR, star);
			shapes.put(Constants.HEXAGON, hex);
			shapes.put(Constants.PENTAGON, pent);
			shapes.put(Constants.DIAMOND, diamond);
			shapes.put(Constants.TRIANGLE, triangle);
			shapes.put(Constants.CIRCLE, circle);
			shapes.put(Constants.SQUARE, square);
			
			vertexShapeLegendHash.put(star, new SEMOSSVertexShape(Constants.STAR + Constants.LEGEND, createStarL()));
			vertexShapeLegendHash.put(hex, new SEMOSSVertexShape(Constants.HEXAGON + Constants.LEGEND, createHexL()));
			vertexShapeLegendHash.put(pent, new SEMOSSVertexShape(Constants.PENTAGON + Constants.LEGEND, createPentL()));
			vertexShapeLegendHash.put(diamond, new SEMOSSVertexShape(Constants.DIAMOND + Constants.LEGEND, createRhombusL()));
			vertexShapeLegendHash.put(triangle, new SEMOSSVertexShape(Constants.TRIANGLE + Constants.LEGEND, createUpTriangleL()));
			vertexShapeLegendHash.put(circle, new SEMOSSVertexShape(Constants.CIRCLE + Constants.LEGEND, new Ellipse2D.Double(0,0,20,20)));
			vertexShapeLegendHash.put(square, new SEMOSSVertexShape(Constants.SQUARE + Constants.LEGEND, new Rectangle2D.Double(0,0,40, 40)));
		}
		
		
		public SEMOSSVertexShape getLegendShape(Shape shape) {
			return vertexShapeLegendHash.get(shape);
		}


		public SEMOSSVertexShape nextShape() {
			lastIndexChosen++;
			if (lastIndexChosen == shapeNames.length){
				lastIndexChosen = 0;
			}
			String shapeName = this.shapeNames[lastIndexChosen];
			SEMOSSVertexShape shape = shapes.get(shapeName).clone();
			return shape;
		}
		 
		public SEMOSSVertexShape getNamedShape(String key){
			if (shapes.containsKey(key)){
				return shapes.get(key);
			}
			else {
				logger.warn("Warning - No named shape exists for the key : " + key);
				return null;
			}
		}
		
		public Shape[] getAllShapes(){
			Collection<SEMOSSVertexShape> collection = shapes.values();
			Iterator<SEMOSSVertexShape> iterator = collection.iterator();
			Shape[] array = new Shape[collection.size()];
			int counter = 0;
			while (iterator.hasNext()){
				SEMOSSVertexShape svshape = iterator.next();
				array[counter] = svshape.shape;
				counter++;
			}
			return array;
		}
		
		/**
		 * Creates a star shape.
		 * 
		 * @return Star
		 * */
		private Shape createStar() {
			double x = .5;
			double points[][] = { { 0 * x, -15 * x }, { 4.5 * x, -5 * x },
					{ 14.5 * x, -5 * x }, { 7.5 * x, 3 * x },
					{ 10.5 * x, 13 * x }, { 0 * x, 7 * x },
					{ -10.5 * x, 13 * x }, { -7.5 * x, 3 * x },
					{ -14.5 * x, -5 * x }, { -4.5 * x, -5 * x }, { 0, -15 * x } };
			final GeneralPath star = new GeneralPath();
			star.moveTo(points[0][0], points[0][1]);

			for (int k = 1; k < points.length; k++)
				star.lineTo(points[k][0], points[k][1]);
			
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
			star.moveTo(points[0][0], points[0][1]);

			for (int k = 1; k < points.length; k++)
				star.lineTo(points[k][0], points[k][1]);

			star.closePath();
			return star;
		}

		/**
		 * Creates a hexagon shape.
		 * 
		 * @param s
		 *            start position (X-coordinate) for drawing the hexagon.
		 * 
		 * @return Hexagon
		 */
		private Shape createHex(final double s) {
			GeneralPath hexagon = new GeneralPath();
			hexagon.moveTo(s, 0);
			for (int i = 0; i < 6; i++)
				hexagon.lineTo((float) Math.cos(i * Math.PI / 3) * s,
						(float) Math.sin(i * Math.PI / 3) * s);
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
			pent.moveTo(points[0][0], points[0][1]);

			for (int k = 1; k < points.length; k++)
				pent.lineTo(points[k][0], points[k][1]);

			pent.closePath();
			return pent;
		}

		/**
		 * Creates a pentagon shape.
		 * 
		 * @param s
		 *            start position (X-coordinate) for drawing the pentagon
		 * 
		 * @return Pentagon
		 */
		private Shape createPent(final double s) {
			GeneralPath hexagon = new GeneralPath();
			hexagon.moveTo((float) Math.cos(Math.PI / 10) * s,
					(float) Math.sin(Math.PI / 10) * (-s));
			for (int i = 0; i < 5; i++)
				hexagon.lineTo(
						(float) Math.cos(i * 2 * Math.PI / 5 + Math.PI / 10)
								* s,
						(float) Math.sin(i * 2 * Math.PI / 5 + Math.PI / 10)
								* (-s));
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
			pent.moveTo(points[0][0], points[0][1]);

			for (int k = 1; k < points.length; k++)
				pent.lineTo(points[k][0], points[k][1]);

			pent.closePath();
			return pent;
		}

		/**
		 * Creates a rhombus shape.
		 * 
		 * @param s
		 *            start position (X-coordinate) for drawing the rhombus
		 * 
		 * @return Rhombus
		 */
		private Shape createRhombus(final double s) {
			double points[][] = { { 0, -s }, { -s, 0 }, { 0, s }, { s, 0 }, };
			final GeneralPath r = new GeneralPath();
			r.moveTo(points[0][0], points[0][1]);

			for (int k = 1; k < points.length; k++)
				r.lineTo(points[k][0], points[k][1]);

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
			r.moveTo(points2[0][0], points2[0][1]);

			for (int k = 1; k < points2.length; k++)
				r.lineTo(points2[k][0], points2[k][1]);

			r.closePath();
			return r;
		}

		/**
		 * Creates a triangle.
		 * 
		 * @param s
		 *            start position (X-coordinate) for drawing the triangle.
		 * 
		 * @return Triangle
		 */
		private Shape createUpTriangle(final double s) {
			final GeneralPath p0 = new GeneralPath();
			p0.moveTo(0, -s);
			p0.lineTo(s, s);
			p0.lineTo(-s, s);
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

			p0.moveTo(10, 0);
			p0.lineTo(20, 20);
			p0.lineTo(0, 20);
			p0.closePath();
			return p0;
		}
	}
	
	
	public class SEMOSSVertexShape {
		
		public final String name;
		
		public final Shape shape;
		
		public SEMOSSVertexShape(String name, Shape shape){
			this.name = name;
			this.shape = shape;
		}
		
		public SEMOSSVertexShape clone(){
			if (this.shape instanceof GeneralPath){
				return new SEMOSSVertexShape(this.name, (Shape)((GeneralPath)shape).clone());
			}
			else if (this.shape instanceof Ellipse2D){
				return new SEMOSSVertexShape(this.name, (Shape)((Ellipse2D)shape).clone());
			}
			else if (this.shape instanceof Rectangle2D){
				return new SEMOSSVertexShape(this.name, (Shape)((Rectangle2D)shape).clone());
			}
			else {
				return null;
			}
		}
		
	}	
}