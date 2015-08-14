/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author ryan
 */
@Controller
public class HomeController extends SemossControllerBase {

  private static final Logger log = Logger.getLogger( HomeController.class );

  @RequestMapping( value = "/", method = RequestMethod.GET )
  public String getWelcome() {
    return "index.vm";
  }
}
