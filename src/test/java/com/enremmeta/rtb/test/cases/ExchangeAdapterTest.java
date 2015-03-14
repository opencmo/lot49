package com.enremmeta.rtb.test.cases;

import java.util.Arrays;

import org.junit.Test;

import com.enremmeta.rtb.Bidder;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbRequest;
import com.enremmeta.rtb.api.proto.openrtb.User;
import com.enremmeta.rtb.config.ExchangesConfig;
import com.enremmeta.rtb.config.Lot49Config;
import com.enremmeta.rtb.proto.ExchangeAdapter;
import com.enremmeta.rtb.proto.adaptv.AdaptvAdapter;
import com.enremmeta.rtb.proto.adaptv.AdaptvConfig;
import com.enremmeta.rtb.proto.testexchange.Test1ExchangeAdapter;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ExchangeAdapterTest extends TestCase {

    @Test
    public void testAdaptvCorruptedCookies() {
        String cookies[] = new String[] {
                        "rB4An1XwPzGF8RtVvsAaAg%25252525252525252525252525252525252525253D%25252525252525252525252525252525252525253D",
                        "CrVYGVWf%2525252525252525252525252525252525252525252525252525252525252525252525252525252F71RpW%25252525252525252525252525252525252525252525252525252525252525252525252525252BaIE1QAg%2525252525252525252525252525252525252525252525252525252525252525252525252525253D%2525252525252525252525252525252525252525252525252525252525252525252525252525253D",
                        "CrVYGVWTSZF9QgZgW%252B73Ag%25253D%25253D",
                        "rB4An1XMvsBsxVoszvuhAg%252525253D%252525253D",
                        "rB4An1YIrD2ZIEZH9AOFAg%252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D%252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D",
                        "rB4An1XNEY9sxVosD%25252525252525252525252525252525252525252BJTAg%2525252525252525252525252525252525252525253D%2525252525252525252525252525252525252525253D",
                        "CtQF91T67%2525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252FtjYkMXvw0pAg%2525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D%2525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D",
                        "CtQF91TtVuYSxwEvW0vaAg%25252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D%25252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525252525253D"};

        Lot49Config config = new Lot49Config();
        config.setExchanges(new ExchangesConfig());
        config.getExchanges().setAdaptv(new AdaptvConfig());
        config.getExchanges().getAdaptv().setBuyerId("test");
        try {
            Class.forName(Bidder.class.getName());
        } catch (ClassNotFoundException cnfe) {

        }
        Bidder.getInstance().setConfig(config);
        AdaptvAdapter a = new AdaptvAdapter();
        for (String cookie : cookies) {
            OpenRtbRequest req = new OpenRtbRequest();
            req.setUser(new User());
            try {
                a.parseUserId(cookie, req);
                String modUid = req.getLot49Ext().getModUid();
                assertNotNull("Failed on " + cookie, modUid);
                System.out.println(modUid);
            } catch (Throwable t) {
                t.printStackTrace();
                fail(t.getMessage());
            }

        }
    }

    @Test
    public void testUserParsing2() {
        String[] hexes = new String[] {"1705D40A9D3154DD6F81CCE12484C762",
                        "9F001EACD2EC9B56EF0AC948025EFBA6", "9F001EAC20CF8356CD4D43140221D694",
                        "9F001EACAAAD9656886A4F280244F655", "9F001EAC56D297568D6A3A36028DD6B0",
                        "9F001EAC50359056CD63050402D303B7", "9F001EAC6AF39756816A661402E53BB9",
                        "9F001EAC65CA8056CD4D431402FEB6B6"};
        String[] cookies = new String[] {"CtQFF91UMZ3hzIFvYseEJA", "rB4An1ab7NJIyQrvpvteAg",
                        "rB4An1aDzyAUQ03NlNYhAg", "rB4An1aWraooT2qIVfZEAg",
                        "rB4An1aX0lY2OmqNsNaNAg", "rB4An1aQNVAEBWPNtwPTAg",
                        "rB4An1aX82oUZmqBuTvlAg", "rB4An1aAymUUQ03Ntrb-Ag"};
        Assert.assertEquals(hexes.length, cookies.length);
        ExchangeAdapter adapter = new Test1ExchangeAdapter();
        for (int i = 0; i < hexes.length; i++) {

            OpenRtbRequest req = new OpenRtbRequest();
            User user = new User();
            req.setUser(user);
            adapter.parseUserId(hexes[i], req);
            assertEquals(cookies[i], user.getBuyeruid());
            assertEquals(hexes[i], req.getLot49Ext().getModUid());
            assertEquals(hexes[i], req.getLot49Ext().getReceivedBuyerUid());

            req = new OpenRtbRequest();
            user = new User();
            req.setUser(user);
            adapter.parseUserId(cookies[i], req);
            assertEquals(cookies[i], user.getBuyeruid());
            assertEquals(hexes[i], req.getLot49Ext().getModUid());
            assertEquals(cookies[i], req.getLot49Ext().getReceivedBuyerUid());
        }

    }

    @Test
    public void testUserParsing() {
        ExchangeAdapter adapter = new Test1ExchangeAdapter();

        // Data taken by doing
        // cut -f7,102,89 request.log |more
        // BuyerUid - field 7
        // ModUid - field 102
        // Received - field 89

        String[][] testData = {
                        {"rB4An1ZrZakAKBvkSD7ZAg", "9F001EACA9656B56E41B280002D93E48",
                                        "HEX09F001EACA9656B56E41B280002D93E48"},
                        {"rB4An1Zs0a6o0hvYwVRXAg", "9F001EACAED16C56D81BD2A8025754C1",
                                        "HEX09F001EACAED16C56D81BD2A8025754C1",},

                        {"rB4An1ZsxDDAsxvjvbiFAg", "9F001EAC30C46C56E31BB3C00285B8BD",
                                        "HEX09F001EAC30C46C56E31BB3C00285B8BD"},
                        {"rB4An1ZfavN3i2idYVRoAg", "9F001EACF36A5F569D688B7702685461",

                                        "HEX09F001EACF36A5F569D688B7702685461",

                        }};

        for (String[] reqData : testData) {
            String assertMsg = "Error with the triple " + Arrays.asList(reqData);
            String buyerUid = reqData[0];
            String modUid = reqData[1];
            String receivedUid = reqData[2];

            // Test 1.
            OpenRtbRequest req = new OpenRtbRequest();
            User user = new User();
            req.setUser(user);
            adapter.parseUserId(buyerUid, req);
            assertEquals(assertMsg, buyerUid, user.getBuyeruid());
            assertEquals(assertMsg, modUid, req.getLot49Ext().getModUid());
            assertEquals(assertMsg, buyerUid, req.getLot49Ext().getReceivedBuyerUid());

            // Test 2
            req = new OpenRtbRequest();
            user = new User();
            req.setUser(user);
            adapter.parseUserId(modUid, req);
            assertEquals(assertMsg, buyerUid, user.getBuyeruid());
            assertEquals(assertMsg, modUid, req.getLot49Ext().getModUid());
            assertEquals(assertMsg, modUid, req.getLot49Ext().getReceivedBuyerUid());

            // Test 3
            req = new OpenRtbRequest();
            user = new User();
            req.setUser(user);
            adapter.parseUserId(receivedUid, req);
            assertEquals(assertMsg, buyerUid, user.getBuyeruid());
            assertEquals(assertMsg, modUid, req.getLot49Ext().getModUid());
            assertEquals(assertMsg, receivedUid, req.getLot49Ext().getReceivedBuyerUid());
        }
    }
}
