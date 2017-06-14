package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.EmployeeInfo;
import de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo.ProjectInfo;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 6/25/16.
 */
public class OwnThingsTest {
    @Test
    public void testSetup() {
        OwnThings dc = new OwnThings(new ProjectInfo());
        OwnThings ds = new OwnThings();
        ds.init("project");
        assertEquals(dc,ds);
    }

    @Test
    public void testEmployee() {
        OwnThings dc = new OwnThings(new EmployeeInfo());
        OwnThings ds = new OwnThings();
        ds.init("employee");
        assertEquals(dc,ds);
    }

    @Test
    public void testEquals() {
        OwnThings o1 = new OwnThings(new ProjectInfo());
        OwnThings o2 = new OwnThings();
        OwnThings o3 = new OwnThings();
        assertEquals(o2,o3);
        assertNotEquals(o1,o2);
    }
}