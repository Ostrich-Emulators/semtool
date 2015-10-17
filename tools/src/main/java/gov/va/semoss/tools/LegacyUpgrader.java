/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.remote.BigdataSailFactory;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineManagementException.ErrorCode;
import gov.va.semoss.rdf.engine.util.EngineUtil2;
import gov.va.semoss.rdf.engine.util.RepositoryCopier;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

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
		ecb.setReificationModel( ReificationStyle.SEMOSS );
		ecb.setDefaultsFiles( null, null, questions );
		ecb.setDefaultBaseUri( schema, true );
		ecb.setVocabularies( vocabs );
		ecb.setBooleans( true, true, true );
		File dbfile = EngineUtil2.createNew( ecb, null );

		File legacyjnl
				= new File( legacydir, props.getProperty( BigdataSail.Options.FILE ) );

		upgradeInsights( dbfile, UriBuilder.getBuilder( schema ),
				UriBuilder.getBuilder( data ) );

		BigdataSailRepository legacyrepo = BigdataSailFactory.openRepository( legacyjnl.getPath() );
		legacyrepo.initialize();
		RepositoryConnection legacyconn = legacyrepo.getReadOnlyConnection();

		BigdataSailRepository newrepo = BigdataSailFactory.openRepository( dbfile.getPath() );
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

	private static void upgradeInsights( File jnl, UriBuilder owlb, UriBuilder datab )
			throws EngineManagementException {
		BigDataEngine eng = new BigDataEngine( jnl );
		InsightManager im = eng.getInsightManager();

		final Pattern PAT = Pattern.compile( "<([^>]+)>" );

		Perspective genericP = null;
		for ( Perspective p : im.getPerspectives() ) {
			if ( "Generic-Perspective".equals( p.getLabel() ) ) {
				// don't upgrade the generic perspective...get rid of it
				genericP = p;
			}
			else {
				for ( Insight i : p.getInsights() ) {
					String query = i.getSparql();
					StringBuffer sb = new StringBuffer();
					Matcher m = PAT.matcher( query );
					while ( m.find() ) {
						String newstr = m.group( 1 );
						URI repl = new URIImpl( newstr );
						if ( owlb.contains( repl ) ) {
							m.appendReplacement( sb,
									"<" + owlb.build( repl.getLocalName() ).stringValue() + ">" );
						}
						else if ( datab.contains( repl ) ) {
							m.appendReplacement( sb,
									"<" + datab.build( repl.getLocalName() ).stringValue() + ">" );
						}
					}
					m.appendTail( sb );
					i.setSparql( sb.toString() );

					for ( Parameter a : i.getInsightParameters() ) {
						StringBuffer aqb = new StringBuffer();
						String aq = a.getDefaultQuery();
						m.reset( aq );
						while ( m.find() ) {
							String newstr = m.group( 1 );
							URI repl = new URIImpl( newstr );
							if ( owlb.contains( repl ) ) {
								m.appendReplacement( aqb,
										"<" + owlb.build( repl.getLocalName() ).stringValue() + ">" );
							}
							else if ( datab.contains( repl ) ) {
								m.appendReplacement( aqb,
										"<" + datab.build( repl.getLocalName() ).stringValue() + ">" );
							}
						}
						m.appendTail( aqb );
						a.setDefaultQuery( aqb.toString() );
					}
				}
			}
		}

		if ( null != genericP ) {
			im.remove( genericP );
		}

		eng.updateInsights( im );
		eng.closeDB();
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
