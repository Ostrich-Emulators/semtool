package gov.va.semoss.web.init;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ContextLoaderListener;


public class ContextBean extends ContextLoaderListener {

	private static ContextBean instance;
	
	public static  ContextBean instance(){
		if (instance == null){
			instance = new ContextBean();
		}
		return instance;
	}
	
	public ApplicationContext getAppContext() {
		return getCurrentWebApplicationContext();
	}


	
	
}
