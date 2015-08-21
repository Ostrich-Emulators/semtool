/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightTab;
import gov.va.semoss.rdf.engine.api.WriteableParameterTab;
import gov.va.semoss.rdf.engine.api.WriteablePerspectiveTab;
import gov.va.semoss.security.User;
import gov.va.semoss.security.User.UserProperty;
import gov.va.semoss.security.RemoteUserImpl;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriSanitizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public abstract class WriteableInsightManagerImpl extends InsightManagerImpl
		implements WriteableInsightManager {

	private static final Logger log
			= Logger.getLogger( WriteableInsightManagerImpl.class );
	private boolean haschanges = false;
	private final UriSanitizer sanitizer = new DeterministicSanitizer();
	private final Collection<Statement> initialStatements = new ArrayList<>();
	private final User author;
	
	private final WriteablePerspectiveTabImpl wpt;
	private final WriteableInsightTabImpl wit;
	private final WriteableParameterTabImpl wprmt;

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

		wpt = new WriteablePerspectiveTabImpl( this );
		wit = new WriteableInsightTabImpl( this );
		wprmt = new WriteableParameterTabImpl( this );
		//Get current repository from the "InsightManagerImpl":
		//repo = im.getRepository();    
	}

	@Override
	public boolean hasCommittableChanges() {
		return haschanges;
	}

	@Override
	public void dispose() {
		RepositoryConnection rc = getRawConnection();
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
		RepositoryConnection rc = getRawConnection();

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
		RepositoryConnection rc = getRawConnection();
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
		RepositoryConnection rc = getRawConnection();

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
		RepositoryConnection rc = getRawConnection();
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
		RepositoryConnection rc = getRawConnection();

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
		haschanges = true;

		List<Insight> current = getInsights( p );
		insights.removeAll( current );
		if( insights.isEmpty() ){
			return;
		}
		
		wit.saveInsight( current, insights.get( 0 ), Arrays.asList( p ), new ArrayList<>() );
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
		RepositoryConnection rc = getRawConnection();

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
		RepositoryConnection rc = getRawConnection();

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
	 * Provides access to methods that persist changes to "Perspective" tab data.
	 *
	 * @return getWriteablePerspectiveTab -- (WriteablePerspectiveTab) Methods
	 * described above.
	 */
	@Override
	public WriteablePerspectiveTab getWriteablePerspectiveTab() {
		return wpt;
	}

	/**
	 * Provides access to methods that persist changes to "Insight" tab data.
	 *
	 * @return getWriteableInsightTab -- (WriteableInsightTab) Methods described
	 * above.
	 */
	@Override
	public WriteableInsightTab getWriteableInsightTab() {
		return wit;
	}

	/**
	 * Provides access to methods that persist changes to "Parameter" tab data.
	 *
	 * @return getWriteableParameterTab -- (WriteableParameterTab) Methods
	 * described above.
	 */
	@Override
	public WriteableParameterTab getWriteableParameterTab() {
		return wprmt;
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
		String userPrefName = author.getProperty(UserProperty.USER_FULLNAME );
		String userPrefEmail = author.getProperty( UserProperty.USER_EMAIL );
		userPrefEmail = ( !userPrefEmail.isEmpty() ? " <" + userPrefEmail + ">" : "" );
		String userPrefOrg = author.getProperty( UserProperty.USER_ORG );

		if ( !( userPrefName.isEmpty() || userPrefEmail.isEmpty() || userPrefOrg.isEmpty() ) ){
			if ( userPrefName.isEmpty() || userPrefOrg.isEmpty() ) {
				userInfo = userPrefName + userPrefEmail + " " + userPrefOrg;
			}
			else {
				userInfo = userPrefName + userPrefEmail + ", " + userPrefOrg;
			}
		}
		return userInfo;
	}

}//End WriteableInsightManager class.
