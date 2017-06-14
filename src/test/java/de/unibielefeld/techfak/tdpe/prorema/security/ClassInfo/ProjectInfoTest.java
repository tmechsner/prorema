package de.unibielefeld.techfak.tdpe.prorema.security.ClassInfo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mrott on 7/7/16.
 */
public class ProjectInfoTest {
    @Test
    public void testNull() throws Exception {
        ProjectInfo pi = new ProjectInfo();
        assertFalse(pi.isOwner(58, null));
    }
}