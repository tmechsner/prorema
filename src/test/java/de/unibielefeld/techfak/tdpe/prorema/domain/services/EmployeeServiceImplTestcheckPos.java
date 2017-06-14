package de.unibielefeld.techfak.tdpe.prorema.domain.services;

import de.unibielefeld.techfak.tdpe.prorema.controller.command.NewEmployeeCmd;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDecider;
import de.unibielefeld.techfak.tdpe.prorema.security.AccessDeciderPool;
import de.unibielefeld.techfak.tdpe.prorema.security.TestHelpers;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mrott on 7/7/16.
 */
public class EmployeeServiceImplTestcheckPos {

    private AccessDecider<Employee.Position> old;

    @Before
    public void setUp() throws Exception {
        old = AccessDeciderPool.positionChange;
        AccessDeciderPool.positionChange = new AccessDecider<Employee.Position>();
        AccessDeciderPool.positionChange.add(new AllowAll<Employee.Position>());
    }

    @After
    public void tearDown() throws Exception {
        AccessDeciderPool.positionChange = old;
    }

    @Test
    public void test1() throws Exception {
        TestHelpers.setLoginToPos(Employee.Position.SENIOR_MANAGER);
        NewEmployeeCmd cmd = new NewEmployeeCmd();

        EmployeeEntity e = TestHelpers.createEmployeeEntity(Employee.Position.CONSULTANT);
        cmd.setPosition(Employee.Position.SENIOR_CONSULTANT.toString());
        assertTrue(EmployeeServiceImpl.checkPositionChange(e, cmd));

        EmployeeEntity a = TestHelpers.createEmployeeEntity(Employee.Position.ADMINISTRATOR);
        assertFalse(EmployeeServiceImpl.checkPositionChange(a, cmd));

        cmd.setPosition(Employee.Position.ADMINISTRATOR.toString());
        assertFalse(EmployeeServiceImpl.checkPositionChange(e, cmd));
    }

    //@Test
    public void testCreate() {
    }
}