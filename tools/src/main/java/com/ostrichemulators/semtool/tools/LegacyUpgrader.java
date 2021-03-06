/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.remote.BigdataSailFactory;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.impl.EngineFactory;
import com.ostrichemulators.semtool.rdf.engine.impl.LegacyUpgradingInsightManagerImpl;
import com.ostrichemulators.semtool.rdf.engine.util.EngineCreateBuilder;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException.ErrorCode;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.rdf.engine.util.RepositoryCopier;
import com.ostrichemulators.semtool.util.UriBuilder;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;

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
		log.warn( "this upgrade doesn't work at all yet" );

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

		Properties props = Utility.loadProp( smss );
		URI uris[] = new URI[2]; // 0 -> schema uri, 1 -> data uri
		Map<URI, URI> fromto = figureUris( custommap, uris );
		final URI schema = uris[0];
		final URI data = uris[1];

		EngineCreateBuilder ecb = new EngineCreateBuilder( tofile.getParentFile(),
				FilenameUtils.getBaseName( tofile.getName() ) );
		ecb.setReificationModel( ReificationStyle.SEMTOOL );
		//ecb.setInsightsFile( null, null, null );
		ecb.setDefaultBaseUri( schema, true );
		ecb.setVocabularies( vocabs );
		ecb.setBooleans( true, true, true );
		File dbfile = EngineUtil2.createNew( ecb, null );

		IEngine bde = EngineFactory.getEngine( dbfile );
		LegacyUpgradingInsightManagerImpl im = new LegacyUpgradingInsightManagerImpl();
		im.loadLegacyData( Utility.loadProp( questions ) );
		bde.updateInsights( im );
		bde.closeDB();

		File legacyjnl
				= new File( legacydir, props.getProperty( BigdataSail.Options.FILE ) );

		BigdataSailRepository legacyrepo = BigdataSailRepository.class.cast( BigdataSailFactory.openRepository( legacyjnl.getPath() ) );
		legacyrepo.initialize();
		RepositoryConnection legacyconn = legacyrepo.getReadOnlyConnection();

		BigdataSailRepository newrepo = BigdataSailRepository.class.cast( BigdataSailFactory.openRepository( dbfile.getPath() ) );
		newrepo.initialize();
		RepositoryConnection newconn = newrepo.getConnection();

		try {
			legacyconn.export( getCopier( newconn, fromto, schema, data ) );
		}
		catch ( RDFHandlerException rfe ) {
			throw new EngineManagementException( ErrorCode.UNKNOWN, rfe );
		}
		finally {
			legacyconn.close();
			legacyrepo.shutDown();

			newconn.close();
			newrepo.shutDown();
		}
	}

	private static Map<URI, URI> figureUris( File custommap, URI[] uris )
			throws IOException {
		Properties props = Utility.loadProp( custommap );
		props.remove( "IGNORE_URI" );
		final String patstr = "^(.*)/(?:Relation|Concept).*/(.*)$";

		Map<URI, URI> fromto = new HashMap<>();

		for ( Entry<Object, Object> en : props.entrySet() ) {
			String key = en.getKey().toString();
			String val = en.getValue().toString();
			URI uri = new URIImpl( val.replaceAll( patstr, "$1/$2" ) );

			fromto.put( new URIImpl( val ), uri );

			if ( key.endsWith( "_CLASS" ) ) {
				if ( null == uris[0] ) {
					uris[0] = new URIImpl( uri.getNamespace() );
				}
			}
			else {
				if ( null == uris[1] ) {
					uris[1] = new URIImpl( uri.getNamespace() );
				}
			}
		}

		return fromto;
	}

	private static RepositoryCopier getCopier( RepositoryConnection conn,
			Map<URI, URI> fromto, URI schema, URI data ) {
		final UriBuilder owlb = UriBuilder.getBuilder( schema );
		final UriBuilder datab = UriBuilder.getBuilder( data );

		return new RepositoryCopier( conn ) {

			@Override
			public void handleStatement( Statement stmt ) throws RDFHandlerException {
				URI sub = URI.class.cast( stmt.getSubject() );
				URI pred = stmt.getPredicate();
				Value obj = stmt.getObject();

				sub = upgrade( sub, fromto, owlb, datab );
				pred = upgrade( pred, fromto, owlb, datab );
				if ( obj instanceof URI ) {
					obj = upgrade( URI.class.cast( obj ), fromto, owlb, datab );
				}

				super.handleStatement( new StatementImpl( sub, pred, obj ) );
			}
		};
	}

	private static URI upgrade( URI old, Map<URI, URI> fromto, UriBuilder owlb,
			UriBuilder datab ) {
		URI newy = null;
		if ( fromto.containsKey( old ) ) {
			newy = fromto.get( old );
		}
		else {
			if ( owlb.contains( old ) ) {
				newy = owlb.build( old.getLocalName() );
			}
			else if ( datab.contains( old ) ) {
				newy = datab.build( old.getLocalName() );
			}
			else {
				newy = old;
			}
		}

		return newy;
	}
}
