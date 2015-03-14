package com.enremmeta.rtb.config;

import java.util.HashMap;

import org.junit.Test;

import com.enremmeta.rtb.BeanTestHelper;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.spi.providers.integral.IntegralConfig;
import com.enremmeta.rtb.spi.providers.maxmind.MaxMindConfig;

public class Lot49ConfigSpec {

    @Test
    public void testSettersAndGetters() throws Exception {
        Lot49Config config = new Lot49Config();
        
        byte[] logKey = {(byte)10, (byte)13};
        BeanTestHelper.testSetAndGet(config, "setLogKey", "getLogKey", logKey);
        
        BeanTestHelper.testSetAndGet(config, "setSegmentToOwnerCodecClassname", "getSegmentToOwnerCodecClassname", "segmentToOwnerCodecClassname");
        
        BeanTestHelper.testSetAndGet(config, "setRemoveTagOnFirstNurlRequest", "isRemoveTagOnFirstNurlRequest", true);
        
        BeanTestHelper.testSetAndGet(config, "setDnsCacheTtlSeconds", "getDnsCacheTtlSeconds", 1000L);
        
        BeanTestHelper.testSetAndGet(config, "setFavicon", "getFavicon", "Favicon");
        
        BeanTestHelper.testSetAndGet(config, "setTimeoutThresholdMillis", "getTimeoutThresholdMillis", 1000L);
        
        BeanTestHelper.testSetAndGet(config, "setClients", "getClients", new HashMap<String, ClientConfig>());
        
        BeanTestHelper.testSetAndGet(config, "setProviders", "getProviders", new HashMap<String, ClientConfig>());
        
        BeanTestHelper.testSetAndGet(config, "setOrchestrator", "getOrchestrator", new OrchestratorConfig());
        
        BeanTestHelper.testSetAndGet(config, "setDatabases", "getDatabases", new DbConfigs());
        
        BeanTestHelper.testSetAndGet(config, "setJettyAcceptQueueSize", "getJettyAcceptQueueSize", 111);
        
        BeanTestHelper.testSetAndGet(config, "setMaxCookieAge", "getMaxCookieAge", 100);
        
        BeanTestHelper.testSetAndGet(config, "setContainer", "getContainer", "TEST_CONTAINER");
        
        BeanTestHelper.testSetAndGet(config, "setContainer", "getContainer", "TEST_CONTAINER");
        
        BeanTestHelper.testSetAndGet(config, "setStatsUrl", "getStatsUrl", "TEST_STATUS_URL");
        
        BeanTestHelper.testSetAndGet(config, "setDeploy", "getDeploy", "TEST_DEPLOY");
        
        BeanTestHelper.testSetAndGet(config, "setUserCache", "getUserCache", new UserCacheConfig());
        
        BeanTestHelper.testSetAndGet(config, "setHazelcast", "getHazelcast", new HazelcastServiceConfig());
        
        BeanTestHelper.testSetAndGet(config, "setAdmin", "getAdmin", new AdminConfig());
        
        BeanTestHelper.testSetAndGet(config, "setContainer", "getContainer", "TEST_CONTAINER");
        
        BeanTestHelper.testSetAndGet(config, "setContainer", "getContainer", "TEST_CONTAINER");
        
        BeanTestHelper.testSetAndGet(config, "setAdCache", "getAdCache", new AdCacheConfig());
        
        BeanTestHelper.testSetAndGet(config, "setAdConfigScriptDir", "getAdConfigScriptDir", "adConfigScriptDir");
        
        BeanTestHelper.testSetAndGet(config, "setBaseMicroputTrackingUrl", "getBaseMicroputTrackingUrl", "baseMicroputTrackingUrl");
        
        BeanTestHelper.testSetAndGet(config, "setBaseUrl", "getBaseUrl", "baseUrl");
        
        BeanTestHelper.testSetAndGet(config, "setExecutorThreadPoolSize", "getExecutorThreadPoolSize", 100);
        
        BeanTestHelper.testSetAndGet(config, "setHost", "getHost", "host");
        
        BeanTestHelper.testSetAndGet(config, "setKeepAliveTimeoutSeconds", "getKeepAliveTimeoutSeconds", 100);
        
        BeanTestHelper.testSetAndGet(config, "setMaxMind", "getMaxMind", new MaxMindConfig());
        
        BeanTestHelper.testSetAndGet(config, "setPort", "getPort", 5555);
        
        BeanTestHelper.testSetAndGet(config, "setWinTimeoutSeconds", "getWinTimeoutSeconds", 1000L);
        
        BeanTestHelper.testSetAndGet(config, "setDebug", "isDebug", true);
        
        BeanTestHelper.testSetAndGet(config, "setJettyTracing", "isJettyTracing", true);
        
        BeanTestHelper.testSetAndGet(config, "setMagic", "getMagic", new MagicConfig());
        
        BeanTestHelper.testSetAndGet(config, "setSecurityManagerConfig", "getSecurityManagerConfig", new SecurityManagerConfig());
        
        BeanTestHelper.testSetAndGet(config, "setExperimentDataTtlDays", "getExperimentDataTtlDays", 555);
        
        BeanTestHelper.testSetAndGet(config, "setIntegral", "getIntegral", new IntegralConfig());
        
        BeanTestHelper.testSetAndGet(config, "setUserAttributesCache", "getUserAttributesCache", new UserAttributesCacheConfig());
        
        BeanTestHelper.testSetAndGet(config, "setApps", "getApps", new HashMap<String, AppConfig>());
        
        BeanTestHelper.testSetAndGet(config, "setTimeoutBidProcessing", "isTimeoutBidProcessing", true);
        
        BeanTestHelper.testSetAndGet(config, "setUserIdCookie", "getUserIdCookie", "TEST_userIdCookie");
        
        BeanTestHelper.testSetAndGet(config, "setCookieDomain", "getCookieDomain", "TEST_setCookieDomain");
        
        BeanTestHelper.testSetAndGet(config, "setBaseUrl", "getBaseUrl", "TEST_setBaseUrl");
        
        BeanTestHelper.testSetAndGet(config, "setTimeoutBidProcessing", "isTimeoutBidProcessing", true);
        
    }
 

}
