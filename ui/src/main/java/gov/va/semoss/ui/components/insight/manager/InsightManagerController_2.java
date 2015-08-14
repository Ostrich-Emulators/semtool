package gov.va.semoss.ui.components.insight.manager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javafx.fxml.FXMLLoader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import gov.va.semoss.om.ParameterType;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Parameter;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.om.PlaySheet;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.WriteablePerspectiveTab;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;

public class  InsightManagerController_2 implements Initializable{
	public static final String ICON_LOCATION = "/images/icons16/";
	
	protected IEngine engine;
	private FXMLLoader loaderRightPane = null;
    //This DataFormat allows entire Insight objects to be placed on the DragBoard
	//of the "Perspectives" TreeView:
    private static final DataFormat insightFormat = new DataFormat("Object/Insight");      
	@FXML
	protected SplitPane spaneInsightManager;
	@FXML
	protected TreeView<Object> treevPerspectives;
	@FXML
	protected RadioButton radioMove;
	@FXML
	protected RadioButton radioCopy;
	@FXML
	protected AnchorPane apaneContent;
	
	
	protected ObservableList<Perspective> arylPerspectives;
	protected ObservableList<PlaySheet> arylPlaySheets;
	protected ObservableList<ParameterType> arylParameterTypes;
	private static final Logger log = Logger.getLogger( WriteablePerspectiveTab.class );

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	} 
	
	/**    Populates controls when the Insight Manager is loaded. Also provides 
	 * change-listeners for the "Perspectives" tree-view.
	 */
	public void setData(){		    
		engine = DIHelper.getInstance().getRdfEngine();
		//If the engine has been loaded, then populate controls, otherwise skip:
		if(engine != null){
		   arylPerspectives = FXCollections.observableArrayList();
		   arylPlaySheets = FXCollections.observableArrayList();
		   arylParameterTypes = FXCollections.observableArrayList();
		   		   
		   loadReferencesAndData();
		   
		}//End if(engine != null).		
	}
	
	/**   Loads all data needed for the Insight Manager from the database into
	 * the ArrayList<Perspective>, "arylPerspectives". After all Perspectives,
	 * and their Insights, have been loaded, a call is made to populate the
	 * "Perspectives" tree-view.
	 * 
	 * Note: "Perspective" is a value-object for populating various UI fields
	 * One such value is "get/setInsights(...)", which refers to an ArrayList<Insight>,
	 * based upon another value-object containing Insight data.
     */
	protected void loadData(){
		treevPerspectives.getScene().setCursor(Cursor.WAIT);
		
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
	            	treevPerspectives.getScene().setCursor(Cursor.DEFAULT);
	     		   
	     		    //Populate "Perspectives" tree-view:
	     		    populatePerspectiveTreeView();
	     		    
	     		    treevPerspectives.getSelectionModel().selectFirst();
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
	private boolean isDataLoaded;
	private void loadReferencesAndData(){		
		isDataLoaded = false;
		
	    //Convert mouse-pointer to a "wait" cursor:
		treevPerspectives.getScene().setCursor(Cursor.WAIT);
		
		//Define a Task to fetch an ArrayList of PlaySheets:
		Task<ObservableList<PlaySheet>> getPlaySheetData = new Task<ObservableList<PlaySheet>>(){
		    @Override 
		    protected ObservableList<PlaySheet> call() throws Exception {
		    	arylPlaySheets = FXCollections.observableArrayList(engine.getInsightManager().getPlaySheets());		    	
		        return arylPlaySheets;
		    }
		};
	    //Define a listener to load InsightManager data when Task completes,
		//but only if the PlaySheets have been loaded:
		getPlaySheetData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	if(arylPlaySheets.size() > 0){
	            	    //Restore mouse-pointer:
	            		treevPerspectives.getScene().setCursor(Cursor.DEFAULT);	  

	            		//Load Insight Manager data if it has not already been loaded:
	            		if(isDataLoaded == false){
	            		   loadData();
	            		   isDataLoaded = true;
	            		}
	            	}
	      	    }    	    
	        }
	     });
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
		getParameterTypeData.stateProperty().addListener(new ChangeListener<Worker.State>() {
	        @Override 
	        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState){
	            if(newState == Worker.State.SUCCEEDED){
	            	if(arylParameterTypes.size() > 0){
	            	    //Restore mouse-pointer:
	            		treevPerspectives.getScene().setCursor(Cursor.DEFAULT);	
	            		
		                //Load Insight Manager data if it has not already been loaded:
	            		if(isDataLoaded == false){
	            		   loadData();
	            		   isDataLoaded = true;
	            		}
	            	}
	      	    }    	    
	        }
	     });
		 //Run the Tasks on a separate Threads:
		 new Thread(getPlaySheetData).start();
		 new Thread(getParameterTypeData).start();
	}

