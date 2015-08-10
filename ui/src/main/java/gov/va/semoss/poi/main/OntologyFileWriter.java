/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import gov.va.semoss.util.Constants;

/**
 * Creates a custom map file that contains the URIs for all base objects, base predicates, base objects class, and base predicates class for the database
 */
public class OntologyFileWriter {

	private static final Logger logger = Logger.getLogger(OntologyFileWriter.class);

	public String fileName;
	File file;
	File tempFile;
	private String tempFileName;
	public String engineName;
	String questionFileName;
	FileWriter pw;

	/** 
	 * Functions as the main method for the class
	 * Creates a new custom map file containing the URIs created during loading and those already present in the existing map file
	 * @param ontologyFileName 	String containing the path to the current custom map file for the database
	 * @param newURIvalues 		Hashtable containing the added instance node URIs 
	 * @param newBaseURIvalues	Hashtable containing the added SEMOSS node URIs
   * @param newRelURIvalues
   * @param newBaseRelURIvalues
	 * @param propURI 			String	containing the base URI for properties
	 */
	public void runAugment(String ontologyFileName, Map<String, String> newURIvalues,
      Map<String, String> newBaseURIvalues, Map<String, String> newRelURIvalues,
      Map<String, String> newBaseRelURIvalues,
			String propURI){
		fileName = ontologyFileName;
		// clean the temp custom map file name if necessary
		if(ontologyFileName.contains("/"))
			tempFileName = ontologyFileName.substring(0, ontologyFileName.lastIndexOf("/")) + "/TEMP_" + ontologyFileName.substring(ontologyFileName.lastIndexOf("/")+1);
		else
			tempFileName = ontologyFileName.substring(0, ontologyFileName.lastIndexOf("\\")) + "\\TEMP_" + ontologyFileName.substring(ontologyFileName.lastIndexOf("\\")+1);
		// opens the current custom map file and creates a new custom map temp file
		openFile();
		try{
			insertValues(newURIvalues,newBaseURIvalues, newRelURIvalues,newBaseRelURIvalues, propURI);
		}catch(Exception e){
			logger.error( e );
		}
		// close and delete the original custom map file
		// rename the custom map temp file to the original custom map file name
		closeFile();
	}

