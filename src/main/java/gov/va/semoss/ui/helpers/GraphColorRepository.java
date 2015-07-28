package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * The Graph Color Repository is responsible for providing colors to Vertices
 * and Edges semi-randomly, and also for maintaining consistency of colors among
 * types of Vertices and Edges.
 *
 * @author Wayne Warren
 *
 */
public class GraphColorRepository extends AbstractColorRepository {

	/**
	 * Hash which keeps track of Vertex colors, based on their type
	 */
	private final Map<URI, Color> vertexColorHash = new HashMap<>();
	/**
	 * Hash which keeps track of Edge colors, based on their type
	 */
	private final Map<URI, Color> edgeColorHash = new HashMap<>();
	/**
	 * The generator utility instance responsible for producing the colors for
	 * vertices
	 */
	protected ColorGenerator vertexColorGenerator = new ColorGenerator();
	/**
	 * The generator utility instance responsible for producing the colors for
	 * edges (future feature)
	 */
	protected ColorGenerator edgeColorGenerator = new ColorGenerator();
	/**
	 * The singleton instance
	 */
	private static GraphColorRepository instance = null;

	/**
	 * Default constructor
	 */
	private GraphColorRepository() {

	}

	/**
	 * Get the singleton instance of this class
	 *
	 * @return
	 */
	public static GraphColorRepository instance() {
		if ( instance == null ) {
			instance = new GraphColorRepository();
		}
		return instance;
	}

	/**
	 * Get a color appropriate for a given "type" URI
	 *
	 * @param typeURI The type URI
	 * @return The appropriate color, null if it is not found
	 */
	public Color getColor( URI typeURI ) {
		Color color = null;
		if ( typeURI == null ) {
			color = vertexColorGenerator.nextNamedColor();
		}
		else if ( vertexColorHash.containsKey( typeURI ) ) {
			color = vertexColorHash.get( typeURI );
		}
		else {
			color = vertexColorGenerator.nextNamedColor();
			vertexColorHash.put( typeURI, color );
		}
		return color;
	}

	/**
	 * Clear all history of Vertex-Color and Edge-Color association (future
	 * feature)
	 */
	public void clearAll() {
		vertexColorHash.clear();
		edgeColorHash.clear();
		vertexColorGenerator = new ColorGenerator();
		edgeColorGenerator = new ColorGenerator();
	}

	/**
	 * Get all names of colors in the system
	 *
	 * @return An array of names/tags of Colors
	 */
	@Override
	public String[] getAllColorNames() {
		return this.colorGenerator.getAllColorNames();
	}

	/**
	 * Get the name associated with a given color
	 *
	 * @param color The color for which a corresponding name is needed
	 * @return The corresponding name
	 */
	public String getColorName( Color color ) {
		for ( String colorname : this.colorGenerator.getAllColorNames() ) {
			if ( color.equals( this.colorGenerator.getNamedColor( colorname ) ) ) {
				return colorname;
			}
		}
		return null;
	}
}
