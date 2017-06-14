package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.LoginInfoProvider;
import de.unibielefeld.techfak.tdpe.prorema.security.ReadSecurityConfig;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;

/**
 * Created by Matthias on 6/20/16.
 */
public class AllowPrio<T> implements Decider<T> {
    private int pri = 0;
    private Action action = null;

    public AllowPrio() {}
    public AllowPrio(Action action, int pri) {
        this.action = action;
        this.pri = pri;
    }

    @Override
    public boolean isAllowed(Action action, T o) {
        Employee.Position position = LoginInfo.getPosition();
        return  this.action == action && (position.getPriority()) >= (this.pri);
    }

    @Override
    public void init(String args) throws ConfigSyntaxError {
            Tuple<String, Action> t = ReadSecurityConfig.readAction(args);
            action = t.getRight();
            args = t.getLeft();
            Tuple<String, Integer> s = ReadSecurityConfig.readInteger(args);
            pri = s.getRight();
    }

    @Override
    public boolean equals(Object other) {
        AllowPrio<T> o = (AllowPrio<T>) other;

        return (this.pri == o.pri) && (this.action == o.action);
    }
}
