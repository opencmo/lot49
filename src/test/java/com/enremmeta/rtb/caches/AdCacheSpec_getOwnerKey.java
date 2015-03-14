package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_getOwnerKey {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    
    private String owner = "Owner";
    private byte[] key = new byte[] { 1, 2, 3 };

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        adCache = new AdCache(adCacheConfig);
    }

    @Test
    public void negativeFlow_returnsNullIfMapOwnerKeysIsNull() {
        byte[] returnKey = adCache.getOwnerKey(owner);
        
        assertThat(returnKey, equalTo(null));
    }

    @Test
    public void negativeFlow_returnsNullIfKeyNotFound() {
        Map<String, byte[]> ownerKeys = new HashMap<>();
        Whitebox.setInternalState(adCache, "ownerKeys", ownerKeys);
        
        byte[] returnKey = adCache.getOwnerKey(owner);
        
        assertThat(returnKey, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsKeyIfKeyFound() {
        Map<String, byte[]> ownerKeys = new HashMap<>();
        ownerKeys.put(owner, key);
        
        Whitebox.setInternalState(adCache, "ownerKeys", ownerKeys);
        
        byte[] returnKey = adCache.getOwnerKey(owner);
        
        assertThat(returnKey, equalTo(key));
    }
}
