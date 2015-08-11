package gov.va.semoss.ui.components.insight.manager;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.AbstractSesameEngine;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import java.util.HashMap;


public class  InsightManagerController implements Initializable{
	protected final String ICON_LOCATION = "/images/icons16/";
	
	protected IEngine engine;
	@FXML
	protected TabPane tbpTabbedPane;
	@FXML
	protected ComboBox<Perspective> cboPerspectiveTitle;
	protected ObservableList<Perspective> arylPerspectives;
	protected int intCurPerspectiveIndex;
	@FXML
	protected TextField txtPerspectiveTitle;
	@FXML
	protected TextArea txtaPerspectiveDesc;	
	@FXML
	protected ListView<Insight> lstvInsights;
	protected ObservableList<Insight> arylInsights;
	protected int intCurInsightIndex;
	protected String prevQuestionLabel;
	@FXML
	protected TextField txtQuestion_Inst;
	@FXML
	protected ComboBox<PlaySheet> cboDisplayWith_Inst;
	protected ObservableList<PlaySheet> arylPlaySheets;
	@FXML
	protected TextField txtRendererClass_Inst;
	@FXML
	protected CheckBox chkLegacyQuery_Inst;
	@FXML
	protected TextArea txtaQuery_Inst;
	@FXML
	protected ListView<Parameter> lstvParameter_Inst;
	protected ObservableList<Parameter> arylInsightParameters;
	protected int intCurParameterIndex;
	protected String prevParameterLabel;
	@FXML
	protected ListView<Perspective> lstvInsightPerspective_Inst;
	protected ArrayList<Perspective> arylInsightPerspectives;
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
	protected ComboBox<ParameterType> cboParameterType_parm;
	protected ObservableList<ParameterType> arylParameterTypes;
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
    protected Button btnBuildQuery_Parm;
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
		   arylPerspectives = FXCollections.observableArrayList();
		   arylPlaySheets = FXCollections.observableArrayList();
		   arylInsightParameters = FXCollections.observableArrayList();
		   arylParameterTypes = FXCollections.observableArrayList();
		   
		   //The Insight Perspective list-view must handle multiple selections:
		   lstvInsightPerspective_Inst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		   //Instantiate button-handlers for the "Perspective" tab:
		   ptc = new PerspectiveTabController(this);
		   //Instantiate button-handlers for the "Insight" tab:
		   itc = new InsightTabController(this);
		   //Instantiate button-handlers for the "Parameter" tab:
		   prmtc = new ParameterTabController(this);
		   
		   loadReferencesAndData();
		   
		   //If the "Perspective" changes on the "Perspective" tab, then repopulate its text-field, 
		   //its "Description", and its associated "Insights":
		   //--------------------------------------------------------------------------------------
           cboPerspectiveTitle.valueProperty().addListener(new ChangeListener<Object>() {
		        @Override 
		        public void changed(ObservableValue<?> ov, Object t, Object t1) {
		        	if(t1 != null && arylPerspectives != null && arylPerspectives.size() > 0){
			        	//Get selected "Perspective":
			            Perspective perspective = new Perspective();
			            intCurPerspectiveIndex = 0;
			            for(int i = 0; i < arylPerspectives.size(); i++){
			            	perspective = (Perspective) arylPerspectives.get(i);
			            	if((perspective.getUri().equals(((Perspective) t1).getUri()))){
			            		intCurPerspectiveIndex = i;
			            		break;
			            	}
			            }
						arylInsights = FXCollections.observableArrayList(((Perspective) arylPerspectives.get(intCurPerspectiveIndex)).getInsights());
						 
			            //When the selected "Perspective" changes, re-populate fields on the "Perspective" tab:
			            //-------------------------------------------------------------------------------------
			            populatePerspectiveTitleTextField(intCurPerspectiveIndex);
			            populatePerspectiveDescTextArea(intCurPerspectiveIndex);		        	
			 		    populateInsightListView(intCurPerspectiveIndex);
		        	}
		 		}    
		   });	
           
