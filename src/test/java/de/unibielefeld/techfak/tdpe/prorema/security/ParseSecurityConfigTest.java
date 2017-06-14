package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.domain.Employee;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAction;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAll;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowPos;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.OwnThings;
import de.unibielefeld.techfak.tdpe.prorema.security.Stupid;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 5/28/16.
 */
public class ParseSecurityConfigTest {
    private AccessDecider ad;
    private AccessDecider rad;

    @Before
    public void setUp() throws Exception {
        rad = new AccessDecider<>();
    }

    @Test
    public void testAccessDeciderload() {
        ad = ParseSecurityConfig.parseString("");
        assertEquals(ad, new AccessDecider<Stupid>());
    }

    @Test
    public void testOneDecider() {
        ad = ParseSecurityConfig.parseString(AllowAll.class.getSimpleName());
        rad.add(new AllowAll<Stupid>());
        assertEquals(ad, rad);
        assertNotEquals(ad, new AccessDecider<Stupid>());
    }

    @Test
    public void testOtherDecider() {
        ad = ParseSecurityConfig.parseString(OwnThings.class.getSimpleName() + "(employee)");
        rad.add(new AllowAll<>());
        assertNotEquals(ad, rad);
    }

    @Test
    public void testExtractParameters() throws Exception {
        assertEquals("", ParseSecurityConfig.getParameterString(AllowAction.class.getSimpleName() + "()"));
        assertEquals("view", ParseSecurityConfig.getParameterString(AllowAction.class.getSimpleName() + "(view)"));
    }

    @Test
    public void testParameter() throws Exception {
        ad = ParseSecurityConfig.parseString(AllowAction.class.getSimpleName() + "(view)");
        rad.add(new AllowAction<Stupid>(Action.VIEW));
        assertEquals(ad,rad);

    }

    @Test
    public void testMultipleDeciders() {
        ad = ParseSecurityConfig.parseString
                (AllowPos.class.getSimpleName() + "(" + Employee.Position.CONSULTANT.toString() + ")" + AllowAction.class.getSimpleName() + "(view)");
        rad.add(new AllowPos<>(Employee.Position.CONSULTANT));
        rad.add(new AllowAction<>(Action.VIEW));

        assertEquals(ad, rad);
    }

    @Test
    public void loadIntoPool() throws Exception {
        ad = ParseSecurityConfig.load("employee:"+AllowPos.class.getSimpleName() + "(" + Employee.Position.CONSULTANT.toString() + ")");
        rad.add(new AllowPos(Employee.Position.CONSULTANT));
        assertEquals(rad,ad);
        assertEquals(rad, AccessDeciderPool.employee);
    }

    @Test
    public void testEmpty() throws Exception {
        ad = ParseSecurityConfig.load("project:");
        assertEquals(rad,ad);
        assertEquals(rad,AccessDeciderPool.project);
    }

    @Test
    public void loadIntoPool2() throws Exception {
        ad = ParseSecurityConfig.load("project:"+AllowPos.class.getSimpleName() + "(" + Employee.Position.CONSULTANT.toString() + ")");
        rad.add(new AllowPos(Employee.Position.CONSULTANT));
        assertEquals(rad,ad);
        assertEquals(rad, AccessDeciderPool.project);
    }
}
