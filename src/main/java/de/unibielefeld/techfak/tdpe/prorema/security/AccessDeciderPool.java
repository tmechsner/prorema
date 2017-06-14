package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.*;
import de.unibielefeld.techfak.tdpe.prorema.persistence.*;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.Allow;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAction;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAll;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowPos;

/**
 * Created by Matthias on 24.05.16.
 * Contains the Access Deciders user by the services.
 */
public class AccessDeciderPool {

    public static AccessDecider<ClientEntity> client;
    public static AccessDecider<ContactEntity> contact;
    public static AccessDecider<EmployeeEntity> employee;
    public static AccessDecider<EmployeeProfileEntity> employeeProfile;
    public static AccessDecider<OrganisationUnitEntity> organisationUnit;
    public static AccessDecider<ProjectEntity> project;
    public static AccessDecider<SkillEntity> skill;
    public static AccessDecider<WorksOnEntity> worksOn;
    public static AccessDecider<HasSkillEntity> hasSkill;
    public static  AccessDecider<NeedsSkillEntity> needsSkill;
    public static AccessDecider<Employee.Position> positionChange;

    static {

        client = new AccessDecider<ClientEntity>();
        client.add(new AllowAll<ClientEntity>());

        contact = new AccessDecider<ContactEntity>();
        contact.add(new AllowAll<ContactEntity>());

        employee = new AccessDecider<EmployeeEntity>();
        employee.add(new AllowAction<EmployeeEntity>(Action.VIEW));
        employee.add(new Allow<EmployeeEntity>(Employee.Position.MANAGER, Action.MODIFY));
        employee.add(new AllowPos<EmployeeEntity>(Employee.Position.ADMINISTRATOR));

        employeeProfile = new AccessDecider<EmployeeProfileEntity>();
        employeeProfile.add(new AllowAll<EmployeeProfileEntity>());

        organisationUnit = new AccessDecider<OrganisationUnitEntity>();
        organisationUnit.add(new AllowAll<OrganisationUnitEntity>());

        project = new AccessDecider<ProjectEntity>();
        project.add(new AllowAction<ProjectEntity>(Action.VIEW));
        project.add(new AllowPos<ProjectEntity>(Employee.Position.SENIOR_MANAGER));

        skill = new AccessDecider<SkillEntity>();
        skill.add(new AllowAll<SkillEntity>());

        worksOn = new AccessDecider<WorksOnEntity>();
        worksOn.add(new AllowAll<WorksOnEntity>());

        hasSkill = new AccessDecider<HasSkillEntity>();
        hasSkill.add(new AllowAll<HasSkillEntity>());

        needsSkill = new AccessDecider<NeedsSkillEntity>();
        needsSkill.add(new AllowAll<NeedsSkillEntity>());

        positionChange = new AccessDecider<Employee.Position>();
        positionChange.add(new AllowPos<Employee.Position>(Employee.Position.ADMINISTRATOR));

    }
}
