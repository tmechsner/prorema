package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewWorksOnCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.WorksOn;
import de.unibielefeld.techfak.tdpe.prorema.persistence.WorksOnEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by David on 12.05.2016.
 */
public interface WorksOnService extends AbstactService<WorksOn , WorksOnEntity> {

    /**
     * Finds all WorksOns, that are related to the given Employee.
     *
     * @param employeeId Employee the wanted WorksOns belong to.
     * @return List of WorksOns related to the given Employee.
     */
    List<WorksOn> getWorksOnByEmployee(int employeeId);

    /**
     * Finds all WorksOns, that are related to the given Project.
     *
     * @param projectId Project the wanted WorksOns belong to
     * @return List of WorksOns related to the given Project.
     */
    List<WorksOn> getWorksOnByProject(int projectId);

    /**
     * Saves a new entry for WorksOn.
     *
     * @param command data for the entry
     * @return the domain object of the saved entry
     */
    List<WorksOn> createWorksOn(NewWorksOnCmd command);

    List<WorksOn> getByDateAndEmployee(LocalDate start, LocalDate end, int emId);

    List<WorksOn> getByStatus(String status);

}
