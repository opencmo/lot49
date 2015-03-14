package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.config.AerospikeDBServiceConfig;
import com.enremmeta.rtb.config.DbConfig;
import com.enremmeta.rtb.config.DbConfigs;
import com.enremmeta.rtb.config.DynamoDBServiceConfig;
import com.enremmeta.rtb.config.HazelcastServiceConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.rtb.dao.impl.aerospike.AerospikeDBService;
import com.enremmeta.rtb.dao.impl.dynamodb.DynamoDBService;
import com.enremmeta.rtb.dao.impl.hazelcast.HazelcastService;
import com.enremmeta.rtb.dao.impl.redis.RedisService;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initDbServices {
    // should create and initialize database services and add them to the map 'dbServices' 
    
    private DbConfigs dbConfigs = new DbConfigs();
    private Map<String, DbService> dbServices = new HashMap<String, DbService>();
    private Lot49Config lot49ConfigMock;
    private ServiceRunner serviceRunnerMock;

    private Map<String, RedisServiceConfig> redises = new HashMap<String, RedisServiceConfig>();
    private Map<String, DynamoDBServiceConfig> dynamodbs = new HashMap<String, DynamoDBServiceConfig>();
    private Map<String, AerospikeDBServiceConfig> aerospikes = new HashMap<String, AerospikeDBServiceConfig>();
    private Map<String, HazelcastServiceConfig> hazelcasts = new HashMap<String, HazelcastServiceConfig>();
    
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        dbConfigs.setRedises(redises);
        dbConfigs.setDynamos(dynamodbs);
        dbConfigs.setAerospikes(aerospikes);
        dbConfigs.setHazelcasts(hazelcasts);
        
        lot49ConfigMock = Mockito.mock(Lot49Config.class);
        Mockito.when(lot49ConfigMock.getDatabases()).thenReturn(dbConfigs);
        
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        serviceRunnerMock.setConfig(lot49ConfigMock);
        Whitebox.setInternalState(serviceRunnerMock, "dbServices", dbServices);
        Whitebox.setInternalState(serviceRunnerMock, "scheduledExecutor", Mockito.mock(ScheduledExecutorService.class));
        
        PowerMockito.whenNew(RedisService.class).withNoArguments().thenReturn(Mockito.mock(RedisService.class));
        PowerMockito.whenNew(DynamoDBService.class).withNoArguments().thenReturn(Mockito.mock(DynamoDBService.class));
        PowerMockito.whenNew(AerospikeDBService.class).withNoArguments().thenReturn(Mockito.mock(AerospikeDBService.class));
        PowerMockito.whenNew(HazelcastService.class).withArguments(eq(serviceRunnerMock), isA(HazelcastServiceConfig.class))
            .thenReturn(Mockito.mock(HazelcastService.class));
    }
    
    private <T extends DbConfig> T createDbConfig(Class<T> dbConfigClass, boolean isEnabled) throws Exception {
        T dbConfig = dbConfigClass.newInstance();
        dbConfig.setEnabled(isEnabled);
        
        return dbConfig;
    }
    
    @Test
    public void positiveFlow_addsDbServiceIfItIsEnabled() throws Exception {
        redises.put("RedisServiceEnabled", createDbConfig(RedisServiceConfig.class, true));
        dynamodbs.put("DynamoDBServiceEnabled", createDbConfig(DynamoDBServiceConfig.class, true));
        aerospikes.put("AerospikeDBServiceEnabled", createDbConfig(AerospikeDBServiceConfig.class, true));
        hazelcasts.put("HazelcastServiceEnabled", createDbConfig(HazelcastServiceConfig.class, true));
        
        assertThat(dbServices.size(), equalTo(0));
        
        serviceRunnerMock.initDbServices();
        
        PowerMockito.verifyNew(RedisService.class).withNoArguments();
        PowerMockito.verifyNew(DynamoDBService.class).withNoArguments();
        PowerMockito.verifyNew(AerospikeDBService.class).withNoArguments();
        PowerMockito.verifyNew(HazelcastService.class).withArguments(eq(serviceRunnerMock), isA(HazelcastServiceConfig.class));
        
        assertThat(dbServices.size(), equalTo(5));
        assertThat(dbServices.get(ServiceRunner.COLLECTIONS_DB_SERVICE), not(equalTo(null)));
    }

    @Test
    public void negativeFlow_doesNotAddDbServiceIfItIsNotEnabled() throws Exception {
        redises.put("RedisServiceDisabled", createDbConfig(RedisServiceConfig.class, false));
        dynamodbs.put("DynamoDBServiceDisabled", createDbConfig(DynamoDBServiceConfig.class, false));
        aerospikes.put("AerospikeDBServiceDisabled", createDbConfig(AerospikeDBServiceConfig.class, false));
        hazelcasts.put("HazelcastServiceDisabled", createDbConfig(HazelcastServiceConfig.class, false));
        
        assertThat(dbServices.size(), equalTo(0));
        
        serviceRunnerMock.initDbServices();
        
        verifyLogUtilsInit("Service RedisServiceDisabled not enabled");
        verifyLogUtilsInit("Service DynamoDBServiceDisabled not enabled");
        verifyLogUtilsInit("Service AerospikeDBServiceDisabled not enabled");
        verifyLogUtilsInit("Service HazelcastServiceDisabled not enabled");
        
        assertThat(dbServices.size(), equalTo(1));
        assertThat(dbServices.get(ServiceRunner.COLLECTIONS_DB_SERVICE), not(equalTo(null)));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_doesNotCreateDbServiceIfItIsNotEnabled() throws Exception {
        // TODO in ServiceRunner.initDbServices(): database service should not be created if it is not enabled
        redises.put("RedisServiceDisabled", createDbConfig(RedisServiceConfig.class, false));
        dynamodbs.put("DynamoDBServiceDisabled", createDbConfig(DynamoDBServiceConfig.class, false));
        aerospikes.put("AerospikeDBServiceDisabled", createDbConfig(AerospikeDBServiceConfig.class, false));
        hazelcasts.put("HazelcastServiceDisabled", createDbConfig(HazelcastServiceConfig.class, false));
        
        assertThat(dbServices.size(), equalTo(0));
        
        serviceRunnerMock.initDbServices();
        
        PowerMockito.verifyNew(RedisService.class, times(0)).withNoArguments();
        PowerMockito.verifyNew(DynamoDBService.class, times(0)).withNoArguments();
        PowerMockito.verifyNew(AerospikeDBService.class, times(0)).withNoArguments();
        PowerMockito.verifyNew(HazelcastService.class, times(0)).withArguments(eq(serviceRunnerMock), isA(HazelcastServiceConfig.class));
        
        assertThat(dbServices.size(), equalTo(1));
        assertThat(dbServices.get(ServiceRunner.COLLECTIONS_DB_SERVICE), not(equalTo(null)));
    }
    
    private void verifyLogUtilsInit(String msg) {
        PowerMockito.verifyStatic();
        LogUtils.init(contains(msg));
    }
}
