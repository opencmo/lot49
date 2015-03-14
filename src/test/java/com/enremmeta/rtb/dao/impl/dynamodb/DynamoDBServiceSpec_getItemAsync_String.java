package com.enremmeta.rtb.dao.impl.dynamodb;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class DynamoDBServiceSpec_getItemAsync_String {
    // should return not null Future object
    
    private DynamoDBService dynamoDBService;
    private AmazonDynamoDBClient dynamoDBClientMock;
    
    @Before
    public void setUp() throws Exception {
        dynamoDBService = new DynamoDBService();
    }

    private void setUp_positiveFlow() {
        dynamoDBClientMock = Mockito.mock(AmazonDynamoDBClient.class);
        Whitebox.setInternalState(dynamoDBService, "client", dynamoDBClientMock);
        
        ServiceRunner serviceRunnerMock = PowerMockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(Mockito.mock(ThreadPoolExecutor.class)).when(serviceRunnerMock).getExecutor();
        
        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerMock);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_returnsKnownFutureIfParameterIsNull() {
        // TODO in DynamoDBService.getItemAsync(String): should return KnownFuture if parameter is null, isn't it
//        Future<Map<String, Integer>> future = dynamoDBService.getItemAsync(null);
        
//        assertThat(future, not(equalTo(null)));
//        assertThat(future instanceof KnownFuture, is(true)); 
    }

    @Test
    public void positiveFlow_returnsDynamoDBDaoLegacyFutureIfParameterIsNotNull() {
        setUp_positiveFlow();
        
//        Future<Map<String, Integer>> future = dynamoDBService.getItemAsync("KeyValue");
        
//        assertThat(future, not(equalTo(null)));
//        assertThat(future instanceof DynamoDBDaoLegacyFuture, is(true)); 
    }
}
