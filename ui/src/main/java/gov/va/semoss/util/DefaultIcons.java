package gov.va.semoss.util;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Provides a HashMap of various icons available.
 *
 * @author Thomas
 *
 */
public class DefaultIcons {

	public static final String SAVE = "Save Grid";
	
	public static final Map<String, ImageIcon> defaultIcons = new HashMap<>();

	static {
		defaultIcons.put( SAVE,
				new ImageIcon( GuiUtility.loadImage( "icons16/save_diskette1_16.png" ) ) );
	}

}
