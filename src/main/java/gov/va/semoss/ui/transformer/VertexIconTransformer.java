/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.transformer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;

import gov.va.semoss.om.SEMOSSVertex;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import org.apache.log4j.Logger;
import gov.va.semoss.util.Utility;

/**
 */
public class VertexIconTransformer extends DefaultVertexIconTransformer<SEMOSSVertex>  implements Transformer<SEMOSSVertex, Icon>{
	private static final Logger log = Logger.getLogger( VertexIconTransformer.class );
	private boolean fillImages = true;
	private boolean outlineImages = false;

	/**
	 * Constructor for VertexIconTransformer.
	 */
	public VertexIconTransformer() {}

	/**

	 * @return Returns the fillImages. */
	public boolean isFillImages() {
		return fillImages;
	}
	/**
	 * @param fillImages The fillImages to set.
	 */
	public void setFillImages(boolean _fillImages) {
		fillImages = _fillImages;
	}

	/**
	 * Method isOutlineImages.

	 * @return boolean */
	public boolean isOutlineImages() {
		return outlineImages;
	}
	/**
	 * Method setOutlineImages.
	 * @param outlineImages boolean
	 */
	public void setOutlineImages(boolean _outlineImages) {
		outlineImages = _outlineImages;
	}

	/**
	 * Method transform.
	 * @param vertex DBCMVertex

	 * @return Icon */
	@Override
	public Icon transform(SEMOSSVertex vertex) {
		try {
			BufferedImage img = Utility.loadImage( "globe.jpg" );
			if (img == null)
				return null;
		
			ImageIcon icon = new ImageIcon( img.getScaledInstance(20, 20, 0) );

			int borderWidth = 1;
			int spaceAroundIcon = -2;
			Color borderColor = Color.RED;

			BufferedImage bi = 
					new BufferedImage(icon.getIconWidth() + (2 * borderWidth + 2 * spaceAroundIcon),
							icon.getIconHeight() + (2 * borderWidth + 2 * spaceAroundIcon), 
							BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = bi.createGraphics();
			g.setColor(borderColor);
			g.drawImage(icon.getImage(), borderWidth + spaceAroundIcon,
					borderWidth + spaceAroundIcon, null);

			g.setStroke(new BasicStroke(2));
			g.drawOval(0, 0, bi.getWidth(), bi.getHeight());
			g.dispose();

			return new ImageIcon(bi);
		} catch (Exception e) {
			log.error( e );
		}
		return null;
	}
}
