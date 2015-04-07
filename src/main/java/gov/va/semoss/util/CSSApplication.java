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
package gov.va.semoss.util;

import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.StyleException;
import org.apache.log4j.Logger;

/**
 * This class is used to apply specific CSS functionality in the playsheets and UI.
 */
public class CSSApplication {
  private static final Logger log = Logger.getLogger( CSSApplication.class );
	/**
	 * Unregisters a target from the engine, assigns CSS classnames to the target, and applies a specified stylesheet to the target.
	 * @param object Object		Target for CSS to be applied to.
	 * @param cssLine String	Line of CSS code that is applied to target.
	 */
	public CSSApplication(Object object, String cssLine)
	{
	try {
		Style.unregisterTargetClassName(object);
		Style.registerTargetClassName(object, cssLine);
		Style.apply(object, new Style(getClass().getResource("/styles.css")));
	} catch (StyleException e1) {
		log.error( e1 );
		}
	}
	
	/**
	 * Applies a specified CSS stylesheet to the target.
	 * @param object Object		Target for CSS to be applied to.
	 */
	public CSSApplication(Object object)
	{
	try {
		Style.apply(object, new Style(getClass().getResource("/styles.css")));
	} catch (StyleException e1) {
		log.error( e1 );
		}
	}
}
