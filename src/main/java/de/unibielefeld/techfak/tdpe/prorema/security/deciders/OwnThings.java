package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.EmployeeInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.ProjectInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.Ownership;

/**
 * Created by Matthias on 5/7/16.
 *
 * Grants unlimited access to all objects which are owned by the
 * currently logged in user.
 */
public class OwnThings<T> implements Decider<T> {

    private Ownership<T> ownerInfo = null;

    public OwnThings(Ownership<T> ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    public OwnThings(){}
    /**
     * Decides if the action on the object is allowed,
     * due to the actual decider.
     * @param action action to be performed
     * @param o object affected
     * @return true, if access is granted.
     */
    @Override
    public boolean isAllowed(Action action, T o) {
        return ownerInfo.isOwner(LoginInfo.getCurrentLogin().getId(), o);
    }

    @Override
    public void init(String args) {
        if (args.equals("project")) {
            this.ownerInfo = (Ownership<T>) new ProjectInfo();
            return;
        }
        if (args.equals("employee")) {
            this.ownerInfo = (Ownership<T>) new EmployeeInfo();
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean equals(Object o) {
        try {
            OwnThings a = (OwnThings) o;
            return ownerInfo == a.ownerInfo || (this.ownerInfo.equals(a.ownerInfo));
        } catch (ClassCastException|NullPointerException ex) {
            return false;
        }
    }
}
