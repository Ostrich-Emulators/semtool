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
	private double dval;
	private Date dateval;
	private String stringval;
	private boolean bval;
	private Comment comment;

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
		dval = value;
	}

	@Override
	public void setCellValue( Date value ) {
		dateval = value;
	}

	@Override
	public void setCellValue( Calendar value ) {
		dateval = value.getTime();
	}

	@Override
	public void setCellValue( RichTextString value ) {
		stringval = value.getString();
	}

	@Override
	public void setCellValue( String value ) {
		stringval = value;
	}

	@Override
	public void setCellFormula( String formula ) throws FormulaParseException {
		stringval = formula;
	}

	@Override
	public String getCellFormula() {
		return stringval;
	}

	@Override
	public double getNumericCellValue() {
		return dval;
	}

	@Override
	public Date getDateCellValue() {
		return dateval;
	}

	@Override
	public RichTextString getRichStringCellValue() {
		return new HSSFRichTextString( stringval );
	}

	@Override
	public String getStringCellValue() {
		return stringval;
	}

	@Override
	public void setCellValue( boolean value ) {
		bval = value;
	}

	@Override
	public void setCellErrorValue( byte value ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean getBooleanCellValue() {
		return bval;
	}

	@Override
	public byte getErrorCellValue() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setCellStyle( CellStyle style ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public CellStyle getCellStyle() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
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
