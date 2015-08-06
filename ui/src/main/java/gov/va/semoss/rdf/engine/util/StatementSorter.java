/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import java.util.Comparator;
import org.openrdf.model.Statement;

/**
 * A class to sort a list of Statements. From almost every angle, this class is
 * completely unnecessary, but there are times when it makes troubleshooting
 * easier.
 *
 * @author ryan
 */
public class StatementSorter implements Comparator<Statement> {

	@Override
	public int compare( Statement o1, Statement o2 ) {
		int diff = o1.getSubject().stringValue().compareTo( o2.getSubject().stringValue() );
		if ( 0 == diff ) {
			diff = o1.getPredicate().stringValue().compareTo( o2.getPredicate().stringValue() );
			if ( 0 == diff ) {
				diff = o1.getObject().stringValue().compareTo( o2.getObject().stringValue() );
			}
		}
		return diff;
	}
}
