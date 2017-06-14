package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.Decider;
import sun.reflect.annotation.ExceptionProxy;

import java.util.ArrayList;

/**
 * Created by Matthias on 5/6/16.
 *
 * Decides whether the currently logged in person has, the
 * permission to act on an object.
 *
 * In general, this object has a chain with permission deciders;
 * if one of those accepts the request, permission is granted.
 */
public class AccessDecider<T> {

    private ArrayList<Decider<T>> chain = new ArrayList<>();

    public AccessDecider() {}

    @Override
    public boolean equals(Object o) {
        AccessDecider<T> oa = (AccessDecider<T>) o;
        return chain.equals(oa.chain);
    }
    /**
     * Adds an decider to the chain.
     *
     * @param decider the decider to add.
     */
    public void add(Decider<T> decider) {
        this.chain.add(decider);
    }

    /**
     * Decides if the requested method is allowed.
     * @param action the action to perform
     * @param o influenced object
     * @return true, if access is granted.
     */
    public boolean isAllowed(Action action, T o) {
        try {
            for (Decider<T> d : chain) {
                if (d.isAllowed(action, o)) {
                    return true;
                }
            }
        }
        catch (Exception ex) {}
        return false;
    }

    /**
     * Decides if the requested method is allowed.
     *
     * Decides in the same way as isAllowed does.
     * @param action the action to perform
     * @param o the object influenced
     * @throws PermissionDeniedException thrown when the action is no allowed.
     */
    public void isAllowedThrow(Action action, T o) throws PermissionDeniedException {
        if (!this.isAllowed(action, o)) {
            throw new PermissionDeniedException();
        }
    }
}
