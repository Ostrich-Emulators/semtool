/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.main.listener.impl;



import com.google.gson.Gson;
import com.teamdev.jxbrowser.chromium.JSValue;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryException;

/**
 */
public class InferEngineFunction extends AbstractBrowserSPARQLFunction {
	
	/**
	 * Method invoke.
	 * @param arg0 Object[]
	
	 * @return Object */
	@Override
	public JSValue invoke(JSValue... arg0) {
    boolean ok = false;
    try{
      engine.calculateInferences();
    	ok= true;
    }
    catch( RepositoryException re ){
      Logger.getLogger( InferEngineFunction.class ).error(  re, re );
    }
    
    Map<String, Boolean> retHash = new HashMap<>();
    retHash.put("success", ok);
		Gson gson = new Gson();        
		return JSValue.create(gson.toJson(retHash));
	}
	
}
