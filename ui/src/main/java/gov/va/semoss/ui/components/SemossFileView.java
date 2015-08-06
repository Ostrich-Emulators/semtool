/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.ui.actions.DbAction;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ryan
 */
public class SemossFileView extends FileView {

	private final Map<String, Icon> icons = new HashMap<>();

	public SemossFileView() {
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
		String ext = FilenameUtils.getExtension( f.getName() ).toLowerCase();
		return ( icons.containsKey( ext ) ? icons.get( ext ) : null );
	}
}
