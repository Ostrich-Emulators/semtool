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

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.util.PropComparator;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

/**
 * Transforms the property edge label on the graph.
 */
public class EdgeLabelTransformer implements Transformer <SEMOSSEdge, String> {
	
	Logger logger = Logger.getLogger(getClass());
	ControlData data = null;
	
	/**
	 * Constructor for EdgeLabelTransformer.
	 * @param data ControlData
	 */
	public EdgeLabelTransformer(ControlData data)
	{
		this.data = data;
	}

	/**
	 * Method transform.  Get the DI Helper to find what is needed to get for vertex
	 * @param edge DBCMEdge - The edge of which this returns the properties.
	
	 * @return String - The name of the property. */
	@Override
	public String transform(SEMOSSEdge edge) {	
		String propName = "";

		ArrayList<String> props = this.data.getSelectedProperties(edge.getEdgeType());
		if(props != null && props.size() > 0)
		{
			propName = "<html>";
			//want to order the props so that it is always in the order name, type, uri, then the other properties
			Collections.sort(props, new PropComparator());
			for(int propIndex=0;propIndex < props.size();propIndex++){
				if(propIndex!=0) propName = propName + "<br>";
				propName = propName + "<!--"+edge.getURI()+"-->";//Need this stupid comment to keep each html comment different. 
				//For some reason the transformer cannot handle text size changes if two labels are the same
				propName = propName + edge.getProperty(props.get(propIndex)+"");
			}
			propName = propName + "</html>";
		}
		//logger.debug("Prop Name " + propName);
		
		return propName;
	}
	
}
