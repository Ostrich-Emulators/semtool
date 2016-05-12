/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import com.ostrichemulators.semtool.om.Parameter;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.MultiSetMap;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A panel to present the user with insights and parameters for the insights
 *
 * @author ryan
 */
public class BindingPanel extends JPanel {

	private static final Logger log = Logger.getLogger( BindingPanel.class );
	private static final Pattern NEEDVARS
			= Pattern.compile( "^\\s*(?:SELECT\\s*[^\\{]+|CONSTRUCT\\s*\\{.*)\\{(.*)\\}$",
					Pattern.CASE_INSENSITIVE );
	private static final Pattern ONEVAR = Pattern.compile( "\\?(\\w+)" );
	private final Map<Parameter, UriComboBox> combos = new HashMap<>();
	private final ParameterValueListener listener = new ParameterValueListener();
	private Tree<Parameter, Integer> ordered;
	private IEngine engine;

	public BindingPanel() {
		GridBagLayout layout = new GridBagLayout();
		setLayout( layout );
		layout.columnWidths = new int[]{ 0, 0 };
		layout.rowHeights = new int[]{ 0, 0, 0 };
		layout.columnWeights = new double[]{ 0.0, 1.0 };
		layout.rowWeights = new double[]{ 0.0, 0.0 };
	}

	public BindingPanel( IEngine eng ) {
		this();
		engine = eng;
	}

	public void setEngine( IEngine eng ) {
		engine = eng;
	}

	public void setParameters( Collection<Parameter> params ) {
		ordered = treeify( params );

		for ( UriComboBox c : combos.values() ) {
			c.removeItemListener( listener );
		}
		removeAll();
		combos.clear();
		print( ordered, ordered.getRoot() );

		Deque<Parameter> todo = new ArrayDeque<>();
		todo.add( ordered.getRoot() );

		int pcount = -1;
		while ( !todo.isEmpty() ) {
			Parameter parent = todo.poll();

			Collection<Parameter> children = ordered.getChildren( parent );

			for ( Parameter child : children ) {
				pcount++;
				JLabel lbl = new JLabel( child.getLabel() );
				lbl.setBackground( getBackground() );

				UriComboBox cmb = new UriComboBox();
				cmb.setBackground( getBackground() );
				cmb.addItemListener( listener );
				cmb.getUriModel().addListDataListener( listener );
				combos.put( child, cmb );

				GridBagConstraints labelconstraints = new GridBagConstraints();
				labelconstraints.anchor = GridBagConstraints.WEST;
				labelconstraints.insets = new Insets( 0, 5, 5, 5 );
				labelconstraints.gridx = 0;
				labelconstraints.gridy = pcount;

				GridBagConstraints comboconstraints = new GridBagConstraints();
				comboconstraints.anchor = GridBagConstraints.NORTH;
				comboconstraints.fill = GridBagConstraints.HORIZONTAL;
				comboconstraints.insets = new Insets( 0, 5, 5, 0 );
				comboconstraints.gridx = 1;
				comboconstraints.gridy = pcount;
				
				add( lbl, labelconstraints );
				add( cmb, comboconstraints );

				todo.add( child );
			}
		}

		fillInCombos( ordered );
		repaint();
	}

	public Map<Parameter, Value> getBindings() {
		Map<Parameter, Value> map = new HashMap<>();
		for ( Map.Entry<Parameter, UriComboBox> en : combos.entrySet() ) {
			UriComboBox cmb = en.getValue();
			map.put( en.getKey(), cmb.getItemAt( cmb.getSelectedIndex() ) );
		}

		return map;
	}

	private void fillInCombos( Tree<Parameter, Integer> tree ) {
		// we only need to run queries for the toplevel parameters, because
		// everything else is handled by the item listener

		for ( Parameter p : tree.getChildren( tree.getRoot() ) ) {
			requery( p );
		}
	}

	private static void print( Tree<Parameter, Integer> tree, Parameter node ) {
		if ( log.isDebugEnabled() ) {

			StringBuilder sb = new StringBuilder( "Parameter Tree:" );
			int depth = tree.getDepth( node );
			for ( int i = 0; i < depth; i++ ) {
				sb.append( "  " );
			}
			sb.append( tree.getRoot().equals( node ) ? "<root>" : node );
			log.debug( sb.toString() );

			for ( Parameter p : tree.getChildren( node ) ) {
				print( tree, p );
			}
		}
	}

