package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 7/2/16.
 */
public class AllowTest {
    @Test
    public void testEquals() {
        Allow a1 = new Allow(0,0);
        Allow a2 = new Allow(0,1);
        Allow a3 = new Allow(0,1);
        Allow a4 = new Allow(1,0);

        assertNotEquals(a1,a2);
        assertNotEquals(a1,a4);
        assertEquals(a2,a3);
    }

    @Test
    public void testInit() throws ConfigSyntaxError {
        Allow a1 = new Allow(Employee.Position.MANAGER, Action.MODIFY);
        Allow a1s = new Allow();
        a1s.init(Employee.Position.MANAGER.toString() +"," + Action.MODIFY.toString());
        assertEquals(a1,a1s);

        Allow a2 = new Allow(Employee.Position.ADMINISTRATOR, Action.DELETE);
        Allow a2s = new Allow();
        a2s.init(Employee.Position.ADMINISTRATOR.toString() + ","+ Action.DELETE.toString());
        assertEquals(a2,a2s);
    }
}