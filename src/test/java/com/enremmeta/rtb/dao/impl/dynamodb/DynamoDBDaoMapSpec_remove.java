package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBService.class, 
//    DynamoDBDaoMap.class
    })
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_remove {
    private DynamoDBService dynamoDBService;
//    private DynamoDBDaoMap<String> dynamoDBDaoMap;
    private AmazonDynamoDBClient clientMock;
    private DeleteItemRequest requestMock;
    
    private String tableName = "TableName";
    private String keyField = "KeyField";
    private String keyValue = "KeyValue";
    private String expectedResult = "Value of deleted item";
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        requestMock = Mockito.mock(DeleteItemRequest.class);
        PowerMockito.whenNew(DeleteItemRequest.class).withNoArguments().thenReturn(requestMock);
        
        clientMock = Mockito.mock(AmazonDynamoDBClient.class);
        Mockito.when(clientMock.deleteItem(requestMock)).thenReturn((Future<DeleteItemResult>) Mockito.mock(DeleteItemResult.class));
        
        dynamoDBService = PowerMockito.mock(DynamoDBService.class);
        PowerMockito.when(dynamoDBService.getClient()).thenReturn(clientMock);
        PowerMockito.when(dynamoDBService.getTableName()).thenReturn(tableName);
        PowerMockito.when(dynamoDBService.getKeyField()).thenReturn(keyField);
//        PowerMockito.when(dynamoDBService.convert(any(Map.class))).thenReturn(expectedResult);
        
//        dynamoDBDaoMap = new DynamoDBDaoMap<String>(dynamoDBService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deletesItemForTheSpecifiedKey() throws Exception {
//        String result = dynamoDBDaoMap.remove(keyValue);
        
        Mockito.verify(requestMock).withTableName(tableName);
        
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<HashMap> hashMapCaptor = ArgumentCaptor.forClass(HashMap.class);
        Mockito.verify(requestMock).withKey(hashMapCaptor.capture());
        
        HashMap<String, AttributeValue> key = hashMapCaptor.getValue();
        assertThat(key.get(keyField), equalTo(new AttributeValue(keyValue)));
        assertThat(key.size(), equalTo(1));
        
        Mockito.verify(clientMock).deleteItem(requestMock);
        
//        assertThat(result, equalTo(expectedResult));
    }
}
