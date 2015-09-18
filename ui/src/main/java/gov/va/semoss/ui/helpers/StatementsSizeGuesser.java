/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.helpers;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 * A class to estimate the number of statements that will come out of various
 * files types/sizes
 *
 * @author ryan
 */
public class StatementsSizeGuesser {

	public static final int DISK_SIZE_ADVISOR_LIMIT = 1024 * 1024 * 30; // 30M statements
	private static final Map<String, Double> SCALINGS = new HashMap<>();

	static {
		SCALINGS.put( "xlsx", 4d );
		SCALINGS.put( "csv", 2d );
		SCALINGS.put( "nt", 1d );
		SCALINGS.put( "ttl", 1.3d );
	}

	private StatementsSizeGuesser() {
	}

	public static boolean shouldUseDisk( Collection<File> files ) {
		return ( guessStatements( files ) > DISK_SIZE_ADVISOR_LIMIT );
	}

	public static int guessStatements( Collection<File> files ) {
		int size = 0;
		for ( File f : files ) {
			String extension = FilenameUtils.getExtension( f.getName() ).toLowerCase();
			double fsize = f.length() * SCALINGS.getOrDefault( extension, 1d );
			size += (int) ( Math.floor( fsize ) );
		}

		return size;
	}

}
