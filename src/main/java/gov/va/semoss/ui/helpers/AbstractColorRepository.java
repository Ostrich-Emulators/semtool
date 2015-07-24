package gov.va.semoss.ui.helpers;

import gov.va.semoss.util.Constants;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The Abstract Color Repository provides lots of common attributes and
 * utility functions for child implementations.
 * @author Wayne Warren
 */
public abstract class AbstractColorRepository {
	/** A listing of the standard colors provided in the system, using the tag/key from the 
	 * Constants class as the key and the color itself as the value  */
	private HashMap<String, Color> standardColors = new HashMap<String, Color>();
	/** The logger used to post messages */
	protected final Logger logger = Logger.getLogger(AbstractColorRepository.class);
	/** The generator utility class that produces the colors */
	protected final ColorGenerator colorGenerator = new ColorGenerator();
	
	/**
	 * Default constructor
	 */
	protected AbstractColorRepository(){
		initialize();
	}
	
	/**
	 * Initialize the basic attributes of the class
	 */
	private void initialize(){
		Color blue = new Color(31, 119, 180);
		Color green = new Color(44, 160, 44);
		Color red = new Color(214, 39, 40);
		Color brown = new Color(143, 99, 42);
		Color yellow = new Color(254, 208, 2);
		Color orange = new Color(255, 127, 14);
		Color purple = new Color(148, 103, 189);
		Color aqua = new Color(23, 190, 207);
		Color pink = new Color(241, 47, 158);
		Color transparent = new Color(255, 255, 255, 0);
		
		standardColors.put(Constants.BLUE, blue);
		standardColors.put(Constants.GREEN, green);
		standardColors.put(Constants.RED, red);
		standardColors.put(Constants.BROWN, brown);
		standardColors.put(Constants.MAGENTA, pink);
		standardColors.put(Constants.YELLOW, yellow);
		standardColors.put(Constants.ORANGE, orange);
		standardColors.put(Constants.PURPLE, purple);
		standardColors.put(Constants.AQUA, aqua);
		// IMPORTANT - DIHelper had a horrible lookup which ignores case, and doesn't
		// use the constant.  Watch out for this.
		standardColors.put(Constants.TRANSPARENT, transparent);
	}
	
	/**
	 * Get the color named by the given key
	 * @param key The name of the color
	 * @return The color
	 */
	public Color getColor(String key){
		return standardColors.get(key);
	}
	
	/**
	 * Get all standard color names listed in the system
	 * @return The color names
	 */
	public String[] getAllColorNames(){
		return colorGenerator.getAllColorNames();
	}
	
	/**
	 * The utility class which generates all of the colors
	 * @author Wayne Warren
	 *
	 */
	protected class ColorGenerator {
		/** The values of red that have been used by the nextRandomColor() function */
		private HashSet<Integer> reds = new HashSet<Integer>();
		/** The values of green that have been used by the nextRandomColor() function */
		private HashSet<Integer> greens = new HashSet<Integer>();
		/** The values of blues that have been used by the nextRandomColor() function */
		private HashSet<Integer> blues = new HashSet<Integer>();
		/** The series of named colors available in the system */
		private final Map<String, SEMOSSVertexColor> named_colors = new HashMap<String, SEMOSSVertexColor>();
		/** The last index used in the nextColor() call */
		private int lastNamedIndexPicked = -1;
		/** The full complement of the named color tags in the system */
		private String[] allColorNames = new String[]{
				Constants.BLUE,
				Constants.GREEN,
				Constants.RED,
				Constants.BROWN,
				Constants.MAGENTA,
				Constants.YELLOW,
				Constants.ORANGE,
				Constants.PURPLE,
				Constants.AQUA
		};
		
