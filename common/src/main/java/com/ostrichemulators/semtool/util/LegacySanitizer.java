/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

/**
 * A Sanitizer that creates URIs the same way the legacy code code. The
 * resulting URIs <strong>COULD BE</strong> invalid
 *
 * @author ryan
 */
public class LegacySanitizer implements UriSanitizer {

  @Override
  public String sanitize( String raw ) {
    String trimmed = raw.trim();
    if ( trimmed.isEmpty() ) {
      return trimmed;
    }

    // replace all whitespace with underscores
    trimmed = trimmed.replaceAll( "[\\s]+", "_" );

    trimmed = trimmed.replaceAll( "\\{", "(" );
    trimmed = trimmed.replaceAll( "\\}", ")" );
    trimmed = trimmed.replaceAll( "\\\\", "-" );//replace backslashes with dashes
    trimmed = trimmed.replaceAll( "'", "" );//remove apostrophe
    trimmed = trimmed.replaceAll( "\"", "'" );//replace double quotes with single quotes
    trimmed = trimmed.replaceAll( "/", "-" );//replace forward slashes with dashes

    trimmed = trimmed.replaceAll( "\\|", "-" );//replace vertical lines with dashes
    trimmed = trimmed.replaceAll( "<", "(" );
    trimmed = trimmed.replaceAll( ">", ")" );

    trimmed = trimmed.replaceAll( ":", "_x_" ); // can't have colons

    return trimmed;
  }
}
