/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author ryan
 */
public class SemossControllerBase {

  @ExceptionHandler( IllegalArgumentException.class )
  @ResponseStatus( value = HttpStatus.NOT_FOUND )
  public String handleMissingIdError( IllegalArgumentException ex,
      HttpServletRequest req ) {
    req.setAttribute( "message", ex.getMessage() );
    return "exception";
  }

  @ExceptionHandler( ValidationException.class )
  @ResponseStatus( value = HttpStatus.EXPECTATION_FAILED )
  public String handleValidationError( ValidationException ex,
      HttpServletRequest req ) {
    req.setAttribute( "message", ex.getMessage() );
    return "exception";
  }

  @ExceptionHandler( ConstraintViolationException.class )
  @ResponseStatus( value = HttpStatus.EXPECTATION_FAILED )
  public String handleValidationError( ConstraintViolationException ex,
      HttpServletRequest req ) {
    req.setAttribute( "message", ex.getMessage() );
    return "exception";
  }
}
