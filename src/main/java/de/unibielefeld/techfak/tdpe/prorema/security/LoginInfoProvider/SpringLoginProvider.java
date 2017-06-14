package de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by Matthias on 5/9/16.
 * LoginProvider for the Spring-Interface.
 */
public class SpringLoginProvider implements LoginInfoProvider {
    /**
     * Returns current login.
     * @return the login as Employee object.
     */
    @Override
    public Employee getCurrentLogin() {
        try {
            SecurityContext sc = SecurityContextHolder.getContext();
            Authentication authentication = sc.getAuthentication();
            return (Employee) authentication.getPrincipal();
        } catch (ClassCastException| NullPointerException ex) {
            return null;
        }
    }
}
