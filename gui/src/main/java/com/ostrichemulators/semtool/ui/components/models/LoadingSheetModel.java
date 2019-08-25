/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.DataIterator;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ryan
 */
public class LoadingSheetModel extends ValueTableModel {

	private static final Logger log = Logger.getLogger( LoadingSheetModel.class );
	private LoadingSheetData sheetdata;
	private int errorcount = 0;
	private QaChecker realtimer = null;

	private LoadingSheetModel() {
		super( true );
	}

	public LoadingSheetModel( LoadingSheetData naps ) {
		this();
		setLoadingSheetData( naps );
	}

	public static LoadingSheetModel forRel( String rel, List<Value[]> valdata,
			List<String> headers ) {
		List<String> list = new ArrayList<>( headers );
		String props[] = list.toArray( new String[0] );
		String stype = list.remove( 0 );
		String otype = list.remove( 0 );

		LoadingSheetData lsd = LoadingSheetData.relsheet( stype, otype, rel );
		lsd.addProperties( list );

		for ( Value[] val : valdata ) {
			LoadingNodeAndPropertyValues nap
					= lsd.add( val[0].stringValue(), val[1].stringValue() );
			for ( int i = 2; i < val.length; i++ ) {
				nap.put( props[i], val[i] );
			}
		}

		return new LoadingSheetModel( lsd );
	}

	public static LoadingSheetModel forNode( List<Value[]> valdata, List<String> headers ) {
		List<String> list = new ArrayList<>( headers );
		String props[] = list.toArray( new String[0] );
		String stype = list.remove( 0 );

		LoadingSheetData lsd = LoadingSheetData.nodesheet( stype );
		lsd.addProperties( list );

		for ( Value[] val : valdata ) {
			Map<String, Value> propmap = new HashMap<>();
			for ( int i = 1; i < val.length; i++ ) {
				propmap.put( props[i], val[i] );
			}

			LoadingNodeAndPropertyValues nap = lsd.add( val[0].stringValue(), propmap );
		}

		return new LoadingSheetModel( lsd );
	}

	public void setQaChecker( QaChecker el ) {
		realtimer = el;
		checkForErrors();
	}

	public boolean isRealTimeChecking() {
		return ( null != realtimer );
	}

	public void checkForErrors() {
		if ( null == realtimer ) {
			// no error checking, so reset all errors
			DataIterator di = sheetdata.iterator();
			while ( di.hasNext() ) {
				LoadingNodeAndPropertyValues nap = di.next();

				nap.setSubjectIsError( false );
				nap.setObjectIsError( false );
			}

			sheetdata.setSubjectTypeIsError( false );
			sheetdata.setObjectTypeIsError( false );
			sheetdata.setRelationIsError( false );

			for ( String prop : sheetdata.getProperties() ) {
				sheetdata.setPropertyIsError( prop, false );
			}
			errorcount = 0;
			fireTableDataChanged();
		}
		else {
			// check everything when we have a non-null engine loader
			LoadingSheetData lsd = realtimer.checkModelConformance( sheetdata );
			setModelErrors( lsd );

			// need to recheck the whole loading sheet now
			List<LoadingNodeAndPropertyValues> errors
					= realtimer.checkConformance( lsd, null, false );
			setConformanceErrors( errors );
			errorcount = errors.size();
		}
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
		if ( null != sheetdata ) {
			sheetdata.setHeaders( heads );
			checkForErrors();
		}
	}

