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
package gov.va.semoss.ui.components.playsheets.rendererclasses;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.playsheets.BrowserPlaySheet2;
import gov.va.semoss.ui.components.playsheets.rendererclasses.DupeHeatMapFunctions;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;

import com.google.gson.Gson;

public abstract class DupeHeatMapSheet extends BrowserPlaySheet2 {

	private static final long serialVersionUID = -8725998287263132738L;
	private static final Logger logger = Logger.getLogger( DupeHeatMapSheet.class );

	protected Hashtable<String, Hashtable<String, Hashtable<String, Double>>> paramDataHash = new Hashtable<>();
	protected Hashtable<String, String> allHash = new Hashtable<String, String>();

	private Hashtable<String, Hashtable<String, String>> keyHash = new Hashtable<String, Hashtable<String, String>>();
	private ArrayList<String> orderedParams = new ArrayList<>();

	private final static String scoreCst = "Score", keyCst = "key";
	private String xAxisObject = "";
	private String yAxisObject = "";
	private int maxDataSize = 25000;

	public DupeHeatMapSheet() {
		super( "/html/RDFSemossCharts/app/heatmapcustom.html" );
		registerFunctions();
	}

	@Override
	public void create( List<Value[]> data, List<String> heads, IEngine engine ) {
		createData();
		createView();
	}

	@Override
	public void callIt() {
		Gson gson = new Gson();
		ArrayList<String> args = new ArrayList<>();

		ArrayList<Integer> sizes = new ArrayList<>();
		for ( String paramKey : paramDataHash.keySet() ) {
			int index = 0;
			for ( int storedSize : sizes ) {
				if ( paramDataHash.get( paramKey ).size() > storedSize ) {
					index++;
				}
			}
			sizes.add( index, paramDataHash.get( paramKey ).size() );
			orderedParams.add( index, paramKey );
		}

		for ( String key : paramDataHash.keySet() ) {
			args.add( key );
		}

		ArrayList<Hashtable<String, Hashtable<String, Double>>> calculatedArray = calculateJSRetArray( args, new Hashtable<String, Double>() );
		for ( Hashtable<String, Hashtable<String, Double>> hash : calculatedArray ) {
			executeJavaScript( "dataBuilder('" + gson.toJson( hash ) + "');" );
		}

		//send available dimensions to JS.. in case of data overload this will lighten the JSON Load
		executeJavaScript( "dimensionData('" + gson.toJson( args ) + "', 'categories');" );

		for ( String key : allHash.keySet() ) {
			Object value = (Object) allHash.get( key );
			executeJavaScript( "dimensionData('" + gson.toJson( value ) + "', '" + key + "');" );
		}

		executeJavaScript( "start();" );
		progressComplete( "100%...Visualization Complete" );
		logger.debug( "Finished the visualization." );

		//clear all Hash at the end to clear it from SEMOSS memory
		allHash.clear();
	}

	protected void progressComplete( String txt ) {
		updateProgressBar( txt, 100 );
	}

	protected void updateProgressBar( String txt, int val ) {
		getPlaySheetFrame().updateProgress( txt, val );
	}

	//registering the browser functions
	public void registerFunctions() {
		registerFunction( "java", new DupeHeatMapFunctions( this ) );
	}

	//processing duplication hashes...often used by children
	public Hashtable<String, Hashtable<String, Double>> processHashForJS( Hashtable<String, Hashtable<String, Double>> dataHash ) {
		Hashtable<String, Hashtable<String, Double>> dataRetHash = new Hashtable<String, Hashtable<String, Double>>();
		//loop through all the hashes inside the large hash to create the key and the keyHash
		for ( Entry<String, Hashtable<String, Double>> objectKey1 : dataHash.entrySet() ) {
			String object1 = objectKey1.getKey();
			Hashtable<String, Double> objectDataHash = objectKey1.getValue();
			for ( Entry<String, Double> objectKey2 : objectDataHash.entrySet() ) {
				String object2 = objectKey2.getKey();
				double objectCompValue = objectKey2.getValue();
				if ( !object1.equals( object2 ) ) {
					Hashtable<String, Double> elementHash = new Hashtable<String, Double>();
					elementHash.put( "Score", objectCompValue * 100 );
					String key = object1 + "-" + object2;
					dataRetHash.put( key, elementHash );
					if ( !keyHash.containsKey( key ) ) {
						Hashtable<String, String> keyElementHash = new Hashtable<String, String>();
						keyElementHash.put( xAxisObject, object1 );
						keyElementHash.put( yAxisObject, object2 );
						keyHash.put( key, keyElementHash );
					}
				}

			}
		}
		return dataRetHash;
	}

