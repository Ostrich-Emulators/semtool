package gov.va.semoss.ui.components.insight.manager;

import java.util.ArrayList;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.util.Utility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Tooltip;


public class PerspectiveTabController extends InsightManagerController{
	private InsightManagerController imc;
	
	/**   Constructor: Sets a local object of InsightManagerController, 
	 * defines tool-tips, and declares handlers for all buttons on the 
	 * "Perspective" tab.
	 * 
	 * @param imc -- (InsightManagerController) Reference to an object of the parent class.
	 */
	public PerspectiveTabController(InsightManagerController imc){
		this.imc = imc;
		this.imc.btnAddInsight.setOnAction(this::handleAddInsight);
		this.imc.btnAddInsight.setTooltip(
			new Tooltip("Add a new dummy insight to the Perspective."));
		this.imc.btnRemoveInsight.setOnAction(this::handleRemoveInsight);
		this.imc.btnRemoveInsight.setTooltip(
			new Tooltip("Remove selected Insight from Perspective,\nbut leave the Insight dangling."));
		this.imc.btnAddPerspective.setOnAction(this::handleAddPerspective);
		this.imc.btnAddPerspective.setTooltip(
			new Tooltip("Add a new Perspective\nwith one dummy Insight."));
		this.imc.btnDeletePerspective.setOnAction(this::handleDeletePerspective);
		this.imc.btnDeletePerspective.setTooltip(
			new Tooltip("Delete Perspective, but\nleave its Insights dangling."));
		this.imc.btnSavePerspective.setOnAction(this::handleSavePerspective);
		this.imc.btnSavePerspective.setTooltip(
		    new Tooltip("Save changes to Perspective's Title and Description."));
		this.imc.btnReloadPerspective.setOnAction(this::handleReloadPerspectives);
		this.imc.btnReloadPerspective.setTooltip(
			new Tooltip("Reload visual controls, and move all dangling\nInsights to the Detached-Insight-Perspective."));
		
		//Whenever the "Add Insight" or "Remove Insight" buttons are clicked (in focus),
		//The "Insights" ListView must be selected to ensure correct ui updating during
		//the button's action:
	    imc.btnAddInsight.focusedProperty().addListener(new ChangeListener<Boolean>(){
		   @Override
		   public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
		 	 if(arg2 == true){
				focusInsightListView();
	         }
		   }
	    });
	    imc.btnRemoveInsight.focusedProperty().addListener(new ChangeListener<Boolean>(){
		   @Override
		   public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
		 	 if(arg2 == true){
				focusInsightListView();
	         }
		   }
	    });
	}
    /**   Sets focus to the Insights ListView. Called whenever the "Add Insight"
     * or "Remove Insight" button is clicked.
     */
	private void focusInsightListView(){
		imc.lstvInsights.requestFocus();
		imc.lstvInsights.getSelectionModel().select(imc.lstvInsights.getSelectionModel().getSelectedIndex());
		imc.lstvInsights.getFocusModel().focus(imc.lstvInsights.getSelectionModel().getSelectedIndex());		
	}
	
	/**   Click-handler for the "Add Insight" button. Adds a new Insight under the current Perspective. 
	 */
	private void handleAddInsight(ActionEvent event){		
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);

		//Define a Task to save the current Perspective:
		Task<Boolean> addInsight = new Task<Boolean>(){
		   @Override 
	       protected Boolean call() throws Exception{
			   return imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab().addInsight(insight.getOrder(), perspective);
	       }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
	    addInsight.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(addInsight.getValue() == true){
          		    Utility.showMessage("New Insight added to current Perspective ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), null, null);         		    
          		    
            	 }else{
                	Utility.showError("Error adding Insight to current Perspective. Operation rolled back.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error adding Insight to current Perspective. Operation rolled back.");
              }
           }
        });
	    //Run the Task on a separate Thread:
	    new Thread(addInsight).start();
	}
	
	/**   Click-handler for the "Remove Insight" button. Removes the currently selected
	 * Insight from the screen and from the current Perspective.
	 */
	private void handleRemoveInsight(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		
		//Define a Task to remove the current Insight:
		Task<Boolean> removeInsight = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{				  			  
		      return imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab().removeInsight(new ArrayList<Insight>(imc.arylInsights), insight, perspective, true);
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		removeInsight.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){
            	 if(removeInsight.getValue() == true){
          		    Utility.showMessage("Insight removed ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), null, null);

            	 }else{
            		Utility.showError("Error removing Insight. Operation rolled back.");
            	 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error removing Insight. Operation rolled back.");
              }
           }
        });
		if(Utility.showWarningOkCancel("Are you sure you want to remove this Insight?") == 0){
		   if(imc.lstvInsights.getItems().size() > 1){
			  //Run the Task on a separate Thread:
		      new Thread(removeInsight).start();
		   }else{
			  Utility.showError("Error removing Insight. Cannot remove the only Insight under a Perspective."); 
		   }
	    }
	}
	
	/**   Click-handler for the "Add Perspective" button. Adds a new Perspective, with the entered
	 * Title and Description to the screen and database. Also adds one dummy Insight and associated
	 * fields.
	 */
	private void handleAddPerspective(ActionEvent event){
		String strTitle = imc.legalizeQuotes(imc.txtPerspectiveTitle.getText().trim());		  
		//Replace all spaces in "strTitle" with underscores,
		//to make the Title suitable for a URI:
		String strUriTitle = strTitle.replaceAll("\\s+", "");
		
		String strDescription = imc.legalizeQuotes(imc.txtaPerspectiveDesc.getText().trim());
		
		//Define a Task to save the current Perspective:
		Task<Boolean> addPerspective = new Task<Boolean>(){
		   @Override 
	       protected Boolean call() throws Exception{
			   boolean boolReturnValue = false;
			   boolReturnValue = imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab().addPerspective(strTitle, strUriTitle, strDescription, true);
               return boolReturnValue;
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
	    addPerspective.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
              if(newState == Worker.State.SUCCEEDED){           	  
                 if(addPerspective.getValue() == true){
                	Utility.showMessage("New Perspective with Title and Description added ok.");
    	 	        //Reload the UI from the database:
          		    imc.loadData(imc.txtPerspectiveTitle.getText().trim(), null, null);          		 

                 }else{
                	Utility.showError("Error adding Perspective. Operation rolled back.");
                 }          		 
              }else if(newState == Worker.State.FAILED){
            	 Utility.showError("Error adding Perspective. Operation rolled back.");
              }
           }
        });
		if(strUriTitle.equals("") == false && isUriTitleUnique(strUriTitle) == true){
		     //Run the Task on a separate Thread:
	         new Thread(addPerspective).start();
		}else{
			 Utility.showError("Error: The internal Perspective identifier must be unique and non-empty.\n" +
		        "It is based upon the Title, and all spaces within the Title are compressed\n" +
				"to form the identifier.");
		}
	}
	
	/**   Returns true only if the passed-in URI Title is unique in the "arylPerspectives"
	 * ObservableList. The URI Title is based upon a user-entered Title for a new Perspective,
	 * but it is stripped of all internal spaces.
	 * 
	 * @param strUriTitle -- (Sting) User-entered Title for a new Perspective, stripped of all
	 *    internal spaces.
	 *    
	 * @return isUriTitleUnique -- (boolean) Described above.
	 */
	private boolean isUriTitleUnique(String strUriTitle){
	   boolean boolReturnValue = true;
	   
	   for(Perspective perspective: imc.arylPerspectives){
		   if(perspective.getLabel().replaceAll("\\s+", "").equals(strUriTitle)){
			  boolReturnValue = false;
			  break;
		   }
	   }
	   return boolReturnValue;
	}
	
	/**   Click-handler for the "Delete Perspective" button. Removes the current Perspective
	 * from the screen and the database.
	 */
	private void handleDeletePerspective(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		
		//Define a Task to delete the current Perspective:
		Task<Boolean> deletePerspective = new Task<Boolean>(){
		   @Override 
		   protected Boolean call() throws Exception{
			  boolean boolReturnValue = false;			  
			  boolReturnValue = imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab().deletePerspective(perspective);
              return boolReturnValue;
		   }
		};
        //Define a listener to update the JavaFX UI when the Task completes:
		deletePerspective.stateProperty().addListener(new ChangeListener<Worker.State>(){
           @Override 
           public void changed(ObservableValue<? extends Worker.State> observableValue, 
        	  Worker.State oldState, Worker.State newState){
                if(newState == Worker.State.SUCCEEDED){
            	   if(deletePerspective.getValue() == true){
      	 	          Utility.showMessage("Perspective deleted ok.");
       	 	          //Reload the UI from the database:
             		  imc.loadData(null, null, null);                  		  
          		    
            	   }else{
                      Utility.showError("Error deleting Perspective. Operation rolled back.");
            	   }         		 
                }else if(newState == Worker.State.FAILED){
            	   Utility.showError("Error deleting Perspective. Operation rolled back.");
                }
           }
        });
		//Run the Task on a separate Thread:
		if(Utility.showWarningOkCancel("Are you sure you want to delete this Perspective permanently,\nand remove all of its Insights?") == 0){
		   if(imc.cboPerspectiveTitle.getItems().size() > 1){
		      new Thread(deletePerspective).start();
		   }else{
			  Utility.showError("Error deleting Perspective. Cannot delete the only Perspective.");		  
		   }
		}
	}
	
	/**   Click-handler for the "Save Perspective" button. Persists changes to the Title and
	 * Description of the current Perspective in the database.
	 */
	private void handleSavePerspective(ActionEvent event){
		Perspective perspective = imc.arylPerspectives.get(imc.intCurPerspectiveIndex);
		Insight insight = imc.arylInsights.get(imc.intCurInsightIndex);
		String strTitle = imc.legalizeQuotes(imc.txtPerspectiveTitle.getText().trim());
		String strDescription = imc.legalizeQuotes(imc.txtaPerspectiveDesc.getText().trim());
		   //Define a Task to save the current Perspective:
		   Task<Boolean> savePerspective = new Task<Boolean>(){
		       @Override 
		       protected Boolean call() throws Exception{
		    	   return imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab()
		    		  .savePerspective(new ArrayList<Insight>(imc.arylInsights), perspective.getUri().toString(), strTitle, strDescription);
		       }
		   };
	       //Define a listener to update the JavaFX UI when the Task completes:
		   savePerspective.stateProperty().addListener(new ChangeListener<Worker.State>(){
	          @Override 
	          public void changed(ObservableValue<? extends Worker.State> observableValue, 
	        	 Worker.State oldState, Worker.State newState){
	             if(newState == Worker.State.SUCCEEDED){
	            	if(savePerspective.getValue() == true){
          	 	       Utility.showMessage("Perspective Title and Description saved ok.");
       	 	           //Reload the UI from the database:
             		   imc.loadData(imc.txtPerspectiveTitle.getText().trim(), insight.getOrderedLabel(), null);
             		   
	            	}else{
		               Utility.showError("Error saving Perspective. Operation rolled back.");
	            	}
	              }else if(newState == Worker.State.FAILED){
	             	 Utility.showError("Error saving Perspective. Operation rolled back.");
	              }
	          }
	       });
		   if(strTitle.equals("") == false){
		      //Run the Task on a separate Thread:
		      new Thread(savePerspective).start();
		   }else{
			  Utility.showError("Error: You must enter a Title, and you should enter a Description."); 
		   }
	}
	
	/**   Click-handler for the "Reload" button on the "Perspective" tab.
	 * Reloads all ui fields from the database.
	 */
	protected void handleReloadPerspectives(ActionEvent event){
		associateDetachedInsights();
	}
	
	/**    Attempts to associate all dangling Insights with the "Detached-Insight-Perspective".
	 * Dangling Insights occur when an Insight is removed from the "Perspective" tab, or when
	 * a Perspective is deleted. Insights are never completely removed from the database by
	 * any action on the "Perspective" tab.
	 * 
	 *     This method is designed to be invoked within the button-click handler,
	 * "handleReloadPerspectives(...)".
	 * 
	 */
	private void associateDetachedInsights(){
		//Define a Task to associate detached Insights to the "Detached-Insight-Perspective":
		Task<Boolean> associateInsights = new Task<Boolean>(){
		    @Override 
		    protected Boolean call() throws Exception{
		    	return imc.engine.getWriteableInsightManager().getWriteablePerspectiveTab().saveDetachedInsights();
		    }
		};
	    //Define a listener to update the JavaFX UI when the Task completes:
		associateInsights.stateProperty().addListener(new ChangeListener<Worker.State>(){
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, 
	           Worker.State oldState, Worker.State newState){    	
	            if(newState == Worker.State.SUCCEEDED){
	               if(associateInsights.getValue() == true){
       	 	          Utility.showMessage("Dangling Insights may have been moved to Detached-Insight-Perspective.");

	               }else{
		              Utility.showError("Error reassociating dangling Insights. Operation rolled back.");
	               }
	            }else if(newState == Worker.State.FAILED){
	               Utility.showError("Error reassociating dangling Insights. Operation rolled back.");
	            }
	 	        //Reload the UI from the database:
	            imc.loadData(imc.txtPerspectiveTitle.getText().trim(), 
	               imc.arylInsights.get(imc.intCurInsightIndex).getOrderedLabel(), null); 	            
	        }
	    });
		//Run the Task on a separate Thread:
		new Thread(associateInsights).start();
	}
	
}//End PerspectiveTabController class.
