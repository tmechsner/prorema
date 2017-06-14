package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.persistence.EmployeeEntity;
import de.unibielefeld.techfak.tdpe.prorema.security.LoginInfoProvider.TestLoginProvider;

import static org.junit.Assert.fail;

/**
 * Created by mrott on 10.05.16.
 */
public class TestHelpers {
    public static void setCurrentLogin(Employee e) {
        try {
            LoginInfo.setLoginProvider(new TestLoginProvider(e));
        } catch (Exception ex) {
            fail("Exception thrown: " + ex.toString());
        }
    }

    private static int employee_id;
    public static Employee createEmployee(Employee.Position pos) {
        return createEmployee("user", pos);
    }
    public static Employee createEmployee(String username) {
        return createEmployee(username, Employee.Position.SENIOR_MANAGER);
    }

    public static Employee createEmployee(String username, Employee.Position pos) {
        return new Employee(createEmployeeEntity(username,pos));
    }

    public static EmployeeEntity createEmployeeEntity(Employee.Position pos) {
        return createEmployeeEntity("Blub", pos);
    }

    public static EmployeeEntity createEmployeeEntity(String username, Employee.Position pos) {
        EmployeeEntity e;
        e = new EmployeeEntity();

        e.setNameTitle("Blun");
        e.setLastName("Blun");
        e.setFirstName("Blun");
        e.setEmail("Blub@atlantic.com");
        e.setTel("213858183904");
        e.setStreet("street");
        e.setZip("sjkdlfasjdf");
        e.setCity("Moon City");
        e.setCountry("Moon");
        e.setCountry("Moon");

        e.setId(employee_id++);
        e.setUsername(username);
        e.setPosition(pos.toString());
        e.setPassword("password");
        return e;
    }

    public static void setLoginToPos(Employee.Position pos) {
        EmployeeEntity act = createEmployeeEntity(pos);
        setCurrentLogin(new Employee(act));
    }
}
