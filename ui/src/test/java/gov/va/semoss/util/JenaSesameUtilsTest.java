package gov.va.semoss.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.openrdf.model.impl.URIImpl;

import org.openrdf.model.URI;

public class JenaSesameUtilsTest {

	private static RDFNode jenaResource;
	
	private static  Property jenaPredicate;
	
	private static Resource jenaSubject;
	
	private static org.openrdf.model.Resource sesameResource;
	
	private static URI sesamePredicate;
	
	private static final org.openrdf.model.Resource sesameSubject = new org.openrdf.model.impl.BNodeImpl("Mantech");
	
	private static final Model testingModel = ModelFactory.createDefaultModel();
	
	@Before
	public void setUp() throws Exception {
		jenaResource = testingModel.createResource("class:Company");
		jenaSubject = testingModel.createResource("thing:Mantech");
		jenaPredicate = new PropertyImpl("rdf:is_a");
	}
	
	@Test
	public void testAsSesameResource() {
		org.openrdf.model.Resource sResource = JenaSesameUtils.asSesameResource(jenaSubject);
		String s = sResource.stringValue();
		assertEquals(s, "thing:Mantech");
	}
	
	@Test
	public void testAsSesameStatement(){
		Statement sStatement = getJenaStatement();
		org.openrdf.model.Statement tStatement = JenaSesameUtils.asSesameStatement(sStatement);
		String object = tStatement.getObject().stringValue();
		String subject = tStatement.getSubject().stringValue();
		String predicate = tStatement.getPredicate().stringValue();
		assertEquals(object, "class:Company");
		assertEquals(subject, "thing:Mantech");
		assertEquals(predicate, "rdf:is_a");
	}

	@Test
	public void testAsSesameURI(){
		org.openrdf.model.URI sURI = JenaSesameUtils.asSesameURI(jenaPredicate);
		assertEquals(sURI.stringValue(), "rdf:is_a");
	}
	
	
	@Test
	public void testAsJenaStatement(){
		
		
	}
	
	private Statement getJenaStatement(){
		Statement stmt = new StatementImpl(jenaSubject, jenaPredicate, jenaResource);
		return stmt;
	}
	
	private org.openrdf.model.Statement getSesameStatement(){
		org.openrdf.model.Statement stmt = 
				new org.openrdf.model.impl.StatementImpl(sesameSubject, sesamePredicate , sesameResource);
		return stmt;
	}

}
