package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.domain.EmployeeProfile;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeProfileEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;


import java.util.List;

/**
 * Created by Alex Schneider.
 */
public interface EmployeeProfileService extends AbstactService<EmployeeProfile , EmployeeProfileEntity> {
    /**
     * Gets all profiles related to one employee by id.
     *
     * @param employeeId the employee a profile belongs to.
     * @return Relation as list of profiles to given employee.
     */
    List<EmployeeProfile> findProfileByEmployee(int employeeId);

    /**
     * creates an EmployeeProfile from user input.
     * creates a database entry.
     *
     * @param employee employee to whom the profile belongs
     * @param url      url to the profile
     * @return The created EmployeeProfile.
     */
    EmployeeProfile createProfile(EmployeeEntity employee, String url);
}
