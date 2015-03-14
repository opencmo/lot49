package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.Map;

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
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.UserAttributesCacheConfig;
import com.enremmeta.rtb.config.UserSegmentsCacheConfig;
import com.enremmeta.rtb.dao.DaoMapOfUserAttributes;
import com.enremmeta.rtb.dao.DaoMapOfUserSegments;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_initCaches {
    // should initialize caches
    
    private Map<String, DbService> dbServices = new HashMap<String, DbService>();
    private Lot49Config lot49ConfigMock = Mockito.mock(Lot49Config.class);
    private ServiceRunner serviceRunnerMock;
    private String uaDb = "UserAttributesCacheConfig.MapDb";
    private String usDb = "UserSegmentsCacheConfig.SegmentsDb";
    
    private AdCache adCacheMock = Mockito.mock(AdCache.class);
    private DaoMapOfUserAttributes userAttributesCacheServiceMock = Mockito.mock(DaoMapOfUserAttributes.class);
    private DaoMapOfUserSegments userSegmentsCacheServiceMock = Mockito.mock(DaoMapOfUserSegments.class);

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        PowerMockito.whenNew(AdCache.class).withArguments(any()).thenReturn(adCacheMock);
        
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        serviceRunnerMock.setConfig(lot49ConfigMock);
        Whitebox.setInternalState(serviceRunnerMock, "dbServices", dbServices);
    }

    @Test
    public void positiveFlow_initializesCachesIfConfigsAreNotNull() throws Exception {
        UserAttributesCacheConfig uaConfig = new UserAttributesCacheConfig();
        uaConfig.setMapDb(uaDb);
        
        UserSegmentsCacheConfig usConfig = new UserSegmentsCacheConfig();
        usConfig.setSegmentsDb(usDb);
        
        Mockito.when(lot49ConfigMock.getUserAttributesCache()).thenReturn(uaConfig);
        Mockito.when(lot49ConfigMock.getUserSegmentsCache()).thenReturn(usConfig);

        DbService dbServiceMock = Mockito.mock(DbService.class);
        Mockito.when(dbServiceMock.getDaoMapOfUserAttributes()).thenReturn(userAttributesCacheServiceMock);
        Mockito.when(dbServiceMock.getDaoMapOfUserSegments()).thenReturn(userSegmentsCacheServiceMock);
        dbServices.put(uaDb, dbServiceMock);
        dbServices.put(usDb, dbServiceMock);

        serviceRunnerMock.initCaches(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.init("Implementation of UserAttributesCache used: " + userAttributesCacheServiceMock.getClass());

        PowerMockito.verifyStatic();
        LogUtils.init("Implementation of UserSegmentsCache used: " + userSegmentsCacheServiceMock.getClass());

        assertThat(serviceRunnerMock.getAdCache(), equalTo(adCacheMock));
        assertThat(serviceRunnerMock.getUserAttributesCacheService(), equalTo(userAttributesCacheServiceMock));
        assertThat(serviceRunnerMock.getUserSegmentsCacheService(), equalTo(userSegmentsCacheServiceMock));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_initializesCachesIfConfigsAreNull() throws Lot49Exception {
        // TODO in ServiceRunner.initCaches(): add check of equality to null for uaConfig and usConfig
        UserAttributesCacheConfig uaConfig = null;
        UserSegmentsCacheConfig usConfig = null;
        
        Mockito.when(lot49ConfigMock.getUserAttributesCache()).thenReturn(uaConfig);
        Mockito.when(lot49ConfigMock.getUserSegmentsCache()).thenReturn(usConfig);

        serviceRunnerMock.initCaches(); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.init("UserAttributes cache is not configured.");

        PowerMockito.verifyStatic();
        LogUtils.init("UserSegments cache is not configured.");

        assertThat(serviceRunnerMock.getAdCache(), equalTo(adCacheMock));
        assertThat(serviceRunnerMock.getUserAttributesCacheService(), equalTo(null));
        assertThat(serviceRunnerMock.getUserSegmentsCacheService(), equalTo(null));
    }
}
