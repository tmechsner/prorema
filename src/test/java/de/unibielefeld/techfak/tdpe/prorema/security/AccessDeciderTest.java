package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.OrganisationUnitEntity;
import de.unibielefeld.techfak.tdpe.prorema.persistence.ProjectEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.EmployeeInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.ProjectInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.*;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 5/6/16.
 */
public class AccessDeciderTest {

    private Employee e;
    private EmployeeEntity e_;
    private Employee e1;
    private EmployeeEntity e1_;
    private Employee e2;
    private EmployeeEntity e2_;
    private ProjectEntity p;
    private AccessDecider<ProjectEntity> adp;
    private AccessDecider<EmployeeEntity> ade;
    private Employee sM;


    @Before
    public void setUp() throws Exception {
        adp = new AccessDecider<ProjectEntity>();
        ade = new AccessDecider<EmployeeEntity>();
        e_ = TestHelpers.createEmployeeEntity(Employee.Position.CONSULTANT);
        e  = new Employee(e_);
        TestHelpers.setCurrentLogin(e);
        e1_ = TestHelpers.createEmployeeEntity(Employee.Position.CONSULTANT);
        e1 = new Employee(e1_);
        e2_ = TestHelpers.createEmployeeEntity(Employee.Position.CONSULTANT);
        e2 = new Employee(e2_);
        p = new ProjectEntity(
                "blah",
                "blah",
                "finished",
                new BigDecimal(0),
                0,
                false,
                0,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "country",
                "city",
                "street",
                "zip");
        sM = TestHelpers.createEmployee(Employee.Position.SENIOR_MANAGER);
    }
    @Test
    public void testModDefaultManager() {
        ade.add(new AllowPos<EmployeeEntity>(Employee.Position.ADMINISTRATOR));
        EmployeeEntity a = TestHelpers.createEmployeeEntity(Employee.Position.ADMINISTRATOR);
        TestHelpers.setCurrentLogin(new Employee(a));
        assertTrue(ade.isAllowed(Action.MODIFY, a));
        assertTrue(ade.isAllowed(Action.MODIFY, e_));

        TestHelpers.setCurrentLogin(e);
        assertFalse(ade.isAllowed(Action.MODIFY, a));
    }

    @Test
    public void testModEmployeeEmployee() {
        ade.add(new OwnThings<EmployeeEntity>(new EmployeeInfo()));

        assertTrue(ade.isAllowed(Action.MODIFY, e_));
        assertFalse(ade.isAllowed(Action.MODIFY, e1_));
    }

    @Test
    public void testProject() {
        OrganisationUnitEntity orgaUnit = new OrganisationUnitEntity();
        orgaUnit.setFirstManager(e_);
        orgaUnit.setSecondManager(e2_);
        p.setOrganisationUnit(orgaUnit);

        adp.add(new AllowAction(Action.VIEW));
        adp.add(new OwnThings(new ProjectInfo()));

        assertTrue(adp.isAllowed(Action.MODIFY, p));
        TestHelpers.setCurrentLogin(e2);
        assertTrue(adp.isAllowed(Action.MODIFY, p));

        TestHelpers.setCurrentLogin(e1);
        assertFalse(adp.isAllowed(Action.MODIFY, p));
        assertTrue(adp.isAllowed(Action.VIEW, p));
    }

    @Test
    public void testSeniorManager() {
        adp.add(new AllowPos<ProjectEntity>(Employee.Position.SENIOR_MANAGER));
        TestHelpers.setCurrentLogin(sM);
        assertTrue(adp.isAllowed(Action.MODIFY, p));
        TestHelpers.setCurrentLogin(e1);
        assertFalse(adp.isAllowed(Action.MODIFY, p));
    }

    @Test
    public void testAllowedAction() {
        adp.add(new AllowPos<ProjectEntity>(Employee.Position.SENIOR_MANAGER, Action.CREATE));
        assertFalse(adp.isAllowed(Action.CREATE, null));

        TestHelpers.setCurrentLogin(sM);
        assertTrue(adp.isAllowed(Action.CREATE, null));

        assertFalse(adp.isAllowed(Action.DELETE, null));
    }

    @Test
    public void testSeniorManagerNoRights() {
        Employee sM = TestHelpers.createEmployee(Employee.Position.SENIOR_MANAGER);
        TestHelpers.setCurrentLogin(sM);
        assertFalse(adp.isAllowed(Action.MODIFY, p));
    }

    @Test
    public void testAllowDelete() throws Exception {
        adp.add(new AllowAction<ProjectEntity>(Action.DELETE));
        assertTrue(adp.isAllowed(Action.DELETE, p));
    }

    @Test
    public void testPrio() throws Exception {
        adp.add(new AllowPrio<ProjectEntity>(Action.MODIFY, 510));
        assertFalse(adp.isAllowed(Action.MODIFY, p));
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.ADMINISTRATOR));
        assertTrue(adp.isAllowed(Action.MODIFY, p));
        assertFalse(adp.isAllowed(Action.DELETE, p));
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.SENIOR_CONSULTANT));
        assertTrue(adp.isAllowed(Action.MODIFY, p));
    }

    @Test
    public void testExceptionThrowing() {
        try {
            adp.isAllowedThrow(Action.VIEW, null);
        } catch (PermissionDeniedException ex) {
            return;
        }
        fail("Exception not thrown");
    }

    @Test
    public void testExceptionNotThrowing() {
        adp.add(new AllowAll<ProjectEntity>());
        try {
            adp.isAllowedThrow(Action.VIEW, null);
        } catch (PermissionDeniedException ex) {
            fail("Exception Thrown");
        }
    }

    @Test
    public void testAllow() {
        adp.add(new Allow<ProjectEntity>(Employee.Position.SENIOR_MANAGER.getPriority(), Action.MODIFY.getPriority()));
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.SENIOR_MANAGER));
        assertTrue(adp.isAllowed(Action.MODIFY, null));
        assertFalse(adp.isAllowed(Action.DELETE, null));
        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.SENIOR_CONSULTANT));
        assertFalse(adp.isAllowed(Action.MODIFY, null));
        assertFalse(adp.isAllowed(Action.DELETE, null));

        TestHelpers.setCurrentLogin(TestHelpers.createEmployee(Employee.Position.ADMINISTRATOR));
        assertFalse(adp.isAllowed(Action.DELETE, null));
        assertTrue(adp.isAllowed(Action.MODIFY, null));

    }
}