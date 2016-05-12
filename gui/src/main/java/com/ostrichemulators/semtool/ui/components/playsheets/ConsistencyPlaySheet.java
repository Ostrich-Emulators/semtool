/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.sksamuel.diffpatch.DiffMatchPatch;
import com.sksamuel.diffpatch.DiffMatchPatch.Diff;
import static com.sksamuel.diffpatch.DiffMatchPatch.Operation.EQUAL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineConsistencyChecker;
import com.ostrichemulators.semtool.rdf.engine.util.EngineConsistencyChecker.Hit;
import com.ostrichemulators.semtool.ui.components.models.ValueTableModel;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public class ConsistencyPlaySheet extends GridRAWPlaySheet {

	private static final Logger log = Logger.getLogger( ConsistencyPlaySheet.class );
	private final JSlider slider = new JSlider( 70, 100 );

	public ConsistencyPlaySheet( URI type, MultiMap<URI, EngineConsistencyChecker.Hit> hits,
			Map<URI, String> labels, IEngine engine ) {
		super( new ValueTableModel( false ) );
		getModel().setReadOnly( true );
		slider.setMajorTickSpacing( 10 );
		slider.addChangeListener( new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e ) {
				slider.setToolTipText( slider.getValue() + "% Similar" );
				getModel().fireTableDataChanged();
			}
		} );

		JTable tbl = getTable();

		log.debug( "making grid for " + type + " (" + labels.get( type ) + ")" );

		List<Value[]> data = new ArrayList<>();
		ValueFactory vf = new ValueFactoryImpl();

		// get all the labels we need
		List<URI> needlabels = new ArrayList<>();

		for ( Map.Entry<URI, List<Hit>> en : hits.entrySet() ) {
			if ( !labels.containsKey( en.getKey() ) ) {
				needlabels.add( en.getKey() );
			}

			for ( Hit h : en.getValue() ) {
				if ( !labels.containsKey( h.getMatchType() ) ) {
					needlabels.add( h.getMatchType() );
				}
			}
		}

		labels.putAll( Utility.getInstanceLabels( needlabels, engine ) );

		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;

		for ( Map.Entry<URI, List<Hit>> en : hits.entrySet() ) {
			URI needle = en.getKey();
			for ( Hit hit : en.getValue() ) {
				float score = hit.getScore();
				if ( score > max ) {
					max = score;
				}
				if ( score < min ) {
					min = score;
				}

				Value row[] = {
					vf.createLiteral( labels.get( needle ) ),
					vf.createLiteral( hit.getMatchLabel() ),
					vf.createLiteral( score ),
					vf.createLiteral( labels.get( hit.getMatchType() ) )
				};
				data.add( row );
			}
		}

		slider.setMaximum( (int) ( 100 * max ) );
		slider.setMinimum( (int) ( 100 * min ) );

		List<String> headers
				= Arrays.asList( labels.get( type ), "Match", "Score", "Match Type" );
		create( data, headers, engine );
		setTitle( labels.get( type ) );

		final NumberFormat df = DecimalFormat.getInstance();
		df.setMaximumFractionDigits( 2 );
		df.setMinimumFractionDigits( 2 );

		TableColumnModel tcm = tbl.getColumnModel();
		tcm.getColumn( 1 ).setCellRenderer( new DiffRenderer() );

		tcm.getColumn( 2 ).setCellRenderer( new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent( JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column ) {
				setHorizontalAlignment( JLabel.RIGHT );
				Float val = Float.class.cast( value );
				return super.getTableCellRendererComponent( table,
						df.format( val ), isSelected, hasFocus, row, column );
			}

		} );

		TableRowSorter<ValueTableModel> trs = new TableRowSorter<>( getModel() );
		tbl.setRowSorter( trs );
		trs.setSortKeys( Arrays.asList( new RowSorter.SortKey( 0, SortOrder.ASCENDING ),
				new RowSorter.SortKey( 3, SortOrder.DESCENDING ) ) );

		trs.setRowFilter( new RowFilter<ValueTableModel, Integer>() {

			@Override
			public boolean include( RowFilter.Entry<? extends ValueTableModel, ? extends Integer> entry ) {
				Object o = entry.getValue( 2 );
				Float f = Float.class.cast( o );
				return f * 100 >= slider.getValue();
			}
		} );

		int sizes[] = { 500, 500, 100, 250 };
		for ( int i = 0; i < sizes.length; i++ ) {
			tcm.getColumn( i ).setPreferredWidth( sizes[i] );
		}
	}

	@Override
	public void populateToolBar( JToolBar jtb, final String tabTitle ) {
		jtb.add( slider );
		slider.setPreferredSize( new Dimension( 50, 15 ) );
		slider.repaint();
	}

	public void setThreshhold( int pct ) {
		if ( pct < slider.getMinimum() && pct > slider.getMaximum() ) {
			log.warn( pct + " not within slider's range, resetting" );
			pct = ( slider.getMinimum() + slider.getMaximum() ) / 2;
		}

		slider.setValue( pct );
	}

	private class DiffRenderer extends JTextArea implements TableCellRenderer {

		private final DefaultHighlighter hilight = new DefaultHighlighter();
		private final Highlighter.HighlightPainter inspainter
				= new DefaultHighlighter.DefaultHighlightPainter( Color.GREEN );
		private final Highlighter.HighlightPainter delpainter
				= new DefaultHighlighter.DefaultHighlightPainter( Color.YELLOW );

		public DiffRenderer() {
			setHighlighter( hilight );
			setOpaque( true );
			setBorder( new EmptyBorder( 0, 2, 0, 2 ) );
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object val,
				boolean sel, boolean focus, int row, int col ) {

			String orig = table.getValueAt( row, 0 ).toString();
			String hit = val.toString();

			setFont( table.getFont() );
			setOpaque( sel );

			if ( sel ) {
				setBackground( table.getSelectionBackground() );
				setForeground( table.getSelectionForeground() );
			}
			else {
				setBackground( table.getBackground() );
				setForeground( table.getForeground() );
			}

			hilight.removeAllHighlights();

			List<Diff> diffs = new DiffMatchPatch().diff_main( orig, hit );
			StringBuilder textval = new StringBuilder();
			for ( Diff diff : diffs ) {
				textval.append( diff.text );
			}
			setText( textval.toString() );

			int currentpos = 0;
			for ( Diff diff : diffs ) {
				final int start = currentpos;
				currentpos += diff.text.length();
				final int end = currentpos;

				switch ( diff.operation ) {
					case INSERT:
						try {
							hilight.addHighlight( start, end, inspainter );
						}
						catch ( BadLocationException e ) {
							// ignore 
							log.warn( e, e );
						}
						break;
					case DELETE:
						try {
							hilight.addHighlight( start, end, delpainter );
						}
						catch ( BadLocationException e ) {
							// ignore 
							log.warn( e, e );
						}
						break;
					case EQUAL:
						break;
				}
			}

			return this;
		}
	}
}
