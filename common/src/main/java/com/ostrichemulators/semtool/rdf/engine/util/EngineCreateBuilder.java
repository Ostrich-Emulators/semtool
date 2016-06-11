/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.impl.AbstractSesameEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.SesameEngine;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class EngineCreateBuilder {

	private URI defaultBaseUri;
	private ReificationStyle reificationModel;
	private File questions;
	private final File topdir;
	private boolean defaultBaseOverridesFiles;
	private boolean stageInMemory;
	private boolean calcInfers;
	private boolean dometamodel;
	private Class<? extends AbstractSesameEngine> impl = SesameEngine.class;
	private final String engine;
	private final List<File> toload = new ArrayList<>();
	private final List<URL> vocabularies = new ArrayList<>();

	public EngineCreateBuilder( File parentdir, String engineName ) {
		topdir = parentdir;
		engine = engineName;
	}

	public EngineCreateBuilder setDefaultBaseUri( URI base, boolean override ) {
		defaultBaseUri = base;
		defaultBaseOverridesFiles = override;
		return this;
	}

	public EngineCreateBuilder setEngineImpl( Class<? extends AbstractSesameEngine> klass ) {
		impl = klass;
		return this;
	}

	public EngineCreateBuilder setReificationModel( ReificationStyle model ) {
		reificationModel = model;
		return this;
	}

	public EngineCreateBuilder setInsightsFile( File questionsmodel ) {
		questions = questionsmodel;
		return this;
	}

	public EngineCreateBuilder setInsightsFile( String questionsmodel ) {
		questions = ( null == questionsmodel || questionsmodel.isEmpty()
				? null : new File( questionsmodel ) );
		return this;
	}

	public EngineCreateBuilder setBooleans( boolean inmem, boolean infers, boolean metamodel ) {
		stageInMemory = inmem;
		calcInfers = infers;
		dometamodel = metamodel;
		return this;
	}

	public EngineCreateBuilder addFile( File f ) {
		toload.add( f );
		return this;
	}

	public EngineCreateBuilder setFiles( Collection<File> f ) {
		toload.clear();
		toload.addAll( f );
		return this;
	}

	public List<File> getFiles() {
		return new ArrayList<>( toload );
	}

	public EngineCreateBuilder addVocabulary( URL f ) {
		vocabularies.add( f );
		return this;
	}

	public EngineCreateBuilder setVocabularies( Collection<URL> f ) {
		vocabularies.clear();
		vocabularies.addAll( f );
		return this;
	}

	public List<URL> getVocabularies() {
		return new ArrayList<>( vocabularies );
	}

	public URI getDefaultBaseUri() {
		return defaultBaseUri;
	}

	public boolean isDefaultBaseOverridesFiles() {
		return defaultBaseOverridesFiles;
	}

	public ReificationStyle getReificationModel() {
		return reificationModel;
	}

	public File getQuestions() {
		return questions;
	}

	public boolean isStageInMemory() {
		return stageInMemory;
	}

	public boolean isCalcInfers() {
		return calcInfers;
	}

	public boolean isDoMetamodel() {
		return dometamodel;
	}

	public String getEngineName() {
		return engine;
	}

	public File getEngineDir() {
		return topdir;
	}

	public Class<? extends AbstractSesameEngine> getEngineImpl() {
		return impl;
	}
}
