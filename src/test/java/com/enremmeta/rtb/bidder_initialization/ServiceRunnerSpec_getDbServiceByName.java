package com.enremmeta.rtb.bidder_initialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;

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
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.rtb.dao.impl.collections.CollectionsDbService;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ServiceRunnerSpec_getDbServiceByName {
    // should return requested database service by its name
    
    private Map<String, DbService> dbServices = new HashMap<String, DbService>();
    private ServiceRunner serviceRunnerMock;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LogUtils.class);
        
        serviceRunnerMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);
        Whitebox.setInternalState(serviceRunnerMock, "dbServices", dbServices);
    }

    @Test
    public void negativeFlow_returnsNullIfDbServiceIsNotInitialized() {
        DbService dbService = serviceRunnerMock.getDbServiceByName(ServiceRunner.COLLECTIONS_DB_SERVICE);
        
        assertThat(dbService, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsRequestedDbServiceIfItIsInitialized() {
        CollectionsDbService collectionsDbService = new CollectionsDbService();
        dbServices.put(ServiceRunner.COLLECTIONS_DB_SERVICE, collectionsDbService);
        
        DbService dbService = serviceRunnerMock.getDbServiceByName(ServiceRunner.COLLECTIONS_DB_SERVICE);
        
        assertThat(dbService, is(collectionsDbService));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_writesCorrectRequesterToLog() {
        // TODO in ServiceRunner.getDbServiceByName(): wrong direction stack bypass in the for-loop
        CollectionsDbService collectionsDbService = new CollectionsDbService();
        dbServices.put(ServiceRunner.COLLECTIONS_DB_SERVICE, collectionsDbService);
        
        serviceRunnerMock.getDbServiceByName(ServiceRunner.COLLECTIONS_DB_SERVICE);
        
        String currentMethod = getClass().getName() + "." + "positiveFlow_writesCorrectRequesterToLog";
        PowerMockito.verifyStatic();
        LogUtils.init(contains(currentMethod));
    }
}
