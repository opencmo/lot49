package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.enremmeta.rtb.LogUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({
    //DynamoDBDaoMap.class,
    LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_put {
    private DynamoDBService dynamoDBService;
    private Table tableMock = Mockito.mock(Table.class);
    private Item itemSpy = Mockito.spy(new Item());
    Map<String, Class<?>> klasses = new HashMap<String, Class<?>>();

    private String keyField = "KeyField";
    private String keyValue = "KeyValue";

    @Before
    public void setUp() throws Exception {
        dynamoDBService = Mockito.mock(DynamoDBService.class);
        //Mockito.doReturn(tableMock).when(dynamoDBService).getTable();
        Mockito.doReturn(keyField).when(dynamoDBService).getKeyField();
//        Mockito.doReturn(klasses).when(dynamoDBService).getKlasses();

        PowerMockito.whenNew(Item.class).withNoArguments().thenReturn(itemSpy);
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsString() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.lang.String":'
        String objectAttributeValue = "String value";
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, String.class.getName());
//        Mockito.verify(itemSpy).withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, objectAttributeValue);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsInteger() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.lang.Integer":'
        Integer objectAttributeValue = 123;
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Integer.class.getName());
//        Mockito.verify(itemSpy).withInt(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, objectAttributeValue);
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsFloat() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.lang.Float":'
        Float objectAttributeValue = 123F;
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Float.class.getName());
//        Mockito.verify(itemSpy).withFloat(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, objectAttributeValue);
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsLong() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.lang.Long":'
        Long objectAttributeValue = 123L;
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Long.class.getName());
//        Mockito.verify(itemSpy).withLong(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, objectAttributeValue);
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsMap() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.util.Set":'
        Map<Integer, String> objectAttributeValue = new HashMap<Integer, String>();
        objectAttributeValue.put(1, "One");
        objectAttributeValue.put(2, "Two");
        objectAttributeValue.put(3, "Three");
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Map.class.getName());
//        Mockito.verify(itemSpy).withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, Utils.MAPPER.writeValueAsString(objectAttributeValue));
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsSet() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.util.Set":'
        Set<Integer> objectAttributeValue = new HashSet<Integer>(Arrays.asList(1, 2, 3));
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Set.class.getName());
//        Mockito.verify(itemSpy).withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, Utils.MAPPER.writeValueAsString(objectAttributeValue));
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsList() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.util.Set":'
        List<Integer> objectAttributeValue = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        
        testPut(objectAttributeValue);
        
 //       Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, List.class.getName());
 //       Mockito.verify(itemSpy).withString(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR, Utils.MAPPER.writeValueAsString(objectAttributeValue));
    }
    
    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_doesNotPutObjectAttrIntoTableIfParameterTypeIsListAndThrowsSerializationException() throws Exception {
        // TODO in DynamoDBDaoMap.put(): add statement 'break' in the end of the section 'case "java.util.Set":'
        PowerMockito.mockStatic(LogUtils.class);
        
        List<Custom> objectAttributeValue = new ArrayList<Custom>(Arrays.asList(new Custom()));;
        
        testPut(objectAttributeValue);
        
 //       Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, List.class.getName());
 //       Mockito.verify(itemSpy, never()).withString(eq(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR), any());
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Error writing value " + objectAttributeValue), isA(JsonProcessingException.class));
    }
    
    @Test
    public void positiveFlow_putsAllDataIntoTableIfParameterTypeIsExternalizable() throws Exception {
        Externalizable objectAttributeValue = new Custom();
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Custom.class.getName());
//        Mockito.verify(itemSpy).withBinary(eq(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR), isA(byte[].class));
    }
    
    @Test
    public void negativeFlow_doesNotPutObjectAttrIntoTableIfParameterTypeIsExternalizableAndThrowsInstantiationException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        Custom2 objectAttributeValue = new Custom2();
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Custom2.class.getName());
//        Mockito.verify(itemSpy, never()).withBinary(eq(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR), any(byte[].class));

        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot instantiate class for " + Custom2.class.getName()), isA(InstantiationException.class));
    }
    
    @Test
    public void negativeFlow_doesNotPutObjectAttrIntoTableIfParameterTypeIsExternalizableAndThrowsIOException() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        Custom3 objectAttributeValue = new Custom3();
        
        testPut(objectAttributeValue);
        
//        Mockito.verify(itemSpy).with(DynamoDBService.DYN_DB_PREFIX_TYPE_ATTR, Custom3.class.getName());
//        Mockito.verify(itemSpy, never()).withBinary(eq(DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR), any(byte[].class));

        PowerMockito.verifyStatic();
        LogUtils.error(eq("Cannot deserialize " + Custom3.class.getName()), isA(IOException.class));
    }
    
    private <T> void testPut(T objectAttributeValue) {
//        DynamoDBDaoMap<T> dynamoDBDaoMap = new DynamoDBDaoMap<T>(dynamoDBService);
        
//        dynamoDBDaoMap.put(keyValue, objectAttributeValue);
        
        Mockito.verify(itemSpy).withPrimaryKey(keyField, keyValue);
//        Mockito.verify(itemSpy).withLong(eq(DynamoDBService.DYN_DB_TIMESTAMP_ATTR), any(Long.class));
        Mockito.verify(tableMock).putItem(any(Item.class));
    }
    
    // auxiliary  inner classes
    
    @JsonSerialize(using = CustomSerializer.class)
    public static class Custom implements Externalizable {
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {}

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {}
    }
    
    public static class CustomSerializer extends JsonSerializer<Custom> {
        @Override
        public void serialize(Custom value, JsonGenerator jgen, SerializerProvider provider) 
          throws IOException, JsonProcessingException {
            throw new RuntimeException("Serialization exception");
        }
    }
    
    public class Custom2 extends Custom {} /// throws InstantiationException outside the class DynamoDBDaoMapSpec_put

    public static class Custom3 extends Custom {
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            throw new IOException("Write external exception");
        }
    }
}
