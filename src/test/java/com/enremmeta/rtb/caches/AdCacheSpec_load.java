package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.times;

import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
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
import com.enremmeta.rtb.ReflectionUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.ExchangeAdapterFactory;
import com.enremmeta.rtb.sandbox.EnremmetaGroovyClassLoader;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

@RunWith(PowerMockRunner.class)
@MockPolicy(SharedSetUp.class)
@PrepareForTest({LogUtils.class, Utils.class, ExchangeAdapterFactory.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_load {
    private AdCacheConfig adCacheConfig;
    private AdCache adCache;
    private ServiceRunner serviceRunnerMock;
    private EnremmetaGroovyClassLoader loader;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        serviceRunnerMock = SharedSetUp.createServiceRunnerMock();
        adCacheConfig = SharedSetUp.createAdCacheConfig();
        SharedSetUp.createRedisClientMock();

        adCache = new AdCache(adCacheConfig);
        
        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.mockStatic(Utils.class);

        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        loader = new EnremmetaGroovyClassLoader(sandboxSecurityManager);
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_returnsNullIfGroovyClassLoaderIsNull() throws Exception {
        // TODO in AdCache.load: if parameter loader == null then method AdCache.load() must log an error and return null (according to method's contract)
        loader = null;
        File file = SharedSetUp.createTempGroovyFile(tempFolder, "", "File_TEST", "");

        AdCache.LoadResultHolder loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, true);
        
        PowerMockito.verifyStatic();
        LogUtils.error(any());
        
        assertThat(loadResult, is(equalTo(null)));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_returnsNullIfGroovyFileIsNull() throws Exception {
        // TODO in AdCache.load: if parameter f == null then method AdCache.load() must log an error and return null (according to method's contract)
        File file = null;
        
        AdCache.LoadResultHolder loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, true);
        
        PowerMockito.verifyStatic();
        LogUtils.error(any());
        
        assertThat(loadResult, is(equalTo(null)));
    }

    @Test
    public void negativeFlow_returnsNullIfGroovyFileNotFound() throws Exception {
        File file = new File("Class.groovy");
        
        AdCache.LoadResultHolder loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, true);
        
        PowerMockito.verifyStatic();
        LogUtils.error(contains("Cannot find file " + file));
        
        assertThat(loadResult, is(equalTo(null)));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void negativeFlow_returnsNullIfParseErrorHappened() throws Exception {
        // TODO in AdCache.load: add try...catch block around command 'groovyClass = loader.parseClass(f);' and return null if error happened (according to method's contract)
        File file = SharedSetUp.createTempGroovyFile(tempFolder, "", "File_TEST", "{ Bad groovy code }");
        
        AdCache.LoadResultHolder loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, true);
        
        PowerMockito.verifyStatic();
        LogUtils.error(any());
        
        assertThat(loadResult, is(equalTo(null)));
    }

    @Test
    public void positiveFlow_returnsExpectedClassIfGroovyFileIsCorrect() throws Exception {
        Mockito.when(serviceRunnerMock.getConfig().getStatsUrl()).thenReturn("http://stats.url");
        PowerMockito.mockStatic(ExchangeAdapterFactory.class);
        ReflectionUtils.setFinalStatic(AdCache.class, "GROOVY_CLASSES", new HashMap<String, Class<Ad>>());

        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, 
                        Lot49Constants.AD_FILENAME_PREFIX + "TEST", SharedSetUp.AD_GROOVE_SAMPLE);
        
        AdCache.LoadResultHolder loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, false);
        
        PowerMockito.verifyStatic(times(0));
        Utils.noop();
        
        PowerMockito.verifyStatic(times(1));
        LogUtils.warn(contains("Loaded class from " + file));
        
        String expectedClass = "class com.enremmeta.rtb.groovy.tc.Ad_78_testMyIPnewAdservice2";
        assertThat(loadResult.getPluginClass().toString(), equalTo(expectedClass));
        assertThat(loadResult.getUpdated(), is(true));
    }

    @Test
    public void positiveFlow_returnsExpectedClassFromMapIfLoadCalledSecondTime() throws Exception {
        Mockito.when(serviceRunnerMock.getConfig().getStatsUrl()).thenReturn("http://stats.url");
        PowerMockito.mockStatic(ExchangeAdapterFactory.class);
        ReflectionUtils.setFinalStatic(AdCache.class, "GROOVY_CLASSES", new HashMap<String, Class<Ad>>());
        
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, 
                        Lot49Constants.AD_FILENAME_PREFIX + "TEST", SharedSetUp.AD_GROOVE_SAMPLE);
        
        Whitebox.invokeMethod(adCache, "load", loader, file, true); /// first call of AdCache.load()
        
        PowerMockito.mockStatic(LogUtils.class); /// reset the spying for LogUtils.class
        
        long initialRunNumber = (long) ReflectionUtils.getPrivateStatic(AdCache.class, "runNumber");
        ReflectionUtils.setPrivateStatic(AdCache.class, "runNumber", 2);
        
        AdCache.LoadResultHolder loadResult = null;
        
        try {
            loadResult = Whitebox.invokeMethod(adCache, "load", loader, file, false); /// second call of AdCache.load()
        } catch (Exception ex) {
            throw ex;
        } finally {
            ReflectionUtils.setPrivateStatic(AdCache.class, "runNumber", initialRunNumber); /// set initial value for field 'runNumber'
        }
        
        PowerMockito.verifyStatic(times(1));
        Utils.noop();
        
        PowerMockito.verifyStatic(times(0));
        LogUtils.warn(contains("Loaded class from " + file));
        
        String expectedClass = "class com.enremmeta.rtb.groovy.tc.Ad_78_testMyIPnewAdservice2";
        assertThat(loadResult.getPluginClass().toString(), equalTo(expectedClass));
        assertThat(loadResult.getUpdated(), is(false));
    }

    @Ignore("The test will be successful, if make steps that described in the comments below")
    @Test
    public void positiveFlow_freesUsedGroovyFile() throws Exception {
        // TODO in AdCache.load(): need to close FileInputStream
        File file = SharedSetUp.createTempGroovyFile(tempFolder, adCache.AD_CONFIG_SCRIPT_SUBDIR, 
                        Lot49Constants.AD_FILENAME_PREFIX + "TEST", SharedSetUp.AD_GROOVE_SAMPLE);
        
        Whitebox.invokeMethod(adCache, "load", loader, file, true);
        
        assertThat(file.exists(), is(true));
        file.delete();
        assertThat(file.exists(), is(false));
    }
}
