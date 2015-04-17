/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightTab;
import gov.va.semoss.rdf.engine.api.WriteableParameterTab;
import gov.va.semoss.rdf.engine.api.WriteablePerspectiveTab;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.UriSanitizer;
import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
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
  private final Pattern pattern = Pattern.compile( "^(\\w+)(.*)$" );
  
  private WriteablePerspectiveTabImpl wpt;
  private WriteableInsightTabImpl wit;
  private WriteableParameterTabImpl wprmt;
  
  public WriteableInsightManagerImpl( InsightManager im ) {
    super( new SailRepository( new ForwardChainingRDFSInferencer( new MemoryStore() ) ) );

    try {
      initialStatements.addAll( im.getStatements() );
      getRawConnection().add( initialStatements );
    }
    catch ( Exception re ) {
      log.error( re, re );
    }
    
    wpt = new WriteablePerspectiveTabImpl(this);
    wit = new WriteableInsightTabImpl(this);
    wprmt = new WriteableParameterTabImpl(this);
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
    throw new UnsupportedOperationException( "Not supported yet." );
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
    RepositoryConnection rc = getRawConnection();

    try {
      rc.begin();
      rc.remove(p.getUri(), VAS.insight, null );
      for ( Insight i : insights ) {
        rc.add(p.getUri(), VAS.insight, i.getId() );
      }
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
  
  /**   Provides access to methods that persist changes to "Perspective" tab data.
   * 
   * @return getWriteablePerspectiveTab -- (WriteablePerspectiveTab)
   *    Methods described above.
   */
  @Override
  public WriteablePerspectiveTab getWriteablePerspectiveTab(){
	  return wpt;
  }
  
  /**   Provides access to methods that persist changes to "Insight" tab data.
   * 
   * @return getWriteableInsightTab -- (WriteableInsightTab)
   *    Methods described above.
   */
  @Override
  public WriteableInsightTab getWriteableInsightTab(){
	  return wit;
  }
 
  /**   Provides access to methods that persist changes to "Parameter" tab data.
   * 
   * @return getWriteableParameterTab -- (WriteableParameterTab)
   *    Methods described above.
   */
  @Override
  public WriteableParameterTab getWriteableParameterTab(){
	  return wprmt;
  }
    
}//End WriteableInsightManager class.
