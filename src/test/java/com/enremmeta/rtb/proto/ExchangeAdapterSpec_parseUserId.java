package com.enremmeta.rtb.proto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.pubmatic.PubmaticAdapter;
import com.enremmeta.rtb.proto.pubmatic.PubmaticConfig;
import com.enremmeta.util.ServiceRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogUtils.class, ServiceRunner.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class ExchangeAdapterSpec_parseUserId {
    private ServiceRunner serviceRunnerSimpleMock;
    private ExchangesConfig exchangesConfigMock;
    private PubmaticConfig pmCfg;
    ExchangeAdapter<OpenRtbRequest, OpenRtbResponse> ea;

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

        pmCfg.setVcode("VCODE");
        ea = new PubmaticAdapter();

    }

    @Test
    public void negativeFlow_userIdIsNull() {
        String userId = null;
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertNull(req.getLot49Ext().getReceivedBuyerUid());

    }

    @Test
    public void negativeFlow_wrongFormatOfUserId() {
        String userId = "WRONG_FOTMAT_OF_USER_ID";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals("WRONG_FOTMAT_OF_USER_ID", req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: WRONG_FOTMAT_OF_USER_ID | Parsed: WRONG_FOTMAT_OF_USER_ID | Decoded: 1B8D1359C04C4EF17FE1FC13FC112151");
    }

    @Test
    public void negativeFlow_idEqualsCOOKIEMONSTER_V2_() {
        String userId = "COOKIEMONSTER_V2_";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals("COOKIEMONSTER_V2_", req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: COOKIEMONSTER_V2_ | Parsed:  | Decoded: null");
    }


    @Test
    public void negativeFlow_idStartsWithCOOKIEMONSTER_V2_() {
        String userId = "COOKIEMONSTER_V2_110101";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals("COOKIEMONSTER_V2_110101", req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: COOKIEMONSTER_V2_110101 | Parsed: 110101 | Decoded: 110101");
    }

    @Test
    public void positiveFlow_32HexCharacterModUid() {
        String userId = "COOKIEMONSTER_V2_a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals("COOKIEMONSTER_V2_a123bc4fb4a123bc4fb4a123bc4fb4ff",
                        req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: COOKIEMONSTER_V2_a123bc4fb4a123bc4fb4a123bc4fb4ff | Parsed: T7wjobwjobQjobRP_7RPvA | Decoded: a123bc4fb4a123bc4fb4a123bc4fb4ff");
    }

    @Test
    public void positiveFlow_sholudSetModUidAndUserId() {
        String userId = "COOKIEMONSTER_V2_a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsAndEndsWithB64() {
        String userId = "B64a123bc4fb4a123bc4fb4a123bc4fb4ffB64";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("B64a123bc4fb4a123bc4fb4a123bc4fb4ffB64",
                        req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithHEX0() {
        String userId = "HEX0a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("HEX0a123bc4fb4a123bc4fb4a123bc4fb4ff",
                        req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithHEX1() {
        String userId = "HEX1a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("HEX1a123bc4fb4a123bc4fb4a123bc4fb4ff",
                        req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithodspEq() {
        String userId = "odsp=a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("odsp=a123bc4fb4a123bc4fb4a123bc4fb4ff",
                        req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithAAAAEq() {
        String userId = "aaaa=a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("aaaa=a123bc4fb4a123bc4fb4a123bc4fb4ff",
                        req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithXX() {
        String userId = "XXa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals("XXa123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void positiveFlow_userIdStartsWithB65() {
        String userId = "B65a123bc4fb4a123bc4fb4a123bc4fb4fffpSOMETHING";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getLot49Ext().getModUid());
        assertEquals("T7wjobwjobQjobRP_7RPvA", req.getUser().getBuyeruid());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithXX() {
        String userId = "XXXXa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XXXXa123bc4fb4a123bc4fb4a123bc4fb4ff | Parsed: a123bc4fb4a123bc4fb4a123bc4fb4ff | Decoded: null");
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn(Mockito.eq("UserId: Cannot parse a123bc4fb4a123bc4fb4a123bc4fb4ff"),
                        Mockito.any(com.enremmeta.rtb.CorruptedUserIdException.class));
        assertEquals(null, req.getLot49Ext().getModUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getUser().getBuyeruid());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithXX_shouldSetForceCookieResetToTrue() {
        String userId = "XXXXa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieReset());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithXX_shouldSetForceCookieResyncToTrue() {
        String userId = "XXXXa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieResync());
    }

    @Test
    public void postiveFlow_notParsedIntegerIdStartsWithXX_idLengthBetween22And25() {
        String userId = "XXXXa123bc4fb4a123bc4fb4a123";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XXXXa123bc4fb4a123bc4fb4a123 | Parsed: a123bc4fb4a123bc4fb4a123 | Decoded: 6DB75D6B866F1FCEDC76DBB56BF8F6E1");
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.warn(Mockito.any(), Mockito.any());

        assertEquals("6DB75D6B866F1FCEDC76DBB56BF8F6E1", req.getLot49Ext().getModUid());
        assertEquals("a123bc4fb4a123bc4fb4a123", req.getUser().getBuyeruid());

        assertFalse(userId, req.getLot49Ext().isForceCookieReset());
        assertFalse(userId, req.getLot49Ext().isForceCookieResync());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithProc22() {
        String userId = "XX%22a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XX%22a123bc4fb4a123bc4fb4a123bc4fb4ff | Parsed: a123bc4fb4a123bc4fb4a123bc4fb4ff | Decoded: null");
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn(Mockito.eq("UserId: Cannot parse a123bc4fb4a123bc4fb4a123bc4fb4ff"),
                        Mockito.any(com.enremmeta.rtb.CorruptedUserIdException.class));
        assertEquals(null, req.getLot49Ext().getModUid());
        assertEquals("a123bc4fb4a123bc4fb4a123bc4fb4ff", req.getUser().getBuyeruid());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithProc22_shouldSetForceCookieResetToTrue() {
        String userId = "XX%22a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieReset());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithProc22_shouldSetForceCookieResyncToTrue() {
        String userId = "XX%22a123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieResync());
    }

    @Test
    public void postiveFlow_notParsedIntegerIdStartsWithProc22_idLengthBetween22And25() {
        String userId = "XX%22a123bc4fb4a123bc4fb4a123";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XX%22a123bc4fb4a123bc4fb4a123 | Parsed: a123bc4fb4a123bc4fb4a123 | Decoded: 6DB75D6B866F1FCEDC76DBB56BF8F6E1");
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.warn(Mockito.any(), Mockito.any());

        assertEquals("6DB75D6B866F1FCEDC76DBB56BF8F6E1", req.getLot49Ext().getModUid());
        assertEquals("a123bc4fb4a123bc4fb4a123", req.getUser().getBuyeruid());

        assertFalse(userId, req.getLot49Ext().isForceCookieReset());
        assertFalse(userId, req.getLot49Ext().isForceCookieResync());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithOther() {
        String userId = "XXYYa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XXYYa123bc4fb4a123bc4fb4a123bc4fb4ff | Parsed: YYa123bc4fb4a123bc4fb4a123bc4fb4ff | Decoded: null");
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.warn(Mockito.eq("UserId: Cannot parse YYa123bc4fb4a123bc4fb4a123bc4fb4ff"),
                        Mockito.any(com.enremmeta.rtb.CorruptedUserIdException.class));
        assertEquals(null, req.getLot49Ext().getModUid());
        assertEquals("YYa123bc4fb4a123bc4fb4a123bc4fb4ff", req.getUser().getBuyeruid());
    }

    @Test
    public void postiveFlow_notParsedIntegerIdStartsWithOther_idLengthBetween22And25() {
        String userId = "XXYYa123bc4fb4a123bc4fb4a1";
        OpenRtbRequest req = new OpenRtbRequest();

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.trace(Mockito.any());
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.warn(Mockito.any(), Mockito.any());

        ea.parseUserId(userId, req);

        assertEquals(userId, req.getLot49Ext().getReceivedBuyerUid());
        PowerMockito.verifyStatic(Mockito.times(1));
        LogUtils.trace("UserId: Parsing result. Received: XXYYa123bc4fb4a123bc4fb4a1 | Parsed: YYa123bc4fb4a123bc4fb4a1 | Decoded: DBB58661F6E1DC76B75D6BF86F1FCE6D");
        PowerMockito.verifyStatic(Mockito.never());
        LogUtils.warn(Mockito.any(), Mockito.any());

        assertEquals("DBB58661F6E1DC76B75D6BF86F1FCE6D", req.getLot49Ext().getModUid());
        assertEquals("YYa123bc4fb4a123bc4fb4a1", req.getUser().getBuyeruid());

        assertFalse(userId, req.getLot49Ext().isForceCookieReset());
        assertFalse(userId, req.getLot49Ext().isForceCookieResync());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithOther_shouldSetForceCookieResetToTrue() {
        String userId = "XXYYa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieReset());
    }

    @Test
    public void negativeFlow_notParsedIntegerIdStartsWithOther_shouldSetForceCookieResyncToTrue() {
        String userId = "XXYYa123bc4fb4a123bc4fb4a123bc4fb4ff";
        OpenRtbRequest req = new OpenRtbRequest();

        ea.parseUserId(userId, req);

        assertTrue(userId, req.getLot49Ext().isForceCookieResync());
    }

}
