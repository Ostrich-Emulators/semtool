/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

/**
 *
 * @author ryan
 */
public class RepositoryRenderer extends DefaultListCellRenderer {

	private static final Icon pin;
	private static final Icon nopin;

	static {
		pin = new ImageIcon( Utility.loadImage( "icons16/dbpin_16.png" ) );
		nopin = new ImageIcon( Utility.loadImage( "icons16/db_16.png" ) );
	}

	@Override
	public Component getListCellRendererComponent( JList list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		IEngine eng = IEngine.class.cast( val );

		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		String title = null;
		try {
			eng.query( mq );
			title = mq.getOne();
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			// don't care
		}

		if ( null == title ) {
			title = eng.getEngineName();
		}

		boolean pinned = Boolean.parseBoolean( eng.getProperty( Constants.PIN_KEY ) );
		super.getListCellRendererComponent( list, title, idx, sel, hasfocus );
		setIcon( pinned ? pin : nopin );

		return this;
	}
}
