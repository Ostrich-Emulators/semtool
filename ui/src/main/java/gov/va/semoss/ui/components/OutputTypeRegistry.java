/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.om.InsightOutputType;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridPlaySheet;
import gov.va.semoss.ui.components.playsheets.GridRAWPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.util.GuiUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**
 * A class to link {@link InsightOutputType}s to the
 * {@link PlaySheetCentralComponent}s
 *
 * @author ryan
 */
public class OutputTypeRegistry {

	private static final ImageIcon BLANKIMG
			= GuiUtility.loadImageIcon( "icons16/blank_16.png" );
	private final Map<InsightOutputType, Class<? extends IPlaySheet>> lookup
			= new LinkedHashMap<>();
	private final Map<InsightOutputType, ImageIcon> icons = new HashMap<>();
	private final Map<InsightOutputType, String> hints = new HashMap<>();
	private final Map<InsightOutputType, String> names = new HashMap<>();

	public OutputTypeRegistry() {
	}

	public void register( InsightOutputType type, Class<? extends IPlaySheet> ip,
			String name, ImageIcon icon, String hint ) {
		lookup.put( type, ip );
		icons.put( type, icon );
		hints.put( type, hint );
		names.put( type, name );
	}

	public Map<InsightOutputType, Class<? extends IPlaySheet>> getPlaySheetMap() {
		return new HashMap<>( lookup );
	}

	/**
	 * Returns an instance of the playsheet class from the current PlaySheetEnum.
	 *
	 * @return getSheetInstance -- (IPlaySheet)
	 */
	public IPlaySheet getSheetInstance( InsightOutputType type ) {
		if ( lookup.containsKey( type ) ) {
			try {
				return lookup.get( type ).newInstance();
			}
			catch ( InstantiationException | IllegalAccessException e ) {
				Logger.getLogger( getClass() ).warn( "cannot instantiate playsheet class", e );
				return new GridPlaySheet();
			}
		}

		return new GridRAWPlaySheet();
	}

	/**
	 * Returns the playsheet icon of the current PlaySheetEnum.
	 *
	 * @return getSheetIcon -- (ImageIcon)
	 */
	public ImageIcon getSheetIcon( InsightOutputType type ) {
		return ( icons.containsKey( type ) ? icons.get( type ) : BLANKIMG );
	}

	/**
	 * Returns the playsheet "hint" (for the Custom Sparql Query Window) from the
	 * current PlaySheetEnum.
	 *
	 * @return getSheetHint -- (String)
	 */
	public String getSheetHint( InsightOutputType type ) {
		return ( hints.containsKey( type ) ? hints.get( type ) : "" );
	}

	public String getSheetName( InsightOutputType type ) {
		return ( names.containsKey( type ) ? names.get( type ) : "" );
	}

	public Collection<InsightOutputType> getRegisterKeys() {
		List<InsightOutputType> list = new ArrayList<>();
		list.addAll( lookup.keySet() );
		return list;
	}

	public InsightOutputType getTypeFromClass( Class<? extends IPlaySheet> klazz ) {
		for ( Map.Entry<InsightOutputType, Class<? extends IPlaySheet>> en : lookup.entrySet() ) {
			if ( en.getValue().equals( klazz ) ) {
				return en.getKey();
			}
		}

		return InsightOutputType.GRID;
	}
}
