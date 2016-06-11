/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.ui.actions.DbAction;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ryan
 */
public class SemtoolFileView extends FileView {

	private final Map<String, Icon> icons = new HashMap<>();

	public SemtoolFileView() {
		icons.put( "smss", getIcon( "smssdoc" ) );
		icons.put( "jnl", getIcon( "semossjnl" ) );
		icons.put( "xls", getIcon( "excel" ) );
		icons.put( "xlsx", getIcon( "excel" ) );
		icons.put( "csv", getIcon( "csv" ) );

		for ( String ext : new String[]{ "ttl", "rdf", "owl", "rdfs", "n3",
			"spq", "sparql" } ) {
			icons.put( ext, getIcon( "semantic-webdoc" ) );
		}
	}

	private Icon getIcon( String name ) {
		return DbAction.getIcon( name );
	}

	@Override
	public Icon getIcon( File f ) {
		// directories might be OpenRDF data stores
		if ( f.isDirectory() && Files.exists( Paths.get( f.getPath(), "repo" ) ) ) {
			return icons.get( "jnl" );
		}
		else {
			String ext = FilenameUtils.getExtension( f.getName() ).toLowerCase();
			return ( icons.containsKey( ext ) ? icons.get( ext ) : null );
		}
	}
}