		/**
		 * Default constructor
		 */
		public ColorGenerator(){
			Color blue = new Color(31, 119, 180);
			Color green = new Color(44, 160, 44);
			Color red = new Color(214, 39, 40);
			Color brown = new Color(143, 99, 42);
			Color yellow = new Color(254, 208, 2);
			Color orange = new Color(255, 127, 14);
			Color purple = new Color(148, 103, 189);
			Color aqua = new Color(23, 190, 207);
			Color pink = new Color(241, 47, 158);
			
			named_colors.put(Constants.BLUE, new SEMOSSVertexColor(Constants.BLUE, blue));
			named_colors.put(Constants.GREEN, new SEMOSSVertexColor(Constants.GREEN, green));
			named_colors.put(Constants.RED, new SEMOSSVertexColor(Constants.RED, red));
			named_colors.put(Constants.BROWN, new SEMOSSVertexColor(Constants.BROWN, brown));
			named_colors.put(Constants.MAGENTA, new SEMOSSVertexColor(Constants.MAGENTA, pink));
			named_colors.put(Constants.YELLOW, new SEMOSSVertexColor(Constants.YELLOW, yellow));
			named_colors.put(Constants.ORANGE, new SEMOSSVertexColor(Constants.ORANGE, orange));
			named_colors.put(Constants.PURPLE, new SEMOSSVertexColor(Constants.PURPLE, purple));
			named_colors.put(Constants.AQUA, new SEMOSSVertexColor(Constants.AQUA, aqua));
		}
		
		/**
		 * Get the next random color
		 * @return A named color
		 */
		public Color nextRandomColor(){
			Integer red = new Integer(255);
			Integer green = new Integer(255);
			Integer blue = new Integer(255);
			red = nextValue(reds);
			green = nextValue(greens);
			blue = nextValue(blues);
			Color color = new Color(red, green, blue);
			return color;
		}
		
		/**
		 * Get the next named color, when the choices have been exhausted the
		 * color choices will continue from the start
		 * @param set
		 * @return
		 */
		private Integer nextValue(HashSet<Integer> set){
			Integer selectedValue = new Integer(255);
			Integer divisor = 2;
			while (set.contains(selectedValue)){
				selectedValue = selectedValue/divisor;
				for (int i=0; i<divisor; i++){
					Integer transformedValue = selectedValue * (i + 1);
					if (!set.contains(transformedValue)){
						set.add(transformedValue);
						return transformedValue;
					}
				}
				divisor++;
			}
			set.add(selectedValue);
			return selectedValue;
		}
		
		/**
		 * Get the next named color, when the choices have been exhausted the
		 * color choices will continue from the start
		 * @return A named color
		 */
		public SEMOSSVertexColor nextNamedColor(){
			lastNamedIndexPicked++;
			if (lastNamedIndexPicked == allColorNames.length){
				lastNamedIndexPicked = 0;
			}
			return named_colors.get(allColorNames[lastNamedIndexPicked]);
		}
		
		/**
		 * Get a color represented by a given name/key
		 * @param name The name of the color, from the Constants file
		 * @return The color 
		 */
		public SEMOSSVertexColor getNamedColor(String name){
			SEMOSSVertexColor namedColor = named_colors.get(name);
			if (namedColor == null){
				logger.warn("No named color referred to by: " + name);
			}
			return namedColor;
		}
		
		/**
		 * Get all of the named colors in the system
		 * @return An array of the named colors
		 */
		public SEMOSSVertexColor[] getAllNamedColors(){
			Collection<SEMOSSVertexColor> collection = named_colors.values();
			SEMOSSVertexColor[] colorArray = new SEMOSSVertexColor[collection.size()];
			collection.toArray(colorArray);
			return colorArray;
		}
		
		/**
		 * Get all color names in the system
		 * @return An array of color names, corresponding to those in the Constants class
		 */
		public String[] getAllColorNames(){
			return allColorNames;
		}
	}
	
	/**
	 * A convenience class designed to contain a color as well as its name
	 * @author Wayne Warren
	 *
	 */
	public class SEMOSSVertexColor {
		/** The name of the color */
		public final String name;
		/** The color */
		public final Color color;
		
		/**
		 * Default constructor
		 * @param name The name of the color
		 * @param color The color itself
		 */
		public SEMOSSVertexColor(String name, Color color){
			this.name = name;
			this.color = color;
		}
	}
}