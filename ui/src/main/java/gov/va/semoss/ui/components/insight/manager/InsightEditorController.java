package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.AbstractSesameEngine;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.ui.components.ExecuteQueryProcessor;
import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.ui.components.ParamPanel;
import gov.va.semoss.ui.components.SelectDatabasePanel;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.helpers.NonLegacyQueryBuilder;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;

import java.awt.Component;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

public class InsightEditorController implements Initializable{
	protected TreeView<Object> treevPerspectives;
    protected ExecuteQueryProcessor insightAction;
	public URI itemURI;
	
	@FXML
	protected TextField txtQuestion_Inst;
	@FXML
	protected ComboBox<PlaySheet> cboDisplayWith_Inst;
	@FXML
	protected CheckBox chkLegacyQuery_Inst;
	@FXML
	protected TextArea txtaQuery_Inst;
	@FXML
	protected Button btnTestQuery_Inst;
	@FXML
	protected TextArea txtaInsightDesc_Inst;
	@FXML
	protected TextField txtCreator_Inst;
	@FXML
	protected TextField txtCreated_Inst;
	@FXML
	protected TextField txtModified_Inst;

	private static final Logger log = Logger.getLogger( SelectDatabasePanel.class );
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
	
	/**   Loads the "Insight" editor with Insight Question, Playsheets, Renderer Class,
	 * Query, Description, and identification fields. Also establishes change-handlers 
	 * on the editor fields.
	 * 
	 * @param treevPerspectives -- (TreeView<Object>) The InsightManager's tree-view.
	 * 
	 * @param obsPlaySheets -- (ObservableList<PlaySheet>) A list of available PlaySheets.
	 */
	public void setData(TreeView<Object> treevPerspectives, ObservableList<PlaySheet> obsPlaySheets){
		this.treevPerspectives = treevPerspectives;
		TreeItem<Object> itemSelected = treevPerspectives.getSelectionModel().getSelectedItem();
		Insight insight = (Insight) itemSelected.getValue();
		List<Insight> arylInsights = ((Perspective) itemSelected.getParent().getValue()).getInsights();
		int indexInsight = arylInsights.indexOf(insight);
		itemURI = insight.getId();
		
		//Insight Question:
		//-----------------
		txtQuestion_Inst.setText(insight.getLabel());
		txtQuestion_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
			insight.setLabel(newValue);
			//Update Perspective's Insight collection:
			arylInsights.set(indexInsight, insight);
			//This hack is necessary to update the TreeItem:
			itemSelected.getParent().setExpanded(false);
			itemSelected.getParent().setExpanded(true);
			treevPerspectives.getSelectionModel().select(itemSelected);
			setCreatorModifiedFields(insight);
		});
		
		//"Display with" Combo-Box:
		//-------------------------
	    String playSheetClass = insight.getOutput();
	    cboDisplayWith_Inst.setItems(obsPlaySheets);
 	    for(PlaySheet playsheet: obsPlaySheets){
 	    	if(playsheet.getViewClass().equals(playSheetClass)){
 	    		cboDisplayWith_Inst.getSelectionModel().select(playsheet);	
 	    		
 				//Disable "Query" text-area and "Legacy Query" check-box only 
 				//if the underlying playsheet requires no query:
 	    		try {
					if(((Class<PlaySheetCentralComponent>) Class.forName(playsheet.getViewClass())).newInstance().requiresQuery()){
						txtaQuery_Inst.setDisable(false);
						chkLegacyQuery_Inst.setDisable(false);
					}else{
						txtaQuery_Inst.setDisable(true);
						chkLegacyQuery_Inst.setDisable(true);
					}
				} catch (Exception e) {
					txtaQuery_Inst.setDisable(false);
					chkLegacyQuery_Inst.setDisable(false);
				}
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
	                   Image image = new Image(getPlaySheetIcon(item.toString(), obsPlaySheets));
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
	
 	    cboDisplayWith_Inst.valueProperty().addListener((observable, oldValue, newValue) -> {
 	    	try{
               Image image = new Image(getPlaySheetIcon(newValue.getLabel(), obsPlaySheets));
               ImageView imageView = new ImageView(image);
 		       insight.setOutput(newValue.getViewClass());  
 		       itemSelected.setGraphic(imageView);
 	    	}catch(Exception e){
 	    	   itemSelected.setGraphic(null);
 	    	}
			//Update Perspective's Insight collection:
			arylInsights.set(indexInsight, insight);
			//This hack is necessary to update the TreeItem's icon:
			itemSelected.getParent().setExpanded(false);
			itemSelected.getParent().setExpanded(true);
			treevPerspectives.getSelectionModel().select(itemSelected);

			//Disable "Query" text-area and "Legacy Query" check-box only 
			//if the underlying playsheet requires no query:
	    	try {
				if(((Class<PlaySheetCentralComponent>) Class.forName(newValue.getViewClass())).newInstance().requiresQuery()){
					txtaQuery_Inst.setDisable(false);
					chkLegacyQuery_Inst.setDisable(false);
				}else{
					txtaQuery_Inst.setDisable(true);
					chkLegacyQuery_Inst.setDisable(true);
				}
			} catch (Exception e) {
				txtaQuery_Inst.setDisable(false);
				chkLegacyQuery_Inst.setDisable(false);
			}
			setCreatorModifiedFields(insight);
 	    });
		
		//Legacy Query check-box:
		//-----------------------
		chkLegacyQuery_Inst.setSelected(insight.isLegacy());;
		chkLegacyQuery_Inst.selectedProperty().addListener((observable, oldValue, newValue) -> {
			insight.setLegacy(newValue);
			//Update Perspective's Insight collection:
			arylInsights.set(indexInsight, insight);
			setCreatorModifiedFields(insight);
		});

		//Insight Query:
		//--------------
		txtaQuery_Inst.setText(insight.getSparql());
		txtaQuery_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
			insight.setSparql(newValue);
			//Update Perspective's Insight collection:
			arylInsights.set(indexInsight, insight);
			setCreatorModifiedFields(insight);
		});

		//Build Test Query Button:
 	    //------------------------
 	    btnTestQuery_Inst.setOnAction(this::handleTestQuery);
		btnTestQuery_Inst.setTooltip(new Tooltip("Test Insight Query and its Parameters."));

		//Insight Description:
		//--------------------
		txtaInsightDesc_Inst.setText(insight.getDescription());
		txtaInsightDesc_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
			insight.setDescription(newValue);
			//Update Perspective's Insight collection:
			arylInsights.set(indexInsight, insight);
			setCreatorModifiedFields(insight);
		});
		
		//Author (read-only):
		//-------------------
		txtCreator_Inst.setText(insight.getCreator());
		
		//Date Created (read-only):
		//-------------------------
		txtCreated_Inst.setText(insight.getCreated());
		
		//Last Updated (read-only):
		//-------------------------
		txtModified_Inst.setText(insight.getModified());
        
	}//End "setData()".

	/**   Updates the "Creator" and "Modified" fields of the Insight.
	 * Called whenever an Insight screen-field is modified.
	 * 
	 * @param insight -- (Insight).
	 */
	private void setCreatorModifiedFields(Insight insight){
		IEngine engine = DIHelper.getInstance().getRdfEngine();	    
		String now = new Date().toString();
		insight.setCreator(engine.getWriteableInsightManager().userInfoFromToolPreferences(insight.getCreator()));
        insight.setModified(now);	    
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
	private String getPlaySheetIcon(String strPlaySheetLabel, ObservableList<PlaySheet> obsPlaySheets){
	   String strReturnValue = "";
	   
	   for(PlaySheet playsheet: obsPlaySheets){
		   if(playsheet.getLabel().equals(strPlaySheetLabel)){
			  if(playsheet.getIcon() != null){
			     strReturnValue = InsightManagerController_2.ICON_LOCATION + playsheet.getIcon();
			  }
			  break;
		   }
	   }
	   return strReturnValue;
	}

	/**   Click-handler for the "Test Query" button. First validates all queries related to the
	 * Insight, and displays an warning-message popup if the Insight's SPARQL or that of the Parameters
	 * is invalid. If the queries are valid, then an attempt is made to render the Insight. At this
	 * time, a popup may be displayed, showing Parameter dropdowns, from which selections can be
	 * made.
	 * 
	 * @param event -- (ActionEvent).
	 */
	private void handleTestQuery(ActionEvent event){
		
		Stage stage = new Stage();
	    final Label lblHeading = new Label();
	    final SwingNode swingNode = new SwingNode();
	    final Label lblSpacer = new Label();
	    final Button btnTestQuery = new Button();
        final Insight insight = (Insight) treevPerspectives.getSelectionModel().getSelectedItem().getValue();
        
        //If SPARQL (Insight or Parameters) is invalid, display an warning popup
        //Do no further processing is "Cancel" is clicked:
        if(!queryValidationDialog(insight)){
        	return;
        }
        
        //If the Insight has Parameters, then display a Swing dialog to select them
        //and test the query:
        //--------------------------------------------------------------------------
        if(!insight.getParameters().isEmpty()){
	        //Insight Heading:
	        lblHeading.setText("TEST \"" + insight.getLabel() + "\":");
	        lblHeading.setStyle("-fx-font-weight: bold");
	        lblHeading.setPrefWidth(450);
	        lblHeading.setMaxWidth(1000);
	        lblHeading.setPrefHeight(60);
	        lblHeading.setMaxHeight(120);
	        lblHeading.setWrapText(true);
		    //Insight Parameter dropdowns:
		    ParamPanel currentParamPanel = new ParamPanel();
		    SwingUtilities.invokeLater(new Runnable() {
		        @Override
		        public void run() {
		    	   currentParamPanel.setInsight(insight);
		    	   currentParamPanel.paintParam();
		           swingNode.setContent(currentParamPanel);
		        }
		    });
	        swingNode.autosize();
			//Spacer:
	        lblSpacer.setPrefHeight(5);
			//"Test Query" button:
			btnTestQuery.setText("Test Query");
			btnTestQuery.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					insightAction = new InsightAction(currentParamPanel);
					insightAction.actionPerformed(new java.awt.event.ActionEvent(event.getSource(), 1, null));
					stage.close();
				}
			});			
			//Layout container:
		    VBox vbox = new VBox();
		    vbox.setPadding(new Insets(5,5,5,5));
		    vbox.setBackground(Background.EMPTY);
		    vbox.getChildren().addAll(lblHeading, swingNode, lblSpacer, btnTestQuery);
		    vbox.setAlignment(Pos.TOP_CENTER);
		    
		    //Pop-up:
		    stage.setScene(new Scene(vbox, 450, 200));
		    stage.setTitle("Select Parameters and Test Query");
		    stage.initModality(Modality.APPLICATION_MODAL);
		    stage.initOwner(stage.getOwner());
		    stage.widthProperty().addListener(new ChangeListener<Number>() {
		        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		            currentParamPanel.repaint();
		        }
		    });
		    stage.heightProperty().addListener(new ChangeListener<Number>() {
		        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		        	currentParamPanel.repaint();
		        }
		    });
		    stage.showAndWait();
        
		//If the Insight has no Parameters, then immediately test the query:
		//------------------------------------------------------------------
        }else{
		    insightAction = new InsightAction(null);
			insightAction.actionPerformed(new java.awt.event.ActionEvent(event.getSource(), 1, null));
        }
	}
	
	/**   Examines all SPARQL queries related to the Insight passed in (Main query
	 * and Parameter queries). If any are found to be invalid, then an warning dialog
	 * is displayed, and false is returned if "Cancel" is clicked; true if "Ok" is clicked.
	 * 
	 * @param insight -- (Insight) The Insight whose SPARQL (including Parameters)
	 *    must be validated.
	 *    
	 * @return queryValidationDialog -- (boolean) Described above.
	 */
	private boolean queryValidationDialog(Insight insight){
		boolean boolReturnValue = true;
		String strErrorMsg = "The following problems were found in your SPARQL:\n";

		String strQueryInsight = insight.getSparql();
		StringBuilder strbErrMsg_Insight = new StringBuilder("--Insight:\n");
		if(!doQueryValidation(strQueryInsight, strbErrMsg_Insight)){
			strErrorMsg += strbErrMsg_Insight.toString();
			boolReturnValue = false;
		}		
		if(!insight.getInsightParameters().isEmpty() && 
		   insight.getInsightParameters().iterator().next().getDefaultQuery() != ""){
		   for(Parameter parameter: insight.getInsightParameters()){
		  	   String strQueryParameter = parameter.getDefaultQuery();
			   StringBuilder strbErrMsg_Parameter = new StringBuilder("--Parameter, \"" + 
			      parameter.getLabel() + "\":\n");
			   if(!doQueryValidation(strQueryParameter, strbErrMsg_Parameter)){
				   strErrorMsg += strbErrMsg_Parameter.toString();
				   boolReturnValue = false;
			   }
		   }
		}
		if(!boolReturnValue){
			strErrorMsg += "\n(Advisable to Cancel. But legacy queries are reported as invalid.)";
			if(GuiUtility.showWarningOkCancel(strErrorMsg) == 0){
				boolReturnValue = true;
			}else{
				boolReturnValue = false;
			}
		}
		return boolReturnValue;
	}
	
	/**   If the passed-in query is invalid or cannot be executed, this method
	 * returns false along with an error-message by side-effect. Returns true 
	 * if the query is valid and can be executed.
	 * 
	 * @param strQuery -- (String) A query to test.
	 * 
	 * @param strErrorMessage -- (StringBuilder) An error message returned by
	 *    side-effect. It can be passed in, and new messages will be appended
	 *    to it. This variable has no effect upon the return value of this
	 *    function.
	 * 
	 * @return doQueryValidation -- (boolean) Described above.
	 */
	private boolean doQueryValidation(String strQuery, StringBuilder strbErrorMsg){
		boolean boolReturnValue = true;
		IEngine engine = DIHelper.getInstance().getRdfEngine();
		
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
				boolReturnValue = false;
				strbErrorMsg.append("   The query must begin with SELECT or CONSTRUCT.\n");
			}
		}catch(MalformedQueryException e){
			boolReturnValue = false;
			strbErrorMsg.append("   The query is malformed.\n");			
		} catch (Exception e) {
			boolReturnValue = false;
			strbErrorMsg.append("   The query cannot be evaluated as written.\n");
		}
		return boolReturnValue;
	}

	/**   Provides methods to test an Insight Query and its Parameter settings
	 * in the Display Pane.
	 * 
	 * @author Thomas
	 */
	private class InsightAction extends ExecuteQueryProcessor {
		private static final long serialVersionUID = -7336504206497572431L;
		Perspective perspective;
        Insight insight;
        ParamPanel currentParamPanel;
		
		public InsightAction(ParamPanel currentParamPanel) {
			super( "Test Query" );
			this.currentParamPanel = currentParamPanel;
			perspective = (Perspective) treevPerspectives.getSelectionModel().getSelectedItem().getParent().getValue();
			insight = (Insight) treevPerspectives.getSelectionModel().getSelectedItem().getValue();
		}

		@Override
		protected String getTitle() {
			return perspective.getLabel() + "-Insight-" + perspective.indexOf( insight );
		}

		@Override
		protected String getFrameTitle() {
			return "TEST OF  \"" + insight.getLabel() + "\"";
		}

		@Override
		protected String getQuery() {
			String sparql = Utility.normalizeParam(insight.getSparql());

			Map<String, String> paramHash = getParameterValues();

			log.debug( "SPARQL " + sparql );
			if ( insight.isLegacy() ) {
				sparql = Utility.fillParam( sparql, paramHash );
			}
			else {
				sparql = NonLegacyQueryBuilder.buildNonLegacyQuery( sparql, paramHash );
			}
			return sparql;
		}

		@Override
		protected Class<? extends IPlaySheet> getPlaySheet() {
			String output = insight.getOutput();
			try{
				return (Class<IPlaySheet>) Class.forName( output );
			}
			catch( ClassNotFoundException n ){
				log.error( n, n);
			}
			
			return null;
		}

		@Override
		protected IEngine getEngine() {
			return DIHelper.getInstance().getRdfEngine();
		}

		@Override
		protected boolean isAppending() {
			return false;
		}
		
		private Map<String, String> getParameterValues() {
			Map<String, String> paramHash = new HashMap<>();
			if ( null != currentParamPanel ) {
				Component[] fields = currentParamPanel.getComponents();

				for ( Component field : fields ) {
					if ( field instanceof ParamComboBox ) {
						String fieldName = ( (ParamComboBox) field ).getParamName();
						String fieldValue = ( (ParamComboBox) field ).getSelectedItem() + "";
						String uriFill = ( (ParamComboBox) field ).getURI( fieldValue );
						if ( uriFill == null ) {
							uriFill = fieldValue;
						}
						paramHash.put( fieldName, uriFill );
					}
				}
			}
			return paramHash;
		}
	}//End inner class, "InsightAction".

}//End class, "InsightEditorController".
