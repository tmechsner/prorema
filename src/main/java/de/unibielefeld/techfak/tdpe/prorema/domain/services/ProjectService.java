package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewProjectCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Project;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;

import java.util.List;

/**
 * Service interface for Project
 * Created by Timo Mechsner on 4/18/16.
 */
public interface ProjectService extends AbstactService<Project , ProjectEntity> {

    /**
     * Create a project from user input.
     * A database entry will be created.
     *
     * @param command Command with user input
     * @return The newly created Project.
     */
    Project create(NewProjectCmd command) throws PermissionDeniedException;

    List<Project> getByOrganisationUnit(Integer id);

    List<Project> getByRunning(Boolean running);

    List<Project> getByEndDateIsPast();

    Project convert(Project project);

}
