/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.models;

import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.NodeLoadingSheetData;
import gov.va.semoss.poi.main.RelationshipLoadingSheetData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public class LoadingSheetModel extends ValueTableModel {

	private static final Logger log = Logger.getLogger( LoadingSheetModel.class );
	private LoadingSheetData sheetdata;
	private int errorcount = 0;

	public LoadingSheetModel() {
		super( true );

	}

	public LoadingSheetModel( LoadingSheetData naps ) {
		this();
		setLoadingSheetData( naps );
	}

	public boolean isRel() {
		return sheetdata.isRel();
	}

	@Override
	public void setData( List<Value[]> newdata, List<String> heads ) {
		log.error( "this function is unsafe here...use setLoadingSheetData instead" );
		super.setData( newdata, heads );
	}

	@Override
	public void setHeaders( List<String> heads ) {
		super.setHeaders( heads );
		if( null != sheetdata ){
			sheetdata.setHeaders( heads );
		}
	}

	/**
	 * Sets the conformance errors values based on the given values. Values not
	 * already in the model are ignored
	 *
	 * @param errors the {@link LoadingNodeAndPropertyValues} to update
	 */
	public void setConformanceErrors( Collection<LoadingNodeAndPropertyValues> errors ) {
		// use 0 for subject, 1 for object, and null for both
		errorcount = 0;
		Map<LoadingNodeAndPropertyValues, Integer> hash = new HashMap<>();
		for ( LoadingNodeAndPropertyValues nap : errors ) {
			Integer problem = null;
			if ( nap.isSubjectError() ) {
				problem = 0;
			}
			if ( nap.isObjectError() ) {
				problem = ( null == problem ? 1 : null );
			}

			// log.debug( "in setConfE: " + nap + " => " + problem );
			hash.put( nap, problem );
		}

		for ( LoadingNodeAndPropertyValues nap : sheetdata.getData() ) {
			if ( hash.containsKey( nap ) ) {
				Integer problem = hash.get( nap );
				if ( null == problem ) {
					nap.setSubjectIsError( true );
					nap.setObjectIsError( true );
				}
				else if ( 0 == problem ) {
					nap.setSubjectIsError( true );
					nap.setObjectIsError( false );
				}
				else {
					nap.setSubjectIsError( false );
					nap.setObjectIsError( true );
				}
			}
			else {
				nap.setSubjectIsError( false );
				nap.setObjectIsError( false );
			}
		}

		errorcount += errors.size();
		fireTableDataChanged();
	}

	public void setModelErrors( LoadingSheetData lsd ) {
		if ( sheetdata.getSubjectType().equals( lsd.getSubjectType() ) ) {
			sheetdata.setSubjectTypeIsError( lsd.hasSubjectTypeError() );
		}
		if ( sheetdata.getObjectType().equals( lsd.getObjectType() ) ) {
			sheetdata.setObjectTypeIsError( lsd.hasObjectTypeError() );
		}
		if ( sheetdata.getRelname().equals( lsd.getRelname() ) ) {
			sheetdata.setRelationIsError( lsd.hasRelationError() );
		}

		for ( String prop : lsd.getProperties() ) {
			sheetdata.setPropertyIsError( prop, lsd.propertyIsError( prop ) );
		}

		fireTableDataChanged();
	}

	public boolean hasModelErrors() {
		return sheetdata.hasModelErrors();
	}

	public boolean hasConformanceErrors() {
		return ( errorcount > 0 );
	}

	public List<Integer> getModelErrorColumns() {
		List<Integer> errors = new ArrayList<>();
		if ( sheetdata.hasSubjectTypeError() ) {
			errors.add( 0 );
		}
		if ( sheetdata.hasObjectTypeError() ) {
			errors.add( 1 );
		}

		int i = 2;
		for ( String p : sheetdata.getProperties() ) {
			if ( sheetdata.propertyIsError( p ) ) {
				errors.add( i );
			}
			i++;
		}

		return errors;
	}

	public int getConformanceErrorCount() {
		return errorcount;
	}

	public final void setLoadingSheetData( LoadingSheetData naps ) {
		sheetdata = naps;
		errorcount = 0;

		log.debug( "filling model for tab: " + naps.getName() );
		ValueFactory vf = new ValueFactoryImpl();
		List<String> heads = naps.getHeaders();
		List<Value[]> valdata = new ArrayList<>();
		for ( LoadingSheetData.LoadingNodeAndPropertyValues node : naps.getData() ) {
			valdata.add( node.convertToValueArray( vf ) );
			if ( node.hasError() ) {
				errorcount++;
			}
		}

		super.setData( valdata, heads );
	}

	public LoadingNodeAndPropertyValues getNap( int row ) {
		if ( row < sheetdata.getData().size() ) {
			return sheetdata.getData().get( row );
		}
		return null;
	}

	public NodeLoadingSheetData toNodeLoadingSheetData( String tabname ) {
		List<String> heads = sheetdata.getHeaders();
		String stype = heads.remove( 0 );

		NodeLoadingSheetData lsd
				= new NodeLoadingSheetData( tabname, stype, heads );
		int rows = getRowCount();
		int cols = getColumnCount();
		boolean hasprops = ( cols > 1 );

		for ( int r = 0; r < rows; r++ ) {
			LoadingSheetData.LoadingNodeAndPropertyValues nap
					= lsd.add( getValueAt( r, 0 ).toString() );
			nap.setSubjectIsError( sheetdata.getData().get( r ).isSubjectError() );
			nap.setObjectIsError( sheetdata.getData().get( r ).isObjectError() );

			if ( hasprops ) {
				for ( int p = 0; p < heads.size(); p++ ) {
					Value v = getRdfValueAt( r, p + 1 ); // remember: we took off the first col
					if ( null != v ) {
						nap.put( heads.get( p ), v );
					}
				}
			}
		}

		return lsd;
	}

	public RelationshipLoadingSheetData toRelationshipLoadingSheetData( String tabname ) {
		List<String> heads = sheetdata.getHeaders();

		String stype = heads.remove( 0 );
		String otype = heads.remove( 0 );
		RelationshipLoadingSheetData lsd = new RelationshipLoadingSheetData( tabname,
				stype, otype, sheetdata.getRelname(), heads );
		int rows = getRowCount();
		int cols = getColumnCount();
		boolean hasprops = ( cols > 2 );

		for ( int r = 0; r < rows; r++ ) {
			String sbj = getValueAt( r, 0 ).toString();
			String obj = getValueAt( r, 1 ).toString();
			LoadingSheetData.LoadingNodeAndPropertyValues nap = lsd.add( sbj, obj );
			nap.setSubjectIsError( sheetdata.getData().get( r ).isSubjectError() );
			nap.setObjectIsError( sheetdata.getData().get( r ).isObjectError() );

			if ( hasprops ) {
				for ( int p = 0; p < heads.size(); p++ ) {
					Value v = getRdfValueAt( r, p + 2 ); // remember: we took off the first two cols
					if ( null != v ) {
						nap.put( heads.get( p ), v );
					}
				}
			}
		}

		return lsd;
	}
}
