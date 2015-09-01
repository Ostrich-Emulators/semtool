package gov.va.semoss.web.filters;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * @author Wayne Warren
 *
 */
public class ServerACL extends AbstractAccessControlList  {

	
	public void initialize(){
		
	}
	
	public boolean currentUserCanRead(){
		
		return true;
	}
	
	public boolean currentUserCanWrite(){
		return true;
	}
	
}