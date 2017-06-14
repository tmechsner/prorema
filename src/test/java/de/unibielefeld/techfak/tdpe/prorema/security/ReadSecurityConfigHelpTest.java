package de.unibielefeld.techfak.tdpe.prorema.security;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 7/1/16.
 */
public class ReadSecurityConfigHelpTest {
    @Test
    public void testChopOff() {
        assertEquals("", ReadSecurityConfig.choppOff("jaklsdjfk", 1000));
        assertEquals("cd",ReadSecurityConfig.choppOff("abcd",2));
    }


}