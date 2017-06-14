package de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Matthias on 5/9/16.
 * Interface for different LoginProviders
 */
public interface LoginInfoProvider {
    /**
     * Returns current login.
     * @return the login as Employee object.
     */
    Employee getCurrentLogin();
}
