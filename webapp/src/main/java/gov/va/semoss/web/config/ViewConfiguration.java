package gov.va.semoss.web.config;



import javax.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

	@EnableWebMvc
	@Configuration
	@ComponentScan({ "gov.va.semoss.web.*" })
	@Import({ AppSecurityConfig.class })
	public class ViewConfiguration implements ServletContextAware {
		
		/** The Velocity Engine used to render the requested views, accessible to
		 * the rest of the system */
		public static VelocityEngine VELOCITY_ENGINE = null; 

		@Bean
		public VelocityConfigurer velocityConfig() {
			VelocityConfigurer config = new VelocityConfigurer();
			config.setVelocityEngine(VELOCITY_ENGINE);
			return config;
		}

		/**
		 * Sets the Servlet Context on initialized of the Spring "dispatch" Servlet
		 */
		@Override
		public void setServletContext(ServletContext theContext) {
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty("resource.loader", "webapp");
			engine.setProperty("webapp.resource.loader.class",
					"org.apache.velocity.tools.view.servlet.WebappLoader");
			engine.setProperty("webapp.resource.loader.path", "/WEB-INF/views");
			engine.setApplicationAttribute("javax.servlet.ServletContext",
					theContext);
			VELOCITY_ENGINE = engine;
		}	
	}