           //If the "Insights" list-view changes on the "Perspective" tab, then repopulate
           //the "Insight" tab fields:
           //-----------------------------------------------------------------------------
           lstvInsights.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>(){
			  @Override
			  public void changed(ObservableValue<?> ov, Object t, Object t1){
				 if(t1 != null){
				    //Get selected "Insight":
	                Insight insight = new Insight();
	                intCurInsightIndex = 0;
	                for(int i = 0; i < arylInsights.size(); i++){
	              	    insight = (Insight) arylInsights.get(i);
	            	    if((insight.getId()).equals(((Insight) t1).getId())){
	            		   intCurInsightIndex = i;
	            		   break;
	            	    }
	                }
				    arylInsightParameters = FXCollections.observableArrayList(arylInsights.get(intCurInsightIndex).getInsightParameters());

                    //When the selected "Insight" changes, re-populate fields on the "Insight" tab:
	                //-----------------------------------------------------------------------------
	                populateQuestionTextField(intCurPerspectiveIndex, intCurInsightIndex);
	                populateRendererClassTextField(intCurPerspectiveIndex, intCurInsightIndex);
	                populateLegacyQueryCheckBox(intCurPerspectiveIndex, intCurInsightIndex);
	                populateQueryTextArea(intCurPerspectiveIndex, intCurInsightIndex);	
	                populatePlaysheetComboBox(intCurPerspectiveIndex, intCurInsightIndex);
	                populateInsightDescTextArea(intCurPerspectiveIndex, intCurInsightIndex);
	                populateCreatorTextField(intCurPerspectiveIndex, intCurInsightIndex);
	                populateCreatedTextField(intCurPerspectiveIndex, intCurInsightIndex);
	                populateModifiedTextField(intCurPerspectiveIndex, intCurInsightIndex);
	                populateInsightParameterListView(intCurPerspectiveIndex, intCurInsightIndex);
	     	        //Whenever the "Parameters" list-view is reloaded,
	     	        //if it has no elements, disable the "Parameter" tab:
	                if(lstvParameter_Inst.getItems() == null || 
	             	   lstvParameter_Inst.getItems().get(0).getLabel().equals("")){
	     			    tabParameter.setDisable(true);
	     		    //Otherwise, enable the "Parameter" tab only if
	     		    //the "Renderer Class" text-field is empty:
	     		    }else{
	     		        if(txtRendererClass_Inst.getText() == null || 
	     		    	   txtRendererClass_Inst.getText().trim().isEmpty()){
		     			    tabParameter.setDisable(false);
	     		        }else{
	     			        tabParameter.setDisable(true);
	     		        }
	     		    }
	                populateInsightPerspectivesListView(intCurPerspectiveIndex, intCurInsightIndex);
	             }
			  }
		   });
           
           //If the "Parameters" list-view changes on the "Insight" tab, then repopulate
           //the "Parameter" tab fields:
           //---------------------------------------------------------------------------
           lstvParameter_Inst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>(){
			  @Override
			  public void changed(ObservableValue<?> ov, Object t, Object t1){
				 //Get selected "Parameter":
	             Parameter parameter = new Parameter();
	             intCurParameterIndex = 0;
	             for(int i = 0; i < arylInsightParameters.size(); i++){
	            	 parameter = (Parameter) arylInsightParameters.get(i);
	            	 if(parameter.equals((Parameter) t1)){
	            		intCurParameterIndex = i;
	            		break;
	            	 }
	             }
                 //When the selected "Parameter" changes, re-populate fields on the "Parameter" tab:
	             //---------------------------------------------------------------------------------
	             populateNameTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);	          				   
			     populateVariableTextField(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
			     populateParameterTypeComboBox(intCurPerspectiveIndex, intCurInsightIndex, intCurParameterIndex);
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
			      if(strNewText == null || strNewText.trim().isEmpty()){
			    	  cboDisplayWith_Inst.setDisable(false);
			    	  chkLegacyQuery_Inst.setDisable(false);
			    	  txtaQuery_Inst.setDisable(false);
			    	  lstvParameter_Inst.setDisable(false);
			    	  //If the "Renderer Class" text-field becomes empty, enable the 
			    	  //"Parameter" tab only if external Parameters are defined:
			          if(lstvParameter_Inst.getItems() == null || 
			        	 lstvParameter_Inst.getItems().size() == 0 ||	  
			             lstvParameter_Inst.getItems().get(0).getLabel().equals("")){
			     		  tabParameter.setDisable(true);
			     	  }else{
			     		  tabParameter.setDisable(false);
			     	  }
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
		this.prevQuestionLabel = prevQuestionLabel;
		this.prevParameterLabel = prevParameterLabel;
	    //Convert mouse-pointer to a "wait" cursor:
		cboPerspectiveTitle.getScene().setCursor(Cursor.WAIT);
		
		//Define a Task to fetch an ArrayList of Perspectives/Insights:
		Task<ObservableList<Perspective>> getInsightManagerData = new Task<ObservableList<Perspective>>(){
		    @Override 
		    protected ObservableList<Perspective> call() throws Exception {
		    	arylPerspectives = FXCollections.observableArrayList(engine.getInsightManager().getPerspectives());
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
	     		    populatePerspectiveComboBox();
	     		    
	     		    //If passed-in label is not null, then navigate to that Perspective:
	     		    if(cboPerspectiveTitle.getItems().size() != 0 && 
	     		       perspectiveLabel != null && perspectiveLabel.trim().equals("") == false){
	     		    	cboPerspectiveTitle.getSelectionModel().select(arylPerspectives.get(intCurPerspectiveIndex));

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
	 * combo-box and the Parameter tab's "Value Type" combo-box: Designed to be run once when 
	 * the Insight Manager is loaded initially.
	 */
	private void loadReferencesAndData(){		
	    //Convert mouse-pointer to a "wait" cursor:
		tbpTabbedPane.getScene().setCursor(Cursor.WAIT);
		
		//Define a Task to fetch an ArrayList of PlaySheets:
		Task<ObservableList<PlaySheet>> getPlaySheetData = new Task<ObservableList<PlaySheet>>(){
		    @Override 
		    protected ObservableList<PlaySheet> call() throws Exception {
		    	arylPlaySheets = FXCollections.observableArrayList(engine.getInsightManager().getPlaySheets());		    	
		        return arylPlaySheets;
		    }
		};
		//Define a Task to fetch an ArrayList of Concept Value Types:
		Task<ObservableList<ParameterType>> getParameterTypeData = new Task<ObservableList<ParameterType>>(){
		    @Override 
		    protected ObservableList<ParameterType> call() throws Exception {
		    	arylParameterTypes = FXCollections.observableArrayList(engine.getInsightManager().getParameterTypes());		    	
		        return arylParameterTypes;
		    }
		};
	    //Define a listener to load Insight Manager data when Task completes,
		//but only if the Parameter Types have been loaded:
		getPlaySheetData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	if(arylParameterTypes.size() > 0){
	            	   //Restore mouse-pointer:
	            	   tbpTabbedPane.getScene().setCursor(Cursor.DEFAULT);	            	
	            	   //Load Insight Manager data:
	            	   loadData(null, null, null);
	            	}
	      	    }    	    
	        }
	     });
	    //Define a listener to load Insight Manager data when Task completes,
		//but only if the PlaySheets have been loaded:
		getParameterTypeData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	if(arylPlaySheets.size() > 0){
	            	   //Restore mouse-pointer:
	            	   tbpTabbedPane.getScene().setCursor(Cursor.DEFAULT);	            	
	            	   //Load Insight Manager data:
	            	   loadData(null, null, null);
	            	}
	      	    }    	    
	        }
	     });
		 //Run the Tasks on a separate Threads:
		 new Thread(getPlaySheetData).start();
		 new Thread(getParameterTypeData).start();
	}
