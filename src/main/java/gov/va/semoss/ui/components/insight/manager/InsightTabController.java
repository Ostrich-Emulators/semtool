package gov.va.semoss.ui.components.insight.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.util.Utility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Tooltip;

public class InsightTabController extends InsightManagerController {
	private InsightManagerController imc;
	
	/**   Constructor: Sets a local object of InsightManagerController, 
	 * defines tool-tips, and declares handlers for all buttons on the 
	 * "Insight" tab.
	 * 
	 * @param imc -- (InsightManagerController) Reference to an object of the parent class.
	 */
	public InsightTabController(InsightManagerController imc){
		this.imc = imc;
		this.imc.btnAddParameter_Inst.setOnAction(this::handleAddParameter);
		this.imc.btnAddParameter_Inst.setTooltip(
			new Tooltip("Add a new dummy Parameter to the Insight."));
		this.imc.btnDeleteParameter_Inst.setOnAction(this::handleDeleteParameter);
		this.imc.btnDeleteParameter_Inst.setTooltip(
			new Tooltip("Delete selected Parameter from the Insight."));
		this.imc.btnDeleteInsight_Inst.setOnAction(this::handleDeleteInsight);
		this.imc.btnDeleteInsight_Inst.setTooltip(
			new Tooltip("Delete entire Insight and all references to it."));
		this.imc.btnSaveInsight_Inst.setOnAction(this::handleSaveInsight);
		this.imc.btnSaveInsight_Inst.setTooltip(
			new Tooltip("Save changes to the Insight's Question, Display mode,\nQuery, Perspectives, and Description."));
		//Note: the "Reload" handler has been defined once for all in "PerspectiveTabController":
		this.imc.btnReloadInsight_Inst.setOnAction(this.imc.ptc::handleReloadPerspectives);
		this.imc.btnReloadInsight_Inst.setTooltip(new Tooltip(this.imc.btnReloadPerspective.getTooltip().getText()));

		//Whenever the "Add Parameter" or "Delete Parameter" buttons are clicked (in focus),
		//The "Parameters" ListView must be selected to ensure correct ui updating during
		//the button's action:
		imc.btnAddParameter_Inst.focusedProperty().addListener(new ChangeListener<Boolean>(){
		   @Override
		   public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
		 	  if(arg2 == true){
			     focusParameterListView();
	          }
		   }
		});
		imc.btnDeleteParameter_Inst.focusedProperty().addListener(new ChangeListener<Boolean>(){
		   @Override
		   public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
		 	  if(arg2 == true){
			     focusParameterListView();
	          }
		   }
		});
	}
    /**   Sets focus to the "Parameters" ListView. Called whenever the "Add Parameter"
     * or "Delete Parameter" button is clicked.
     */
	private void focusParameterListView(){
		imc.lstvParameter_Inst.requestFocus();
		imc.lstvParameter_Inst.getSelectionModel().select(imc.lstvParameter_Inst.getSelectionModel().getSelectedIndex());
		imc.lstvParameter_Inst.getFocusModel().focus(imc.lstvParameter_Inst.getSelectionModel().getSelectedIndex());
	}
	

	/**   Click-handler for the "Add Parameter" button. Adds a new Parameter under the current Insight. 
	 */
	private void handleAddParameter(ActionEvent event){		
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);

		//Define a Task to save the current Parameter:
		Task<Boolean> addParameter = new Task<Boolean>(){
		   @Override 
	       protected Boolean call() throws Exception{
			   return imc.engine.getWriteableInsightManager().getWriteableInsightTab().addParameter(insight);
	       }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
	    addParameter.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(addParameter.getValue() == true){
           		    Utility.showMessage("New Parameter added to current Insight ok.");
           		    
         		    //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(),
               		    insight.getOrderedLabel(perspective.getUri()), null);         		    	            		    
            	 }else{
                	Utility.showError("Error adding Parameter to current Insight. Operation rolled back.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error adding Parameter to current Insight. Operation rolled back.");
              }
           }
        });
	    //Run the Task on a separate Thread:
	    new Thread(addParameter).start();
	}

	/**   Click-handler for the "Delete Parameter" button. Removes the currently selected
	 * Parameter from the list-view and the database.
	 */
	private void handleDeleteParameter(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		Parameter parameter = imc.arylInsightParameters.get(imc.intCurParameterIndex);
		
		//Define a Task to remove the current Insight:
		Task<Boolean> deleteParameter = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{
		      return imc.engine.getWriteableInsightManager().getWriteableInsightTab().deleteParameter(insight, parameter);
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		deleteParameter.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(deleteParameter.getValue() == true){
           		    Utility.showMessage("Insight Parameter, deleted ok.");
           		    
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(),
                   		insight.getOrderedLabel(perspective.getUri()), null);   
            	 }else{
            		Utility.showError("Error deleting Parameter. Operation rolled back.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error deleting Parameter. Operation rolled back.");
              }
           }
        });
		if(parameter.getParameterURI().equals("") == false && 
		   Utility.showWarningOkCancel("Are you sure you want to delete this Parameter\npermanently from the Insight?") == 0){
		   //Run the Task on a separate Thread:
		   new Thread(deleteParameter).start();
	    }	
	}

	/**   Click-handler for the "Delete Insight" button. Removes the currently selected
	 * Insight from the screen and the database.
	 */
	private void handleDeleteInsight(ActionEvent event){
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		
		//Define a Task to remove the current Insight:
		Task<Boolean> deleteInsight = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{
		      return imc.engine.getWriteableInsightManager().getWriteableInsightTab().deleteInsight(insight);
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		deleteInsight.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(deleteInsight.getValue() == true){
          		    Utility.showMessage("Insight, and all references to it, deleted ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), null, null);

            	 }else{
            		Utility.showError("Error deleting Insight. Operation rolled back.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error deleting Insight. Operation rolled back.");
              }
           }
        });
		if(Utility.showWarningOkCancel("Are you sure you want to delete this Insight permanently,\nalong with all Perspective references to it?") == 0){
		   Collection<String> colEndangeredPerspectives = getEndangeredPerspectiveTitles(insight.getId());
		   if(colEndangeredPerspectives.size() == 0){
			  //Run the Task on a separate Thread:
		      new Thread(deleteInsight).start();
		      
		   }else{
			  Utility.showError("Error removing Insight. Cannot remove the only Insight under the following Perspectives:\n"
				  + colEndangeredPerspectives.toString()); 
		   }
	    }
	}

    /**   Returns a list of Perspective labels, where each Perspective has only one
     * Insight, corresponding to the URI passed in, and where removal of the passed-in
     * Insight would leave the Perspective with no Insights (This method is designed 
     * to be called from "handleDeleteInsight(...)".
     * 
     * @param insightURI -- (URI) URI of an Insight.
     * 
     * @return getEndangeredPerspectiveTitles -- (Collection<String>) Described above.
     */
	private Collection<String> getEndangeredPerspectiveTitles(URI insightURI){
		ArrayList<String> arylPerspectiveTitles = new ArrayList<String>();
		
		for(Perspective perspective: imc.arylPerspectives){
			ArrayList<Insight> arylInsights = perspective.getInsights();
			
			if(arylInsights.size() == 1){
               if(arylInsights.get(0).getId().equals(insightURI)){
            	   arylPerspectiveTitles.add(perspective.getLabel());
               }
			}
		}		
		return arylPerspectiveTitles;
	}
	
	/**   Click-handler for the "Save Insight" button. Saves changes to all fields on the "Insight" tab,
	 * except for the "Parameters" list-view and the "Date Created" text-field.
	 * 
	 * @param event
	 */
	private void handleSaveInsight(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		Collection<String> colManuallySelectedPerspectiveTitles = imc.lstvInsightPerspective_Inst.getSelectionModel().getSelectedItems();
		Collection<Perspective> colPerspectivesToAddInsight = new ArrayList<Perspective>();
		Collection<Perspective> colPerspectivesToRemoveInsight = new ArrayList<Perspective>();
		Collection<String> colEndangeredPerspectiveTitles = new ArrayList<String>();
		Preferences prefs = Preferences.userRoot();
		
		//Set Insight fields from "Insight" tab in UI:
		insight.setLabel(imc.legalizeQuotes(imc.txtQuestion_Inst.getText().trim()));
		for(PlaySheet playsheet: imc.arylPlaySheets){
			if(playsheet.getLabel().equals(imc.cboDisplayWith_Inst.getValue())){
				insight.setOutput(playsheet.getViewClass());
				break;
			}
		}
		if(imc.txtRendererClass_Inst.getText() != null){
		  insight.setRendererClass(imc.legalizeQuotes(imc.txtRendererClass_Inst.getText().trim()));
		}else{
		  insight.setRendererClass("");
		}
		insight.setIsLegacy(imc.chkLegacyQuery_Inst.isSelected());
		insight.setSparql(imc.legalizeQuotes(imc.txtaQuery_Inst.getText().trim()));
		insight.setDescription(imc.legalizeQuotes(imc.txtaInsightDesc_Inst.getText().trim()));
		insight.setCreator(imc.legalizeQuotes(imc.txtCreator_Inst.getText().trim()));
		insight.setModified(String.valueOf(new Date()));

		//Build a collection of Perspectives that must add this Insight:
		for(String perspectiveTitle: colManuallySelectedPerspectiveTitles){
			for(Perspective p_1: imc.arylPerspectives){
				if(p_1.getLabel().equals(perspectiveTitle)
				   && imc.arylInsightPerspectives.contains(perspectiveTitle) == false){
					 colPerspectivesToAddInsight.add(p_1);
				}
			}
		}	
		//Build a collection of Perspectives that must remove this Insight. 
		//Be careful not to include Perspectives that have only this Insight left:
		colEndangeredPerspectiveTitles.addAll(getEndangeredPerspectiveTitles(insight.getId()));
		for(Perspective p_2: imc.arylPerspectives){
			if(imc.arylInsightPerspectives.contains(p_2.getLabel()) == true 
			   && colManuallySelectedPerspectiveTitles.contains(p_2.getLabel()) == false
			   && colEndangeredPerspectiveTitles.contains(p_2.getLabel()) == false){
				 colPerspectivesToRemoveInsight.add(p_2);
			}
		}
		//Define a Task to save the current Insight:
		Task<Boolean> saveInsight = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{
		      return imc.engine.getWriteableInsightManager().getWriteableInsightTab()
		    	 .saveInsight(insight, colPerspectivesToAddInsight, colPerspectivesToRemoveInsight);
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		saveInsight.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(saveInsight.getValue() == true){
          		    Utility.showMessage("Insight, and associated Perspectives, saved ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), 
          		       insight.getOrderedLabel(perspective.getUri()), null);
            	 }else{
            		Utility.showError("Error saving Insight.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error saving Insight. Operation rolled back.");
              }
           }
        });
		//Run the Task on a separate Thread:
		new Thread(saveInsight).start();
	}

}//End "InsightTabController" class.
