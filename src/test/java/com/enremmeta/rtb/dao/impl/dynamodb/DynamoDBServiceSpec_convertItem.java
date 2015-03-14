package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.enremmeta.rtb.LogUtils;

@Ignore("Method DynamoDBService.convert(Item) was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBService.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_convertItem implements Externalizable{

    @Test
    public void testEmptyItem() {
        DynamoDBService ddbs = new DynamoDBService();
//        assertEquals(HashMap.class, ddbs.convert(new Item()).getClass());
//        assertTrue(((HashMap)ddbs.convert(new Item())).isEmpty());
    }
    
    @Test
    public void testNull() {
        DynamoDBService ddbs = new DynamoDBService();
//        assertNull(ddbs.convert((Item)null));
    }
    
    @Test
    public void testNotEmptyItem() {
        DynamoDBService ddbs = new DynamoDBService();
        Item item = new Item().withString("TEST_ATTR_NAME", "TEST_VAL");
//        assertEquals(HashMap.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ((HashMap) ddbs.convert(item)).get("TEST_ATTR_NAME"));
    }
    
    @Test
    public void testNotEmptyItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.lang.String")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "TEST_VAL");
//        assertEquals(String.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ddbs.convert(item));
    }
    
    @Test
    public void testIntegerItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.lang.Integer")
//            .withInt(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, 10101);
//        assertEquals(Integer.class, ddbs.convert(item).getClass());
//        assertEquals(10101, ddbs.convert(item));
    }
    
    @Test
    public void testFloatItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.lang.Float")
//            .withFloat(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, (float)10101.01);
//        assertEquals(Float.class, ddbs.convert(item).getClass());
//        assertEquals(10101.01f, ddbs.convert(item));
    }
    
    @Test
    public void testLongItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.lang.Long")
//            .withLong(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, 10101L);
//        assertEquals(Long.class, ddbs.convert(item).getClass());
//        assertEquals(10101L, ddbs.convert(item));
    }
    
    @Test
    public void testListItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.util.List")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "[\"TEST_VAL\"]");
//        assertEquals(ArrayList.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ((List)ddbs.convert(item)).get(0));
    }
    
    @Test
    public void testSetItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.util.Set")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "[\"TEST_VAL\"]");
//        
//        Object convertedItem = ddbs.convert(item);
        
//        assertEquals(HashSet.class, convertedItem.getClass());
//        assertTrue(((Set<?>)convertedItem).contains("TEST_VAL"));
    }
    
    @Test
    public void testCannotParseSetItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.util.Set")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "TEST_VAL");
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot parse " + "TEST_VAL"), isA(IOException.class));

        //assertNull(null, convertedItem);
    }
    
    @Test
    public void testMapItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.util.Map")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "[\"TEST_VAL\"]");
//        assertEquals(ArrayList.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ((List)ddbs.convert(item)).get(0));
    }
    
    @Test
    public void testCannotParseMapItem_withDYN_DB_PREFIX_TYPE_ATTR() {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "java.util.Map")
//            .withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, "TEST_VAL");
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot parse " + "TEST_VAL"), isA(IOException.class));

        //assertNull(null, convertedItem);
    }
    
    public class ExternalizableMarker implements Externalizable {
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
   
        }
        
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            
        }
    };
    
    @Test
    public void testDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        DynamoDBService ddbs = new DynamoDBService();
 
//        ddbs.getKlasses().put("com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBServiceSpec_convertItem", this.getClass());
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, 
//                            "com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBServiceSpec_convertItem")
//            .withBinary(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new byte[10]);
        
//        Item itemSpy = Mockito.spy(item);
//        Mockito.when(itemSpy.getBinary(
//                        Mockito.eq(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR)))
//                        .thenReturn(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        
        PowerMockito.whenNew(ObjectInputStream.class).withAnyArguments()
        .thenReturn(null);
        
//        assertEquals(this.getClass(), ddbs.convert(itemSpy).getClass());
    }

    @Test
    public void testCannotFindClassDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
 
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, "ClassNotFound")
//            .withBinary(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new byte[10]);
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot find class for " + "ClassNotFound"), isA(ClassNotFoundException.class));

//        assertNull(null, convertedItem);
    }

    @Test
    public void testCannotInstantiateClassDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
 
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, ExternalizableMarker.class.getName())
//            .withBinary(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new byte[10]);
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot instantiate class for " + ExternalizableMarker.class.getName()), isA(InstantiationException.class));

//        assertNull(null, convertedItem);
    }

    @Test
    public void testCannotDeserializeDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
 
//        Item item = new Item()
//            .withString(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, this.getClass().getName())
//            .withBinary(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new byte[10]);
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot deserialize " + this.getClass().getName()), isA(IOException.class));

//        assertNull(null, convertedItem);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

}
