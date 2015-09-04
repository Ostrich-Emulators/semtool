package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Perspective;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.openrdf.model.URI;

public class ParameterEditorController implements Initializable {

	protected URI itemURI;
	private int indexParameter;
	private List<Insight> arylInsights;
	private int indexInsight;
	private Insight insight;
	private Parameter parameter;
	@FXML
	private TextField txtLabel_parm;
	@FXML
	private TextField txtVariable_parm;
	@FXML
	private ComboBox<ParameterType> cboParameterType_parm;
	@FXML
	private Button btnBuildQuery_Parm;
	@FXML
	private TextArea txtaDefaultQuery_parm;

	@Override
	public void initialize( URL arg0, ResourceBundle arg1 ) {
	}

	/**
	 * Loads the "Parameter" editor with Parameter Name, Variable, Parameter
	 * Types, and a Default Query. Also establishes change-handlers on the editor
	 * fields.
	 *
	 * @param treevPerspectives -- (TreeView<Object>) The InsightManager's
	 * tree-view.
	 *
	 * @param obsParameterTypes -- (ObservableList<ParameterType>) A list of
	 * Parameter Types.
	 */
	public void setData( TreeView<Object> treevPerspectives, ObservableList<ParameterType> obsParameterTypes ) {
		TreeItem<Object> itemSelected = treevPerspectives.getSelectionModel().getSelectedItem();
		insight = (Insight) itemSelected.getParent().getValue();
		arylInsights = ( (Perspective) itemSelected.getParent().getParent().getValue() ).getInsights();
		indexInsight = arylInsights.indexOf( insight );
		parameter = (Parameter) itemSelected.getValue();
		indexParameter = ( (ArrayList<Parameter>) insight.getInsightParameters() ).indexOf( parameter );
		itemURI = parameter.getParameterId();

		//Parameter Name:
		//---------------
		txtLabel_parm.setText( parameter.getLabel() );
		txtLabel_parm.textProperty().addListener( ( observable, oldValue, newValue ) -> {
			parameter.setLabel( newValue );
			updateParameterInInsightInPerspective();
			//This hack is necessary to update the TreeItem:
			itemSelected.getParent().setExpanded( false );
			itemSelected.getParent().setExpanded( true );
			treevPerspectives.getSelectionModel().select( itemSelected );
		} );

		//Variable:
		//---------
		txtVariable_parm.setText( parameter.getVariable() );
		txtVariable_parm.textProperty().addListener( ( observable, oldValue, newValue ) -> {
			parameter.setVariable( newValue );
			updateParameterInInsightInPerspective();
		} );

		//Parameter Type Combo-Box:
		//-------------------------
		String parameterType = parameter.getParameterType();
		cboParameterType_parm.setItems( obsParameterTypes );
		boolean boolSelected = false;
		for ( ParameterType valueType : obsParameterTypes ) {
			if ( parameterType != null && parameterType.equals( valueType.getParameterClass() ) ) {
				cboParameterType_parm.getSelectionModel().select( valueType );
				boolSelected = true;
				break;
			}
		}
		//If nothing has been selected, the select the "(Unselected)" item:
		if ( boolSelected == false ) {
			cboParameterType_parm.getSelectionModel().selectFirst();
		}
		cboParameterType_parm.valueProperty().addListener( ( observable, oldValue, newValue ) -> {
			parameter.setParameterType( newValue.getParameterClass() );
			updateParameterInInsightInPerspective();
		} );

 	    //Build Default Query Button:
		//---------------------------
		btnBuildQuery_Parm.setOnAction( this::handleBuildQuery );
		btnBuildQuery_Parm.setTooltip( new Tooltip( "Build Parameter query from Type URI." ) );

		//Parameter Query:
		//----------------
		txtaDefaultQuery_parm.setText( parameter.getDefaultQuery() );
		txtaDefaultQuery_parm.textProperty().addListener( ( observable, oldValue, newValue ) -> {
			parameter.setDefaultQuery( newValue );
			updateParameterInInsightInPerspective();
		} );

	}//End "setData(...)"

	/**
	 * Click-handler for the "Build Query from Type" button. Builds and inserts a
	 * simple Sparql query into the "Parameter Query" field to fetch instances of
	 * the URI entered in the "Parameter Type" field with associated labels.
	 *
	 * @param event
	 */
	private void handleBuildQuery( ActionEvent event ) {
		String strParameterType = ( (ParameterType) cboParameterType_parm.getValue() ).getParameterClass();
		if ( strParameterType != null && strParameterType.equals( "" ) == false ) {
		   //Note: A generalized prefix resolver API should be used here 
			//to map the URI to an appropriate prefixed name:
			if ( strParameterType.contains( "http://semoss.org/ontologies/" ) == true ) {
				strParameterType = strParameterType.replace( "http://semoss.org/ontologies/", "semoss:" );
			}
			else {
				strParameterType = "<" + strParameterType + ">";
			}
			String generatedQuery = "SELECT ?entity ?label "
					+ "\nWHERE{\n    ?entity a " + strParameterType + " . \n    "
					+ "?entity rdfs:label ?label . \n}";
			txtaDefaultQuery_parm.setText( generatedQuery );
		}
	}

	/**
	 * Updates this Parameter in its parent Insight, in the "getParameters()" Map
	 * and in the "getInsightParameters()" Collection. Also updates the parent
	 * Insight in its Perspective. This method is called by each screen-field
	 * change handler.
	 */
	private void updateParameterInInsightInPerspective() {
		ArrayList<Parameter> arylParameters = (ArrayList<Parameter>) insight.getInsightParameters();
		arylParameters.set( indexParameter, parameter );
		insight.setParameter( parameter.getVariable(), parameter.getLabel(),
				parameter.getParameterType(), parameter.getDefaultQuery() );
		arylInsights.set( indexInsight, insight );
	}
}
