/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
import java.net.URL;
import java.util.Map;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface GraphColorShapeRepository {

	public static final Color COLORS[] = {
		new Color( 31, 119, 180 ), // blue
		new Color( 44, 160, 44 ), // green
		new Color( 214, 39, 40 ), // red
		new Color( 143, 99, 42 ), // brown
		new Color( 254, 208, 2 ), // yellow
		new Color( 255, 127, 14 ), // orange
		new Color( 148, 103, 189 ), // purple
		new Color( 23, 190, 207 ), // aqua
		new Color( 241, 47, 158 ), // pink
		Color.GRAY
	};

	public void importFrom( GraphColorShapeRepository repo );

	public void set( GraphElement ge, Color color, NamedShape shape );

	public void set( URI ge, Color color, NamedShape shape );

	public void set( GraphElement ge, URL imageloc );

	public void set( URI ge, URL imageloc );

	/**
	 *
	 * A convenience function to {@link #getShape(org.openrdf.model.URI) }
	 *
	 * @param ge
	 * @return
	 */
	public NamedShape getShape( GraphElement ge );

	/**
	 * A convenience function to {@link #getColor(org.openrdf.model.URI) }.
	 *
	 * @param ge
	 * @return
	 */
	public Color getColor( GraphElement ge );

	public NamedShape getShape( URI ge );

	public Color getColor( URI ge );

	public Map<URI, Color> getColors();

	public Map<URI, NamedShape> getShapes();

	public Map<URI, URL> getIcons();

	/**
	 * Do we already have a shape for this URI?
	 *
	 * @param uri
	 * @return
	 */
	public boolean hasShape( URI uri );
}
