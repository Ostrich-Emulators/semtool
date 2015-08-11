package gov.va.semoss.util;

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
import org.apache.xerces.util.XMLChar;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * This class offers utility methods for converting between Objects and Values,
 * as well as offering the ability to derive data types for RDF entities.
 *
 * @author Wayne Warren
 *
 */
public class RDFDatatypeTools {

	/**
	 * The logger for this class
	 */
	private static final Logger logger = Logger.getLogger( RDFDatatypeTools.class );
	private static final ValueFactory vf = new ValueFactoryImpl();
	/**
	 * The singleton instance for this class
	 */
	private static RDFDatatypeTools instance;
	/**
	 * A lookup which stores the various static tags for the data types that one
	 * might find in an XML Schema as keys, and the corresponding native Java
	 * classes as values
	 */
	private static final Map<URI, Class<?>> TYPELOOKUP = new HashMap<>();
	
	/**
	 * Default constructor
	 */
	private RDFDatatypeTools() {
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

	public static RDFDatatypeTools instance() {
		if ( instance == null ) {
			instance = new RDFDatatypeTools();
		}
		return instance;
	}

	/**
	 * Derives the classes of a set of columns based on the row data that they
	 * describe
	 *
	 * @param newdata The row data described by the columns
	 * @param columns The number of columns to be "classed"
	 * @return A list (ordinal) of the classes describing the data types of the
	 * columns
	 */
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

	/**
	 * Derive the data type for the value of a tabular field
	 *
	 * @param v The value for which we need to derive a class
	 * @return The class describing the value's data type
	 */
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

	/**
	 * Parse the data type of an XML entity based on its string content
	 *
	 * @param input The XML entity, in string form
	 * @return The entity instance, properly classed
	 */
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

	/**
	 * Gets a proper native object from a given RDF value
	 *
	 * @param value The RDF Value
	 * @return A proper native object
	 */
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

		return input.stringValue();
	}

	/**
	 * Converts a native object instance to its equivalent RDF value
	 *
	 * @param o The native object to be converted
	 * @return A proper RDF Value
	 */
	public static Value getValueFromObject( Object o ) {
		if ( null == o ) {
			return null;
		}

		if ( o instanceof Value ) {
			return Value.class.cast( o );
		}

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

	/**
	 * Internal convenience method to eliminate unnecessary quotes
	 *
	 * @param input The input containing potentially unnecessary quote chars
	 * @return The string content without the unnecessary quotes
	 */
	private String removeExtraneousDoubleQuotes( String input ) {
		while ( input != null && input.length() > 2
				&& input.charAt( 0 ) == '\"'
				&& input.charAt( input.length() - 1 ) == '\"' ) {
			input = input.substring( 1, input.length() - 1 );
		}

		return input;
	}

	public static boolean isValidUriChars( String raw ) {
		// Check if character is valid in the localpart (http://en.wikipedia.org/wiki/QName)
		// NC is "non-colonized" name:  http://www.w3.org/TR/xmlschema-2/#NCName
		return XMLChar.isValidNCName( raw );
		// return VALIDCHARS.matcher( raw ).matches();
	}

	/**
	 * Derives an RDF Value from a proper datatype and the stringified version of
	 * the content
	 *
	 * @param datatype URI describing the datatype of the RDF entity
	 * @param content The stringified version of the value
	 * @return A proper RDF value
	 */
	public static Value getValueFromDatatypeAndString( URI datatype, String content ) {
		return vf.createLiteral( content, datatype );
	}
}
