package gov.va.semoss.web.security;

import java.util.HashMap;

import org.openrdf.model.URI;

public class DBPrivileges extends HashMap<URI, DbAccess>{

	private static final long serialVersionUID = 1L;
	
	private String username;
	
	public DBPrivileges (String username){
		this.username = username;
	}
	
	public DBPrivileges(){
		
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public void getUsername(String username){
		this.username = username;
	}
	
	public boolean hasReadAccess(URI uri){
		if (this.containsKey(uri)){
			DbAccess access = this.get(uri);
			if (access.equals(DbAccess.WRITE) | access.equals(DbAccess.READ)){
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean hasWriteAccess(URI uri){
		if (this.containsKey(uri)){
			DbAccess access = this.get(uri);
			if (access.equals(DbAccess.WRITE)){
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

}
