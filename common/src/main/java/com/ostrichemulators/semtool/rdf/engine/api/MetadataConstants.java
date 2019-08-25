/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;

/**
 *
 * @author ryan
 */
public class MetadataConstants {

	public static final IRI DCT_DESC = DCTERMS.DESCRIPTION;
	public static final IRI DCT_CONTRIB = DCTERMS.CONTRIBUTOR;
	public static final IRI DCT_CREATOR = DCTERMS.CREATOR;
	public static final IRI DCT_PUBLISHER = DCTERMS.PUBLISHER;
	public static final IRI OWLIRI = OWL.VERSIONIRI;

	public static final String VOID_PREFIX = "void";
	public static final String VOID_NS = "http://rdfs.org/ns/void#";
	public static final IRI VOID_SUBSET = SimpleValueFactory.getInstance().createIRI( VOID_NS, "subset" );
	public static final IRI VOID_DS = SimpleValueFactory.getInstance().createIRI( VOID_NS, "Dataset" );

	public static final IRI DCT_CREATED = DCTERMS.CREATED;
	public static final IRI DCT_MODIFIED = DCTERMS.MODIFIED;
}
