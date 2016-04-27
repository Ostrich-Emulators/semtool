/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main.xlsxml;

import com.ostrichemulators.semtool.poi.main.xlsxml.LoadingSheetXmlHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class LoadingSheetXmlHandlerTest {

	@Test
	public void testGetColNum() {
		assertEquals( 0, LoadingSheetXmlHandler.getColNum( "A" ) );
		assertEquals( 1, LoadingSheetXmlHandler.getColNum( "B" ) );
		assertEquals( 26, LoadingSheetXmlHandler.getColNum( "AA" ) );
		assertEquals( 107, LoadingSheetXmlHandler.getColNum( "DD" ) );
		assertEquals( 734, LoadingSheetXmlHandler.getColNum( "ABG" ) );
	}
}
