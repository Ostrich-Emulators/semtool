/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

/**
 *
 * @author john
 */
public class CreateCountryCodeHashtableInJson {
	private static final Logger log = Logger.getLogger( CreateCountryCodeHashtableInJson.class );
	private static final File COUNTRYCODESCSV = new File( "src/test/resources/countrycodes.csv" );
	private static final File COUNTRYCODESJS = new File( "src/test/resources/countrycodes.js" );

	private HashMap<String,HashMap<String,String>> theUberHash = new HashMap<String,HashMap<String,String>>();
	
	public CreateCountryCodeHashtableInJson() {}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@After
	public void tearDown() throws FileNotFoundException {
		PrintWriter out = new PrintWriter(COUNTRYCODESJS);
		out.println( "var countryCodes = " + new Gson().toJson( theUberHash ) + ";" );
		out.close();
	}

	@Test( )
	public void createJson() throws Exception {
		try ( BufferedReader rdr = new BufferedReader( new FileReader( COUNTRYCODESCSV ) ) ) {
			rdr.lines().forEach( line -> processLine( line ) );
		}
		finally {
		}
	}
	
	private void processLine(String line) {
		theUberHash.putAll(new CountryCode(line).putInHashMap());
	}
	
	class CountryCode {
		String name;
		String twoLetterCode;
		String threeLetterCode;
		String numericalCode;
		
		static final String NAME = "name";
		static final String TWOLETTERCODE = "twoLetterCode";
		static final String THREELETTERCODE = "threeLetterCode";
		static final String NUMERICALCODE = "numericalCode";

		public CountryCode(String line) {
			String[] pieces = line.split(",");
			if (pieces.length != 4) {
				log.error("Could not parse out country codes.");
				return;
			}
			
			name = pieces[0].replace("<>", ",");
			twoLetterCode = pieces[1];
			threeLetterCode = pieces[2];
			numericalCode = pieces[3];
			
			if (numericalCode.length() == 2) {
				numericalCode = "0" + numericalCode;
			}
			
			if (numericalCode.length() == 1) {
				numericalCode = "00" + numericalCode;
			}
		}
		
		public HashMap<String,HashMap<String,String>> putInHashMap() {
			HashMap<String,String> objectHash = new HashMap<String,String>();			
			objectHash.put(NAME, name);
			objectHash.put(TWOLETTERCODE, twoLetterCode);
			objectHash.put(THREELETTERCODE, threeLetterCode);
			objectHash.put(NUMERICALCODE, numericalCode);
			
			HashMap<String,HashMap<String,String>> containingHash = new HashMap<String,HashMap<String,String>>();
			containingHash.put(name, objectHash);
			containingHash.put(twoLetterCode, objectHash);
			containingHash.put(threeLetterCode, objectHash);
			containingHash.put(numericalCode, objectHash);
			
			return containingHash;
		}
	}
}