package de.unibielefeld.techfak.tdpe.prorema.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrott on 10.05.16.
 */
public class ServiceListTest extends TestCase {

    ArrayList<Integer> createList(int n) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0;i<n;i++) {
            list.add(i);
        }
        return list;
    }
    @Test
    public void testConstruction() throws Exception {
        ArrayList<Integer> testList = createList(10);
        ServiceList<Integer> l = new ServiceList<>(testList.size(),20,testList);
        assertEquals(20,l.getAllEntities());
        assertEquals(testList,l.getList());
        assertEquals(testList.size(), l.getAllowedLength());
    }
}