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
 * ****************************************************************************
 */
package gov.va.semoss.poi.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileNotFoundException;
import java.util.Properties;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Loading data into SEMOSS using comma separated value (CSV) files
 */
public class CSVReader implements ImportFileReader {

	private static final Logger logger = Logger.getLogger( CSVReader.class );
	private final static String START_ROW = "START_ROW";
	private final static String END_ROW = "END_ROW";
	private final static String RELATION = "RELATION";
	private final static String NODE_PROP = "NODE_PROP";
	private final static String RELATION_PROP = "RELATION_PROP";
	private static final Pattern RELATION_PAT = Pattern.compile( "^(.*)[@](.*)[@](.*)$" );
	private static final Pattern NODEPROP_PAT = Pattern.compile( "^([^%]+)[%](.*)$" );
	private static final Map<String, URI> datatypes = new HashMap<>();

	private CsvMapReader mapReader;
	private String[] header;
	private final List<String> relationArrayList = new ArrayList<>();
	private final Map<String, List<String>> nodePropArrayList = new HashMap<>();
	private final List<String> relPropArrayList = new ArrayList<>();
	private File propFile;
	private final Properties rdfMap = new Properties();

	public CSVReader() {
	}

	public CSVReader( File control ) {
		propFile = control;
	}

	/**
	 * Loads the prop file for the CSV file
	 *
	 * @param fileName	Absolute path to the prop file specified in the last column
	 * of the CSV file
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void setRdfMapFromFile( File fileName ) throws FileNotFoundException, IOException {
		Properties rdfPropMap = new Properties();
		rdfPropMap.load( new FileReader( fileName ) );

		for ( String name : rdfPropMap.stringPropertyNames() ) {
			rdfMap.put( name, rdfPropMap.getProperty( name ) );
		}
	}

	@Override
	public ImportMetadata getMetadata( File file ) throws IOException {
		return new ImportMetadata(); // no metadata for CSVs
	}

	@Override
	public ImportData readOneFile( File file ) throws IOException {
		ImportData data = new ImportData();
		ImportMetadata im = data.getMetadata();
		im.setLegacyMode( true );

		try ( Reader rdr = new BufferedReader( new FileReader( file ) ) ) {
			mapReader = new CsvMapReader( rdr, CsvPreference.STANDARD_PREFERENCE );
			File propfile = propCSVFile( mapReader );
			setRdfMapFromFile( propfile ); // will throw an IOException if missing file

			createProcessors();
			processConceptRelationURIs( data );
			processNodePropURIs( data );
			// processRelationPropURIs( data );
			processRelationships( data );
		}
		finally {
			if ( null != mapReader ) {
				mapReader.close();
			}
		}

		return data;
	}

	/**
	 * Matches user inputed column type in prop file to the specific variable type
	 * name within Java SuperCSV API
	 */
	public void createProcessors() {
		// Columns in prop file that are NON_OPTIMAL must contain a value
		Map<String, URI> dtlkp = new HashMap<>();
		dtlkp.put( "Double", XMLSchema.DOUBLE );
		dtlkp.put( "Int", XMLSchema.INT );
		dtlkp.put( "Integer", XMLSchema.INTEGER );
		dtlkp.put( "Float", XMLSchema.FLOAT );
		dtlkp.put( "String", XMLSchema.STRING );

		for ( int col = 0; col < header.length; col++ ) {
			// find the type for each column
			String type = rdfMap.getProperty( Integer.toString( col + 1 ), null );

			// we have some sort of datatype to worry about, so keep track of it
			if ( dtlkp.containsKey( type ) ) {
				datatypes.put( header[col], dtlkp.get( type ) );
			}
		}
	}

