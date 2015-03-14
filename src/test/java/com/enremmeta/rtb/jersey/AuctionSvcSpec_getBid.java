package com.enremmeta.rtb.jersey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LocalOrchestrator;
import com.enremmeta.rtb.OrchestratorConfig;
import com.enremmeta.rtb.SharedSetUp;
import com.enremmeta.rtb.api.Ad;
import com.enremmeta.rtb.api.FixedDimension;
import com.enremmeta.rtb.api.Tag;
import com.enremmeta.rtb.api.proto.openrtb.Banner;
import com.enremmeta.rtb.api.proto.openrtb.Bid;
import com.enremmeta.rtb.api.proto.openrtb.Impression;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuctionsSvc.class, ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class AuctionSvcSpec_getBid {

    private ServiceRunner serviceRunnerSimpleMock;
    private OrchestratorConfig orchConfig;

    @Before
    public void setUp() throws Exception {

        serviceRunnerSimpleMock = Mockito.mock(ServiceRunner.class, Mockito.CALLS_REAL_METHODS);

        Lot49Config configMock = Mockito.mock(Lot49Config.class);

        ExchangesConfig exchangesConfig = new ExchangesConfig();
        SharedSetUp.prepareExchanges(exchangesConfig);
        Mockito.when(configMock.getExchanges()).thenReturn(exchangesConfig);
        Mockito.when(configMock.getStatsUrl()).thenReturn("hhtp://stats.url");

        orchConfig = new OrchestratorConfig();

        Mockito.when(serviceRunnerSimpleMock.getConfig()).thenReturn(configMock);
        Mockito.when(serviceRunnerSimpleMock.getOrchestrator())
                        .thenReturn(new LocalOrchestrator(orchConfig));

        PowerMockito.mockStatic(ServiceRunner.class);
        Mockito.when(ServiceRunner.getInstance()).thenReturn(serviceRunnerSimpleMock);
    }

    @Test
    public void negativeFlow_No_Impressions() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "tags", new LinkedList<Tag>());

        OpenRtbRequest req = new OpenRtbRequest();
        req.setId("TEST_REQ_ID");
        req.getLot49Ext().setAdapter(new AdaptvAdapter());

        Bid result = Whitebox.invokeMethod(AuctionsSvc.class, "getBid", ad, req);

        assertNull(result);

        assertEquals(Lot49Constants.DECISION_NO_IMPRESSIONS,
                        ((Map<?, ?>) Whitebox.getInternalState(ad, "uniqOptoutReasonMap"))
                                        .get("adaptv_TEST_REQ_ID"));
    }

    @SuppressWarnings("serial")
    @Test
    public void negativeFlow_No_Potential_Bids_0_Tags() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "tags", new LinkedList<Tag>());

        OpenRtbRequest req = new OpenRtbRequest();
        req.setId("TEST_REQ_ID");
        req.getLot49Ext().setAdapter(new AdaptvAdapter());

        req.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });

        Bid result = Whitebox.invokeMethod(AuctionsSvc.class, "getBid", ad, req);

        assertNull(result);

        assertEquals(Lot49Constants.DECISION_TAG,
                        ((Map<?, ?>) Whitebox.getInternalState(ad, "uniqOptoutReasonMap"))
                                        .get("adaptv_TEST_REQ_ID"));
        assertEquals("Out of 0 tags:", req.getLot49Ext().getOptoutReasons().get(ad.getId()).trim());
    }

    @SuppressWarnings("serial")
    @Test
    public void negativeFlow_No_Potential_Bids_1_Tags() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_fake();
        Tag tag = new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);

        Whitebox.setInternalState(ad, "tags", new LinkedList<Tag>() {
            {
                add(tag);
            }
        });

        OpenRtbRequest req = new OpenRtbRequest();
        req.setId("TEST_REQ_ID");
        req.getLot49Ext().setAdapter(new AdaptvAdapter());

        req.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });

        Bid result = Whitebox.invokeMethod(AuctionsSvc.class, "getBid", ad, req);

        assertNull(result);

        assertEquals(Lot49Constants.DECISION_TAG,
                        ((Map<?, ?>) Whitebox.getInternalState(ad, "uniqOptoutReasonMap"))
                                        .get("adaptv_TEST_REQ_ID"));
        assertEquals("Out of 1 tags: 2002002: Unknown",
                        req.getLot49Ext().getOptoutReasons().get(ad.getId()).trim());
    }

    @SuppressWarnings("serial")
    @Test
    public void negativeFlow_No_Potential_Bids_Not_Banner() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_fake();
        Tag tag = new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);
        Whitebox.setInternalState(tag, "banner", true);

        Whitebox.setInternalState(ad, "tags", new LinkedList<Tag>() {
            {
                add(tag);
            }
        });

        OpenRtbRequest req = new OpenRtbRequest();
        req.setId("TEST_REQ_ID");
        req.getLot49Ext().setAdapter(new AdaptvAdapter());

        req.setImp(new LinkedList<Impression>() {
            {
                add(new Impression());
            }
        });

        Bid result = Whitebox.invokeMethod(AuctionsSvc.class, "getBid", ad, req);

        assertNull(result);

        assertEquals(Lot49Constants.DECISION_TAG,
                        ((Map<?, ?>) Whitebox.getInternalState(ad, "uniqOptoutReasonMap"))
                                        .get("adaptv_TEST_REQ_ID"));
        assertEquals("Out of 1 tags: 2002002: Not banner",
                        req.getLot49Ext().getOptoutReasons().get(ad.getId()).trim());
    }

    @SuppressWarnings("serial")
    @Test
    public void positiveFlow_resultingBidHoldsAdAndTagAttributes() throws Exception {
        Ad ad = new SharedSetUp.Ad_1001001_fake();
        Whitebox.setInternalState(ad, "adomain", new LinkedList<String>() {
            {
                add("myip.io");
            }
        });

        Tag tag = new SharedSetUp.Tag_2002002_tagMarker_1001001_fake(ad);
        Whitebox.setInternalState(tag, "banner", true);
        Whitebox.setInternalState(tag, "dim", new FixedDimension(200, 100));
        Whitebox.setInternalState(tag, "clickRedir", "http//click-redir-test.io");

        Whitebox.setInternalState(ad, "tags", new LinkedList<Tag>() {
            {
                add(tag);
            }
        });

        OpenRtbRequest req = new OpenRtbRequest();
        req.setId("TEST_REQ_ID");
        req.getLot49Ext().setAdapter(new AdaptvAdapter());

        Impression imp = new Impression();
        Banner banner = new Banner();
        banner.setH(100);
        banner.setW(200);
        imp.setBanner(banner);
        req.setImp(new LinkedList<Impression>() {
            {
                add(imp);
            }
        });

        Bid result = Whitebox.invokeMethod(AuctionsSvc.class, "getBid", ad, req);

        assertNotNull(result);

        // Campaign ID
        assertEquals(ad.getId(), result.getCid());

        assertEquals(ad.getAdomain(), result.getAdomain());

        // Creative ID
        assertEquals(tag.getId(), result.getCrid());

        assertEquals("<div>test tag</div>", result.getAdm());

    }

}
