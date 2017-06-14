package de.unibielefeld.techfak.tdpe.prorema.security.deciders;

import de.unibielefeld.techfak.tdpe.prorema.security.Action;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by mrott on 6/16/16.
 */
public class AllowActionTest {

    @Test
    public void init() throws Exception {
        AllowAction d = new AllowAction(Action.DELETE);
        AllowAction sd = new AllowAction();
        sd.init("delete");
        assertEquals(d,sd);
    }


    @Test
    public void initIlleagal() throws Exception {
        AllowAction ill = new AllowAction();
        try {
            ill.init("blubber die blub");
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail("Exception expected but no thrown");
    }
}