//----------------------------------------------------------------------------------------------------
//                          P e r s p e c t i v e   T r e e - V i e w
//----------------------------------------------------------------------------------------------------
	/**   Populates the Perspective tree-view. 
	 * 
	 * All Perspectives and their Insights are loaded into this tree-view,
	 * so that the data can be used within the Insight Manager.
	 */
    private boolean isItemExpanded;
	protected void populatePerspectiveTreeView(){
        TreeItem<Object> rootItem = new TreeItem<Object>("Perspectives", null);
        rootItem.setExpanded(true);
        for (Perspective perspective: arylPerspectives) {
            TreeItem<Object> item_1 = new TreeItem<Object>(perspective);            
            rootItem.getChildren().add(item_1);
            
            for(Insight insight: perspective.getInsights()){
                InsightTreeItem<Object> item_2;
                try{
                   String strIcon = getInsightIcon(insight);
                   Image image = new Image(strIcon);
                   ImageView imageView = new ImageView(image);
                   item_2 = new InsightTreeItem<Object>(insight, imageView);
                }catch(Exception e){
                   item_2 = new InsightTreeItem<Object>(insight, null);
                }
            	item_1.getChildren().add(item_2);
            	
        	    for(Parameter parameter: insight.getInsightParameters()){
        	  	    TreeItem<Object> item_3 = new TreeItem<Object>(parameter);
        		    item_2.getChildren().add(item_3);
        	    }
            }
        }        
        treevPerspectives.setRoot(rootItem);    
        treevPerspectives.setEditable(true);
        
        //On mouse-pressed, note whether the selected tree-view item
        //has been expanded or not:
        treevPerspectives.setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent mouseEvent){
	           isItemExpanded = treevPerspectives.getSelectionModel().getSelectedItem().isExpanded();
	           mouseEvent.consume();
			}
        });
        //Set double-click handler for the tree-view to display editors
        //for Perspectives, Insights, and Parameters:
        treevPerspectives.setOnMouseClicked(new EventHandler<MouseEvent>(){
           @Override
           public void handle(MouseEvent mouseEvent){    
              if(mouseEvent.getClickCount() == 2){
            	 doubleClickTreeItem();
            	 //Restore selected tree-view item's expanded state to what it was before the
            	 //first click:
            	 treevPerspectives.getSelectionModel().getSelectedItem().setExpanded(isItemExpanded); 
              }
              mouseEvent.consume();
           }
        });

        //CellFactory to enable click-and-drag of Insights and display of Insight icons:
        //------------------------------------------------------------------------------
        treevPerspectives.setCellFactory(new Callback<TreeView<Object>, TreeCell<Object>>() {
        	//This TransferMode could either enable "Move" or "Copy":
            private TransferMode transferMode;
            //Context menu variables:
            private ContextMenu rootMenu;
            private ContextMenu perspectiveMenu;
            private ContextMenu insightMenu;
            private ContextMenu parameterMenu;
            //Initializer:
     	    {
     		  buildContextMenus();
     	    }
            
            @Override
            public TreeCell<Object> call(TreeView<Object> stringTreeView) {            	          	
               TreeCell<Object> treeCell = new TreeCell<Object>() {                   
                	@Override
                	protected void updateItem(Object item, boolean empty) {
                	    super.updateItem(item, empty);
                	    if (!empty && item != null) {
                	        setText(item.toString());
                	        setGraphic(getTreeItem().getGraphic());
                	        //Set context menu:
                            if(getTreeItem().getValue().equals("Perspectives")){
                               setContextMenu(rootMenu);
                            }else if(getTreeItem().getValue() instanceof Perspective){
                               setContextMenu(perspectiveMenu);
                            }else if(getTreeItem().getValue() instanceof Insight){
                               setContextMenu(insightMenu);
                            }else if(getTreeItem().getValue() instanceof Parameter){
                               setContextMenu(parameterMenu);
                            }else{
                               setContextMenu(null);
                            }
                        }else{
                	        setText(null);
                	        setGraphic(null);
                	    }
                	}
                };
                 
                //Begins dragging of object:
                //--------------------------
                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
                   @Override
                   public void handle(MouseEvent mouseEvent) {
                      if(treeCell.getItem() == null){
                         return;
                      }
                      //Only allow Insights to be dragged:
                      Object treeItem = treeCell.getItem();
                      if(treeItem instanceof Perspective){
                    	 return;
                      }
                  	  //Determine transfer mode based on which radio-button is selected:
                      if(radioMove.isSelected() == true){
                    	  transferMode = TransferMode.MOVE;
                      }else if(radioCopy.isSelected() == true){
                    	  transferMode = TransferMode.COPY;
                      }else{
                        transferMode = TransferMode.MOVE;
                      }
                      Dragboard dragBoard = treeCell.startDragAndDrop(transferMode);
                      ClipboardContent content = new ClipboardContent();
                      content.put(insightFormat, treeItem);
                      dragBoard.setContent(content);
                      
                      mouseEvent.consume();
                   }
                });

                //Indicates where drag transfer is possible:
                //------------------------------------------
                treeCell.setOnDragOver(new EventHandler<DragEvent>() {               	
                    @Override
                    public void handle(DragEvent dragEvent) {
                    	TreeCell<Object> cellDraggedOver = treeCell;
                        
                        if(dragEvent.getDragboard().hasContent(insightFormat) &&
                           cellDraggedOver.getItem() instanceof Insight) {
                             Insight valueDragged = (Insight) dragEvent.getDragboard().getContent(insightFormat);
                             if(!valueDragged.equals(treeCell.getItem())) {
                                dragEvent.acceptTransferModes(transferMode);
                             }
                        }
                        dragEvent.consume();
                    }
                });
 
                //Draws drag indication border:
                //-----------------------------
                treeCell.setOnDragEntered(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        treeCell.setStyle("-fx-border-color: #111111 #111111 #111111 #111111");

                        dragEvent.consume();
                    }              	
                });
                
                //Clears drag indication border:
                //------------------------------
                treeCell.setOnDragExited(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent dragEvent) {
					   //Clear the drag insertion line:
                        treeCell.setStyle("-fx-border-color: transparent transparent transparent transparent");

                        dragEvent.consume();
					}                	
                });

                //Transfers dragged object:
                //-------------------------
                treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        Insight valueDragged = (Insight) dragEvent.getDragboard().getContent(insightFormat);
  
                        TreeItem<Object> itemDragged = search(treevPerspectives.getRoot(), valueDragged);
                        TreeItem<Object> itemTarget = treeCell.getTreeItem(); 

                        ObservableList<TreeItem<Object>> olstOldInsights = itemDragged.getParent().getChildren(); 
                        ObservableList<TreeItem<Object>> olstInsights = itemTarget.getParent().getChildren();
                        int index = olstInsights.indexOf(itemTarget);
                        if(itemTarget.getValue() instanceof Perspective){
                           return;
                        }
                        //If "Move Insight" is selected, then remove the Insight 
                        //from its old location and place it where dropped:
                        if(transferMode.equals(TransferMode.MOVE)){
                           olstOldInsights.remove(itemDragged);
                           olstInsights.add(index, itemDragged);
                           
                        }else{
                           String strUniqueIdentifier = "_C"+String.valueOf(System.currentTimeMillis());
                           Insight insight = (Insight) itemDragged.getValue();
                           RepositoryConnection rc;
						   try {
							  //Make a deep copy of the Insight, and assign a unique URI to it:
							  rc = engine.getInsightManager().getRepository().getConnection();
	                   		  ValueFactory insightVF = rc.getValueFactory();
	                   		  Insight insightCopy = (Insight) SerializationUtils.clone(insight);
	                          URI uriInsightCopy = insightVF.createURI(insight.getIdStr() + strUniqueIdentifier);
                              insightCopy.setId(uriInsightCopy);
                              TreeItem<Object> itemDraggedCopy = new TreeItem<Object>(insightCopy);

                              //Assign unique URIs to Insight Parameters, and add Parameter 
                              //children to the Insight tree item:
                              for(Parameter parameter: insightCopy.getInsightParameters()){
                            	  parameter.setParameterURI(parameter.getParameterURI()+strUniqueIdentifier);
                      	  	      TreeItem<Object> item = new TreeItem<Object>(parameter);
                      	  	      itemDraggedCopy.getChildren().add(item);
                              }
                              
                              //Make a copy of the icon associated with "itemDragged":
                              Image imageCopy = new Image(getInsightIcon(insightCopy));
                              ImageView imageViewCopy = new ImageView(imageCopy);
                              itemDraggedCopy.setGraphic(imageViewCopy);
                              
                              //Insert the copy of the dragged Insight into the ObservableList,
                              //at the location dropped:
                              olstInsights.add(index, itemDraggedCopy);
                              
 						   } catch (RepositoryException e) {
 							  log.error(e, e);
						   }
                        } 
                        //Renumber Insights after move or copy:
                        renumberInsights();

                        dragEvent.consume();
                    }
                });
                
                //Completes drag operation:
                //-------------------------
                treeCell.setOnDragDone(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                       dragEvent.consume();
                    }
                });
                return treeCell;
            }//End "call(...)".
            
        	/**   Builds context menus and menu-click-handlers for tree-view items
        	 * (for the root item, Perspective items, Insight items, and Parameter
        	 * items).
        	 */
        	private void buildContextMenus(){
        		//Root menu:
        		rootMenu = new ContextMenu();
                MenuItem rootItem = new MenuItem("Add Perspective");
                rootMenu.getItems().add(rootItem);
                rootItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                    	addPerspective();
                    }
                });
                //Perspective menu:
                perspectiveMenu = new ContextMenu();
                MenuItem perspectiveItem = new MenuItem("Delete Perspective");
                perspectiveMenu.getItems().add(perspectiveItem);
                perspectiveItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                       deletePerspective();
                    }
                });
                perspectiveItem = new MenuItem("Add Insight");
                perspectiveMenu.getItems().add(perspectiveItem);
                perspectiveItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                       addInsight();
                    }
                });
                //Insight menu:
                insightMenu = new ContextMenu();
                MenuItem insightItem = new MenuItem("Delete Insight");
                insightMenu.getItems().add(insightItem);
                insightItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                       deleteInsight();
                    }
                });
                insightItem = new MenuItem("Add Parameter");
                insightMenu.getItems().add(insightItem);
                insightItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                    	addParameter();
                    }
                });
                //Parameter menu:
                parameterMenu = new ContextMenu();
                MenuItem parameterItem = new MenuItem("Delete Parameter");
                parameterMenu.getItems().add(parameterItem);
                parameterItem.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                       deleteParameter();
                    }
                });
        	}//End "buildContextMenus()".
        	
        });//End "treevPerspectives.setCellFactory".   
        
	}//End "populatePerspectiveTreeView()".
	
    /**   Searches the tree-view data for the Insight passed-in, and returns the TreeItem
     * associated with that Insight. Note: This is a recursive function that compares URI's
     * in its search.
     * 
     * @param currentNode -- (TreeItem<Object>) A node of the tree-view (usually is started
     *    at the root).
     * @param valueToSearch -- (Insight) A value that may exist in the tree-view.
     * @return search -- TreeItem<Object> The TreeItem associated with "valueToSearch".
     */
    private TreeItem<Object> search(final TreeItem<Object> currentNode, final Insight valueToSearch) {
       TreeItem<Object> result = null;
     
       if(currentNode.getValue().equals("Perspectives") == true ||
    	  currentNode.getValue() instanceof Perspective == true){
          for(TreeItem<Object> child : currentNode.getChildren()) {
             result = search(child, valueToSearch);
             if (result != null) {
                 break;
             }
          }                	 
       }else if(currentNode.getValue() instanceof Insight == true &&
          ((Insight) currentNode.getValue()).getId().equals(valueToSearch.getId())){
    	  result = currentNode;
       }
       return result;
    }

	/**   Renumbers the Insights under each Perspective TreeItem.
	 */
	private void renumberInsights(){
		ObservableList<TreeItem<Object>> olstPerspectives = treevPerspectives.getRoot().getChildren();
		
		for(TreeItem<Object> treeItem: olstPerspectives){
            ObservableList<TreeItem<Object>> olstInsights = treeItem.getChildren(); 
     		
    		for(int i = 0; i < olstInsights.size(); i++){
    			Insight insight = (Insight) olstInsights.get(i).getValue();
    			insight.setOrder(i + 1);
    		}
		}
	}
	
	/**   Adds a new empty Perspective to the top of the tree-view.
	 * (Called by "buildContextMenus()".)
	 */
	private void addPerspective(){
		//Build new Perspective, giving it a unique URI:
		//----------------------------------------------
		String strUniqueIdentifier = "_"+String.valueOf(System.currentTimeMillis());	
		Perspective perspective = new Perspective();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		} catch (RepositoryException e) {
			log.warn(e, e);
		}
   		ValueFactory insightVF = rc.getValueFactory();
        URI uriPerspective = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, 
           "Perspective"+strUniqueIdentifier);
		perspective.setUri(uriPerspective);
		perspective.setLabel("(A New Perspective)");
		
		//Add new Perspective to the tree-view:
		//-------------------------------------
		TreeItem<Object> item = new TreeItem<Object>(perspective);
        treevPerspectives.getRoot().getChildren().add(item);
        treevPerspectives.getRoot().getChildren().sort(Comparator.comparing(t->t.toString()));
        
        //Select new Perspective and open its editor:
        //-------------------------------------------
        treevPerspectives.getSelectionModel().select(item);
        doubleClickTreeItem();
	}
	
	/**    Removes the selected Perspective from the tree-view if the response to the
	 *  warning popup is OK. Also clears the editor in the right-pane if it represents
	 *  or is contained by the deleted Perspective. (Called by "buildContextMenus()".)
	 */
	private void deletePerspective(){
		if(GuiUtility.showWarningOkCancel("Are you sure you want to delete this Perspective?") == 0){
		   TreeItem<Object> itemPerspective = treevPerspectives.getSelectionModel().getSelectedItem();
		   clearRightPane(itemPerspective);
		   treevPerspectives.getRoot().getChildren().remove(itemPerspective);
		}
	}

	/**    Adds a new empty Insight to the top of the current Perspective's Insight
	 *  list. (Called by "buildContextMenus()".)
	 */
	private void addInsight(){
		//Build new Insight, giving it a unique URI:
		//------------------------------------------
		String strUniqueIdentifier = "_"+String.valueOf(System.currentTimeMillis());	
		Insight insight = new Insight();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		} catch (RepositoryException e) {
			log.warn(e, e);
		}
   		ValueFactory insightVF = rc.getValueFactory();
        URI uriInsight = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, 
           "Insight"+strUniqueIdentifier);
        insight.setId(uriInsight);
        insight.setLabel("(A New Insight)");
        insight.setOrder(0);
		insight.setOutput("gov.va.semoss.ui.components.playsheets.GridPlaySheet");
        
		//Add new Insight to the tree-view:
		//---------------------------------
        Image image = new Image(getInsightIcon(insight));
        ImageView imageView = new ImageView(image);
        InsightTreeItem<Object> item = new InsightTreeItem<Object>(insight, imageView);
        treevPerspectives.getSelectionModel().getSelectedItem().getChildren().add(0, item);
        renumberInsights();
        
        //Select new Insight and open its editor:
        //---------------------------------------
        treevPerspectives.getSelectionModel().select(item);
        doubleClickTreeItem();
	}
	
	/**    Removes the selected Insight from the tree-view if the response to the
	 *  warning popup is OK. Also clears the editor in the right-pane if it represents
	 *  or is contained by the deleted Insight. (Called by "buildContextMenus()".)
	 */
	private void deleteInsight(){
		if(GuiUtility.showWarningOkCancel("Are you sure you want to delete this Insight?") == 0){
		   TreeItem<Object> itemInsight = treevPerspectives.getSelectionModel().getSelectedItem();
		   clearRightPane(itemInsight);
		   TreeItem<Object> itemPerspective = treevPerspectives.getSelectionModel()
			  .getSelectedItem().getParent();
		   
		   itemPerspective.getChildren().remove(itemInsight);
           renumberInsights();
		}
	}

	/**    Adds a new empty Parameter to the bottom of the current Insight's 
     *  Parameter list. (Called by "buildContextMenus()".)
	 */
	private void addParameter(){
		//Build new Parameter, giving it a unique URI:
		//-------------------------------------------
		String strUniqueIdentifier = "_"+String.valueOf(System.currentTimeMillis());	
		Parameter parameter = new Parameter();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		} catch (RepositoryException e) {
			log.warn(e, e);
		}
   		ValueFactory insightVF = rc.getValueFactory();
        URI uriParameter = insightVF.createURI(MetadataConstants.VA_INSIGHTS_NS, 
           "Parameter"+strUniqueIdentifier);
        parameter.setParameterURI(uriParameter.toString());
        parameter.setLabel("(A New Parameter)");
        
		//Add new Parameter to the tree-view:
		//-----------------------------------
        InsightTreeItem<Object> insightTreeItem = (InsightTreeItem<Object>) treevPerspectives.getSelectionModel().getSelectedItem();
        Insight insight = (Insight) insightTreeItem.getValue();
        ObservableList<TreeItem<Object>> olstInsightParameters = insightTreeItem.getChildren();
        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>) insight.getInsightParameters();                
        TreeItem<Object> parameterTreeItem = new TreeItem<Object>(parameter);
        //First update the Insight's list of Parameters:
        if(arylParameters.size() > 0 && arylParameters.get(0).toString().equals("")){
	    	arylParameters.set(0, parameter);
	    }else{
        	arylParameters.add(parameter);
	    }
        //Then update the InsightTreeItem's children list:
        if(olstInsightParameters.size() > 0 && olstInsightParameters.get(0).getValue().toString().equals("")){
           olstInsightParameters.set(0, parameterTreeItem);
        }else{
           olstInsightParameters.add(parameterTreeItem);
        }                
        
        //Select new Parameter and open its editor:
        //-----------------------------------------
        treevPerspectives.getSelectionModel().select(parameterTreeItem);
        doubleClickTreeItem();
	}//End "addParameter()".
	
	/**    Removes the selected Parameter from the tree-view if the response to the
	 *  warning popup is OK. Also clears the editor in the right-pane if it represents
	 *  or is contained by the deleted Parameter. (Called by "buildContextMenus()".)
	 */
	private void deleteParameter(){
		if(GuiUtility.showWarningOkCancel("Are you sure you want to delete this Parameter?") == 0){
		   TreeItem<Object> itemParameter = treevPerspectives.getSelectionModel().getSelectedItem();
		   clearRightPane(itemParameter);
		   TreeItem<Object> itemInsight = itemParameter.getParent();
		   Insight insight = (Insight) itemInsight.getValue();
           ArrayList<Parameter> arylParameters = (ArrayList<Parameter>) insight.getInsightParameters();                
           arylParameters.remove((Parameter) itemParameter.getValue());
           itemInsight.getChildren().remove(itemParameter);
		}
	}
	
	/**   Clears the right pane when a TreeItem is deleted, if that TreeItem represents or
	 * contains the data displayed in the right pane.
	 * 
	 * Note: This is a recursive function.
	 * 
	 * @param treeItem -- (TreeItem<Object>) TreeItem to be deleted.
	 */
	private void clearRightPane(TreeItem<Object> treeItem){
		URI uriTreeItem = null;
		Object objTreeItem = treeItem.getValue();
		URI uriController = null;
		if(loaderRightPane == null){
			return;
		}
		Object objController = loaderRightPane.getController();
		
		//Get the URI of the object (Parameter, Insight, or Perspective)
		//behind the TreeItem passed in:
		if(objTreeItem instanceof Perspective){
			uriTreeItem = ((Perspective) objTreeItem).getUri();
		}else if(objTreeItem instanceof Insight){
			uriTreeItem = ((Insight) objTreeItem).getId();
		}else if(objTreeItem instanceof Parameter){
			uriTreeItem = ((Parameter) objTreeItem).getParameterId();
		}
		
		//Get the URI of the object (Parameter, Insight, or Perspective)
		//displayed in the right-pane:
		if(objController instanceof PerspectiveEditorController){
			uriController = ((PerspectiveEditorController) objController).itemURI;			
		}else if(objController instanceof InsightEditorController){
			uriController = ((InsightEditorController) objController).itemURI;		   
		}else if(objController instanceof ParameterEditorController){
			uriController = ((ParameterEditorController) objController).itemURI;
	    }
		
		if(uriTreeItem != null && uriController != null &&
		   uriTreeItem.equals(uriController)){
			apaneContent.getChildren().clear();
			
		}else{
			for(TreeItem<Object> treeItem_2: treeItem.getChildren()){
				clearRightPane(treeItem_2);
			}
		}	
	}//End "clearRightPane(...)".
	
	/**   Class to override the ".isLeaf()" method for Insight tree-items.
	 * We need to be sure that no Parameters are listed under the Insight.
	 * 
	 * @author Thomas
	 *
	 * @param <T>
	 */
    public class InsightTreeItem<T> extends TreeItem<Object>{
    	 private Insight insight = null;
    	
         public InsightTreeItem(Insight insight, ImageView imageView){
        	 super(insight, imageView);
        	 this.insight = insight;
         }
    	
      	 @Override
      	 public boolean isLeaf() { 
      		 boolean boolReturnValue = true;
      		 
      		 if(insight.getInsightParameters().size() > 0 &&
      		    ((ArrayList<?>) insight.getInsightParameters()).get(0).toString().equals("") == false){
      		    boolReturnValue = false;
      	     }
      		 return boolReturnValue;
      	 }
    }
	
	/**   Returns a PlaySheet icon for the passed-in Insight.The array-list of PlaySheets
	 * is consulted. The base path to all icons is defined at the top of this class, 
	 * in "ICON_LOCATION".
	 * 
	 * @param insight -- (Insight) The Insight for which an icon is required.
	 *    
	 * @return getInsightIcon -- (String) The file-path to the icon, described above.
	 */
	private String getInsightIcon(Insight insight){
		String strReturnValue = "";
		
	    for(PlaySheet playsheet: arylPlaySheets){
		   if(insight.getOutput().equals(playsheet.getViewClass())){
			  if(playsheet.getIcon() != null){
			     strReturnValue = ICON_LOCATION + playsheet.getIcon();
			  }
			  break;
		   }
	    }
		return strReturnValue;
	}
	
	/**   When double-clicking a TreeItem, we must load the right-pane with 
	 * the contents of the item clicked. Provides dynamic loading of FXML
	 * editors with their controllers.
	 */
	private void doubleClickTreeItem(){
		PerspectiveEditorController contPerspectiveEditor = null;
		InsightEditorController contInsightEditor = null;
		ParameterEditorController contParameterEditor = null;
	    TreeItem<Object> item = treevPerspectives.getSelectionModel().getSelectedItem();
  	    apaneContent.getChildren().clear();
	    try{
	       if(item.getValue() instanceof Perspective){    	
			  loaderRightPane = new FXMLLoader(getClass().getResource("/fxml/PerspectiveEditor.fxml"));
			  apaneContent.getChildren().add(loaderRightPane.load());
			  contPerspectiveEditor = loaderRightPane.getController();
			  contPerspectiveEditor.setData(treevPerspectives);

	       }else if(item.getValue() instanceof Insight){
			  loaderRightPane = new FXMLLoader(getClass().getResource("/fxml/InsightEditor.fxml"));
			  apaneContent.getChildren().add(loaderRightPane.load());
			  contInsightEditor = loaderRightPane.getController();
			  contInsightEditor.setData(treevPerspectives, arylPlaySheets);

	       }else if(item.getValue() instanceof Parameter){
			  loaderRightPane = new FXMLLoader(getClass().getResource("/fxml/ParameterEditor.fxml"));
			  apaneContent.getChildren().add(loaderRightPane.load());
			  contParameterEditor = loaderRightPane.getController();
			  contParameterEditor.setData(treevPerspectives, arylParameterTypes);
	       }
	    }catch(IOException e){
			log.warn(e, e);
		}
	}
