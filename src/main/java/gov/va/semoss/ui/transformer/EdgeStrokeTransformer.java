/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.transformer;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import gov.va.semoss.om.SEMOSSEdge;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class EdgeStrokeTransformer implements Transformer<SEMOSSEdge, Stroke> {

	Map<SEMOSSEdge, Double> edges = new HashMap<>();

	/**
	 * Constructor for EdgeStrokeTransformer.
	 */
	public EdgeStrokeTransformer() {

	}

	/**
	 * Method setEdges.
	 *
	 * @param edges Hashtable
	 */
	public void setEdges( Map<SEMOSSEdge, Double> edges ) {
		this.edges = edges;
	}

	/**
	 * Method getEdges.
	 *
	 * @return Hashtable
	 */
	public Map<SEMOSSEdge, Double> getEdges() {
		return edges;
	}

	/**
	 * Method transform.
	 *
	 * @param edge DBCMEdge
	 *
	 * @return Stroke
	 */
	@Override
	public Stroke transform( SEMOSSEdge edge ) {

		float selectedFontFloat = 3.0f;
		float unselectedFontFloat = 0.1f;

		float standardFontFloat = 0.3f;

		Stroke retStroke = new BasicStroke( 1.0f );
		try {
			if ( edges.isEmpty() ) {
				retStroke = new BasicStroke( standardFontFloat, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_ROUND );
			}
			else {
				if ( edges.containsKey( edge ) ) {
					double valDouble = edges.get( edge );
					float valFloat = (float) valDouble;
					float newFontFloat = selectedFontFloat * valFloat;
					retStroke = new BasicStroke( newFontFloat, BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_MITER, 10.0f );

				}
				else {
					retStroke = new BasicStroke( unselectedFontFloat );
				}
			}
		}
		catch ( Exception ex ) {
			//TODO: Specify exception(s) and behavior
			//ignore
		}
		return retStroke;
	}
}
