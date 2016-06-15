/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import java.util.Map;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public interface EdgeModeler {

	/**
	 * Adds a new relationship node to the repository
	 *
	 * @param nap
	 * @param namespaces
	 * @param sheet
	 * @param metas Metadata to use when loading. This argument MUST pass the
	 * {@link AbstractEdgeModeler#isValidMetadata(com.ostrichemulators.semtool.poi.main.ImportMetadata) }
	 * check
	 * @param rc
	 * @return the newly-created relationship node
	 * @throws RepositoryException
	 */
	public URI addRel( LoadingNodeAndPropertyValues nap,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException;

	/**
	 * Adds a new node to the repository
	 *
	 * @param nap
	 * @param namespaces
	 * @param sheet
	 * @param metas Metadata to use when loading. This argument MUST pass the
	 * {@link AbstractEdgeModeler#isValidMetadata(com.ostrichemulators.semtool.poi.main.ImportMetadata) }
	 * check
	 * @param rc
	 * @return the newly-create node
	 * @throws RepositoryException
	 */
	public URI addNode( LoadingNodeAndPropertyValues nap,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException;

	/**
	 * Create statements for all of the properties of the instanceURI
	 *
	 * @param subject URI containing the subject instance URI
	 * @param properties Map of all properties
	 * @param namespaces
	 * @param sheet
	 * @param metas Metadata to use when loading. This argument MUST pass the
	 * {@link AbstractEdgeModeler#isValidMetadata(com.ostrichemulators.semtool.poi.main.ImportMetadata) }
	 * check
	 * @param rc
	 *
	 * @throws RepositoryException
	 */
	public void addProperties( URI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException;

	public void setQaChecker( QaChecker qaer );

	/**
	 * Creates the metamodel for the given data using the given namespaces
	 *
	 * @param alldata the data that contains the model nodes. This
	 * {@link ImportData#getMetadata()} must pass the 
	 * {@link AbstractEdgeModeler#isValidMetadata(com.ostrichemulators.semtool.poi.main.ImportMetadata) }
	 * check
	 * @param namespaces the namespaces to use for resolving metamodel elements
	 * @param vf the value factory to use, or null
	 * @return a model containing all the mm statements
	 * @throws RepositoryException
	 */
	public Model createMetamodel( ImportData alldata, Map<String, String> namespaces,
			ValueFactory vf ) throws RepositoryException;
}
