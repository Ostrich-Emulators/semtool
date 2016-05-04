package com.ostrichemulators.semtool.ui.components.playsheets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import com.ostrichemulators.semtool.ui.components.playsheets.helpers.DupeHeatMapSheet;

public abstract class AppDupeHeatMapSheet extends DupeHeatMapSheet {

	private static final long serialVersionUID = -7745410541064513908L;

	public AppDupeHeatMapSheet() {
		super();
		setObjectTypes( "App1", "App2" );
	}

	/**
	 * The Application Duplication Heatmap defines its own queries. So, the user
	 * need not enter one into the Insight Manager.
	 */
	@Override
	public boolean requiresQuery() {
		return false;
	}

	@Override
	public abstract void createData();

	@Override
	protected BufferedImage getExportImage() throws IOException {
		return getExportImageFromSVGBlock();
	}
}
