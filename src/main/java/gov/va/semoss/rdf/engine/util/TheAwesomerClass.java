package gov.va.semoss.rdf.engine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class TheAwesomerClass {
	/** The logger for this class */
	private static final Logger logger = Logger.getLogger( TheAwesomeClass.class );
	
	private static TheAwesomerClass instance;
	
	private static final Map<URI, Class<?>> TYPELOOKUP = new HashMap<>();
	
	private TheAwesomerClass(){
		TYPELOOKUP.put( XMLSchema.INT, Integer.class );
		TYPELOOKUP.put( XMLSchema.INTEGER, Integer.class );
		TYPELOOKUP.put( XMLSchema.DOUBLE, Double.class );
		TYPELOOKUP.put( XMLSchema.FLOAT, Float.class );
		TYPELOOKUP.put( XMLSchema.DECIMAL, Double.class );
		TYPELOOKUP.put( XMLSchema.STRING, String.class );
		TYPELOOKUP.put( XMLSchema.DATE, Date.class );
		TYPELOOKUP.put( XMLSchema.DATETIME, Date.class );
		TYPELOOKUP.put( XMLSchema.BOOLEAN, Boolean.class );
	}
	
	public static TheAwesomerClass instance(){
		if (instance == null){
			instance = new TheAwesomerClass();
		}
		return instance;
	}
	
	public List<Class<?>> figureColumnClassesFromData( List<Value[]> newdata,
			int columns ) {
		List<Class<?>> columnClasses = new ArrayList<>();
		if ( newdata.isEmpty() ) {
			for ( int i = 0; i < columns; i++ ) {
				columnClasses.add( Object.class );
			}
		}
		else {
			// we'd like to be able to figure out column types even
			// if a row doesn't have values for every column.
			// we used to determine the class of a column from it's first non-null Value,
			// but that's just not always true for every element of the column, so we
			// need to check until we're sure all values are the same, or we can decide
			// if the column is a String or Object.
			List<Integer> colsToFigure = new ArrayList<>();
			for ( int i = 0; i < columns; i++ ) {
				colsToFigure.add( i );
			}
			Class<?> arr[] = new Class<?>[columns];

			// now iterate as far as we have to until we have all the column classes
			Iterator<Value[]> it = newdata.iterator();
			Set<Class<?>> finalClasses
					= new HashSet<>( Arrays.asList( String.class, Object.class ) );

			while ( !colsToFigure.isEmpty() && it.hasNext() ) {
				Value[] first = it.next();

				// we have a row of data, so see if it can provide a class for 
				// any column we don't yet have a class for
				ListIterator<Integer> colit = colsToFigure.listIterator();
				while ( colit.hasNext() ) {
					int col = colit.next();
					Value v = first[col];
					Class<?> k = getClassForValue( v );

					// getClassForValue returns Object when the classtype can't be determined
					if ( !Object.class.equals( k ) ) {
						Class<?> previousK = arr[col];

						if ( null == previousK ) {
							// first time we've set a value for this column
							arr[col] = k;
						}
						else if ( previousK != k ) {
							// we have a previous column class, 
							if ( !finalClasses.contains( previousK ) ) {
								// we're not already at a "final" class, so figure out what we want
								if ( finalClasses.contains( k ) ) {
									// we're going to be final, so set it
									arr[col] = k;
								}
								else {
									// we have two different classes, 
									// assume they're irreconcilable
									arr[col] = Object.class;
								}
							}
							// else we're "final," so don't change
						}

						if ( finalClasses.contains( arr[col] ) ) {
							colit.remove();
						}
					}
				}
			}

			// remove any columns where we have a class, albeit a non-"final" one
			ListIterator<Integer> li = colsToFigure.listIterator();
			while ( li.hasNext() ) {
				int col = li.next();
				if ( null != arr[col] ) {
					li.remove();
				}
			}

			// we don't have any data for the remaining columns, so do something safe
			for ( int col : colsToFigure ) {
				arr[col] = Object.class;
			}

			columnClasses.addAll( Arrays.asList( arr ) );
		}

		return columnClasses;
	}
	
	public Class<?> getClassForValue( Value v ) {

		if ( v instanceof URI ) {
			return URI.class;
		}

		if ( v instanceof Literal ) {
			Literal l = Literal.class.cast( v );
			URI dt = l.getDatatype();
			return ( TYPELOOKUP.containsKey( dt )
					? TYPELOOKUP.get( dt ) : String.class );
		}

		return Object.class;
	}

	public Object parseXMLDatatype( String input ) {
		if ( input == null ) {
			return null;
		}

		input = input.trim();

		String[] pieces = input.split( "\"" );
		if ( pieces.length != 3 ) {
			return removeExtraneousDoubleQuotes( input );
		}

		Class<?> theClass = null;
		for ( URI datatypeUri : TYPELOOKUP.keySet() ) {
			if ( pieces[2].contains( datatypeUri.stringValue() ) ) {
				theClass = TYPELOOKUP.get( datatypeUri );
			}
		}

		String dataPiece = pieces[1];

		if ( theClass == Double.class && XMLDatatypeUtil.isValidDouble( dataPiece ) ) {
			return XMLDatatypeUtil.parseDouble( dataPiece );
		}

		if ( theClass == Float.class && XMLDatatypeUtil.isValidFloat( dataPiece ) ) {
			return XMLDatatypeUtil.parseFloat( dataPiece );
		}

		if ( theClass == Integer.class && XMLDatatypeUtil.isValidInteger( dataPiece ) ) {
			return XMLDatatypeUtil.parseInteger( dataPiece );
		}

		if ( theClass == Boolean.class && XMLDatatypeUtil.isValidBoolean( dataPiece ) ) {
			return XMLDatatypeUtil.parseBoolean( dataPiece );
		}

		if ( theClass == Date.class && XMLDatatypeUtil.isValidDate( dataPiece ) ) {
			return XMLDatatypeUtil.parseCalendar( dataPiece );
		}

		return removeExtraneousDoubleQuotes( input );
	}

	public Object getObjectFromValue( Value value ) {
		if ( value == null ) {
			return null;
		}

		Class<?> theClass = getClassForValue( value );

		if ( URI.class == theClass ) {
			return value;
		}

		Literal input = Literal.class.cast( value );
		String val = input.getLabel();
		boolean isempty = val.isEmpty();

		if ( theClass == Double.class ) {
			return ( isempty ? null : input.doubleValue() );
		}

		if ( theClass == Integer.class ) {
			return ( isempty ? null : input.intValue() );
		}

		if ( theClass == Boolean.class ) {
			return ( isempty ? null : input.booleanValue() );
		}

		if ( theClass == Float.class ) {
			return ( isempty ? null : input.floatValue() );
		}

		if ( theClass == Date.class ) {
			return ( isempty ? null : input.calendarValue() );
		}

		return removeExtraneousDoubleQuotes( input.stringValue() );
	}

	public static Value getValueFromObject( Object o ) {
		if ( null == o ) {
			return null;
		}

		if ( o instanceof Value ) {
			return Value.class.cast( o );
		}

		ValueFactory vf = new ValueFactoryImpl();
		if ( o instanceof String ) {
			return vf.createLiteral( String.class.cast( o ) );
		}
		else if ( o instanceof Double ) {
			return vf.createLiteral( Double.class.cast( o ) );
		}
		else if ( o instanceof Integer ) {
			return vf.createLiteral( Integer.class.cast( o ) );
		}
		else if ( o instanceof Boolean ) {
			return vf.createLiteral( Boolean.class.cast( o ) );
		}
		else if ( o instanceof Date ) {
			return vf.createLiteral( Date.class.cast( o ) );
		}
		else if ( o instanceof Float ) {
			return vf.createLiteral( Float.class.cast( o ) );
		}

		logger.warn( "unhandled data type for object: " + o );
		return null;
	}
	
	public String removeExtraneousDoubleQuotes( String input ) {
		while ( input != null && input.length() > 2
				&& input.charAt( 0 ) == '\"'
				&& input.charAt( input.length() - 1 ) == '\"' ) {
			input = input.substring( 1, input.length() - 1 );
		}

		return input;
	}

	public static Value getValueFromDatatypeAndString(URI datatype, String content) {
		if ( XMLSchema.INTEGER == datatype || XMLSchema.INT == datatype) {
			try {
				return new LiteralImpl(Integer.parseInt(content) + "");
			} catch (NumberFormatException e) {
				return null;
			}
		} else if ( XMLSchema.DOUBLE == datatype ) {
			try {
				return new LiteralImpl(Double.parseDouble(content) + "");
			} catch (NumberFormatException e) {
				return null;
			}
		} else if ( XMLSchema.FLOAT == datatype ) {
			try {
				return new LiteralImpl(Float.parseFloat(content) + "");
			} catch (NumberFormatException e) {
				return null;
			}
		} else if ( XMLSchema.BOOLEAN == datatype ) {
			return new LiteralImpl(Boolean.parseBoolean(content) + "");
		} else if ( XMLSchema.DATE == datatype ) {
			logger.warn("Parsing RDF datatype Date not yet supported.");
			return null;
		} else if ( XMLSchema.ANYURI == datatype ) {
			try {
				return new URIImpl(content);
			} catch (Exception e) {
				return null;
			}
		} else if ( XMLSchema.STRING == datatype ) {
			return new LiteralImpl(content);
		} else {
			logger.warn("Trying to parse a value for a datatype not yet supported: " + datatype);
			return null;
		}
	}
	
}
