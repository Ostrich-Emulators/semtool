package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * The Graph Color Repository is responsible for providing colors to
 * Vertices and Edges semi-randomly, and also for maintaining consistency
 * of colors among types of Vertices and Edges. 
 * @author Wayne Warren
 *
 */
public class GraphColorRepository extends AbstractColorRepository {
	/** Hash which keeps track of Vertex colors, based on their type */
	private final Map<URI, SEMOSSVertexColor> vertexColorHash = new HashMap<URI, SEMOSSVertexColor>();
	/** Hash which keeps track of Edge colors, based on their type */
	private final Map<URI, SEMOSSVertexColor> edgeColorHash = new HashMap<URI, SEMOSSVertexColor>();
	/** The generator utility instance responsible for producing the colors for vertices */
	protected ColorGenerator vertexColorGenerator = new ColorGenerator();
	/** The generator utility instance responsible for producing the colors for edges (future feature) */
	protected ColorGenerator edgeColorGenerator = new ColorGenerator();
	/** The singleton instance */
	private static GraphColorRepository instance = null;
	
	/**
	 * Default constructor
	 */
	private GraphColorRepository(){

	}
	
	/**
	 * Get the singleton instance of this class
	 * @return
	 */
	public static GraphColorRepository instance(){
		if (instance == null){
			instance = new GraphColorRepository();
		}
		return instance;
	}
	
	/**
	 * Get a color appropriate for a vertex
	 * @param vertex The vertex to which the color will be associated in a graph visualization
	 * @return An appropriate color
	 */
	public SEMOSSVertexColor getColor(SEMOSSVertex vertex){
		URI typeURI =vertex.getType();
		return getColor(typeURI);
	}
	
	/**
	 * Get a color appropriate for a given "type" URI
	 * @param typeURI The type URI
	 * @return The appropriate color, null if it is not found
	 */
	public SEMOSSVertexColor getColor(URI typeURI){
		SEMOSSVertexColor color = null;
		if (typeURI == null){
			color =  vertexColorGenerator.nextNamedColor();
		}
		else if (vertexColorHash.containsKey(typeURI)){
			color = vertexColorHash.get(typeURI);
		}
		else {
			color = vertexColorGenerator.nextNamedColor();
			vertexColorHash.put(typeURI, color);
		}
		return color;
	}
	 
	/**
	 * Clear all history of Vertex-Color and Edge-Color association (future feature)
	 */
	public void clearAll(){
		vertexColorHash.clear();
		edgeColorHash.clear();
		vertexColorGenerator = new ColorGenerator();
		edgeColorGenerator = new ColorGenerator();
	}
	
	/**
	 * Sets the color for a vertex, as well as the name of the Color
	 * @param colorString String - The name/tag/handle of the Color, from the Constants class
	 * @param SEMOSSVertex vertex - the vertex whose color we are setting
	 */
	public boolean setColor( String colorString, SEMOSSVertex vertex ) {
		SEMOSSVertexColor svcolor = vertexColorGenerator.getNamedColor(colorString);
		if (svcolor != null){
			vertex.setColor(svcolor.color );
			vertex.setColorString( svcolor.name );
		}
		else {
			logger.warn("Named color not found: " + colorString);
		}
		return true;
	}
	

	/**
	 * Get all names of colors in the system
	 * @return An array of names/tags of Colors
	 */
	public String[] getAllColorNames(){
		return this.colorGenerator.getAllColorNames();
	}
	
	/**
	 * Get a color appropriate for a given Edge
	 * @param edge The edge that the color is to be assigned to
	 * @return The appropriate Color instance, or null
	 */
	public SEMOSSVertexColor getColor(SEMOSSEdge edge){
		URI typeURI = edge.getType();
		if (edgeColorHash.containsKey(typeURI)){
			return edgeColorHash.get(typeURI);
		}
		else {
			SEMOSSVertexColor color = edgeColorGenerator.nextNamedColor();
			if (color == null){
				logger.warn("No color found for edge.");
			}
			edgeColorHash.put(typeURI, color);
			return color;
		}
	}

}