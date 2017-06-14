package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import de.unibielefeld.techfak.tdpe.prorema.security.Exception.ConfigSyntaxError;
import org.aspectj.lang.annotation.DeclareError;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mrott on 6/20/16.
 */
public class AllowPrioTest {
    @Test
    public void test() throws Exception, ConfigSyntaxError {
        Decider ds = new AllowPrio<>();
        ds.init(Action.CREATE.toString() +',' + "10");
        Decider ds2 = new AllowPrio<>();
        /*ds2.init(Action.CREATE.toString() +", " + "10");
        Decider dc = new AllowPrio<>(Action.CREATE, 10);
        assertEquals(dc, ds2);*/
    }

    @Test
    public void rubbishInput() throws Exception {
        try {
            Decider ds = new AllowPrio<>();
            ds.init("blub");
        } catch (ConfigSyntaxError ex) {
            return;
        }
        fail("Exception not thrown");
    }

    @Test
    public void testEquals() throws Exception {
        Decider d1 = new AllowPrio(Action.VIEW, 20);
        Decider d2 = new AllowPrio(Action.VIEW, 20);
        Decider d3 = new AllowPrio(Action.DELETE, 20);
        Decider d4 = new AllowPrio(Action.VIEW, 30);
        assertEquals(d1,d2);
        assertNotEquals(d1,d3);
        assertNotEquals(d1,d4);
    }
}