	public void processRelationships( ImportData data ) throws IOException {
		Map<String, LoadingSheetData> rels = new HashMap<>();
		Map<String, LoadingSheetData> nodes = new HashMap<>();
		for ( LoadingSheetData r : data.getSheets() ) {
			if ( r.isRel() ) {
				rels.put( r.getName(), r );
			}
			else {
				nodes.put( r.getName(), r );
			}
		}

		//start count at 1 just row 1 is the header
		int count = 1;
		int startRow = Integer.parseInt( rdfMap.getProperty( START_ROW, "2" ) );

		while ( count < startRow && mapReader.read( header ) != null ) {
			count++;
			logger.debug( "Skipping line: " + count );
		}

		// get all the relation
		// max row predetermined value
		// overwrite this value if user specified the max rows to load
		int maxRows = Integer.parseInt( rdfMap.getProperty( END_ROW, "10000" ) );

		// only start from the maxRow - the startRow
		// added -1 is because of index nature
		// the earlier rows should already have been skipped
		Map<String, String> jcrMap;
		//while ( null != ( jcrMap = mapReader.read( header, processors ) )
		//  && count < maxRows ) {
		while ( ++count < maxRows
				&& null != ( jcrMap = mapReader.read( header ) ) ) {
			// logger.debug( "Process line: " + count );

			for ( Map.Entry<String, List<String>> en : nodePropArrayList.entrySet() ) {
				String nodetype = en.getKey();
				Collection<String> valuesForProp = en.getValue();

				String sbjinstance = createInstanceValue( nodetype, jcrMap );

				LoadingSheetData nlsd = nodes.get( nodetype );

				Map<String, Value> props = new HashMap<>();
				for ( String propValColumn : valuesForProp ) {
					Value v = createObject( propValColumn, jcrMap, data );
					if ( null != v ) {
						props.put( propValColumn, v );
					}
				}

				nlsd.add( sbjinstance, props );
			}

			// process all relationships in row
			for ( String relation : relationArrayList ) {
				Matcher m = RELATION_PAT.matcher( relation );
				if ( !m.matches() ) {
					logger.error( "can't find previously-found match (?)" );
					break; // don't expect to ever get here
				}

				LoadingSheetData rlsd = rels.get( relation );

				// get the subject and object for triple (the two indexes)
				String sub = m.group( 1 );
				// String predicate = m.group( 2 );
				String obj = m.group( 3 );

				String sbjinstance = createInstanceValue( sub, jcrMap );
				String objinstance = createInstanceValue( obj, jcrMap );
				rlsd.add( sbjinstance, objinstance, new HashMap<>() );

				// FIXME: need to worry about relationship properties
			}
		}
	}

	private void processConceptRelationURIs( ImportData data ) {
		// get the list of relationships from the prop file

		Map<String, LoadingSheetData> rels = new HashMap<>();

		if ( null != rdfMap.getProperty( RELATION ) ) {
			String relationNames = rdfMap.getProperty( RELATION );

			ValueFactory vf = new ValueFactoryImpl();

			relationArrayList.clear();
			for ( String relation : relationNames.split( ";" ) ) {
				Matcher m = RELATION_PAT.matcher( relation );
				if ( !m.matches() ) {
					logger.warn( "skipping unparseable relationship definition: " + relation );
					break;
				}

				relationArrayList.add( relation );
				logger.debug( "Loading relation " + relation );
				// get the subject and object for triple (the two indexes)
				String sub = m.group( 1 );
				String predicate = m.group( 2 );
				String obj = m.group( 3 );

				String subjectLabel = processAutoConcat( sub );
				String objectLabel = processAutoConcat( obj );

				// String name, String sType, String oType,String relname
				LoadingSheetData rlsd = LoadingSheetData.relsheet( relation, subjectLabel,
						objectLabel, predicate );
				data.add( rlsd );
				rels.put( relation, rlsd );
			}
		}
	}

	public void processNodePropURIs( ImportData data ) {

		Map<String, LoadingSheetData> nodes = new HashMap<>();

		if ( null != rdfMap.getProperty( NODE_PROP ) ) {
			nodePropArrayList.clear();
			String nodePropNames = rdfMap.getProperty( NODE_PROP );

			for ( String nt : nodePropNames.split( ";" ) ) {
				Matcher m = NODEPROP_PAT.matcher( nt );
				if ( !m.matches() ) {
					break;
				}

				logger.debug( "Loading Node Prop " + nt );

				// get the subject and object for triple (the two indexes)
				String sub = m.group( 1 );
				String prop = m.group( 2 );
				if ( !nodePropArrayList.containsKey( sub ) ) {
					nodePropArrayList.put( sub, new ArrayList<>() );
				}

				List<String> propnames = Arrays.asList( prop.split( "%" ) );
				nodePropArrayList.get( sub ).addAll( propnames );

				String subjectLabel = processAutoConcat( sub );

				if ( !nodes.containsKey( sub ) ) {
					LoadingSheetData nlsd = LoadingSheetData.nodesheet( sub, subjectLabel );
					nodes.put( sub, nlsd );
					data.add( nlsd );
				}

				LoadingSheetData nlsd = nodes.get( sub );
				for ( String pname : propnames ) {
					nlsd.addProperty( pname, datatypes.get( pname ) );
				}
			}
		}
	}