	@Override
	public void setValueAt( Object aValue, int r, int c ) {
		boolean isinsert = isInsertRow( r );

		ValueFactory vf = SimpleValueFactory.getInstance();

		if ( isinsert ) {
			LoadingNodeAndPropertyValues nap = sheetdata.add( aValue.toString() );

			if ( null != realtimer ) {
				boolean iserr = !realtimer.instanceExists( nap.getSubjectType(),
						nap.getSubject() );
				nap.setSubjectIsError( iserr );
				if ( iserr ) {
					errorcount++;
				}
			}
		}
		else {
			LoadingNodeAndPropertyValues nap = sheetdata.getData().get( r );
			final boolean hadError = nap.hasError();

			if ( 0 == c ) {
				nap.setSubject( aValue.toString() );
			}
			else {
				if ( sheetdata.isRel() ) {
					if ( 1 == c ) {
						nap.setObject( aValue.toString() );
					}
					else {
						// we're setting a property
						String prop = sheetdata.getHeaders().get( c );
						// FIXME: handle datatypes
						nap.put( prop, vf.createLiteral( aValue.toString() ) );
					}
				}
				else {
					// we're setting a property
					String prop = sheetdata.getHeaders().get( c );
					// FIXME: handle datatypes
					nap.put( prop, vf.createLiteral( aValue.toString() ) );
				}
			}

			// do a real-time conformance check?
			if ( null != realtimer && ( 0 == c || ( 1 == c && isRel() ) ) ) {
				if ( 0 == c ) {
					boolean iserr
							= !realtimer.instanceExists( nap.getSubjectType(), nap.getSubject() );
					nap.setSubjectIsError( iserr );
				}
				else {
					boolean iserr
							= !realtimer.instanceExists( nap.getObjectType(), nap.getObject() );
					nap.setObjectIsError( iserr );
				}

				boolean hasError = nap.hasError();
				if ( hasError != hadError ) {
					// if we have an error, then we didn't use to, so our errors go up
					errorcount += ( hasError ? 1 : -1 );
				}
			}
		}

		super.setValueAt( aValue, r, c );
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

		DataIterator di = sheetdata.iterator();
		while ( di.hasNext() ) {
			LoadingNodeAndPropertyValues nap = di.next();
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

		String ot = sheetdata.getObjectType();
		if ( null != ot && ot.equals( lsd.getObjectType() ) ) {
			sheetdata.setObjectTypeIsError( lsd.hasObjectTypeError() );
		}

		String rn = sheetdata.getRelname();
		if ( null != rn && rn.equals( lsd.getRelname() ) ) {
			sheetdata.setRelationIsError( lsd.hasRelationError() );
		}

		for ( String prop : lsd.getProperties() ) {
			sheetdata.setPropertyIsError( prop, lsd.propertyIsError( prop ) );
		}

		fireTableDataChanged();
	}

	public void setRelationshipName( String relname ) {
		sheetdata.setRelname( relname );
		LoadingSheetData lsd = realtimer.checkModelConformance( sheetdata );
		setModelErrors( lsd );
	}

	public LoadingSheetData copyLoadingSheetHeaders() {
		return LoadingSheetData.copyHeadersOf( sheetdata );
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

		if ( sheetdata.hasRelationError() ) {
			errors.add( -1 );
		}

		int i = ( isRel() ? 2 : 1 );
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

		int count = 0;

		log.debug( "filling model for tab: " + naps.getName() );
		ValueFactory vf = SimpleValueFactory.getInstance();
		List<String> heads = naps.getHeaders();
		List<Value[]> valdata = new ArrayList<>();

		DataIterator di = naps.iterator();
		while ( di.hasNext() ) {
			count++;

			LoadingNodeAndPropertyValues node = di.next();
			valdata.add( node.convertToValueArray( vf ) );
			if ( node.hasError() ) {
				errorcount++;
			}

			if ( 0 == count % 100 ) {
				log.debug( String.format( "filled %d rows", count ) );
			}
		}
		log.debug( String.format( "filled %d rows", count ) );

		super.setData( valdata, heads );
	}

	public LoadingNodeAndPropertyValues getNap( int row ) {
		if ( row < sheetdata.rows() ) {
			return sheetdata.getData().get( row );
		}
		return null;
	}

	public LoadingSheetData toLoadingSheet( String tabname ) {
		List<String> heads = sheetdata.getHeaders();
		String stype = heads.remove( 0 );

		LoadingSheetData lsd;
		int firstprop;
		if ( sheetdata.isRel() ) {
			String otype = heads.remove( 0 );
			lsd = LoadingSheetData.relsheet( tabname, stype, otype, sheetdata.getRelname() );
			firstprop = 2;
		}
		else {
			lsd = LoadingSheetData.nodesheet( tabname, stype );
			firstprop = 1;
		}
		lsd.addProperties( heads );

		int rows = getRealRowCount();
		int cols = getColumnCount();
		boolean hasprops = ( cols > firstprop );

		DataIterator di = sheetdata.iterator();
		for ( int r = 0; r < rows; r++ ) {
			String sbj = getValueAt( r, 0 ).toString();
			LoadingSheetData.LoadingNodeAndPropertyValues nap = ( sheetdata.isRel()
					? lsd.add( sbj, getValueAt( r, 1 ).toString() ) : lsd.add( sbj ) );

			LoadingNodeAndPropertyValues oldnap = di.next();
			nap.setSubjectIsError( oldnap.isSubjectError() );
			nap.setObjectIsError( oldnap.isObjectError() );

			if ( hasprops ) {
				for ( int p = 0; p < heads.size(); p++ ) {
					Value v = getRdfValueAt( r, p + firstprop ); // remember: we took off some cols
					if ( null != v ) {
						nap.put( heads.get( p ), v );
					}
				}
			}
		}

		return lsd;
	}
}
