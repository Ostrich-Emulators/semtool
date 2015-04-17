package gov.va.semoss.ui.components.insight.manager;

import java.awt.AWTException;
import java.awt.Robot;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.PlaySheetEnum;
import gov.va.semoss.util.Utility;


public class  InsightManagerController implements Initializable{
	protected IEngine engine;
	@FXML
	protected TabPane tbpTabbedPane;
	@FXML
	protected ComboBox<String> cboPerspectiveTitle;
	protected ArrayList<Perspective> arylPerspectives;
	protected int intCurPerspectiveIndex;
	@FXML
	protected TextField txtPerspectiveTitle;
	@FXML
	protected TextArea txtaPerspectiveDesc;	
	@FXML
	protected ListView<String> lstvInsights;
	protected ArrayList<Insight> arylInsights;
	protected int intCurInsightIndex;
	protected String prevQuestionLabel;
	@FXML
	protected TextField txtQuestion_Inst;
	@FXML
	protected ComboBox<String> cboDisplayWith_Inst;
	protected ArrayList<PlaySheet> arylPlaySheets;
	@FXML
	protected TextField txtRendererClass_Inst;
	@FXML
	protected CheckBox chkLegacyQuery_Inst;
	@FXML
	protected TextArea txtaQuery_Inst;
	@FXML
	protected ListView<String> lstvParameter_Inst;
	protected ArrayList<Parameter> arylInsightParameters;
	protected int intCurParameterIndex;
	protected String prevParameterLabel;
	@FXML
	protected ListView<String> lstvInsightPerspective_Inst;
	protected ArrayList<String> arylInsightPerspectives;
	@FXML
	protected TextArea txtaInsightDesc_Inst;
	@FXML
	protected TextField txtCreator_Inst;
	@FXML
	protected TextField txtCreated_Inst;
	@FXML
	protected TextField txtModified_Inst;
	@FXML
	protected TextField txtLabel_parm;
    @FXML 
    protected TextField txtVariable_parm;
    @FXML
    protected TextField txtValueType_parm;
    @FXML
    protected TextField txtDefaultValue_parm;
    @FXML
    protected TextArea txtaDefaultQuery_parm;
	@FXML
	protected Tab tabParameter;
	
	protected PerspectiveTabController ptc;
	@FXML
	protected Button btnAddInsight;
	@FXML
	protected Button btnRemoveInsight;
	@FXML
	protected Button btnAddPerspective;
	@FXML
	protected Button btnDeletePerspective;
	@FXML
	protected Button btnSavePerspective;
	@FXML
	protected Button btnReloadPerspective;
	
	protected InsightTabController itc;
	@FXML
	protected Button btnAddParameter_Inst;
	@FXML
	protected Button btnDeleteParameter_Inst;
	@FXML
	protected Button btnDeleteInsight_Inst;
	@FXML
	protected Button btnSaveInsight_Inst;
	@FXML
	protected Button btnReloadInsight_Inst;
	
