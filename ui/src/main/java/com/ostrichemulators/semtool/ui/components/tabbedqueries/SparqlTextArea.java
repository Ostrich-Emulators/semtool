package com.ostrichemulators.semtool.ui.components.tabbedqueries;

import com.ostrichemulators.semtool.ui.actions.AbstractSavingAction;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;

/**
 * Applies SPARQL syntax-highlighting, code-completion, and hint-insertion to
 * the RSyntaxTextArea/
 *
 * This example uses RSyntaxTextArea 2.0.1
 * .<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class SparqlTextArea extends RSyntaxTextArea {

	private static final long serialVersionUID = 1L;
	private final CodeTemplateManager ctm;
	private final QuerySavingAction saver = new QuerySavingAction();

	private String strTag;

	public SparqlTextArea() {
		// Whether templates are enabled is a global property affecting all
		// RSyntaxTextAreas, so this method is static.
		RSyntaxTextArea.setTemplatesEnabled( true );

		// Code templates are shared among all RSyntaxTextAreas. You add and
		// remove templates through the shared CodeTemplateManager instance.
		ctm = RSyntaxTextArea.getCodeTemplateManager();

		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping( "text/sparql", SPARQLTokenMaker.class.getCanonicalName() );

		setSyntaxEditingStyle( "text/sparql" );
		changeStyleViaThemeXml();
		setCodeFoldingEnabled( false );
		setAntiAliasingEnabled( true );
		setLineWrap( true );
		setWrapStyleWord( true );

		Font f = getFont();
		// use a mono-spaced font so indentation works well		
		setFont( new Font( Font.MONOSPACED, f.getStyle(), f.getSize() ) );

		CompletionProvider provider = createCompletionProvider();

		AutoCompletion ac = new AutoCompletion( provider );
		ac.install( this );

		setBracketMatchingEnabled( true );

		// Remove the code-folding component and it's separator from the popup menu:
		JPopupMenu popup = getPopupMenu();
		popup.remove( popup.getComponent( 9 ) ); // the separator
		popup.remove( popup.getComponent( 9 ) ); // the item
		popup.addSeparator();
		popup.add( saver );
	}

	/**
	 * Getter for an arbitrary string associated with this RSyntaxTextArea.
	 *
	 * @return getTag -- (String) Described above.
	 */
	public String getTag() {
		return this.strTag;
	}

	/**
	 * Setter for an arbitrary string associated with this RSyntaxTextArea
	 *
	 * @param strTag -- (String) Described above.
	 */
	public void setTag( String strTag ) {
		this.strTag = strTag;
	}

	/**
	 * Changes the styles used by the editor via an XML file specification. This
	 * method is preferred because of its ease and modularity.
	 */
	private void changeStyleViaThemeXml() {
		try {
			Theme theme = Theme.load( getClass().getResourceAsStream( "/idea.xml" ) );
			theme.apply( this );
		}
		catch ( IOException ioe ) { // Never happens
			Logger.getLogger( getClass() ).error( ioe, ioe );
		}
	}

	/**
	 * Create a simple provider that adds some SPARQL-related completions.
	 *
	 * @return The completion provider.
	 */
	private CompletionProvider createCompletionProvider() {

		// A DefaultCompletionProvider is the simplest concrete implementation
		// of CompletionProvider. This provider has no understanding of
		// language semantics. It simply checks the text entered up to the
		// caret position for a match against known completions. This is all
		// that is needed in the majority of cases.
		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		// Add completions for all SPARQL keywords. A BasicCompletion is just
		// a straightforward word completion.
		provider.addCompletion( new BasicCompletion( provider, "SELECT" ) );
		provider.addCompletion( new BasicCompletion( provider, "CONSTRUCT" ) );
		provider.addCompletion( new BasicCompletion( provider, "ASK" ) );
		provider.addCompletion( new BasicCompletion( provider, "DESCRIBE" ) );
		provider.addCompletion( new BasicCompletion( provider, "INSERT" ) );
		provider.addCompletion( new BasicCompletion( provider, "DELETE" ) );
		provider.addCompletion( new BasicCompletion( provider, "DATA" ) );
		provider.addCompletion( new BasicCompletion( provider, "FROM" ) );
		provider.addCompletion( new BasicCompletion( provider, "NAMED" ) );
		provider.addCompletion( new BasicCompletion( provider, "GRAPH" ) );
		provider.addCompletion( new BasicCompletion( provider, "OPTIONAL" ) );
		provider.addCompletion( new BasicCompletion( provider, "UNION" ) );
		provider.addCompletion( new BasicCompletion( provider, "WITH" ) );
		ctm.addTemplate( new StaticCodeTemplate( "WITH", null, "SELECT . . .\nWITH{\n   . . . subQuery . . .\n} AS %subQueryName\nWHERE{\n   . . .\n   INCLUDE %subQueryName .\n}" ) );
		provider.addCompletion( new BasicCompletion( provider, "INCLUDE" ) );
		ctm.addTemplate( new StaticCodeTemplate( "INCLUDE", null, "SELECT . . .\nWITH{\n   . . . subQuery . . .\n} AS %subQueryName\nWHERE{\n   . . .\n   INCLUDE %subQueryName .\n}" ) );
		provider.addCompletion( new BasicCompletion( provider, "WHERE" ) );
		provider.addCompletion( new BasicCompletion( provider, "DISTINCT" ) );
		provider.addCompletion( new BasicCompletion( provider, "REDUCED" ) );
		provider.addCompletion( new BasicCompletion( provider, "ORDER" ) );
		provider.addCompletion( new BasicCompletion( provider, "BY" ) );
		provider.addCompletion( new BasicCompletion( provider, "GROUP" ) );
		provider.addCompletion( new BasicCompletion( provider, "HAVING" ) );
		provider.addCompletion( new BasicCompletion( provider, "OFFSET" ) );
		provider.addCompletion( new BasicCompletion( provider, "LIMIT" ) );
		provider.addCompletion( new BasicCompletion( provider, "BIND" ) );
		ctm.addTemplate( new StaticCodeTemplate( "BIND", null, "BIND(<http://some_URI> AS ?variable) or BIND(some_expression AS ?variable)" ) );
		provider.addCompletion( new BasicCompletion( provider, "AS" ) );
		provider.addCompletion( new BasicCompletion( provider, "VALUES" ) );
		provider.addCompletion( new BasicCompletion( provider, "FILTER" ) );

		provider.addCompletion( new BasicCompletion( provider, "COUNT" ) );
		ctm.addTemplate( new StaticCodeTemplate( "COUNT", null, "COUNT(?anyVariableType)" ) );
		provider.addCompletion( new BasicCompletion( provider, "SUM" ) );
		ctm.addTemplate( new StaticCodeTemplate( "SUM", null, "SUM(?numberVariable)" ) );
		provider.addCompletion( new BasicCompletion( provider, "MIN" ) );
		ctm.addTemplate( new StaticCodeTemplate( "MIN", null, "MIN(?numberVariable)" ) );
		provider.addCompletion( new BasicCompletion( provider, "MAX" ) );
		ctm.addTemplate( new StaticCodeTemplate( "MAX", null, "MAX(?numberVariable)" ) );
		provider.addCompletion( new BasicCompletion( provider, "AVG" ) );
		ctm.addTemplate( new StaticCodeTemplate( "AVG", null, "AVG(?numberVariable)" ) );
		provider.addCompletion( new BasicCompletion( provider, "GROUP_CONCAT" ) );
		provider.addCompletion( new BasicCompletion( provider, "SAMPLE" ) );
		provider.addCompletion( new BasicCompletion( provider, "IF" ) );
		provider.addCompletion( new BasicCompletion( provider, "COALESCE" ) );
		provider.addCompletion( new BasicCompletion( provider, "EXISTS" ) );
		provider.addCompletion( new BasicCompletion( provider, "NOT" ) );
		provider.addCompletion( new BasicCompletion( provider, "||" ) );
		provider.addCompletion( new BasicCompletion( provider, "&&" ) );
		provider.addCompletion( new BasicCompletion( provider, "=" ) );
		provider.addCompletion( new BasicCompletion( provider, "sameTerm" ) );
		provider.addCompletion( new BasicCompletion( provider, "IN" ) );
		provider.addCompletion( new BasicCompletion( provider, "isIRI" ) );
		provider.addCompletion( new BasicCompletion( provider, "isBlank" ) );
		provider.addCompletion( new BasicCompletion( provider, "isLiteral" ) );
		provider.addCompletion( new BasicCompletion( provider, "isNumeric" ) );
		provider.addCompletion( new BasicCompletion( provider, "str" ) );
		provider.addCompletion( new BasicCompletion( provider, "lang" ) );
		provider.addCompletion( new BasicCompletion( provider, "datatype" ) );
		provider.addCompletion( new BasicCompletion( provider, "IRI" ) );
		provider.addCompletion( new BasicCompletion( provider, "BNODE" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRDT" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRLANG" ) );
		provider.addCompletion( new BasicCompletion( provider, "UUID" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRUUID" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRLEN" ) );
		provider.addCompletion( new BasicCompletion( provider, "SUBSTR" ) );
		provider.addCompletion( new BasicCompletion( provider, "UCASE" ) );
		provider.addCompletion( new BasicCompletion( provider, "LCASE" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRSTARTS" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRENDS" ) );
		provider.addCompletion( new BasicCompletion( provider, "CONTAINS" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRBEFORE" ) );
		provider.addCompletion( new BasicCompletion( provider, "STRAFTER" ) );
		provider.addCompletion( new BasicCompletion( provider, "ENCODE_FOR_URI" ) );
		provider.addCompletion( new BasicCompletion( provider, "CONCAT" ) );
		provider.addCompletion( new BasicCompletion( provider, "langMatches" ) );
		provider.addCompletion( new BasicCompletion( provider, "REGEX" ) );
		provider.addCompletion( new BasicCompletion( provider, "REPLACE" ) );
		provider.addCompletion( new BasicCompletion( provider, "abs" ) );
		provider.addCompletion( new BasicCompletion( provider, "round" ) );
		provider.addCompletion( new BasicCompletion( provider, "ceil" ) );
		provider.addCompletion( new BasicCompletion( provider, "floor" ) );
		provider.addCompletion( new BasicCompletion( provider, "RAND" ) );
		provider.addCompletion( new BasicCompletion( provider, "now" ) );
		provider.addCompletion( new BasicCompletion( provider, "year" ) );
		provider.addCompletion( new BasicCompletion( provider, "month" ) );
		provider.addCompletion( new BasicCompletion( provider, "day" ) );
		provider.addCompletion( new BasicCompletion( provider, "hours" ) );
		provider.addCompletion( new BasicCompletion( provider, "minutes" ) );
		provider.addCompletion( new BasicCompletion( provider, "seconds" ) );
		provider.addCompletion( new BasicCompletion( provider, "timezone" ) );
		provider.addCompletion( new BasicCompletion( provider, "tz" ) );
		provider.addCompletion( new BasicCompletion( provider, "MD5" ) );
		provider.addCompletion( new BasicCompletion( provider, "SHA1" ) );
		provider.addCompletion( new BasicCompletion( provider, "SHA256" ) );
		provider.addCompletion( new BasicCompletion( provider, "SHA384" ) );
		provider.addCompletion( new BasicCompletion( provider, "SHA512" ) );

		// Add a couple of "shorthand" completions. These completions don't
		// require the input text to be the same thing as the replacement text.
		provider.addCompletion( new ShorthandCompletion( provider, "rdf:type",
				"a", "The \"is a\" relationship" ) );
		provider.addCompletion( new ShorthandCompletion( provider, "rdfs:subPropertyOf",
				"rdfs:subPropertyOf", "An instance of a general property" ) );

		return provider;
	}

	private class QuerySavingAction extends AbstractSavingAction {

		public QuerySavingAction() {
			super( "Save Query" );
			super.setAppendDate( true );
			super.setDefaultFileName( "Query" );
		}

		@Override
		protected void saveTo( File exploc ) throws IOException {
			FileUtils.write(exploc, SparqlTextArea.this.getText() );
		}

		@Override
		protected void finishFileChooser( JFileChooser chsr ) {
			super.finishFileChooser( chsr );
			FileFilter spqFilter
					= new FileNameExtensionFilter( "SPARQL Files (*.spq, *.sparql)",
							"spq", "sparql" );
			chsr.setFileFilter( spqFilter );
			chsr.setAcceptAllFileFilterUsed( true );
		}
	};

}
