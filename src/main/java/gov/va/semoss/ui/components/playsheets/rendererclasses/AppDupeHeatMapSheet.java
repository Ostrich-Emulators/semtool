package gov.va.semoss.ui.components.playsheets.rendererclasses;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.DIHelper;

import java.util.List;

public class AppDupeHeatMapSheet extends DupeHeatMapSheet {

	private static final long serialVersionUID = -7745410541064513908L;

	public AppDupeHeatMapSheet() {
		super();
		setObjectTypes( "App1", "App2" );
	}

	private static final String PREFIXES = "PREFIX semoss: <http://semoss.org/ontologies/>\nPREFIX vcamp: <http://va.gov/ontologies/vcamp#>\n";

	@Override
	public void createData() {
		IEngine engine  = DIHelper.getInstance().getRdfEngine();
		DupeFunctions df = new DupeFunctions( engine );
		//get list of systems first
		updateProgressBar( "10%...Getting all systems for evaluation", 10 );
		String q = PREFIXES + "SELECT DISTINCT ?ApplicationModule WHERE { ?ApplicationModuleURI a semoss:ApplicationModule ; rdfs:label ?ApplicationModule . }";
		List<String> comparisonlist = df.createComparisonObjectList( q );

		String dataQuery = PREFIXES + "SELECT DISTINCT ?ApplicationModule ?DataObject  WHERE { ?ApplicationModuleURI  a semoss:ApplicationModule . ?ApplicationModuleURI semoss:provides ?DataObjectURI . ?DataObjectURI  a semoss:DataObject ; rdfs:label ?DataObject . ?ApplicationModuleURI rdfs:label ?ApplicationModule .}";
		updateProgressBar( "20%...Evaluating Data Object", 20 );
		Hashtable<String, Hashtable<String, Double>> dataObjectHash = df.compareObjectParameterScore( dataQuery, comparisonlist );
		dataObjectHash = processHashForJS( dataObjectHash );

		String procQuery = PREFIXES + "SELECT DISTINCT ?ApplicationModule ?BusinessProcess WHERE { ?ApplicationModuleURI a semoss:ApplicationModule . ?ApplicationModuleURI semoss:provides ?DataObject . ?DataObject a semoss:DataObject . ?Activity semoss:needs ?DataObject . ?Activity a semoss:Activity . ?BusinessProcessURI semoss:consists ?Activity . ?BusinessProcessURI a semoss:BusinessProcess ; rdfs:label ?BusinessProcess . ?ApplicationModuleURI rdfs:label ?ApplicationModule . }";
		updateProgressBar( "30%...Evaluating Business Process", 30 );
		Hashtable<String, Hashtable<String, Double>> procHash = df.compareObjectParameterScore( procQuery, comparisonlist );
		procHash = processHashForJS( procHash );

		String bluQuery = PREFIXES + "SELECT DISTINCT ?ApplicationModule ?BLU WHERE { ?ApplicationModuleURI a semoss:ApplicationModule . ?ApplicationModuleURI semoss:provides ?BLU_URI . ?BLU_URI  a semoss:BusinessLogicUnit ; rdfs:label ?BLU . ?ApplicationModuleURI rdfs:label ?ApplicationModule . }";
		updateProgressBar( "40%...Evaluating BLU", 40 );
		Hashtable<String, Hashtable<String, Double>> bluHash = df.compareObjectParameterScore( bluQuery, comparisonlist );
		bluHash = processHashForJS( bluHash );

		String taskQuery = PREFIXES + "SELECT DISTINCT  ?ApplicationModule ?Task WHERE { ?ApplicationModuleURI a semoss:ApplicationModule . ?ApplicationModuleURI semoss:needs ?TaskURI .  ?TaskURI a semoss:Task ; rdfs:label ?Task . ?ApplicationModuleURI rdfs:label ?ApplicationModule . }";
		updateProgressBar( "50%...Evaluating Task", 50 );
		Hashtable<String, Hashtable<String, Double>> taskHash = df.compareObjectParameterScore( taskQuery, comparisonlist );
		taskHash = processHashForJS( taskHash );

		String actQuery = PREFIXES + "SELECT DISTINCT ?ApplicationModule ?Activity WHERE { ?ApplicationModuleURI a semoss:ApplicationModule . ?ApplicationModuleURI semoss:needs ?Task . ?Task a semoss:Task . ?ActivityURI semoss:consists ?Task . ?ActivityURI a semoss:Activity ; rdfs:label ?Activity . ?ApplicationModuleURI rdfs:label ?ApplicationModule . }";
		updateProgressBar( "60%...Evaluating Activities", 60 );
		Hashtable<String, Hashtable<String, Double>> actHash = df.compareObjectParameterScore( actQuery, comparisonlist );
		actHash = processHashForJS( actHash );

		updateProgressBar( "80%...Creating Heat Map Visualization", 80 );
		paramDataHash.put( "Data_Objects_Supported", dataObjectHash );
		paramDataHash.put( "Processes_Supported", procHash );
		paramDataHash.put( "BLUs_Supported", bluHash );
		paramDataHash.put( "Tasks_Supported", taskHash );
		paramDataHash.put( "Direct_Activities_Supported", actHash );

		allHash.put( "title", "Application Duplication" );
		allHash.put( "xAxisTitle", "App1" );
		allHash.put( "yAxisTitle", "App2" );
		allHash.put( "value", "Score" );
		progressComplete( "100%...Visualization Complete" );
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}
}