	private void requery( Parameter child ) {
		log.debug( "requerying for parameter: " + child );
		OneVarListQueryAdapter<URI> onevar
				= OneVarListQueryAdapter.getUriList( child.getDefaultQuery() );
		for ( Parameter ancestor : ordered.getPredecessors( child ) ) {
			if ( !ancestor.equals( ordered.getRoot() ) ) {
				UriComboBox cmb = combos.get( ancestor );
				onevar.bind( ancestor.getVariable(),
						cmb.getItemAt( cmb.getSelectedIndex() ) );
			}
		}

		List<URI> vals = engine.queryNoEx( onevar );
		UriComboBox combo = combos.get( child );
		combo.removeItemListener( listener );
		Map<URI, String> labels = Utility.getInstanceLabels( vals, engine );
		combo.setData( Utility.sortUrisByLabel( labels ) );
		//combo.setMaximumSize( new Dimension( 100, 25  ) );
		//combo.setPreferredSize( new Dimension( 100, 25  ) );
		combo.setMinimumSize( new Dimension( 10, 25  ) );
		
		combo.addItemListener( listener );
	}

	/**
	 * Figures out which parameters are dependent on others.
	 *
	 * @param unordered the parameters
	 * @return an list of Parameters, with
	 */
	private static Tree<Parameter, Integer> treeify( Collection<Parameter> unordered ) {
		Parameter root = new Parameter();

		// 1) run through all the parameters to see what variables they return, and 
		//     what variables the need (if any)
		// 2) go through our needs map, and if a parameter needs a variable
		//     provided by another, then it must be a child of the other
		// if a parameter doesn't have any values satisfied by other parameters, it's
		// a child of the root
		Map<String, Parameter> provides = new LinkedHashMap<>();
		MultiSetMap<Parameter, String> needs = new MultiSetMap<>();
		//Map<Parameter, Parameter> childParentLkp = new HashMap<>();
		MultiSetMap<Parameter, Parameter> parentChildren = new MultiSetMap<>();

		for ( Parameter p : unordered ) {
			String providervar = p.getVariable();
			provides.put( providervar, p );

			Matcher m = NEEDVARS.matcher( p.getDefaultQuery().replaceAll( "\n", " " ) );
			if ( m.matches() ) {
				String whereclause = m.group( 1 );

				Matcher varmatch = ONEVAR.matcher( whereclause );
				while ( varmatch.find() ) {
					String var = varmatch.group( 1 );
					if ( !var.equals( providervar ) ) {
						needs.add( p, var );
					}
				}
			}
		}

		for ( Map.Entry<Parameter, Set<String>> en : needs.entrySet() ) {
			Parameter needer = en.getKey();
			boolean isrootchild = true;

			for ( String need : en.getValue() ) {
				if ( provides.containsKey( need ) ) {
					Parameter provider = provides.get( need );
					parentChildren.add( provider, needer );
					isrootchild = false;
				}
			}

			if ( isrootchild ) {
				parentChildren.add( root, needer );
			}
		}

		// make sure we add to our tree parameters with no needs
		for ( Parameter p : unordered ) {
			if ( !needs.containsKey( p ) ) {
				parentChildren.add( root, p );
			}
		}

		DelegateTree<Parameter, Integer> ordered = new DelegateTree<>();
		ordered.addVertex( root );
		Deque<Parameter> todo = new ArrayDeque<>();
		todo.add( root );
		int edgecount = 0;
		while ( !todo.isEmpty() ) {
			Parameter parent = todo.poll();

			for ( Parameter child : parentChildren.getNN( parent ) ) {
				ordered.addChild( edgecount++, parent, child );
				todo.add( child );
			}
		}

		return ordered;

	}

	private class ParameterValueListener implements ItemListener, ListDataListener {

		@Override
		public void itemStateChanged( ItemEvent e ) {
			log.debug( "item state changed for " + e.getSource() );
			handleChange( UriComboBox.class.cast( e.getSource() ) );
		}

		@Override
		public void intervalAdded( ListDataEvent e ) {
		}

		@Override
		public void intervalRemoved( ListDataEvent e ) {
		}

		@Override
		public void contentsChanged( ListDataEvent e ) {

			// this is a little ugly, because the model is doing the signalling,
			// so we need to figure out which combobox it came from
			Object model = e.getSource();
			for ( Map.Entry<Parameter, UriComboBox> en : combos.entrySet() ) {
				if ( en.getValue().getUriModel().equals( model ) ) {
					log.debug( "contents changed for " + en.getValue() );
					handleChange( en.getValue() );
				}
			}
		}

		private void handleChange( UriComboBox combo ) {
			// figure out what parameter we're talking about
			// so we can decide if it's children need to requery
			for ( Map.Entry<Parameter, UriComboBox> en : combos.entrySet() ) {
				if ( en.getValue().equals( combo ) ) {
					for ( Parameter p : ordered.getChildren( en.getKey() ) ) {
						log.debug( "new value for parameter: " + en.getKey() );
						requery( p );
					}
				}
			}
		}
	}
}
