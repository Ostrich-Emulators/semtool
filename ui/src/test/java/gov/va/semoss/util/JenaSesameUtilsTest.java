package gov.va.semoss.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.URI;

public class JenaSesameUtilsTest {

	private static final Resource jenaResource = new ResourceImpl("Mantech");
	
	private static final  Property jenaPredicate = new PropertyImpl("is_a");
	
	private static final RDFNode jenaSubject = new ResourceImpl("Company");
	
	private static final org.openrdf.model.Resource sesameResource = new org.openrdf.model.impl.BNodeImpl("Mantech");
	
	private static URI sesamePredicate;
	
	private static final org.openrdf.model.Resource sesameSubject = new org.openrdf.model.impl.BNodeImpl("Mantech");
	
	@Before
	public void setUp() throws Exception {
		sesamePredicate = new URIImpl("rdf:type");
		PropertyImpl prop = new PropertyImpl("name");
		jenaResource.addProperty(prop, "Mantech");
	}
	
	@Test
	public void testAsSesameResource() {
		org.openrdf.model.Resource sResource = JenaSesameUtils.asSesameResource(jenaResource);
		String value = sResource.stringValue();
		assertEquals(value, "Mantech");
	}
	
	@Test
	public void testAsSesameStatement(){
		org.openrdf.model.Statement sStatement = JenaSesameUtils.asSesameStatement(this.getJenaStatement());
		assertEquals(sStatement.getObject().stringValue(), "Company");
		assertEquals(sStatement.getSubject().stringValue(), "Mantech");
	}
	
	@Test
	public void testAsJenaStatement(){
		
		
	}
	
	private Statement getJenaStatement(){
		Statement stmt = new StatementImpl(jenaResource, jenaPredicate, jenaSubject);
		return stmt;
	}
	
	private org.openrdf.model.Statement getSesameStatement(){
		org.openrdf.model.Statement stmt = 
				new org.openrdf.model.impl.StatementImpl(sesameResource, sesamePredicate ,sesameSubject);
		return stmt;
	}

}
