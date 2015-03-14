package com.enremmeta.rtb.dao.impl.dynamodb;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamoDBService.class, 
//    DynamoDBDaoMap.class
    })
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_replace {
    private DynamoDBService dynamoDBService;
//    private DynamoDBDaoMap<String> dynamoDBDaoMap;
    private Table tableMock;
    private UpdateItemSpec updateItemSpecSpy;
    
    private String keyField = "KeyField";
    private String keyValue = "KeyValue";
    private String newObjectAttributeValue = "NewObjectAttributeValue";
    
    @Before
    public void setUp() throws Exception {
        updateItemSpecSpy = Mockito.spy(new UpdateItemSpec()); 
        PowerMockito.whenNew(UpdateItemSpec.class).withNoArguments().thenReturn(updateItemSpecSpy);
        
        tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.updateItem(updateItemSpecSpy)).thenReturn(Mockito.mock(UpdateItemOutcome.class));
        
        dynamoDBService = PowerMockito.mock(DynamoDBService.class);
        //PowerMockito.when(dynamoDBService.getTable()).thenReturn(tableMock);
        PowerMockito.when(dynamoDBService.getKeyField()).thenReturn(keyField);
//        PowerMockito.when(dynamoDBService.convert(any(Item.class))).thenReturn(newObjectAttributeValue);
        
//        dynamoDBDaoMap = new DynamoDBDaoMap<String>(dynamoDBService);
    }

    @Test
    public void updatesItemForTheSpecifiedKeyByNewObjectAttributeValue() throws Exception {
 //       String result = dynamoDBDaoMap.replace(keyValue, newObjectAttributeValue);
        
        Mockito.verify(updateItemSpecSpy).withPrimaryKey(keyField, keyValue);
 //       Mockito.verify(updateItemSpecSpy).withUpdateExpression("SET " + DynamoDBService.DYN_DB_PREFIX_OBJECT_ATTR + " = :val");
        Mockito.verify(updateItemSpecSpy).withValueMap(new ValueMap().with(":val", newObjectAttributeValue));
        
        Mockito.verify(tableMock).updateItem(updateItemSpecSpy);
        
 //       assertThat(result, equalTo(newObjectAttributeValue));
    }
}
