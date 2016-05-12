package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.junit.Test;

public class DefaultIconsTest {

	@Test
	public void verifySaveIcon() {
		BufferedImage image =  GuiUtility.loadImage( "icons16/save_diskette1_16.png" );
		assertNotNull(image);
	}

}
