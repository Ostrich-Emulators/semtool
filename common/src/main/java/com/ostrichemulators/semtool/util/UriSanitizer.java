/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

/**
 * An interface used by {@link UriBuilder} to sanitize URI components
 *
 * @author ryan
 */
public interface UriSanitizer {

  /**
   * Sanitizes the given string so that it can be part of a URI
   * @param raw the raw string
   * @return a sanitized version of the raw string
   */
  public String sanitize( String raw );  
}
