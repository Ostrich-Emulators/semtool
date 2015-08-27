package gov.va.semoss.web.ui;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class SEMOSSUIFactory {
	
	private static SEMOSSUIFactory instance = null;

	private static VelocityEngine engine;
	
	private static final Logger log = Logger.getLogger(SEMOSSUIFactory.class);

	private SEMOSSUIFactory() {
		//SEMOSSUIFactory.engine = ViewConfiguration.VELOCITY_ENGINE;
	}

	public static SEMOSSUIFactory instance() {
		if (instance == null) {
			instance = new SEMOSSUIFactory();
		}
		return instance;
	}
	
	public String getUI(String templatePath, HashMap<String, Object> valueMap){
		Template template = null;
		
		VelocityContext context = new VelocityContext();
		Iterator<String> keys = valueMap.keySet().iterator();
		while (keys.hasNext()){
			String key = keys.next();
			Object value = valueMap.get(key);
			context.put(key, value);
		}
		try {
			template = engine.getTemplate(templatePath);
		} catch (ResourceNotFoundException rnfe) {
			log.error("Resource not found in UI Factory: " + templatePath);
			log.error(rnfe);
		} catch (ParseErrorException pee) {
			log.error("There was a problem producing the User Interface: Parsing Error");
			log.error(pee);
		} catch (MethodInvocationException mie) {
			log.error("There was a problem producing the User Interface: Method Invocation Exception");
			log.error(mie);
		} catch (Exception e) {
			log.error("There was a problem producing the User Interface: General Exception");
			log.error(e);
		}
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		return sw.toString();
	}
}