    protected ParameterTabController prmtc;	
	@FXML
	protected Button btnSaveParameter_Parm;
	@FXML
	protected Button btnReloadParameter_Parm;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	} 
	
	/**    Populates controls when the Insight Manager is loaded. Also provides change-listeners 
	 * for text-fields, combo-boxes and list-views that affect other controls.
	 */
	public void setData(){		    
		engine = DIHelper.getInstance().getRdfEngine();
		
		//If the engine has been loaded, then populate controls, otherwise skip:
		if(engine != null){
		   arylPerspectives = new ArrayList<Perspective>();
		   arylPlaySheets = new ArrayList<PlaySheet>();
		   arylInsightParameters = new ArrayList<Parameter>();
		   //The Insight Perspective list-view must handle multiple selections:
		   lstvInsightPerspective_Inst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		   //Instantiate button-handlers for the "Perspective" tab:
		   ptc = new PerspectiveTabController(this);
		   //Instantiate button-handlers for the "Insight" tab:
		   itc = new InsightTabController(this);
		   //Instantiate button-handlers for the "Parameter" tab:
		   prmtc = new ParameterTabController(this);
		   
		   loadPlaySheetsAndData();
		   
		   //If the "Perspective" changes on the "Perspective" tab, then repopulate its text-field, 
		   //its "Description", and its associated "Insights:
		   //--------------------------------------------------------------------------------------
           cboPerspectiveTitle.valueProperty().addListener(new ChangeListener<Object>() {
		        @Override 
		        public void changed(ObservableValue<?> ov, Object t, Object t1) {
		        	
		        	//Get selected "Perspective":
		            Perspective perspective = new Perspective();
		            intCurPerspectiveIndex = 0;
		            for(int i = 0; i < arylPerspectives.size(); i++){
		            	perspective = (Perspective) arylPerspectives.get(i);
		            	if((perspective.getLabel().equals((String) t1))){
		            		intCurPerspectiveIndex = i;
		            		break;
		            	}
		            }
					arylInsights = ((Perspective) arylPerspectives.get(intCurPerspectiveIndex)).getInsights();
					 
		            //When the selected "Perspective" changes, re-populate fields on the "Perspective" tab:
		            //-------------------------------------------------------------------------------------
		            populatePerspectiveTitleTextField(intCurPerspectiveIndex);
		            populatePerspectiveDescTextArea(intCurPerspectiveIndex);		        	
		 		    populateInsightListView(intCurPerspectiveIndex);
		 		}    
		   });	
           
           //If the "Insights" list-view changes on the "Perspective" tab, then repopulate
           //the "Insight" tab fields:
           //-----------------------------------------------------------------------------
           lstvInsights.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>(){
			  @Override
			  public void changed(ObservableValue<?> ov, Object t, Object t1){
				 //Get selected "Insight":
	             Insight insight = new Insight();
	             intCurInsightIndex = 0;
	             for(int i = 0; i < arylInsights.size(); i++){
	            	 insight = (Insight) arylInsights.get(i);
	            	 if((insight.getOrderedLabel(((Perspective) arylPerspectives.get(intCurPerspectiveIndex)).getUri()).equals((String) t1))){
	            		intCurInsightIndex = i;
	            		break;
	            	 }
	             }
				 arylInsightParameters = (ArrayList<Parameter>) ((Insight) arylInsights.get(intCurInsightIndex)).getInsightParameters();

                 //When the selected "Insight" changes, re-populate fields on the "Insight" tab:
	             //-----------------------------------------------------------------------------
	             populateQuestionTextField(intCurPerspectiveIndex, intCurInsightIndex);
	             populateLegacyQueryCheckBox(intCurPerspectiveIndex, intCurInsightIndex);
	             populateQueryTextArea(intCurPerspectiveIndex, intCurInsightIndex);	
	             populatePlaysheetComboBox(intCurPerspectiveIndex, intCurInsightIndex);
	             populateInsightDescTextArea(intCurPerspectiveIndex, intCurInsightIndex);
	             populateCreatorTextField(intCurPerspectiveIndex, intCurInsightIndex);
	             populateCreatedTextField(intCurPerspectiveIndex, intCurInsightIndex);
	             populateModifiedTextField(intCurPerspectiveIndex, intCurInsightIndex);
	             populateInsightParameterListView(intCurPerspectiveIndex, intCurInsightIndex);
	             populateInsightPerspectivesListView(intCurPerspectiveIndex, intCurInsightIndex);
	          }				   
		   });
           
           //If the "Parameters" list-view changes on the "Insight" tab, then repopulate
           //the "Parameter" tab fields:
           //---------------------------------------------------------------------------
           lstvParameter_Inst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>(){
			  @Override
			  public void changed(ObservableValue<?> ov, Object t, Object t1){
				 arylInsights = ((Perspective) arylPerspectives.get(intCurPerspectiveIndex)).getInsights();

				 //Get selected "Parameter":
	             Parameter parameter = new Parameter();
	             intCurParameterIndex = 0;
	             for(int i = 0; i < arylInsightParameters.size(); i++){
	            	 parameter = (Parameter) arylInsightParameters.get(i);
	            	 if(parameter.getLabel().equals((String) t1)){
	            		intCurParameterIndex = i;
	            		break;
	            	 }
	             }
                 //When the selected "Parameter" changes, re-populate fields on the "Parameter" tab:
	             //---------------------------------------------------------------------------------
	             populateNameTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);	          				   
			     populateVariableTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
			     populateTypeTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
			     populateDefaultValueTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
			     populateDefaultQueryTextArea(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
			  }
		   });
		   
           //Initially check the "Renderer Class" text-field on the "Insight" tab. 
           //If it is not empty, then disable the various query-related controls:
	       if(txtRendererClass_Inst.getText().trim().isEmpty()){
	    	  cboDisplayWith_Inst.setDisable(false);
	    	  chkLegacyQuery_Inst.setDisable(false);
	    	  txtaQuery_Inst.setDisable(false);
	    	  lstvParameter_Inst.setDisable(false);
	    	  tabParameter.setDisable(false);
	       }else{
	    	  cboDisplayWith_Inst.setDisable(true);
	    	  chkLegacyQuery_Inst.setDisable(true);
	    	  txtaQuery_Inst.setDisable(true);
	    	  lstvParameter_Inst.setDisable(true);
	    	  tabParameter.setDisable(true);
	       }
	       //If the "Renderer Class" text-field changes, and it is not empty, 
	       //then disable query-related controls. Otherwise, enable them:
	       //----------------------------------------------------------------
           txtRendererClass_Inst.textProperty().addListener(new ChangeListener<Object>(){
			   @Override
			   public void changed(ObservableValue<?> ov, Object t, Object t1) {
				  String strNewText = (String) t1;
			      if(strNewText.trim().isEmpty()){
			    	  cboDisplayWith_Inst.setDisable(false);
			    	  chkLegacyQuery_Inst.setDisable(false);
			    	  txtaQuery_Inst.setDisable(false);
			    	  lstvParameter_Inst.setDisable(false);
			    	  tabParameter.setDisable(false);
			      }else{
			    	  cboDisplayWith_Inst.setDisable(true);
			    	  chkLegacyQuery_Inst.setDisable(true);
			    	  txtaQuery_Inst.setDisable(true);
			    	  lstvParameter_Inst.setDisable(true);
			    	  tabParameter.setDisable(true);
			      }
			   }        	   
           });  		
		}//End if(engine != null).		
	}
	
	/**   Loads all data needed for the Insight Manager from the database into
	 * the ArrayList<Perspective>, "arylPerspectives". After all Perspectives,
	 * and their Insights, have been loaded, a call is made to populate the
	 * "Perspective" combo-box.
	 * 
	 * Note: "Perspective" is a value-object for populating various ui fields
	 * One such value is "get/setInsight(...)", which refers to an ArrayList<Insight>,
	 * based upon another value-object containing Insight data.
	 * 
     * @param perspectiveLabel -- (String) The current Perspective Title, passed in
     *    from the "PerspectiveTabController" click-handlers. Enables maintaining the 
     *    selected Perspective, after adding/deleting Insights and adding/saving
     *    Perspectives. If null is passed in (as in the case of the initial load,
     *    and when a Perspective is deleted), then the first Perspective is selected
     *    from the combo-box.	 
     *    
     * @param prevQuestionLabel -- (String) Before new data is loaded, a previous
     *    Insight may have been selected. This is the label of that Insight. In the 
     *    case of a starting load or a removal/deletion, this value can be null.
     *    
     * @param prevParameterLabel -- (String) Before new data is loaded, a previous
     *    Insight Parameter may have been selected. This is the label of that Parameter. 
     *    In the case of a starting load, removal/deletion, or a persistence on the 
     *    "Perspective" or "Insight" tab, this value can be null.
     */
	protected void loadData(String perspectiveLabel, String prevQuestionLabel, String prevParameterLabel){
		arylPerspectives.clear();
		this.prevQuestionLabel = prevQuestionLabel;
		this.prevParameterLabel = prevParameterLabel;
	    //Convert mouse-pointer to a "wait" cursor:
		cboPerspectiveTitle.getScene().setCursor(Cursor.WAIT);
		
		//Define a Task to fetch an ArrayList of Perspectives/Insights:
		Task<ArrayList<Perspective>> getInsightManagerData = new Task<ArrayList<Perspective>>(){
		    @Override 
		    protected ArrayList<Perspective> call() throws Exception {
		    	arylPerspectives.addAll(engine.getInsightManager().getPerspectives());
		    	   
		    	for(Perspective perspective: arylPerspectives){
		    		ArrayList<Insight> arylInsights = new ArrayList<Insight>();
		    		arylInsights.addAll(engine.getInsightManager().getInsights(perspective));
		    		
		    		for(Insight insight: arylInsights){
			    		ArrayList<Parameter> arylInsightParameters = new ArrayList<Parameter>();
			    		arylInsightParameters.addAll(engine.getInsightManager().getInsightParameters(insight.getId()));
			    		insight.setInsightParameters(arylInsightParameters);
		    		}
		    		perspective.setInsights(arylInsights);
		    	}
		        return arylPerspectives;
		    }
		};
	    //Define a listener to set the return value when the  Task completes:
		getInsightManagerData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	//Restore mouse-pointer:
	            	cboPerspectiveTitle.getScene().setCursor(Cursor.DEFAULT);
	     		   
	     		    //Populate "Perspectives" combo-box (first, we must change the value, so that 
	            	//population triggers the change-handler that populates the Insight list-view:
	            	cboPerspectiveTitle.setValue("");
	     		    populatePerspectiveComboBox();
	     		    
	     		    //If passed-in label is not null, then navigate to that Perspective:
	     		    if(perspectiveLabel != null && perspectiveLabel.trim().equals("") == false){
	     		    	cboPerspectiveTitle.getSelectionModel().select(perspectiveLabel);

	     		    //Otherwise, navigate to the first Perspective:
	     		    }else{
	     		    	cboPerspectiveTitle.getSelectionModel().selectFirst();
	     		    }
	      	    }    	    
	        }
	     });
		 //Run the Task on a separate Thread:
		 new Thread(getInsightManagerData).start();
	}
	
	/**   Same as "loadData(...)", but also fetches data for the Insight tab's "Display with"
	 * combo-box. Designed to be run once when the Insight Manager is loaded initially.
	 */
	private void loadPlaySheetsAndData(){		
		arylPlaySheets.clear();
	    //Convert mouse-pointer to a "wait" cursor:
		cboDisplayWith_Inst.getScene().setCursor(Cursor.WAIT);
		
		//Define a Task to fetch an ArrayList of PlaySheets:
		Task<ArrayList<PlaySheet>> getPlaySheetData = new Task<ArrayList<PlaySheet>>(){
		    @Override 
		    protected ArrayList<PlaySheet> call() throws Exception {
		    	arylPlaySheets.addAll(engine.getInsightManager().getPlaySheets());		    	
		        return arylPlaySheets;
		    }
		};
	    //Define a listener to set the return value when the Task completes:
		getPlaySheetData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	//Restore mouse-pointer:
	            	cboDisplayWith_Inst.getScene().setCursor(Cursor.DEFAULT);
	            	
	            	//Load Insight Manager data:
	            	loadData(null, null, null);
	      	    }    	    
	        }
	     });
		 //Run the Task on a separate Thread:
		 new Thread(getPlaySheetData).start();
	}
