/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public final class ImportData {
	private static final Logger log = Logger.getLogger( ImportData.class );
	private final List<LoadingSheetData> sheets = new ArrayList<>();
	private ImportMetadata metadata;

	public ImportData() {
		this( new ImportMetadata() );
	}

	public ImportData( ImportMetadata md ) {
		setMetadata( md );
	}

	/**
	 * Releases any resources used by this object
	 */
	public void release() {
		for ( LoadingSheetData lsd : sheets ) {
			lsd.release();
		}

		sheets.clear();
		metadata.clear();
	}

	public ImportData add( LoadingSheetData d ) {
		sheets.add( d );
		return this;
	}

	public ImportData add( Collection<LoadingSheetData> newsheets,
			Collection<String[]> stmts ) {

		sheets.addAll( newsheets );
		metadata.addAll( stmts );

		return this;
	}

	public final ImportData setMetadata( ImportMetadata im ) {
		metadata = im;
		return this;
	}

	public ImportMetadata getMetadata() {
		return metadata;
	}

	public Collection<LoadingSheetData> getRels() {
		List<LoadingSheetData> rels = new ArrayList<>();
		for ( LoadingSheetData d : sheets ) {
			if ( d.isRel() ) {
				rels.add( d );
			}
		}
		return rels;
	}

	public Collection<LoadingSheetData> getNodes() {
		List<LoadingSheetData> nodes = new ArrayList<>();
		for ( LoadingSheetData d : sheets ) {
			if ( !d.isRel() ) {
				nodes.add( d );
			}
		}
		return nodes;
	}

	/**
	 * Gets all node and relationship loading data
	 *
	 * @return
	 */
	public Collection<LoadingSheetData> getSheets() {
		return new ArrayList<>( sheets );
	}

	public Collection<String[]> getStatements() {
		return metadata.getStatements();
	}

	/**
	 * Are there any nodes or relationships in this instance?
	 *
	 * @return true if getSheets returns an empty collection
	 */
	public boolean isEmpty() {
		return sheets.isEmpty();
	}

	public List<String> getSheetNames() {
		List<String> names = new ArrayList<>();
		for ( LoadingSheetData lsd : sheets ) {
			names.add( lsd.getName() );
		}

		return names;
	}

	public LoadingSheetData getSheet( String name ) {
		for ( LoadingSheetData lsd : sheets ) {
			if ( lsd.getName().equals( name ) ) {
				return lsd;
			}
		}

		return null;
	}

	/**
	 * Looks through all properties for each sheet to see if the property is
	 * actually a link to a node in some other sheet
	 *
	 * @see LoadingSheetData#findPropertyLinks(java.util.Collection)
	 */
	public void findPropertyLinks() {
		for ( LoadingSheetData sheet : sheets ) {
			if ( sheet.hasProperties() ) {
				Set<LoadingSheetData> sheetset = new HashSet<>( sheets );
				sheetset.remove( sheet ); // don't look at ourselves
				sheet.findPropertyLinks( sheetset );
			}
		}
		log.debug( "property links resolved" );
	}
}
