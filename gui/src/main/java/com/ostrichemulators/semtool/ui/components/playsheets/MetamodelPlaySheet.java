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
package com.ostrichemulators.semtool.ui.components.playsheets;

import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;

import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import com.ostrichemulators.semtool.util.MultiMap;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDFS;

/**
 */
public class MetamodelPlaySheet extends GraphPlaySheet implements PropertyChangeListener {

	private static final long serialVersionUID = 4699492732234656487L;
	private static final Logger log = Logger.getLogger( MetamodelPlaySheet.class );

	/**
	 * Constructor for GraphPlaySheetFrame.
	 */
	public MetamodelPlaySheet() {
		super( new MetamodelGraphDataModel() );
	}

	@Override
	public MultiMap<URI, SEMOSSVertex> getVerticesByType() {
		MultiMap<URI, SEMOSSVertex> typeToInstances = new MultiMap<>();
		for ( SEMOSSVertex v : getVisibleGraph().getVertices() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	@Override
	public MultiMap<URI, SEMOSSEdge> getEdgesByType() {
		MultiMap<URI, SEMOSSEdge> typeToInstances = new MultiMap<>();
		for ( SEMOSSEdge v : getVisibleGraph().getEdges() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	/**
	 * Create a new metamodel graph, regardless of input
	 *
	 * @param data ignored
	 * @param headers ignored
	 * @param engine
	 * @throws IllegalArgumentException
	 *
	 */
	@Override
	public void create( List<Value[]> data, List<String> headers, IEngine engine ) {
		LinkedHashModel model = new LinkedHashModel();

		Collection<URI> concepts = NodeDerivationTools.createConceptList( engine );
		// make sure we can add nodes for concepts that don't have any edges
		Set<URI> conceptsNoEdges = new HashSet<>( concepts );

		for ( URI stype : concepts ) {
			for ( URI otype : concepts ) {
				for ( URI edge : NodeDerivationTools.getConnections( stype, otype, engine ) ) {
					model.add( stype, edge, otype );

					conceptsNoEdges.remove( stype );
					conceptsNoEdges.remove( otype );

					getView().getEdgeLabelTransformer().setDisplay( edge, RDFS.LABEL, true );
				}
			}
		}

		add( model, new ArrayList<>( conceptsNoEdges ), engine );
	}

	private static class MetamodelGraphDataModel extends GraphDataModel {

		public MetamodelGraphDataModel() {
			super( new DirectedSparseMultigraph<>() );
		}

		@Override
		public Collection<GraphElement> addGraphLevel( Model model, IEngine engine,
				int overlayLevel ) {
			// we're lucky here, because we know all our subjects and objects in the
			// model are classes, so we don't have to do a bunch of hunting to
			// figure out properties and such

			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = super.getGraph();

			for ( Statement s : model ) {
				URI sub = URI.class.cast( s.getSubject() );
				URI obj = URI.class.cast( s.getObject() );
				URI rel = s.getPredicate();

				SEMOSSVertex src = super.createOrRetrieveVertex( sub, overlayLevel );
				SEMOSSVertex dst = super.createOrRetrieveVertex( obj, overlayLevel );
				src.setType( RDFS.CLASS );
				dst.setType( RDFS.CLASS );

				SEMOSSEdge edge = super.createOrRetrieveEdge( rel, src, dst, overlayLevel );

				graph.addEdge( edge, src, dst );
			}

			return elementsFromLevel( overlayLevel );
		}

		@Override
		public Collection<GraphElement> addGraphLevel( Collection<URI> nodes, IEngine engine, int overlayLevel ) {
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = super.getGraph();

			for ( URI u : nodes ) {
				SEMOSSVertex src = super.createOrRetrieveVertex( u, overlayLevel );
				src.setType( RDFS.CLASS );
				graph.addVertex( src );
			}

			return elementsFromLevel( overlayLevel );
		}

	}
}
