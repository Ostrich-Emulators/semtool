package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.ExecuteQueryProcessor;
import gov.va.semoss.ui.components.ParamComboBox;
import gov.va.semoss.ui.components.SelectDatabasePanel;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.helpers.NonLegacyQueryBuilder;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;

import java.awt.Component;
import java.awt.Event;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListCell;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

public class InsightEditorController implements Initializable{
	TreeView<Object> treevPerspectives;
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
		itemURI = insight.getId();
		
		//Insight Question:
		//-----------------
		txtQuestion_Inst.setText(insight.getLabel());
		txtQuestion_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
			insight.setLabel(newValue);
			//This hack is necessary to update the TreeItem:
			itemSelected.getParent().setExpanded(false);
			itemSelected.getParent().setExpanded(true);
			treevPerspectives.getSelectionModel().select(itemSelected);
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
 	    });
		
		//Legacy Query:
		//-------------
		chkLegacyQuery_Inst.setSelected(insight.isLegacy());;
		chkLegacyQuery_Inst.selectedProperty().addListener((observable, oldValue, newValue) -> {
			insight.setLegacy(newValue);
		});

		//Insight Query:
		//--------------
		txtaQuery_Inst.setText(insight.getSparql());
		txtaQuery_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
			insight.setSparql(newValue);
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

	/**
	 * 
	 * @param event
	 */
	private void handleTestQuery(ActionEvent event){
		ExecuteQueryProcessor insightAction = new InsightAction();
		insightAction.actionPerformed(new java.awt.event.ActionEvent(event.getSource(), 1, null));
	}

	/**   Provides methods to test an Insight Query and its Parameter settings
	 * in the Display Pane.
	 * 
	 * @author Thomas
	 */
	private class InsightAction extends ExecuteQueryProcessor {
        Perspective perspective;
        Insight insight;
		
		public InsightAction() {
			super( "Test Query" );
			perspective = (Perspective) treevPerspectives.getSelectionModel().getSelectedItem().getParent().getValue();
			insight = (Insight) treevPerspectives.getSelectionModel().getSelectedItem().getValue();
		}

		@Override
		protected String getTitle() {
			return perspective.getLabel() + "-Insight-" + insight.getOrder();
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
		protected Class<? extends PlaySheetCentralComponent> getPlaySheetCentralComponent() throws ClassNotFoundException {
			String output = insight.getOutput();
			return (Class<PlaySheetCentralComponent>) Class.forName(output);
		}

		@Override
		protected IEngine getEngine() {
			return DIHelper.getInstance().getRdfEngine();
		}

		@Override
		protected boolean isAppending() {
			return false;
		}
	}
	private Map<String, String> getParameterValues() {
		Map<String, String> paramHash = new HashMap<>();

		return paramHash;
	}

}
