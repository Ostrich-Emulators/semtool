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

	private final List<RelationshipLoadingSheetData> rels = new ArrayList<>();
	private final List<NodeLoadingSheetData> nodes = new ArrayList<>();
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
		rels.clear();
		nodes.clear();
		metadata.clear();
	}

	/**
	 * Gets the loading sheet data with the given name, or null. If multiple
	 * sheets have the same name, the first one is returned
	 *
	 * @param name
	 * @return
	 */
	public NodeLoadingSheetData getNodeSheet( String name ) {
		for ( NodeLoadingSheetData d : getNodes() ) {
			if ( d.getName().equals( name ) ) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Gets the loading sheet data with the given name, or null. If multiple
	 * sheets have the same name, the first one is returned
	 *
	 * @param name
	 * @return
	 */
	public RelationshipLoadingSheetData getRelationsSheet( String name ) {
		for ( RelationshipLoadingSheetData d : getRels() ) {
			if ( d.getName().equals( name ) ) {
				return d;
			}
		}
		return null;
	}

	public ImportData add( RelationshipLoadingSheetData d ) {
		rels.add( d );
		return this;
	}

	public ImportData add( NodeLoadingSheetData d ) {
		nodes.add( d );
		return this;
	}

	public ImportData add( Collection<NodeLoadingSheetData> newnodes,
			Collection<RelationshipLoadingSheetData> newrels, Collection<Statement> stmts ) {

		nodes.addAll( newnodes );
		rels.addAll( newrels );
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

	public Collection<RelationshipLoadingSheetData> getRels() {
		return rels;
	}

	public Collection<NodeLoadingSheetData> getNodes() {
		return nodes;
	}

	/**
	 * Gets all node and relationship loading data
	 *
	 * @return
	 */
	public Collection<LoadingSheetData> getAllData() {
		List<LoadingSheetData> data = new ArrayList<>( nodes );
		data.addAll( rels );
		return data;
	}

	public Collection<Statement> getStatements() {
		return metadata.getStatements();
	}

	/**
	 * Are there any nodes or relationships in this instance?
	 *
	 * @return true if getAllData returns an empty collection
	 */
	public boolean isEmpty() {
		return ( nodes.isEmpty() && rels.isEmpty() );
	}

	public List<String> getSheetNames() {
		List<String> sheets = new ArrayList<>();
		for ( LoadingSheetData lsd : getNodes() ) {
			sheets.add( lsd.getName() );
		}

		for ( LoadingSheetData lsd : getRels() ) {
			sheets.add( lsd.getName() );
		}

		return sheets;
	}
}
