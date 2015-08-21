package gov.va.semoss.web.init;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware {

	   private static ApplicationContext applicationContext = null;

	    public static ApplicationContext getApplicationContext() {
	        return applicationContext;
	    }
	    
	    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	         this.applicationContext = applicationContext;
	    }
	    public void init(){

	    }
	}
