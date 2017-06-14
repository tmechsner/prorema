package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.Action;

/**
 * Created by Matthias on 5/27/16.
 * Allows everything.
 */
public class AllowAll<T> implements Decider<T> {
    @Override
    public boolean isAllowed(Action action, Object o) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return true;
    }

    @Override
    public void init(String args) {

    }
}
