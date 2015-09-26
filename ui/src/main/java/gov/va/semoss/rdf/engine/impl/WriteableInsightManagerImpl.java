/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.model.vocabulary.ARG;
import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.user.User;
import gov.va.semoss.user.User.UserProperty;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.UriSanitizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public abstract class WriteableInsightManagerImpl extends InsightManagerImpl
		implements WriteableInsightManager {

	private static final Logger log = Logger.getLogger( WriteableInsightManagerImpl.class );
	private boolean haschanges = false;
	private final UriSanitizer sanitizer = new DeterministicSanitizer();
	private final Collection<Statement> initialStatements = new ArrayList<>();
	private final User author;
	private final RepositoryConnection rc;

	public WriteableInsightManagerImpl( InsightManager im, User auth ) {
		super( new SailRepository( new ForwardChainingRDFSInferencer( new MemoryStore() ) ) );
		author = auth;
		try {
			initialStatements.addAll( im.getStatements() );
			getRawConnection().add( initialStatements );
		}
		catch ( Exception re ) {
			log.error( re, re );
		}
		rc = getRawConnection();
	}

	@Override
	public void setData( List<Perspective> perspectives ) {
		try {
			rc.begin();
			removeOldData();

			ValueFactory vf = rc.getValueFactory();
			int idx = 0;
			for ( Perspective p : perspectives ) {
				rc.add( InsightManagerImpl.getStatements( p, author ) );
				rc.add( p.getId(), OLO.index, vf.createLiteral( ++idx ) );
			}

			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception x ) {
				log.warn( x, x );
			}
		}
	}

	private void removeOldData() throws RepositoryException {
		// basically, anything in the insights or arg namespaces is insight data
		Set<String> namespacesToRemove
				= new HashSet<>( Arrays.asList( MetadataConstants.VA_INSIGHTS_NS, ARG.NAMESPACE ) );
		RepositoryResult<Statement> it = rc.getStatements( null, null, null, false );
		List<Statement> removers = new ArrayList<>();
		while ( it.hasNext() ) {
			Statement s = it.next();
			Resource rsr = s.getSubject();
			if ( rsr instanceof URI ) {
				if ( namespacesToRemove.contains( URI.class.cast( rsr ).getNamespace() ) ) {
					removers.add( s );
				}
			}
		}

		rc.remove( removers );
	}

	@Override
	public boolean hasCommittableChanges() {
		return haschanges;
	}

	@Override
	public void dispose() {
		try {
			rc.begin();
			rc.clear();
			rc.add( initialStatements );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
			try {
				rc.rollback();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}
	}

	@Override
	public URI add( Insight ins ) {
		haschanges = true;
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
			
		ins.setId( urib.uniqueUri() );

		try {
			rc.begin();
			rc.add( InsightManagerImpl.getInsightStatements( ins, new ValueFactoryImpl(),
					urib, author ) );
			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		
		return ins.getId();
	}

	@Override
	public void remove( Insight ins ) {
		haschanges = true;
		try {
			rc.begin();
			rc.clear( ins.getId(), null, null );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
			try {
				rc.rollback();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}
	}

	@Override
	public void update( Insight ins ) {
		haschanges = true;
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public URI add( Perspective p ) {
		haschanges = true;
		String clean = sanitizer.sanitize( p.getLabel() );

		ValueFactory vf = rc.getValueFactory();
		URI perspectiveURI = vf.createURI( VAS.NAMESPACE, clean );
		p.setId( perspectiveURI );
		try {
			rc.begin();
			rc.add( perspectiveURI, RDF.TYPE, VAS.Perspective );
			rc.add( perspectiveURI, RDFS.LABEL, vf.createLiteral( p.getLabel() ) );
			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		return perspectiveURI;
	}

	@Override
	public void remove( Perspective p ) {
		haschanges = true;
		try {
			rc.begin();
			rc.remove( p.getId(), null, null );
			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
	}

	@Override
	public void update( Perspective p ) {
		haschanges = true;

		ValueFactory vf = rc.getValueFactory();
		try {
			rc.begin();
			rc.remove( p.getId(), null, null );

			Collection<Statement> stmts = getPerspectiveStatements( p, vf,
					UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS ),
					author );

			rc.add( stmts );

			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
	}

	@Override
	public void setInsights( Perspective p, List<Insight> insights ) throws RepositoryException {
		ValueFactory vf = rc.getValueFactory();
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );

		rc.add( getOrderingStatements( p, insights, vf, urib ) );
		haschanges = true;
	}

	/**
	 * Release heavy-weight database objects.
	 */
	@Override
	public void release() {
		dispose();
		super.release();
	}

	@Override
	public void addRawStatements( Collection<Statement> stmts ) throws RepositoryException {
		haschanges = true;

		try {
			rc.begin();
			rc.add( stmts );
			rc.commit();
		}
		catch ( RepositoryException re ) {
			try {
				rc.rollback();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
			throw re;
		}
	}

	@Override
	public void clear() {
		try {
			rc.begin();
			rc.clear();
			rc.commit();
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
			try {
				rc.rollback();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}
	}

	/**
	 * Extracts from V-CAMP/SEMOSS preferences the user's name, email, and
	 * organization, and returns a string of user-info for saving with Insights,
	 * based upon these. If these preferences have not been set, then the passe-in
	 * value is returned.
	 *
	 * @param strOldUserInfo -- (String) User-info that has been displayed from a
	 * database fetch.
	 *
	 * @return userInfoFromToolPreferences -- (String) Described above.
	 */
	@Override
	public String userInfoFromToolPreferences( String strOldUserInfo ) {
		String userInfo = strOldUserInfo;
		String userPrefName = author.getProperty( UserProperty.USER_FULLNAME );
		String userPrefEmail = author.getProperty( UserProperty.USER_EMAIL );
		userPrefEmail = ( !userPrefEmail.isEmpty() ? " <" + userPrefEmail + ">" : "" );
		String userPrefOrg = author.getProperty( UserProperty.USER_ORG );

		if ( !( userPrefName.isEmpty() && userPrefEmail.isEmpty() && userPrefOrg.isEmpty() ) ) {
			if ( userPrefName.isEmpty() || userPrefOrg.isEmpty() ) {
				userInfo = userPrefName + userPrefEmail + " " + userPrefOrg;
			}
			else {
				userInfo = userPrefName + userPrefEmail + ", " + userPrefOrg;
			}
		}
		return userInfo;
	}
	//---------------------------------------------------------------------------------------------------------
//  D e l e t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
//---------------------------------------------------------------------------------------------------------

	/**
	 * Deletes all Parameters from all Insights in the database.
	 *
	 * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded.
	 */
	@Override
	public boolean deleteAllParameters() {
		boolean boolReturnValue = false;

		String query_1 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
				+ "DELETE{ ?query ?p ?o .} "
				+ "WHERE{ ?parameter sp:query ?query . "
				+ "?insight spin:constraint ?parameter . "
				+ "?query ?p ?o .}";

		String query_2 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
				+ "DELETE{ ?predicate ?p ?o .} "
				+ "WHERE{ ?parameter spl:predicate ?predicate . "
				+ "?insight spin:constraint ?parameter . "
				+ "?predicate ?p ?o .}";

		String query_3 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "DELETE{ ?parameter ?p ?o .} "
				+ "WHERE{ ?insight spin:constraint ?parameter . "
				+ "?parameter ?p ?o .}";

		String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "DELETE{ ?insight spin:constraint ?parameter .} "
				+ "WHERE{ ?insight spin:constraint ?parameter .}";

		try {
			rc.begin();

			Update uq_1 = rc.prepareUpdate( QueryLanguage.SPARQL, query_1 );
			Update uq_2 = rc.prepareUpdate( QueryLanguage.SPARQL, query_2 );
			Update uq_3 = rc.prepareUpdate( QueryLanguage.SPARQL, query_3 );
			Update uq_4 = rc.prepareUpdate( QueryLanguage.SPARQL, query_4 );
			uq_1.execute();
			uq_2.execute();
			uq_3.execute();
			uq_4.execute();

			rc.commit();
			boolReturnValue = true;

		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		return boolReturnValue;
	}

	/**
	 * Deletes all Insights from all Perspectives in the database.
	 *
	 * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded.
	 */
	@Override
	public boolean deleteAllInsights() {
		boolean boolReturnValue = false;

		String query_1 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
				+ "DELETE{ ?perspective olo:slot ?slot .} "
				+ "WHERE{ ?perspective olo:slot ?slot .}";

		String query_2 = "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
				+ "DELETE{ ?query ?p ?o .} "
				+ "WHERE{ ?constraint sp:query ?query . "
				+ "?query ?p ?o .}";

		String query_3 = "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
				+ "DELETE{ ?predicate ?p ?o .} "
				+ "WHERE{ ?constraint spl:predicate ?predicate . "
				+ "?predicate ?p ?o .} ";

		String query_4 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "DELETE{ ?constraint ?p ?o .} "
				+ "WHERE{ ?insight spin:constraint ?constraint . "
				+ "?constraint ?p ?o .} ";

		String query_5 = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "DELETE{ ?body ?p ?o .} "
				+ "WHERE{ ?insight spin:body ?body . "
				+ "?body ?p ?o .} ";

		String query_6 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
				+ "DELETE{ ?insight ?p ?o .} "
				+ "WHERE{ ?slot olo:item ?insight . "
				+ "?insight ?p ?o .} ";

		String query_7 = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
				+ "DELETE{ ?slot ?p ?o .} "
				+ "WHERE{ ?slot olo:item ?insight . "
				+ "?slot ?p ?o .} ";

		try {
			rc.begin();
			Update uq_1 = rc.prepareUpdate( QueryLanguage.SPARQL, query_1 );
			Update uq_2 = rc.prepareUpdate( QueryLanguage.SPARQL, query_2 );
			Update uq_3 = rc.prepareUpdate( QueryLanguage.SPARQL, query_3 );
			Update uq_4 = rc.prepareUpdate( QueryLanguage.SPARQL, query_4 );
			Update uq_5 = rc.prepareUpdate( QueryLanguage.SPARQL, query_5 );
			Update uq_6 = rc.prepareUpdate( QueryLanguage.SPARQL, query_6 );
			Update uq_7 = rc.prepareUpdate( QueryLanguage.SPARQL, query_7 );
			uq_1.execute();
			uq_2.execute();
			uq_3.execute();
			uq_4.execute();
			uq_5.execute();
			uq_6.execute();
			uq_7.execute();

			rc.commit();
			boolReturnValue = true;

		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		return boolReturnValue;
	}

	/**
	 * Deletes all Perspectives from the database.
	 *
	 * @return deleteAllPerspectives -- (boolean) Whether the deletion succeeded.
	 */
	@Override
	public boolean deleteAllPerspectives() {
		boolean boolReturnValue = false;

		String query_1 = "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
				+ "DELETE{ ?perspective ?p ?o .} "
				+ "WHERE{ ?perspective a vas:Perspective . "
				+ "?perspective ?p ?o . }";

		String query_2 = "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
				+ "DELETE{ ?argument ?p ?o .} "
				+ "WHERE{ ?argument a spl:Argument . "
				+ "?argument ?p ?o .} ";

		try {
			rc.begin();
			Update uq_1 = rc.prepareUpdate( QueryLanguage.SPARQL, query_1 );
			Update uq_2 = rc.prepareUpdate( QueryLanguage.SPARQL, query_2 );

			uq_1.execute();
			uq_2.execute();

			rc.commit();
			boolReturnValue = true;
		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		return boolReturnValue;
	}

//---------------------------------------------------------------------------------------------------------
// I n s e r t i o n   o f   P e r s p e c t i v e s ,   I n s i g h t s ,   a n d   P a r a m e t e r s
//---------------------------------------------------------------------------------------------------------
	/**
	 * Saves the passed-in Perspective's Title and Description into the
	 * triple-store on disk.
	 *
	 * NOTE: The Perspective parameter is returned by side-effect, because its URI
	 * is used to create Insight slots.
	 *
	 * @param perspective -- (Perspective) The Perspective to persist.
	 *
	 * @return savePerspective -- (boolean) Whether the save to disk succeeded.
	 */
	@Override
	public void savePerspective( Perspective perspective ) throws RepositoryException {
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
		ValueFactory vf = rc.getValueFactory();
		URI pid = urib.build( perspective.getLabel() );
		perspective.setId( pid );
		rc.add( getPerspectiveStatements( perspective, vf, urib, author ) );

	}

	/**
	 * Saves the various Insight fields to the triple-store on disk.
	 *
	 * @param perspective -- (Perspective) A Perspective, extracted from the
	 * tree-view of the Insight Manager.
	 *
	 * @param insight -- (Insight) An Insight, belonging to the above Perspective.
	 *
	 * @return saveInsight -- (boolean) Whether the save succeeded.
	 */
	@Override
	public void saveInsight( Perspective perspective, Insight insight ) throws RepositoryException {
		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
		ValueFactory vf = rc.getValueFactory();
		URI iid = urib.build( insight.getLabel() );
		insight.setId( iid );

		rc.add( getInsightStatements( insight, vf, urib, author ) );
	}

	/**
	 * Saves the various Parameter fields to the triple-store on disk.
	 *
	 * @param insight -- (Insight) An Insight, extracted from the tree-view of the
	 * Insight Manager.
	 *
	 * @param parameter -- (Parameter) A Parameter, belonging to the above
	 * Insight.
	 *
	 * @return saveParameter -- (boolean) Whether the save succeeded.
	 */
	@Override
	public void saveParameter( Perspective perspective, Insight insight,
			Parameter parameter ) throws RepositoryException {

		UriBuilder urib = UriBuilder.getBuilder( MetadataConstants.VA_INSIGHTS_NS );
		ValueFactory vf = rc.getValueFactory();
		URI pid = urib.build( insight.getLabel() + "-" + parameter.getLabel() );
		parameter.setId( pid );

		final String pianame = perspective.getLabel() + "-"
				+ insight.getLabel() + "-" + parameter.getLabel();

		URI predicateUri = urib.build( pianame + "-pred" );
		URI queryUri = urib.build( pianame + "-query" );

		rc.add( getParameterStatements( parameter, predicateUri, queryUri, vf, urib,
				author ) );
		rc.add( getConstraintStatements( insight, Arrays.asList( parameter ) ) );
	}
}//End WriteableInsightManager class.
