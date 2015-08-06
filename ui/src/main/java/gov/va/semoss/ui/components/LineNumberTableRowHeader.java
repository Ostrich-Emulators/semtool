package gov.va.semoss.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.log4j.Logger;

/**
 * Provides line numbering for JTables within JScrollPane components. This
 * component has been taken from
 * "https://github.com/java-tester-x/JavaAppGui/blob/master/com/todolist/
 * util/LineNumberTableRowHeader.java", and modified slightly.
 *
 * An example of implementation can be found on the site,
 * "http://futuretechacademy.com/display-line-numbers-jtable/", and in the
 * SEMOSS codebase, "gov.va.semoss/ui/components/playsheets/GridPlaySheet.java".
 *
 * @author Thomas
 *
 */
public class LineNumberTableRowHeader extends JComponent {

	private final JTable table;
	private final JScrollPane scrollPane;

	public LineNumberTableRowHeader( JScrollPane jScrollPane, JTable table ) {
		this.scrollPane = jScrollPane;
		this.table = table;
		this.table.getModel().addTableModelListener( new TableModelListener() {
			@Override
			public void tableChanged( TableModelEvent tme ) {
				LineNumberTableRowHeader.this.repaint();
			}
		} );

		this.table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( ListSelectionEvent lse ) {
				LineNumberTableRowHeader.this.repaint();
			}
		} );

		this.scrollPane.getVerticalScrollBar().addAdjustmentListener( new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged( AdjustmentEvent ae ) {
				LineNumberTableRowHeader.this.repaint();
			}
		} );

		setPreferredSize( new Dimension( 40, 90 ) );

		this.addMouseListener( new MouseAdapter() {

			@Override
			public void mouseClicked( MouseEvent e ) {
				Point p = e.getPoint();
				Point viewPosition = scrollPane.getViewport().getViewPosition();
				p = new Point( p.x, p.y + viewPosition.y );

				int row = table.rowAtPoint( p );
				table.getSelectionModel().setSelectionInterval( row, row );
			}

		} );

	}

	@Override
	protected void paintComponent( Graphics g ) {
		Point viewPosition = scrollPane.getViewport().getViewPosition();
		Rectangle rect = scrollPane.getVisibleRect();
		int height = rect.y + rect.height;

		super.paintComponent( g );
		g.setColor( getBackground() );
		g.fillRect( 0, 0, getWidth(), getHeight() );

		FontMetrics fm = g.getFontMetrics();

		int rowheight = table.getRowHeight();
		int startrow = viewPosition.y / rowheight;
		int stoprow = ( viewPosition.y + height ) / rowheight;
		if ( stoprow > table.getRowCount() ) {
			stoprow = table.getRowCount();
		}

		for ( int r = startrow; r < stoprow; r++ ) {
			Rectangle cellRect = table.getCellRect( r, 0, false );
			cellRect.y -= viewPosition.y;

			if ( r == startrow ) {
				g.setColor( Color.WHITE );
				g.drawLine( 0, cellRect.y, getWidth(), cellRect.y );
			}

			boolean rowSelected = table.isRowSelected( r );

			if ( rowSelected ) {
				g.setColor( table.getSelectionBackground() );
				g.fillRect( 0, cellRect.y, getWidth(), cellRect.height );
			}

			int yPlusHeight = cellRect.y + cellRect.height;

			g.setColor( Color.WHITE );
			g.drawLine( 0, yPlusHeight, getWidth(), yPlusHeight );

			g.setColor( rowSelected ? table.getSelectionForeground() : getForeground() );
			String s = Integer.toString( r + 1 );
			g.drawString( s, getWidth() - fm.stringWidth( s ) - 2,
					yPlusHeight - fm.getDescent() );
		}

		if ( table.getShowVerticalLines() ) {
			g.setColor( table.getGridColor() );
			g.drawRect( 0, 0, getWidth() - 1, getHeight() - 1 );
		}
	}
}
