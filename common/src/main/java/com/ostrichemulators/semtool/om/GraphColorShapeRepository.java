/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;

/**
 * A repository to hold colors and shapes (and icons, too!) for graph elements.
 * In all cases, the getXX functions should return a valid value, even if a
 * random value must be generated. In fact, for consistency, random values
 * <em>should</em> be generated and saved for future calls (this rule doesn't
 * apply to icons)
 *
 * @author ryan
 */
public interface GraphColorShapeRepository {

	public static final Color TRANSPARENT = new Color( 255, 255, 255, 0 );
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

	public void set( Collection<GraphElement> ge, Color color, NamedShape shape );

	public void set( IRI ge, Color color, NamedShape shape );

	public void set( GraphElement ge, URL imageloc );

	public void set( IRI ge, URL imageloc );

	public void set( IRI ge, NamedShape s );

	public void set( IRI ge, Color c );

	/**
	 *
	 * A convenience function to {@link #getShape(org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.model.URI)
	 * }
	 *
	 * @param ge
	 * @return
	 */
	public NamedShape getShape( GraphElement ge );

	/**
	 * A convenience function to {@link #getColor(org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.model.URI)
	 * }
	 *
	 * @param ge
	 * @return
	 */
	public Color getColor( GraphElement ge );

	/**
	 * Gets the previously-saved or newly-generated shape for this URI
	 *
	 * @param ge
	 * @return
	 */
	public NamedShape getShape( IRI ge );

	/**
	 * Gets the shape for this instance, or if none is set, the shape for this
	 * type
	 *
	 * @param type
	 * @param instance
	 * @return
	 */
	public NamedShape getShape( IRI type, IRI instance );

	/**
	 * Gets the previously-saved or newly-generated color for this URI
	 *
	 * @param ge
	 * @return
	 */
	public Color getColor( IRI ge );

	public Color getColor( IRI type, IRI instance );

	public URL getUrl( IRI ge );

	public URL getUrl( IRI ge, IRI instance );

	public Map<IRI, Color> getColors();

	public Map<IRI, NamedShape> getShapes();

	public Map<IRI, URL> getIcons();

	public double getIconSize();

	public void setIconSize( double d );

	public void addListener( GraphColorShapeRepositoryListener l );

	public void removeListener( GraphColorShapeRepositoryListener l );
}
