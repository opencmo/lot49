package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_getAsync {
    private DynamoDBService dynamoDBService;
    //private DynamoDBDaoMap<String> dynamoDBDaoMap;
    private String keyValue = "KeyValue";
    
    @SuppressWarnings("unchecked")
    private Future<String> futureMock = Mockito.mock(Future.class);

    @Before
    public void setUp() throws Exception {
        dynamoDBService = Mockito.mock(DynamoDBService.class);
        //Mockito.when(dynamoDBService.<String>getItemAsync(keyValue)).thenReturn(futureMock);
        
        //dynamoDBDaoMap = new DynamoDBDaoMap<String>(dynamoDBService);
    }

    @Test
    public void positiveFlow_returnsExpectedValueIfAllRight() throws Exception {
        //Future<String> result = dynamoDBDaoMap.getAsync(keyValue);
        
        //assertThat(result, equalTo(futureMock));
    }
}
