package com.enremmeta.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

public class UtilsTest {
    @Ignore
    @Test
    public void runTest() throws Exception {

        Map<Integer, String> testMap = new HashMap<Integer, String>();
        Map<Integer, String> resultMap = new HashMap<Integer, String>();

        ExecutorService pool = Executors.newFixedThreadPool(5);

        for (int i = 1; i <= 10; i++) {
            final int thread = i;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        Integer val = (int) Math.ceil(Math.random() * 10);

                        if (thread % 2 == 0) {
                            testMap.put(j, val.toString());
                        } else {
                            if (testMap.containsKey(j)) {
                                testMap.remove(j);
                            }
                        }
                    }

                    String delimString = Utils.delimFormat(';', ',', "-", false, 0, testMap);
                    int sizeMap = testMap.size();
                    int delimStringLength = delimString.split(";").length;
                    System.out.println("thread: " + thread + "; Size of map: " + sizeMap + "/"
                                    + delimStringLength + "; " + delimString);

                    resultMap.put(thread, delimString);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        String delimString = Utils.delimFormat(';', ',', "-", false, 0, testMap);
        int sizeMap = testMap.size();
        int delimStringLength = delimString.split(";").length;

        System.out.println("Size map: " + sizeMap + "/" + delimStringLength + "; " + delimString);

        for (Integer key : resultMap.keySet()) {
            String valueString = resultMap.get(key);
            assertNotNull(valueString);
            assertFalse(valueString.length() == 0);
        }
    }


    @Test
    public void replaceTest() {
        StringBuilder sb = null;
        String s1 = "cc";
        String s2 = "aa";
        String searched = "a";
        String replaceTo = "b";

        assertNull(sb);
        sb = Utils.replace(sb, searched, replaceTo);
        assertNull(sb);

        sb = new StringBuilder();

        assertTrue(sb.length() == 0);
        sb = Utils.replace(sb, searched, replaceTo);
        assertTrue(sb.length() == 0);

        sb.append(s1);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replace(sb, null, replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replace(sb, "", replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replace(sb, searched, null);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replace(sb, "notexists_substring", replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb.append(s2);
        assertTrue(sb.length() == 4);
        assertTrue(sb.indexOf(s2) >= 0);
        sb = Utils.replace(sb, searched, replaceTo);
        assertTrue(sb.length() == 4);
        assertTrue(sb.indexOf(searched) >= 0);
        assertTrue(sb.indexOf(replaceTo) >= 0);
        assertEquals(sb.indexOf(searched), sb.lastIndexOf(searched));
        assertEquals(sb.indexOf(replaceTo), sb.lastIndexOf(replaceTo));
        sb = Utils.replace(sb, searched, replaceTo);
        assertTrue(sb.length() == 4);
        assertTrue(sb.indexOf(searched) < 0);
        assertTrue(sb.indexOf(replaceTo) >= 0);
        assertNotEquals(sb.indexOf(replaceTo), sb.lastIndexOf(replaceTo));

        sb = Utils.replace(sb, replaceTo, "");
        assertTrue(sb.length() == 3);
    }

    @Test
    public void replaceAllTest() {
        StringBuilder sb = null;
        String s1 = "cc";
        String s2 = "aa";
        String searched = "a";
        String replaceTo = "b";

        assertNull(sb);
        sb = Utils.replaceAll(sb, searched, replaceTo);
        assertNull(sb);

        sb = new StringBuilder();

        assertTrue(sb.length() == 0);
        sb = Utils.replaceAll(sb, searched, replaceTo);
        assertTrue(sb.length() == 0);

        sb.append(s1);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replaceAll(sb, null, replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replaceAll(sb, "", replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replaceAll(sb, searched, null);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb = Utils.replaceAll(sb, "notexists_substring", replaceTo);
        assertTrue(sb.length() == 2);
        assertEquals(s1, sb.toString());

        sb.append(s2);
        assertTrue(sb.length() == 4);
        assertTrue(sb.indexOf(s2) >= 0);
        sb = Utils.replaceAll(sb, searched, replaceTo);
        assertTrue(sb.length() == 4);
        assertTrue(sb.indexOf(searched) < 0);
        assertTrue(sb.indexOf(replaceTo) >= 0);
        assertNotEquals(sb.indexOf(replaceTo), sb.lastIndexOf(replaceTo));

        sb = Utils.replaceAll(sb, replaceTo, "");
        assertTrue(sb.length() == 2);
    }

}