	//called by the barchart listener
	public ArrayList<Hashtable<String, Object>> getCellBarChartData( String cellKey, String[] selectedParams, Hashtable<String, Double> specifiedWeights ) {
		//used to take the selected parameters and threshholds and return the appropriate arraylist of values for that given cell
		ArrayList<Hashtable<String, Object>> retList = new ArrayList<Hashtable<String, Object>>();

		//loop through all the vars and get see if the scores should get reported based off the threshhold
		for ( String varName : selectedParams ) {
			//this hashtable to see if minimum weight exists
			Hashtable<String, Hashtable<String, Double>> varHash = paramDataHash.get( varName );
			Hashtable<String, Double> cellHash = varHash.get( cellKey );
			double value = cellHash.get( scoreCst );

			//min value is not indicated or the new val has to be greater
			if ( specifiedWeights.get( varName ) == null || value >= specifiedWeights.get( varName ) ) {
				Hashtable<String, Object> newHash = new Hashtable<String, Object>();
				newHash.put( keyCst, varName );
				newHash.put( scoreCst, value );
				retList.add( newHash );
			}
		}
		return retList;
	}

	//called by the refreshSimHeatListener, it just returns boolean but the other functions will call JS
	public boolean refreshSimHeat( String[] selectedParams, Hashtable<String, Double> specifiedWeights ) {
		ArrayList<Hashtable<String, Hashtable<String, Double>>> calculatedArray = calculateJSRetArray( Arrays.asList( selectedParams ), specifiedWeights );
		for ( Hashtable<String, Hashtable<String, Double>> hash : calculatedArray ) {
			executeJavaScript( "dataBuilder('" + new Gson().toJson( hash ) + "');" );
		}

		executeJavaScript( "refreshDataFunction();" );

		return true;
	}

	//calculate the individual elements that needs to be passed to js
	public ArrayList<Hashtable<String, Hashtable<String, Double>>> calculateJSRetArray( List<String> selectedParams, Hashtable<String, Double> minimumWeights ) {
		int minVarLoc = getMinVarLoc( selectedParams );
		if ( minVarLoc == -1 ) {
			return new ArrayList<Hashtable<String, Hashtable<String, Double>>>();
		}

		String minVar = orderedParams.get( minVarLoc );

		//when summing the scores, the assumption is that all the scores need to exist to get a final score
		//so if a cell doesn't have a score for even one of the selected params, it will break out of the loop
		ArrayList<Hashtable<String, Hashtable<String, Double>>> retList = new ArrayList<Hashtable<String, Hashtable<String, Double>>>();
		for ( String key : paramDataHash.get( minVar ).keySet() ) {
			Hashtable<String, Object> finalCellHash = new Hashtable<String, Object>();
			Double score = 0.0;
			Boolean storeBoolean = true;
			//start at the one with minimum vars using minVarLoc idx
			for ( int orderedVarIdx = minVarLoc; orderedVarIdx < orderedParams.size(); orderedVarIdx++ ) {
				String var = orderedParams.get( orderedVarIdx );
				if ( selectedParams.contains( var ) ) {//this means it is a valid var (aka checked)
					Hashtable<String, Hashtable<String, Double>> varHash = paramDataHash.get( var );
					Hashtable<String, Double> elementHash = varHash.get( key );
					if ( elementHash != null ) {
						Double newVal = (Double) elementHash.get( scoreCst );
						//if cell ever doesn't exist for a variable, the cell does not get stored in retHash
						//if it does the value will get averaged
						if ( minimumWeights.get( var ) == null || newVal >= minimumWeights.get( var ) ) { // then it is valid
							score = score + ( newVal / selectedParams.size() );
						}
						else {
							storeBoolean = false;
							break;
						}
					}
					else {
						storeBoolean = false;
						break;
					}
					//store all information if first time looking at the cell
					if ( orderedVarIdx == minVarLoc ) {
						finalCellHash.putAll( keyHash.get( key ) );
					}
				}
			}

			//if all the values check out then put it in the retList
			if ( storeBoolean ) {
				finalCellHash.put( scoreCst, score );
				retList = storeCellInArray( key, finalCellHash, retList );
			}
		}
		return retList;
	}

	private int getMinVarLoc( List<String> selectedParams ) {
		//get the smallest variable to start with, this is used to optimize the processing
		for ( int varLoc = 0; varLoc < orderedParams.size(); varLoc++ ) {
			if ( selectedParams.contains( orderedParams.get( varLoc ) ) )//this means it is a valid var (aka checked)
			{
				return varLoc;
			}
		}

		return -1;
	}

	public ArrayList<Hashtable<String, Hashtable<String, Double>>> storeCellInArray( String key, Hashtable<String, Object> cellHash, ArrayList<Hashtable<String, Hashtable<String, Double>>> arrayStore ) {
		if ( arrayStore.size() == 0 ) {
			Hashtable hash = new Hashtable();
			hash.put( key, cellHash );

			arrayStore.add( hash );
			return arrayStore;
		}

		Hashtable hash = arrayStore.get( 0 );
		if ( hash.size() > this.maxDataSize ) {
			hash = new Hashtable();
			arrayStore.add( 0, hash );
		}

		hash.put( key, cellHash );
		return arrayStore;
	}

	//sets comparison types usually called by child
	public void setObjectTypes( String xAxisObject, String yAxisObject ) {
		this.xAxisObject = xAxisObject;
		this.yAxisObject = yAxisObject;
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}
}