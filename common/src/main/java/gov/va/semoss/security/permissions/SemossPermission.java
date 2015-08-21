/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.security.permissions;

import java.security.BasicPermission;

/**
 *
 * @author ryan
 */
public class SemossPermission extends BasicPermission {

	public static final SemossPermission ADMIN = new SemossPermission( "semoss.*" );
	public static final SemossPermission LOGVIEWER = new SemossPermission( "semoss.gui.logviewer" );
	public static final SemossPermission INSIGHTREADER = new SemossPermission( "semoss.insight.reader" );
	public static final SemossPermission INSIGHTWRITER = new SemossPermission( "semoss.insight.*" );
	public static final SemossPermission DATAWRITER = new SemossPermission( "semoss.data.*" );
	public static final SemossPermission DATAREADER = new SemossPermission( "semoss.data.reader" );

	public SemossPermission( String name ) {
		super( name );
	}

	public SemossPermission( String name, String actions ) {
		super( name, actions );
	}

	public static SecurityException newSecEx() {
		return new SecurityException( "You do not have permission to access this feature" );
	}
}
