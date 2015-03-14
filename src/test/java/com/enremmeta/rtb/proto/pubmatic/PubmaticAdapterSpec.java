package com.enremmeta.rtb.proto.pubmatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.BidInFlightInfo;
import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.Lot49Exception;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.caches.AdCache;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.dao.impl.collections.CollectionsShortLivedMap;
import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceRunner.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class PubmaticAdapterSpec {
    private ServiceRunner serviceRunnerSimpleMock;
    private ExchangesConfig exchangesConfigMock;
    private PubmaticConfig pmCfg;

    @Before
    public void setUp() throws Throwable {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        pmCfg = new PubmaticConfig();

        Mockito.when(configMock.getExchanges()).thenAnswer(new Answer<ExchangesConfig>() {
            public ExchangesConfig answer(InvocationOnMock invocation) {

                exchangesConfigMock = Mockito.mock(ExchangesConfig.class);
                Mockito.when(exchangesConfigMock.getPubmatic()).thenReturn(pmCfg);
                return exchangesConfigMock;
            }
        });

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
    }

    @Test
    public void constructorNegativeFlow_vcodeWasNotProvided() {

        try {
            new PubmaticAdapter();
            fail("Exception expected but not thrown!");
        } catch (Lot49Exception e) {
            assertEquals("Missing required configuration field 'vcode'.", e.getMessage());
        }

    }

    @Test
    public void constructorNegativeFlow_vcodeWasEmpty() {

        pmCfg.setVcode("  ");

        try {
            new PubmaticAdapter();
            fail("Exception expected but not thrown!");
        } catch (Lot49Exception e) {
            assertEquals("Missing required configuration field 'vcode'.", e.getMessage());
        }

    }

    @Test
    public void constructorPositiveFlow_shouldSetPubmaticConfig() throws Lot49Exception {

        pmCfg.setVcode("VCODE");

        PubmaticAdapter adapter = new PubmaticAdapter();

        Mockito.verify(exchangesConfigMock, Mockito.times(1)).getPubmatic();

        assertEquals("VCODE", Whitebox.getInternalState(adapter, "vcode"));

    }

    @Test
    public void convertRequestPositiveFlow_shouldSetAdapter() throws Throwable {

        pmCfg.setVcode("VCODE");
        PubmaticAdapter adapter = new PubmaticAdapter();

        OpenRtbRequest req = new OpenRtbRequest();

        OpenRtbRequest req2 = adapter.convertRequest(req);

        assertEquals(adapter, req2.getLot49Ext().getAdapter());

    }

    @Test
    public void convertRequestPositiveFlow_shouldSetBuyerId() throws Throwable {

        pmCfg.setVcode("VCODE");
        PubmaticAdapter adapter = new PubmaticAdapter();

        OpenRtbRequest req = new OpenRtbRequest();
        User user = new User();
        user.setBuyeruid("TEST_BUYER_ID");
        req.setUser(user);

        OpenRtbRequest req2 = adapter.convertRequest(req);

        assertEquals("TEST_BUYER_ID", req2.getLot49Ext().getReceivedBuyerUid());

    }

    @Test
    public void convertRequestNegativeFlow_noUserInRequest() throws Throwable {

        pmCfg.setVcode("VCODE");
        PubmaticAdapter adapter = new PubmaticAdapter();

        OpenRtbRequest req = new OpenRtbRequest();

        OpenRtbRequest req2 = adapter.convertRequest(req);

        assertNull(req2.getLot49Ext().getReceivedBuyerUid());

    }

    
    @Test
    public void parseWinLossInfoNegativeFlow_quietDie() throws Throwable {

        pmCfg.setVcode("VCODE");

        PubmaticAdapter adapter = new PubmaticAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.error(anyString());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.info(anyString());
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.info(anyString());
        
        Whitebox.invokeMethod(adapter, "parseWinLossInfo", req);

        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.trace(anyString());
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.info(anyString());
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.error(anyString());
    }
    
    @Test
    public void parseWinLossInfoNegativeFlow_WLI_WIN_throwsNPE() throws Throwable {

        pmCfg.setVcode("VCODE");

        PubmaticAdapter adapter = new PubmaticAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setExt(new HashMap(){{
            put("wli", new HashMap(){{
                put("rId", "TEST_RID");
                put("bCur", "TEST_BCUR");
                put("wBid", new Double(10000.0));
                put("csa", new Integer(10));
                put("bInfo", new LinkedList(){{
                    add(new HashMap(){{
                        put("bId", "TEST_B_ID");
                        put("st", PubmaticWinLossStatus.WIN.getCode());
                    }});
                }});
            }});
        }});

        try{
        
            Whitebox.invokeMethod(adapter, "parseWinLossInfo", req);
            fail("throws NPE in current implementation");
        }catch(NullPointerException e){}

       
    }
    
    @Test
    public void parseWinLossInfoPositiveFlow_WLI_TIMEOUT() throws Throwable {
        
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);
        
        AdCache adcMock = PowerMockito.mock(AdCache.class);
        Mockito.when(adcMock.getBidInFlightInfoMap())
            .thenReturn(new  CollectionsShortLivedMap<BidInFlightInfo>());
        Mockito.when(serviceRunnerSimpleMock.getAdCache())
                        .thenReturn(adcMock);

        pmCfg.setVcode("VCODE");

        PubmaticAdapter adapter = new PubmaticAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setExt(new HashMap(){{
            put("wli", new HashMap(){{
                put("rId", "TEST_RID");
                put("bCur", "TEST_BCUR");
                put("wBid", new Double(10000.0));
                put("csa", new Integer(10));
                put("bInfo", new LinkedList(){{
                    add(new HashMap(){{
                        put("bId", "TEST_B_ID");
                        put("st", PubmaticWinLossStatus.TIMEOUT.getCode());
                    }});
                }});
            }});
        }});

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logLost(any(), any(), any(), any(), any(),
                        any(), any(), any(),
                        any(), any(), any(), any(),
                        any(), any(), any(), any());
        
        Whitebox.invokeMethod(adapter, "parseWinLossInfo", req);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logLost(any(), any(), any(), any(), any(),
                        any(), any(), any(),
                        any(), any(), any(), any(),
                        any(), any(), any(), any());

       
    }
    
    @Test
    public void parseWinLossInfoPositiveFlow_WLI_OUTBID() throws Throwable {
        
        LocalOrchestrator lo = new LocalOrchestrator(new OrchestratorConfig());
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator()).thenReturn(lo);
        
        AdCache adcMock = PowerMockito.mock(AdCache.class);
        Mockito.when(adcMock.getBidInFlightInfoMap())
            .thenReturn(new  CollectionsShortLivedMap<BidInFlightInfo>());
        Mockito.when(serviceRunnerSimpleMock.getAdCache())
                        .thenReturn(adcMock);

        pmCfg.setVcode("VCODE");

        PubmaticAdapter adapter = new PubmaticAdapter();
        
        OpenRtbRequest req = new OpenRtbRequest();
        req.setExt(new HashMap(){{
            put("wli", new HashMap(){{
                put("rId", "TEST_RID");
                put("bCur", "TEST_BCUR");
                put("wBid", new Double(10000.0));
                put("csa", new Integer(10));
                put("bInfo", new LinkedList(){{
                    add(new HashMap(){{
                        put("bId", "TEST_B_ID");
                        put("st", PubmaticWinLossStatus.OUTBID.getCode());
                    }});
                }});
            }});
        }});

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doCallRealMethod().when(LogUtils.class);
        LogUtils.logLost(any(), any(), any(), any(), any(),
                        any(), any(), any(),
                        any(), any(), any(), any(),
                        any(), any(), any(), any());
        
        Whitebox.invokeMethod(adapter, "parseWinLossInfo", req);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.logLost(any(), any(), any(), any(), any(),
                        any(), any(), any(),
                        any(), any(), any(), any(),
                        any(), any(), any(), any());

    }   

    @Test
    public void test_PubmaticJsonParsing() {
        try {
            File bidswitchDir = getTestDataFile("pubmatic");
            for (File json : bidswitchDir.listFiles()) {
                if (json.getName().endsWith(".json")) {
                    // Just ensure things parse correctly
                    // and check some fields which should be filled with data
                    OpenRtbRequest req = Utils.MAPPER.readValue(json, OpenRtbRequest.class);
                    assertNotNull(req);
                    assertNotNull(req.getId());
                    assertNotNull(req.getUser());
                    assertNotNull(req.getImp());
                    assertFalse(req.getImp().isEmpty());
                    Impression impression = req.getImp().get(0);
                    assertNotNull(impression);
                    assertNotNull(impression.getId());
                    assertNotNull(req.getDevice());
                    assertNotNull(req.getSite());
                    assertNotNull(req.getLot49Ext());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void test_BidswitchAdapterConvertRequest() throws Throwable {
        pmCfg.setVcode("VCODE");
        PubmaticAdapter adapter = new PubmaticAdapter();
        File pubmaticRequest = getTestDataFile("pubmatic/display1.json");
        try {
            OpenRtbRequest req = Utils.MAPPER.readValue(pubmaticRequest, OpenRtbRequest.class);
            assertNotNull(req);
            OpenRtbRequest convertedRequest = adapter.convertRequest(req);
            assertNotNull(convertedRequest);
            assertNotNull(convertedRequest.getLot49Ext().getAdapter());

            assertNotNull(req.getId());
            assertNotNull(convertedRequest.getId());
            assertEquals(req.getId(), convertedRequest.getId());

            assertNotNull(req.getImp());
            assertNotNull(convertedRequest.getImp());
            assertFalse(convertedRequest.getImp().isEmpty());
            assertEquals(req.getImp().size(), convertedRequest.getImp().size());

            assertEquals(req.getImp().get(0).getTagid(),
                            convertedRequest.getImp().get(0).getTagid());
            assertEquals(req.getImp().get(0).getId(), convertedRequest.getImp().get(0).getId());
            assertEquals(req.getImp().get(0).getBidfloor(),
                            convertedRequest.getImp().get(0).getBidfloor());
            assertEquals(req.getImp().get(0).getBidfloorcur(),
                            convertedRequest.getImp().get(0).getBidfloorcur());

            assertNotNull(req.getUser());
            assertNotNull(convertedRequest.getUser());
            assertEquals(req.getUser().getBuyeruid(), convertedRequest.getUser().getBuyeruid());
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    protected File getTestDataFile(String name) {
        String cwd = System.getProperty("user.dir");
        File f = new File(cwd, "src/test/resources/data/" + name);
        return f;
    }
}
