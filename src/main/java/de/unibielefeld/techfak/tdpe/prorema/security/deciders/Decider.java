package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;

/**
 * Created by Matthias on 5/7/16.
 *
 * Interface of the Deciders.
 */
public interface Decider<T> {
    /**
     * Decides if the action on the object is allowed,
     * due to the actual decider.
     * @param action action to be performed
     * @param o object affected
     * @return true, if access is granted.
     */
    boolean isAllowed(Action action, T o);

    boolean equals(Object o);

    void init(String args) throws ConfigSyntaxError;

}
