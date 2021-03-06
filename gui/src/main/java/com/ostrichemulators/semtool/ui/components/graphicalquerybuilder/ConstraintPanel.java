/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.graphicalquerybuilder;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.ui.components.graphicalquerybuilder.ConstraintPanel.ConstraintValueSet.JoinType;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;

import com.ostrichemulators.semtool.util.Utility;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class ConstraintPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( ConstraintPanel.class );
	private static final Map<Class<?>, IRI> typelookup = new HashMap<>();

	static {
		typelookup.put( String.class, XMLSchema.STRING );
		typelookup.put( Integer.class, XMLSchema.INTEGER );
		typelookup.put( Double.class, XMLSchema.DOUBLE );
		typelookup.put( Boolean.class, XMLSchema.BOOLEAN );
		typelookup.put( Date.class, XMLSchema.DATETIME );
	}

	private static boolean showDialog( String label, ConstraintPanel cp ) {
		String[] choices = { "Save", "Cancel" };
		int ans = JOptionPane.showOptionDialog( null, cp, label, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[0] );
		return ( JOptionPane.YES_OPTION == ans );
	}

	private static ConstraintValueSet makeCVSet( String val, IRI type,
			IRI property, boolean included ) {

		List<String> newvals = ( val.contains( "|" )
				? explode( val ) : Arrays.asList( val ) );

		ConstraintValueSet values = new ConstraintValueSet( included, property,
				( newvals.size() > 1 ? JoinType.OR : JoinType.SINGLE ), val );

		ValueFactory vf = SimpleValueFactory.getInstance();
		for ( String v : newvals ) {
			values.add( ( XMLSchema.ANYURI == type
					? vf.createIRI( v ) : vf.createLiteral( v, type ) ) );
		}
		return values;
	}

	public static ConstraintValueSet getValues( IRI property, String label,
			Collection<Value> value, boolean checked ) {
		JTextField input = new JTextField();
		if ( null != value ) {
			input.setText( implode( value ) );
		}

		Map<IRI, String> propmap = new HashMap<>();
		propmap.put( property, Utility.getInstanceLabel( property,
				DIHelper.getInstance().getRdfEngine() ) );

		ConstraintPanel cp = new ConstraintPanel( property, label, input, checked,
				value, propmap );
		if ( showDialog( label, cp ) ) {

			String val = input.getText();
			IRI type = cp.getType();
			return makeCVSet( val, type, property, cp.isIncluded() );
		}
		return null;
	}

	public static ConstraintValueSet getValues( String label, Map<IRI, String> propmap ) {
		JTextField input = new JTextField();

		ConstraintPanel cp = new ConstraintPanel( null, label, input, true,
				(Value) null, propmap );
		if ( showDialog( label, cp ) ) {
			String val = input.getText();
			IRI type = cp.getType();

			return makeCVSet( val, type, cp.getPropertyType(), cp.isIncluded() );
		}
		return null;
	}

	public static ConstraintValue getValue( IRI property, String label, IRI value,
			Map<IRI, String> choices, boolean checked ) {
		choices = Utility.sortIrisByLabel( choices );
		IRI[] uris = choices.keySet().toArray( new IRI[0] );
		LabeledPairRenderer<IRI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( choices );

		Map<IRI, String> propmap = new HashMap<>();
		propmap.put( property, Utility.getInstanceLabel( property,
				DIHelper.getInstance().getRdfEngine() ) );

		if ( choices.size() > 5 ) {
			JList<IRI> list = new JList<>( uris );
			list.setCellRenderer( renderer );
			list.setSelectedValue( value, true );
			list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			ConstraintPanel cp = new ConstraintPanel( property, label,
					new JScrollPane( list ), checked, value, propmap );
			if ( showDialog( label, cp ) ) {
				return new ConstraintValue( list.getSelectedValue(), cp.isIncluded(),
						property );
			}
		}
		else {
			DefaultComboBoxModel<IRI> model = new DefaultComboBoxModel<>( uris );
			JComboBox<IRI> box = new JComboBox<>( model );
			box.setRenderer( renderer );
			box.setSelectedItem( value );
			ConstraintPanel cp = new ConstraintPanel( property, label, box, checked,
					value, propmap );
			if ( showDialog( label, cp ) ) {
				return new ConstraintValue( box.getItemAt( box.getSelectedIndex() ),
						cp.isIncluded(), property );
			}
		}

		return null;
	}

	/**
	 * Gets a one-key map of values
	 *
	 * @param property
	 * @param label
	 * @param values
	 * @param choices
	 * @param checked
	 * @return
	 */
	public static ConstraintValueSet getValues( IRI property, String label,
			Collection<Value> values, Map<IRI, String> choices, boolean checked ) {
		choices = Utility.sortIrisByLabel( choices );

		IRI[] uris = choices.keySet().toArray( new IRI[0] );
		Map<IRI, Integer> choicepos = new HashMap<>();
		List<Integer> selections = new ArrayList<>();
		for ( int i = 0; i < uris.length; i++ ) {
			choicepos.put( uris[i], i );
			if ( values.contains( uris[i] ) ) {
				selections.add( i );
			}
		}

		LabeledPairRenderer<IRI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( choices );

		Map<IRI, String> propmap = new HashMap<>();
		propmap.put( property, Utility.getInstanceLabel( property,
				DIHelper.getInstance().getRdfEngine() ) );

		JList<IRI> list = new JList<>( uris );
		list.setCellRenderer( renderer );
		for ( int idx : selections ) {
			list.addSelectionInterval( idx, idx );
		}

		ConstraintPanel cp = new ConstraintPanel( property, label,
				new JScrollPane( list ), checked, values, propmap );
		if ( showDialog( label, cp ) ) {
			List<IRI> vals = list.getSelectedValuesList();

			ConstraintValueSet returns = new ConstraintValueSet( cp.isIncluded(), property,
					( vals.size() > 1 ? JoinType.AND : JoinType.SINGLE ) );
			for ( Value v : vals ) {
				returns.add( v );
			}

			return returns;
		}

		return null;
	}

	protected ConstraintPanel( IRI proptype, String label, JComponent input,
			boolean checked, Value valForType, Map<IRI, String> propmap ) {
		initComponents();

		inputarea.setLayout( new BorderLayout() );
		inputarea.add( input );
		this.label.setText( label );
		include.setSelected( checked );

		LabeledPairRenderer<IRI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( propmap );
		propmap = Utility.sortIrisByLabel( propmap );
		IRI[] uris = propmap.keySet().toArray( new IRI[0] );
		DefaultComboBoxModel<IRI> model = new DefaultComboBoxModel<>( uris );

		property.setModel( model );
		property.setEditable( false );
		property.setRenderer( renderer );
		property.setSelectedItem( null == proptype ? Constants.ANYNODE : proptype );

		setType( valForType );
	}

	protected ConstraintPanel( IRI proptype, String lbl, JComponent input,
			boolean checked, Collection<Value> values, Map<IRI, String> propmap ) {
		initComponents();

		inputarea.setLayout( new BorderLayout() );
		inputarea.add( input );
		label.setText( lbl );
		include.setSelected( checked );

		LabeledPairRenderer<IRI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( propmap );
		propmap = Utility.sortIrisByLabel( propmap );
		IRI[] uris = propmap.keySet().toArray( new IRI[0] );
		DefaultComboBoxModel<IRI> model = new DefaultComboBoxModel<>( uris );

		property.setModel( model );
		property.setEditable( false );
		property.setRenderer( renderer );
		property.setSelectedItem( null == proptype ? Constants.ANYNODE : proptype );

		if ( !( null == values || values.isEmpty() ) ) {
			setType( values.iterator().next() );
		}
		else {
			setType( null );
		}
	}

	protected IRI getPropertyType() {
		return property.getItemAt( property.getSelectedIndex() );
	}

	private void setType( Value o ) {
		if ( null == o ) {
			stringtype.setSelected( true );
		}
		else if ( o instanceof IRI ) {
			uritype.setSelected( true );
		}
		else {
			Enumeration<AbstractButton> radios = typegroup.getElements();
			Class<?> typeclass = RDFDatatypeTools.getClassForValue( o );

			while ( radios.hasMoreElements() ) {
				AbstractButton radio = radios.nextElement();
				if ( radio.getActionCommand().equalsIgnoreCase( typeclass.getSimpleName() ) ) {
					radio.setSelected( true );
				}
			}
		}
	}

	/**
	 * Gets the currently-selected data type.
	 *
	 * @return The datatype, or null if the datatype is {@link XMLSchema#STRING},
	 * or {@link XMLSchema#ANYURI} if "URI" was selected.
	 */
	public IRI getType() {
		Enumeration<AbstractButton> radios = typegroup.getElements();
		while ( radios.hasMoreElements() ) {
			AbstractButton radio = radios.nextElement();
			if ( radio.isSelected() ) {
				String command = radio.getActionCommand();

				for ( Map.Entry<Class<?>, IRI> types : typelookup.entrySet() ) {
					if ( types.getKey().getSimpleName().equalsIgnoreCase( command ) ) {
						if ( XMLSchema.STRING.equals( types.getValue() ) ) {
							return null;
						}
						return types.getValue();
					}
				}
			}
		}

		return XMLSchema.ANYURI;
	}

	public boolean isIncluded() {
		return include.isSelected();
	}

	public void setInclude( boolean inc ) {
		include.setSelected( inc );
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    typegroup = new javax.swing.ButtonGroup();
    label = new javax.swing.JLabel();
    include = new javax.swing.JCheckBox();
    jLabel1 = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    stringtype = new javax.swing.JRadioButton();
    doubletype = new javax.swing.JRadioButton();
    integertype = new javax.swing.JRadioButton();
    uritype = new javax.swing.JRadioButton();
    booleantype = new javax.swing.JRadioButton();
    datetype = new javax.swing.JRadioButton();
    jLabel2 = new javax.swing.JLabel();
    inputarea = new javax.swing.JPanel();
    property = new javax.swing.JComboBox<>();

    label.setText("New Value");

    include.setText("Return in Query");

    jLabel1.setText("Data Type");

    jPanel1.setLayout(new java.awt.GridLayout(2, 3));

    typegroup.add(stringtype);
    stringtype.setSelected(true);
    stringtype.setText("String");
    jPanel1.add(stringtype);

    typegroup.add(doubletype);
    doubletype.setText("Double");
    jPanel1.add(doubletype);

    typegroup.add(integertype);
    integertype.setText("Integer");
    jPanel1.add(integertype);

    typegroup.add(uritype);
    uritype.setText("URI");
    jPanel1.add(uritype);

    typegroup.add(booleantype);
    booleantype.setText("Boolean");
    jPanel1.add(booleantype);

    typegroup.add(datetype);
    datetype.setText("Date");
    jPanel1.add(datetype);

    jLabel2.setText("Property");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(include)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(label, javax.swing.GroupLayout.Alignment.LEADING))
              .addComponent(jLabel2))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(property, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(inputarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))))
        .addGap(0, 0, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(property, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(2, 2, 2)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
          .addComponent(inputarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(include))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton booleantype;
  private javax.swing.JRadioButton datetype;
  private javax.swing.JRadioButton doubletype;
  private javax.swing.JCheckBox include;
  private javax.swing.JPanel inputarea;
  private javax.swing.JRadioButton integertype;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JLabel label;
  private javax.swing.JComboBox<IRI> property;
  private javax.swing.JRadioButton stringtype;
  private javax.swing.ButtonGroup typegroup;
  private javax.swing.JRadioButton uritype;
  // End of variables declaration//GEN-END:variables

	private static List<String> explode( String val ) {
		List<String> vals = new ArrayList<>();
		StringBuffer processed = new StringBuffer();
		// first, remove any quoted section
		Pattern quotepattern = Pattern.compile( "\"([^\"]+)\"" );
		Matcher quoter = quotepattern.matcher( val );
		while ( quoter.find() ) {
			vals.add( quoter.group( 1 ) );
			quoter.appendReplacement( processed, "|" );
		}
		if ( processed.length() > 0 ) {
			quoter.appendTail( processed );
		}
		else {
			processed.append( val );
		}

		for ( String s : processed.toString().split( "\\s*\\|\\s*" ) ) {
			if ( !s.trim().isEmpty() ) {
				vals.add( s.trim() );
			}
		}
		return vals;
	}

	private static String implode( Collection<Value> vals ) {
		StringBuilder sb = new StringBuilder();
		for ( Value v : vals ) {
			if ( sb.length() > 0 ) {
				sb.append( " | " );
			}

			if ( v instanceof Literal ) {
				Literal l = Literal.class.cast( v );
				String str = l.getLabel();
				if ( str.contains( "|" ) ) {
					sb.append( "\"" ).append( str ).append( "\"" );
				}
				else {
					sb.append( str );
				}
			}
			else {
				sb.append( v.stringValue() );
			}
		}

		return sb.toString();
	}

	public static class ConstraintValue {

		public final Value val;
		public final boolean included;
		public final IRI property;

		public ConstraintValue( Value val, boolean included, IRI property ) {
			this.val = val;
			this.included = included;
			this.property = property;
		}
	}

	// for the record, I have no idea why I can't use import statements for these classes
	public static class ConstraintValueSet extends java.util.LinkedHashSet<org.eclipse.rdf4j.model.Value> {

		public enum JoinType {

			SINGLE, OR, AND
		};

		public final boolean included;
		public final IRI property;
		public final JoinType joiner;
		public final String raw;

		public ConstraintValueSet( boolean included, IRI property, JoinType joiner,
				String raw ) {
			this.included = included;
			this.property = property;
			this.joiner = joiner;
			this.raw = ( JoinType.SINGLE == joiner ? null : raw );
		}

		public ConstraintValueSet( boolean included, IRI property, JoinType joiner ) {
			this( included, property, joiner, null );
		}

		public ConstraintValueSet( boolean included, IRI property, JoinType joiner,
				Collection<Value> vals, String raw ) {
			this( included, property, joiner, raw );
			addAll( vals );
		}

		/**
		 * Presents the data of this set as a list of
		 * {@link ConstraintValue ConstraintValues}
		 *
		 * @return
		 */
		public Set<ConstraintValue> asCVs() {
			Set<ConstraintValue> ret = new HashSet<>();
			Iterator<Value> it = this.iterator();
			while ( it.hasNext() ) {
				ret.add( new ConstraintValue( it.next(), included, property ) );
			}
			return ret;
		}

		/**
		 * Gets the first value as a {@link ConstraintValue}, or null if we have no
		 * values
		 *
		 * @return the first value as a ConstraintValue or null if we have no values
		 */
		public ConstraintValue firstCV() {
			if ( size() > 0 ) {
				return new ConstraintValue( iterator().next(), included, property );
			}
			return null;
		}

		public boolean isSingle() {
			return ( 1 == size() );
		}
	}
}
