package gov.va.semoss.web.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.velocity.VelocityView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

/**
 * Sets Apache Velocity as the View Resolver
 * @author Wayne Warren
 *
 */
@EnableWebMvc
@Configuration
@ComponentScan({ "gov.va.semoss.web.*" })
@Import({ AppSecurityConfig.class })
public class ViewConfig {

	@Bean
	public VelocityViewResolver viewResolver() {
		VelocityViewResolver viewResolver 
                          = new VelocityViewResolver();
		viewResolver.setViewClass(VelocityView.class);
		return viewResolver;
	}

	
}
