package com.enremmeta.rtb.regression_tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.caches.AdCacheSpec_Refresh;
import com.enremmeta.rtb.config.AdCacheConfig;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.config.PacingServiceConfig;
import com.enremmeta.rtb.dao.impl.redis.RedisServiceConfig;
import com.enremmeta.util.ServiceRunner;
import com.lambdaworks.redis.RedisConnection;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RedisConnection.class, ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ODD_536 {
    
    private AdCacheConfig config;
    private ServiceRunner serviceRunnerSimpleMock;
    private Lot49Config configMock;
    AdCacheSpec_Refresh spec;

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
    public void AdCache_Refresh_shouldNot_duplicateOptoutLogEntries() throws Exception {
        spec = new AdCacheSpec_Refresh();
        spec.setConfig(config);
        spec.setServiceRunnerSimpleMock(serviceRunnerSimpleMock);
        spec.setConfigMock(configMock);
        spec.folder = this.folder;
        
        AdCacheSpec_Refresh.AdData ad1 = spec.new AdData("78");
        ad1.test1Requests = 1;
        ad1.test2Requests = 3;
        ad1.test2CategoryOptouts = 1;
        ad1.test2PacingOptouts = 1;
        ad1.test2BidAmount = 20;

        AdCacheSpec_Refresh.AdData ad2 = spec.new AdData("78");
        ad2.shiftLoadedOn = 60 * 1000; /// after one minute

        AdCache adCache = spec.createAdCache();
        String resultMessages1 = spec.testRefresh(adCache, ad1);
        String resultMessages2 = spec.testRefresh(adCache, ad2);
        
        assertThat(resultMessages1, not(containsString("Decision:")));
        spec.commonAssertions(resultMessages1, 1, 0);
        
        String testOptoutSample = "Decision: Sanity check: test1: 0=0: FAIL!";
        assertThat(resultMessages2, containsString(testOptoutSample));
        assertEquals(1, StringUtils.countMatches(resultMessages2, testOptoutSample));
    }

}
