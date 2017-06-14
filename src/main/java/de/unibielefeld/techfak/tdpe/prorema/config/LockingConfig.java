package de.unibielefeld.techfak.tdpe.prorema.config;

import de.unibielefeld.techfak.tdpe.prorema.domain.services.EmployeeService;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockInterceptor;
import de.unibielefeld.techfak.tdpe.prorema.locking.SimpleLockService;
import de.unibielefeld.techfak.tdpe.prorema.locking.WorksOnLockInterceptor;
import de.unibielefeld.techfak.tdpe.prorema.locking.WorksOnLockService;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Config for locking.
 *
 * @author Benedikt Volkmer
 *         Created on 5/19/16.
 */
@Configuration
@Log4j2
@NoArgsConstructor
public class LockingConfig extends WebMvcConfigurerAdapter {

    @Autowired private SimpleLockService simpleLockService;
    @Autowired private EmployeeService employeeService;
    @Autowired private WorksOnLockService worksOnLockService;

    @Bean
    public HandlerInterceptor simpleLockInterceptor() {
        return new SimpleLockInterceptor(simpleLockService, employeeService);
    }

    @Bean
    public HandlerInterceptor worksOnLockInterceptor() {
        return new WorksOnLockInterceptor(worksOnLockService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(simpleLockInterceptor());
        registry.addInterceptor(worksOnLockInterceptor());
        super.addInterceptors(registry);
    }

}
