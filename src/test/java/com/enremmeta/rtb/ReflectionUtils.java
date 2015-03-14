package com.enremmeta.rtb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
    /**
     * Change value of static final field. Use it as follows:<br /><br />
     * setFinalStatic(MyClass.class.getField("myField"), "newValue"); // For a String
     * 
     * @param field static final field
     * @param newValue new value for this field
     * @throws Exception
     */
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    /**
     * Change value of static final field. Use it as follows:<br /><br />
     * setFinalStatic(MyClass.class, "myField", "newValue"); // For a String
     * 
     * @param clazz class with static final field
     * @param fieldName name of static final field
     * @param newValue new value for this field
     * @throws Exception
     */
    public static void setFinalStatic(Class<?> clazz, String fieldName, Object newValue) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        setFinalStatic(field, newValue);
    }

    public static Object getPrivateStatic(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    public static void setPrivateStatic(Class<?> clazz, String fieldName, Object newValue) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
