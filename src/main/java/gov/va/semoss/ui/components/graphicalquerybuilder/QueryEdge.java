/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import java.awt.Color;
import org.openrdf.model.URI;

/**
 * An extension of SEMOSSVertex to allow multiple values for one property
 *
 * @author ryan
 */
public class QueryEdge extends AbstractQueryNodeEdgeBase implements QueryNodeEdgeBase {

	public QueryEdge( URI _uri ) {
		super( _uri );
		
		setColor( Color.DARK_GRAY );
	}
}
