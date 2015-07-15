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
package gov.va.semoss.ui.components;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.PropertyEditorPlaySheet;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * This class is used to display information about a node in a popup window.
 */
public class NodePropertiesPopup extends AbstractAction {
	private static final long serialVersionUID = -1859278887122010885L;
	
	private GraphPlaySheet gps;
	private SEMOSSVertex pickedVertex;

	public NodePropertiesPopup( GraphPlaySheet gps, SEMOSSVertex pickedVertex ) {
		super( "Edit Node Properties" );
		this.putValue( Action.SHORT_DESCRIPTION,
				"Edit the properties of this node" );
		this.gps = gps;
		
		this.pickedVertex = pickedVertex;
	}

	public NodePropertiesPopup( GraphPlaySheet gps, Collection<SEMOSSVertex> pickedVertexList ) {
		super( "Edit Node Properties" );
		this.putValue( Action.SHORT_DESCRIPTION,
				"Edit the properties of this node" );
		this.gps = gps;
		
		for (SEMOSSVertex v:pickedVertexList)
			pickedVertex = v;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		showPropertiesView();
	}
	
	public void showPropertiesView() {
		ValueFactory vf = new ValueFactoryImpl();
		List<Value[]> data = new ArrayList<>();
		for ( Map.Entry<URI, Object> entry : pickedVertex.getProperties().entrySet() ) {
			Value[] row = { entry.getKey(), vf.createLiteral( entry.getValue()+"" ), vf.createLiteral( entry.getValue().getClass().getCanonicalName()) };
			data.add( row );
		}

		PropertyEditorPlaySheet grid = new PropertyEditorPlaySheet(pickedVertex);
		grid.setTitle( "Selected Node Properties" );
		grid.create( data, null, gps.getEngine() );
		gps.addSibling( grid );
	}
}
