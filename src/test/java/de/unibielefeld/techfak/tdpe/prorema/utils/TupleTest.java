package de.unibielefeld.techfak.tdpe.prorema.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Benedikt Volkmer
 *         Created on 7/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TupleTest {

    @Mock Object left;
    @Mock Object right;

    @Test
    public void getLeft() throws Exception {
        // given
        Tuple<Object, Object> tuple = new Tuple<>(left, right);
        // when
        Object result = tuple.getLeft();
        // then
        assertThat(result).isEqualTo(left);
    }

    @Test
    public void getRight() throws Exception {
        // given
        Tuple<Object, Object> tuple = new Tuple<>(left, right);
        // when
        Object result = tuple.getRight();
        // then
        assertThat(result).isEqualTo(right);
    }

}