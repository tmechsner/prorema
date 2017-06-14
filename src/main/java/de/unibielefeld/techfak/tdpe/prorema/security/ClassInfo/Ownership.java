package de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;

/**
 * Created by Matthias on 5/7/16.
 *
 * Provides ownership information.
 */
public interface Ownership<T> {
    /**
     * Returns, if the object is owned by the employee with the id e.
     * @param e the id of the employee to check.
     * @param obj the object to check.
     * @return true if e is the owner of obj.
     */
    boolean isOwner(Integer e, T obj);


}
