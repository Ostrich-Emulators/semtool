/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.util.Utility;
import java.util.Map;
import javax.swing.JPopupMenu;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class VertexPopup extends JPopupMenu {

	public VertexPopup( SEMOSSVertex v, GraphicalQueryBuilderPanel pnl ) {
		add( new OneVariableDialogItem( v, pnl, RDFS.LABEL, "Set Instance Label",
				"Set the label of this node", "Instance Label" ) );

		Map<URI, String> labels = Utility.getInstanceLabels(
				DBToLoadingSheetExporter.createConceptList( pnl.getEngine() ), pnl.getEngine() );
		add( new OneVariableDialogItem( v, pnl, RDF.TYPE, "Set Type",
				"Change the type of this node", "New Type", Utility.sortUrisByLabel( labels ) ) );
	}
}
