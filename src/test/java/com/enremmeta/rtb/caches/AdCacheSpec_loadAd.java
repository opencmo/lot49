package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class, ExchangeAdapterFactory.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_loadAd {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private ServiceRunner serviceRunnerMock;
    
    private EnremmetaGroovyClassLoader loader; 
    private File groovyPkgDir;
    private String adFileName = "Ad_78_testMyIPnewAdservice2";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();
        
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        Mockito.when(serviceRunnerMock.getConfig().getStatsUrl()).thenReturn("http://stats.url");
        PowerMockito.mockStatic(ExchangeAdapterFactory.class);
        PowerMockito.mockStatic(LogUtils.class);
        
        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        loader = new EnremmetaGroovyClassLoader(sandboxSecurityManager);

        adCache = new AdCache(adCacheConfig);
        
        File groovySrcDir = new File(tempFolder.getRoot(), "src");
        groovyPkgDir = new File(groovySrcDir, adCache.AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();
    }

    @Test
    public void negativeFlow_returnsNullIfGroovyFileNotFound() throws Exception {
        groovyPkgDir.mkdirs();
        File file = new File(adFileName + ".groovy");

        Ad loadedAd = Whitebox.invokeMethod(adCache, "loadAd", groovyPkgDir, loader, file);
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.debug(contains("Instantiated object in "));
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.warn(contains("Error loading " + file), any());
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.logAdLoadingError(eq(0L), eq(file), anyString());
        
        assertThat(loadedAd, equalTo(null));
    }

    @Test
    public void negativeFlow_returnsNullIfLoadedAdIsNotValid() throws Exception {
        String invalidAd = SharedSetUp.AD_GROOVE_SAMPLE.replace("adomain = [\"myip.io\"]", "adomain = [\"bad domain 1\", \"bad domain 2\"]");
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, adFileName, invalidAd);
        
        Ad loadedAd = Whitebox.invokeMethod(adCache, "loadAd", groovyPkgDir, loader, file);
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.debug(contains("Instantiated object in "));
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.warn(contains("Errors in " + file));
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.logDecision(eq(0L), any(), eq(null), eq(Lot49Constants.DECISION_VALIDATION), eq(null), eq(null), anyString());
        
        assertThat(loadedAd, equalTo(null));
    }

    @Test
    public void negativeFlow_returnsNullIfValidateCausesException() throws Exception {
        String invalidAd = SharedSetUp.AD_GROOVE_SAMPLE.replace("void init() {", 
                        "@Override public List<String> validate() { throw new RuntimeException(); }\n" + "void init() {");
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, adFileName, invalidAd);
        
        Ad loadedAd = Whitebox.invokeMethod(adCache, "loadAd", groovyPkgDir, loader, file);
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.debug(contains("Instantiated object in "));
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.debug(contains("Loaded ad " + loadedAd + " from " + file));
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.logDecision(eq(0L), any(), eq(null), eq(Lot49Constants.DECISION_LOADING_ERROR), eq(null), eq(null), anyString());
        
        assertThat(loadedAd, equalTo(null));
    }

    @Test
    public void positiveFlow_returnsExpectedAdIfGroovyFileIsCorrect() throws Exception {
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, adFileName, SharedSetUp.AD_GROOVE_SAMPLE);
        
        Ad loadedAd = Whitebox.invokeMethod(adCache, "loadAd", groovyPkgDir, loader, file);
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.debug(contains("Instantiated object in "));
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.debug(contains("Loaded ad " + loadedAd + " from " + file));
        
        String expectedAdClass = "class com.enremmeta.rtb.groovy.tc.Ad_78_testMyIPnewAdservice2";
        assertThat(loadedAd.getClass().toString(), equalTo(expectedAdClass));
    }
}
