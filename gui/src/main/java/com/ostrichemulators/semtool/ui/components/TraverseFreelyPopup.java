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
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.NeighborMenuItem;
import com.ostrichemulators.semtool.om.GraphElement;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.transformer.LabelTransformer;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.event.MouseAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

/**
 * This class is used to create a popup menu for the TF instance relation.
 */
public class TraverseFreelyPopup extends JMenu {

	private static final Logger logger
			= Logger.getLogger( TraverseFreelyPopup.class );
	private static final long serialVersionUID = -3788376340326236013L;
	private final GraphPlaySheet gps;
	private final Collection<IRI> instances = new HashSet<>();
	private final boolean isInstance;
	private boolean populated = false;

	private final IEngine engine;

	public TraverseFreelyPopup( GraphElement vertex, IEngine e,
			GraphPlaySheet ps, Collection<SEMOSSVertex> picked, boolean instance ) {
		super( "Traverse Freely: "
				+ ( instance
						? LabelTransformer.chop( vertex.getLabel(), 30 )
						: "All " + ps.getLabelCache().get( vertex.getType() ) + "(s) " ) );
		this.isInstance = instance;
		this.gps = ps;
		this.engine = e;

		// if we're only looking at this one instance, just use the URIs from
		// pickedVertex. if we're looking at all instances with this type, we 
		// need to process the graph
		Set<SEMOSSVertex> verts = new HashSet<>( picked );
		if ( !isInstance ) {
			MultiMap<IRI, SEMOSSVertex> typelkp = gps.getVerticesByType();
			Set<IRI> seen = new HashSet<>();

			for ( SEMOSSVertex v : picked ) {
				if ( !seen.contains( v.getType() ) ) {
					seen.add( v.getType() );
					verts.addAll( typelkp.getNN( v.getType() ) );
				}
			}
		}

		for ( SEMOSSVertex thisVert : verts ) {
			instances.add( thisVert.getIRI() );
		}

		addMouseListener( new MouseAdapter() {
			@Override
			public void mouseEntered( MouseEvent arg0 ) {
				if ( !populated ) {
					int added = addRelations( true );
					if ( added > 0 ) {
						addSeparator();
					}
					addRelations( false );
				}
			}
		} );
	}

	/**
	 * Queries the database to get neighboring instance types
	 *
	 * @param subjectStyle are the <code>pickedVertex</code>s subjects or objects?
	 * @return the number of items added
	 */
	public int addRelations( boolean subjectStyle ) {
		// we have two alternate st
		Set<IRI> neighborTypes = new HashSet<>();

		if ( isInstance ) {
			for ( IRI instance : instances ) {
				neighborTypes.addAll(
						NodeDerivationTools.getConnectedConceptTypes( instance, engine,
								subjectStyle ) );
			}
		}
		else {
			neighborTypes.addAll(
					NodeDerivationTools.getConnectedConceptTypes( instances, engine,
							subjectStyle ) );
		}
		neighborTypes.removeAll( Arrays.asList( OWL.THING, RDFS.RESOURCE, SKOS.CONCEPT,
				engine.getSchemaBuilder().getConceptIri().build() ) );

		for ( IRI nt : neighborTypes ) {
			logger.debug( "neighbor type: " + nt );
		}

		neighborTypes.removeAll( Arrays.asList( RDFS.RESOURCE, RDFS.CLASS,
				OWL.NOTHING, OWL.THING, OWL.CLASS ) );

		Map<IRI, String> labelmap = Utility.getInstanceLabels( neighborTypes, engine );
		labelmap = Utility.sortIrisByLabel( labelmap );

		if ( !labelmap.isEmpty() ) {
			add( new JLabel( subjectStyle ? "To:" : "From:" ) );
		}

		for ( Map.Entry<IRI, String> en : labelmap.entrySet() ) {
			add( new NeighborMenuItem( en.getValue(), gps,
					getExpander( instances, en.getKey(), subjectStyle ) ) );
		}

		populated = true;
		return neighborTypes.size();
	}

	private ModelQueryAdapter getExpander( Collection<IRI> instances, IRI totype,
			boolean instanceIsSubject ) {
		// we only want the "root" edges (those in the 
		// schema namespace, also, those without properties)
		StringBuilder query = new StringBuilder( "CONSTRUCT { ?subject ?predicate ?object } " )
				.append( "WHERE { " )
				.append( "  ?subject ?predicate ?object ." )
				.append( "  ?subject a ?subtype . FILTER( ?subtype != ?skos ) ." )
				.append( "  ?object a ?objtype . FILTER( ?objtype != ?skos ) ." )
				.append( "   VALUES ?" )
				.append( instanceIsSubject ? "subject" : "object" );
		query.append( "{ " )
				.append( Utility.implode( instances ) )
				.append( "} ." );
		query.append( "}" );

		ModelQueryAdapter mqa = new ModelQueryAdapter( query.toString() );
		mqa.bind( "skos", SKOS.CONCEPT );
		mqa.bind( instanceIsSubject ? "objtype" : "subtype", totype );
		logger.debug( "expander query is: " + mqa.bindAndGetSparql() );

		return mqa;
	}
}
