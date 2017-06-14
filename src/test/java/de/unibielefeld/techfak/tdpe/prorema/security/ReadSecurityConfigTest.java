package de.unibielefeld.techfak.tdpe.prorema.security;

import org.junit.Test;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Created by Matthias on 6/18/16.
 */
public class ReadSecurityConfigTest {

    private CharArrayReader r;
    private SimpleStringConsumer consumer = new SimpleStringConsumer();

    public void setString(String str) {
        r = new CharArrayReader(str.toCharArray());
    }

    private void testNextLine(String out) {
        try {
            ReadSecurityConfig.readLine(r, consumer);
        } catch (IOException ex) {
            fail("IO exception thrown");
        }
        assertEquals(out, consumer.str.poll());
    }

    @Test
    public void testReadLine1() throws IOException {
        setString("");
        testNextLine(null);
    }

    @Test
    public void testReadLine2() throws IOException {
        setString("Blub");
        testNextLine("Blub");
    }

    @Test
    public void testReadSecondLine() {
        setString("line 1\nline 2");
        testNextLine("line 1");
        testNextLine("line 2");
    }

    @Test
    public void testReadThirdLine() {
        setString("line 1\nline 2\nline3\n");
        testNextLine("line 1");
        testNextLine("line 2");
        testNextLine("line3");
    }

    private class SimpleStringConsumer implements Consumer<String> {
        public Queue<String> str = new ArrayDeque<String>();
        @Override
        public void accept(String s) {
            str.offer(s);
        }
    }

}