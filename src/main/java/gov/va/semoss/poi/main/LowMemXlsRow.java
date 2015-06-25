/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * A skeletal implementation of the Row interface. It's really only good as a
 * sort or array wrapper...everything else will throw an exception
 *
 * @author ryan
 * @see LowMemXlsSheet
 */
public class LowMemXlsRow implements Row {

	private final Map<Integer, LowMemXlsCell> cells = new HashMap<>();
	private int rownum;
	private final LowMemXlsSheet sheet;

	public LowMemXlsRow( LowMemXlsSheet s ) {
		sheet = s;
	}

	@Override
	public LowMemXlsCell createCell( int column ) {
		LowMemXlsCell cell = new LowMemXlsCell( column, this );
		cells.put( column, cell );
		return cell;
	}

	@Override
	public LowMemXlsCell createCell( int column, int type ) {
		LowMemXlsCell cell = createCell( column );
		cell.setCellType( type );
		return cell;
	}

	@Override
	public void removeCell( Cell cell ) {
		cells.remove( cell.getColumnIndex() );
	}

	@Override
	public void setRowNum( int rowNum ) {
		rownum = rowNum;
	}

	@Override
	public int getRowNum() {
		return rownum;
	}

	@Override
	public LowMemXlsCell getCell( int cellnum ) {
		return ( cells.containsKey( cellnum ) ? cells.get( cellnum ) : null );
	}

	@Override
	public LowMemXlsCell getCell( int cellnum, MissingCellPolicy policy ) {
		LowMemXlsCell cell = getCell( cellnum );

		if ( null == cell ) {
			return ( CREATE_NULL_AS_BLANK == policy
					? createCell( cellnum, Cell.CELL_TYPE_BLANK ) : null );
		}

		if ( RETURN_BLANK_AS_NULL == policy
				&& Cell.CELL_TYPE_BLANK == cell.getCellType() ) {
			return null;
		}

		return cell;
	}

	@Override
	public short getFirstCellNum() {
		return 0;
	}

	@Override
	public short getLastCellNum() {
		int maxkey = Integer.MIN_VALUE;
		for( Integer key : cells.keySet() ){
			if( key > maxkey ){
				maxkey = key;
			}
		}
		
		return (short) maxkey;
	}

	@Override
	public int getPhysicalNumberOfCells() {
		return cells.size();
	}

	@Override
	public void setHeight( short height ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setZeroHeight( boolean zHeight ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean getZeroHeight() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setHeightInPoints( float height ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public short getHeight() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public float getHeightInPoints() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isFormatted() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public CellStyle getRowStyle() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setRowStyle( CellStyle style ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterator<Cell> cellIterator() {
		return iterator();
	}

	@Override
	public Sheet getSheet() {
		return sheet;
	}

	@Override
	public Iterator<Cell> iterator() {
		List<Cell> list = new ArrayList<>( cells.values() );
		return list.iterator();
	}
}
