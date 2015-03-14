package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_refreshOwnerKeys {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private RedisConnection<String, String> redisConnectionMock;
    
    private String owner = "Owner";
    private byte[] key = new byte[] { 1, 2, 3 };

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        adCache = new AdCache(adCacheConfig);
        
        redisConnectionMock = Mockito.mock(RedisConnection.class);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Test
    public void negativeFlow_DoesNotLogInfoAboutKeyIfKeyNotFound() throws Exception {
        String msg = Whitebox.invokeMethod(adCache, "refreshOwnerKeys", redisConnectionMock);
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.info(contains("Key for " + owner + ": "));
        
        assertThat(msg.contains("Refreshing owner keys"), is(true));
    }

    @Test
    public void positiveFlow_LogsInfoAboutKeyIfKeyFound() throws Exception {
        // TODO in AdCache.refreshOwnerKeys(): map with refreshed keys (local variable 'newKeys') cannot be used in the future
        List<String> keys = new ArrayList<>();
        keys.add(KVKeysValues.OWNER_KEY_PREFIX + owner);
        
        byte[] encodedKey = Base64.encodeBase64(key);
        String encodedKeyString = new String(encodedKey);
        
        Mockito.when(redisConnectionMock.keys(KVKeysValues.OWNER_KEY_PREFIX + "*")).thenReturn(keys);
        Mockito.when(redisConnectionMock.get(KVKeysValues.OWNER_KEY_PREFIX + owner)).thenReturn(encodedKeyString);
        
        String msg = Whitebox.invokeMethod(adCache, "refreshOwnerKeys", redisConnectionMock);
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.info(contains("Key for " + owner + ": " + encodedKeyString));
        
        assertThat(msg.contains("Refreshing owner keys"), is(true));
    }
}
