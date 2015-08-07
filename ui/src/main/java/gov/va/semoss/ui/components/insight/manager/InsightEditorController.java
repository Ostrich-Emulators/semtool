package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.PlaySheet;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListCell;

import org.openrdf.model.URI;

public class InsightEditorController implements Initializable{
	public URI itemURI;
	@FXML
	protected TextField txtQuestion_Inst;
	@FXML
	protected ComboBox<PlaySheet> cboDisplayWith_Inst;
	@FXML
	protected TextField txtRendererClass_Inst;
	@FXML
	protected CheckBox chkLegacyQuery_Inst;
	@FXML
	protected TextArea txtaQuery_Inst;
	@FXML
	protected TextArea txtaInsightDesc_Inst;
	@FXML
	protected TextField txtCreator_Inst;
	@FXML
	protected TextField txtCreated_Inst;
	@FXML
	protected TextField txtModified_Inst;
	
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
 	    });

		//Renderer Class:
		//---------------
	    txtRendererClass_Inst.setText(insight.getRendererClass());
        //Initially check the "Renderer Class" text-field on the "Insight" tab. 
        //If it is not empty, then disable the various query-related controls:
        if(txtRendererClass_Inst.getText() == null || txtRendererClass_Inst.getText().trim().isEmpty()){
   	       cboDisplayWith_Inst.setDisable(false);
   	       chkLegacyQuery_Inst.setDisable(false);
   	       txtaQuery_Inst.setDisable(false);
        }else{
   	       cboDisplayWith_Inst.setDisable(true);
   	       chkLegacyQuery_Inst.setDisable(true);
   	       txtaQuery_Inst.setDisable(true);  	       
        }
        txtRendererClass_Inst.textProperty().addListener((observable, oldValue, newValue) -> {
        	insight.setRendererClass(newValue);
            //If the "Renderer Class" text-field changes, and it is not empty, 
            //then disable query-related controls. Otherwise, enable them:
        	if(newValue.trim().isEmpty()){
		       cboDisplayWith_Inst.setDisable(false);
		       chkLegacyQuery_Inst.setDisable(false);
		       txtaQuery_Inst.setDisable(false);
		    }else{
		       cboDisplayWith_Inst.setDisable(true);
		       chkLegacyQuery_Inst.setDisable(true);
		       txtaQuery_Inst.setDisable(true);
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

}
