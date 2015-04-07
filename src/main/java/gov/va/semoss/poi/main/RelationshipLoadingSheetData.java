/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.Collection;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A class to encapsulate relationship loading sheet information.
 *
 * @author ryan
 */
public class RelationshipLoadingSheetData extends LoadingSheetData {

	public RelationshipLoadingSheetData( String name, String sType, String oType,
			String relname ) {
		super( name, sType, oType, relname );
	}

	public RelationshipLoadingSheetData( String name, String sType, String oType,
			String relname, Map<String, URI> props ) {
		super( name, sType, oType, relname, props );
	}

	public RelationshipLoadingSheetData( String name, String sType, String oType,
			String relname, Collection<String> props ) {
		super( name, sType, oType, relname, props );
	}

	public static RelationshipLoadingSheetData copyHeadersOf( RelationshipLoadingSheetData model ) {
		return new RelationshipLoadingSheetData( model.getName(), model.getSubjectType(),
				model.getObjectType(), model.getRelname(), model.getPropertiesAndDataTypes() );
	}

	public LoadingNodeAndPropertyValues add( String slabel, String olabel ) {
		cacheNapLabel( slabel );
		cacheNapLabel( olabel );

		LoadingNodeAndPropertyValues nap
				= new LoadingNodeAndPropertyValues( slabel, olabel );
		add( nap );
		return nap;
	}

	public LoadingNodeAndPropertyValues add( String slabel, String olabel,
			Map<String, Value> props ) {
		LoadingNodeAndPropertyValues nap = add( slabel, olabel );
		nap.putAll( props );
		return nap;
	}
}
