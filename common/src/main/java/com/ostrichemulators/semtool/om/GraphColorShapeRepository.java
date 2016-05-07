/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
import java.net.URL;
import java.util.Map;
import javax.swing.ImageIcon;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface GraphColorShapeRepository {

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

	public ImageIcon getIcon( GraphElement ge );

	public ImageIcon getIcon( URI uri );

	public ImageIcon getIcon( URI uri, double size );

	public void setIconPadding( double sz );

	public void setIconSize( double sz );

	public double getIconSize();

	public double getIconPadding();
}
