/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.api;

import java.util.Collection;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;

import java.util.List;

/**
 *
 * @author ryan
 */
public interface InsightManager {

	public Repository getRepository();

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
	 * Gets all Parameter objects under the passed-in Insight URI.
	 *
	 * @param insightURI -- (URI) An Insight URI.
	 *
	 * @return -- (Collection<Parameter>) Described above.
	 */
	public Collection<Parameter> getParameters( Insight insight );

	/**
	 * Gets all insight URIs for a given perspective, in order
	 *
	 * @param perspective
	 *
	 * @return
	 */
	public List<Insight> getInsights( Perspective perspective );

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
	 * Returns a collection of data about the playsheets used to render Insights.
	 *
	 * @return -- (Collection<PlaySheet>) Described above.
	 */
	public Collection<PlaySheet> getPlaySheets();

	/**
	 * Gets the raw statements for the insights
	 *
	 * @return all the statements that together comprise the Insight data
	 * @throws RepositoryException
	 */
	public Collection<Statement> getStatements() throws RepositoryException;

	/**
	 * Gets the generic perspective for a particular IEngine
	 *
	 * @param eng the engine
	 * @return a fully-instantiated perspective with the "Generic-Perspective"
	 * Insights
	 */
	public Perspective getSystemPerspective( IEngine eng );

	/**
	 * Releases any resources needed while this class is running. In general, this
	 * should only be called by {@link IEngine#closeDB() }
	 */
	public void release();
}
