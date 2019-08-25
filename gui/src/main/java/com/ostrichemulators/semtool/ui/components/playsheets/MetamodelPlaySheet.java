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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import com.ostrichemulators.semtool.om.GraphDataModel;
import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManager;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManagerFactory;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.Utility;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

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
	public MultiMap<IRI, SEMOSSVertex> getVerticesByType() {
		MultiMap<IRI, SEMOSSVertex> typeToInstances = new MultiMap<>();
		for ( SEMOSSVertex v : getVisibleGraph().getVertices() ) {
			typeToInstances.add( v.getType(), v );
		}
		return typeToInstances;
	}

	@Override
	public MultiMap<IRI, SEMOSSEdge> getEdgesByType() {
		MultiMap<IRI, SEMOSSEdge> typeToInstances = new MultiMap<>();
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

		StructureManager sm = StructureManagerFactory.getStructureManager( engine );

		Collection<IRI> concepts = sm.getTopLevelConcepts();
		// make sure we can add nodes for concepts that don't have any edges
		Set<IRI> conceptsNoEdges = new HashSet<>( concepts );

		for ( IRI stype : concepts ) {
			for ( IRI otype : concepts ) {
				Model m = sm.getLinksBetween( stype, otype );
				for ( IRI edge : m.predicates() ) {
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
				IRI sub = IRI.class.cast( s.getSubject() );
				IRI obj = IRI.class.cast( s.getObject() );
				IRI rel = s.getPredicate();

				SEMOSSVertex src = super.createOrRetrieveVertex( sub, overlayLevel );
				SEMOSSVertex dst = super.createOrRetrieveVertex( obj, overlayLevel );
				src.setType( RDFS.CLASS );
				dst.setType( RDFS.CLASS );

				SEMOSSEdge edge = super.createOrRetrieveEdge( rel, src, dst, overlayLevel );

				graph.addEdge( edge, src, dst );
			}


			Map<IRI, String> edgelabels
					= Utility.getInstanceLabels( model.predicates(), engine );
			for ( Statement s : model ) {

				String edgekey = s.getPredicate().stringValue()
						+ s.getSubject().stringValue()
						+ s.getObject().stringValue();
				SEMOSSEdge edge = edgeStore.get( edgekey );
				String elabel = edgelabels.get( s.getPredicate() );
				edge.setLabel( elabel );
			}

			fireModelChanged( overlayLevel );
			return elementsFromLevel( overlayLevel );
		}

		@Override
		public Collection<GraphElement> addGraphLevel( Collection<IRI> nodes,
				IEngine engine, int overlayLevel ) {
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = super.getGraph();

			for ( IRI u : nodes ) {
				SEMOSSVertex src = super.createOrRetrieveVertex( u, overlayLevel );
				src.setType( RDFS.CLASS );
				graph.addVertex( src );
			}

			fireModelChanged( overlayLevel );
			return elementsFromLevel( overlayLevel );
		}

	}
}
