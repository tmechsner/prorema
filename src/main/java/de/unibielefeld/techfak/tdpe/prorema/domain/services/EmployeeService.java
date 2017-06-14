package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;

import java.util.List;

/**
 * Service interface to Employee
 * Created by Benedikt Volkmer on 4/18/16.
 */
public interface EmployeeService extends AbstactService<Employee , EmployeeEntity> {


    /**
     * Create Employee from user input/command.
     * <p>
     * It will create a database entry.
     * </p>
     *
     * @param command Command with user input
     * @return Employee instance
     */
    Employee create(NewEmployeeCmd command) throws PermissionDeniedException;

    List<Employee> getByOrganisationUnit(Integer id);

    /**
     * Returns all Employees that aren't administrators
     * @return List of employees.
     */
    List<Employee> getAllExceptAdmins();

    /**
     * Returns all Employees that are no administrators and that are still employed.
     * @return
     */
    List<Employee> getAllNonExitedNonAdmin();

    /**
     * Returns all Employees that are still employed.
     * @return
     */
    List<Employee> getAllNonExited();

    /**
     * Get all employees with the specified position.
     *
     * @param position Position of the wanted employee(s)
     * @return List of employees
     */
    List<Employee> getByPosition(String position);

    List<Employee> getAllFormerEmployees();

}
