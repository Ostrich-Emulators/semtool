/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import gov.va.semoss.rdf.engine.util.EngineManagementException;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
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

	public void upgradeTo( File tofile )
			throws RepositoryException, IOException, EngineManagementException {
		log.info( "upgrading database in " + legacydir + " to " + tofile );
		log.warn( "upgrade not yet implemented" );

//		EngineCreateBuilder ecb
//				= new EngineCreateBuilder( tofile.getParentFile(), legacydir.getName() );
//		ecb.setReificationModel( ReificationStyle.SEMOSS );
//
//		EngineUtil2.createNew( ecb, null );


		//BigDataEngine engine = new BigDataEngine();
	}
}
