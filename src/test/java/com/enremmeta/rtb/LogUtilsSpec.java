package com.enremmeta.rtb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.enremmeta.rtb.api.AdImpl;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.TagImpl;
import com.enremmeta.rtb.api.UserExperimentAttributes;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Deal;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.api.proto.openrtb.PMP;
import com.enremmeta.rtb.api.proto.openrtb.SeatBid;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49Ext;
import com.enremmeta.rtb.api.proto.openrtb.lot49.Lot49ExtRemote;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.openx.OpenXAdapter;
import com.enremmeta.util.ServiceRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({LogManager.class, ServiceRunner.class})
public class LogUtilsSpec {
    private static Logger logMock;
    private ServiceRunner serviceRunnerSimpleMock;

    @BeforeClass
    public static void staticSetUp() {
        logMock = Mockito.mock(Logger.class);
        PowerMockito.mockStatic(LogManager.class);
        PowerMockito.when(LogManager.getLogger(Mockito.anyString())).thenReturn(logMock);
    }

    @Before
    public void setUp() {
        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);
        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);

        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);

        OrchestratorConfig orchConfig = new OrchestratorConfig();

        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(new LocalOrchestrator(orchConfig));
    }

    @Test
    public void testLogAdLoadingError() {

        long runNumber = 0;
        LogUtils.logAdLoadingError(runNumber, null, "TEST_COMMENT");

        Mockito.verify(logMock, Mockito.times(1)).info(Mockito.eq(""), Mockito.anyString(),
                        Mockito.anyLong(), Mockito.eq(0L), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq("01.01.Ad loading error"), Mockito.eq("M1"), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq("TEST_COMMENT"), Mockito.eq("M2"),
                        Mockito.any(), Mockito.eq("local"));

    }

    @Test
    public void testLogTagDecision() {

        Tag tagMock = Mockito.mock(TagImpl.class);
        Mockito.when(tagMock.getId()).thenReturn("TEST_TAG_ID");
        AdImpl adMock = Mockito.mock(AdImpl.class);
        Mockito.when(adMock.getId()).thenReturn("TEST_AD_ID");
        Mockito.when(tagMock.getAd()).thenReturn(adMock);


        long runNumber = 0;
        LogUtils.logTagDecision(runNumber, tagMock, "xch", "type", 1000L, 1010101010L);

        Mockito.verify(logMock, Mockito.times(1)).info(Mockito.eq(""), Mockito.anyString(),
                        Mockito.anyLong(), Mockito.eq(0L), Mockito.eq("TEST_AD_ID"),
                        Mockito.eq("TEST_TAG_ID"), Mockito.eq("xch"), Mockito.eq("type"),
                        Mockito.eq(1000L), Mockito.eq(1010101010L), Mockito.any(),
                        Mockito.eq("local"));

    }

    @Test
    public void testLogExperimentCampaign() {

        OpenRtbRequest reqMock = Mockito.mock(OpenRtbRequest.class);
        Mockito.when(reqMock.getId()).thenReturn("TEST_REQ_ID");
        Mockito.when(reqMock.getUser()).thenReturn(new User());
        Lot49Ext ext = new Lot49Ext();
        ext.setLot49ExtRemote(new Lot49ExtRemote());
        Mockito.when(reqMock.getLot49Ext()).thenReturn(ext);
        AdImpl adMock = Mockito.mock(AdImpl.class);
        Mockito.when(adMock.getId()).thenReturn("TEST_AD_ID");
        Mockito.when(adMock.getCampaignId()).thenReturn("AD_C_ID");

        UserExperimentAttributes ueAttrib =
                        new UserExperimentAttributes(new HashMap<String, String>() {
                            {
                                put("c_AD_C_ID", "TestStatusForCampaign");
                            }
                        });

        LogUtils.logExperimentCampaign(reqMock, "modUid", adMock, ueAttrib);

        Mockito.verify(logMock, Mockito.times(1)).info(Mockito.eq(""), Mockito.anyString(),
                        Mockito.anyLong(), Mockito.eq("TEST_REQ_ID"), Mockito.eq(null),
                        Mockito.eq("modUid"), Mockito.eq("C"), Mockito.eq("AD_C_ID"),
                        Mockito.eq("TestStatusForCampaign"), Mockito.eq("2"),
                        Mockito.eq("{c_AD_C_ID=TestStatusForCampaign}"), Mockito.eq(null), 
                        Mockito.eq("AD_C_ID"), Mockito.eq("local"));

    }

    @Test
    public void testLogExperimentTargetingStrategy() {

        OpenRtbRequest reqMock = Mockito.mock(OpenRtbRequest.class);
        Mockito.when(reqMock.getId()).thenReturn("TEST_REQ_ID");
        Mockito.when(reqMock.getUser()).thenReturn(new User());
        Lot49Ext ext = new Lot49Ext();
        ext.setLot49ExtRemote(new Lot49ExtRemote());
        Mockito.when(reqMock.getLot49Ext()).thenReturn(ext);
        AdImpl adMock = Mockito.mock(AdImpl.class);
        Mockito.when(adMock.getId()).thenReturn("TEST_AD_ID");
        Mockito.when(adMock.getCampaignId()).thenReturn("AD_C_ID");

        UserExperimentAttributes ueAttrib =
                        new UserExperimentAttributes(new HashMap<String, String>() {
                            {
                                put("c_AD_C_ID", "TestStatusForCampaign");
                            }
                        });

        LogUtils.logExperimentTargetingStrategy(reqMock, "modUid", adMock, ueAttrib);

        Mockito.verify(logMock, Mockito.times(1)).info(Mockito.eq(""), Mockito.anyString(),
                        Mockito.anyLong(), Mockito.eq("TEST_REQ_ID"), Mockito.eq(null),
                        Mockito.eq("modUid"), Mockito.eq("TS"), Mockito.eq("TEST_AD_ID"),
                        Mockito.eq(null), Mockito.eq("2"), Mockito.any(Map.class),
                        Mockito.eq(null), Mockito.eq("AD_C_ID"), Mockito.eq("local"));

    }

    @Test
    public void testLogResponse() throws Lot49Exception {

        OpenRtbRequest reqMock = Mockito.mock(OpenRtbRequest.class);
        Mockito.when(reqMock.getId()).thenReturn("TEST_REQ_ID");
        Mockito.when(reqMock.getUser()).thenReturn(new User());
        Lot49Ext ext = new Lot49Ext();
        ext.setLot49ExtRemote(new Lot49ExtRemote());

        ExchangeAdapter adapter = new OpenXAdapter();

        ext.setAdapter(adapter);
        Mockito.when(reqMock.getLot49Ext()).thenReturn(ext);


        Mockito.when(logMock.getLevel()).thenReturn(Level.DEBUG);

        LogUtils.logResponse(reqMock, new OpenRtbResponse(), "Object_Response");

        Mockito.verify(logMock, Mockito.times(1)).debug("\n"
                        + "================================================================================\n"
                        + "openx\tnull\tnull\n" + "{\"seatbid\":[],\"cur\":\"USD\"}\n"
                        + "---------------------------- EXCHANGE SPECIFIC ---------------------------------\n"
                        + "Object_Response\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "\n"
                        + "================================================================================\n");
    }

    @Test
    public void testLogResponse_notNullSeatbid() throws Lot49Exception {

        OpenRtbRequest reqMock = Mockito.mock(OpenRtbRequest.class);
        Mockito.when(reqMock.getId()).thenReturn("TEST_REQ_ID");
        Mockito.when(reqMock.getUser()).thenReturn(new User());
        Lot49Ext ext = new Lot49Ext();
        ext.setLot49ExtRemote(new Lot49ExtRemote());

        ExchangeAdapter adapter = new OpenXAdapter();

        ext.setAdapter(adapter);
        Mockito.when(reqMock.getLot49Ext()).thenReturn(ext);


        Mockito.when(logMock.getLevel()).thenReturn(Level.DEBUG);

        LogUtils.logResponse(reqMock, new OpenRtbResponse() {
            {
                setSeatbid(new LinkedList<SeatBid>() {
                    {
                        add(new SeatBid() {
                            {
                                setBid(new LinkedList<Bid>() {
                                    {
                                        add(new Bid());
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, "Object_Response");

        Mockito.verify(logMock, Mockito.times(1)).debug("\n"
                        + "================================================================================\n"
                        + "openx\tnull\tnull\n"
                        + "{\"seatbid\":[{\"bid\":[{\"price\":0.0,\"adomain\":[],\"ext\":{}}]}],\"cur\":\"USD\"}\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "Bid ID: null\n" + "SSL Required: false\n" + "Ad markup:\n" + "null\n"
                        + "NUrl: null\n" + "Tag object under NUrl:\n" + "null\n"
                        + "Tag text under NUrl:\n" + "null\n" + "\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "\n"
                        + "---------------------------- EXCHANGE SPECIFIC ---------------------------------\n"
                        + "Object_Response\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "\n"
                        + "================================================================================\n");
    }

    @Test
    public void testLogResponseV2() throws Lot49Exception {

        Mockito.when(logMock.getLevel()).thenReturn(Level.DEBUG);

        LogUtils.logResponse("brx", "ssp", "bidRequestId", "bidId", "tag");

        Mockito.verify(logMock, Mockito.times(1)).debug("\n"
                        + "================================================================================\n"
                        + "brx\tssp\tbidRequestId\n" + "\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "Bid ID: bidId\n" + "tag\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "\n"
                        + "---------------------------- EXCHANGE SPECIFIC ---------------------------------\n"
                        + "N/A\n"
                        + "--------------------------------------------------------------------------------\n"
                        + "\n"
                        + "================================================================================\n");
    }

    @Test
    public void testLogRequest() throws Lot49Exception {

        OpenRtbRequest reqMock = Mockito.mock(OpenRtbRequest.class);
        Mockito.when(reqMock.getId()).thenReturn("TEST_REQ_ID");
        Mockito.when(reqMock.getUser()).thenReturn(new User());
        Mockito.when(reqMock.getImp()).thenReturn(new LinkedList<Impression>() {
            {
                add(new Impression() {
                    {
                        setPmp(new PMP() {
                            {
                                setDeals(new LinkedList<Deal>() {
                                    {
                                        add(new Deal() {
                                            {
                                                setId("TEST_DEAL_ID");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        Lot49Ext ext = new Lot49Ext();
        ext.setLot49ExtRemote(new Lot49ExtRemote());

        ExchangeAdapter adapter = new OpenXAdapter();

        ext.setAdapter(adapter);
        Mockito.when(reqMock.getLot49Ext()).thenReturn(ext);


        Mockito.when(logMock.getLevel()).thenReturn(Level.DEBUG);

        LogUtils.logRequest(reqMock, false, 0);

        Mockito.verify(logMock, Mockito.times(1))
                        .info(Mockito.eq(""), Mockito.anyString(), Mockito.anyLong(), Mockito
                                        .eq("M0"), Mockito.eq("openx"), Mockito.eq("TEST_REQ_ID"),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq("M1"), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.any(), Mockito.eq(0),
                        Mockito.eq(0), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(false),
                        Mockito.eq("X1"), Mockito.eq("M2"), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(0), Mockito.eq("M3"), Mockito.eq(null), Mockito.eq(null),
                        Mockito.eq(0), Mockito.eq(null), Mockito.eq(null), Mockito.eq("M4"),
                        Mockito.eq(null), Mockito.eq(null), Mockito.eq(null), Mockito.eq(0),
                        Mockito.eq(null), Mockito.eq(0), Mockito.eq(null), Mockito.eq("M5"),
                        Mockito.eq("openx"), Mockito.eq(false), Mockito.eq(false), Mockito.eq(null),
                        Mockito.eq("X2"), Mockito.eq("M6"), Mockito.eq("INTEGRAL_TARGETING[]"),
                        Mockito.any(), Mockito.eq(null), Mockito.eq(null), Mockito.any(),
                        Mockito.eq(null), Mockito.eq("M7"), Mockito.eq(false), Mockito.eq(null),
                        Mockito.eq(0.0f), Mockito.eq(null), Mockito.eq("M8"), Mockito.any(),
                        Mockito.eq("{privateAuction:-1, deals:{ id: TEST_DEAL_ID,auctionType0,floor: 0.0,cur: null,wadomain: null,wseat: null,ext: null }]}"),
                        Mockito.any(), Mockito.eq("local"), Mockito.eq(null));
    }
}
