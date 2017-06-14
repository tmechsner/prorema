package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;

/**
 * Created by Matthias on 5/8/16.
 *
 * Grants to all persons,
 * who have the given position, to execute the given action.
 */
public class AllowPos<T> implements Decider<T> {
    private Action action = null;
    private Employee.Position pos = null;

    /**
     * Grants unlimited access to people with position pos.
     * @param pos the position to grant unlimited access.
     */
    public AllowPos(Employee.Position pos) {
        this.pos = pos;
    }

    public AllowPos(Employee.Position pos, Action action) {
        this.pos = pos;
        this.action = action;
    }

    public AllowPos() {
    }

    /**
     * Decides if the action on the object is allowed,
     * due to the actual decider.
     * @param action action to be performed
     * @param o object affected
     * @return true, if access is granted.
     */
    @Override
    public boolean isAllowed(Action action, T o) {
        if (this.action != null) {
            if (this.action != action) {
                return false;
            }
        }

        return pos == LoginInfo.getPosition();
    }

    @Override
    public void init(String args) {
        int i = args.indexOf(',');
        if (i != -1) {
            action = Action.fromString(args.substring(i+1));
            args = args.substring(0,i);
        }
        pos = Employee.Position.fromString(args);
    }

    @Override
    public boolean equals(Object other) {
        try {
            AllowPos<T> o = (AllowPos<T>) other;
            return o.pos == this.pos && o.action == this.action;
        } catch (ClassCastException ex) {
            return false;
        }
    }
}
