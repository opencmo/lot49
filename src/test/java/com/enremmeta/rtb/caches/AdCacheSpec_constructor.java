package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.lambdaworks.redis.RedisClient;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_constructor {
    private AdCacheConfig adCacheConfig;
    private RedisClient redisClientMock;
    private AdCache adCache;

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        redisClientMock = SharedSetUp.createRedisClientMock();
    }

    @Test(expected = Lot49Exception.class)
    public void negativeFlow_throwsExceptionIfMessageTtlMinutesIsNotPositive() throws Lot49Exception {
        adCacheConfig.getPacing().setMessageTtlMinutes(0);
        
        adCache = new AdCache(adCacheConfig);
    }

    @Test
    public void positiveFlow_initializesNecessaryFieldsIfAllConfigSataAreCorrect() throws Lot49Exception {
        int ttlMinutes = 10;
        adCacheConfig.setTtlMinutes(ttlMinutes);
        
        adCache = new AdCache(adCacheConfig);
        
        assertThat(Whitebox.getInternalState(adCache, "config"), equalTo(adCacheConfig));
        assertThat(Whitebox.getInternalState(adCache, "ttlMinutes"), equalTo(ttlMinutes));
        assertThat(Whitebox.getInternalState(adCache, "client"), equalTo(redisClientMock));
    }
}