////----------------------------------------------------------------------------------------------------
////	                            P e r s p e c t i v e   T a b
////----------------------------------------------------------------------------------------------------
//	/**   Populates the Perspective combo-box on the "Perspective" tab.
//	 */
//	protected void populatePerspectiveComboBox(){
//  	    cboPerspectiveTitle.setItems(arylPerspectives);
// 	}
//
//	/**   Populates the Perspective Title text-field with the title of
//	 * the currently selected Perspective.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 */
//	protected void populatePerspectiveTitleTextField(int perspectiveIndex){
//		String strTitle = "";
//		if(arylPerspectives.get(perspectiveIndex).getLabel() != null){
//		   strTitle = arylPerspectives.get(perspectiveIndex).getLabel();
//		}
//	    txtPerspectiveTitle.setText(strTitle);	
//	}
//	
//	/**   Populates the Perspective Description text-area with the description of
//	 * the currently selected Perspective.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 */
//	protected void populatePerspectiveDescTextArea(int perspectiveIndex){		
//		String strDescription = "";
//		if(arylPerspectives.get(perspectiveIndex).getDescription() != null){
//		   strDescription = arylPerspectives.get(perspectiveIndex).getDescription();
//		}
//	    txtaPerspectiveDesc.setText(strDescription);	
//	}
//	
//	/**   Populates the Insight list-view with Insights under the currently
//	 * selected Perspective.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 */
//	protected void populateInsightListView(int perspectiveIndex){
//		Perspective perspective = (Perspective) arylPerspectives.get(perspectiveIndex);
//  	    lstvInsights.setItems(arylInsights);
//
//  	    //Cell Factory for "Insight" list-view to display PlaySheet icons
//	    //to the left of Insight labels:
//	    lstvInsights.setCellFactory(listView -> new ListCell<Insight>() {
//	        private ImageView imageView = new ImageView();
//	        
//	        @Override
//	        public void updateItem(Insight item, boolean empty) {
//	            super.updateItem(item, empty);
//	            if (empty) {
//	                setText(null);
//	                setGraphic(null);
//	            }else{
//	                setText(item.toString());
//	            	try{
//	                   Image image = new Image(getInsightIcon(item.getOrderedLabel(perspective.getUri())));
//	                   imageView.setImage(image);
//	                   setGraphic(imageView);
//	            	}catch(Exception e){
//		               setGraphic(null);
//	            	}
//	            }
//	        }
//	        
//	        /**   Class-initializer to define drag-and-drop operations.
//	         */
//	        {
//	            ListCell thisCell = this;
//
//	            setOnDragDetected(event ->{
//	                if(getItem() == null){
//	                    return;
//	                }
//	                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
//	                ClipboardContent content = new ClipboardContent();
//	                content.putString(getText());
//	                dragboard.setDragView(
//	                	new Image(getInsightIcon(getItem().getOrderedLabel(perspective.getUri())))
//	                );
//	                dragboard.setContent(content);
//	                event.consume();
//	            });
//
//	            setOnDragOver(event -> {
//	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
//	                    event.acceptTransferModes(TransferMode.MOVE);
//	                }
//	                event.consume();
//	            });
//
//	            setOnDragEntered(event -> {
//	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
//	                    setOpacity(0.3);
//	                }
//	            });
//
//	            setOnDragExited(event -> {
//	                if(event.getGestureSource() != thisCell && event.getDragboard().hasString()){
//	                    setOpacity(1);
//	                }
//	            });
//
//	            setOnDragDropped(event -> {
//	                if(getItem() == null){
//	                    return;
//	                }
//	                Dragboard dragboard = event.getDragboard();
//	                boolean success = false;
//
//	                if (dragboard.hasString()) {
//	                    int thisIdx = arylInsights.indexOf(getItem());
//	                    int draggedIdx = 0;
//	                    for(Insight item: arylInsights){
//	                    	if(item.getOrderedLabel(perspective.getUri()).equals(dragboard.getString())){
//	                    		break;
//	                    	}
//	                    	draggedIdx++;
//	                    }
//                        moveInsight(draggedIdx, thisIdx, perspective.getUri().toString(), 
//                           arylInsights.get(draggedIdx));
//
//	                    success = true;
//	                }
//	                event.setDropCompleted(success);
//	                event.consume();
//	            });
//
//	            setOnDragDone(DragEvent::consume);
//
//	        }//End Class-initializer.
//
//	        /**   Moves a dragged Insight to a new position in the list-view, and either pushes 
//	         * other Insights back or forward, depending upon the drag direction
//	         * 
//	         * @param startIdx -- (int) Origin index of dragged Insight in the list-view's
//	         *     ObservableList.
//	         *     
//	         * @param endIdx -- (int) Destination index of dragged Insight in the list-view's
//	         *     ObservableList.
//	         *     
//	         * @param strPerspectiveUri -- (String) URI string of the Perspective that contains
//	         *     the visible Insights.
//	         *     
//	         * @param insight -- (Insight) The Insight to be moved.
//	         */
//	        private void moveInsight(int startIdx, int endIdx, String strPerspectiveUri, Insight insight){
//	        	if(startIdx < endIdx){
//	        	   for(int i = startIdx; i < endIdx; i++){
//	        		   arylInsights.set(i, arylInsights.get(i + 1));
//	        	   }
//	        	   arylInsights.set(endIdx, insight);
//	        	}
//	        	if(startIdx > endIdx){
//	        	   for(int i = startIdx; i > endIdx; i--){
//	        		   arylInsights.set(i, arylInsights.get(i - 1));
//	        	   }
//		           arylInsights.set(endIdx, insight);
//	        	}
//	        	//Reorder Insights and redisplay:
//	        	for(int i = 0; i < arylInsights.size(); i++){
//	        		arylInsights.get(i).setOrder(strPerspectiveUri, i + 1);
//	        	}
//	        	lstvInsights.setItems(null);
//	        	lstvInsights.setItems(arylInsights);
//	        }
//	        
//	    });//End setCellFactory.
//	    
//	    //Navigate to the previously selected Insight (or the first Insight under
//	    //the Perspective, if the previously selected Insight has been moved):
//    	lstvInsights.getSelectionModel().selectFirst();
//	    for(Insight element: arylInsights){
//	    	 if(element.getOrderedLabel(perspective.getUri()).equals(prevQuestionLabel)){
//	    	     lstvInsights.getSelectionModel().select(element);
//	    	     break;
//	    	 }
//	    }
// 	}
//	
////----------------------------------------------------------------------------------------------------
////                                   I n s i g h t   T a b
////----------------------------------------------------------------------------------------------------
//	/**   Populates the question text-field with the question from the 
//	 * currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateQuestionTextField(int perspectiveIndex, int insightIndex){
//  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    txtQuestion_Inst.setText(((Insight) arylInsights.get(insightIndex)).getLabel());		   
//	}
//
//	/**   Populates the "Display with" dropdown with all playsheets defined
//	 * in the Insights KB. Also, pre-selects the playsheet associated with 
//	 * the passed-in Insight index.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populatePlaysheetComboBox(int perspectiveIndex, int insightIndex){
//  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    String playSheetClass = ((Insight) arylInsights.get(insightIndex)).getOutput();
//	    cboDisplayWith_Inst.setItems(arylPlaySheets);
// 	    for(PlaySheet playsheet: arylPlaySheets){
// 	    	if(playsheet.getViewClass().equals(playSheetClass)){
// 	    		cboDisplayWith_Inst.getSelectionModel().select(playsheet);	
// 	    	}
//	    }
//        //Cell Factory for "Display with" combo-box list-view to display 
//	    //PlaySheet icons to the left of Playsheet labels:
// 	    cboDisplayWith_Inst.setCellFactory(ListView -> new ListCell<PlaySheet>() {
//	        private ImageView imageView = new ImageView();
//	        @Override
//	        public void updateItem(PlaySheet item, boolean empty) {
//	            super.updateItem(item, empty);
//	            if (empty) {
//	                setText(null);
//	                setGraphic(null);
//	            }else{
//                    setText(item.toString());
//                    try{
//	                   Image image = new Image(getPlaySheetIcon(item.toString()));
//	                   imageView.setImage(image);
//	                   setGraphic(imageView);
//                    }catch(Exception e){
//                       setGraphic(null);
//                    }
//	            } 
//	        }
//	    });	
// 	    //Necessary to display an icon on the button area of the combo-box:
// 	    cboDisplayWith_Inst.setButtonCell(cboDisplayWith_Inst.getCellFactory().call(null));
//	}
//	
//	/**   Returns a PlaySheet icon for the passed-in PlaySheet label. The array-list
//	 * of PlaySheets is consulted. The base path to all icons is defined at the top 
//	 * of this class, in "ICON_LOCATION".
//	 * 
//	 * @param strPlaySheetLabel -- (String) The label of the PlaySheet for which an 
//	 *    icon is required.
//	 *    
//	 * @return getPlaySheetIcon -- (String) The file-path to the icon, described above.
//	 */
//	private String getPlaySheetIcon(String strPlaySheetLabel){
//	   String strReturnValue = "";
//	   
//	   for(PlaySheet playsheet: arylPlaySheets){
//		   if(playsheet.getLabel().equals(strPlaySheetLabel)){
//			  if(playsheet.getIcon() != null){
//			     strReturnValue = ICON_LOCATION + playsheet.getIcon();
//			  }
//			  break;
//		   }
//	   }
//	   return strReturnValue;
//	}
//
//	/**   Populates the Renderer-Class text-field from the 
//	 * currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateRendererClassTextField(int perspectiveIndex, int insightIndex){
//  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    txtRendererClass_Inst.setText(((Insight) arylInsights.get(insightIndex)).getRendererClass());		   
//	}
//	
//	/**   Populates the legacy-query check-box with a check from the 
//	 * currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateLegacyQueryCheckBox(int perspectiveIndex, int insightIndex){
//  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    chkLegacyQuery_Inst.setSelected(((Insight) arylInsights.get(insightIndex)).getIsLegacy());		   
//	}
//	
//	/**   Populates the query text-area with Sparql from the currently 
//	 * selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.git
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateQueryTextArea(int perspectiveIndex, int insightIndex){
//  	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//		String strQuery = "";
//		if(arylInsights.get(insightIndex).getSparql() != null){
//			strQuery = arylInsights.get(insightIndex).getSparql();
//		}
//	    txtaQuery_Inst.setText(strQuery);		   
//	}
//	
//	/**   Populates the Parameter list-view with Parameters under the currently
//	 * selected Insight.
//	 * 
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateInsightParameterListView(int perspectiveIndex, int insightIndex){
//		lstvParameter_Inst.setItems(arylInsightParameters);
//	    //Navigate to the previously selected Parameter (or the first Parameter
//	    //under the Insight, if the previously selected Parameter has been moved):
//    	lstvParameter_Inst.getSelectionModel().selectFirst();
//	    for(Parameter element: arylInsightParameters){
//	    	 if(element.getLabel().equals(prevParameterLabel)){
//	    	     lstvParameter_Inst.getSelectionModel().select(element);
//	    	     break;
//	    	 }
//	    }
//	}
//	
//	/**   Populates the Insight Perspectives list-view with all Perspectives,
//	 * and selects the Perspectives having the current Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateInsightPerspectivesListView(int perspectiveIndex, int insightIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    Insight insight = (Insight) arylInsights.get(insightIndex);
//	    arylInsightPerspectives = new ArrayList<Perspective>();
//          
//	    lstvInsightPerspective_Inst.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//  	    lstvInsightPerspective_Inst.setItems(arylPerspectives);
//
//  	    lstvInsightPerspective_Inst.getSelectionModel().clearSelection();
//  	    for(Perspective perspective: arylPerspectives){
//  	    	for(Insight Insight: perspective.getInsights()){
//  	    		if(insight.getId().equals(Insight.getId())){
//  	    			lstvInsightPerspective_Inst.getSelectionModel().select(perspective);
//  	    			arylInsightPerspectives.add(perspective);
//  	    		}
//  	    	}
//  	    }
//	}
//	
//	/**   Populates the Description text-area with a description  
//	 * from the currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateInsightDescTextArea(int perspectiveIndex, int insightIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//		String strDescription = "";
//		if(arylInsights.get(insightIndex).getDescription() != null){
//			strDescription = arylInsights.get(insightIndex).getDescription();
//		}
//	    txtaInsightDesc_Inst.setText(strDescription);		   
//	}
//	
//	/**   Populates the Creator text-field with the Insight creator  
//	 * from the currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateCreatorTextField(int perspectiveIndex, int insightIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    txtCreator_Inst.setText(((Insight) arylInsights.get(insightIndex)).getCreator());		   
//	}
//	
//	/**   Populates the Created text-field with the Insight create-date  
//	 * from the currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateCreatedTextField(int perspectiveIndex, int insightIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    txtCreated_Inst.setText(((Insight) arylInsights.get(insightIndex)).getCreated());		   
//	}
//	
//	/**   Populates the Modified text-field with the Insight update-date  
//	 * from the currently selected Insight.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 */
//	private void populateModifiedTextField(int perspectiveIndex, int insightIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//	    txtModified_Inst.setText(((Insight) arylInsights.get(insightIndex)).getModified());		   
//	}
//
////----------------------------------------------------------------------------------------------------
////                                   P a r a m e t e r   T a b
////----------------------------------------------------------------------------------------------------
//
//	/**   Populates the "Name" text-field with the "label" property from the currently  
//	 * selected Parameter.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 * @param parameterIndex -- (int) Index of currently selected Parameter.
//	 */
//	private void populateNameTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//        Insight insight = arylInsights.get(insightIndex);
//        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
//        txtLabel_parm.setText(arylParameters.get(parameterIndex).getLabel());
//	}
//	
//	/**   Populates the "Variable" text-field with the "variable" property from the currently  
//	 * selected Parameter.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 * @param parameterIndex -- (int) Index of currently selected Parameter.
//	 */
//	private void populateVariableTextField(int perspectiveIndex, int insightIndex, int parameterIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//        Insight insight = arylInsights.get(insightIndex);
//        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
//        txtVariable_parm.setText(arylParameters.get(parameterIndex).getVariable());
//	}
//	
//	/**   Populates the "Parameter Type" dropdown with all Concept Types defined
//	 * in the Main KB, and pre-selects the Type associated with the current Parameter.
//	 */
//	private void populateParameterTypeComboBox(int perspectiveIndex, int insightIndex, int parameterIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//        Insight insight = arylInsights.get(insightIndex);
//        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
//        Parameter parameter = arylParameters.get(parameterIndex);
//	    cboParameterType_parm.setItems(arylParameterTypes);
//	    
//	    boolean boolSelected = false;
//	    for(ParameterType valueType: cboParameterType_parm.getItems()){
//	    	if(parameter.getParameterType().equals(valueType.getParameterClass())){
//	    		cboParameterType_parm.getSelectionModel().select(valueType);
//	    		boolSelected = true;
//	    		break;
//	    	}
//	    }
//	    //If nothing has been selected, the select the "(Unselected)" item:
//	    if(boolSelected == false){
//    		cboParameterType_parm.getSelectionModel().selectFirst();
//	    }
//	}
//	
//	/**   Populates the "Default Query" text-field with the "defaultQuery" property from  
//	 * the currently selected Parameter.
//	 * 
//	 * @param perspectiveIndex -- (int) Index of currently selected Perspective.
//	 * @param insightIndex -- (int) Index of currently selected Insight.
//	 * @param parameterIndex -- (int) Index of currently selected Parameter.
//	 */
//	private void populateDefaultQueryTextArea(int perspectiveIndex, int insightIndex, int parameterIndex){
// 	    ArrayList<Insight> arylInsights = ((Perspective) arylPerspectives.get(perspectiveIndex)).getInsights();
//        Insight insight = arylInsights.get(insightIndex);
//        ArrayList<Parameter> arylParameters = (ArrayList<Parameter>)insight.getInsightParameters();
//        txtaDefaultQuery_parm.setText(arylParameters.get(parameterIndex).getDefaultQuery());
//	}
//	
////----------------------------------------------------------------------------------------------------
////                      I n s i g h t   M a n a g e r   U t i l i t i e s
////----------------------------------------------------------------------------------------------------
//
//	/**   Prepares a string for use in a dynamic Sparql query, where " and ' are
//	 * delimiters. The double-quote, ", is changed to ', and existing single-quotes
//	 * are left alone. This utility is also used thoughout the Insight Manager,
//	 * where user-editable RDF strings are persisted.
//	 *
//	 * @param quotedString -- (String) The string containing double and single
//	 * quotes.
//	 *
//	 * @return legalizeQuotes -- (String) The cleaned string, as described above.
//	 */
//	public String legalizeQuotes( String quotedString ) {
//		String strReturnValue = quotedString;
//
//		strReturnValue = strReturnValue.replaceAll( "\"", "'" );
//
//		return strReturnValue;
//	}
//	


}
