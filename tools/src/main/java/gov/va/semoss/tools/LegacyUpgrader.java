/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil2;
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class LegacyUpgrader {

	private static final Logger log = Logger.getLogger( LegacyUpgrader.class );
	private final File legacydir;

	public LegacyUpgrader( File legacy ) {
		legacydir = legacy;
	}

	public void upgradeTo( File tofile, Collection<URL> vocabs )
			throws RepositoryException, IOException, EngineManagementException {
		log.info( "upgrading database in " + legacydir + " to " + tofile );
		log.warn( "upgrade not yet implemented" );
		if ( true ) {
			return;
		}

		File smss = legacydir.listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name ) {
				return FilenameUtils.isExtension( name, "smss" );
			}
		} )[0];
		File custommap = legacydir.listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name ) {
				return name.contains( "_Custom_Map" );
			}
		} )[0];
		File questions = legacydir.listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name ) {
				return name.contains( "_Question" );
			}
		} )[0];

		String enginename = FilenameUtils.getBaseName( smss.getName() );

		URI uris[] = new URI[2]; // 0 -> schema uri, 1 -> data uri
		figureUris( custommap, uris );
		URI schema = uris[0];
		URI data = uris[1];

		EngineCreateBuilder ecb
				= new EngineCreateBuilder( tofile.getParentFile(), enginename );
		ecb.setReificationModel( ReificationStyle.SEMOSS );
		ecb.setDefaultsFiles( null, null, questions );
		ecb.setDefaultBaseUri( schema, true );
		ecb.setVocabularies( vocabs );
		ecb.setBooleans( true, true, true );
		File dbfile = EngineUtil2.createNew( ecb, null );

		// BigDataEngine engine = new BigDataEngine( dbfile );
	}

	private static void figureUris( File custommap, URI[] uris ) throws IOException {
		Properties props = Utility.loadProp( custommap );
		final String PAT = "^(.*)/(Relation|Concept)/.*";

		for ( Entry<Object, Object> en : props.entrySet() ) {
			String key = en.getKey().toString();
			String val = en.getValue().toString();
			if ( key.endsWith( "_CLASS" ) ) {
				uris[0] = new URIImpl( val.replaceAll( PAT, "$1" ) );
				if ( null != uris[1] ) {
					break;
				}
			}
			else {
				uris[1] = new URIImpl( val.replaceAll( PAT, "$1" ) );
				if ( null != uris[0] ) {
					break;
				}
			}
		}
	}
}
