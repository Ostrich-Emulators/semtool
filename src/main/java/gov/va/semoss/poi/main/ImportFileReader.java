/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author ryan
 */
public interface ImportFileReader {

	public abstract ImportData readOneFile( File f ) throws IOException;
}
