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
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import gov.va.semoss.util.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Controls the export graph to grid feature.
 */
public class GraphPlaySheetExportListener extends AbstractAction {
	private static final long serialVersionUID = -8428913823791195745L;
	private static final Logger logger
			= Logger.getLogger( GraphPlaySheetExportListener.class );
	private final GraphPlaySheet gps;

	public GraphPlaySheetExportListener( GraphPlaySheet ps ) {
		super( "Convert to Table" );
		putValue( Action.SHORT_DESCRIPTION,
				"Convert graph display to a table display" );
		gps = ps;
	}

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param arg0 ActionEvent - The event that triggers the actions in the
	 * method.
	 */
	@Override
	public void actionPerformed( ActionEvent arg0 ) {
		logger.debug( "Export button has been pressed" );

		Set<SEMOSSVertex> verts
				= new HashSet<>( gps.getView().getGraphLayout().getGraph().getVertices() );
		Set<SEMOSSEdge> edges
				= new HashSet<>( gps.getView().getGraphLayout().getGraph().getEdges() );

		ValueFactory vf = new ValueFactoryImpl();
		List<Value[]> vals = new ArrayList<>();
		for ( SEMOSSVertex v : verts ) {
			Value subj = vf.createLiteral( v.getLabel() );
			Object o = v.getProperty( RDF.TYPE );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Vertex Type" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}
			o = v.getURI();
			if ( null != o ) {
				Value pred = vf.createLiteral( "URI" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}
			Value pred = vf.createLiteral( "Type" );
			Value obj = vf.createLiteral( "Vertex" );
			vals.add( new Value[]{ subj, pred, obj } );
		}

		for ( SEMOSSEdge v : edges ) {
			Value subj = vf.createLiteral( v.getName() );
			Object o = v.getProperty( Constants.EDGE_NAME );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Name" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}
			o = v.getURI();
			if ( null != o ) {
				Value pred = vf.createLiteral( "URI" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}

			o = v.getProperty( Constants.EDGE_TYPE );
			if ( null != o ) {
				Value pred = vf.createLiteral( "Edge Type" );
				Value obj = vf.createLiteral( o.toString() );
				vals.add( new Value[]{ subj, pred, obj } );
			}

			Value pred = vf.createLiteral( "Type" );
			Value obj = vf.createLiteral( "Edge" );
			vals.add( new Value[]{ subj, pred, obj } );
		}

		GridRAWPlaySheet.convertUrisToLabels( vals, gps.getEngine() );

		GridRAWPlaySheet newGps = new GridRAWPlaySheet();
		newGps.setTitle( "EXPORT: " + gps.getTitle() );
		newGps.create( vals, Arrays.asList( "Vertex or Edge Label", "Property", "Value" ),
				gps.getEngine() );
		gps.getPlaySheetFrame().addTab( "Graph as Table", newGps );
	}

	/**
	 * Method convertConstructToSelect. Converts the construct query to a select
	 * query.
	 *
	 * @param ConstructQuery String The construct query to be converted.
	 *
	 * @return String the converted select query.
	 */
	public String convertConstructToSelect( String ConstructQuery ) {
		logger.info( "Begining to convert query" );
		String result;
		String newConstructQuery;

		//need to separate {{ so that tokenizer gets every piece of the construct query
		if ( ConstructQuery.contains( "{{" ) ) {
			newConstructQuery = ConstructQuery.replaceAll( Pattern.quote( "{{" ), "{ {" );
		}
		else {
			newConstructQuery = ConstructQuery;
		}
		StringTokenizer QueryBracketTokens = new StringTokenizer( newConstructQuery, "{" );
		//Everything before first { becomes SELECT
		String firstToken = QueryBracketTokens.nextToken();
		String newFirstToken = null;
		if ( firstToken.toUpperCase().contains( "CONSTRUCT" ) ) {
			newFirstToken = firstToken.toUpperCase().replace( "CONSTRUCT", "SELECT DISTINCT " );
		}
		else {
			logger.info( "Converting query that is not CONSTRUCT...." );
		}

		//the second token coming from between the first two brackets must have all period and semicolons removed
		String secondToken = QueryBracketTokens.nextToken();
		String newsecondToken = null;
		if ( secondToken.contains( ";" ) && secondToken.contains( "." ) ) {
			String secondTokensansSemi = secondToken.replaceAll( ";", " " );
			String secondTokensansPer = secondTokensansSemi.replaceAll( "\\.", " " );
			newsecondToken = secondTokensansPer.replace( "}", " " );
		}
		else if ( secondToken.contains( "." ) && !secondToken.contains( ";" ) ) {
			String secondTokensansPer = secondToken.replaceAll( "\\.", " " );
			newsecondToken = secondTokensansPer.replace( "}", " " );
		}
		else if ( secondToken.contains( ";" ) && !secondToken.contains( "." ) ) {
			String secondTokensansSemi = secondToken.replaceAll( ";", " " );
			newsecondToken = secondTokensansSemi.replace( "}", " " );
		}
		else if ( !secondToken.contains( ";" ) && !secondToken.contains( "." ) ) {
			String secondTokensansSemi = secondToken.replaceAll( ";", " " );
			newsecondToken = secondTokensansSemi.replace( "}", " " );
		}

		// Rest of the tokens go unchanged.  Must iterate through and replace {
		String restOfQuery = "";
		while ( QueryBracketTokens.hasMoreTokens() ) {
			String token = QueryBracketTokens.nextToken();
			restOfQuery = restOfQuery + "{" + token;
		}
		result = newFirstToken + newsecondToken + restOfQuery;
		return result;
	}
}