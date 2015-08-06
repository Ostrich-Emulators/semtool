package gov.va.semoss.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

import org.mvel2.MVEL;
import gov.va.semoss.om.SEMOSSVertex;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class CustomNodeCreator {

	private static final Logger log = Logger.getLogger( CustomNodeCreator.class );

	GraphPlaySheet ps = null;
	String selected = null;
	String expression = null;

	public void setGraphPlaySheet( GraphPlaySheet ps ) {
		this.ps = ps;
	}

	public void execute() {
		// core job of this class is to create custom nodes
		// Here is how it works
		// 1. Gets the reference to graph playsheet
		// 2. Displays the types of nodes in VertexFilterData
		// 3. The user selects one of the types of node
		// 4. Next it asks for a formula
		// 5. Now it runs through the formula for each of it
		// 6. Telling us what is the evaluated piece

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		String input = null;
		try {
			while ( ( input = reader.readLine() ) != null ) {
				if ( input.equalsIgnoreCase( "H" ) ) {
					System.out.println( "L - List all the types of nodes on this graph" );
					System.out.println( "S - Select a type of node from the list of nodes. S <Node to Select>" );
					System.out.println( "E - Express what you would like to do with this E <Expression>" );
					System.out.println( "R - Run this and output the results " );
					System.out.println( "C - Clear all and start fresh " );
					System.out.println( "H - This menu / help " );
				}
				if ( input.equalsIgnoreCase( "L" ) ) {
					// displays the vertex filter data
					MultiMap<URI, SEMOSSVertex> vfd = ps.getVerticesByType();
					System.out.println( "Vertex Types Available " );
					System.out.println( "======================= " );
					Collection<URI> keys = vfd.keySet();
					int keyCount = 1;
					for ( URI key : keys ) {
						log.debug( keyCount + " ." + key );
						keyCount++;
					}
				}
				if ( input.toUpperCase().startsWith( "S" ) ) {
					StringTokenizer tokens = new StringTokenizer( input );
					tokens.nextToken();
					selected = tokens.nextToken();
				}
				if ( input.toUpperCase().startsWith( "E" ) ) {
					StringTokenizer tokens = new StringTokenizer( input );
					tokens.nextToken();
					expression = tokens.nextToken();
				}
				if ( input.equalsIgnoreCase( "R" ) ) {
					MultiMap<URI, SEMOSSVertex> vfd = ps.getVerticesByType();

					// show me the MVEL Magic BOY !!
					if ( selected != null && vfd.containsKey( new URIImpl( selected ) ) );
					{
						List<SEMOSSVertex> objs = vfd.get( new URIImpl( selected ) );
						for ( int vertIndex = 0; vertIndex <= objs.size(); vertIndex++ ) {
							System.out.println( "" + MVEL.eval( expression, objs.get( vertIndex ) ) );
						}

						// the patterns are
						// basic properties
						// navigate to another node and then properties
					}
				}
			}
		}
		catch ( IOException e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}
	}
}
