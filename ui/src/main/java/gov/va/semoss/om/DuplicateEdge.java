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
public class DuplicateEdge extends SEMOSSEdgeImpl implements DuplicateGraphElement {

	private URI real;

	public DuplicateEdge( SEMOSSVertex _outVertex, SEMOSSVertex _inVertex, URI _uri ) {
		super( _outVertex, _inVertex, _uri );
	}

	public DuplicateEdge( URI _uri ) {
		super( _uri );
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
