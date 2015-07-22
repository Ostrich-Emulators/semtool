package gov.va.semoss.ui.helpers;

import gov.va.semoss.util.Constants;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

public class AbstractColorRepository {
	
	private HashMap<String, Color> standardColors = new HashMap<String, Color>();
	
	protected final Logger logger = Logger.getLogger(AbstractColorRepository.class);
	
	protected final ColorGenerator colorGenerator = new ColorGenerator();
	
	public AbstractColorRepository(){
		initialize();
	}
	
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
	
	
	public Color getColor(String key){
		return standardColors.get(key);
	}
	
	public String[] getAllColorNames(){
		return colorGenerator.getAllColorNames();
	}
	
	
	protected class ColorGenerator {
		
		private HashSet<Integer> reds = new HashSet<Integer>();
		
		private HashSet<Integer> greens = new HashSet<Integer>();
		
		private HashSet<Integer> blues = new HashSet<Integer>();
		
		private final Map<String, SEMOSSVertexColor> named_colors = new HashMap<String, SEMOSSVertexColor>();
		
		private int lastNamedIndexPicked = -1;
		
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
		
		
		public SEMOSSVertexColor nextNamedColor(){
			lastNamedIndexPicked++;
			if (lastNamedIndexPicked == allColorNames.length){
				lastNamedIndexPicked = 0;
			}
			return named_colors.get(allColorNames[lastNamedIndexPicked]);
		}
		
		public SEMOSSVertexColor getNamedColor(String name){
			return named_colors.get(name);
		}
		
		public SEMOSSVertexColor[] getAllNamedColors(){
			Collection<SEMOSSVertexColor> collection = named_colors.values();
			SEMOSSVertexColor[] colorArray = new SEMOSSVertexColor[collection.size()];
			collection.toArray(colorArray);
			return colorArray;
		}
		
		public String[] getAllColorNames(){
			return allColorNames;
		}
	}
	
	public class SEMOSSVertexColor {
		
		public final String name;
		
		public final Color color;
		
		public SEMOSSVertexColor(String name, Color color){
			this.name = name;
			this.color = color;
		}
	}
}