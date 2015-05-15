/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 *
 * @author ryan
 */

public interface GraphWriter {

	public static final Pattern NUMERIC = Pattern.compile( "^\\d+.?\\d*$" );

	/**
	 * Writes the given data to the output file. This file (and it's parents) will
	 * be created if they don't already exist.
	 *
	 * @param data
	 * @param output
	 * @throws IOException
	 */
	void write( ImportData data, File file ) throws IOException;

	void write( ImportData data, OutputStream out ) throws IOException;
}
