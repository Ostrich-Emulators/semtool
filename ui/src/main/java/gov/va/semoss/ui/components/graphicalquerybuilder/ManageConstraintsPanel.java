/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.NodeDerivationTools;
import gov.va.semoss.ui.components.graphicalquerybuilder.GraphicalQueryPanel.QueryOrder;
import gov.va.semoss.ui.components.renderers.LabeledPairTableCellRenderer;
import gov.va.semoss.util.Constants;

import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.Utility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class ManageConstraintsPanel extends javax.swing.JPanel {

  private final Logger log = Logger.getLogger( ManageConstraintsPanel.class );
  private final IEngine engine;
  private final SparqlResultTableModel model;
  private final DirectedGraph<QueryNode, QueryEdge> graph;
  private final FilterRenderer filterRenderer;

  /**
   * Creates new form ManageConstraintsPanel
   *
   * @param engine
   * @param graph
   * @param ordering
   */
  public ManageConstraintsPanel( IEngine engine,
		  DirectedGraph<QueryNode, QueryEdge> graph, List<QueryOrder> ordering ) {
	this.engine = engine;
	this.graph = graph;
	List<QueryGraphElement> elements = new ArrayList<>( graph.getVertices() );
	elements.addAll( graph.getEdges() );
	model = new SparqlResultTableModel( elements, ordering );
	filterRenderer = new FilterRenderer( model );

	initComponents();

	LabeledPairTableCellRenderer renderer
			= LabeledPairTableCellRenderer.getUriPairRenderer();
	Set<URI> labels = getAllProperties( elements );
	renderer.cache( GuiUtility.getInstanceLabels( labels, engine ) );

	LabeledPairTableCellRenderer trenderer
			= LabeledPairTableCellRenderer.getValuePairRenderer( engine );
	trenderer.cache( Constants.ANYNODE, "<Any>" );

	table.setFillsViewportHeight( true );

	table.setDefaultRenderer( URI.class, renderer );
	table.setDefaultRenderer( Value.class, trenderer );

	table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {

	  @Override
	  public void valueChanged( ListSelectionEvent e ) {
		upbtn.setEnabled( !table.getSelectionModel().isSelectionEmpty() );
		downbtn.setEnabled( !table.getSelectionModel().isSelectionEmpty() );
	  }
	} );

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    table = new ConstraintTable(engine, graph);
    upbtn = new javax.swing.JButton();
    downbtn = new javax.swing.JButton();

    table.setModel(model);
    table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane1.setViewportView(table);

    upbtn.setText("Move Up");
    upbtn.setEnabled(false);
    upbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        upbtnActionPerformed(evt);
      }
    });

    downbtn.setText("Move Down");
    downbtn.setEnabled(false);
    downbtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        downbtnActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(downbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(upbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 0, 0))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, Short.MAX_VALUE))
      .addGroup(layout.createSequentialGroup()
        .addGap(27, 27, 27)
        .addComponent(upbtn)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(downbtn)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void upbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upbtnActionPerformed
	// TODO add your handling code here:
	swap( -1 );
  }//GEN-LAST:event_upbtnActionPerformed

  private void downbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downbtnActionPerformed
	swap( 1 );
  }//GEN-LAST:event_downbtnActionPerformed

  private void swap( int diff ) {
	int from = table.getSelectedRow();
	int to = from + diff;
	model.swap( from, to );
	table.getSelectionModel().setSelectionInterval( to, to );
  }

  public List<QueryOrder> getQueryOrdering() {
	return model.getQueryOrdering();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton downbtn;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTable table;
  private javax.swing.JButton upbtn;
  // End of variables declaration//GEN-END:variables

  private static Set<URI> getAllProperties( Collection<QueryGraphElement> data ) {
	Set<URI> props = new HashSet<>();
	for ( QueryGraphElement b : data ) {
	  props.addAll( b.getAllValues().keySet() );
	}
	return props;
  }

  private class ConstraintTable extends JTable {

	private final List<URI> concepts;
	private final Map<URI, String> conceptmap;
	private final IEngine engine;
	private final ValueEditor types = new ValueEditor();
	private final ValueEditor normal = new ValueEditor();
	private final FilterEditor filterEditor = new FilterEditor();
	private final DirectedGraph<QueryNode, QueryEdge> graph;

	public ConstraintTable( IEngine eng, DirectedGraph<QueryNode, QueryEdge> gr ) {
	  super();
	  concepts = NodeDerivationTools.instance().createConceptList( eng );
	  conceptmap
			  = Utility.sortUrisByLabel( GuiUtility.getInstanceLabels( concepts, eng ) );
	  engine = eng;
	  graph = gr;
	}

	@Override
	public TableCellRenderer getCellRenderer( int row, int column ) {
	  return ( 6 == column ? filterRenderer
			  : super.getCellRenderer( row, column ) );
	}

	@Override
	public TableCellEditor getCellEditor( int row, int column ) {
	  QueryOrder src = model.getRawRow( row );
	  if ( 2 == column ) {
		ValueEditor editor;
		if ( RDF.TYPE.equals( src.property ) ) {
		  // figure out if we're a concept or an edge
		  boolean isconcept = false;
		  for ( QueryNode v : graph.getVertices() ) {
			if ( src.base.equals( v ) ) {
			  isconcept = true;
			}
		  }

		  if ( isconcept ) {
			types.setChoices( conceptmap );
		  }
		  else {
			// we have an edge, so figure out the endpoints
			Pair<QueryNode> verts
					= graph.getEndpoints( QueryEdge.class.cast( src.base ) );
			URI starttype = verts.getFirst().getType();
			URI endtype = verts.getSecond().getType();

			List<URI> links = NodeDerivationTools.instance().getPredicatesBetween( starttype,
					endtype, engine );
			Map<URI, String> labels = GuiUtility.getInstanceLabels( links, engine );
			labels.put( Constants.ANYNODE, "<Any>" );
			types.setChoices( Utility.sortUrisByLabel( labels ) );
		  }

		  editor = types;
		}
		else {
		  editor = normal;
		}

		editor.setNode( src.base );
		editor.setType( src.property );
		editor.setChecked( src.base.isSelected( src.property ) );
		return editor;
	  }
	  else if ( 6 == column ) {
		filterEditor.setPropertyLabel( src.base.getLabel( src.property ) );
		return filterEditor;
	  }
	  else {
		return super.getCellEditor( row, column );
	  }
	}
  }
}
