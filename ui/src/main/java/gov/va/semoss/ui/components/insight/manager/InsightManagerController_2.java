package gov.va.semoss.ui.components.insight.manager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javafx.fxml.FXMLLoader;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
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
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import java.util.List;

public class InsightManagerController_2 implements Initializable {

	public static final String ICON_LOCATION = "/images/icons16/";

	protected IEngine engine;
	private FXMLLoader loaderRightPane = null;
	//This DataFormat allows entire Insight objects to be placed on the DragBoard
	//of the "Perspectives" TreeView:
	private static final DataFormat insightFormat = new DataFormat( "Object/Insight" );
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
	@FXML
	protected Button btnSave;
	@FXML
	protected Button btnReload;
	@FXML
	protected ProgressBar pbSaveReload;

	protected ObservableList<Perspective> arylPerspectives;
	protected ObservableList<PlaySheet> arylPlaySheets;
	protected ObservableList<ParameterType> arylParameterTypes;
	private static final Logger log = Logger.getLogger( InsightManagerController_2.class );

	//These are necessary to make sure that the Insight Manager 
	//loads only after the left-pane is completely loaded:
	public static Object guiUpdateMonitor = new Object();
	public static boolean boolLeftPaneUpdated = false;

	@Override
	public void initialize( URL url, ResourceBundle rb ) {
	}

	/**
	 * Populates controls when the Insight Manager is loaded. Also provides
	 * change-listeners for the "Perspectives" tree-view.
	 */
	public void setData() {
		engine = DIHelper.getInstance().getRdfEngine();
		//If the engine has been loaded, then populate controls, otherwise skip:
		if ( engine != null ) {
			arylPerspectives = FXCollections.observableArrayList();
			arylPlaySheets = FXCollections.observableArrayList();
			arylParameterTypes = FXCollections.observableArrayList();

			loadReferencesAndData();

			//Build Save Button:
			//------------------
			btnSave.setOnAction( this::handleSave );
			btnSave.setTooltip( new Tooltip( "Save changes to all Perspectives, Insights, and Parameters." ) );

			//Build Reload Button:
			//--------------------
			btnReload.setOnAction( this::handleReload );
			btnReload.setTooltip( new Tooltip( "Reload Insight Manager from Database." ) );

			//Initially hide the Save-Reload ProgressBar:
			//-------------------------------------------
			pbSaveReload.setVisible( false );
		}//End if(engine != null).		
	}

	/**
	 * Loads all data needed for the Insight Manager from the database into the
	 * ArrayList<Perspective>, "arylPerspectives". After all Perspectives, and
	 * their Insights, have been loaded, a call is made to populate the
	 * "Perspectives" tree-view.
	 *
	 * Note: "Perspective" is a value-object for populating various UI fields One
	 * such value is "get/setInsights(...)", which refers to an
	 * ArrayList<Insight>, based upon another value-object containing Insight
	 * data.
	 */
	protected void loadData() {
		treevPerspectives.getScene().setCursor( Cursor.WAIT );

		//Define a Task to fetch an ArrayList of Perspectives/Insights:
		Task<ObservableList<Perspective>> getInsightManagerData = new Task<ObservableList<Perspective>>() {
			@Override
			protected ObservableList<Perspective> call() throws Exception {
				arylPerspectives = FXCollections.observableArrayList( engine.getInsightManager().getPerspectives() );
				return arylPerspectives;
			}
		};
		//Define a listener to set the return value when the  Task completes:
		getInsightManagerData.stateProperty().addListener( new ChangeListener<Worker.State>() {
			@Override
			public void changed( ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState ) {
				if ( newState == Worker.State.SUCCEEDED ) {
					//Restore mouse-pointer:
					treevPerspectives.getScene().setCursor( Cursor.DEFAULT );

					//Populate "Perspectives" tree-view:
					populatePerspectiveTreeView();
					//Clear the editor pane:
					apaneContent.getChildren().clear();

					treevPerspectives.getSelectionModel().selectFirst();
				}
			}
		} );
		//Run the Task on a separate Thread:
		new Thread( getInsightManagerData ).start();
	}

	/**
	 * Same as "loadData(...)", but also fetches data for the Insight tab's
	 * "Display with" combo-box and the Parameter tab's "Value Type" combo-box:
	 * Designed to be run once when the Insight Manager is loaded initially.
	 */
	private boolean isDataLoaded;