//----------------------------------------------------------------------------------------------------
//	                            P e r s p e c t i v e   T a b
//----------------------------------------------------------------------------------------------------
	/**   Populates the Perspective combo-box on the "Perspective" tab.
	 */
	protected void populatePerspectiveComboBox(){
  	    cboPerspectiveTitle.setItems(arylPerspectives);
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
		Perspective perspective = (Perspective) arylPerspectives.get(perspectiveIndex);
  	    lstvInsights.setItems(arylInsights);

  	    //Cell Factory for "Insight" list-view to display PlaySheet icons
	    //to the left of Insight labels:
	    lstvInsights.setCellFactory(listView -> new ListCell<Insight>() {
	        private ImageView imageView = new ImageView();
	        
	        @Override
	        public void updateItem(Insight item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setText(null);
	                setGraphic(null);
	            }else{
	                setText(item.toString());
	            	try{
	                   Image image = new Image(getInsightIcon(item.getOrderedLabel(perspective.getUri())));
	                   imageView.setImage(image);
	                   setGraphic(imageView);
	            	}catch(Exception e){
		               setGraphic(null);
	            	}
	            }
	        }
	        
	        /**   Class-initializer to define drag-and-drop operations.
	         */
	        {
	            ListCell thisCell = this;

	            setOnDragDetected(event ->{
	                if(getItem() == null){
	                    return;
	                }
	                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
	                ClipboardContent content = new ClipboardContent();
	                content.putString(getText());
	                dragboard.setDragView(
	                	new Image(getInsightIcon(getItem().getOrderedLabel(perspective.getUri())))
	                );
	                dragboard.setContent(content);
	                event.consume();
	            });

	            setOnDragOver(event -> {
	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
	                    event.acceptTransferModes(TransferMode.MOVE);
	                }
	                event.consume();
	            });

	            setOnDragEntered(event -> {
	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
	                    setOpacity(0.3);
	                }
	            });

	            setOnDragExited(event -> {
	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
	                    setOpacity(1);
	                }
	            });

	            setOnDragDropped(event -> {
	                if(getItem() == null){
	                    return;
	                }
	                Dragboard dragboard = event.getDragboard();
	                boolean success = false;

	                if (dragboard.hasString()) {
	                    int thisIdx = arylInsights.indexOf(getItem());
	                    int draggedIdx = 0;
	                    for(Insight item: arylInsights){
	                    	if(item.getOrderedLabel(perspective.getUri()).equals(dragboard.getString())){
	                    		break;
	                    	}
	                    	draggedIdx++;
	                    }
                        moveInsight(draggedIdx, thisIdx, perspective.getUri().toString(), 
                           arylInsights.get(draggedIdx));

	                    success = true;
	                }
	                event.setDropCompleted(success);
	                event.consume();
	            });

	            setOnDragDone(DragEvent::consume);

	        }//End Class-initializer.

	        /**   Moves a dragged Insight to a new position in the list-view, and either pushes 
	         * other Insights back or forward, depending upon the drag direction
	         * 
	         * @param startIdx -- (int) Origin index of dragged Insight in the list-view's
	         *     ObservableList.
	         *     
	         * @param endIdx -- (int) Destination index of dragged Insight in the list-view's
	         *     ObservableList.
	         *     
	         * @param strPerspectiveUri -- (String) URI string of the Perspective that contains
	         *     the visible Insights.
	         *     
	         * @param insight -- (Insight) The Insight to be moved.
	         */
	        private void moveInsight(int startIdx, int endIdx, String strPerspectiveUri, Insight insight){
	        	if(startIdx < endIdx){
	        	   for(int i = startIdx; i < endIdx; i++){
	        		   arylInsights.set(i, arylInsights.get(i + 1));
	        	   }
	        	   arylInsights.set(endIdx, insight);
	        	}
	        	if(startIdx > endIdx){
	        	   for(int i = startIdx; i > endIdx; i--){
	        		   arylInsights.set(i, arylInsights.get(i - 1));
	        	   }
		           arylInsights.set(endIdx, insight);
	        	}
	        	//Reorder Insights and redisplay:
	        	for(int i = 0; i < arylInsights.size(); i++){
	        		arylInsights.get(i).setOrder(strPerspectiveUri, i + 1);
	        	}
	        	lstvInsights.setItems(null);
	        	lstvInsights.setItems(arylInsights);
	        }
	        
	    });//End setCellFactory.
	    
	    //Navigate to the previously selected Insight (or the first Insight under
	    //the Perspective, if the previously selected Insight has been moved):
    	lstvInsights.getSelectionModel().selectFirst();
	    for(Insight element: arylInsights){
	    	 if(element.getOrderedLabel(perspective.getUri()).equals(prevQuestionLabel)){
	    	     lstvInsights.getSelectionModel().select(element);
	    	     break;
	    	 }
	    }
 	}
	
	/**   Returns a PlaySheet icon for the passed-in Insight label. The array-list
	 * of Insights is consulted along with the array-list of PlaySheets. The base
	 * path to all icons is defined at the top of this class, in "ICON_LOCATION".
	 * 
	 * @param strInsightLabel -- (String) The label of the Insight for which an icon
	 *    is required.
	 *    
	 * @return getInsightIcon -- (String) The file-path to the icon, described above.
	 */
	private String getInsightIcon(String strInsightLabel){
		String strReturnValue = "";
		
		for(Insight insight: arylInsights){
			if(insight.getOrderedLabel(((Perspective) arylPerspectives.get(intCurPerspectiveIndex)).getUri()).equals(strInsightLabel)){
			   for(PlaySheet playsheet: arylPlaySheets){
				   if(insight.getOutput().equals(playsheet.getViewClass())){
					  if(playsheet.getIcon() != null){
					     strReturnValue = ICON_LOCATION + playsheet.getIcon();
					  }
					  break;
				   }
			   }
			   break;
			}
		}
		return strReturnValue;
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
	    cboDisplayWith_Inst.setItems(arylPlaySheets);
 	    for(PlaySheet playsheet: arylPlaySheets){
 	    	if(playsheet.getViewClass().equals(playSheetClass)){
 	    		cboDisplayWith_Inst.getSelectionModel().select(playsheet);	
 	    	}
	    }
        //Cell Factory for "Display with" combo-box list-view to display 
	    //PlaySheet icons to the left of Playsheet labels:
 	    cboDisplayWith_Inst.setCellFactory(ListView -> new ListCell<PlaySheet>() {
	        private ImageView imageView = new ImageView();
	        @Override
	        public void updateItem(PlaySheet item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setText(null);
	                setGraphic(null);
	            }else{
                    setText(item.toString());
                    try{
	                   Image image = new Image(getPlaySheetIcon(item.toString()));
	                   imageView.setImage(image);
	                   setGraphic(imageView);
                    }catch(Exception e){
                       setGraphic(null);
                    }
	            } 
	        }
	    });	
 	    //Necessary to display an icon on the button area of the combo-box:
 	    cboDisplayWith_Inst.setButtonCell(cboDisplayWith_Inst.getCellFactory().call(null));
	}
	
	/**   Returns a PlaySheet icon for the passed-in PlaySheet label. The array-list
	 * of PlaySheets is consulted. The base path to all icons is defined at the top 
	 * of this class, in "ICON_LOCATION".
	 * 
	 * @param strPlaySheetLabel -- (String) The label of the PlaySheet for which an 
	 *    icon is required.
	 *    
	 * @return getPlaySheetIcon -- (String) The file-path to the icon, described above.
	 */
	private String getPlaySheetIcon(String strPlaySheetLabel){
	   String strReturnValue = "";
	   
	   for(PlaySheet playsheet: arylPlaySheets){
		   if(playsheet.getLabel().equals(strPlaySheetLabel)){
			  if(playsheet.getIcon() != null){
			     strReturnValue = ICON_LOCATION + playsheet.getIcon();
			  }
			  break;
		   }
	   }
	   return strReturnValue;
	}

	/**   Populates the Renderer-Class text-field from the 
	 * currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateRendererClassTextField(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    txtRendererClass_Inst.setText(((Insight) arylInsights.get(insightIndex)).getRendererClass());		   
	}
	
	/**   Populates the legacy-query check-box with a check from the 
	 * currently selected Insight.
	 * 
	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
	 * @param insightIndex -- (int) Index of currently selected Insight.
	 */
	private void populateLegacyQueryCheckBox(int perspectiveIndex, int insightIndex){
  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
	    chkLegacyQuery_Inst.setSelected(((Insight) arylInsights.get(insightIndex)).isLegacy());		   
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
		lstvParameter_Inst.setItems(arylInsightParameters);
	    //Navigate to the previously selected Parameter (or the first Parameter
	    //under the Insight, if the previously selected Parameter has been moved):
    	lstvParameter_Inst.getSelectionModel().selectFirst();
	    for(Parameter element: arylInsightParameters){
	    	 if(element.getLabel().equals(prevParameterLabel)){
	    	     lstvParameter_Inst.getSelectionModel().select(element);
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
	    arylInsightPerspectives = new ArrayList<Perspective>();
          
	    lstvInsightPerspective_Inst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  	    lstvInsightPerspective_Inst.setItems(arylPerspectives);

  	    lstvInsightPerspective_Inst.getSelectionModel().clearSelection();
  	    for(Perspective perspective: arylPerspectives){
  	    	for(Insight insight_2: perspective.getInsights()){
  	    		if(insight.getId().equals(insight_2.getId())){
  	    			lstvInsightPerspective_Inst.getSelectionModel().select(perspective);
  	    			arylInsightPerspectives.add(perspective);
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
	
	/**   Populates the "Parameter Type" dropdown with all Concept Types defined
	 * in the Main KB, and pre-selects the Type associated with the current Parameter.
	 */
	private void populateParameterTypeComboBox(int perspectiveIndex, int insightIndex, int parameterIndex){
 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
        Insight insight = arylInsights.get(insightIndex);
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
        Parameter parameter = arylParameters.get(parameterIndex);
	    cboParameterType_parm.setItems(arylParameterTypes);
	    
	    boolean boolSelected = false;
	    for(ParameterType valueType: cboParameterType_parm.getItems()){
	    	if(parameter.getParameterType().equals(valueType.getParameterClass())){
	    		cboParameterType_parm.getSelectionModel().select(valueType);
	    		boolSelected = true;
	    		break;
	    	}
	    }
	    //If nothing has been selected, the select the "(Unselected)" item:
	    if(boolSelected == false){
    		cboParameterType_parm.getSelectionModel().selectFirst();
	    }
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
	
	/**   If the passed-in query is invalid or cannot be executed, this method
	 * displays a dialog, indicating the problem, and asks the user if he would
	 * like to save the query anyway. Returns true if the query is valid and can
	 * be executed, or if the user clicks "Ok" on the dialog. If the user cancels
	 * out of the dialog, or closes it, false is returned.
	 * 
	 * @param strQuery -- (String) A query to test.
	 * 
	 * @return queryValidationDialog -- (boolean) Described above.
	 */
	public boolean queryValidationDialog(String strQuery){
		boolean boolReturnValue = true;
		String exception = "";
		
		QueryExecutorAdapter<String> querySelect = new QueryExecutorAdapter<String>() {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
               //Nothing is done here.
			}
		};		
		try{
			if(strQuery.toUpperCase().startsWith("SELECT")){
			   querySelect.setSparql(strQuery);
			   engine.query(querySelect);
			   
			}else if(strQuery.toUpperCase().startsWith("CONSTRUCT")){
				Repository repo = engine.getInsightManager().getRepository();
				RepositoryConnection rc = null;
			    rc = repo.getConnection();
                strQuery = AbstractSesameEngine.processNamespaces(strQuery, new HashMap<>());
				rc.prepareGraphQuery(QueryLanguage.SPARQL, strQuery);
			}else{
				exception += "   The query must begin with SELECT or CONSTRUCT.\n";
			}
		}catch(MalformedQueryException e){
			exception += "   The query is malformed.\n";			
		} catch (Exception e) {
			exception += "   The query cannot be evaluated as written.\n";
		}
		
		if(exception.equals("") == false){
			int msgResponse = GuiUtility.showWarningOkCancel("The following problems exist with your query:\n" +
		       exception + "Would you still like to save it?");

			if(msgResponse == 0){
				boolReturnValue = true;
			}else{
		        boolReturnValue = false;
			}
		}		
		return boolReturnValue;
	}


}
