/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openrdf.model.Statement;

/**
 *
 * @author ryan
 */
public final class ImportData {

	private final List<LoadingSheetData> sheets = new ArrayList<>();
	private ImportMetadata metadata;

	public ImportData() {
		metadata = new ImportMetadata();
	}

	public ImportData( ImportMetadata md ) {
		metadata = md;
	}

	public static ImportData forEngine( IEngine eng ) {
		return new ImportData( ImportMetadata.forEngine( eng ) );
	}

	public void clear() {
		sheets.clear();
		metadata.clear();
	}

	public ImportData add( LoadingSheetData d ) {
		sheets.add( d );
		return this;
	}

	public ImportData add( Collection<LoadingSheetData> newsheets,
			Collection<Statement> stmts ) {

		sheets.addAll( newsheets );
		metadata.addAll( stmts );

		return this;
	}

	public ImportData setMetadata( ImportMetadata im ) {
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

	public Collection<Statement> getStatements() {
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
}
