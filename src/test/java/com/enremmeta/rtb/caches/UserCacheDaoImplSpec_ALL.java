package com.enremmeta.rtb.caches;

import java.util.Set;
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

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.UserCacheConfig;
import com.enremmeta.rtb.dao.DaoMap;
import com.enremmeta.rtb.dao.DbService;
import com.enremmeta.util.ServiceRunner;

@Ignore("UserCacheDaoImpl was removed")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceRunner.class, Bidder.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class UserCacheDaoImplSpec_ALL {
    @SuppressWarnings("unchecked")
    private DaoMap<Set<String>> segmentMap = Mockito.mock(DaoMap.class);
    private UserCacheConfig userCacheConfig;

    @Before
    public void setUp() throws Exception {
        String segmentDb = "SegmentDb";
        DbService dbSegments = Mockito.mock(DbService.class);
        //Mockito.when(dbSegments.<Set<String>>getDaoMap()).thenReturn(segmentMap);
        
        ServiceRunner bidderMock = SharedSetUp.createBidderMock();
        PowerMockito.doReturn(dbSegments).when(bidderMock).getDbServiceByName(segmentDb);
        
        userCacheConfig = new UserCacheConfig();
        userCacheConfig.setSegmentDb(segmentDb);
    }

    @Test
    public void constructor_setsFieldValues() throws Lot49Exception {
        //UserCacheDaoImpl userCacheDaoImpl = new UserCacheDaoImpl(userCacheConfig); /// act
        
        //assertThat(Whitebox.getInternalState(userCacheDaoImpl, "segmentMap"), equalTo(segmentMap));
    }

    @Test
    public void getUserInfo_returnsExpectedValue() throws Lot49Exception {
        String key = "Key";
        @SuppressWarnings("unchecked")
        Future<Set<String>> expectedResult = Mockito.mock(Future.class);
        Mockito.when(segmentMap.getAsync(key)).thenReturn(expectedResult);
        
        //UserCacheDaoImpl userCacheDaoImpl = new UserCacheDaoImpl(userCacheConfig);
        
        PowerMockito.mockStatic(LogUtils.class);
        
        //Future<Set<String>> result = userCacheDaoImpl.getUserInfo(key); /// act
        
        PowerMockito.verifyStatic();
        LogUtils.trace("Calling " + segmentMap + ".getAsync(" + key + ")");

        //assertThat(result, equalTo(expectedResult));
    }
}
