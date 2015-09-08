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
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.security.User;
import gov.va.semoss.security.User.UserProperty;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriSanitizer;

import info.aduna.iteration.Iterations;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.ntriples.NTriplesWriter;
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
	private static long lngUniqueIdentifier = System.currentTimeMillis();

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
		if ( removeOldData() ) {
			try {

				for ( Perspective p : perspectives ) {
					savePerspective( p );

					for ( Insight i : p.getInsights() ) {
						saveInsight( p, i );

						for ( Parameter a : i.getInsightParameters() ) {
							saveParameter( i, a );
						}
					}
				}
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}
	}

	private boolean removeOldData() {
		try ( FileWriter fw = new FileWriter( "/tmp/outtie-before.nt" ) ) {
			rc.export( new NTriplesWriter( fw ) );
		}
		catch ( Exception io ) {
			log.error( io, io );
		}

		try {
			// remove Perspectives, Insights, and Parameters, 
			// but also the things they depend on, like slots, constraints, indexes
			Set<Resource> idsToRemove = new HashSet<>();

			// remove statements that have these objects
			URI objectsToRemove[] = new URI[]{
				VAS.Perspective,
				VAS.InsightProperties
			};

			for ( URI obj : objectsToRemove ) {
				for ( Statement s : Iterations.asList( rc.getStatements( null, null, obj, true ) ) ) {
					idsToRemove.add( s.getSubject() );
				}
			}

			URI predsToRemove[] = new URI[]{
				SPIN.constraint,
				SPIN.body,
				OLO.slot,
				OLO.index,
				OLO.item,
				SPL.predicate,
				SP.text,
				SP.query,
				SP.Construct
			};
			for ( URI pred : predsToRemove ) {
				for ( Statement s : Iterations.asList( rc.getStatements( null, pred, null, true ) ) ) {
					idsToRemove.add( s.getSubject() );
				}
			}

			rc.begin();
			for ( Resource r : idsToRemove ) {
				rc.remove( r, null, null );
			}

			rc.commit();
		}
		catch ( RepositoryException e ) {
			try {
				log.error( e, e );
				rc.rollback();
			}
			catch ( Exception x ) {
				log.warn( x, x );
			}
			return false;
		}

		try ( FileWriter fw = new FileWriter( "/tmp/outtie-after.nt" ) ) {
			rc.export( new NTriplesWriter( fw ) );
		}
		catch ( Exception io ) {
			log.error( io, io );
		}

		return true;
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
		String clean = sanitizer.sanitize( ins.getLabel() );

		ValueFactory vf = rc.getValueFactory();
		URI newId = vf.createURI( VAS.NAMESPACE, clean );
		URI bodyId = vf.createURI( VAS.NAMESPACE, clean + "-" + ( new Date().getTime() ) );
		ins.setId( newId );
		try {
			rc.begin();
			rc.add( newId, RDF.TYPE, VAS.insight );
			rc.add( newId, RDFS.LABEL, vf.createLiteral( ins.getLabel() ) );
			rc.add( newId, UI.dataView, vf.createURI( "vas:", ins.getOutput() ) );

			rc.add( newId, DCTERMS.CREATED, vf.createLiteral( new Date() ) );
			rc.add( newId, DCTERMS.MODIFIED, vf.createLiteral( new Date() ) );
			rc.add( newId, DCTERMS.CREATOR,
					vf.createLiteral( userInfoFromToolPreferences( "" ) ) );
			rc.add( newId, SPIN.body, bodyId );

			String sparql = ins.getSparql();
			rc.add( bodyId, SP.text, vf.createLiteral( sparql ) );
			rc.add( bodyId, RDF.TYPE, SP.Select );

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
		return newId;
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
		p.setUri( perspectiveURI );
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
			rc.remove( p.getUri(), null, null );
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
			rc.remove( p.getUri(), RDFS.LABEL, null );
			rc.add( p.getUri(), RDFS.LABEL, vf.createLiteral( p.getLabel() ) );
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
	public void setInsights( Perspective p, List<Insight> insights ) {
		log.warn( "this function has not yet been implemented." );
		haschanges = true;
	}

	//We do not want to release the this object, because the connection will
	//be closed to the main database.--TKC, 16 Mar 2015.
	@Override
	public void release() {
//    dispose();
//    super.release();
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
	public boolean savePerspective( Perspective perspective ) {
		boolean boolReturnValue = false;
		lngUniqueIdentifier += 1;
		String strUniqueIdentifier = String.valueOf( lngUniqueIdentifier );
		ValueFactory insightVF = rc.getValueFactory();
		String perspectiveUriName = "perspective-" + strUniqueIdentifier;
		URI perspectiveURI = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, perspectiveUriName );
		//Be sure to set the new Perspective URI, because this Perspective object 
		//is returned by side-effect, and it's URI is used to create Insight slots:
		perspective.setUri( perspectiveURI );
		Date now = new Date();
		String creator = userInfoFromToolPreferences( "Created By Insight Manager, " + System.getProperty( "release.nameVersion", "VA SEMOSS" ) );
		//Make sure that embedded quotes and new-line characters can be persisted:
		//String label = Utility.legalizeStringForSparql(perspective.getLabel());
		//      String description = Utility.legalizeStringForSparql(perspective.getDescription());

		String query = "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
				+ "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
				+ "INSERT{ ?uri rdfs:label ?label . "
				+ "?uri dcterms:description ?description . "
				+ "?uri a vas:Perspective . "
				+ "?uri dcterms:created ?now . "
				+ "?uri dcterms:modified ?now . "
				+ "?uri dcterms:creator ?creator .} "
				+ "WHERE{}";
		try {
			rc.begin();
			Update uq = rc.prepareUpdate( QueryLanguage.SPARQL, query );
			uq.setBinding( "uri", perspectiveURI );
			uq.setBinding( "label", insightVF.createLiteral( perspective.getLabel() ) );
			uq.setBinding( "description", insightVF.createLiteral( perspective.getDescription() ) );
			uq.setBinding( "now", insightVF.createLiteral( now ) );
			uq.setBinding( "creator", insightVF.createLiteral( creator ) );
			uq.execute();
			rc.commit();
			boolReturnValue = true;
		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.warn( e, e );
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
	public boolean saveInsight( Perspective perspective, Insight insight ) {
		boolean boolReturnValue = false;
		lngUniqueIdentifier += 1;
		String strUniqueIdentifier = String.valueOf( lngUniqueIdentifier );
		ValueFactory insightVF = rc.getValueFactory();
		URI perspectiveURI = perspective.getUri();
		//Make sure that embedded quotes and new-line characters can be persisted:
		URI dataViewOutputURI = insightVF.createURI( "http://va.gov/ontologies/semoss#" + insight.getOutput() );
		String isLegacy = String.valueOf( insight.isLegacy() );
		//Make sure that embedded quotes and new-line characters can be persisted:
		String sparql = insight.getSparql().trim();
		String description = insight.getDescription().trim();
		String slotUriName = perspective.getUri().getLocalName() + "-slot-" + strUniqueIdentifier;
		URI slotURI = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, slotUriName );
		Literal order = insightVF.createLiteral( perspective.indexOf( insight ) );
		//Insights can only have only SELECT and CONSTRUCT queries:
		URI spinBodyTypeURI = ( sparql.toUpperCase().startsWith( "SELECT" )
				? SP.Select : SP.Construct );

		String spinBodyUriName
				= "insight-" + strUniqueIdentifier + "-" + spinBodyTypeURI.getLocalName();
		URI spinBodyURI = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, spinBodyUriName );

		String created = insight.getCreated();
		String modified = insight.getModified();

		String query = "PREFIX " + OLO.PREFIX + ": <" + OLO.NAMESPACE + "> "
				+ "PREFIX " + DCTERMS.PREFIX + ": <" + DCTERMS.NAMESPACE + "> "
				+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
				+ "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "PREFIX " + VAS.PREFIX + ": <" + VAS.NAMESPACE + "> "
				+ "PREFIX " + UI.PREFIX + ": <" + UI.NAMESPACE + "> "
				+ "INSERT{ ?perspectiveURI olo:slot ?slotURI . "
				+ "?slotURI olo:item ?insightURI . "
				+ "?slotURI olo:index ?order . "
				+ "?insightURI rdfs:label ?question . "
				+ "?insightURI rdfs:subclassof vas:InsightProperties . "
				+ "?insightURI ui:dataView ?dataViewOutputURI . "
				+ "?insightURI vas:isLegacy ?isLegacy . "
				+ "?insightURI spin:body ?spinBodyURI . "
				+ "?spinBodyURI rdf:type ?spinBodyTypeURI . "
				+ "?spinBodyURI sp:text ?sparql . "
				+ "?insightURI dcterms:description ?description . "
				+ "?insightURI dcterms:creator ?creator . "
				+ "?insightURI dcterms:created ?created . "
				+ "?insightURI dcterms:modified ?modified . } "
				+ "WHERE {}";

		try {
			rc.begin();
			Update uq = rc.prepareUpdate( QueryLanguage.SPARQL, query );
			uq.setBinding( "perspectiveURI", perspectiveURI );
			uq.setBinding( "slotURI", slotURI );
			uq.setBinding( "insightURI", insight.getId() );
			uq.setBinding( "order", order );
			uq.setBinding( "question", insightVF.createLiteral( insight.getLabel() ) );
			uq.setBinding( "dataViewOutputURI", dataViewOutputURI );
			uq.setBinding( "isLegacy", insightVF.createLiteral( isLegacy ) );
			uq.setBinding( "spinBodyURI", spinBodyURI );
			uq.setBinding( "spinBodyTypeURI", spinBodyTypeURI );
			uq.setBinding( "sparql", insightVF.createLiteral( sparql ) );
			uq.setBinding( "description", insightVF.createLiteral( description ) );
			uq.setBinding( "creator", insightVF.createLiteral( insight.getCreator() ) );
			uq.setBinding( "created", insightVF.createLiteral( created ) );
			uq.setBinding( "modified", insightVF.createLiteral( modified ) );

			uq.execute();
			rc.commit();
			boolReturnValue = true;
		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.warn( e, e );
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
	public boolean saveParameter( Insight insight, Parameter parameter ) {
		boolean boolReturnValue = false;
		lngUniqueIdentifier += 1;
		String strUniqueIdentifier = String.valueOf( lngUniqueIdentifier );
		ValueFactory insightVF = rc.getValueFactory();
		URI insightURI = insight.getId();
		//We are rebuilding the Constraint and other URIs here, because the designers of 
		//VA_MainDB, v20, decided to reuse Parameters, and we discourage that. No objects 
		//on the tree-view should be reused. They all should be editable as unique items:
		String constraintUriName = "constraint-" + strUniqueIdentifier;
		URI constraintURI = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, constraintUriName );
		String valueTypeUriName = parameter.getParameterType();
		URI valueTypeURI = insightVF.createURI( valueTypeUriName );
		String predicateUriName = "predicate-" + strUniqueIdentifier;
		URI predicateURI = insightVF.createURI( ARG.NAMESPACE + predicateUriName );
		String queryUriName = "query-" + strUniqueIdentifier;
		URI queryURI = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS, queryUriName );

		String query = "PREFIX " + SPIN.PREFIX + ": <" + SPIN.NAMESPACE + "> "
				+ "PREFIX " + SPL.PREFIX + ": <" + SPL.NAMESPACE + "> "
				+ "PREFIX " + SP.PREFIX + ": <" + SP.NAMESPACE + "> "
				+ "INSERT{ ?insightURI spin:constraint ?constraintURI ."
				+ "?constraintURI rdfs:label ?label . "
				+ "?constraintURI spl:valueType ?valueTypeURI . "
				+ "?constraintURI spl:predicate ?predicateURI . "
				+ "?predicateURI rdfs:label ?variable . "
				+ "?constraintURI sp:query ?queryURI . "
				+ "?queryURI sp:text ?defaultQuery .} "
				+ "WHERE{}";

		try {

			rc.begin();
			Update uq = rc.prepareUpdate( QueryLanguage.SPARQL, query );
			uq.setBinding( "label", insightVF.createLiteral( parameter.getLabel() ) );
			uq.setBinding( "variable", insightVF.createLiteral( parameter.getVariable() ) );
			uq.setBinding( "defaultQuery", insightVF.createLiteral( parameter.getDefaultQuery() ) );
			uq.setBinding( "insightURI", insightURI );
			uq.setBinding( "constraintURI", constraintURI );
			uq.setBinding( "predicateURI", predicateURI );
			uq.setBinding( "queryURI", queryURI );
			uq.setBinding( "valueTypeURI", valueTypeURI );
			uq.execute();
			rc.commit();
			boolReturnValue = true;
		}
		catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
			log.warn( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ee ) {
				log.warn( ee, ee );
			}
		}
		return boolReturnValue;
	}

}//End WriteableInsightManager class.
