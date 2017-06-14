package de.unibielefeld.techfak.tdpe.prorema.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Matthias on 7/2/16.
 */
public class ActionTest {
    private void test(Action a) {
        assertEquals(a, Action.fromPriority(a.getPriority()));

    }

    @Test
    public void testgetByPrio() {
        test(Action.DELETE);
        test(Action.CREATE);

        try {
            Action.fromPriority(-1);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail("No exception");
    }
}