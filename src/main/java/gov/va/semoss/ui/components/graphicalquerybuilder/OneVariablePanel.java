/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import java.awt.CardLayout;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class OneVariablePanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( OneVariablePanel.class );
	private static final int STRING = 0;
	private static final int LIST = 1;
	private static final int COMBO = 2;

	private final int type;

	/**
	 * Creates new form OneVariableDialog
	 */
	public OneVariablePanel() {
		this( "New Value", "", true );
	}

	public OneVariablePanel( String ll, Object value, boolean checked ) {
		initComponents();
		label.setText( ll );
		include.setSelected( checked );

		if ( null != value ) {
			inputstring.setText( value.toString() );
		}
		CardLayout card = CardLayout.class.cast( inputarea.getLayout() );
		card.show( inputarea, "string" );
		type = STRING;
		inputlist.setVisibleRowCount( 1 );
	}

	public OneVariablePanel( String ll, List<String> choices, Object choice,
			boolean checked ) {
		initComponents();
		label.setText( ll );
		include.setSelected( checked );

		Collections.sort( choices );

		CardLayout card = CardLayout.class.cast( inputarea.getLayout() );
		if ( choices.size() > 5 ) {
			DefaultListModel<String> model = new DefaultListModel<>();
			for ( String o : choices ) {
				model.addElement( o );
			}

			inputlist.setModel( model );

			if ( null != choice ) {
				inputlist.setSelectedValue( choice, true );
			}
			card.show( inputarea, "list" );
			type = LIST;
		}
		else {
			inputcombo.setModel( new DefaultComboBoxModel<>( choices.toArray( new String[0] ) ) );
			if ( null != choice ) {
				inputcombo.setSelectedItem( choice );
			}
			card.show( inputarea, "combo" );
			type = COMBO;
			inputlist.setVisibleRowCount( 1 );
		}
	}

	public boolean isIncluded() {
		return include.isSelected();
	}

	public void setInclude( boolean inc ) {
		include.setSelected( inc );
	}

	public String getInput() {
		switch ( type ) {
			case STRING:
				return inputstring.getText();
			case LIST:
				return inputlist.getSelectedValue();
			case COMBO:
				return inputcombo.getItemAt( inputcombo.getSelectedIndex() );
		}

		log.error( "unknown input type!" );
		return null; // shouldn't get here
	}

	public void setInput( String text ) {
		switch ( type ) {
			case STRING:
				inputstring.setText( text );
				break;
			case LIST:
				inputlist.setSelectedValue( text, true );
				break;
			case COMBO:
				inputcombo.setSelectedItem( text );
			default:
				log.error( "setting text on unknown field!" );
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    label = new javax.swing.JLabel();
    include = new javax.swing.JCheckBox();
    inputarea = new javax.swing.JPanel();
    inputstring = new javax.swing.JTextField();
    inputcombo = new javax.swing.JComboBox<String>();
    jScrollPane2 = new javax.swing.JScrollPane();
    inputlist = new javax.swing.JList<String>();

    label.setText("New Value");

    include.setText("Return in Query");
    include.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

    inputarea.setLayout(new java.awt.CardLayout());
    inputarea.add(inputstring, "string");

    inputarea.add(inputcombo, "combo");

    inputlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    inputlist.setVisibleRowCount(1);
    jScrollPane2.setViewportView(inputlist);

    inputarea.add(jScrollPane2, "list");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(label)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(inputarea, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(include, javax.swing.GroupLayout.Alignment.TRAILING)))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(inputarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(include))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox include;
  private javax.swing.JPanel inputarea;
  private javax.swing.JComboBox<String> inputcombo;
  private javax.swing.JList<String> inputlist;
  private javax.swing.JTextField inputstring;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JLabel label;
  // End of variables declaration//GEN-END:variables
}
