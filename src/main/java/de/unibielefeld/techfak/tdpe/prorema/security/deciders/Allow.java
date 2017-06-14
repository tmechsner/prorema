package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.ReadSecurityConfig;
import de.unibielefeld.techfak.tdpe.prorema.utils.Tuple;

/**
 * Allows every person which has a higher position than given,
 * to execute every action with low priority than given.
 */
public class Allow<T> implements Decider<T> {
    private int act;
    private int pos;

    public Allow(Employee.Position pos, Action act) {
        this.pos = pos.getPriority();
        this.act = act.getPriority();
    }

    public Allow(int pos, int act) {
        this.pos = pos;
        this.act = act;
    }

    public Allow() {
        pos = -1;
        act = -1;
    }

    @Override
    public boolean isAllowed(Action action, Object o) {
        return action.getPriority()<=act && LoginInfo.getPosition().getPriority() >= pos;
    }

    @Override
    public void init(String args) throws ConfigSyntaxError {
        Tuple<String, Employee.Position> t = ReadSecurityConfig.readPosition(args);
        pos = t.getRight().getPriority();
        args = t.getLeft();

        Tuple<String, Action> s = ReadSecurityConfig.readAction(args);
        act = s.getRight().getPriority();
    }

    @Override
    public boolean equals(Object o) {
        Allow a = (Allow) o;
        return this.act == a.act && this.pos == a.pos;
    }
}
