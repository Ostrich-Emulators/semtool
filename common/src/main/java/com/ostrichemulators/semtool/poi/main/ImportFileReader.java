/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author ryan
 */
public interface ImportFileReader {

	/**
	 * Reads and parses the given file
	 *
	 * @param f the file to parse
	 * @return the data
	 * @throws IOException If there is a problem opening the file
	 * @throws com.ostrichemulators.semtool.poi.main.ImportValidationException If the file can be
	 * opened, but some other problem prevents it from being parsed
	 */
	public ImportData readOneFile( File f ) throws IOException, ImportValidationException;

	/**
	 * Reads the file as little as possible to return the metadata. This function
	 * can be more performant than {@link #readOneFile(java.io.File)} if all you
	 * want is the metadata and not the actual data
	 *
	 * @param f the file to read
	 * @return the metadata from the file
	 * @throws IOException
	 * @throws com.ostrichemulators.semtool.poi.main.ImportValidationException
	 */
	public ImportMetadata getMetadata( File f ) throws IOException, ImportValidationException;
	
	public void keepLoadInMemory( boolean b );
}
