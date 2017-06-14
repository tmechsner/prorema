package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 6/18/16.
 */
public class AllowPosTest {

    private Decider d;

    @Before
    public void setUp() throws Exception {
        d = new AllowPos();
    }

    @Test
    public void testInit() throws ConfigSyntaxError {
        d.init(Employee.Position.SENIOR_MANAGER.toString());
        assertEquals(new AllowPos(Employee.Position.SENIOR_MANAGER), d);
        testNonsense("");
        testNonsense("blub");
    }

    @Test
    public void testEquals() {
        assertNotEquals(new AllowPos(Employee.Position.MANAGER), new AllowPos(Employee.Position.MANAGER,Action.MODIFY));
        AllowPos aP = new AllowPos(Employee.Position.MANAGER);
        aP.equals("jksadfjkl");
    }

    @Test
    public void testInitwithAction() throws ConfigSyntaxError {
        d.init(Employee.Position.MANAGER.toString() + "," + Action.DELETE.toString());
        assertEquals(new AllowPos(Employee.Position.MANAGER, Action.DELETE), d);
    }

    private void testNonsense(String str) throws ConfigSyntaxError {
        try {
            d.init(str);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail("No Exception thrown");
    }
}