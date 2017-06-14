package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.Mocks.EmployeeRepositoryMock;
import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
/**
 * Created by Matthias on 4/22/16.
 */


public class EmployeeSecuSerTest {

    private EmployeeRepositoryMock mock;
    private EmployeeSecuSer ser;
    private EmployeeEntity emp;

    @Before
    public void setUp() throws Exception {
        mock = new EmployeeRepositoryMock();
        ser = new EmployeeSecuSer(mock);
        emp = TestHelpers.createEmployeeEntity("blub", Employee.Position.PARTNER);
        mock.addEmployee(emp);
    }

    @Test
    public void testRetrival() {
        assertEquals(new Employee(emp), ser.loadUserByUsername("blub"));
        try {
            ser.loadUserByUsername("blah");
        } catch (UsernameNotFoundException exp) {
            return;
        }
        fail("Exception not thrown");
    }

    @Test
    public void testDoubleEntry() {
        mock.addEmployee(emp);
        try {
            ser.loadUserByUsername("blub");
        } catch (UsernameNotFoundException exp) {
            return;
        }
        fail("Exception not thrown");
    }
}
