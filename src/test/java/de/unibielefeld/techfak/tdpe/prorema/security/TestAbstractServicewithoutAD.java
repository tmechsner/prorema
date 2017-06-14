package de.unibielefeld.techfak.tdpe.prorema.security;

import de.unibielefeld.techfak.tdpe.prorema.security.Exception.PermissionDeniedException;
import de.unibielefeld.techfak.tdpe.prorema.security.deciders.AllowAll;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Matthias on 5/27/16.
 */
public class TestAbstractServicewithoutAD {

    private StupidRep rep;
    private StupidService service;

    @Before
    public void setUp() throws Exception {
        rep = new StupidRep();
        service = new StupidService(new StupidRep());
    }

    @Test
    public void testAbstractServiceWithoutAD() {
        try {
            service.findOne(0);
        } catch (PermissionDeniedException ex) {
            return;
        }
        fail("Exception not thrown");
    }

    @Test
    public void testAbstractServiceWithAD() {
        AccessDecider ad = new AccessDecider();
        ad.add(new AllowAll());
        service.setAccessDecider(ad);
        assertNotNull(service.findOne(0));
    }
}
