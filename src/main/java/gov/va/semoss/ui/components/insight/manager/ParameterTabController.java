package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.util.Utility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Tooltip;

public class ParameterTabController extends InsightManagerController {
	private InsightManagerController imc;

	/**   Constructor: Sets a local object of InsightManagerController, 
	 * defines tool-tips, and declares handlers for all buttons on the 
	 * "Parameter" tab.
	 * 
	 * @param imc -- (InsightManagerController) Reference to an object of the parent class.
	 */
	public ParameterTabController(InsightManagerController imc){
		this.imc = imc;
		this.imc.btnBuildQuery_Parm.setOnAction(this::handleBuildQuery);
		this.imc.btnBuildQuery_Parm.setTooltip(
				new Tooltip("Build Parameter query from Type URI."));
		this.imc.btnSaveParameter_Parm.setOnAction(this::handleSaveParameter);
		this.imc.btnSaveParameter_Parm.setTooltip(
				new Tooltip("Save all Parameter fields."));
		//Note: the "Reload" handler has been defined once for all in "PerspectiveTabController":
		this.imc.btnReloadParameter_Parm.setOnAction(this.imc.ptc::handleReloadPerspectives);
		this.imc.btnReloadParameter_Parm.setTooltip(new Tooltip(this.imc.btnReloadPerspective.getTooltip().getText()));
	}
	
	/**   Click-handler for the "Build Query from Type" button. Builds and inserts a simple 
	 * Sparql query into the "Parameter Query" field to fetch instances of the URI entered in the 
	 * "Parameter Type" field with associated labels. The entered "Variable" name is returned in 
	 * "?label". 
	 * 
	 * @param event
	 */
	private void handleBuildQuery(ActionEvent event){
		String strVariable = "?" + imc.legalizeQuotes(imc.txtVariable_parm.getText().trim());
        String strParameterType = ((ParameterType) imc.cboParameterType_parm.getValue()).getParameterClass();
        if(strParameterType != null && strParameterType.equals("") == false){
		   // TBD: This is insufficient and added to get past the 5/15 demo.  A prefix resolver API
		   //      should be used to map the URI to an appropriate prefixed name.
		   strParameterType = strParameterType.replace( "http://semoss.org/ontologies/", "semoss:" );
		   String generatedQuery = "SELECT " + strVariable + " ?label " +
		      "\nWHERE{\n    " + strVariable + " a " + strParameterType + " . \n    " +
		      strVariable + " rdfs:label ?label . \n}";
		   imc.txtaDefaultQuery_parm.setText(generatedQuery);
        }
	}
	
	
	/**   Click-handler for the "Save Parameter" button. Saves changes to all fields on the "Parameter" tab.
	 * 
	 * @param event
	 */
	private void handleSaveParameter(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		Parameter parameter = imc.arylInsightParameters.get(imc.intCurParameterIndex);
		
		parameter.setLabel(imc.legalizeQuotes(imc.txtLabel_parm.getText().trim()));
		parameter.setVariable(imc.legalizeQuotes(imc.txtVariable_parm.getText().trim()));

        String strParameterType = ((ParameterType) imc.cboParameterType_parm.getValue()).getParameterClass();
        if(strParameterType != null && strParameterType.equals("") == false){
		   parameter.setParameterType(strParameterType);
        }else{
           parameter.setParameterType("");
        }
		parameter.setDefaultQuery(imc.legalizeQuotes(imc.txtaDefaultQuery_parm.getText().trim()));
		
		//Define a Task to save the current Parameter:
		Task<Boolean> saveParameter = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{
		      return imc.engine.getWriteableInsightManager().getWriteableParameterTab().saveParameter(insight, parameter);
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		saveParameter.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(saveParameter.getValue() == true){
          		    Utility.showMessage("Parameter fields saved ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), 
          		       insight.getOrderedLabel(perspective.getUri()), parameter.getLabel());
            	 }else{
            		Utility.showError("Error saving Parameter.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error saving Parameter. Operation rolled back.");
              }
           }
        });
		//Run the Task on a separate Thread:
		new Thread(saveParameter).start();
	}

}//End "ParameterTabController" class.
