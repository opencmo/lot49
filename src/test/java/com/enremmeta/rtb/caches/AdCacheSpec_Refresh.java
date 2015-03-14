package com.enremmeta.rtb.caches;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.KVKeysValues;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ClientConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.config.SecurityManagerConfig;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.dao.DaoCounters;
import com.enremmeta.rtb.dao.impl.redis.RedisDaoCounters;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.rtb.sandbox.SandboxSecurityManager;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RedisConnection.class, ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AdCacheSpec_Refresh {
    private AdCacheConfig config;
    private ServiceRunner serviceRunnerSimpleMock;
    private Lot49Config configMock;

    
    
    public void setConfig(AdCacheConfig config) {
        this.config = config;
    }
    

    public void setServiceRunnerSimpleMock(ServiceRunner serviceRunnerSimpleMock) {
        this.serviceRunnerSimpleMock = serviceRunnerSimpleMock;
    }
    

    public void setConfigMock(Lot49Config configMock) {
        this.configMock = configMock;
    }
    

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);
        Mockito.when(configMock.getStatsUrl()).thenReturn("http://stats.url");

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        config = new AdCacheConfig();

        RedisServiceConfig redisServiceConfig = new RedisServiceConfig();
        redisServiceConfig.setHost("221.34.157.44");
        redisServiceConfig.setPort(3000);

        config.setPacing(new PacingServiceConfig());
        config.getPacing().setRedis(redisServiceConfig);

    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void negativeFlow_noBidPriceCalculatorsInDir() throws Lot49Exception, IOException {

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        AdCache adc = new AdCache(config);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());

        @SuppressWarnings("unchecked")
        String result = adc.refresh(Mockito.mock(RedisConnection.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.contains("AdCache: No Bid Price Calculators found in"));

    }
    
    @Test
    public void negativeFlow_noAdsInAdDir() throws Lot49Exception, IOException {

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        AdCache adc = new AdCache(config);

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File bpcFile = new File(groovyPkgDir,
                        Lot49Constants.BID_PRICE_CALCULATOR_FILENAME_PREFIX + "123_TEST" + ".groovy");
        bpcFile.createNewFile();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.info(anyString());

        @SuppressWarnings("unchecked")
        String result = adc.refresh(Mockito.mock(RedisConnection.class));

        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.info(Mockito.contains("AdCache: Refreshed ad cache, loaded 0 ads"));

    }

    String AD_CONFIG_SCRIPT_PACKAGE = "com.enremmeta.rtb.groovy.tc";
    String AD_CONFIG_SCRIPT_SUBDIR = AD_CONFIG_SCRIPT_PACKAGE.replaceAll("\\.", "/");

    @Test
    public void negativeFlow_adFileNameNeedsNumber() throws Lot49Exception, IOException {

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File adFile = new File(groovyPkgDir,
                        Lot49Constants.AD_FILENAME_PREFIX + "TEST" + ".groovy");
        adFile.createNewFile();

        AdCache adc = new AdCache(config);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), Mockito.any());
        
        @SuppressWarnings("unchecked")
        String result = adc.refresh(Mockito.mock(RedisConnection.class));
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.error(Mockito.contains("AdCache: Unexpected error in AdCache.loadAdsFromDir(): java.lang.NumberFormatException: For input string: \"TEST.groovy\""),
                        Mockito.any(NumberFormatException.class));
    }
    
    @Test
    public void negativeFlow_adFileNameIsEmpty() throws Lot49Exception, IOException {

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File adFile = new File(groovyPkgDir,
                        Lot49Constants.AD_FILENAME_PREFIX + "123_TEST" + ".groovy");
        adFile.createNewFile();
        File bpcFile = new File(groovyPkgDir,
                        Lot49Constants.BID_PRICE_CALCULATOR_FILENAME_PREFIX + "123_TEST" + ".groovy");
        bpcFile.createNewFile();

        AdCache adc = new AdCache(config);

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString(), Mockito.any());
        
        @SuppressWarnings("unchecked")
        String result = adc.refresh(Mockito.mock(RedisConnection.class));
        
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(Mockito.contains("AdCache: Unexpected error in AdCache.refresh(): java.lang.NumberFormatException: For input string: \"TEST.groovy\""),
                        Mockito.any(NumberFormatException.class));

        assertTrue(result.contains("Setting status from 'Nothing-to-run' to 'Nothing-to-run'"));
        assertTrue(result.contains(
                        "Refreshed ad cache, loaded 0 ads with no user info needed, and 0 with user needed."));
    }

    @Test
    public void negativeFlow_cannotAllocateMoney() throws Exception {

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());
        
        config.setValidateBudgetBeforeLoadingAd(true);

        config.setValidateBudgetBeforeLoadingAd(true);

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File adFile = new File(groovyPkgDir,
                        Lot49Constants.AD_FILENAME_PREFIX + "123_TEST" + ".groovy");
        adFile.createNewFile();

        FileWriter writer = new FileWriter(adFile);
        writer.write(SharedSetUp.AD_GROOVE_SAMPLE);
        writer.close();

        AdCache adc = new AdCache(config);

        @SuppressWarnings("unchecked")
        String result = adc.refresh(Mockito.mock(RedisConnection.class));

        assertTrue(result.contains("Eligible ad count: 0"));
        assertTrue(result.contains(
                        "Refreshed ad cache, loaded 0 ads with no user info needed, and 0 with user needed."));
    }


    @Test
    public void positiveFlow_loadedOneAd() throws Exception {

        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);

        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        Mockito.doReturn(sandboxSecurityManager).when(serviceRunnerSimpleMock).getSandboxSecurityManager();

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File adFile = new File(groovyPkgDir,
                        Lot49Constants.AD_FILENAME_PREFIX + "78_TEST" + ".groovy");
        adFile.createNewFile();

        FileWriter writer = new FileWriter(adFile);
        writer.write(SharedSetUp.AD_GROOVE_SAMPLE);
        writer.close();

        AdCache adc = new AdCache(config);
        Whitebox.setInternalState(adc, "ttlMinutes", 1000);
        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + 78)).thenReturn(1000L);
        Whitebox.setInternalState(adc, "winRateCounters", rdc);

        RedisConnection rCon = Mockito.mock(RedisConnection.class);
        Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + 78)).thenReturn("10000000");

        @SuppressWarnings("unchecked")
        String result = adc.refresh(rCon);

        assertTrue(result.contains("Eligible ad count: 1"));
        assertTrue(result.contains(
                        "Refreshed ad cache, loaded 1 ads with no user info needed, and 0 with user needed."));
    }

    @Test
    public void positiveFlow_2consecutiveRuns() throws Exception {

        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);

        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        Mockito.doReturn(sandboxSecurityManager).when(serviceRunnerSimpleMock).getSandboxSecurityManager();

        folder.newFile("wrote.txt");

        config.setDir(folder.getRoot().getAbsolutePath());

        File groovySrcDir = folder.newFolder("src");;

        groovySrcDir.setWritable(true);
        groovySrcDir.setReadable(true);

        File groovyPkgDir = new File(groovySrcDir, AD_CONFIG_SCRIPT_SUBDIR).getAbsoluteFile();

        Path pathToFile = Paths.get(groovyPkgDir.getAbsolutePath());
        Files.createDirectories(pathToFile);
        File adFile = new File(groovyPkgDir,
                        Lot49Constants.AD_FILENAME_PREFIX + "78_TEST" + ".groovy");
        adFile.createNewFile();

        FileWriter writer = new FileWriter(adFile);
        writer.write(SharedSetUp.AD_GROOVE_SAMPLE);
        writer.close();

        AdCache adc = new AdCache(config);
        Whitebox.setInternalState(adc, "ttlMinutes", 1000);
        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + 78)).thenReturn(1000L);
        Whitebox.setInternalState(adc, "winRateCounters", rdc);

        RedisConnection rCon = Mockito.mock(RedisConnection.class);
        Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + 78)).thenReturn("10000000");

        @SuppressWarnings("unchecked")
        String result = adc.refresh(rCon); // first run

        @SuppressWarnings("unchecked") // second run
        String result2 = adc.refresh(rCon);

        // fail(result2);
        assertTrue(result.contains("Eligible ad count: 1"));
        assertTrue(result.contains(
                        "Refreshed ad cache, loaded 1 ads with no user info needed, and 0 with user needed."));
        assertTrue(result.contains("New all: [Ad 78]"));
        assertTrue(result.contains("Old all: []"));
        assertTrue(result2.contains("Setting status from 'Nothing-to-run' to 'Nothing-to-run'"));
        assertTrue(result2.contains(
                        "Refreshed ad cache, loaded 0 ads with no user info needed, and 0 with user needed."));
        assertTrue(result2.contains("New all: []"));
        assertTrue(result2.contains("Old all: [Ad 78]"));
    }

    // additional tests
    
    private int ttlMinutes = 1000;
    private String wroteFile = "wrote.txt";
    
    public class AdData {
        private final String defaultAdId = "78";

        public String adId;
        public String scriptSubdir = null;
        public long budget = 300000;
        public long wins = 0;
        public long spendAmount = 0;
        
        public boolean needCanBid2 = false;
        public boolean noName = false;
        public String nodeId = null;
        public int bidProbability = 100;
        public long shiftLoadedOn = 0;
        
        public boolean redisConZError = false;
        public boolean timeout = false;
        public boolean cancelStoreAdState = false;
        
        public int test1Requests = 0;
        public int test2Requests = 0;
        public int test2CategoryOptouts = 0;
        public int test2PacingOptouts = 0;
        public int test2BidAmount = 0;
        
        public AdData(String adId) {
            if (adId == null || adId.isEmpty()) {
                throw new IllegalArgumentException("Ad id cannot be null or empty");
            }
            
            this.adId = adId;
        }
        
        public String getAdBody() {
            String adBody = SharedSetUp.AD_GROOVE_SAMPLE;
            
            if (!adId.equals(defaultAdId)) {
                adBody = adBody.replace("Ad_" + defaultAdId, "Ad_" + adId);
                adBody = adBody.replace("tags = [\nnew com.enremmeta.rtb.Tag_178_MYIP_78_testMyIPnewAdservice2(this)]", "");
            }
            
            adBody = adBody.replace("void init() {\n", 
                            "List<String> validate() { return null; }\n" +
                            "boolean needCanBid2() { return " + needCanBid2 + "; }\n" +
                            (shiftLoadedOn != 0 ? "long getLoadedOn() { return super.getLoadedOn() + " + shiftLoadedOn + "; }\n" : "") +
                            (cancelStoreAdState ? "void storeAdState() { }\n" : "") +
                            
                            "void init() {\n" +
                            (wins > 0 ? "incrWins(" + wins + ")\n" : "") +
                            (spendAmount > 0 ? "incrSpendAmount(" + spendAmount + ")\n" : "") +
                            (bidProbability != 100 ? "setBidProbability(" + bidProbability + ");\n" : "") +
                            (noName ? "name = null;\n" : "") +
                            (nodeId != null ? "nodeId = \"" + nodeId + "\";\n" : "") +
                            
                            (test1Requests > 0 ? "incrRequestCount(Lot49Constants.EXCHANGE_TEST1, " + test1Requests + ");\n" : "") +
                            (test2Requests > 0 ? "incrRequestCount(Lot49Constants.EXCHANGE_TEST2, " + test2Requests + ");\n" : "") +
                            (test2CategoryOptouts > 0 ? "getOptoutsByExchange().get(Lot49Constants.EXCHANGE_TEST2)" + 
                                            ".get(Lot49Constants.DECISION_CATEGORY).set(" +  test2CategoryOptouts + ");\n" : "") +
                            (test2PacingOptouts > 0 ? "getOptoutsByExchange().get(Lot49Constants.EXCHANGE_TEST2)" + 
                                            ".get(Lot49Constants.DECISION_PACING).set(" +  test2PacingOptouts + ");\n" : "") +
                            (test2BidAmount > 0 ? "incrBids(Lot49Constants.EXCHANGE_TEST2, " + test2BidAmount + ");\n" : "")
                            );

            return adBody;
        }
    }
    
    public AdCache createAdCache() throws Lot49Exception, IOException {
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.doReturn(lo).when(serviceRunnerSimpleMock).getOrchestrator();
        
        SandboxSecurityManager sandboxSecurityManager = new SandboxSecurityManager(new SecurityManagerConfig());
        Mockito.doReturn(sandboxSecurityManager).when(serviceRunnerSimpleMock).getSandboxSecurityManager();

        File wf = new File(folder.getRoot(), wroteFile);
        if (!wf.exists()) { wf.createNewFile(); }

        config.setDir(folder.getRoot().getAbsolutePath());
        config.setTtlMinutes(ttlMinutes);
        AdCache adCache = new AdCache(config);
        
        return adCache;
    }
    
    private ClientConfig createClientConfig(String packageName) {
        ClientConfig clientConfig = new ClientConfig();

        clientConfig.setPackageName(packageName);
        clientConfig.setDir(folder.getRoot().getAbsolutePath());

        return clientConfig;
    }


    private RedisDaoCounters createRedisDaoCountersMock(List<AdData> ads) throws Lot49Exception {
        RedisDaoCounters rdc = Mockito.mock(RedisDaoCounters.class);
        
        if (ads != null) {
            for (AdData ad : ads) {
                if (ad.wins != 0) { Mockito.when(rdc.get(KVKeysValues.WIN_COUNT_PREFIX + ad.adId)).thenReturn(ad.wins); }
            }
        }
        
        return rdc;
    }

    @SuppressWarnings("unchecked")
    private RedisConnection<String, String> createRedisConnectionMock(List<AdData> ads) {
        RedisConnection<String, String> rCon = Mockito.mock(RedisConnection.class);
        
        if (ads != null) {
            for (AdData ad : ads) {
                if (ad.budget != 0) {
                    String budgetStr = ad.budget == Long.MAX_VALUE ? KVKeysValues.BUDGET_UNLIMITED : String.valueOf(ad.budget);
                    Mockito.when(rCon.get(KVKeysValues.BUDGET_PREFIX + ad.adId)).thenReturn(budgetStr);
                }
            
                if (ad.redisConZError) {
                    Mockito.when(rCon.zremrangebyscore(eq(KVKeysValues.PACING_LOG_PREFIX + ad.adId), eq("-inf"), anyString())).thenThrow(new RuntimeException());
                    Mockito.when(rCon.zadd(eq(KVKeysValues.PACING_LOG_PREFIX + ad.adId), anyLong(), anyString())).thenThrow(new RuntimeException());
                }
            }
        }
        
        return rCon;
    }
    
    @Test
    public void negativeFlow_redisConnectionIsNull() throws Lot49Exception {
        RedisConnection<String, String> redisConnection = null;
        
        config.setDir(folder.getRoot().getAbsolutePath());
        AdCache adCache = new AdCache(config);
        
        String resultMessage = adCache.refresh(redisConnection);
        
        assertThat(resultMessage, containsString("Unexpectedly Redis connection is null"));
        assertThat(resultMessage, not(containsString("Entering refresh()")));
    }
    
    @Ignore("Slow - lasts more than 6 seconds")
    @SuppressWarnings("unchecked")
    @Test
    public void negativeFlow_wroteFileDoesNotExist() throws Exception {
        RedisConnection<String, String> redisConnection = Mockito.mock(RedisConnection.class);
        File wf = new File(folder.getRoot(), wroteFile);
        
        config.setDir(folder.getRoot().getAbsolutePath());
        AdCache adCache = new AdCache(config);
        
        String resultMessage = adCache.refresh(redisConnection);
        
        assertThat(resultMessage, containsString("File " + wf + " does not exist, will skip loading groovy this time around."));
        assertThat(resultMessage, containsString("File " + wf + " does not exist, will try again..."));
        assertThat(resultMessage, not(containsString("Last refresh: ")));
    }
    
    @Test
    public void positiveFlow_severalAdFiles() throws Exception {
        List<AdData> ads = new ArrayList<AdData>();
        ads.add(new AdData("78")); /// standard ad (with no user info needed)
        
        AdData ad79 = new AdData("79"); /// no money left
        ad79.budget = 0;
        ads.add(ad79);
        
        AdData ad80 = new AdData("80"); /// ad with user info needed
        ad80.needCanBid2 = true;
        ads.add(ad80);
        
        String resultMessage = testRefresh(ads);
        
        assertThat(resultMessage, containsString("New all: [Ad 78, Ad 80]"));
        commonAssertions(resultMessage, 1, 1);
    }
    
    @Test
        public void negativeFlow_badClientConfig() throws Exception {
                String clientId1 = "ClientId-1";
                ClientConfig clientConfig1 = new ClientConfig();
                
                Map<String, ClientConfig> clients = new HashMap<String, ClientConfig>();
                clients.put(clientId1, clientConfig1);
                
                Mockito.when(configMock.getClients()).thenReturn(clients);
                
                PowerMockito.mockStatic(LogUtils.class);
                PowerMockito.doNothing().when(LogUtils.class);
                LogUtils.error(anyString(), any());
                
                String resultMessage = testRefresh((List<AdData>)null);
                
                assertThat(resultMessage, containsString("Unexpected error in AdCache.refresh(): "));
            }

    @Test
    public void positiveFlow_clientAdFiles() throws Exception {
        String packageName4Client = "com.client.packageName";
        ClientConfig clientConfig1 = createClientConfig(packageName4Client);

        Map<String, ClientConfig> clients = new HashMap<String, ClientConfig>();
        clients.put(packageName4Client, clientConfig1);

        Mockito.when(configMock.getClients()).thenReturn(clients);

        String clientScriptSubdir1 = clientConfig1.getPackageName().replaceAll("\\.", "/");
        List<AdData> ads = new ArrayList<AdData>();

        AdData ad78 = new AdData("78"); /// standard client's ad (with no user info needed)
        ad78.scriptSubdir = clientScriptSubdir1;
        ads.add(ad78);

        AdData ad79 = new AdData("79"); /// client's ad with no money left
        ad79.scriptSubdir = clientScriptSubdir1;
        ad79.budget = 0;
        ads.add(ad79);

        AdData ad80 = new AdData("80"); /// client's ad with user info needed
        ad80.scriptSubdir = clientScriptSubdir1;
        ad80.needCanBid2 = true;
        ads.add(ad80);

        String resultMessage = testRefresh(ads);

        assertThat(resultMessage, containsString("New all: [Ad 78, Ad 80]"));
        commonAssertions(resultMessage, 1, 1);
    }

    @Test
    public void positiveFlow_adHasNoName() throws Exception {
        AdData ad = new AdData("79");
        ad.noName = true;
        
        String resultMessage = testRefresh(ad);
        
        assertThat(resultMessage, containsString("has no name, it's not cool."));
        commonAssertions(resultMessage, 1, 0);
    }
    
    @Test
    public void negativeFlow_notMyNodeId() throws Exception {
        AdData ad = new AdData("79");
        ad.nodeId = "notMyNodeId";
        
        String resultMessage = testRefresh(ad);
        
        String myNodeId = serviceRunnerSimpleMock.getOrchestrator().getNodeId();
        assertThat(resultMessage, containsString("I am node " + myNodeId + ", ad required " + ad.nodeId));
        
        commonAssertions(resultMessage, 0, 0);
    }
    
    @Test
    public void negativeFlow_bidProbabilityIsZero() throws Exception {
        AdData ad = new AdData("79");
        ad.bidProbability = 0;
        
        String resultMessage = testRefresh(ad);
        
        assertThat(resultMessage, containsString("has 0 probability."));
        commonAssertions(resultMessage, 0, 0);
    }
    
    @Test
    public void positiveFlow_redisConZError() throws Exception {
        AdData ad = new AdData("79");
        ad.redisConZError = true;
        
        String resultMessage = testRefresh(ad);
        
        assertThat(resultMessage, containsString("Error in ZREMRANGEBYSCORE for " + KVKeysValues.PACING_LOG_PREFIX + ad.adId));
        assertThat(resultMessage, containsString("Error in ZADD for " + KVKeysValues.PACING_LOG_PREFIX + ad.adId));
        commonAssertions(resultMessage, 1, 0);
    }
    
    @Test
    public void negativeFlow_noMoneyLeft() throws Exception {
        config.setValidateBudgetBeforeLoadingAd(false);
        
        AdData ad = new AdData("79"); /// no money left
        ad.budget = 0;
        
        String resultMessage = testRefresh(ad);
        
        assertThat(resultMessage, containsString("no money left."));
        commonAssertions(resultMessage, 0, 0);
    }
    
    @Test
    public void negativeFlow_adTimeout() throws Exception {
        AdData ad = new AdData("79");
        ad.timeout = true;
        
        String resultMessage = testRefresh(ad);
        
        assertThat(resultMessage, containsString("black listed because of timeouts."));
        commonAssertions(resultMessage, 0, 0);
    }

    @Test
    public void positiveFlow_twoConsecutiveRefresh() throws Exception {
        AdData ad1 = new AdData("78");
        AdData ad2 = new AdData("78");
        ad2.shiftLoadedOn = 60 * 1000; /// after one minute

        AdCache adCache = createAdCache();
        String resultMessages1 = testRefresh(adCache, ad1);
        String resultMessages2 = testRefresh(adCache, ad2);
        
        assertThat(resultMessages1, containsString("New all: [Ad 78]"));
        assertThat(resultMessages1, containsString("Old all: []"));
        assertThat(resultMessages1, not(containsString("Decision:")));
        commonAssertions(resultMessages1, 1, 0);
        
        assertThat(resultMessages2, containsString("New all: [Ad 78]"));
        assertThat(resultMessages2, containsString("Old all: [Ad 78]"));
        assertThat(resultMessages2, not(containsString("Decision:")));
        commonAssertions(resultMessages2, 1, 0);
    }
    
    @Test
    public void positiveFlow_twoConsecutiveRefreshWithOptouts() throws Exception {
        AdData ad1 = new AdData("78");
        ad1.test1Requests = 1;
        ad1.test2Requests = 3;
        ad1.test2CategoryOptouts = 1;
        ad1.test2PacingOptouts = 1;
        ad1.test2BidAmount = 20;

        AdData ad2 = new AdData("78");
        ad2.shiftLoadedOn = 60 * 1000; /// after one minute

        AdCache adCache = createAdCache();
        String resultMessages1 = testRefresh(adCache, ad1);
        String resultMessages2 = testRefresh(adCache, ad2);
        
        assertThat(resultMessages1, not(containsString("Decision:")));
        commonAssertions(resultMessages1, 1, 0);
        
        assertThat(resultMessages2, containsString("Decision: Sanity check: test1: 0=0: FAIL!"));
        assertThat(resultMessages2, containsString("Decision: Sanity check: 1+1=2; 3-1=2;"));
        commonAssertions(resultMessages2, 1, 0);
    }
    
    @Test
    public void positiveFlow_twoConsecutiveRefreshWithCancelStoreAdState() throws Exception {
        config.getPacing().setWinRateTtlMinutes(1);

        AdData ad1 = new AdData("78");
        ad1.wins = 10;
        ad1.test2BidAmount = 20;
        ad1.spendAmount = 500;
        ad1.cancelStoreAdState = true; /// override AdImpl.storeAdState()
        
        AdData ad2 = new AdData("78");
        ad2.shiftLoadedOn = 60 * 1000; /// after one minute

        AdCache adCache = createAdCache();
        String resultMessages1 = testRefresh(adCache, ad1);
        String resultMessages2 = testRefresh(adCache, ad2);
        
        commonAssertions(resultMessages1, 1, 0);
        
        assertThat(resultMessages2, containsString("Win rate reset to 0."));
        assertThat(resultMessages2, containsString("Adding 10 to winsCount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 1 to bidsCount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 0 to bidAmount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 0 to spendAmount_v7_78 for running total for last period: 0"));
        commonAssertions(resultMessages2, 1, 0);
    }
    
    @Test
    public void positiveFlow_twoConsecutiveRefreshWithDifferentAds() throws Exception {
        config.getPacing().setWinRateTtlMinutes(1);

        AdData ad1 = new AdData("78");
        ad1.wins = 10;
        ad1.test2BidAmount = 20;
        ad1.spendAmount = 500;
        
        AdData ad2 = new AdData("79");
        
        AdCache adCache = createAdCache();
        String resultMessages1 = testRefresh(adCache, ad1);
        String resultMessages2 = testRefresh(adCache, ad2);
        
        commonAssertions(resultMessages1, 1, 0);
        
        assertThat(resultMessages2, containsString("Win rate reset to 0."));
        assertThat(resultMessages2, containsString("Adding 10 to winsCount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 1 to bidsCount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 20 to bidAmount_v7_78: 0"));
        assertThat(resultMessages2, containsString("Adding 500 to spendAmount_v7_78 for running total for last period: 0"));
        commonAssertions(resultMessages2, 1, 0);
    }
    
    private String testRefresh(AdData ad) throws Lot49Exception, IOException {
        return testRefresh(null, ad);
    }
    
    public String testRefresh(AdCache adCache, AdData ad) throws Lot49Exception, IOException {
        List<AdData> ads = new ArrayList<AdData>(Arrays.asList(ad));
        
        return testRefresh(adCache, ads);
    }
    
    private String testRefresh(List<AdData> ads) throws Lot49Exception, IOException {
        return testRefresh(null, ads);
    }
    
    private String testRefresh(AdCache adCache, List<AdData> ads) throws Lot49Exception, IOException {
        DaoCounters daoCounters = createRedisDaoCountersMock(ads);
        RedisConnection<String, String> redisConnection = createRedisConnectionMock(ads);
        
        if (adCache == null) {
            adCache = createAdCache();
        }
        Whitebox.setInternalState(adCache, "winRateCounters", daoCounters);
        
        List<File> adFiles = new ArrayList<File>();
        if (ads != null) {
            for (AdData ad : ads) {
                File f = SharedSetUp.createTempGroovyFile(folder, 
                                ad.scriptSubdir == null ? adCache.AD_CONFIG_SCRIPT_SUBDIR : ad.scriptSubdir,  
                                                Lot49Constants.AD_FILENAME_PREFIX + ad.adId + "_test", ad.getAdBody());
                adFiles.add(f);

                if (ad.timeout) { adCache.getTimeoutMap().put(ad.adId, 1L); }
            }
        }
        
        String resultMessage = adCache.refresh(redisConnection);
        
        for (File f : adFiles) { f.delete(); }
        
        return resultMessage;
    }
    
    public void commonAssertions(String resultMessage, int adsNoUserNeeded, int adsUserNeeded) {
        assertThat(resultMessage, containsString("Eligible ad count: " + (adsNoUserNeeded + adsUserNeeded)));
        assertThat(resultMessage, containsString("Refreshed ad cache, loaded " + adsNoUserNeeded + 
                        " ads with no user info needed, and " + adsUserNeeded + " with user needed."));
    }
}