	public void processRelationPropURIs( ImportData data ) {
		if ( rdfMap.getProperty( RELATION_PROP ) == null ) {
			return;
		}

		logger.error( "this function doesn't do anything" );

		String propNames = rdfMap.getProperty( RELATION_PROP );
		StringTokenizer propTokens = new StringTokenizer( propNames, ";" );
		relPropArrayList.clear();

		while ( propTokens.hasMoreElements() ) {
			String relation = propTokens.nextToken();
			//just in case the end of the prop string is empty string or spaces
			if ( !relation.contains( "%" ) ) {
				break;
			}

			relPropArrayList.add( relation );

			logger.debug( "Loading relation prop " + relation );
			// get the subject (index 0) and all objects for triple
			// loop through all properties on the relationship
			String[] strSplit = relation.split( "%" );

			for ( int i = 1; i < strSplit.length; i++ ) {
				String prop = strSplit[i];

				// see if property node URI exists in prop file
				String property = prop;
				if ( rdfMap.containsKey( prop ) ) {
					String userProp = rdfMap.getProperty( prop );
					property = userProp.substring( userProp.lastIndexOf( "/" ) + 1 );
				}
				else if ( prop.contains( "+" ) ) {
					// if no user specified URI, use generic URI
					property = processAutoConcat( prop );
				}
			}
		}
	}

	/**
	 * Change the name of nodes that are concatenations of multiple CSV columns
	 * Example: changes the string "Cat+Dog" into "CatDog"
	 *
	 * @param input String name of the node that is a concatenation
	 *
	 * @return String name of the node removing the "+" to indicate a
	 * concatenation
	 */
	public String processAutoConcat( String input ) {
		return input.replaceAll( "\\+", "" );
	}

	/**
	 * Determine if the node is a concatenation of multiple columns in the CSV
	 * file
	 *
	 * @param input String containing the name of the node
	 *
	 * @return true when the node is a concatenation
	 */
	public boolean isProperConcatHeader( String input ) {
		boolean ret = true;
		List<String> headerList = Arrays.asList( header );
		for ( String split1 : input.split( "\\+" ) ) {
			if ( !headerList.contains( split1 ) ) {
				ret = false;
				break;
			}
		}
		return ret;
	}

	/**
	 * Constructs the node instance name
	 *
	 * @param subject String containing the node type name
	 * @param jcrMap Map containing the data in the CSV file
	 *
	 * @return retString String containing the instance level name
	 */
	public String createInstanceValue( String subject, Map<String, String> jcrMap ) {
		String retString = "";
		// if node is a concatenation
		if ( subject.contains( "+" ) ) {
			String elements[] = subject.split( "\\+" );
			for ( String subjectElement : elements ) {
				if ( jcrMap.containsKey( subjectElement ) && jcrMap.get( subjectElement ) != null ) {
					String value = jcrMap.get( subjectElement );
					retString = retString + value + "-";
				}
				else {
					retString = retString + "null-";
				}
			}
			// a - will show up at the end of this and we need to get rid of that
			if ( !retString.equals( "" ) ) {
				retString = retString.substring( 0, retString.length() - 1 );
			}
		}
		else {
			if ( jcrMap.containsKey( subject ) && jcrMap.get( subject ) != null ) {
				retString = jcrMap.get( subject );
			}
		}

		return retString;
	}

	private Value createObject( String object, Map<String, String> jcrMap,
			ImportData data ) {
		ValueFactory vf = new ValueFactoryImpl();

		// need to do the class vs. object magic
		if ( object.contains( "+" ) ) {
			StringBuilder strBuilder = new StringBuilder();
			String[] objList = object.split( "\\+" );
			for ( String objList1 : objList ) {
				strBuilder.append( jcrMap.get( objList1 ) );
			}
			return vf.createLiteral( strBuilder.toString() );
		}

		Object o = jcrMap.get( object );
		if ( null == o ) {
			return null;
		}

		String val = o.toString();
		// see if we have a special datatype to worry about
		if ( datatypes.containsKey( object ) ) {
			return vf.createLiteral( val, datatypes.get( object ) );
		}
		return vf.createLiteral( val );
	}

	/**
	 * Gets the headers for each column and returns the property file. If this
	 * reader already has a prop file set (from {@link #CSVReader(java.io.File,
	 * gov.va.semoss.util.UriBuilder, prerna.util.UriBuilder) }, this value from
	 * the header file is ignored, and the pre-set file is returned in all cases.
	 *
	 * @param file the CSV file
	 *
	 * @return the control file for the CSV (last column of the header row)
	 *
	 * @throws java.io.IOException
	 */
	private File propCSVFile( CsvMapReader rdr ) throws IOException {
		// store the headers of each of the columns
		String rawheaders[] = rdr.getHeader( true );
		header = new String[rawheaders.length];

		// don't copy the last index of the array, because there is no corresponding
		// data column (the last column is the control file location)
		System.arraycopy( rawheaders, 0, header, 0, header.length - 1 );

		// last header in CSV file is the absolute path to the prop file
		return ( null == propFile ? new File( rawheaders[rawheaders.length - 1] )
				: propFile );
	}
}