	private void loadReferencesAndData() {
		isDataLoaded = false;

		//Convert mouse-pointer to a "wait" cursor:
		treevPerspectives.getScene().setCursor( Cursor.WAIT );

		//Define a Task to fetch an ArrayList of PlaySheets:
		Task<ObservableList<PlaySheet>> getPlaySheetData = new Task<ObservableList<PlaySheet>>() {
			@Override
			protected ObservableList<PlaySheet> call() throws Exception {
				arylPlaySheets = FXCollections.observableArrayList( engine.getInsightManager().getPlaySheets() );
				return arylPlaySheets;
			}
		};
		//Define a listener to load InsightManager data when Task completes,
		//but only if the PlaySheets have been loaded:
		getPlaySheetData.stateProperty().addListener( new ChangeListener<Worker.State>() {
			@Override
			public void changed( ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState ) {
				if ( newState == Worker.State.SUCCEEDED ) {
					if ( arylPlaySheets.size() > 0 ) {
						//Restore mouse-pointer:
						treevPerspectives.getScene().setCursor( Cursor.DEFAULT );

						//Load Insight Manager data if it has not already been loaded:
						if ( isDataLoaded == false ) {
							loadData();
							isDataLoaded = true;
						}
					}
				}
			}
		} );
		//Define a Task to fetch an ArrayList of Concept Value Types:
		Task<ObservableList<ParameterType>> getParameterTypeData = new Task<ObservableList<ParameterType>>() {
			@Override
			protected ObservableList<ParameterType> call() throws Exception {
				arylParameterTypes = FXCollections.observableArrayList( engine.getInsightManager().getParameterTypes() );
				return arylParameterTypes;
			}
		};
		//Define a listener to load Insight Manager data when Task completes,
		//but only if the Parameter Types have been loaded:
		getParameterTypeData.stateProperty().addListener( new ChangeListener<Worker.State>() {
			@Override
			public void changed( ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState ) {
				if ( newState == Worker.State.SUCCEEDED ) {
					if ( arylParameterTypes.size() > 0 ) {
						//Restore mouse-pointer:
						treevPerspectives.getScene().setCursor( Cursor.DEFAULT );
						//Load Insight Manager data if it has not already been loaded:
						if ( isDataLoaded == false ) {
							loadData();
							isDataLoaded = true;
						}
					}
				}
			}
		} );
		//Run the Tasks on a separate Threads:
		new Thread( getPlaySheetData ).start();
		new Thread( getParameterTypeData ).start();
	}

//----------------------------------------------------------------------------------------------------
//                          P e r s p e c t i v e   T r e e - V i e w
//----------------------------------------------------------------------------------------------------
	/**
	 * Populates the Perspective tree-view.
	 *
	 * All Perspectives and their Insights are loaded into this tree-view, so that
	 * the data can be used within the Insight Manager.
	 */
	protected void populatePerspectiveTreeView() {
		TreeItem<Object> rootItem = new TreeItem<>( "Perspectives", null );
		rootItem.setExpanded( true );
		for ( Perspective perspective : arylPerspectives ) {
			TreeItem<Object> perspectiveItem = new TreeItem<>( perspective );
			rootItem.getChildren().add( perspectiveItem );

			for ( Insight insight : perspective.getInsights() ) {
				InsightTreeItem<Object> insightItem;
				try {
					String strIcon = getInsightIcon( insight );
					Image image = new Image( strIcon );
					ImageView imageView = new ImageView( image );
					insightItem = new InsightTreeItem<>( insight, imageView );
				}
				catch ( Exception e ) {
					insightItem = new InsightTreeItem<>( insight, null );
				}
				perspectiveItem.getChildren().add( insightItem );

				for ( Parameter parameter : insight.getInsightParameters() ) {
					TreeItem<Object> parameterItem = new TreeItem<>( parameter );
					insightItem.getChildren().add( parameterItem );
				}
			}
		}
		treevPerspectives.setRoot( rootItem );
		treevPerspectives.setEditable( true );

		//Set double-click handler for the tree-view to display editors
		//for Perspectives, Insights, and Parameters:
		treevPerspectives.setOnMouseClicked( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( mouseEvent.getClickCount() == 1 ) {
					clickTreeItem();
				}
				mouseEvent.consume();
			}
		} );

		//CellFactory to enable click-and-drag of Insights and display of Insight icons:
		//------------------------------------------------------------------------------
		treevPerspectives.setCellFactory( new Callback<TreeView<Object>, TreeCell<Object>>() {
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
			public TreeCell<Object> call( TreeView<Object> stringTreeView ) {
				TreeCell<Object> treeCell = new TreeCell<Object>() {
					@Override
					protected void updateItem( Object item, boolean empty ) {
						super.updateItem( item, empty );
						if ( !empty && item != null ) {
							setText( item.toString() );
							setGraphic( getTreeItem().getGraphic() );
							//Set context menu:
							if ( getTreeItem().getValue().equals( "Perspectives" ) ) {
								setContextMenu( rootMenu );
							}
							else if ( getTreeItem().getValue() instanceof Perspective ) {
								setContextMenu( perspectiveMenu );
							}
							else if ( getTreeItem().getValue() instanceof Insight ) {
								setContextMenu( insightMenu );
							}
							else if ( getTreeItem().getValue() instanceof Parameter ) {
								setContextMenu( parameterMenu );
							}
							else {
								setContextMenu( null );
							}
						}
						else {
							setText( null );
							setGraphic( null );
						}
					}
				};

				//Begins dragging of object:
				//--------------------------
				treeCell.setOnDragDetected( new EventHandler<MouseEvent>() {
					@Override
					public void handle( MouseEvent mouseEvent ) {
						if ( treeCell.getItem() == null ) {
							return;
						}
						//Only allow Insights to be dragged:
						Object treeItem = treeCell.getItem();
						if ( treeItem instanceof Perspective ) {
							return;
						}
						//Determine transfer mode based on which radio-button is selected:
						if ( radioMove.isSelected() ) {
							transferMode = TransferMode.MOVE;
						}
						else if ( radioCopy.isSelected() ) {
							transferMode = TransferMode.COPY;
						}
						else {
							transferMode = TransferMode.MOVE;
						}
						Dragboard dragBoard = treeCell.startDragAndDrop( transferMode );
						ClipboardContent content = new ClipboardContent();
						content.put( insightFormat, treeItem );
						dragBoard.setContent( content );

						mouseEvent.consume();
					}
				} );

				//Indicates where drag transfer is possible:
				//------------------------------------------
				treeCell.setOnDragOver( new EventHandler<DragEvent>() {
					@Override
					public void handle( DragEvent dragEvent ) {
						TreeCell<Object> cellDraggedOver = treeCell;

						if ( dragEvent.getDragboard().hasContent( insightFormat )
								&& cellDraggedOver.getItem() instanceof Insight ) {
							Insight valueDragged = (Insight) dragEvent.getDragboard().getContent( insightFormat );
							if ( !valueDragged.equals( treeCell.getItem() ) ) {
								dragEvent.acceptTransferModes( transferMode );
							}
						}
						dragEvent.consume();
					}
				} );

				//Draws drag indication border:
				//-----------------------------
				treeCell.setOnDragEntered( new EventHandler<DragEvent>() {
					@Override
					public void handle( DragEvent dragEvent ) {
						treeCell.setStyle( "-fx-border-color: #111111 #111111 #111111 #111111" );

						dragEvent.consume();
					}
				} );

				//Clears drag indication border:
				//------------------------------
				treeCell.setOnDragExited( new EventHandler<DragEvent>() {
					@Override
					public void handle( DragEvent dragEvent ) {
						//Clear the drag insertion line:
						treeCell.setStyle( "-fx-border-color: transparent transparent transparent transparent" );

						dragEvent.consume();
					}
				} );

				//Transfers dragged object:
				//-------------------------
				treeCell.setOnDragDropped( new EventHandler<DragEvent>() {
					@Override
					public void handle( DragEvent dragEvent ) {
						Insight valueDragged = (Insight) dragEvent.getDragboard().getContent( insightFormat );

						TreeItem<Object> itemDragged = search( treevPerspectives.getRoot(), valueDragged );
						TreeItem<Object> itemTarget = treeCell.getTreeItem();

						ObservableList<TreeItem<Object>> olstOldInsights = itemDragged.getParent().getChildren();
						ObservableList<TreeItem<Object>> olstInsights = itemTarget.getParent().getChildren();
						int index = olstInsights.indexOf( itemTarget );
						if ( itemTarget.getValue() instanceof Perspective ) {
							return;
						}
						//If "Move Insight" is selected, then remove the Insight 
						//from its old location and place it where dropped:
						if ( transferMode.equals( TransferMode.MOVE ) ) {
							olstOldInsights.remove( itemDragged );
							olstInsights.add( index, itemDragged );

						}
						else {
							String strUniqueIdentifier = "_C" + String.valueOf( System.currentTimeMillis() );
							Insight insight = (Insight) itemDragged.getValue();
							RepositoryConnection rc;
							try {
								//Make a deep copy of the Insight, and assign a unique URI to it:
								rc = engine.getInsightManager().getRepository().getConnection();
								ValueFactory insightVF = rc.getValueFactory();
								Insight insightCopy = new Insight( insight );
								URI uriInsightCopy = insightVF.createURI( insight.getIdStr() + strUniqueIdentifier );
								insightCopy.setId( uriInsightCopy );
								TreeItem<Object> itemDraggedCopy = new TreeItem<>( insightCopy );

								//Assign unique URIs to Insight Parameters, and add Parameter 
								//children to the Insight tree item:
								for ( Parameter parameter : insightCopy.getInsightParameters() ) {
									parameter.setParameterId( parameter.getParameterURI() + strUniqueIdentifier );
									TreeItem<Object> item = new TreeItem<>( parameter );
									itemDraggedCopy.getChildren().add( item );
								}

								//Make a copy of the icon associated with "itemDragged":
								Image imageCopy = new Image( getInsightIcon( insightCopy ) );
								ImageView imageViewCopy = new ImageView( imageCopy );
								itemDraggedCopy.setGraphic( imageViewCopy );

								//Insert the copy of the dragged Insight into the ObservableList,
								//at the location dropped:
								olstInsights.add( index, itemDraggedCopy );

							}
							catch ( RepositoryException e ) {
								log.error( e, e );
							}
						}
						//Renumber Insights after move or copy:
						renumberInsights();

						dragEvent.consume();
					}
				} );

				//Completes drag operation:
				//-------------------------
				treeCell.setOnDragDone( new EventHandler<DragEvent>() {
					@Override
					public void handle( DragEvent dragEvent ) {
						dragEvent.consume();
					}
				} );
				return treeCell;
			}//End "call(...)".

			/**
			 * Builds context menus and menu-click-handlers for tree-view items (for
			 * the root item, Perspective items, Insight items, and Parameter items).
			 */
			private void buildContextMenus() {
				//Root menu:
				rootMenu = new ContextMenu();
				MenuItem rootItem = new MenuItem( "Add Perspective" );
				rootMenu.getItems().add( rootItem );
				rootItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						addPerspective();
					}
				} );
				//Perspective menu:
				perspectiveMenu = new ContextMenu();
				MenuItem perspectiveItem = new MenuItem( "Delete Perspective" );
				perspectiveMenu.getItems().add( perspectiveItem );
				perspectiveItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						deletePerspective();
					}
				} );
				perspectiveItem = new MenuItem( "Add Insight" );
				perspectiveMenu.getItems().add( perspectiveItem );
				perspectiveItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						addInsight();
					}
				} );
				//Insight menu:
				insightMenu = new ContextMenu();
				MenuItem insightItem = new MenuItem( "Delete Insight" );
				insightMenu.getItems().add( insightItem );
				insightItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						deleteInsight();
					}
				} );
				insightItem = new MenuItem( "Add Parameter" );
				insightMenu.getItems().add( insightItem );
				insightItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						addParameter();
					}
				} );
				//Parameter menu:
				parameterMenu = new ContextMenu();
				MenuItem parameterItem = new MenuItem( "Delete Parameter" );
				parameterMenu.getItems().add( parameterItem );
				parameterItem.setOnAction( new EventHandler<ActionEvent>() {
					@Override
					public void handle( ActionEvent t ) {
						deleteParameter();
					}
				} );
			}//End "buildContextMenus()".

		} );//End "treevPerspectives.setCellFactory".   

	}//End "populatePerspectiveTreeView()".

	/**
	 * Searches the tree-view data for the Insight passed-in, and returns the
	 * TreeItem associated with that Insight. Note: This is a recursive function
	 * that compares URI's in its search.
	 *
	 * @param currentNode -- (TreeItem<Object>) A node of the tree-view (usually
	 * is started at the root).
	 * @param valueToSearch -- (Insight) A value that may exist in the tree-view.
	 * @return search -- TreeItem<Object> The TreeItem associated with
	 * "valueToSearch".
	 */
	private TreeItem<Object> search( final TreeItem<Object> currentNode,
			final Insight valueToSearch ) {
		TreeItem<Object> result = null;

		if ( currentNode.getValue().equals( "Perspectives" ) == true
				|| currentNode.getValue() instanceof Perspective == true ) {
			for ( TreeItem<Object> child : currentNode.getChildren() ) {
				result = search( child, valueToSearch );
				if ( result != null ) {
					break;
				}
			}
		}
		else if ( currentNode.getValue() instanceof Insight
				&& ( (Insight) currentNode.getValue() ).getId().equals( valueToSearch.getId() ) ) {
			result = currentNode;
		}
		return result;
	}

	/**
	 * Renumbers the Insights under each Perspective TreeItem.
	 */
	private void renumberInsights() {
		ObservableList<TreeItem<Object>> olstPerspectives
				= treevPerspectives.getRoot().getChildren();

		for ( TreeItem<Object> treeItem : olstPerspectives ) {
			ObservableList<TreeItem<Object>> olstInsights = treeItem.getChildren();
			List<Insight> ordered = new ArrayList<>();
			for ( TreeItem<Object> ti : olstInsights ) {
				Insight ins = Insight.class.cast( ti.getValue() );
				ordered.add( ins );
			}

			Perspective perspective = Perspective.class.cast( treeItem.getValue() );
			perspective.setInsights( ordered );
		}
	}

	/**
	 * Adds a new empty Perspective to the top of the tree-view. (Called by
	 * "buildContextMenus()".)
	 */
	private void addPerspective() {
		//Build new Perspective, giving it a unique URI:
		//----------------------------------------------
		String strUniqueIdentifier = String.valueOf( System.currentTimeMillis() );
		Perspective perspective = new Perspective();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		}
		catch ( RepositoryException e ) {
			log.warn( e, e );
		}
		ValueFactory insightVF = rc.getValueFactory();
		URI uriPerspective = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS,
				"Perspective-" + strUniqueIdentifier );
		perspective.setUri( uriPerspective );
		perspective.setLabel( "(A New Perspective)" );

		//Add new Perspective to the tree-view:
		//-------------------------------------
		TreeItem<Object> item = new TreeItem<>( perspective );
		treevPerspectives.getRoot().getChildren().add( item );
		treevPerspectives.getRoot().getChildren().sort( Comparator.comparing( t -> t.toString() ) );

		//Select new Perspective and open its editor:
		//-------------------------------------------
		treevPerspectives.getSelectionModel().select( item );
		clickTreeItem();
	}

	/**
	 * Removes the selected Perspective from the tree-view if the response to the
	 * warning popup is OK. Also clears the editor in the right-pane if it
	 * represents or is contained by the deleted Perspective. (Called by
	 * "buildContextMenus()".)
	 */
	private void deletePerspective() {
		if ( GuiUtility.showWarningOkCancel( "Are you sure you want to delete this Perspective?" ) == 0 ) {
			TreeItem<Object> itemPerspective = treevPerspectives.getSelectionModel().getSelectedItem();
			clearRightPane( itemPerspective );
			treevPerspectives.getRoot().getChildren().remove( itemPerspective );
		}
	}

	/**
	 * Adds a new empty Insight to the top of the current Perspective's Insight
	 * list. (Called by "buildContextMenus()".)
	 */
	private void addInsight() {
		//Build new Insight, giving it a unique URI:
		//------------------------------------------
		String strUniqueIdentifier = String.valueOf( System.currentTimeMillis() );
		Insight insight = new Insight();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		}
		catch ( RepositoryException e ) {
			log.warn( e, e );
		}
		ValueFactory insightVF = rc.getValueFactory();
		URI uriInsight = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS,
				"Insight-" + strUniqueIdentifier );
		String now = new Date().toString();

		insight.setId( uriInsight );
		insight.setLabel( "(A New Insight)" );
		//insight.setOrder( 0 );
		insight.setOutput( "gov.va.semoss.ui.components.playsheets.GridPlaySheet" );
		insight.setCreator( engine.getWriteableInsightManager().userInfoFromToolPreferences( "V-CAMP/SEMOSS Insight Manager" ) );
		insight.setCreated( now );
		insight.setModified( now );

		//Add new Insight to the tree-view:
		//---------------------------------
		Image image = new Image( getInsightIcon( insight ) );
		ImageView imageView = new ImageView( image );
		InsightTreeItem<Object> item = new InsightTreeItem<>( insight, imageView );
		treevPerspectives.getSelectionModel().getSelectedItem().getChildren().add( 0, item );
		renumberInsights();

		Perspective perspective = (Perspective) item.getParent().getValue();
		perspective.getInsights().add( insight );

		//Select new Insight and open its editor:
		//---------------------------------------
		treevPerspectives.getSelectionModel().select( item );
		clickTreeItem();
	}

	/**
	 * Removes the selected Insight from the tree-view and from its parent
	 * Perspective if the response to the warning popup is OK. Also clears the
	 * editor in the right-pane if it represents or is contained by the deleted
	 * Insight. (Called by "buildContextMenus()".)
	 */
	private void deleteInsight() {
		if ( GuiUtility.showWarningOkCancel( "Are you sure you want to delete this Insight?" ) == 0 ) {
			TreeItem<Object> itemInsight = treevPerspectives.getSelectionModel().getSelectedItem();
			Insight insight = (Insight) itemInsight.getValue();
			TreeItem<Object> itemPerspective = treevPerspectives.getSelectionModel()
					.getSelectedItem().getParent();
			Perspective perspective = (Perspective) itemPerspective.getValue();
			perspective.getInsights().remove( insight );
			clearRightPane( itemInsight );
			itemPerspective.getChildren().remove( itemInsight );
			renumberInsights();
		}
	}

	/**
	 * Adds a new empty Parameter to the bottom of the current Insight's Parameter
	 * list. (Called by "buildContextMenus()".)
	 */
	private void addParameter() {
		//Build new Parameter, giving it a unique URI:
		//-------------------------------------------
		String strUniqueIdentifier = "_" + String.valueOf( System.currentTimeMillis() );
		Parameter parameter = new Parameter();
		RepositoryConnection rc = null;
		try {
			rc = engine.getInsightManager().getRepository().getConnection();
		}
		catch ( RepositoryException e ) {
			log.warn( e, e );
		}
		ValueFactory insightVF = rc.getValueFactory();
		URI uriParameter = insightVF.createURI( MetadataConstants.VA_INSIGHTS_NS,
				"Parameter" + strUniqueIdentifier );
		parameter.setParameterId( uriParameter );
		parameter.setLabel( "(A New Parameter)" );

		//Add new Parameter to the tree-view:
		//-----------------------------------
		InsightTreeItem<Object> insightTreeItem = (InsightTreeItem<Object>) treevPerspectives.getSelectionModel().getSelectedItem();
		Insight insight = (Insight) insightTreeItem.getValue();
		ObservableList<TreeItem<Object>> olstInsightParameters = insightTreeItem.getChildren();
		ArrayList<Parameter> arylParameters = (ArrayList<Parameter>) insight.getInsightParameters();
		TreeItem<Object> parameterTreeItem = new TreeItem<Object>( parameter );
		//First update the Insight's list of Parameters:
		if ( arylParameters.size() > 0 && arylParameters.get( 0 ).toString().equals( "" ) ) {
			arylParameters.set( 0, parameter );
		}
		else {
			arylParameters.add( parameter );
		}
		//Then update the InsightTreeItem's children list:
		if ( olstInsightParameters.size() > 0 && olstInsightParameters.get( 0 ).getValue().toString().equals( "" ) ) {
			olstInsightParameters.set( 0, parameterTreeItem );
		}
		else {
			olstInsightParameters.add( parameterTreeItem );
		}

		//Select new Parameter and open its editor:
		//-----------------------------------------
		treevPerspectives.getSelectionModel().select( parameterTreeItem );
		clickTreeItem();
	}//End "addParameter()".

	/**
	 * Removes the selected Parameter from the tree-view if the response to the
	 * warning popup is OK. Also clears the editor in the right-pane if it
	 * represents or is contained by the deleted Parameter. (Called by
	 * "buildContextMenus()".)
	 */
	private void deleteParameter() {
		if ( GuiUtility.showWarningOkCancel( "Are you sure you want to delete this Parameter?" ) == 0 ) {
			TreeItem<Object> itemParameter = treevPerspectives.getSelectionModel().getSelectedItem();
			TreeItem<Object> itemInsight = itemParameter.getParent();
			Insight insight = (Insight) itemInsight.getValue();
			ArrayList<Parameter> arylParameters = (ArrayList<Parameter>) insight.getInsightParameters();
			arylParameters.remove( (Parameter) itemParameter.getValue() );
			clearRightPane( itemParameter );
			itemInsight.getChildren().remove( itemParameter );
		}
	}

	/**
	 * Clears the right pane when a TreeItem is deleted, if that TreeItem
	 * represents or contains the data displayed in the right pane.
	 *
	 * Note: This is a recursive function.
	 *
	 * @param treeItem -- (TreeItem<Object>) TreeItem to be deleted.
	 */
	private void clearRightPane( TreeItem<Object> treeItem ) {
		URI uriTreeItem = null;
		Object objTreeItem = treeItem.getValue();
		URI uriController = null;
		if ( loaderRightPane == null ) {
			return;
		}
		Object objController = loaderRightPane.getController();

		//Get the URI of the object (Parameter, Insight, or Perspective)
		//behind the TreeItem passed in:
		if ( objTreeItem instanceof Perspective ) {
			uriTreeItem = ( (Perspective) objTreeItem ).getUri();
		}
		else if ( objTreeItem instanceof Insight ) {
			uriTreeItem = ( (Insight) objTreeItem ).getId();
		}
		else if ( objTreeItem instanceof Parameter ) {
			uriTreeItem = ( (Parameter) objTreeItem ).getParameterId();
		}

		//Get the URI of the object (Parameter, Insight, or Perspective)
		//displayed in the right-pane:
		if ( objController instanceof PerspectiveEditorController ) {
			uriController = ( (PerspectiveEditorController) objController ).itemURI;
		}
		else if ( objController instanceof InsightEditorController ) {
			uriController = ( (InsightEditorController) objController ).itemURI;
		}
		else if ( objController instanceof ParameterEditorController ) {
			uriController = ( (ParameterEditorController) objController ).itemURI;
		}

		if ( uriTreeItem != null && uriController != null
				&& uriTreeItem.equals( uriController ) ) {
			apaneContent.getChildren().clear();

		}
		else {
			for ( TreeItem<Object> treeItem_2 : treeItem.getChildren() ) {
				clearRightPane( treeItem_2 );
			}
		}
	}//End "clearRightPane(...)".

	/**
	 * Class to override the ".isLeaf()" method for Insight tree-items. We need to
	 * be sure that no Parameters are listed under the Insight.
	 *
	 * @author Thomas
	 *
	 * @param <T>
	 */
	public class InsightTreeItem<T> extends TreeItem<Object> {

		private Insight insight = null;

		public InsightTreeItem( Insight insight, ImageView imageView ) {
			super( insight, imageView );
			this.insight = insight;
		}

		@Override
		public boolean isLeaf() {
			boolean boolReturnValue = true;

			if ( insight.getInsightParameters().size() > 0
					&& ( (ArrayList<?>) insight.getInsightParameters() ).get( 0 ).toString().equals( "" ) == false ) {
				boolReturnValue = false;
			}
			return boolReturnValue;
		}
	}

	/**
	 * Returns a PlaySheet icon for the passed-in Insight.The array-list of
	 * PlaySheets is consulted. The base path to all icons is defined at the top
	 * of this class, in "ICON_LOCATION".
	 *
	 * @param insight -- (Insight) The Insight for which an icon is required.
	 *
	 * @return getInsightIcon -- (String) The file-path to the icon, described
	 * above.
	 */
	private String getInsightIcon( Insight insight ) {
		String strReturnValue = "";

		for ( PlaySheet playsheet : arylPlaySheets ) {
			if ( insight.getOutput().equals( playsheet.getViewClass() ) ) {
				if ( playsheet.getIcon() != null ) {
					strReturnValue = ICON_LOCATION + playsheet.getIcon();
				}
				break;
			}
		}
		return strReturnValue;
	}

	/**
	 * When double-clicking a TreeItem, we must load the right-pane with the
	 * contents of the item clicked. Provides dynamic loading of FXML editors with
	 * their controllers.
	 */
	private void clickTreeItem() {
		PerspectiveEditorController contPerspectiveEditor = null;
		InsightEditorController contInsightEditor = null;
		ParameterEditorController contParameterEditor = null;
		TreeItem<Object> item = treevPerspectives.getSelectionModel().getSelectedItem();
		apaneContent.getChildren().clear();
		try {
			if ( item.getValue() instanceof Perspective ) {
				loaderRightPane = new FXMLLoader( getClass().getResource( "/fxml/PerspectiveEditor.fxml" ) );
				apaneContent.getChildren().add( loaderRightPane.load() );
				contPerspectiveEditor = loaderRightPane.getController();
				contPerspectiveEditor.setData( treevPerspectives );

			}
			else if ( item.getValue() instanceof Insight ) {
				loaderRightPane = new FXMLLoader( getClass().getResource( "/fxml/InsightEditor.fxml" ) );
				apaneContent.getChildren().add( loaderRightPane.load() );
				contInsightEditor = loaderRightPane.getController();
				contInsightEditor.setData( treevPerspectives, arylPlaySheets );

			}
			else if ( item.getValue() instanceof Parameter ) {
				loaderRightPane = new FXMLLoader( getClass().getResource( "/fxml/ParameterEditor.fxml" ) );
				apaneContent.getChildren().add( loaderRightPane.load() );
				contParameterEditor = loaderRightPane.getController();
				contParameterEditor.setData( treevPerspectives, arylParameterTypes );
			}
		}
		catch ( IOException e ) {
			log.warn( e, e );
		}
	}

	/**
	 * Click-handler for the "Save" button, beneath the TreeView. Fires methods to
	 * rebuild the Insights KB from the contents of the Insight Manager TreeView.
	 * Then reloads the TreeView from the database.
	 *
	 * @param event -- (ActionEvent)
	 */
	private void handleSave( ActionEvent event ) {
		ObservableList<TreeItem<Object>> olstPerspectives = treevPerspectives.getRoot().getChildren();

		pbSaveReload.setVisible( true );
		//Define a Task to persist the TreeView's data:
		Task<ObservableValue<Boolean>> doPersistence = new Task<ObservableValue<Boolean>>() {
			@Override
			protected ObservableValue<Boolean> call() throws Exception {
				ObservableValue<Boolean> oboolReturnValue;
				WriteableInsightManager wim = engine.getWriteableInsightManager();
				oboolReturnValue = new SimpleBooleanProperty( persistenceWrapper( olstPerspectives, wim ) ).asObject();
				if ( !oboolReturnValue.getValue() ) {
					updateProgress( -1.0, 1.0 );

					//Try to import Insights into the database:
				}
				else {
					if ( EngineUtil.getInstance().importInsights( wim ) ) {
						//This is necessary to make sure that the Insight Manager loads
						//only after the left-pane is completely loaded:
						synchronized ( guiUpdateMonitor ) {
							while ( !boolLeftPaneUpdated ) {
								try {
									guiUpdateMonitor.wait();

								}
								catch ( InterruptedException e ) {
									boolLeftPaneUpdated = false;
								}
							}
							boolLeftPaneUpdated = false;
						}
						updateProgress( 1.0, 1.0 );
						Thread.sleep( 1000 );
						loadData();
						oboolReturnValue = new SimpleBooleanProperty( true ).asObject();
					}
					else {
						oboolReturnValue = new SimpleBooleanProperty( false ).asObject();
					}
				}
				return oboolReturnValue;
			}
		};
		//Define a listener to display status when the  Task completes:
		doPersistence.stateProperty().addListener( new ChangeListener<Worker.State>() {
			@Override
			public void changed( ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState ) {
				if ( newState == Worker.State.SUCCEEDED ) {
					if ( doPersistence.getValue().getValue().booleanValue() == true ) {
						GuiUtility.showMessage( "Perspective, Insights, and Parameters saved." );
					}
					else {
						GuiUtility.showError( "ERROR: Some Perspectives, Insights, and/or Parameters could not be saved." );
					}
					pbSaveReload.setVisible( false );
				}
			}
		} );
		//Run the Task on a separate Thread:
		new Thread( doPersistence ).start();
		pbSaveReload.progressProperty().bind( doPersistence.progressProperty() );
	}

	/**
	 * Reloads the Insight Manager TreeView from the database.
	 *
	 * @param event -- (ActionEvent)
	 */
	private void handleReload( ActionEvent event ) {
		loadData();
	}

	/**
	 * Loops through each Perspective of the Insight Manager's TreeView, and
	 * persists data (Perspectives, Insights, and Parameters) to the Insights KB
	 * on disk. This method first deletes all Perspectives, Insights, and
	 * Parameters from the KB. Then it attempts to Insert the TreeView data.
	 * Should this method encounter an object that cannot be persisted, false is
	 * returned, and further processing is halted. Otherwise, true is returned.
	 *
	 * @param olstPerspective -- (ObservableList<TreeItem<Object>>) Children of
	 * the root of the TreeView.
	 *
	 * @param wim -- (WriteableInsightManager) Methods to persist changes to the
	 * Insights KB.
	 */
	private boolean persistenceWrapper( ObservableList<TreeItem<Object>> olstPerspectives,
			WriteableInsightManager wim ) {
		boolean boolReturnValue = true;

		if ( !wim.deleteAllParameters() || !wim.deleteAllInsights() || !wim.deleteAllPerspectives() ) {
			boolReturnValue = false;

		}
		else {
			try {
				for ( TreeItem<Object> treeItem : olstPerspectives ) {
					Perspective perspective = (Perspective) treeItem.getValue();
					wim.savePerspective( perspective );

					ObservableList<TreeItem<Object>> insightItems = treeItem.getChildren();
					if ( !insightItems.isEmpty() ) {
						for ( TreeItem<Object> iitem : insightItems ) {
							Insight insight = Insight.class.cast( iitem.getValue() );
							wim.saveInsight( perspective, insight );

							ObservableList<TreeItem<Object>> paramItems = iitem.getChildren();
							if ( !paramItems.isEmpty() ) {
								for ( TreeItem<Object> pitem : paramItems ) {
									Parameter parameter = Parameter.class.cast( pitem.getValue() );
									wim.saveParameter( insight, parameter );
								}
							}
						}
					}
				}
			}
			catch ( Exception e ) {
				boolReturnValue = false;
			}
		}
		return boolReturnValue;
	}

}//End "InsightManagerController_2.java"
