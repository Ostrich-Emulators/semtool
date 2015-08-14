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

	public static enum PermissionType {

		INSIGHT_MANAGER( "semoss.insightmanager" ),
		LOG_VIEWER( "semoss.logviewer" );

		private final String val;

		PermissionType( String val ) {
			this.val = val;
		}

		public String stringValue() {
			return val;
		}
	};

	public SemossPermission( String name ) {
		super( name );
	}

	public SemossPermission( String name, String actions ) {
		super( name, actions );
	}

	public static BasicPermission insightManager() {
		return new SemossPermission( PermissionType.INSIGHT_MANAGER.stringValue() );
	}

	public static BasicPermission logViewer() {
		return new SemossPermission( PermissionType.LOG_VIEWER.stringValue() );
	}

}
