/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.ui.components.renderers.QuestionRenderer;
import gov.va.semoss.ui.components.renderers.PerspectiveRenderer;
import gov.va.semoss.ui.components.renderers.RepositoryRenderer;
import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineOperationAdapter;
import gov.va.semoss.rdf.engine.util.EngineOperationListener;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.insight.manager.InsightManagerController_2;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.helpers.NonLegacyQueryBuilder;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SelectDatabasePanel extends javax.swing.JPanel {
	private static final long serialVersionUID = 5868154574694278392L;
	private static final Logger log = Logger.getLogger( SelectDatabasePanel.class );
	private final ExecuteQueryProcessor insightAction = new InsightAction();
	private ParamPanel currentParamPanel = null;

	public SelectDatabasePanel() {
		this( false );
	}

	public SelectDatabasePanel( boolean neverEmpty ) {
		initComponents();

		if ( neverEmpty ) {
			JViewport vp = reposcroller.getViewport();
			vp.remove( repoList );
			vp.add( repoList.getNeverEmptyLayer() );
		}

		paramLabel.setVisible( false );
		repoList.setCellRenderer( new RepositoryRenderer() );

		final PerspectiveRenderer pr = new PerspectiveRenderer();
		perspectiveSelector.setRenderer( pr );
		perspectiveSelector.setToolTipText( "Select the point-of-view of the question you want to ask" );
		perspectiveSelector.setBackground( new Color( 119, 136, 153 ) );

		final QuestionRenderer qr = new QuestionRenderer();
		questionSelector.setRenderer( qr );
		questionSelector.setToolTipText( "Select the specific question you want to ask" );
		questionSelector.setBackground( new Color( 119, 136, 153 ) );

		submitButton.setBackground( new Color( 0x51a351 ) );

		EngineOperationListener eol = new EngineOperationAdapter() {

			@Override
			public void insightsModified( IEngine eng, Collection<URI> perspectives,
					Collection<URI> insights ) {
				if ( repoList.getSelectedValue().equals( eng ) ) {
					perspectiveSelector.removeAllItems();
					for ( Perspective uri : eng.getInsightManager().getPerspectives() ) {
						perspectiveSelector.addItem( uri );
					}
					//This is necessary to make sure that the Insight Manager loads
					//after the left-pane is completely loaded:
                    Object monitor = InsightManagerController_2.guiUpdateMonitor;
                    InsightManagerController_2.boolLeftPaneUpdated = true;
					synchronized(monitor){
					    monitor.notifyAll();
					}
				}
			}
		};
		EngineUtil.getInstance().addEngineOpListener( eol );

		repoList.addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( ListSelectionEvent lse ) {
				IEngine eng = repoList.getSelectedValue();
				pr.setEngine( eng );
				qr.setEngine( eng );

				perspectiveSelector.removeAllItems();
				if ( null != eng ) {
					for ( Perspective uri : eng.getInsightManager().getPerspectives() ) {
						perspectiveSelector.addItem( uri );
					}
				}
			}
		} );

		questionSelector.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				Insight ii = questionSelector.getItemAt( questionSelector.getSelectedIndex() );
				if ( null != ii ) {
					paramLabel.setVisible( !ii.getParametersKeySet().isEmpty() );
					//Determine whether to enable/disable the "Overlay" CheckBox, based upon
					//how the renderer of the selected visualization compares with that of the 
					//currently selected question:
					JDesktopPane pane = DIHelper.getInstance().getDesktop();
					PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );

					PlaySheetCentralComponent pscc = ( null == psf ? null
							: psf.getActivePlaySheet() );
					String psccClass = ( null == pscc ? ""
							: pscc.getClass().getCanonicalName() );
					appendChkBox.setEnabled( ii.getOutput().equals( psccClass ) );
					setupForInsight( ii );
				}
			}
		} );

		perspectiveSelector.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				Perspective persp
						= perspectiveSelector.getItemAt( perspectiveSelector.getSelectedIndex() );
				log.debug( "selected perspective: " + persp );
				if ( null != persp ) {
					qr.setPerspective( persp );
				}
				questionSelector.removeAllItems();
				IEngine eng = repoList.getSelectedValue();
				if ( !( null == eng || null == persp ) ) {
					for ( Insight insight : eng.getInsightManager().getInsights( persp ) ) {
						questionSelector.addItem( insight );
					}
				}
			}
		} );

		submitButton.setToolTipText( "Execute SPARQL query for selected question and display results in Display Pane" );
		Image insightIcon = GuiUtility.loadImage( "icons16/insight_16.png" );
		submitButton.setIcon( new ImageIcon( insightIcon ) );
		submitButton.setAction( insightAction );
		submitButton.setForeground( Color.WHITE );

		appendChkBox.setToolTipText( "Display the question results graph on top of graph in the foreground window" );
	}

	public JCheckBox getOverlay() {
		return appendChkBox;
	}

	public JComboBox<Perspective> getPerspectiveSelector() {
		return perspectiveSelector;
	}

	public JComboBox<Insight> getInsightSelector() {
		return questionSelector;
	}

	public JButton getSubmitButton() {
		return submitButton;
	}

	public JPanel getParamPanel() {
		return paramPanel;
	}

	public RepositoryList getRepoList() {
		return repoList;
	}

	public void setLabelsFont( Font f ) {
		for ( JLabel l : new JLabel[]{ oneLabel, twoLabel, threeLabel, paramLabel } ) {
			l.setFont( f );
		}
	}

	private void setupForInsight( Insight question ) {
		if ( question != null ) {
			String sparql = question.getSparql();
			log.debug( "Sparql is " + sparql );

			currentParamPanel = new ParamPanel();
			currentParamPanel.setInsight( question );
			currentParamPanel.paintParam();

			// finally add the param to the core panel
			// confused about how to add this need to revisit
			JPanel env = new JPanel();
			env.add( currentParamPanel );
			paramPanel.add( currentParamPanel, question + "_1" ); // mark it to the question index
			CardLayout layout = (CardLayout) paramPanel.getLayout();
			layout.show( paramPanel, question + "_1" );
		}
	}

	private Map<String, String> getParameterValues() {
		Map<String, String> paramHash = new HashMap<>();
		if ( null != currentParamPanel ) {
			// get all the param field
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

	public Action getInsightAction() {
		return insightAction;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
    repoPanel = new javax.swing.JPanel();
    oneLabel = new javax.swing.JLabel();
    reposcroller = new javax.swing.JScrollPane();
    repoList = DIHelper.getInstance().getRepoList();
    javax.swing.JPanel insightsPanel = new javax.swing.JPanel();
    twoLabel = new javax.swing.JLabel();
    perspectiveSelector = new javax.swing.JComboBox<Perspective>();
    threeLabel = new javax.swing.JLabel();
    questionSelector = new javax.swing.JComboBox<Insight>();
    paramPanel = new javax.swing.JPanel();
    submitButton = new javax.swing.JButton();
    appendChkBox = new javax.swing.JCheckBox();
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

    setBackground(java.awt.Color.white);

    jSplitPane1.setBackground(java.awt.Color.white);
    jSplitPane1.setDividerLocation(125);
    jSplitPane1.setDividerSize(2);
    jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setResizeWeight(0.5);
    jSplitPane1.setToolTipText("");
    jSplitPane1.setContinuousLayout(true);

    repoPanel.setBackground(java.awt.Color.white);
    repoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
    repoPanel.setMinimumSize(new java.awt.Dimension(100, 100));
    repoPanel.setPreferredSize(new java.awt.Dimension(275, 150));
    repoPanel.setLayout(new java.awt.BorderLayout());

    oneLabel.setText("1. Select a database to explore:");
    repoPanel.add(oneLabel, java.awt.BorderLayout.PAGE_START);

    reposcroller.setViewportView(repoList);

    repoPanel.add(reposcroller, java.awt.BorderLayout.CENTER);

    jSplitPane1.setLeftComponent(repoPanel);

    insightsPanel.setBackground(java.awt.Color.white);

    twoLabel.setText("2. Select the perspective to view:");

    threeLabel.setText("3. Select a specific insight:");

    paramLabel.setText("4. Select available parameters:");

    paramPanel.setBackground(java.awt.Color.gray);
    paramPanel.setLayout(new java.awt.CardLayout());

    submitButton.setText("Get Insight!");

    appendChkBox.setText("Overlay");
    appendChkBox.setEnabled(false);
    appendChkBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    appendChkBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

    javax.swing.GroupLayout insightsPanelLayout = new javax.swing.GroupLayout(insightsPanel);
    insightsPanel.setLayout(insightsPanelLayout);
    insightsPanelLayout.setHorizontalGroup(
      insightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(insightsPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(insightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(paramPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(perspectiveSelector, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(twoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addComponent(questionSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(threeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(paramLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, insightsPanelLayout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(insightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(appendChkBox, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(submitButton, javax.swing.GroupLayout.Alignment.TRAILING))))
        .addContainerGap())
    );
    insightsPanelLayout.setVerticalGroup(
      insightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(insightsPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(twoLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(perspectiveSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(threeLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(questionSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(paramLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(paramPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(submitButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(appendChkBox)
        .addGap(0, 0, Short.MAX_VALUE))
    );

    jSplitPane1.setRightComponent(insightsPanel);

    jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
    jLabel1.setText("Select Database");
    jLabel1.setToolTipText("Find a pre-written question");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jSplitPane1)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox appendChkBox;
  private javax.swing.JLabel oneLabel;
  private final javax.swing.JLabel paramLabel = new javax.swing.JLabel();
  private javax.swing.JPanel paramPanel;
  javax.swing.JComboBox<Perspective> perspectiveSelector;
  javax.swing.JComboBox<Insight> questionSelector;
  private gov.va.semoss.ui.components.RepositoryList repoList;
  private javax.swing.JPanel repoPanel;
  private javax.swing.JScrollPane reposcroller;
  private javax.swing.JButton submitButton;
  private javax.swing.JLabel threeLabel;
  private javax.swing.JLabel twoLabel;
  // End of variables declaration//GEN-END:variables

	private class InsightAction extends ExecuteQueryProcessor {
	    private static final long serialVersionUID = -5360951711543979184L;

		public InsightAction() {
			super( "Create Insight!" );
		}

		@Override
		protected String getTitle() {
			Perspective persp
					= perspectiveSelector.getItemAt( perspectiveSelector.getSelectedIndex() );
			Insight insight = questionSelector.getItemAt( questionSelector.getSelectedIndex() );
			return persp.getLabel() + "-Insight-" + persp.indexOf( insight );
		}

		@Override
		protected String getFrameTitle() {
			return questionSelector.
					getItemAt( questionSelector.getSelectedIndex() ).getLabel();
		}

		@Override
		protected String getQuery() {
			Insight insight = questionSelector.getItemAt( questionSelector.getSelectedIndex() );
			String sparql = Utility.normalizeParam( insight.getSparql() );

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
			Insight insight = questionSelector.getItemAt( questionSelector.getSelectedIndex() );
			String output = insight.getOutput();

			return (Class<PlaySheetCentralComponent>) Class.forName( output );
		}

		@Override
		protected IEngine getEngine() {
			return repoList.getSelectedValue();
		}

		@Override
		protected boolean isAppending() {
			return appendChkBox.isSelected();
		}
	}
}
