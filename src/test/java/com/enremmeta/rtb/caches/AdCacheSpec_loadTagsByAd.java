package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
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

import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;

import groovy.lang.GroovyClassLoader;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({AdCache.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_loadTagsByAd {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private File groovyPkgDir;
    private GroovyClassLoader loader;
    
    private String adId = "78";
    private String adFileName = "Ad_78_testMyIPnewAdservice2";
    private String tagFileName = "Tag_178_MYIP_78_testMyIPnewAdservice2";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();

        adCache = new AdCache(adCacheConfig);
        
        File groovySrcDir = new File(tempFolder.getRoot(), "src");
        groovyPkgDir = new File(groovySrcDir, adCache.AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();
        
        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        loader = new EnremmetaGroovyClassLoader(sandboxSecurityManager);
    }

    @Test(expected = NullPointerException.class)
    public void negativeFlow_throwsExceptionIfGroovyPkgDirDoesNotExist() throws Exception {
        Whitebox.invokeMethod(adCache, "loadTagsByAd", groovyPkgDir, loader, adFileName + ".groovy", adId);
    }

    @Test
    public void positiveFlow_returnsFalseIfNoTagFileExists() throws Exception {
        groovyPkgDir.mkdirs();
        
        Boolean updated = Whitebox.invokeMethod(adCache, "loadTagsByAd", groovyPkgDir, loader, adFileName + ".groovy", adId);
        
        assertThat(updated, is(false));
    }

    @Test
    public void positiveFlow_invokesLoadTagIfTagFileExists() throws Exception {
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, tagFileName, SharedSetUp.TAG_GROOVE_SAMPLE);
        AdCache adCacheSpy = PowerMockito.spy(adCache);
        
        Boolean updated = Whitebox.invokeMethod(adCacheSpy, "loadTagsByAd", groovyPkgDir, loader, adFileName + ".groovy", adId);

        PowerMockito.verifyPrivate(adCacheSpy).invoke("loadTag", loader, file);
        
        assertThat(updated, is(true));
    }
}
