/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import java.awt.Color;
import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author ryan
 */
public class FilterEditor extends DefaultCellEditor implements TableCellEditor {

  private static final Pattern CONCATS = Pattern.compile( "(^|&&|\\|\\|)+([^|&$]+)" );
  private static final String UNARIES[] = { "bound", "isURI", "isIRI",
	"isBlank", "isLiteral", "str", "lang", "datatype" };
  private static final String BINARIES[] = { "<", ">", "!=", "*", "/", "+",
	"-", "=" };

  private String label;
  private final JTextField textfield;

  public FilterEditor() {
	super( new JTextField() );
	textfield = JTextField.class.cast( super.getComponent() );
	textfield.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
  }

  public void setPropertyLabel( String lbl ) {
	label = lbl;
  }

  @Override
  public Object getCellEditorValue() {
	return shorthandToValid( textfield.getText(), label );
  }

  //Implement the one method defined by TableCellEditor.
  @Override
  public Component getTableCellEditorComponent( JTable table,
		  Object value, boolean isSelected, int row, int col ) {
	String txt = ( null == value ? "" : value.toString() );
	return super.getTableCellEditorComponent( table,
			validToShorthand( txt, label ), isSelected, row, col );
  }

  /**
   * Interprets user-generated shorthand for a filter, and converts it to a
   * valid filter (fragment)
   *
   * @param shorthand
   * @param label
   * @return
   */
  public static String shorthandToValid( String shorthand, String label ) {
	if ( null == shorthand ) {
	  return shorthand;
	}

	String val = shorthand.trim();
	StringBuilder sb = new StringBuilder();

		// we might have a filter, but check to see if we have some logical
	// concatenations as well
	Matcher m = CONCATS.matcher( val );
	while ( m.find() ) {
	  
	  String concat = m.group( 1 ).trim();	  
	  if( !concat.isEmpty() ){
		sb.append( " " ).append( concat ).append( " " );
	  }

	  String group = m.group( 2 ).trim();

	  for ( String unary : UNARIES ) {
		if ( unary.equalsIgnoreCase( group ) ) {
		  sb.append( group ).append( "( ?" ).append( label ).append( " ) " );
		  break;
		}
	  }

	  for ( String binary : BINARIES ) {
		if ( group.startsWith( binary ) ) {
		  sb.append( "?" ).append( label ).append( " " ).append( group );
		  break;
		}
	  }
	}

	if ( 0 == sb.length() ) {
	  sb.append( val );
	}

	return sb.toString();
  }

  /**
   * Converts a valid filter fragment to a shorthand version, if possible
   *
   * @param valid
   * @param label
   * @return
   */
  public static String validToShorthand( String valid, String label ) {
	if ( null == valid ) {
	  return valid;
	}

	valid = valid.trim();

	StringBuilder sb = new StringBuilder();
	Matcher m = CONCATS.matcher( valid );
	while ( m.find() ) {
	  sb.append( m.group( 1 ) );

	  String group = m.group( 2 ).trim();

	  for ( String unary : UNARIES ) {
		Pattern pat = Pattern.compile( "^" + unary + "\\(\\s*\\?" + label + "\\s*\\)",
				Pattern.CASE_INSENSITIVE );
		if ( pat.matcher( group ).matches() ) {
		  return unary;
		}
	  }

	  for ( String binary : BINARIES ) {
		Pattern pat = Pattern.compile( "^\\?" + label + "\\s*(\\"
				+ binary + ".*)$" );
		Matcher m2 = pat.matcher( group );
		if ( m2.matches() ) {
		  sb.append( m2.group( 1 ) );
		  break;
		}
	  }
	}

	return sb.toString();
  }
}
