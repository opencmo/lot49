package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.times;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_loadTag {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private AdCache adCacheSpy;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();

        adCache = new AdCache(adCacheConfig);
        adCacheSpy = PowerMockito.spy(adCache);
        
        PowerMockito.mockStatic(LogUtils.class);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_warnsAboutErrorIfGroovyFileIsNull() throws Exception {
        // TODO in AdCache.loadTag(): add 'resultHolder = new LoadResultHolder();' or 'return false;' in the catch-block
        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        EnremmetaGroovyClassLoader loader = new EnremmetaGroovyClassLoader(sandboxSecurityManager);
        File file = null;
        
        Whitebox.invokeMethod(adCacheSpy, "loadTag", loader, file);
        
        PowerMockito.verifyPrivate(adCacheSpy).invoke("load", loader, file, false);
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.warn(contains("Error loading " + file), any());
    }

    @Test
    public void positiveFlow_loadsTagWithoutWarningIfGroovyFileIsCorrect() throws Exception {
        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        EnremmetaGroovyClassLoader loader = new EnremmetaGroovyClassLoader(sandboxSecurityManager);
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, 
                        Lot49Constants.TAG_FILENAME_PREFIX + "TEST", SharedSetUp.TAG_GROOVE_SAMPLE);
        
        Boolean updated = Whitebox.invokeMethod(adCacheSpy, "loadTag", loader, file);
        
        PowerMockito.verifyPrivate(adCacheSpy).invoke("load", loader, file, false);
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.warn(contains("Error loading " + file), any());
        
        assertThat(updated, is(true));
    }
}
