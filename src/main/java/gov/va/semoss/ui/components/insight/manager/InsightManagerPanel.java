package gov.va.semoss.ui.components.insight.manager;

import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class InsightManagerPanel extends JPanel {
	private static final long serialVersionUID = 6184892035434769170L; 
    private final JFXPanel jfxPanel = new JFXPanel();
 
    /**   Creates the Scene-graph within the JFXPanel, 
     * and adds the JFXPanel to this JPanel.
     * 
     */
    protected void initComponents() {
        createScene();
        add(jfxPanel);
    }
 
    /**   Creates the Scene-graph on the JavaFX thread.
     * 
     */
    private void createScene() {
		Platform.setImplicitExit(false);
 
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try{
          		   if(DIHelper.getInstance().getRdfEngine() != null){
                      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InsightManagerPanel.fxml"));
        		      Parent root = loader.load();
        		      InsightManagerController imController = loader.getController();
        		      Scene scene = new Scene((Parent) root);
               		  jfxPanel.setScene(scene);
        		      imController.setData();
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