//----------------------------------------------------------------------------------------------------
//	                            P e r s p e c t i v e   T a b
//----------------------------------------------------------------------------------------------------
	/**   Populates the Perspective combo-box on the "Perspective" tab.
	 */
	protected void populatePerspectiveComboBox(){
  	    cboPerspectiveTitle.getItems().clear();
 	    for(int i = 0; i < arylPerspectives.size(); i++){
		   cboPerspectiveTitle.getItems().add(((Perspective) arylPerspectives.get(i)).getLabel());
	    }
 	}

	/**   Populates the Perspective Title text-field with the title of
	 * the currently selected Perspective.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 */
	protected void populatePerspectiveTitleTextField(int perspectiveIndex){
		String strTitle = "";
		if(arylPerspectives.get(perspectiveIndex).getLabel() != null){
		   strTitle = arylPerspectives.get(perspectiveIndex).getLabel();
		}
	    txtPerspectiveTitle.setText(strTitle);	
	}
	
	/**   Populates the Perspective Description text-area with the description of
	 * the currently selected Perspective.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 */
	protected void populatePerspectiveDescTextArea(int perspectiveIndex){		
		String strDescription = "";
		if(arylPerspectives.get(perspectiveIndex).getDescription() != null){
		   strDescription = arylPerspectives.get(perspectiveIndex).getDescription();
		}
	    txtaPerspectiveDesc.setText(strDescription);	
	}
	
	/**   Populates the Insight list-view with Insights under the currently
	 * selected Perspective.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 */
	protected void populateInsightListView(int perspectiveIndex){
  	    lstvInsights.getItems().clear();
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    for(Insight element: arylInsights){
		   lstvInsights.getItems().add(element.getOrderedLabel(((Perspective) arylPerspectives.get(perspectiveIndex)).getUri()));
	    }
	    //Navigate to the previously selected Insight (or the first Insight under
	    //the Perspective, if the previously selected Insight has been moved):
    	lstvInsights.getSelectionModel().selectFirst();
	    for(Insight element: arylInsights){
	    	 if(element.getOrderedLabel(((Perspective) arylPerspectives.get(perspectiveIndex)).getUri()).equals(prevQuestionLabel)){
	    	     lstvInsights.getSelectionModel().select(prevQuestionLabel);
	    	     break;
	    	 }
	    }
 	}
	
