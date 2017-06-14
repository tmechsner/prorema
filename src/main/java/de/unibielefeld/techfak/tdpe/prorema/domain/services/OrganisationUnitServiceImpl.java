package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewOrganisationUnitCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.OrganisationUnit;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.EmployeeRepository;
import de.unibielefeld.techfak.tdpe.prorema.persistence.repository.OrganisationUnitRepository;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Standard implementation of the OrganisationUnitService.
 */
@Service
@Primary
public class OrganisationUnitServiceImpl
        extends AbstactServiceImpl<OrganisationUnit, OrganisationUnitRepository, OrganisationUnitEntity>
        implements OrganisationUnitService {

    private static final Integer AMDIN_OU_ID = 4000;

    private final EmployeeRepository employeeRepository;

    @Autowired
    public OrganisationUnitServiceImpl(OrganisationUnitRepository repository,
                                       EmployeeRepository employeeRepository) {
        super(repository);
        this.employeeRepository = employeeRepository;
        accessDecider = AccessDeciderPool.organisationUnit;
    }

    @Override
    protected OrganisationUnit init(OrganisationUnitEntity entity) {
        return new OrganisationUnit(entity);
    }

    @Override
    public OrganisationUnit createOrganisationUnit(NewOrganisationUnitCmd command) {
        if (command != null) {
            OrganisationUnitEntity organisationUnitEntity;
            EmployeeEntity firstManager = employeeRepository.findOne(Integer.valueOf(command.getFirstManagerId()));
            EmployeeEntity secondManager = employeeRepository.findOne(Integer.valueOf(command.getSecondManagerId()));
            if (command.getId() != null && !command.getId().isEmpty()
                && repository.exists(Integer.valueOf(command.getId()))) {
                accessDecider.isAllowedThrow(Action.MODIFY, null);
                organisationUnitEntity = repository.findOne(Integer.valueOf(command.getId()));
                organisationUnitEntity.setName(command.getName());
                organisationUnitEntity.setDescription(command.getDescription());
                organisationUnitEntity
                        .setFirstManager(firstManager);
                organisationUnitEntity
                        .setSecondManager(secondManager);
            } else {
                accessDecider.isAllowedThrow(Action.CREATE, null);
                organisationUnitEntity = new OrganisationUnitEntity(command.getName(), command.getDescription());
                organisationUnitEntity
                        .setFirstManager(firstManager);
                organisationUnitEntity
                        .setSecondManager(secondManager);
            }

            OrganisationUnitEntity saved = repository.save(organisationUnitEntity);
            return new OrganisationUnit(saved);
        }
        return null;
    }

    @Override
    public List<OrganisationUnit> getAllExceptAdminUnit() {
        return this.getAll().stream()
                   .filter(organisationUnit -> !organisationUnit.getId()
                                                                .equals(OrganisationUnitServiceImpl.AMDIN_OU_ID))
                   .collect(Collectors.toList());
    }
}

