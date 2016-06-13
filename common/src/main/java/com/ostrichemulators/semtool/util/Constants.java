/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.util;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * This class contains all of the constants referenced elsewhere in the code.
 */
public class Constants {

	private Constants() {
	}

	public static final String PDF = "PDF";
	public static final String PNG = "PNG";

	public static final int INITIAL_GRAPH_FONT_SIZE = 10;
	public static final String TBD = "TBD";
	public static final String NA = "n/a";

	public static final String LAYOUT = "LAYOUT";
	public static final String PERSPECTIVE = "PERSPECTIVE";
	public static final String PROCESS_CURRENT_DATE = "PROCESS_CURRENT_DATE";
	public static final String PROCESS_CURRENT_USER = "PROCESS_CURRENT_USER";
	public static final String BASE_FOLDER = "BaseFolder";

	public static final URI VERTEX_NAME = RDFS.LABEL;
	public static final URI VERTEX_TYPE = RDF.TYPE;
	public static final URI IN_EDGE_CNT = new URIImpl( "semoss://count.edge.in" );
	public static final URI OUT_EDGE_CNT = new URIImpl( "semoss://count.edge.out" );
	public static final URI EDGE_CNT = new URIImpl( "semoss://count.edge" );

	public static final URI ANYNODE = new URIImpl( "semoss://any" );
	public static final URI NONODE = new URIImpl( "semoss://none" );

	public static final URI EDGE_NAME = RDFS.LABEL;
	public static final URI EDGE_TYPE = RDF.TYPE;

	//Used by POIReader
	public static final String RELATION_URI_CONCATENATOR = "_x_"; //used in between the in node and out node for relation instance uris.
	public static final String RELATION_LABEL_CONCATENATOR = ":"; //used in between the in node and out node for relation instance uris.
	public static final String DEFAULT_NODE_CLASS = "Concept";
	public static final String CONTAINS = "Contains";

	public static final String SEMOSS_URI = "SEMOSS_URI";
	public static final String INTERNAL_NS = "semoss://internal/";

//	// layouts
//	public static final String FR = "Fruchterman-Reingold";
//	public static final String KK = "Kamada-Kawai";
//	public static final String SPRING = "Spring-Layout";
//	public static final String SPRING2 = "Spring-Layout2";
//	public static final String CIRCLE_LAYOUT = "Circle-Layout";
//	public static final String ISO = "ISO-Layout";
//	public static final String TREE_LAYOUT = "Tree-Layout";
//	public static final String RADIAL_TREE_LAYOUT = "Radial-Tree-Layout";
//	public static final String BALLOON_LAYOUT = "Balloon Layout";
	//public static final String LEGEND = "_LEGEND";

	public static final String DESCR = "DESCRIPTION";
	public static final String QUERY = "QUERY";

	public static final String ENGINE_NAME = "ENGINE";
	public static final String ENGINE_IMPL = "ENGINE_TYPE";
	public static final String DATASERIES = "dataSeries";

	public static final String ENTITY = "entity";

	public static final String BLANK_URL = "http://bornhere.com/noparent/blank/";

	public static final String DREAMER = "DREAMER";
	public static final String ONTOLOGY = "ONTOLOGY";

	public static final String SPARQL_QUERY_ENDPOINT = "SPARQL_QUERY_ENDPOINT";
	public static final String SPARQL_UPDATE_ENDPOINT = "SPARQL_UPDATE_ENDPOINT";

	//Load Sheet Export Panel
	public static final int MAX_EXPORTS = 9;

	public static final String OWLFILE = "OWL";
	public static final String URL_PARAM = "URL_PARAM";

	public static final String SMSS_LOCATION = "SMSS_LOCATION";
	public static final String SMSS_SEARCHPATH = "SMSS_SEARCHPATH";
	public static final String SMSS_VERSION_KEY = "VERSION";
	public static final String SMSS_RWSTORE_KEY = "RWStore";

	public static final String METADATA_SHEET_NAME = "Metadata";
	public static final String HELPURI_KEY = "HelpURI";
	public static final String LATESTRELEASE_KEY = "LatestRelease";
	public static final String EXPERIMENTALRELEASE_KEY = "ExperimentalRelease";
	public static final String LICENSEURI_KEY = "LicenseURI";
	public static final String BASEURI_KEY = "baseuri";
	public static final String PIN_KEY = "pinned";
	public static final String DEFAULTUI_KEY = "DefaultUI";

	public static final String CALC_INFERENCES_PREF = "calculateInferences";
	public static final String SEMEX_USE_LABELS_PREF = "semexUseLabels";
	public static final String INSIGHTKB = "insights";
}
