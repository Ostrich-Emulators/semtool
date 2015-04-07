/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;

/**
 * A class to encapsulate node loading sheet information.
 *
 * @author ryan
 */
public class NodeLoadingSheetData extends LoadingSheetData {

	public NodeLoadingSheetData( String name, String type ) {
		this( name, type, new HashMap<>() );
	}

	public NodeLoadingSheetData( String name, String type, Collection<String> props ) {
		super( name, type, props );
	}

	public NodeLoadingSheetData( String name, String type, Map<String, URI> props ) {
		super( name, type, props );
	}

	public static NodeLoadingSheetData copyHeadersOf( NodeLoadingSheetData model ) {
		return new NodeLoadingSheetData( model.getName(), model.getSubjectType(),
				model.getPropertiesAndDataTypes() );
	}
}
