/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;

/**
 *
 * @author ryan
 */
public class MetadataConstants {

	public static final URI DCT_DESC = DCTERMS.DESCRIPTION;
	public static final URI DCT_CONTRIB = DCTERMS.CONTRIBUTOR;
	public static final URI DCT_CREATOR = DCTERMS.CREATOR;
	public static final URI DCT_PUBLISHER = DCTERMS.PUBLISHER;
	public static final URI OWLIRI = OWL.VERSIONIRI;

	public static final String VOID_PREFIX = "void";
	public static final String VOID_NS = "http://rdfs.org/ns/void#";
	public static final URI VOID_SUBSET = new URIImpl( VOID_NS + "subset" );
	public static final URI VOID_DS = new URIImpl( VOID_NS + "Dataset" );

	public static final URI DCT_CREATED = DCTERMS.CREATED;
	public static final URI DCT_MODIFIED = DCTERMS.MODIFIED;
}
