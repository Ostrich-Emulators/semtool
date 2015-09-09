package gov.va.semoss.ui.helpers;



import gov.va.semoss.om.AbstractColorRepository;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;


/**
 * The Graph Color Repository is responsible for providing colors to Vertices
 * and Edges from the Semoss.properties.  If the color is not found the colors 
 * gets generated semi-randomly, and also for maintaining consistency of colors
 * among types of Vertices and Edges.
 *
 * @author Wayne Warren
 * Extended By Desai Siddabathula
 *
 */
public class DynamicColorRepository extends AbstractColorRepository {

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
	private static DynamicColorRepository instance = null;

	/**
	 * Default constructor
	 */
	private DynamicColorRepository() {

	}

	/**
	 * Get the singleton instance of this class
	 *
	 * @return
	 */
	public static DynamicColorRepository instance() {
		if ( instance == null ) {
			instance = new DynamicColorRepository();
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
			color = getColor( Constants.TRANSPARENT );
		}
		else if ( vertexColorHash.containsKey( typeURI ) ) {
			color = vertexColorHash.get( typeURI );
		}
		else {
			
			// Check if the color exist in the file
			String colorName
			= DIHelper.getInstance().getProperty( typeURI.getLocalName() + "_COLOR" );

			if ( colorName == null ) {
				//If no color found use the random color generator
				color = vertexColorGenerator.nextNamedColor();
			}else{
				  color = this.colorGenerator.getNamedColor(colorName);
			}
			vertexColorHash.put( typeURI, color );
			
			
		}
		return color;
	}
	
	/**
	 * 
	 * @param typeURI
	 * @param color
	 */
	  public void updateColor(URI typeURI, Color color)
	  {
		  vertexColorHash.put( typeURI, color );
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
