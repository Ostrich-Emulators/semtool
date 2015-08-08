/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface DuplicateGraphElement extends GraphElement {

	public boolean isDuplicateOf( GraphElement ge );

	public URI getRealUri();

	public void setRealUri( URI uri );

}
