package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.enremmeta.rtb.LogUtils;

@Ignore("Method DynamoDBService.convert(Map) was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBService.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_convertMap implements Externalizable{

    @Test
    public void testEmptyItem() {
        DynamoDBService ddbs = new DynamoDBService();
//        assertNull(ddbs.convert(new HashMap<String, AttributeValue>()));
    }
    
    @Test
    public void testNull() {
        DynamoDBService ddbs = new DynamoDBService();
//        assertNull(ddbs.convert((HashMap<String, AttributeValue>)null));
    }
    
    @Test
    public void testNotEmptyItem() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("TEST_ATTR_NAME", new AttributeValue().withS("TEST_VAL"));
//        assertNull(ddbs.convert(item));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_nullType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("TEST_VAL"));
//        try{
//            ddbs.convert(item);
//            fail("shoul throw exception");
//        }
//        catch(NullPointerException npe){
//            assertTrue(npe.getStackTrace()[0].toString().contains("DynamoDBService.convert"));
//        }
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_stringType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("TEST_VAL"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.lang.String"));
//        assertEquals(String.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ddbs.convert(item));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_integerType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("101"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.lang.Integer"));
//        assertEquals(Integer.class, ddbs.convert(item).getClass());
//        assertEquals(101, ddbs.convert(item));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_floatType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("101.01"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.lang.Float"));
//        assertEquals(Float.class, ddbs.convert(item).getClass());
//        assertEquals(101.01f, ddbs.convert(item));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_longType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("1010101010101"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.lang.Long"));
//        assertEquals(Long.class, ddbs.convert(item).getClass());
//        assertEquals(1010101010101L, ddbs.convert(item));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_listType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("[\"TEST_VAL\"]"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.util.List"));
//        assertEquals(ArrayList.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ((List)ddbs.convert(item)).get(0));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_setType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("[\"TEST_VAL\"]"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.util.Set"));
//        
//        Object convertedItem = ddbs.convert(item);
        
//        assertEquals(HashSet.class, convertedItem.getClass());
//        assertTrue(((Set<?>)convertedItem).contains("TEST_VAL"));
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_cannotParseSetType() {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("TEST_VAL"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.util.Set"));
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot parse " + "TEST_VAL"), isA(IOException.class));

//        assertNull(null, convertedItem);
    }
    
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_mapType() {
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("{\"TEST_KEY\":\"TEST_VAL\"}"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.util.Map"));
//        assertEquals(LinkedHashMap.class, ddbs.convert(item).getClass());
//        assertEquals("TEST_VAL", ((Map)ddbs.convert(item)).get("TEST_KEY"));
    }
       
    @Test
    public void testValueOfDYN_DB_PREFIX_OBJECT_ATTR_cannotParseMapType() {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withS("TEST_VAL"));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("java.util.Map"));
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot parse " + "TEST_VAL"), isA(IOException.class));

//        assertNull(null, convertedItem);
    }
       
    @Test
    public void testDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        DynamoDBService ddbs = new DynamoDBService();
 
//        ddbs.getKlasses().put("com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBServiceSpec_convertMap", this.getClass());
//        
//        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue()
//            .withB(java.nio.ByteBuffer.allocate(100)));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue()
//            .withS("com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBServiceSpec_convertMap"));
        
        PowerMockito.whenNew(ObjectInputStream.class).withAnyArguments()
            .thenReturn(null);
        
//        assertEquals(this.getClass(), ddbs.convert(item).getClass());
    }

    @Test
    public void testCannotFindClassDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
 
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withB(java.nio.ByteBuffer.allocate(100)));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS("ClassNotFound"));
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
 
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withB(java.nio.ByteBuffer.allocate(100)));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS(DefaultTypeMarker.class.getName()));
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot instantiate class for " + DefaultTypeMarker.class.getName()), isA(InstantiationException.class));

//        assertNull(null, convertedItem);
    }

    @Test
    public void testCannotDeserializeDefaultItem_withDYN_DB_PREFIX_TYPE_ATTR() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        DynamoDBService ddbs = new DynamoDBService();
 
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        item.put(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, new AttributeValue().withB(java.nio.ByteBuffer.allocate(100)));
//        item.put(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, new AttributeValue().withS(this.getClass().getName()));
//        
//        Object convertedItem = ddbs.convert(item);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot deserialize " + this.getClass().getName()), isA(IOException.class));

//        assertNull(null, convertedItem);
    }
    
    public class DefaultTypeMarker { }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

}
