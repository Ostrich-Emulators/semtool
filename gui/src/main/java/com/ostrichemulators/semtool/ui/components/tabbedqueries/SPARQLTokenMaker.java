/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.tabbedqueries;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class SPARQLTokenMaker extends AbstractTokenMaker {

	private int currentTokenStart;
	private int currentTokenType;
	protected final String separators = "{}()[]";

	@Override
	public TokenMap getWordsToHighlight() {
	   //The parameter passed into the "TokenMap(...)" constructor
		//indicates whether to perform case insensitive searches:
		TokenMap tokenMap = new TokenMap( true );

		tokenMap.put( "SELECT", Token.RESERVED_WORD );
		tokenMap.put( "CONSTRUCT", Token.RESERVED_WORD );
		tokenMap.put( "ASK", Token.RESERVED_WORD );
		tokenMap.put( "DESCRIBE", Token.RESERVED_WORD );
		tokenMap.put( "INSERT", Token.RESERVED_WORD );
		tokenMap.put( "DELETE", Token.RESERVED_WORD );
		tokenMap.put( "DATA", Token.RESERVED_WORD );
		tokenMap.put( "FROM", Token.RESERVED_WORD );
		tokenMap.put( "NAMED", Token.RESERVED_WORD );
		tokenMap.put( "GRAPH", Token.RESERVED_WORD );
		tokenMap.put( "OPTIONAL", Token.RESERVED_WORD );
		tokenMap.put( "UNION", Token.RESERVED_WORD );
		tokenMap.put( "WITH", Token.RESERVED_WORD );
		tokenMap.put( "INCLUDE", Token.RESERVED_WORD );
		tokenMap.put( "WHERE", Token.RESERVED_WORD );
		tokenMap.put( "DISTINCT", Token.RESERVED_WORD );
		tokenMap.put( "REDUCED", Token.RESERVED_WORD );
		tokenMap.put( "ORDER", Token.RESERVED_WORD );
		tokenMap.put( "BY", Token.RESERVED_WORD );
		tokenMap.put( "GROUP", Token.RESERVED_WORD );
		tokenMap.put( "HAVING", Token.RESERVED_WORD );
		tokenMap.put( "OFFSET", Token.RESERVED_WORD );
		tokenMap.put( "LIMIT", Token.RESERVED_WORD );
		tokenMap.put( "BIND", Token.RESERVED_WORD );
		tokenMap.put( "AS", Token.RESERVED_WORD );
		tokenMap.put( "VALUES", Token.RESERVED_WORD );
		tokenMap.put( "FILTER", Token.RESERVED_WORD );

		tokenMap.put( "COUNT", Token.FUNCTION );
		tokenMap.put( "SUM", Token.FUNCTION );
		tokenMap.put( "MIN", Token.FUNCTION );
		tokenMap.put( "MAX", Token.FUNCTION );
		tokenMap.put( "AVG", Token.FUNCTION );
		tokenMap.put( "GROUP_CONCAT", Token.FUNCTION );
		tokenMap.put( "SAMPLE", Token.FUNCTION );
		tokenMap.put( "IF", Token.FUNCTION );
		tokenMap.put( "COALESCE", Token.FUNCTION );
		tokenMap.put( "EXISTS", Token.FUNCTION );
		tokenMap.put( "NOT", Token.FUNCTION );
		tokenMap.put( "||", Token.FUNCTION );
		tokenMap.put( "&&", Token.FUNCTION );
		tokenMap.put( "=", Token.FUNCTION );
		tokenMap.put( "sameTerm", Token.FUNCTION );
		tokenMap.put( "IN", Token.FUNCTION );
		tokenMap.put( "isIRI", Token.FUNCTION );
		tokenMap.put( "isBlank", Token.FUNCTION );
		tokenMap.put( "isLiteral", Token.FUNCTION );
		tokenMap.put( "isNumeric", Token.FUNCTION );
		tokenMap.put( "str", Token.FUNCTION );
		tokenMap.put( "lang", Token.FUNCTION );
		tokenMap.put( "datatype", Token.FUNCTION );
		tokenMap.put( "IRI", Token.FUNCTION );
		tokenMap.put( "BNODE", Token.FUNCTION );
		tokenMap.put( "STRDT", Token.FUNCTION );
		tokenMap.put( "STRLANG", Token.FUNCTION );
		tokenMap.put( "UUID", Token.FUNCTION );
		tokenMap.put( "STRUUID", Token.FUNCTION );
		tokenMap.put( "STRLEN", Token.FUNCTION );
		tokenMap.put( "SUBSTR", Token.FUNCTION );
		tokenMap.put( "UCASE", Token.FUNCTION );
		tokenMap.put( "LCASE", Token.FUNCTION );
		tokenMap.put( "STRSTARTS", Token.FUNCTION );
		tokenMap.put( "STRENDS", Token.FUNCTION );
		tokenMap.put( "CONTAINS", Token.FUNCTION );
		tokenMap.put( "STRBEFORE", Token.FUNCTION );
		tokenMap.put( "STRAFTER", Token.FUNCTION );
		tokenMap.put( "ENCODE_FOR_URI", Token.FUNCTION );
		tokenMap.put( "CONCAT", Token.FUNCTION );
		tokenMap.put( "langMatches", Token.FUNCTION );
		tokenMap.put( "REGEX", Token.FUNCTION );
		tokenMap.put( "REPLACE", Token.FUNCTION );
		tokenMap.put( "abs", Token.FUNCTION );
		tokenMap.put( "round", Token.FUNCTION );
		tokenMap.put( "ceil", Token.FUNCTION );
		tokenMap.put( "floor", Token.FUNCTION );
		tokenMap.put( "RAND", Token.FUNCTION );
		tokenMap.put( "now", Token.FUNCTION );
		tokenMap.put( "year", Token.FUNCTION );
		tokenMap.put( "month", Token.FUNCTION );
		tokenMap.put( "day", Token.FUNCTION );
		tokenMap.put( "hours", Token.FUNCTION );
		tokenMap.put( "minutes", Token.FUNCTION );
		tokenMap.put( "seconds", Token.FUNCTION );
		tokenMap.put( "timezone", Token.FUNCTION );
		tokenMap.put( "tz", Token.FUNCTION );
		tokenMap.put( "MD5", Token.FUNCTION );
		tokenMap.put( "SHA1", Token.FUNCTION );
		tokenMap.put( "SHA256", Token.FUNCTION );
		tokenMap.put( "SHA384", Token.FUNCTION );
		tokenMap.put( "SHA512", Token.FUNCTION );

		return tokenMap;
	}

	@Override
	public void addToken( Segment segment, int start, int end, int tokenType, int startOffset ) {
		// This assumes all keywords, etc. were parsed as "identifiers."
		if ( tokenType == Token.IDENTIFIER ) {
			int value = wordsToHighlight.get( segment, start, end );
			if ( value != -1 ) {
				tokenType = value;
			}
		}
		super.addToken( segment, start, end, tokenType, startOffset );
	}

	@Override
	public Token getTokenList( Segment text, int startTokenType, int startOffset ) {

		resetTokenList();

		char[] array = text.array;
		int offset = text.offset;
		int count = text.count;
		int end = offset + count;

		   // Token starting offsets are always of the form:
		// 'startOffset + (currentTokenStart-offset)', but since startOffset and
		// offset are constant, tokens' starting positions become:
		// 'newStartOffset+currentTokenStart'.
		int newStartOffset = startOffset - offset;

		currentTokenStart = offset;
		currentTokenType = startTokenType;

		for ( int i = offset; i < end; i++ ) {

			char c = array[i];

			switch ( currentTokenType ) {

				case Token.NULL:

					currentTokenStart = i;   // Starting a new token here.

					switch ( c ) {
						case ' ':
						case '\t':
						case '\n':
							currentTokenType = Token.WHITESPACE;
							break;

						case '"':
							currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
							break;

						case '<':
						case '>':
							currentTokenType = Token.MARKUP_TAG_DELIMITER;
							break;

						case '?':
						case '$':
						case '%':
							currentTokenType = Token.VARIABLE;
							break;
						case '#':
							currentTokenType = Token.COMMENT_EOL;
							break;

						default:
							if ( RSyntaxUtilities.isDigit( c ) ) {
								currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
								break;
							}
							else if ( RSyntaxUtilities.isLetter( c ) || c == '/' || c == '_' ) {
								currentTokenType = Token.IDENTIFIER;
								break;
							}
							else if ( separators.indexOf( c, 0 ) > -1 ) {
								currentTokenType = Token.SEPARATOR;
								break;
							}
							// Anything not currently handled - mark as an identifier
							currentTokenType = Token.IDENTIFIER;
							break;

					} // End of switch (c).

					break;

				case Token.WHITESPACE:

					switch ( c ) {

						case ' ':
						case '\t':
						case '\n':
							break;   // Still whitespace.

						case '"':
							addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
							break;

						case '<':
						case '>':
							addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.MARKUP_TAG_DELIMITER;
							break;

						case '?':
						case '$':
						case '%':
							addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.VARIABLE;
							break;

						case '#':
							addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.COMMENT_EOL;
							break;

						default:   // Add the whitespace token and start anew.

							addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
							currentTokenStart = i;

							if ( RSyntaxUtilities.isDigit( c ) ) {
								currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
								break;
							}
							else if ( RSyntaxUtilities.isLetter( c ) || c == '/' || c == '_' ) {
								currentTokenType = Token.IDENTIFIER;
								break;
							}
							else if ( separators.indexOf( c, 0 ) > -1 ) {
								currentTokenType = Token.SEPARATOR;
								break;
							}

							// Anything not currently handled - mark as identifier
							currentTokenType = Token.IDENTIFIER;

					} // End of switch (c).

					break;

				default: // Should never happen
				case Token.IDENTIFIER:

					switch ( c ) {

						case ' ':
						case '\t':
						case '\n':
							addToken( text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.WHITESPACE;
							break;

						case '"':
							addToken( text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
							break;

						case '<':
						case '>':
							addToken( text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.MARKUP_TAG_DELIMITER;
							break;

						case '?':
						case '$':
						case '%':
							addToken( text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.VARIABLE;
							break;

						default:
							if ( RSyntaxUtilities.isLetterOrDigit( c ) || c == '/' || c == '_' ) {
								break;   // Still an identifier of some type.
							}
							else if ( separators.indexOf( c, 0 ) > -1 ) {
								addToken( text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart );
								currentTokenStart = i;
								currentTokenType = Token.SEPARATOR;
								break;
							}
						// Otherwise, we're still an identifier (?).

					} // End of switch (c).

					break;

				case Token.LITERAL_NUMBER_DECIMAL_INT:

					switch ( c ) {

						case ' ':
						case '\t':
							addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.WHITESPACE;
							break;

						case '"':
							addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
							break;

						case '<':
						case '>':
							addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.MARKUP_TAG_DELIMITER;
							break;

						case '?':
						case '$':
						case '%':
							addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.VARIABLE;
							break;

						default:

							if ( RSyntaxUtilities.isDigit( c ) ) {
								break;   // Still a literal number.
							}
							else if ( separators.indexOf( c, 0 ) > -1 ) {
								addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
								currentTokenStart = i;
								currentTokenType = Token.SEPARATOR;
								break;
							}

							// Otherwise, remember this was a number and start over.
							addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
							i--;
							currentTokenType = Token.NULL;

					} // End of switch (c).

					break;

				case Token.COMMENT_EOL:
					i = end - 1;
					addToken( text, currentTokenStart, i, currentTokenType, newStartOffset + currentTokenStart );
					// We need to set token type to null so at the bottom we don't add one more token.
					currentTokenType = Token.NULL;
					break;

				case Token.LITERAL_STRING_DOUBLE_QUOTE:
					if ( c == '"' ) {
						addToken( text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart );
						currentTokenType = Token.NULL;
					}
					break;

				case Token.MARKUP_TAG_DELIMITER:
					if ( c == '>' ) {
						addToken( text, currentTokenStart, i, Token.MARKUP_TAG_DELIMITER, newStartOffset + currentTokenStart );
						currentTokenType = Token.NULL;
					}
					break;

				case Token.VARIABLE:
					if ( c == ' ' || c == '\t' || c == '\n' ) {
						addToken( text, currentTokenStart, i, Token.VARIABLE, newStartOffset + currentTokenStart );
						currentTokenType = Token.NULL;
					}
					if ( separators.indexOf( c, 0 ) > -1 ) {
						addToken( text, currentTokenStart, i - 1, Token.VARIABLE, newStartOffset + currentTokenStart );
						currentTokenStart = i;
						currentTokenType = Token.SEPARATOR;
					}
					break;
				case Token.SEPARATOR:
					switch ( c ) {
						case ' ':
						case '\t':
						case '\n':
							addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.WHITESPACE;
							break;

						case '"':
							addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
							break;

						case '<':
						case '>':
							addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.MARKUP_TAG_DELIMITER;
							break;

						case '?':
						case '$':
						case '%':
							addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.VARIABLE;
							break;

						case Token.IDENTIFIER:
							addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
							currentTokenStart = i;
							currentTokenType = Token.IDENTIFIER;
							break;

						default:
							if ( RSyntaxUtilities.isLetterOrDigit( c ) || c == '/' || c == '_' ) {
								addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
								currentTokenStart = i;
								currentTokenType = Token.IDENTIFIER;
								break;
							}
							if ( separators.indexOf( c, 0 ) > -1 ) {
								addToken( text, currentTokenStart, i - 1, Token.SEPARATOR, newStartOffset + currentTokenStart );
								currentTokenStart = i;
								currentTokenType = Token.SEPARATOR;
								break;
							}
						// Otherwise, we're still a separator (?).
					} // End of switch (c).
					break;
			} // End of switch (currentTokenType).

		} // End of for (int i=offset; i<end; i++).

		switch ( currentTokenType ) {

			// Remember what token type to begin the next line with.
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
			case Token.MARKUP_TAG_DELIMITER:
				addToken( text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart );
				break;

			// Do nothing if everything was okay.
			case Token.NULL:
				addNullToken();
				break;

			// All other token types don't continue to the next line...
			default:
				addToken( text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart );
				addNullToken();

		}

		// Return the first token in our linked list.
		return firstToken;

	}
}
