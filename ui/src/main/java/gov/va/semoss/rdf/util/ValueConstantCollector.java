package gov.va.semoss.rdf.util;

import org.apache.log4j.Logger;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

class ValueConstantCollector extends QueryModelVisitorBase<Exception> {
	public Object value;

	@Override
	public void meet(ValueConstant node) {
		Logger.getLogger( getClass() ).debug("Value Constant is  " + node.getValue());
		value = node.getValue();
	}

}