package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.Action;

/**
 * Created by Matthias on 5/21/16.
 *
 * Allows all users to execute the given action on this object.
 */
public class AllowAction<T> implements Decider<T> {
    private Action action;

    public AllowAction() {
        this.action = null;
    }
    public AllowAction(Action action) {
        this.action = action;
    }

    /**
     * Decides if the action on the object is allowed,
     * due to the actual decider.
     * @param action action to be performed
     * @param o object affected
     * @return true, if access is granted.
     */
    @Override
    public boolean isAllowed(Action action, Object o) {
        return action == this.action;
    }

    @Override
    public void init(String args) {
        action = Action.fromString(args);
    }
    @Override
    public boolean equals(Object other) {
        AllowAction<T> o = (AllowAction<T>) other;
        if (o.action == this.action) {
            return true;
        }
        return false;
    }
}