//----------------------------------------------------------------------------------------------------
//                                   I n s i g h t   T a b
//----------------------------------------------------------------------------------------------------
	/**   Populates the question text-field with the question from the 
	 * currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateQuestionTextField(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    txtQuestion_Inst.setText(((Insight) arylInsights.get(insightIndex)).getLabel());		   
	}

	/**   Populates the "Display with" dropdown with all playsheets defined
	 * in the Insights KB. Also, pre-selects the playsheet associated with 
	 * the passed-in Insight index.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populatePlaysheetComboBox(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    String playSheetClass = ((Insight) arylInsights.get(insightIndex)).getOutput();
	    cboDisplayWith_Inst.getItems().clear();
 	    for(PlaySheet playsheet: arylPlaySheets){
 	    	cboDisplayWith_Inst.getItems().add(playsheet.getLabel());
 	    	if(playsheet.getViewClass().equals(playSheetClass)){
 	    		cboDisplayWith_Inst.getSelectionModel().select(playsheet.getLabel());	
 	    	}
	    }
	}
	
	/**   Populates the legacy-query check-box with a check from the 
	 * currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateLegacyQueryCheckBox(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    chkLegacyQuery_Inst.setSelected(((Insight) arylInsights.get(insightIndex)).getIsLegacy());		   
	}
	
	/**   Populates the query text-area with Sparql from the currently 
	 * selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.git
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateQueryTextArea(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
		String strQuery = "";
		if(arylInsights.get(insightIndex).getSparql() != null){
			strQuery = arylInsights.get(insightIndex).getSparql();
		}
	    txtaQuery_Inst.setText(strQuery);		   
	}
	
	/**   Populates the Parameter list-view with Parameters under the currently
	 * selected Insight.
	 * 
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateInsightParameterListView(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    ArrayList<Parameter> arylInsightParameters = (ArrayList<Parameter>) ((Insight) arylInsights.get(insightIndex)).getInsightParameters();	   

	    lstvParameter_Inst.getItems().clear();
	    for(Parameter parameter: arylInsightParameters){
	       lstvParameter_Inst.getItems().add(parameter.getLabel());
	    }
	    //Navigate to the previously selected Parameter (or the first Parameter
	    //under the Insight, if the previously selected Parameter has been moved):
    	lstvParameter_Inst.getSelectionModel().selectFirst();
	    for(Parameter element: arylInsightParameters){
	    	 if(element.getLabel().equals(prevParameterLabel)){
	    	     lstvParameter_Inst.getSelectionModel().select(prevParameterLabel);
	    	     break;
	    	 }
	    }	    
	}
	
	/**   Populates the Insight Perspectives list-view with all Perspectives,
	 * and selects the Perspectives having the current Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateInsightPerspectivesListView(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    Insight insight = (Insight) arylInsights.get(insightIndex);
	    arylInsightPerspectives = new ArrayList<String>();
          
	    lstvInsightPerspective_Inst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  	    lstvInsightPerspective_Inst.getItems().clear();

  	    for(Perspective perspective: arylPerspectives){
  	    	String label = perspective.getLabel();
  	    	lstvInsightPerspective_Inst.getItems().add(label);
  	    	for(Insight insight_2: perspective.getInsights()){
  	    		if(insight.getId().equals(insight_2.getId())){
  	    			lstvInsightPerspective_Inst.getSelectionModel().select(label);
  	    			arylInsightPerspectives.add(label);
  	    		}
  	    	}
  	    }
	}
	
	/**   Populates the Description text-area with a description  
	 * from the currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateInsightDescTextArea(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
		String strDescription = "";
		if(arylInsights.get(insightIndex).getDescription() != null){
			strDescription = arylInsights.get(insightIndex).getDescription();
		}
	    txtaInsightDesc_Inst.setText(strDescription);		   
	}
	
	/**   Populates the Creator text-field with the Insight creator  
	 * from the currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateCreatorTextField(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    txtCreator_Inst.setText(((Insight) arylInsights.get(insightIndex)).getCreator());		   
	}
	
	/**   Populates the Created text-field with the Insight create-date  
	 * from the currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateCreatedTextField(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    txtCreated_Inst.setText(((Insight) arylInsights.get(insightIndex)).getCreated());		   
	}
	
	/**   Populates the Modified text-field with the Insight update-date  
	 * from the currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateModifiedTextField(int perspectiveIndex, int insightIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    txtModified_Inst.setText(((Insight) arylInsights.get(insightIndex)).getModified());		   
	}

//----------------------------------------------------------------------------------------------------
//                                   P a r a m e t e r   T a b
//----------------------------------------------------------------------------------------------------

	/**   Populates the "Name" text-field with the "label" property from the currently  
	 * selected Parameter.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 * @param parameterIndex -- (int) Index of currently selected Parameter.
	 */
	private void populateNameTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        txtLabel_parm.setText(arylParameters.get(parameterIndex).getLabel());
	}
	
	/**   Populates the "Variable" text-field with the "variable" property from the currently  
	 * selected Parameter.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 * @param parameterIndex -- (int) Index of currently selected Parameter.
	 */
	private void populateVariableTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        txtVariable_parm.setText(arylParameters.get(parameterIndex).getVariable());
	}
	
	/**   Populates the "Type" text-field with the "valueType" property from the currently  
	 * selected Parameter.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 * @param parameterIndex -- (int) Index of currently selected Parameter.
	 */
	private void populateTypeTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        txtValueType_parm.setText(arylParameters.get(parameterIndex).getValueType());
	}
	
	/**   Populates the "Default Value" text-field with the "defaultValue" property from  
	 * the currently selected Parameter.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 * @param parameterIndex -- (int) Index of currently selected Parameter.
	 */
	private void populateDefaultValueTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        txtDefaultValue_parm.setText(arylParameters.get(parameterIndex).getDefaultValue());
	}

	/**   Populates the "Default Query" text-field with the "defaultQuery" property from  
	 * the currently selected Parameter.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 * @param parameterIndex -- (int) Index of currently selected Parameter.
	 */
	private void populateDefaultQueryTextArea(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        txtaDefaultQuery_parm.setText(arylParameters.get(parameterIndex).getDefaultQuery());
	}
	
//----------------------------------------------------------------------------------------------------
//                      I n s i g h t   M a n a g e r   U t i l i t i e s
//----------------------------------------------------------------------------------------------------

	/**   Prepares a string for use in a dynamic Sparql query, where " and ' are
	 * delimiters. The double-quote, ", is changed to ', and existing single-quotes
	 * are left alone. This utility is also used thoughout the Insight Manager,
	 * where user-editable RDF strings are persisted.
	 *
	 * @param quotedString -- (String) The string containing double and single
	 * quotes.
	 *
	 * @return legalizeQuotes -- (String) The cleaned string, as described above.
	 */
	public String legalizeQuotes( String quotedString ) {
		String strReturnValue = quotedString;

		strReturnValue = strReturnValue.replaceAll( "\"", "'" );

		return strReturnValue;
	}


}
