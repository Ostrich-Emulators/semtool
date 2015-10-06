/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.api;

import java.util.Collection;

import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;

/**
 *
 * @author ryan
 */
public interface InsightManager {

	public void setInsightNamespace( String ns );

	public String getInsightNamespace();

	/**
	 * Adds the given perspectives to this IM's data tree
	 *
	 * @param newdata
	 * @param clearfirst
	 */
	public void addAll( Collection<Perspective> newdata, boolean clearfirst );

	/**
	 * Gets all perspectives
	 *
	 * @return
	 */
	public Collection<Perspective> getPerspectives();

	/**
	 * Retrieves the given perspective from the datastore
	 *
	 * @param id
	 * @return
	 * @throws IllegalArgumentException if a perspective with the given id is not
	 * found
	 */
	public Perspective getPerspective( URI id );

	/**
	 * Retrieves the given insight from the datastore
	 *
	 * @param id
	 * @return
	 * @throws IllegalArgumentException if an insight with the given id is not
	 * found
	 */
	public Insight getInsight( URI id );

	/**
	 * Adds a completely-new Insights
	 *
	 * @param ins the insight to add
	 *
	 * @return the URI of the new insight
	 */
	public URI add( Insight ins );

	public void remove( Insight ins );

	public void update( Insight ins );

	/**
	 * Adds a completely-new Perspective
	 *
	 * @param p the perspective to add
	 *
	 * @return the URI of the new perspective
	 */
	public URI add( Perspective p );

	public void remove( Perspective p );

	public void update( Perspective p );

	/**
	 * Adds the given insight to the given perspective at the given position
	 *
	 * @param p the perspective
	 * @param i the insight to add. This insight must already exist in the db
	 * (created with {@link #add(gov.va.semoss.om.Insight) }
	 * @param pos where to add the insight. Numbers &lt; 0 will prepend the
	 * insight
	 */
	public void addInsight( Perspective p, Insight i, int pos );

	/**
	 * Gets the generic perspective for a particular IEngine
	 *
	 * @param eng the engine
	 * @return a fully-instantiated perspective with the "Generic-Perspective"
	 * Insights
	 */
	public Perspective getSystemPerspective( IEngine eng );
}
