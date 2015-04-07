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
 *****************************************************************************
 */
package gov.va.semoss.ui.main.listener.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.BrowserPlaySheet2;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.QuestionPlaySheetStore;

/**
 * Controls the running of node editor.
 */
public class NodeEditorListener implements ActionListener {

	private static final Logger log = Logger.getLogger( NodeEditorListener.class );
	private IEngine engine;
	private GraphPlaySheet gps;
	private SEMOSSVertex node;

	private String htmlFileName= "/html/RDFNodeEditor/app/index.html#/rdfnode/";
	
	public NodeEditorListener(GraphPlaySheet gps, SEMOSSVertex node, IEngine engine) {
		this.gps = gps;
		this.node = node;
		this.engine = engine;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		String uri = node.getProperty( Constants.URI_KEY ) + "";

		String replacedURI = "<" + uri.replaceAll( "/", "^" ) + ">";
		String fullAddress = htmlFileName + replacedURI;

		BrowserPlaySheet2 tabS = new BrowserPlaySheet2( fullAddress );
		NodeEditorNavigationListener navListener = new NodeEditorNavigationListener();
		navListener.setNode( node );
		navListener.setFilterHash( gps.getGraphData().getBaseFilterHash() );
		navListener.setBrowser( tabS.getBrowser() );
		navListener.setEngine( engine );
		tabS.getBrowser().addLoadListener( navListener );
		gps.getPlaySheetFrame().addTab( "Node Editor", tabS );

		SPARQLExecuteFunction sparqlFunction = new SPARQLExecuteFunction();
		sparqlFunction.setEngine( engine );
		sparqlFunction.setGps( gps );
		tabS.getBrowser().registerFunction( "SPARQLExecute", sparqlFunction );
		SPARQLExecuteFilterNoBaseFunction filterFunction = new SPARQLExecuteFilterNoBaseFunction();
		filterFunction.setFilterHash( new Hashtable<>( gps.getGraphData().getBaseFilterHash() ) );
		filterFunction.setEngine( engine );
		tabS.getBrowser().registerFunction( "SPARQLExecuteFilterNoBase", filterFunction );
		SPARQLExecuteFilterBaseFunction filterBaseFunction = new SPARQLExecuteFilterBaseFunction();
		filterBaseFunction.setFilterHash( new Hashtable<>( gps.getGraphData().getBaseFilterHash() ) );
		filterBaseFunction.setEngine( engine );
		tabS.getBrowser().registerFunction( "SPARQLExecuteFilterBase", filterBaseFunction );
		InferEngineFunction inferFunction = new InferEngineFunction();
		inferFunction.setEngine( engine );
		tabS.getBrowser().registerFunction( "InferFunction", inferFunction );
		RefreshPlaysheetFunction refreshFunction = new RefreshPlaysheetFunction();
		refreshFunction.setGps( gps );
		tabS.getBrowser().registerFunction( "RefreshFunction", refreshFunction );
		tabS.createView();
	}
}
