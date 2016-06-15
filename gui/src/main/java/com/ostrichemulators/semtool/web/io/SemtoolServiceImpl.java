/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.io;

/**
 *
 * @author ryan
 */
public class SemtoolServiceImpl implements SemtoolService {

	private final String root;

	public SemtoolServiceImpl( String root ) {
		this.root = root;
	}

	@Override
	public String root() {
		return this.root;
	}

	@Override
	public String databases() {
		return root + "/databases/";
	}

	@Override
	public String user() {
		return root + "/login?whoami";
	}
}
