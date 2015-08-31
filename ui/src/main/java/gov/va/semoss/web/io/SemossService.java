/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.io;

/**
 * And interface describing the layout of the Semoss web service endpoints
 * @author ryan
 */
public interface SemossService {

	public String root();
	
	public String databases();

	public String user();
}
