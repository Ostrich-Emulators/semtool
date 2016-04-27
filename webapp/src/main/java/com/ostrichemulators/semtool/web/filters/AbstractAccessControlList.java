package com.ostrichemulators.semtool.web.filters;

import java.util.Collection;

import com.ostrichemulators.semtool.web.security.SemossUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractAccessControlList {

	
	protected SemossUser getCurrentUser(){
		SemossUser currentUser = (SemossUser)SecurityContextHolder.getContext().getAuthentication();
		return currentUser;
	}
	
	protected GrantedAuthority[] getCurrentUserRoles(){
		Collection<GrantedAuthority> roles = getCurrentUser().getAuthorities();
		GrantedAuthority[] array = new GrantedAuthority[roles.size()];
		roles.toArray(array);
		return array;
	}
}
