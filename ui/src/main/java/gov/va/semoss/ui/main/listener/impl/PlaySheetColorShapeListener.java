/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.components.renderers.ColorRenderer;
import gov.va.semoss.ui.components.renderers.ResourceNameRenderer;
import gov.va.semoss.ui.components.renderers.ShapeRenderer;
import gov.va.semoss.ui.components.renderers.TableColorRenderer;
import gov.va.semoss.ui.components.renderers.TableShapeRenderer;
import gov.va.semoss.ui.helpers.GraphColorRepository;
import gov.va.semoss.ui.helpers.GraphShapeRepository;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.awt.Color;
import java.awt.Shape;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.TableColumnModel;

/**
 */
public class PlaySheetColorShapeListener implements InternalFrameListener {

	private final PlaySheetFrame psf;

	public PlaySheetColorShapeListener( PlaySheetFrame ps ) {
		this.psf = ps;
	}

	/**
	 * Method internalFrameActivated.
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameActivated( InternalFrameEvent e ) {
		JTable colorShapeTable = DIHelper.getJTable( Constants.COLOR_SHAPE_TABLE );

		PlaySheetCentralComponent pscc = psf.getActivePlaySheet();
		if ( pscc instanceof GraphPlaySheet ) {
			GraphPlaySheet ps = GraphPlaySheet.class.cast( pscc );

			colorShapeTable.setModel( ps.getColorShapeData() );

			TableColumnModel tcm = colorShapeTable.getColumnModel();

			tcm.getColumn( 1 ).setCellRenderer( new ResourceNameRenderer() );

			JComboBox<Shape> shapes = new JComboBox<>( GraphShapeRepository.instance().getAllShapes() );
			shapes.setRenderer( new ShapeRenderer() );
			tcm.getColumn( 2 ).setCellRenderer( new TableShapeRenderer() );
			tcm.getColumn( 2 ).setCellEditor( new DefaultCellEditor( shapes ) );

			JComboBox<Color> colors = new JComboBox<>( GraphColorRepository.instance().getAllNamedColors() );
			colors.setRenderer( new ColorRenderer() );
			tcm.getColumn( 3 ).setCellRenderer( new TableColorRenderer() );
			tcm.getColumn( 3 ).setCellEditor( new DefaultCellEditor( colors ) );
		}
		else {
			internalFrameClosed( null );
		}
	}

	/**
	 * Method internalFrameClosed - need to empty the tables
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameClosed( InternalFrameEvent e ) {
		Utility.resetJTable( Constants.COLOR_SHAPE_TABLE );
	}

	@Override
	public void internalFrameOpened( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameClosing( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameIconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameDeiconified( InternalFrameEvent e ) {
	}

	@Override
	public void internalFrameDeactivated( InternalFrameEvent e ) {
	}
}
