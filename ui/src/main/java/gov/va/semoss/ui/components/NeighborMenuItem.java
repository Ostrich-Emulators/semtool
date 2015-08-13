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

import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.GuiUtility;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openrdf.model.Model;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * This class is used to create a menu item for the neighborhood.
 */
public class NeighborMenuItem extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger( NeighborMenuItem.class );
	
	private final GraphPlaySheet gps;
	private final ModelQueryAdapter mqa;
	
	public NeighborMenuItem( String tName, GraphPlaySheet ps, ModelQueryAdapter mqa ) {
		super( tName );
		this.gps = ps;
		this.mqa = mqa;
	}
	
	@Override
	public void actionPerformed( ActionEvent ae ) {
		ProgressTask pt = new ProgressTask( "Expanding Graph", new Runnable() {
			
			@Override
			public void run() {
				try {
					Model model = gps.getEngine().construct( mqa );
					gps.overlay( model, gps.getEngine() );
				}
				catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
					logger.error( e, e );
					GuiUtility.showError( e.getLocalizedMessage() );
				}
			}
		} );
		
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}
}
