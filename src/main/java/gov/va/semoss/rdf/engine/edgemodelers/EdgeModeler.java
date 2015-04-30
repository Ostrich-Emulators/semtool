/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public interface EdgeModeler {

	public URI addRel( LoadingNodeAndPropertyValues nap,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException;
	
	public URI addNode( LoadingNodeAndPropertyValues nap, 
			Map<String, String> namespaces,	LoadingSheetData sheet, 
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException ;

		/**
	 * Create statements for all of the properties of the instanceURI
	 *
	 * @param subject URI containing the subject instance URI
	 * @param properties Map<String, Object> that contains all properties
	 * @param namespaces
	 * @param sheet
	 * @param metas
	 * @param rc
	 *
	 * @throws RepositoryException
	 */
	public void addProperties( URI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException;

	
	public void setCaches( Map<String, URI> schemaNodes,
			Map<EngineLoader.ConceptInstanceCacheKey, URI> dataNodes,
			Map<String, URI> relationClassCache,
			Map<String, URI> relationCache );
}
