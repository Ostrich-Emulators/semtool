package gov.va.semoss.ui.helpers;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * The Graph Color Repository is resonsible for providing colors to
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
	

	protected ColorGenerator vertexColorGenerator = new ColorGenerator();

	protected ColorGenerator edgeColorGenerator = new ColorGenerator();
	
	private static GraphColorRepository instance = null;
	
	private GraphColorRepository(){
		// Populate the repository with the standard colors from the 
		// SEMOSS properties
		
		// Set these colors in the random color generator so that we don't duplicate
		// the presets
	}
	
	public static GraphColorRepository instance(){
		if (instance == null){
			instance = new GraphColorRepository();
		}
		return instance;
	}
	
	public SEMOSSVertexColor getColor(SEMOSSVertex vertex){
		URI typeURI =vertex.getType();
		return getColor(typeURI);
	}
	
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
	 
	public void clearAll(){
		vertexColorHash.clear();
		edgeColorHash.clear();
		vertexColorGenerator = new ColorGenerator();
		edgeColorGenerator = new ColorGenerator();
	}
	
	/**
	 * Method setColor. Setes the color for a vertex
	 *
	 * @param colorString String - the color itself
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
	
	public String[] getAllColorNames(){
		return this.colorGenerator.getAllColorNames();
	}
	
	public SEMOSSVertexColor getColor(SEMOSSEdge edge){
		URI typeURI = edge.getType();
		if (edgeColorHash.containsKey(typeURI)){
			return edgeColorHash.get(typeURI);
		}
		else {
			SEMOSSVertexColor color = edgeColorGenerator.nextNamedColor();
			edgeColorHash.put(typeURI, color);
			return color;
		}
	}

}