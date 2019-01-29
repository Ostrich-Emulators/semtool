/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.om;

import java.util.Map;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 *
 * @author pkapaleeswaran Something that expresses the edge
 * @version $Revision: 1.0 $
 */
public class SEMOSSEdgeImpl extends AbstractGraphElement implements SEMOSSEdge {

	private IRI specifictype;

	public SEMOSSEdgeImpl( IRI _uri ) {
		super( _uri, null, _uri.getLocalName() );
	}

	@Override
	public void setSpecificType( IRI st ) {
		specifictype = st;
	}

	@Override
	public IRI getSpecificType() {
		return specifictype;
	}

	@Override
	public SEMOSSEdgeImpl duplicate() {
		SEMOSSEdgeImpl newone = new SEMOSSEdgeImpl( getIRI() );
		newone.setLabel( getLabel() );
		newone.setSpecificType( specifictype );

		for ( Map.Entry<IRI, Value> en : getValues().entrySet() ) {
			newone.setValue( en.getKey(), en.getValue() );
		}

		return newone;
	}

	@Override
	public boolean isNode() {
		return false;
	}
}
