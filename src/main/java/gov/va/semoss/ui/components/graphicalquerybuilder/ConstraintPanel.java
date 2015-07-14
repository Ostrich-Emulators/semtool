/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.renderers.LabeledPairRenderer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import java.awt.BorderLayout;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class ConstraintPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( ConstraintPanel.class );
	private static final Map<Class<?>, URI> typelookup = new HashMap<>();

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

	public static ConstraintValue getValue( URI property, String label, Object value,
			boolean checked ) {
		JTextField input = new JTextField();
		input.setText( value.toString() );

		Map<URI, String> propmap = new HashMap<>();
		propmap.put( property, Utility.getInstanceLabel( property,
				DIHelper.getInstance().getRdfEngine() ) );

		ConstraintPanel cp = new ConstraintPanel( property, label, input, checked,
				value, propmap );
		if ( showDialog( label, cp ) ) {
			String val = input.getText();
			URI type = cp.getType();
			return new ConstraintValue( ( null == type ? new URIImpl( val )
					: new LiteralImpl( val, type ) ), cp.isIncluded(), property );
		}
		return null;
	}

	public static ConstraintValue getValue( String label, Object value,
			Map<URI, String> propmap ) {
		JTextField input = new JTextField();
		if ( null != value ) {
			input.setText( value.toString() );
		}

		ConstraintPanel cp = new ConstraintPanel( null, label, input, true,
				value, propmap );
		if ( showDialog( label, cp ) ) {
			String val = input.getText();
			URI type = cp.getType();
			return new ConstraintValue( ( null == type ? new URIImpl( val )
					: new LiteralImpl( val, type ) ), cp.isIncluded(), cp.getPropertyType() );
		}
		return null;
	}

	public static ConstraintValue getValue( URI property, String label, URI value,
			Map<URI, String> choices, boolean checked ) {
		choices = Utility.sortUrisByLabel( choices );
		URI[] uris = choices.keySet().toArray( new URI[0] );
		LabeledPairRenderer<URI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( choices );

		Map<URI, String> propmap = new HashMap<>();
		propmap.put( property, Utility.getInstanceLabel( property,
				DIHelper.getInstance().getRdfEngine() ) );

		if ( choices.size() > 5 ) {
			JList<URI> list = new JList<>( uris );
			list.setCellRenderer( renderer );
			list.setSelectedValue( value, true );
			ConstraintPanel cp = new ConstraintPanel( property, label,
					new JScrollPane( list ), checked, value, propmap );
			if ( showDialog( label, cp ) ) {
				return new ConstraintValue( list.getSelectedValue(), cp.isIncluded(),
						property );
			}
		}
		else {
			DefaultComboBoxModel<URI> model = new DefaultComboBoxModel<>( uris );
			JComboBox<URI> box = new JComboBox<>( model );
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

	protected ConstraintPanel( URI proptype, String label, JComponent input,
			boolean checked, Object valForType, Map<URI, String> propmap ) {
		initComponents();

		inputarea.setLayout( new BorderLayout() );
		inputarea.add( input );
		this.label.setText( label );
		include.setSelected( checked );

		LabeledPairRenderer<URI> renderer
				= LabeledPairRenderer.getUriPairRenderer().cache( propmap );
		propmap = Utility.sortUrisByLabel( propmap );
		URI[] uris = propmap.keySet().toArray( new URI[0] );
		DefaultComboBoxModel<URI> model = new DefaultComboBoxModel<>( uris );

		property.setModel( model );
		property.setEditable( false );
		property.setRenderer( renderer );
		property.setSelectedItem( null == proptype ? Constants.ANYNODE : valForType );
		
		setType( valForType );
	}

	protected URI getPropertyType() {
		return property.getItemAt( property.getSelectedIndex() );
	}

	private void setType( Object o ) {
		if ( null == o ) {
			stringtype.setSelected( true );
		}
		else if ( o instanceof URI ) {
			uritype.setSelected( true );
		}
		else {
			Enumeration<AbstractButton> radios = typegroup.getElements();
			while ( radios.hasMoreElements() ) {
				AbstractButton radio = radios.nextElement();
				if ( radio.getActionCommand().equalsIgnoreCase( o.getClass().getSimpleName() ) ) {
					radio.setSelected( true );
				}
			}
		}
	}

	public URI getType() {

		Enumeration<AbstractButton> radios = typegroup.getElements();
		while ( radios.hasMoreElements() ) {
			AbstractButton radio = radios.nextElement();
			if ( radio.isSelected() ) {
				String command = radio.getActionCommand();

				for ( Map.Entry<Class<?>, URI> types : typelookup.entrySet() ) {
					if ( types.getKey().getSimpleName().equalsIgnoreCase( command ) ) {
						return types.getValue();
					}
				}
			}
		}

		return null;
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
    property = new javax.swing.JComboBox<URI>();

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
  private javax.swing.JComboBox<URI> property;
  private javax.swing.JRadioButton stringtype;
  private javax.swing.ButtonGroup typegroup;
  private javax.swing.JRadioButton uritype;
  // End of variables declaration//GEN-END:variables

	public static class ConstraintValue {

		public final Value val;
		public final boolean included;
		public final URI property;

		public ConstraintValue( Value val, boolean included, URI property ) {
			this.val = val;
			this.included = included;
			this.property = property;
		}
	}
}
