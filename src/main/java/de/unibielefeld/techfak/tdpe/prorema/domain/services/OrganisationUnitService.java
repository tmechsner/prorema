package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;

import java.util.List;

/**
 * Service handling OrganisationUnit Domains.
 */
public interface OrganisationUnitService extends AbstactService<OrganisationUnit , OrganisationUnitEntity> {
    /**
     * creates an organisationUnit from user input.
     * creates a database entry.
     *
     * @param command Command with user input
     * @return The created organisationUnit.
     */
    OrganisationUnit createOrganisationUnit(NewOrganisationUnitCmd command);

    /**
     * Returns all organisationUnits that aren't administrators.
     *
     * @return List of employees.
     */
    List<OrganisationUnit> getAllExceptAdminUnit();
}
