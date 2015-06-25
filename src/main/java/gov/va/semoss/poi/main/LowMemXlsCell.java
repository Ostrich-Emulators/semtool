/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.util.Calendar;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * A skeletal implementation of the Cell interface. It's really only good as a
 * sort or data wrapper...everything else will throw an exception
 *
 * @author ryan
 */
public class LowMemXlsCell implements Cell {

	private final int col;
	private final Row row;
	private int celltype;
	private String val;
	private Comment comment;
	private CellStyle style;

	public LowMemXlsCell( int col, Row row ) {
		this.col = col;
		this.row = row;
	}

	@Override
	public int getColumnIndex() {
		return col;
	}

	@Override
	public int getRowIndex() {
		return row.getRowNum();
	}

	@Override
	public Sheet getSheet() {
		return row.getSheet();
	}

	@Override
	public Row getRow() {
		return row;
	}

	@Override
	public void setCellType( int cellType ) {
		celltype = cellType;
	}

	@Override
	public int getCellType() {
		return celltype;
	}

	@Override
	public int getCachedFormulaResultType() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setCellValue( double value ) {
		val = Double.toString( value );
	}

	@Override
	public void setCellValue( Date value ) {
		setCellValue( DateUtil.getExcelDate( value ) );
	}

	@Override
	public void setCellValue( Calendar value ) {
		setCellValue( value.getTime() );
	}

	@Override
	public void setCellValue( RichTextString value ) {
		val = value.getString();
	}

	@Override
	public void setCellValue( String value ) {
		val = value;
	}

	@Override
	public void setCellFormula( String formula ) throws FormulaParseException {
		val = formula;
	}

	@Override
	public String getCellFormula() {
		return val;
	}

	@Override
	public double getNumericCellValue() {
		return Double.parseDouble( val );
	}

	@Override
	public Date getDateCellValue() {
		return DateUtil.getJavaDate( getNumericCellValue() );
	}

	@Override
	public RichTextString getRichStringCellValue() {
		return new HSSFRichTextString( val );
	}

	@Override
	public String getStringCellValue() {
		return val;
	}

	@Override
	public void setCellValue( boolean value ) {
		val = Boolean.toString( value );
	}

	@Override
	public void setCellErrorValue( byte value ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean getBooleanCellValue() {
		return Boolean.parseBoolean( val );
	}

	@Override
	public byte getErrorCellValue() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setCellStyle( CellStyle style ) {
		this.style = style;
	}

	@Override
	public CellStyle getCellStyle() {
		return style;
	}

	@Override
	public void setAsActiveCell() {
	}

	@Override
	public void setCellComment( Comment comment ) {
		this.comment = comment;
	}

	@Override
	public Comment getCellComment() {
		return comment;
	}

	@Override
	public void removeCellComment() {
		comment = null;
	}

	@Override
	public Hyperlink getHyperlink() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setHyperlink( Hyperlink link ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public CellRangeAddress getArrayFormulaRange() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isPartOfArrayFormulaGroup() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

}
