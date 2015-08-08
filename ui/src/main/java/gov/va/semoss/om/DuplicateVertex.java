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
public class DuplicateVertex extends SEMOSSVertexImpl implements DuplicateGraphElement {

	private URI real;

	public DuplicateVertex( URI id ) {
		super( id );
	}

	public DuplicateVertex( URI id, URI type, String label ) {
		super( id, type, label );
	}

	@Override
	public boolean isDuplicateOf( GraphElement ge ) {
		return ge.getURI().equals( real );
	}

	@Override
	public URI getRealUri() {
		return real;
	}

	@Override
	public void setRealUri( URI uri ) {
		real = uri;
	}
}
