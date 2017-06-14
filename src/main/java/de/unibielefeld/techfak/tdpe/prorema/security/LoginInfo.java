package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.LoginInfoProvider;

/**
 * Created by Matthias on 5/6/16.
 */
public class LoginInfo {
    private static LoginInfoProvider provider;

    /**
     * Returns the currently logged in user.
     * @return an Employee object representing the logged in user.
     */
    public static Employee getCurrentLogin() {
        return provider.getCurrentLogin();
    }

    /**
     * Returns the position of the logged in user
     * @return position stored in Employee.Position
     */
    public static Employee.Position getPosition() {
        Employee pos = getCurrentLogin();
        if (pos != null)
            return pos.getPosition();
        return null;
    }

    /**
     * Sets the currently active loginProvider.
     *
     * Use with care!
     * @param loginProvider the loginProvider
     */
    public static void setLoginProvider(LoginInfoProvider loginProvider) {
        LoginInfo.provider = loginProvider;
    }

}
