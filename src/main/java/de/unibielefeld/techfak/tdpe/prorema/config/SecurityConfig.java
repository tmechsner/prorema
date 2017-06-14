package de.unibielefeld.techfak.tdpe.prorema.config;

import de.unibielefeld.techfak.tdpe.prorema.security.ModelEnhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Matthias on 07.06.16.
 *
 * Adds an Interceptor so that several basic
 * security information are available from every page
 */
@Configuration
public class SecurityConfig extends WebMvcConfigurerAdapter {

    @Bean
    public HandlerInterceptor modelEnhancer() {
        return new ModelEnhancer();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(modelEnhancer());
        super.addInterceptors(registry);
    }

}
