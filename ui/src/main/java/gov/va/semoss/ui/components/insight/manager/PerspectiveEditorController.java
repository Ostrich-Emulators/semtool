package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Perspective;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.openrdf.model.URI;

public class PerspectiveEditorController implements Initializable{
	public URI itemURI;
	@FXML
	protected TextField txtPerspectiveTitle;
	@FXML
	protected TextArea txtaPerspectiveDesc;	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
	
	/**   Loads the "Perspective" editor with a Title and Description. Also establishes
	 * change-handlers on the editor fields.
	 * 
	 * @param treevPerspectives -- (TreeView<Object>) The InsightManager's tree-view.
	 */
	public void setData(TreeView<Object> treevPerspectives){
		TreeItem<Object> itemSelected = treevPerspectives.getSelectionModel().getSelectedItem();
		Perspective perspective = (Perspective) itemSelected.getValue();
		itemURI = perspective.getUri();
		
		//Perspective Title:
		//------------------
		txtPerspectiveTitle.setText(perspective.getLabel());
		txtPerspectiveTitle.textProperty().addListener((observable, oldValue, newValue) -> {
		   perspective.setLabel(newValue);
		   //Move renamed Perspective to its sorted indexOf on the tree-view:
		   treevPerspectives.getRoot().getChildren().sort(Comparator.comparing(t->t.toString()));
		   //Select the renamed Perspective:
		   treevPerspectives.getSelectionModel().select(itemSelected);
		});
		
	    //Perspective Description:
		//------------------------
		txtaPerspectiveDesc.setText(perspective.getDescription());
		txtaPerspectiveDesc.textProperty().addListener((observable, oldValue, newValue) -> {
		   perspective.setDescription(newValue);
		});
	}

}
