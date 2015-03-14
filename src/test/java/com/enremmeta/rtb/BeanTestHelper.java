package com.enremmeta.rtb;

import static org.junit.Assert.assertEquals;

import org.powermock.reflect.Whitebox;

public class BeanTestHelper {

    
    public static void testSetAndGet(Object target, String setter, String getter, Object argument) throws Exception {
        Whitebox.invokeMethod(target, setter, argument);
        assertEquals(argument, Whitebox.invokeMethod(target, getter));
    }
    
    public static void testGet(Object target, String field, String getter, Object argument) throws Exception {
        Whitebox.setInternalState(target, field, argument);
        assertEquals(argument, Whitebox.invokeMethod(target, getter));
    }

}