	/**
	 * Reads the current custom map file to retrieve URIs already in the database
	 * Writes the URIs from the custom map file and the new URIs stored in Hashtables to a temp custom map file
	 * @param newURIvalues 		Hashtable containing the added instance node URIs 
	 * @param newBaseURIvalues	Hashtable containing the added SEMOSS node URIs
	 * @param relURIvalues		Hashtable containing the added instance relationship URIs
	 * @param relBaseURIvalues 	Hashtable containing the added SEMOSS relationship URIs
	 * @param propURI 			String	containing the base URI for properties
	 */
	private void insertValues(Map<String, String> newURIvalues, 
      Map<String, String> newBaseURIvalues, Map<String, String> relURIvalues, 
      Map<String, String> relBaseURIvalues, String propURI) throws IOException{
		String currentLine, propUriLine, ignoreUriLine;
		int lineNum, baseObjNum, basePredNum, baseObjClassNum, basePredClassNum, propUriNum, ignoreUriNum;
    try (Scanner scNum = new Scanner(new File (fileName))) {
      propUriLine = "";
      ignoreUriLine = "";
      lineNum = 0;
      baseObjNum = 0;
      basePredNum = 0;
      baseObjClassNum = 0;
      basePredClassNum = 0;
      propUriNum = 0;
      ignoreUriNum = 0;
      // determine line number where each of the sections occurs
      while(scNum.hasNextLine()){
        currentLine = scNum.nextLine();
        if(currentLine.length() >= 16 && currentLine.substring(0,16).equals("##Base Objects##"))
        {
          baseObjNum = lineNum;
        }
        if(currentLine.length() >= 19 && currentLine.substring(0,19).equals("##Base Predicates##"))
        {
          basePredNum = lineNum;
        }
        if(currentLine.length() >= 22 && currentLine.substring(0,22).equals("##Base Objects Class##"))
        {
          baseObjClassNum = lineNum;
        }
        if(currentLine.length() >= 25 && currentLine.substring(0,25).equals("##Base Predicates Class##"))
        {
          basePredClassNum = lineNum;
        }
        if(currentLine.length() >= 8 && currentLine.substring(0,8).equals(Constants.PROP_URI))
        {
          propUriNum = lineNum;
          propUriLine = currentLine;
        }
        if(currentLine.length() >= 10 && currentLine.substring(0,10).equals("IGNORE_URI"))
        {
          ignoreUriNum = lineNum;
          ignoreUriLine = currentLine;
        }
        lineNum++;
      }
    }

    try (Scanner scList = new Scanner(new File(fileName))) {
      String[] keyAndUri;
      // skip rows in beginning document to get past the first header
      for(int i = 0; i < baseObjNum + 1; i++){
        scList.nextLine();
      }
      // add objects from custom map if not in hashtable
      for(int i = baseObjNum + 1; i < basePredNum; i++){
        currentLine = scList.nextLine();
        if(currentLine != null && !currentLine.equals(""))
        {
          keyAndUri = currentLine.split(" ");
          if(!newURIvalues.containsKey(keyAndUri[0]))
          {
            newURIvalues.put(keyAndUri[0],keyAndUri[1]);
          }
        }
      }
      // skip header row
      scList.nextLine();
      // add predicate from custom map if not in hashtable
      for(int i = basePredNum + 1; i < baseObjClassNum; i++){
        currentLine = scList.nextLine();
        if(currentLine != null && !currentLine.equals(""))
        {
          keyAndUri = currentLine.split(" ");
          if(!relURIvalues.containsKey(keyAndUri[0]))
          {
            relURIvalues.put(keyAndUri[0],keyAndUri[1]);
          }
        }
      }
      // skip header row
      scList.nextLine();
      // add base objects from custom map if not in hashtable
      for(int i = baseObjClassNum + 1; i < basePredClassNum; i++){
        currentLine = scList.nextLine();
        if(currentLine != null && !currentLine.equals(""))
        {
          keyAndUri = currentLine.split(" ");
          if(!newBaseURIvalues.containsKey(keyAndUri[0]))
          {
            newBaseURIvalues.put(keyAndUri[0],keyAndUri[1]);
          }
        }
      }
      // skip header row
      scList.nextLine();
      // In order to get last line of base predicate classes, determine if prop URI or ignore URI is present
      int lastIndex;
      if(propUriNum > ignoreUriNum){
        lastIndex = basePredClassNum + lineNum - propUriNum;
      }
      // this takes into account that both are not present since both will be equal 0
      else{
        lastIndex = basePredClassNum + lineNum - ignoreUriNum;
      }
      // add base relationships from custom map if not in hashtable
      for(int i = basePredClassNum + 1; i < lastIndex; i++){
        currentLine = scList.nextLine();
        if(currentLine != null && !currentLine.equals(""))
        {
          keyAndUri = currentLine.split(" ");
          if(!relBaseURIvalues.containsKey(keyAndUri[0]))
          {
            relBaseURIvalues.put(keyAndUri[0],keyAndUri[1]);
          }
        }
      }
      
      String name;
      String uri;
      // populate the map file
      Iterator<String> iterator;
      // print base object data to map file
      pw.write("\n##Base Objects##\n\n");
      logger.debug("BASE OBJECTS");
      iterator = newURIvalues.keySet().iterator();
      while(iterator.hasNext()){
        name = iterator.next();
        uri = newURIvalues.get(name);
        pw.write(name + " " + uri + "\n");
        logger.debug(name + " " + uri);
      }
      // print base predicate data to map file
      pw.write("\n##Base Predicates##\n\n");
      logger.debug("BASE PREDICATES");
      iterator = relURIvalues.keySet().iterator();
      while(iterator.hasNext()){
        name = iterator.next();
        uri = relURIvalues.get(name);
        pw.write(name + " " + uri + "\n");
        logger.debug(name + " " + uri);
      }
      // print base object class data to map file
      pw.write("\n##Base Objects Class##\n\n");
      logger.debug("BASE OBJECTS CLASS");
      iterator = newBaseURIvalues.keySet().iterator();
      while(iterator.hasNext()){
        name = iterator.next();
        uri = newBaseURIvalues.get(name);
        pw.write(name + " " + uri + "\n");
        logger.debug(name + " " + uri);
      }
      // print base predicate class data to map file
      pw.write("\n##Base Predicates Class##\n\n");
      logger.debug("BASE PREDICATES CLASS");
      iterator = relBaseURIvalues.keySet().iterator();
      while(iterator.hasNext()){
        name = iterator.next();
        uri = relBaseURIvalues.get(name);
        pw.write(name + " " + uri + "\n");
        logger.debug(name + " " + uri);
      }
      // print the base property URI
      if(propUriLine.equals("")){
        propUriLine = Constants.PROP_URI +" " + propURI;
      }
      pw.write("\n" + propUriLine + "\n\n" + ignoreUriLine);
      logger.debug(propUriLine);
      logger.debug(ignoreUriLine);
    }
		System.gc();
	}

	/**
	 * Loads the existing custom map file
	 * Creates a custom map temp file
	 * Creates a file writer for the custom map temp file
	 */
	private void openFile()
	{
		file = new File(fileName);
		tempFile = new File(tempFileName);
		try {
			pw = new FileWriter(tempFile);
		} catch (IOException e) {
			logger.error( e );
		}
	}

	/**
	 * Close the file writer for the custom map temp file
	 * Delete the old custom map file
	 * Rename the updated custom map temp file to the original custom map file name
	 */
	private void closeFile(){
		try {
			pw.flush();
			pw.close();
			file.delete();
			tempFile.renameTo(file);
		} catch (IOException e) {
			logger.error( e );
		}
	}
}