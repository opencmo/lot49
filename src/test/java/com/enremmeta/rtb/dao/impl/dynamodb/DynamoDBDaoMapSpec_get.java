package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;

@Ignore("DynamoDBDaoMap was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBDaoMapSpec_get {
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
        String expectedResult = "Expected value";
        Mockito.when(futureMock.get()).thenReturn(expectedResult);
        
        //String result = dynamoDBDaoMap.get(keyValue);
        
        //assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void negativeFlow_returnsNullIfThrowsInterruptedException() throws Exception {
        Mockito.doThrow(InterruptedException.class).when(futureMock).get();
        
        //String result = dynamoDBDaoMap.get(keyValue);
        Thread.interrupted(); /// to clear interrupted status 
        
        //assertThat(result, equalTo(null));
    }

    @Test
    public void negativeFlow_returnsNullIfThrowsExecutionException() throws Exception {
        Mockito.doThrow(ExecutionException.class).when(futureMock).get();
        PowerMockito.mockStatic(LogUtils.class);
        
        //String result = dynamoDBDaoMap.get(keyValue);
        
        PowerMockito.verifyStatic();
        LogUtils.error(eq("Error getting " + keyValue), isA(ExecutionException.class));

        //assertThat(result, equalTo(null));
    }
}
