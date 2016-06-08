/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.util.Constants;
import java.util.Collection;
import java.util.Set;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 * A class to handle reading/writing {@link SEMTOOL#Structure} triples. All of
 * the functions can accept either the "top level" class/prop types, or an
 * instance of them, but in all cases, they return top-level data only. For
 * specific instances, use {@link NodeDerivationTools}.
 *
 * @author ryan
 */
public interface StructureManager {

	/**
	 * Gets the properties that can be applied to the given type (either node or
	 * edge)
	 *
	 * @param type the schema type to check
	 * @return a set of all possible datatype properties this type might have
	 */
	public Set<URI> getPropertiesOf( URI type );

	/**
	 * Gets the links that can connect the subject and object class types.
	 *
	 * @param subtype the type of the subject, or {@link Constants#ANYNODE}
	 * @param objtype the type of the object, or {@link Constants#ANYNODE}
	 * @return a model whose statements are subject type -&gt; link type -&gt;
	 * object type
	 */
	public Model getLinksBetween( URI subtype, URI objtype );

	/**
	 * Gets the possible subjects and objects for a given edge type
	 *
	 * @param reltype the type of edge
	 * @return
	 */
	public Model getEndpoints( URI reltype );

	/**
	 * Gets all the relation types for which we have structure data
	 *
	 * @param instances a collection of instances or top-level edges. if null, all
	 * top-level relations will be returned. If empty, the empty set will be
	 * returned
	 * @return
	 */
	public Set<URI> getTopLevelRelations( Collection<URI> instances );

	/**
	 * Gets all the top-level concepts. This function is not based on structure data,
	 * but rather a query on the db
	 * @return 
	 */
	public Set<URI> getTopLevelConcepts();

	/**
	 * Gets the set of concepts connected to these instances
	 *
	 * @param instances the instances (or concept classes) to connect
	 * @return a model containing all instance -&gt; edge type -&gt; concept class
	 * *and* subject class -&gt; edge type -&gt; instance
	 */
	public Model getConnectedConceptTypes( Collection<URI> instances );

	/**
	 * Rebuilds the {@link SEMTOOL#Structure} triples by inspecting the IEngine's
	 * triples
	 *
	 * @param saveToEngine once calculated, save these statements in the engine?
	 * @return All the Structure triples
	 */
	public Model rebuild( boolean saveToEngine );

	/**
	 * Rebuilds the {@link SEMTOOL#Structure} triples by inspecting the IEngine's
	 * triples
	 *
	 * @param uris only rebuild structures for the given concepts/edges
	 * @return The rebuilt Structure triples
	 */
	public Model rebuild( Collection<URI> uris );

	/**
	 * Gets the top-level type for this instance
	 * @param instance
	 * @return
	 */
	public URI getTopLevelType( URI instance );

	/**
	 * Gets all the current structure info
	 * @return
	 */
	public Model getModel();
}
