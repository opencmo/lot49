package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_setBudget {
    private AdCacheConfig adCacheConfig;
    private RedisClient redisClientMock;
    private RedisConnection<String, String> redisConnectionMock;
    private AdCache adCache;

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        redisClientMock = SharedSetUp.createRedisClientMock();
        redisConnectionMock = redisClientMock.connect();

        adCache = new AdCache(adCacheConfig);
    }

    @Test(expected = Lot49Exception.class)
    public void negativeFlow_throwsExceptionIfRedisConnectionIsNull() throws Lot49Exception {
        String adId = "78";
        long amount = 1000L;
        
        PowerMockito.doReturn(null).when(redisClientMock).connect();
        
        adCache.setBudget(adId, amount);
    }

    @Test
    public void positiveFlow_returnsExpectedResult() throws Lot49Exception {
        String adId = "78";
        long amount = 1000L;
        String expectedResult = "OK";
        
        Mockito.when(redisConnectionMock.set(KVKeysValues.BUDGET_PREFIX + adId, String.valueOf(amount))).thenReturn(expectedResult);
        
        String result = adCache.setBudget(adId, amount);
        
        Mockito.verify(redisConnectionMock).close();
        assertThat(result, is(expectedResult));
    }
}
