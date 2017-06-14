package de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;

/**
 * Created by Matthias on 5/21/16.
 *
 * Provides ownership information for Employees.
 * An employee is owned by himself.
 */
public class EmployeeInfo implements Ownership<EmployeeEntity> {
    /**
     * Returns, if the object is owned by the employee with the id e.
     * @param e the id of the employee to check.
     * @param e the object to check.
     * @return true if e is the owner of obj.
     */
    @Override
    public  boolean isOwner(Integer id, EmployeeEntity e) {
        try {
            return id.equals(e.getId());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Compares two objects
     * @param o the other object.
     * @return true if both objects are from the type of this class.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        EmployeeInfo a = (EmployeeInfo) o;
        return true;
    }
}
