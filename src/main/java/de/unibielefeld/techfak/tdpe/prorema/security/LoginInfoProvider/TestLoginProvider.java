package de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;

/**
 * Created by Matthias on 5/9/16.
 * For testing purposes, login can be set.
 */
public class TestLoginProvider implements LoginInfoProvider {
    /**
     * login in use.
     */
    private Employee currentLogin;
    /**
     * Constructs a new TestLoginProvider object.
     * @param currentLogin the login to use
     */
    public TestLoginProvider(Employee currentLogin) {
        this.currentLogin = currentLogin;
    }

    /**
     * Returns current login.
     * @return the login as Employee object.
     */
    @Override
    public Employee getCurrentLogin() {
        return this.currentLogin;
    }

}
