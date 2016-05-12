package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class PropertyEditorRow {
	private static final Logger log = Logger.getLogger( PropertyEditorRow.class );

	private final URI name;
	private final URI datatype;
	private Value value;

	public PropertyEditorRow( Value name, URI datatype, Value value ) throws Exception {
		this.name = new URIImpl( name.stringValue() );
		this.datatype = datatype;
		this.value = value;
	}

	public URI getName() {
		return name;
	}
	
	public URI getDatatype() {
		return datatype;
	}
	
	public Value getValue() {
		return value;
	}

	public String getValueAsDisplayString() {
		if ( sameDatatype(datatype, XMLSchema.DOUBLE) ) {
			Literal l = Literal.class.cast( value );
			return l.doubleValue() + "";
		} else if ( sameDatatype(datatype, XMLSchema.FLOAT) ) {
			Literal l = Literal.class.cast( value );
			return l.floatValue() + "";
		} else if ( sameDatatype(datatype, XMLSchema.INTEGER) || sameDatatype(datatype, XMLSchema.INT) ) {
			Literal l = Literal.class.cast( value );
			return l.intValue() + "";
		} else if ( sameDatatype(datatype, XMLSchema.BOOLEAN) ) {
			Literal l = Literal.class.cast( value );
			return l.booleanValue() + "";
		} else if ( sameDatatype(datatype, XMLSchema.DATE) ) {
			Literal l = Literal.class.cast( value );
			return l.calendarValue().toGregorianCalendar().getTime() + "";
		} else if ( sameDatatype(datatype, XMLSchema.ANYURI) ) {
			return new URIImpl( value.stringValue() ).getLocalName();
		} else if ( sameDatatype(datatype, XMLSchema.STRING) ) {
			return value.stringValue();
		} else {
			log.warn("We need to handle the case for datatype: " + datatype);
			return value.stringValue();
		}
	}
	
	private boolean sameDatatype(URI uri1, URI uri2) {
		return (uri1.stringValue().equals( uri2.stringValue() ));
	}
}