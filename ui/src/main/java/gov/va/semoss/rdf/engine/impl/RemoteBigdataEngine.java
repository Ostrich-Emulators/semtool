/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 */
package gov.va.semoss.rdf.engine.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;
import gov.va.semoss.util.RDFDatatypeTools;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

/**
 * Connects the database to the java engine, except database (.jnl file) is
 * sitting on a server (or the web).
 */
public class RemoteBigdataEngine extends BigDataEngine {

	private static final Logger log = Logger.getLogger( RemoteBigdataEngine.class );
	BigdataSail bdSail = null;
	Properties rdfMap = null;
	RepositoryConnection rc = null;
	ValueFactory vf = null;

	@Override
	public void finishLoading( Properties prop ) throws RepositoryException {
		try {

			String sparqlUEndpoint = prop.getProperty( Constants.SPARQL_UPDATE_ENDPOINT );

			BigdataSailRemoteRepository repo = null;//new BigdataSailRemoteRepository( sparqlUEndpoint );
			repo.initialize();

			//SPARQLRepository repo = new SPARQLRepository(sparqlQEndpoint);
			Map<String, String> myMap = new HashMap<>();
			myMap.put( "apikey", "d0184dd3-fb6b-4228-9302-1c6e62b01465" );

			//HTTPRepository hRepo = new HTTPRepository(sparqlQEndpoint);
			//hRepo.setPreferredRDFFormat(RDFFormat.forMIMEType("application/x-www-form-urlencoded"));
//			hRepo.
			//hRepo.initialize();
			rc = repo.getConnection();
			vf = new ValueFactoryImpl();

			rdfMap = DIHelper.getInstance().getCoreProp();

			//this.connected = true;
			// return g;
		}
		catch ( Exception ignored ) {
			log.error( ignored );
		}
	}

	/**
	 * Method addStatement. Processes a given subject, predicate, object triple
	 * and adds the statement to the given graph.
	 *
	 * @param subject String - RDF Subject for the triple
	 * @param predicate String - RDF Predicate for the triple
	 * @param object Object - RDF Object for the triple
	 * @param concept boolean - True if the statement is a concept
	 * @param graph Graph - The graph where the triple will be added.
	 */
	public void addStatement( String subject, String predicate, Object object, 
			boolean concept, Graph graph ) {
		String subString;
		String predString;
		String sub = subject.trim();
		String pred = predicate.trim();
		
		subString = getUriCompatibleString( sub, false );
		URI newSub = vf.createURI( subString );

		predString = getUriCompatibleString( pred, false );
		URI newPred = vf.createURI( predString );

		if ( !concept ) {
			if ( object instanceof Double ) {
				log.debug( "Found Double " + object );
				graph.add( newSub, newPred, vf.createLiteral( ( (Double) object ) ) );
			}
			else if ( object instanceof Date ) {
				log.debug( "Found Date " + object );
				DateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
				String date = df.format( object );
				URI datatype = vf.createURI( "http://www.w3.org/2001/XMLSchema#dateTime" );
				graph.add( newSub, newPred, vf.createLiteral( date, datatype ) );
			}
			else {
				log.debug( "Found String " + object );
				String value = object.toString();
				// try to see if it already has properties then add to it
				String cleanValue = value.replaceAll( "/", "-" ).replaceAll( "\"", "'" );
				graph.add( newSub, newPred, vf.createLiteral( cleanValue ) );
			}
		}
		else {
			graph.add( newSub, newPred, vf.createURI( object + "" ) );
		}
	}

	/**
	 * Method addGraphToRepository. Adds the specified graph to the current
	 * repository.
	 *
	 * @param graph Graph - The graph to be added to the repository.
	 */
	public void addGraphToRepository( Graph graph ) {
		try {
			rc.add( graph );
		}
		catch ( RepositoryException e ) {
			log.error( e );
		}
	}
	
		/**
	 * Generates a URI-compatible string
	 *
	 * @param original string
	 * @param replaceForwardSlash if true, makes the whole string URI compatible.
	 * If false, splits the string on /, and URI-encodes the intervening
	 * characters
	 *
	 * @return Cleaned string
	 */
	public static String getUriCompatibleString( String original,
			boolean replaceForwardSlash ) {
		String trimmed = original.trim();
		if ( trimmed.isEmpty() ) {
			return trimmed;
		}
		StringBuilder sb = new StringBuilder();
		try {
			if ( replaceForwardSlash || !trimmed.contains( "/" ) ) {
				if ( RDFDatatypeTools.isValidUriChars( trimmed ) ) {
					sb.append( trimmed );
				}
				else {
					sb.append( RandomStringUtils.randomAlphabetic( 1 ) )
							.append( UUID.randomUUID().toString() );
				}
			}
			else {
				Pattern pat = Pattern.compile( "([A-Za-z0-9-_]+://)(.*)" );
				Matcher m = pat.matcher( trimmed );
				String extras;
				if ( m.matches() ) {
					sb.append( m.group( 1 ) );
					extras = m.group( 2 );
				}
				else {
					extras = trimmed;
				}
				boolean first = true;
				for ( String part : extras.split( "/" ) ) {
					String add = ( RDFDatatypeTools.isValidUriChars( part ) ? part
							: RandomStringUtils.randomAlphabetic( 1 )
							+ UUID.randomUUID().toString() );

					if ( first ) {
						first = false;
					}
					else {
						sb.append( "/" );
					}
					sb.append( add );
				}
			}
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}

		return sb.toString();
	}
}
