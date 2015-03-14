package com.enremmeta.rtb.dao.impl.dynamodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.nio.AmazonDynamoDBClient;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.DynamoDBServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, DynamoDBService.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*", "javax.net.ssl.*"})
public class DynamoDBServiceSpec_init {
    private ServiceRunner serviceRunnerSimpleMock;
    
    @Before
    public void setUp() {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        
        ExecutorService testExecutor = Executors.newSingleThreadExecutor();
        Mockito.when(serviceRunnerSimpleMock.getExecutor()).thenReturn(testExecutor);
    }

    @Test
    public void testWrongDBConfig() throws Lot49Exception {
        DynamoDBService ddbs = new DynamoDBService();
        
        try{
        ddbs.init(new RedisServiceConfig());
        fail("Should throw exception!");
        }catch(Lot49Exception e){
            assertEquals("Expected 'DynamoDBServiceConfig', got class com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig", e.getMessage());
        }
        
    }
    
    @Test
    public void testNoENV_DYNAMO_ENDPOINT() throws Lot49Exception {
        DynamoDBService ddbs = new DynamoDBService();
        
        try{
        ddbs.init(new DynamoDBServiceConfig());
        fail("Should throw exception!");
        }catch(Lot49Exception e){
            assertEquals("Expected a 'LOT49_DYNAMODB_ENDPOINT' environment variable", e.getMessage());
        }
        
    }
    
    @Test
    public void testShouldSetTableName() throws Exception {
        DynamoDBService ddbs = new DynamoDBService();
        
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv())
                        .thenReturn(new HashMap<String, String>(){{
                           put(KVKeysValues.ENV_DYNAMO_ENDPOINT, 
                                           "TEST_DYNAMO_ENDPOINT"); 
                        }});
        
        PowerMockito.whenNew(AmazonDynamoDBClient.class).withAnyArguments()
            .thenReturn(Mockito.mock(AmazonDynamoDBClient.class));
        
        
        ddbs.init(new DynamoDBServiceConfig(){{
            setTable("TEST_TABLE");
        }});
        
        assertEquals("TEST_TABLE", ddbs.getTableName());
    }
    
    @Test
    public void testAwsAccessKeyNotNull() throws Exception {
        DynamoDBService ddbs = new DynamoDBService();
        
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv())
                        .thenReturn(new HashMap<String, String>(){{
                           put(KVKeysValues.ENV_DYNAMO_ENDPOINT, 
                                           "TEST_DYNAMO_ENDPOINT"); 
                        }});
        
        PowerMockito.whenNew(AmazonDynamoDBClient.class).withAnyArguments()
            .thenReturn(Mockito.mock(AmazonDynamoDBClient.class));
        
        
        ddbs.init(new DynamoDBServiceConfig(){{
            setTable("TEST_TABLE");
            setAwsAccessKey("AWS_A_K");
            setAwsSecretKey("AWS_S_K");
        }});
        
        assertEquals("TEST_TABLE", ddbs.getTableName());
    }

}
