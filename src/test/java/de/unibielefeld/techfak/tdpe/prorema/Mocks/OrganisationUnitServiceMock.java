package de.unibielefeld.techfak.tdpe.prorema.Mocks;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.domain.services.OrganisationUnitService;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Matthias on 5/21/16.
 */
public class OrganisationUnitServiceMock implements OrganisationUnitService {

    OrganisationUnit organisationUnit;
    Integer id;
    public OrganisationUnitServiceMock(OrganisationUnit organisationUnit, Integer id) {
        this.organisationUnit = organisationUnit;
        this.id = id;
    }

    public OrganisationUnitServiceMock() {
    }

    @Override
    public List<OrganisationUnit> getAll() {
        return null;
    }

    @Override
    public List<OrganisationUnitEntity> getAllEntities() {
        return null;
    }

    @Override
    public List<OrganisationUnit> findAll(Iterable<Integer> ids) {
        return null;
    }

    @Override
    public OrganisationUnit findOne(Integer id) {
        return organisationUnit;
    }

    @Override
    public List<OrganisationUnit> getFiltered(Predicate<OrganisationUnit> filter) {
        return null;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

    @Override
    public OrganisationUnit createOrganisationUnit(NewOrganisationUnitCmd command) {
        return null;
    }

    @Override
    public List<OrganisationUnit> getAllExceptAdminUnit() { return null; }
}
