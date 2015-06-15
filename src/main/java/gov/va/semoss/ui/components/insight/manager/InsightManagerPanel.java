package gov.va.semoss.ui.components.insight.manager;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.util.DIHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class InsightManagerPanel extends JPanel{
	private static final long serialVersionUID = 6184892035434769170L; 
    private final JFXPanel jfxPanel = new JFXPanel();
 
    /**   Creates the Scene-graph within the JFXPanel, 
     * and adds the JFXPanel to this JPanel.
     * 
     */
    protected void initComponents() {
        createScene();
        add(jfxPanel);
        //When this component is manually resized, set the PreferredSize
        //of the "jfxPanel" accordingly:
		addComponentListener(new ComponentAdapter() {
	        public void componentResized(ComponentEvent e) {
	        	//Handles resizes by Maximize or Restore:
				revalidate();
				
	        	jfxPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));
	        }
	    });
    }
 
    /**   Creates the Scene-graph on the JavaFX thread.
     * 
     */
    private void createScene() {
		Platform.setImplicitExit(false);
 
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	FXMLLoader loader;
            	Parent root;
            	InsightManagerController imController;
            	Scene scene;
                try{
                   //If an engine is loaded, then show the Insight Manager:
          		   if(DIHelper.getInstance().getRdfEngine() != null){
                      loader = new FXMLLoader(getClass().getResource("/fxml/InsightManagerPanel.fxml"));
        		      root = loader.load();
        		      imController = loader.getController();
        		      scene = new Scene((Parent) root);
               		  jfxPanel.setScene(scene);
        		      imController.setData();
        			  jfxPanel.setVisible(true);
 
        			  //When the width or height of the scene has changed, access the TabbedPane
        			  //via the Controller, and set its width or height accordingly:
        			  scene.widthProperty().addListener(new ChangeListener<Number>() {
    				     @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
     				        imController.tbpTabbedPane.setPrefWidth((double) newSceneWidth);
    				     }
        			  });
        			  scene.heightProperty().addListener(new ChangeListener<Number>() {
    				     @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
     				        imController.tbpTabbedPane.setPrefHeight((double) newSceneHeight);
    				     }
        			  });
 
       		       //If no engine is loaded, then release all Insight Manager load variables,
        		   //and show nothing:
        		   }else{
        			  jfxPanel.setVisible(false);
                      loader = null;
                      root = null;
                      imController = null;
                	  jfxPanel.setScene(null);
                	  jfxPanel.validate();
        		   }
                }catch(Exception e){
                	e.printStackTrace();
                }
            }
        });
    }
 
    /**   Constructs the JPanel, containing the JFXPanel, on the Swing thread.
     * 
     * @param repoList -- (RepositoryList) The currently displayed database list.
     */
    public InsightManagerPanel(RepositoryList repoList) {
	    insightManagerPanelWorker();
	    
    	//Listen for changes in the passed-in repository-list,
    	//and reconstruct this panel accordingly:
	    repoList.addListSelectionListener( new ListSelectionListener(){
		    @Override
		    public void valueChanged(ListSelectionEvent e){
	    	    insightManagerPanelWorker();
		    }
	    });
    }


    /**   Constructs this panel on the Swing thread. Called by the constructor,
     * and externally, where a new object need not be constructed.
     */
    public void insightManagerPanelWorker(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initComponents();
            }
        });
    }
}