package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
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
public class AdCacheSpec_setStatus {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        adCache = new AdCache(adCacheConfig);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_returnsExpectedResult() throws Exception {
        // TODO in AdCache.setStatus(): old status should be present in the returned value
        String oldStatus = Whitebox.getInternalState(adCache, "status");
        String newStatus = "Nothing-to-run";
        
        String message = Whitebox.invokeMethod(adCache, "setStatus", newStatus);
        
        String expectedMessage = "Setting status from '" + oldStatus + "' to '" + newStatus + "'";
        assertThat(message, containsString(expectedMessage));
    }
}
