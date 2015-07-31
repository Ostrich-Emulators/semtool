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
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.ui.components.models.LoadingSheetModel;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;

/**
 */
public class NodeLoadingPlaySheet extends LoadingPlaySheetBase {

	private static final Logger log = Logger.getLogger( NodeLoadingPlaySheet.class );

	public NodeLoadingPlaySheet( List<Value[]> data, List<String> headers ) {
		super( LoadingSheetModel.forNode( data, headers ) );
	}
	
	public NodeLoadingPlaySheet( LoadingSheetData lsd ) {
		this( lsd, false );
	}

	public NodeLoadingPlaySheet( LoadingSheetData lsd, boolean allowInserts ) {
		super( new LoadingSheetModel( lsd ) );

		getModel().setReadOnly( !allowInserts );
		getModel().setAllowInsertsInPlace( allowInserts );
		
		setTitle( lsd.getName() );
		setHeaders( lsd.getHeaders() );
	}

	@Override
	public boolean okToLoad() {
		return true;
	}

	@Override
	public boolean correct() {
		return true;
	}
}
